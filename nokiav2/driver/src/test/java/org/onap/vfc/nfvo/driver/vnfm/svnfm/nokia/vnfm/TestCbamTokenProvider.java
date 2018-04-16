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

import java.io.IOException;
import java.net.URI;
import okhttp3.Interceptor;
import okhttp3.Request;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.onap.vnfmdriver.model.VnfmInfo;
import org.springframework.http.HttpStatus;

import static junit.framework.TestCase.*;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class TestCbamTokenProvider extends TestBase {

    private static String GOOD_RESPONSE = "{ \"access_token\" : \"myToken\", \"expires_in\" : 1000 }";
    @InjectMocks
    private CbamTokenProvider cbamTokenProvider;
    private VnfmInfo vnfmInfo = new VnfmInfo();
    private HttpTestServer testServer;

    public static String extractToken(Interceptor token) throws IOException {
        Interceptor.Chain chain = Mockito.mock(Interceptor.Chain.class);
        Request request = new Request.Builder().url("http://127.0.0.0/").build();
        when(chain.request()).thenReturn(request);
        ArgumentCaptor<Request> re = ArgumentCaptor.forClass(Request.class);
        when(chain.proceed(re.capture())).thenReturn(null);
        token.intercept(chain);
        return re.getValue().header("Authorization").replaceFirst("Bearer ", "");
    }

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
        String token = extractToken(cbamTokenProvider.getToken(VNFM_ID));
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
        String firstToken = extractToken(cbamTokenProvider.getToken(VNFM_ID));
        testServer.respones.add("{ \"access_token\" : \"myToken2\", \"expires_in\" : 2000 }");
        testServer.codes.add(HttpStatus.OK.value());
        when(systemFunctions.currentTimeMillis()).thenReturn(500L * 1000 + 1L);
        //when
        String token = extractToken(cbamTokenProvider.getToken(VNFM_ID));
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
        String firstToken = extractToken(cbamTokenProvider.getToken(VNFM_ID));
        testServer.respones.add("{ \"access_token\" : \"myToken2\", \"expires_in\" : 2000 }");
        testServer.codes.add(HttpStatus.OK.value());
        when(systemFunctions.currentTimeMillis()).thenReturn(500L * 1000);
        //when
        String token = extractToken(cbamTokenProvider.getToken(VNFM_ID));
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
        String token = extractToken(cbamTokenProvider.getToken(VNFM_ID));
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
