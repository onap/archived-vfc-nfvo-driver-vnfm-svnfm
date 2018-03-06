/*
 * Copyright 2016-2017, Nokia Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm;

import com.google.common.io.ByteStreams;
import org.eclipse.jetty.server.NetworkTrafficServerConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api.VnfmInfoProvider;
import org.onap.vnfmdriver.model.VnfmInfo;
import org.springframework.http.HttpStatus;

import javax.net.ssl.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static junit.framework.TestCase.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

class HttpTestServer {
    Server _server;
    List<String> requests = new ArrayList<>();
    List<Integer> codes = new ArrayList<>();
    List<String> respones = new ArrayList<>();

    public void start() throws Exception {
        configureServer();
        startServer();
    }

    private void startServer() throws Exception {
        requests.clear();
        codes.clear();
        _server.start();
    }

    protected void configureServer() throws Exception {
        Path jksPath = Paths.get(TestCbamTokenProvider.class.getResource("/unittests/localhost.jks").toURI());
        String path = jksPath.normalize().toAbsolutePath().toUri().toString();
        _server = new Server();
        SslContextFactory factory = new SslContextFactory(path);
        factory.setKeyStorePassword("changeit");
        NetworkTrafficServerConnector connector = new NetworkTrafficServerConnector(_server, factory);
        connector.setHost("127.0.0.1");
        _server.addConnector(connector);
        _server.setHandler(new AbstractHandler() {
            @Override
            public void handle(String target, org.eclipse.jetty.server.Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
                requests.add(new String(ByteStreams.toByteArray(request.getInputStream())));
                httpServletResponse.getWriter().write(respones.remove(0));
                httpServletResponse.setStatus(codes.remove(0));
                request.setHandled(true);
            }
        });
    }

    public void stop() throws Exception {
        _server.stop();
    }
}

public class TestCbamTokenProvider extends TestBase {

    private static String GOOD_RESPONSE = "{ \"access_token\" : \"myToken\", \"expires_in\" : 1000 }";
    @InjectMocks
    private CbamTokenProvider cbamTokenProvider;
    private VnfmInfo vnfmInfo = new VnfmInfo();
    private ArgumentCaptor<SSLSocketFactory> sslSocketFactory = ArgumentCaptor.forClass(SSLSocketFactory.class);
    private ArgumentCaptor<HostnameVerifier> hostnameVerifier = ArgumentCaptor.forClass(HostnameVerifier.class);
    private HttpTestServer testServer;

    @Before
    public void initMocks() throws Exception {
        setField(CbamTokenProvider.class, "logger", logger);
        setField(cbamTokenProvider, "username", "myUserName");
        setField(cbamTokenProvider, "password", "myPassword");
        setField(cbamTokenProvider, "skipCertificateVerification", true);
        setField(cbamTokenProvider, "skipHostnameVerification", true);
        when(vnfmInfoProvider.getVnfmInfo(VNFM_ID)).thenReturn(vnfmInfo);
        vnfmInfo.setPassword("vnfmPassword");
        vnfmInfo.setUserName("vnfmUserName");
        vnfmInfo.setUrl("http://127.0.0.3:12345");
        testServer = new HttpTestServer();
        testServer.start();
        URI uri = testServer._server.getURI();
        setField(cbamTokenProvider, "cbamKeyCloakBaseUrl", uri.toString());
    }

    private void addGoodTokenResponse() {
        testServer.respones.add(GOOD_RESPONSE);
        testServer.codes.add(HttpStatus.OK.value());
    }

    @After
    public void testServer() throws Exception {
        testServer.stop();
    }

    /**
     * a new token is requested no token has been requested before
     */
    @Test
    public void testBasicTokenRequest() throws Exception {
        //given
        addGoodTokenResponse();
        //when
        String token = cbamTokenProvider.getToken(VNFM_ID);
        //verify
        assertEquals(1, testServer.requests.size());
        assertTokenRequest(testServer.requests.get(0));
        assertEquals("myToken", token);

    }

    /**
     * a new token is requested if the previous token has expired
     */
    @Test
    public void testTokenIsRequestedIfPreviousExpired() throws Exception {
        //given
        addGoodTokenResponse();
        String firstToken = cbamTokenProvider.getToken(VNFM_ID);
        testServer.respones.add("{ \"access_token\" : \"myToken2\", \"expires_in\" : 2000 }");
        testServer.codes.add(HttpStatus.OK.value());
        when(systemFunctions.currentTimeMillis()).thenReturn(500L * 1000 + 1L);
        //when
        String token = cbamTokenProvider.getToken(VNFM_ID);
        //verify
        assertEquals(2, testServer.requests.size());
        assertTokenRequest(testServer.requests.get(0));
        assertTokenRequest(testServer.requests.get(1));
        assertEquals("myToken2", token);
    }

    /**
     * a new token is not requested if the previous token has not expired
     */
    @Test
    public void testTokenIsNotRequestedIfPreviousHasNotExpired() throws Exception {
        //given
        addGoodTokenResponse();
        String firstToken = cbamTokenProvider.getToken(VNFM_ID);
        testServer.respones.add("{ \"access_token\" : \"myToken2\", \"expires_in\" : 2000 }");
        testServer.codes.add(HttpStatus.OK.value());
        when(systemFunctions.currentTimeMillis()).thenReturn(500L * 1000);
        //when
        String token = cbamTokenProvider.getToken(VNFM_ID);
        //verify
        assertEquals(1, testServer.requests.size());
        assertTokenRequest(testServer.requests.get(0));
        assertEquals("myToken", token);
    }

    /**
     * failed token requests are retried for a fixed number amount of times
     */
    @Test
    public void testRetry() throws Exception {
        //given
        addFailedResponse();
        addFailedResponse();
        addFailedResponse();
        addFailedResponse();
        addGoodTokenResponse();
        //cbamTokenProvider.failOnRequestNumber = 5;
        //when
        String token = cbamTokenProvider.getToken(VNFM_ID);
        //verify
        assertEquals(5, testServer.requests.size());
        assertTokenRequest(testServer.requests.get(0));
        assertTokenRequest(testServer.requests.get(1));
        assertTokenRequest(testServer.requests.get(2));
        assertTokenRequest(testServer.requests.get(3));
        assertTokenRequest(testServer.requests.get(4));
        verify(logger).warn(eq("Unable to get token to access CBAM API (1/5)"), Mockito.<RuntimeException>any());
        verify(logger).warn(eq("Unable to get token to access CBAM API (2/5)"), Mockito.<RuntimeException>any());
        verify(logger).warn(eq("Unable to get token to access CBAM API (3/5)"), Mockito.<RuntimeException>any());
        verify(logger).warn(eq("Unable to get token to access CBAM API (4/5)"), Mockito.<RuntimeException>any());
        assertEquals("myToken", token);
    }

    /**
     * failed token requests are retried for a fixed number amount of times (reacing maximal number or retries)
     */
    @Test
    public void testNoMoreRetry() throws Exception {
        //given
        addFailedResponse();
        addFailedResponse();
        addFailedResponse();
        addFailedResponse();
        addFailedResponse();
        //when
        try {
            cbamTokenProvider.getToken(VNFM_ID);
            fail();
        } catch (RuntimeException e) {
            assertNotNull(e.getCause());
        }
        //verify
        assertEquals(5, testServer.requests.size());
        assertTokenRequest(testServer.requests.get(0));
        assertTokenRequest(testServer.requests.get(1));
        assertTokenRequest(testServer.requests.get(2));
        assertTokenRequest(testServer.requests.get(3));
        assertTokenRequest(testServer.requests.get(4));
        verify(logger).warn(eq("Unable to get token to access CBAM API (1/5)"), Mockito.<RuntimeException>any());
        verify(logger).warn(eq("Unable to get token to access CBAM API (2/5)"), Mockito.<RuntimeException>any());
        verify(logger).warn(eq("Unable to get token to access CBAM API (3/5)"), Mockito.<RuntimeException>any());
        verify(logger).warn(eq("Unable to get token to access CBAM API (4/5)"), Mockito.<RuntimeException>any());
        verify(logger).error(eq("Unable to get token to access CBAM API (giving up retries)"), Mockito.<RuntimeException>any());
    }

    private void addFailedResponse() {
        testServer.codes.add(HttpStatus.UNAUTHORIZED.value());
        testServer.respones.add(new String());
    }

    /**
     * the SSL connection is established without certificate & hostname verification
     */
    @Test
    public void noSslVerification() throws Exception {
        //given
        //the default settings is no SSL & hostname check
        addGoodTokenResponse();
        //when
        cbamTokenProvider.getToken(VNFM_ID);
        //verify
        //no exception is thrown
    }

    /**
     * if SSL is verified the certificates must be defined
     */
    @Test
    public void testInvalidCombinationOfSettings() throws Exception {
        //given
        setField(cbamTokenProvider, "skipCertificateVerification", false);
        //when
        try {
            cbamTokenProvider.getToken(VNFM_ID);
            //verify
            fail();
        } catch (RuntimeException e) {
            assertEquals("If the skipCertificateVerification is set to false (default) the trustedCertificates can not be empty", e.getMessage());
        }
    }

    /**
     * if SSL is verified the certificates must be defined
     */
    @Test
    public void testInvalidCombinationOfSettings2() throws Exception {
        //given
        setField(cbamTokenProvider, "skipCertificateVerification", false);
        setField(cbamTokenProvider, "trustedCertificates", "xx\nxx");
        //when
        try {
            cbamTokenProvider.getToken(VNFM_ID);
            //verify
            fail();
        } catch (RuntimeException e) {
            assertEquals("The trustedCertificates must be a base64 encoded collection of PEM certificates", e.getMessage());
            assertNotNull(e.getCause());
        }
    }

    /**
     * the SSL connection is established without certificate & hostname verification
     */
    @Test
    public void testNotTrustedSslConnection() throws Exception {
        //given
        setField(cbamTokenProvider, "skipCertificateVerification", false);
        Path caPem = Paths.get(TestCbamTokenProvider.class.getResource("/unittests/sample.cert.pem").toURI());
        setField(cbamTokenProvider, "trustedCertificates", Base64.getEncoder().encodeToString(Files.readAllBytes(caPem)));
        addGoodTokenResponse();
        //when
        try {
            cbamTokenProvider.getToken(VNFM_ID);
            //verify
            fail();
        } catch (RuntimeException e) {
            assertTrue(e.getCause().getCause().getMessage().contains("unable to find valid certification path"));
            assertTrue(e.getCause() instanceof SSLHandshakeException);
        }
    }

    /**
     * the SSL connection is established with certificate & hostname verification
     */
    @Test
    public void testHostnameVerificationSucceeds() throws Exception {
        //given
        setField(cbamTokenProvider, "skipCertificateVerification", false);
        Path caPem = Paths.get(TestCbamTokenProvider.class.getResource("/unittests/localhost.cert.pem").toURI());
        setField(cbamTokenProvider, "trustedCertificates", Base64.getEncoder().encodeToString(Files.readAllBytes(caPem)));
        setField(cbamTokenProvider, "cbamKeyCloakBaseUrl", testServer._server.getURI().toString().replace("127.0.0.1", "localhost"));
        setField(cbamTokenProvider, "skipHostnameVerification", false);
        addGoodTokenResponse();
        //when
        cbamTokenProvider.getToken(VNFM_ID);
        //verify
        //no seception is thrown
    }

    /**
     * the SSL connection is dropped with certificate & hostname verification due to invalid hostname
     */
    @Test
    public void testHostnameverifcationfail() throws Exception {
        //given
        setField(cbamTokenProvider, "skipCertificateVerification", false);
        Path caPem = Paths.get(TestCbamTokenProvider.class.getResource("/unittests/localhost.cert.pem").toURI());
        setField(cbamTokenProvider, "trustedCertificates", Base64.getEncoder().encodeToString(Files.readAllBytes(caPem)));
        setField(cbamTokenProvider, "skipHostnameVerification", false);
        addGoodTokenResponse();
        //when
        try {
            cbamTokenProvider.getToken(VNFM_ID);
            //verify
            fail();
        } catch (RuntimeException e) {
            assertTrue(e.getCause().getMessage().contains("Hostname 127.0.0.1 not verified"));
            assertTrue(e.getCause() instanceof SSLPeerUnverifiedException);
        }
    }

    /**
     * invalid certificate content
     */
    @Test
    public void testInvalidCerificateContent() throws Exception {
        //given
        setField(cbamTokenProvider, "skipCertificateVerification", false);
        setField(cbamTokenProvider, "trustedCertificates", Base64.getEncoder().encodeToString("-----BEGIN CERTIFICATE-----\nkuku\n-----END CERTIFICATE-----\n".getBytes()));
        setField(cbamTokenProvider, "skipHostnameVerification", false);
        addGoodTokenResponse();
        //when
        try {
            cbamTokenProvider.getToken(VNFM_ID);
            //verify
            fail();
        } catch (RuntimeException e) {
            assertEquals("Unable to load certificates", e.getMessage());
            assertTrue(e.getCause() instanceof GeneralSecurityException);
        }
    }

    /**
     * Verify client certificates are not verified
     * \
     */
    @Test
    public void testClientCertificates() throws Exception {
        //when
        new CbamTokenProvider.AllTrustedTrustManager().checkClientTrusted(null, null);
        //verify
        //no security exception is thrown
    }

    /**
     * Exception during keystore creation is logged (semi-useless)
     */
    @Test
    public void testKeystoreCreationFailure() {
        KeyStoreException expectedException = new KeyStoreException();
        class X extends CbamTokenProvider {
            X(VnfmInfoProvider vnfmInfoProvider) {
                super(vnfmInfoProvider);
            }

            @Override
            TrustManager[] buildTrustManager() throws KeyStoreException {
                throw expectedException;
            }
        }
        try {
            new X(null).buildSSLSocketFactory();
            fail();
        } catch (RuntimeException e) {
            assertEquals(expectedException, e.getCause());
            verify(logger).error("Unable to create SSL socket factory", expectedException);
        }
    }

    private void assertTokenRequest(String body) {
        assertContains(body, "grant_type", "password");
        assertContains(body, "client_id", "vnfmUserName");
        assertContains(body, "client_secret", "vnfmPassword");
        assertContains(body, "username", "myUserName");
        assertContains(body, "password", "myPassword");
    }

    private void assertContains(String content, String key, String value) {
        assertTrue(content.contains(key + "=" + value));
    }
}
