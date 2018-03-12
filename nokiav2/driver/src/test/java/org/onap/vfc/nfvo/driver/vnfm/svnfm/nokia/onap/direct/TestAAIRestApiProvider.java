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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aai.domain.yang.v11.GenericVnf;
import org.onap.aai.domain.yang.v11.L3Network;
import org.onap.aai.domain.yang.v11.ObjectFactory;
import org.onap.aai.restclient.client.OperationResult;
import org.onap.aai.restclient.client.RestClient;
import org.onap.aai.restclient.enums.RestAuthenticationMode;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;

import javax.xml.bind.JAXBContext;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Base64.getEncoder;
import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import static junit.framework.TestCase.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class TestAAIRestApiProvider extends TestBase {
    private ObjectFactory OBJECT_FACTORY = new ObjectFactory();
    @Mock
    private RestClient restClient;
    private AAIRestApiProvider aaiRestApiProvider;
    private ArgumentCaptor<Map> headers = ArgumentCaptor.forClass(Map.class);
    private ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);

    private OperationResult result = new OperationResult();

    public static String marshall(Object object) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JAXBContext.newInstance(object.getClass()).createMarshaller().marshal(object, bos);
        return bos.toString();
    }

    public static <T> T unmarshal(String content, Class<T> clazz) {
        try {
            return (T) JAXBContext.newInstance(clazz).createUnmarshaller().unmarshal(new StringReader(content));
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @Before
    public void init() {
        //MockitoAnnotations.initMocks(this);
        AAIRestApiProvider real = new AAIRestApiProvider(msbApiProvider);
        setField(AAIRestApiProvider.class, "logger", logger);
        setFieldWithPropertyAnnotation(real, "${aaiUsername}", "aaiUsername");
        setFieldWithPropertyAnnotation(real, "${aaiPassword}", "aaiPassword");
        aaiRestApiProvider = Mockito.spy(real);
        when(aaiRestApiProvider.buildRawClient()).thenReturn(restClient);
        when(restClient.basicAuthPassword("aaiPassword")).thenReturn(restClient);
        when(restClient.basicAuthUsername("aaiUsername")).thenReturn(restClient);
        when(restClient.authenticationMode(RestAuthenticationMode.SSL_BASIC)).thenReturn(restClient);
        when(msbApiProvider.getMicroServiceUrl(AAIRestApiProvider.AAIService.CLOUD.getServiceName(), "v11")).thenReturn("x://1.2.3.4:4/a");
        result.setResultCode(201);
    }

    /**
     * test HTTP GET success scenario
     */
    @Test
    public void testGetSuccess() throws Exception {
        GenericVnf vnf = OBJECT_FACTORY.createGenericVnf();
        vnf.setVnfId("myVnfId");
        when(restClient.get(eq("x://1.2.3.4:4/a/myurl"), headers.capture(), eq(APPLICATION_XML_TYPE))).thenReturn(result);
        result.setResult(marshall(vnf));
        //when
        GenericVnf actualVnf = aaiRestApiProvider.get(logger, AAIRestApiProvider.AAIService.CLOUD, "/myurl", GenericVnf.class);
        //verify
        assertEquals(vnf.getVnfId(), actualVnf.getVnfId());
        assertHeaders();
    }

    /**
     * HTTP GET on non existing resource results in {@link java.util.NoSuchElementException}
     */
    @Test
    public void testGetMissingResource() throws Exception {
        when(restClient.get(eq("x://1.2.3.4:4/a/myurl"), headers.capture(), eq(APPLICATION_XML_TYPE))).thenReturn(result);
        result.setResultCode(404);
        //when
        try {
            aaiRestApiProvider.get(logger, AAIRestApiProvider.AAIService.CLOUD, "/myurl", GenericVnf.class);
            fail();
        } catch (NoSuchElementException e) {
            verify(logger).debug("The resource at /myurl does not exists");
            assertEquals("The resource at /myurl does not exists", e.getMessage());
        }
    }

    /**
     * Non known HTTP response code is propagated
     */
    @Test
    public void testUnknownErroCode() throws Exception {
        when(restClient.get(eq("x://1.2.3.4:4/a/myurl"), headers.capture(), eq(APPLICATION_XML_TYPE))).thenReturn(result);
        result.setResultCode(502);
        result.setFailureCause("myFail");
        //when
        try {
            aaiRestApiProvider.get(logger, AAIRestApiProvider.AAIService.CLOUD, "/myurl", GenericVnf.class);
            fail();
        } catch (RuntimeException e) {
            verify(logger).error("Bad response. Code: 502 cause: myFail");
            assertEquals("Bad response. Code: 502 cause: myFail", e.getMessage());
        }
    }

    /**
     * response content is not used when not requesting result
     */
    @Test
    public void testNoResult() throws Exception {
        when(restClient.get(eq("x://1.2.3.4:4/a/myurl"), headers.capture(), eq(APPLICATION_XML_TYPE))).thenReturn(result);
        result.setResultCode(202);
        //when
        Void result = aaiRestApiProvider.get(logger, AAIRestApiProvider.AAIService.CLOUD, "/myurl", Void.class);
        //verify
        assertNull(result);
    }

    /**
     * test HTTP PUT success scenario
     */
    @Test
    public void putSuccess() throws Exception {
        when(restClient.put(eq("x://1.2.3.4:4/a/myurl"), payload.capture(), headers.capture(), eq(APPLICATION_XML_TYPE), eq(APPLICATION_XML_TYPE))).thenReturn(result);
        GenericVnf request = OBJECT_FACTORY.createGenericVnf();
        request.setVnfId("myVnfId");
        L3Network response = OBJECT_FACTORY.createL3Network();
        response.setNetworkId("myNetworkId");
        result.setResult(marshall(response));
        //when
        L3Network actualResponse = aaiRestApiProvider.put(logger, AAIRestApiProvider.AAIService.CLOUD, "/myurl", request, L3Network.class);
        //verify
        GenericVnf actualValue = unmarshal(payload.getValue(), GenericVnf.class);
        assertEquals("myVnfId", actualValue.getVnfId());
        assertEquals("myNetworkId", actualResponse.getNetworkId());
        assertHeaders();
    }

    /**
     * test HTTP delete success scenario
     */
    @Test
    public void deleteSuccess() throws Exception {
        when(restClient.delete(eq("x://1.2.3.4:4/a/myurl"), headers.capture(), eq(APPLICATION_XML_TYPE))).thenReturn(result);
        //when
        aaiRestApiProvider.delete(logger, AAIRestApiProvider.AAIService.CLOUD, "/myurl");
        //verify
        assertHeaders();
        //the when above is the verify
    }

    /**
     * invalid request content results in error
     */
    @Test
    public void testInvalidInput() throws Exception {
        when(restClient.put(eq("x://1.2.3.4:4/a/myurl"), payload.capture(), headers.capture(), eq(APPLICATION_XML_TYPE), eq(APPLICATION_XML_TYPE))).thenReturn(result);
        //when
        try {
            aaiRestApiProvider.put(logger, AAIRestApiProvider.AAIService.CLOUD, "/myurl", "Invalid content", L3Network.class);
            //verify
            fail();
        } catch (Exception e) {
            assertEquals("Unable to marshal content", e.getMessage());
            verify(logger).error("Unable to marshal content", e.getCause());
        }
    }

    /**
     * invalid response content results in error
     */
    @Test
    public void testInvalidResponse() throws Exception {
        when(restClient.put(eq("x://1.2.3.4:4/a/myurl"), payload.capture(), headers.capture(), eq(APPLICATION_XML_TYPE), eq(APPLICATION_XML_TYPE))).thenReturn(result);
        GenericVnf request = OBJECT_FACTORY.createGenericVnf();
        request.setVnfId("myVnfId");
        result.setResult("invalid");
        //when
        try {
            aaiRestApiProvider.put(logger, AAIRestApiProvider.AAIService.CLOUD, "/myurl", request, L3Network.class);
            //verify
            fail();
        } catch (Exception e) {
            assertEquals("Unable to unmarshal content", e.getMessage());
            verify(logger).error("Unable to unmarshal content", e.getCause());
        }
    }

    /**
     * test AAI service names in AAI
     */
    @Test
    public void testServiceNames() {
        //the names have been base64-ed to prevent "smart" IDEs (idea) to refactor the tests too for the otherwise known fix constants in external systems
        assertEquals("YWFpLWNsb3VkSW5mcmFzdHJ1Y3R1cmU=", getEncoder().encodeToString(AAIRestApiProvider.AAIService.CLOUD.getServiceName().getBytes()));
        assertEquals("YWFpLW5ldHdvcms=", getEncoder().encodeToString(AAIRestApiProvider.AAIService.NETWORK.getServiceName().getBytes()));
        assertEquals("YWFpLWV4dGVybmFsU3lzdGVt", getEncoder().encodeToString(AAIRestApiProvider.AAIService.ESR.getServiceName().getBytes()));
    }

    private void assertHeaders() {
        Map<String, List<String>> actualHeaders = headers.getValue();
        assertEquals(2, actualHeaders.size());
        assertEquals(newArrayList("NokiaSVNFM"), actualHeaders.get("X-FromAppId"));
        assertEquals(newArrayList("application/xml"), actualHeaders.get("Accept"));
    }
}
