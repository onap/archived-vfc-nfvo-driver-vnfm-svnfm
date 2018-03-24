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

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import com.nokia.cbam.lcn.v32.JSON;
import io.reactivex.Observable;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Headers;

import static junit.framework.TestCase.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

interface TestService {
    @Headers({
            "Content-Type:application/json"
    })
    @GET("subscriptions")
    Observable<TestResource> subscriptionsGet();
}

@XmlRootElement(name = "Subscription")
@XmlAccessorType(XmlAccessType.FIELD)
class TestResource {
    @XmlElement(name = "id")
    @SerializedName("id")
    public String id = null;
}

class GsonCustomConverterFactory extends Converter.Factory {
    private final Gson gson;
    private final GsonConverterFactory gsonConverterFactory;

    private GsonCustomConverterFactory(Gson gson) {
        if (gson == null)
            throw new NullPointerException("gson == null");
        this.gson = gson;
        this.gsonConverterFactory = GsonConverterFactory.create(gson);
    }

    public static GsonCustomConverterFactory create(Gson gson) {
        return new GsonCustomConverterFactory(gson);
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        if (type.equals(String.class))
            return new GsonResponseBodyConverterToString<Object>(gson, type);
        else
            return gsonConverterFactory.responseBodyConverter(type, annotations, retrofit);
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        return gsonConverterFactory.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit);
    }
}

class GsonResponseBodyConverterToString<T> implements Converter<ResponseBody, T> {
    private final Gson gson;
    private final Type type;

    GsonResponseBodyConverterToString(Gson gson, Type type) {
        this.gson = gson;
        this.type = type;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        String returned = value.string();
        try {
            return gson.fromJson(returned, type);
        } catch (JsonParseException e) {
            return (T) returned;
        }
    }
}

public class TestGenericSecurityProvider extends TestBase {
    GenericSecurityProvider securityProvider = new CbamSecurityProvider() {
    };

    HttpTestServer testServer = new HttpTestServer();
    String url;

    @Before
    public void init() throws Exception {
        setField(securityProvider, "skipCertificateVerification", true);
        setField(securityProvider, "skipHostnameVerification", true);
        testServer = new HttpTestServer();
        testServer.start();
        url = testServer._server.getURI().toString();
    }

    @After
    public void testServer() throws Exception {
        testServer.stop();
    }

    /**
     * test skipping certificate and skipping hostname verification
     */
    @Test
    public void testSkipHostAndSkipCertifiacateVerification() throws Exception {
        setField(securityProvider, "skipCertificateVerification", true);
        setField(securityProvider, "skipHostnameVerification", true);
        //when
        TestResource testResource = fireRequest();
        //verify
        assertEquals("1234", testResource.id);
        //when
        securityProvider.buildTrustManager().checkClientTrusted(null, null);
        //verify
        //no security exception is thrown
    }

    /**
     * test skipping certificate and doing hostname verification
     */
    @Test
    public void testHostAndSkipCertifiacateVerification() throws Exception {
        setField(securityProvider, "skipCertificateVerification", true);
        setField(securityProvider, "skipHostnameVerification", false);
        url = url.replace("127.0.0.1", "localhost");
        TestResource testResource = fireRequest();
        assertEquals("1234", testResource.id);
    }

    /**
     * test skipping certificate and doing hostname verification
     * (if hostname is invalid exception is propagated)
     */
    @Test
    public void testHostAndSkipCertifiacateVerificationNegativeCase() throws Exception {
        setField(securityProvider, "skipCertificateVerification", true);
        setField(securityProvider, "skipHostnameVerification", false);
        //url = url.replace("127.0.0.1", "localhost");
        try {
            fireRequest();
            fail();
        } catch (Exception e) {
            assertEquals(javax.net.ssl.SSLPeerUnverifiedException.class, e.getCause().getClass());
            assertTrue(e.getCause().getMessage().contains("Hostname 127.0.0.1 not verified"));
        }
    }

    /**
     * test certificate and hostname verification
     */
    @Test
    public void testHostAndCertifiacateVerification() throws Exception {
        Path jksPath = Paths.get(TestCbamTokenProvider.class.getResource("/unittests/localhost.cert.pem").toURI());
        String cert = Base64.getEncoder().encodeToString(Files.readAllBytes(jksPath));
        setField(securityProvider, "trustedCertificates", cert);
        setField(securityProvider, "skipCertificateVerification", false);
        setField(securityProvider, "skipHostnameVerification", false);
        url = url.replace("127.0.0.1", "localhost");
        TestResource testResource = fireRequest();
        assertEquals("1234", testResource.id);
    }

