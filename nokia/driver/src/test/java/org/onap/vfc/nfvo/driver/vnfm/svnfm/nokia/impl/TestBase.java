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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.impl;

import com.google.common.io.ByteStreams;
import com.nokia.cbam.catalog.v1.api.DefaultApi;
import com.nokia.cbam.lcm.v32.api.OperationExecutionsApi;
import com.nokia.cbam.lcm.v32.api.VnfsApi;
import com.nokia.cbam.lcn.v32.api.SubscriptionsApi;
import org.apache.commons.lang.ArrayUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Before;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.onap.msb.sdk.httpclient.msb.MSBServiceClient;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.RestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.SystemFunctions;
import org.onap.vfccatalog.api.VnfpackageApi;
import org.onap.vnfmdriver.api.NSLCMApi;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.when;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.impl.CbamCatalogManager.getFileInZip;

public class TestBase {
    public static final String VNF_ID = "myVnfId";
    public static final String VNFM_ID = "myVnfId";
    public static final String ONAP_CSAR_ID = "myOnapCsarId";
    public static final String VIM_ID = "myVimId";
    public static final String JOB_ID = "myJobId";
    public static final String CBAM_VNFD_ID = "cbamVnfdId";
    @Mock
    protected RestApiProvider restApiProvider;
    @Mock
    protected VnfsApi vnfApi;
    @Mock
    protected OperationExecutionsApi operationExecutionApi;
    @Mock
    protected SelfRegistrationManager selfRegistrationManager;
    @Mock
    protected Logger logger;
    @Mock
    protected SubscriptionsApi lcnApi;
    @Mock
    protected MSBServiceClient msbClient;
    @Mock
    protected DriverProperties driverProperties;
    @Mock
    protected NSLCMApi nsLcmApi;
    @Mock
    protected SystemFunctions systemFunctions;
    @Mock
    protected VnfpackageApi vfcCatalogApi;
    @Mock
    protected DefaultApi cbamCatalogApi;
    @Mock
    protected CloseableHttpClient httpClient;
    @Mock
    protected CloseableHttpResponse response;
    protected ArgumentCaptor<HttpUriRequest> request = ArgumentCaptor.forClass(HttpUriRequest.class);
    @Mock
    protected HttpEntity entity;
    @Mock
    protected HttpServletResponse httpResponse;

    @Before
    public void genericSetup() throws Exception {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(SystemFunctions.class, "INSTANCE", systemFunctions);
        when(restApiProvider.getCbamLcmApi(VNFM_ID)).thenReturn(vnfApi);
        when(restApiProvider.getMsbClient()).thenReturn(msbClient);
        when(restApiProvider.getCbamOperationExecutionApi(VNFM_ID)).thenReturn(operationExecutionApi);
        when(restApiProvider.getNsLcmApi()).thenReturn(nsLcmApi);
        when(restApiProvider.getOnapCatalogApi()).thenReturn(vfcCatalogApi);
        when(restApiProvider.getCbamLcnApi(VNFM_ID)).thenReturn(lcnApi);
        when(restApiProvider.getCbamCatalogApi(VNFM_ID)).thenReturn(cbamCatalogApi);
        when(restApiProvider.getHttpClient()).thenReturn(httpClient);
        when(httpClient.execute(request.capture())).thenReturn(response);
        when(restApiProvider.mapPrivateIpToPublicIp(Mockito.anyString())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocationOnMock) throws Throwable {
                return invocationOnMock.getArgumentAt(0, String.class);
            }
        });
        when(response.getEntity()).thenReturn(entity);
        when(driverProperties.getVnfmId()).thenReturn(VNFM_ID);
    }

    @After
    public void tearGeneric() {
        ReflectionTestUtils.setField(SystemFunctions.class, "INSTANCE", null);
    }

    protected void assertFileInZip(byte[] zip, String path, byte[] expectedContent) throws Exception {
        assertTrue(Arrays.equals(expectedContent, getFileInZip(new ByteArrayInputStream(zip), path).toByteArray()));
    }

    protected void assertItenticalZips(byte[] expected, byte[] actual) throws Exception {
        assertEquals(build(expected), build(actual));
    }

    private Map<String, List<Byte>> build(byte[] zip) throws Exception {
        Map<String, List<Byte>> files = new HashMap<>();
        ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(zip));
        ZipEntry zipEntry;
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ByteStreams.copy(zipInputStream, byteArrayOutputStream);
            files.put(zipEntry.getName(), Lists.newArrayList(ArrayUtils.toObject(byteArrayOutputStream.toByteArray())));
        }
        zipInputStream.close();
        return files;
    }
}