    /**
     * test certificate and hostname verification
     * (not trusted certificate)
     */
    @Test
    public void testHostAndCertifiacateVerificationNegative() throws Exception {
        Path jksPath = Paths.get(TestCbamTokenProvider.class.getResource("/unittests/sample.cert.pem").toURI());
        String cert = Base64.getEncoder().encodeToString(Files.readAllBytes(jksPath));
        setField(securityProvider, "trustedCertificates", cert);
        setField(securityProvider, "skipCertificateVerification", false);
        setField(securityProvider, "skipHostnameVerification", false);
        url = url.replace("127.0.0.1", "localhost");
        try {
            fireRequest();
            fail();
        } catch (Exception e) {
            assertEquals(javax.net.ssl.SSLHandshakeException.class, e.getCause().getClass());
            assertTrue(e.getCause().getMessage().contains("unable to find valid certification path to requested target"));
        }
    }

    /**
     * test certificate and hostname verification
     */
    @Test
    public void testSkipHostAndCertifiacateVerification() throws Exception {
        Path jksPath = Paths.get(TestCbamTokenProvider.class.getResource("/unittests/localhost.cert.pem").toURI());
        String cert = Base64.getEncoder().encodeToString(Files.readAllBytes(jksPath));
        setField(securityProvider, "trustedCertificates", cert);
        setField(securityProvider, "skipCertificateVerification", false);
        setField(securityProvider, "skipHostnameVerification", true);
        //url = url.replace("127.0.0.1", "localhost");
        TestResource testResource = fireRequest();
        assertEquals("1234", testResource.id);
    }

    /**
     * empty trusted pem results in error if verification is required
     */
    @Test
    public void testEmptyTrustStoreWhenCheckingIsRequired() throws Exception {
        setField(securityProvider, "trustedCertificates", "");
        setField(securityProvider, "skipCertificateVerification", false);
        try {
            securityProvider.buildTrustManager();
            fail();
        } catch (Exception e) {
            assertEquals("If the skipCertificateVerification is set to false (default) the trustedCertificates can not be empty", e.getMessage());
        }
    }

    /**
     * invalid PEM results in fast fail error
     */
    @Test
    public void testInvalidPem() throws Exception {
        setField(securityProvider, "trustedCertificates", "______");
        setField(securityProvider, "skipCertificateVerification", false);
        try {
            securityProvider.buildTrustManager();
            fail();
        } catch (Exception e) {
            assertEquals("The trustedCertificates must be a base64 encoded collection of PEM certificates", e.getMessage());
        }
    }

    /**
     * invalid PEM results in fast fail error
     */
    @Test
    public void testEmptyInvalidPem() throws Exception {
        setField(securityProvider, "trustedCertificates", "a3VrdQo=");
        setField(securityProvider, "skipCertificateVerification", false);
        try {
            securityProvider.buildTrustManager();
            fail();
        } catch (Exception e) {
            assertEquals("No certificate can be extracted from kuku\n", e.getMessage());
        }
    }

    /**
     * bad certificate content results in fast fail error
     */
    @Test
    public void testEmptyInvalidPemContent() throws Exception {
        String badCert = "-----BEGIN CERTIFICATE-----\nXXXXXX\n-----END CERTIFICATE-----";
        setField(securityProvider, "trustedCertificates", Base64.getEncoder().encodeToString(badCert.getBytes()));
        setField(securityProvider, "skipCertificateVerification", false);
        try {
            securityProvider.buildTrustManager();
            fail();
        } catch (Exception e) {
            assertEquals("Unable to create keystore", e.getMessage());
        }
    }

    /**
     * bad certificate content results in fast fail error for SSL socket factory
     */
    @Test
    public void testEmptyInvalidPemContentSSl() throws Exception {
        String badCert = "-----BEGIN CERTIFICATE-----\nXXXXXX\n-----END CERTIFICATE-----";
        setField(securityProvider, "trustedCertificates", Base64.getEncoder().encodeToString(badCert.getBytes()));
        setField(securityProvider, "skipCertificateVerification", false);
        try {
            securityProvider.buildSSLSocketFactory();
            fail();
        } catch (Exception e) {
            assertEquals("Unable to create SSL socket factory", e.getMessage());
        }
    }

    private TestResource fireRequest() {
        OkHttpClient client =
                new OkHttpClient.Builder()
                        .sslSocketFactory(securityProvider.buildSSLSocketFactory(), securityProvider.buildTrustManager())
                        .hostnameVerifier(securityProvider.buildHostnameVerifier()).build();
        TestService test1 = new Retrofit.Builder().baseUrl(url).client(client)
                .addConverterFactory(GsonCustomConverterFactory.create(new JSON().getGson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build().create(TestService.class);
        testServer.respones.add("{ \"id\" : \"1234\" } ");
        testServer.codes.add(200);
        TestService test = test1;
        return test.subscriptionsGet().blockingFirst();
    }

}
