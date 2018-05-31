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
import com.nokia.cbam.catalog.v1.api.DefaultApi;
import com.nokia.cbam.lcm.v32.api.OperationExecutionsApi;
import com.nokia.cbam.lcm.v32.api.VnfsApi;
import com.nokia.cbam.lcn.v32.api.SubscriptionsApi;
import io.reactivex.Observable;
import io.reactivex.internal.operators.observable.ObservableFromCallable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.servlet.http.HttpServletResponse;
import junit.framework.TestCase;
import okhttp3.RequestBody;
import okio.Buffer;
import org.apache.commons.lang3.ArrayUtils;
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
import org.onap.msb.api.ServiceResourceApi;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api.INotificationSender;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api.VnfmInfoProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core.MsbApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core.SelfRegistrationManagerForSo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core.SelfRegistrationManagerForVfc;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AaiSecurityProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.vfc.VfcRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.SystemFunctions;
import org.onap.vfccatalog.api.VnfpackageApi;
import org.onap.vnfmdriver.api.NslcmApi;
import org.onap.vnfmdriver.model.VnfmInfo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.test.util.ReflectionTestUtils;
import retrofit2.Call;
import retrofit2.Response;

import static junit.framework.TestCase.*;
import static org.mockito.Mockito.when;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.SEPARATOR;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CatalogManager.getFileInZip;


public class TestBase {

    public static final String VNF_ID = "myVnfId";
    public static final String VNFM_ID = "myVnfmId";
    public static final String ONAP_CSAR_ID = "myOnapCsarId";
    public static final String VIM_ID = "myCloudOwnerId_myRegionName";
    public static final String JOB_ID = "myJobId";
    public static final String CBAM_VNFD_ID = "cbamVnfdId";
    public static final String HTTP_AUTH_URL = "http://authurl/";
    public static final String HTTP_LCM_URL = "http://lcmurl/";
    public static final String HTTP_LCN_URL = "http://lcnurl/";
    public static final String HTTP_CATLOG_URL = "http://catlogurl/";
    public static final String SUBCRIPTION_ID = "subcriptionId";
    protected static VoidObservable VOID_OBSERVABLE = new VoidObservable();
    @Mock
    protected CbamRestApiProvider cbamRestApiProvider;
    @Mock
    protected CbamRestApiProviderForSo cbamRestApiProviderForSo;
    @Mock
    protected CbamRestApiProviderForVfc cbamRestApiProviderForVfc;
    @Mock
    protected VfcRestApiProvider vfcRestApiProvider;
    @Mock
    protected MsbApiProvider msbApiProvider;
    @Mock
    protected AaiSecurityProvider aaiSecurityProvider;
    @Mock
    protected VnfmInfoProvider vnfmInfoProvider;
    @Mock
    protected VnfsApi vnfApi;
    @Mock
    protected OperationExecutionsApi operationExecutionApi;
    @Mock
    protected SelfRegistrationManagerForVfc selfRegistrationManagerForVfc;
    @Mock
    protected SelfRegistrationManagerForSo selfRegistrationManagerForSo;
    @Mock
    protected Logger logger;
    @Mock
    protected SubscriptionsApi lcnApi;
    @Mock
    protected ServiceResourceApi msbClient;
    @Mock
    protected Constants driverProperties;
    @Mock
    protected NslcmApi nsLcmApi;
    @Mock
    protected INotificationSender notificationSender;
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
    @Mock
    protected Environment environment;

    protected VnfmInfo vnfmInfo = new VnfmInfo();

    protected static <T> Call<T> buildCall(T response) {
        Call<T> call = Mockito.mock(Call.class);
        try {
            when(call.execute()).thenReturn(Response.success(response));
        } catch (Exception e) {
            throw new RuntimeException();
        }
        return call;
    }

    protected static <T> Observable<T> buildObservable(T response) {
        return Observable.just(response);
    }

    @Before
    public void genericSetup() throws Exception {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(SystemFunctions.class, "singletonInstance", systemFunctions);
        when(cbamRestApiProvider.getCbamLcmApi(VNFM_ID)).thenReturn(vnfApi);
        when(cbamRestApiProvider.getCbamOperationExecutionApi(VNFM_ID)).thenReturn(operationExecutionApi);
        when(cbamRestApiProvider.getCbamLcnApi(VNFM_ID)).thenReturn(lcnApi);
        when(cbamRestApiProvider.getCbamCatalogApi(VNFM_ID)).thenReturn(cbamCatalogApi);
        when(cbamRestApiProviderForSo.getCbamLcmApi(VNFM_ID)).thenReturn(vnfApi);
        when(cbamRestApiProviderForSo.getCbamOperationExecutionApi(VNFM_ID)).thenReturn(operationExecutionApi);
        when(cbamRestApiProviderForSo.getCbamLcnApi(VNFM_ID)).thenReturn(lcnApi);
        when(cbamRestApiProviderForSo.getCbamCatalogApi(VNFM_ID)).thenReturn(cbamCatalogApi);

        when(cbamRestApiProviderForVfc.getCbamLcmApi(VNFM_ID)).thenReturn(vnfApi);
        when(cbamRestApiProviderForVfc.getCbamOperationExecutionApi(VNFM_ID)).thenReturn(operationExecutionApi);
        when(cbamRestApiProviderForVfc.getCbamLcnApi(VNFM_ID)).thenReturn(lcnApi);
        when(cbamRestApiProviderForVfc.getCbamCatalogApi(VNFM_ID)).thenReturn(cbamCatalogApi);

        when(msbApiProvider.getMsbApi()).thenReturn(msbClient);
        when(vfcRestApiProvider.getNsLcmApi()).thenReturn(nsLcmApi);
        when(vfcRestApiProvider.getVfcCatalogApi()).thenReturn(vfcCatalogApi);
        when(systemFunctions.getHttpClient()).thenReturn(httpClient);
        when(httpClient.execute(request.capture())).thenReturn(response);
        when(response.getEntity()).thenReturn(entity);
        when(systemFunctions.getHttpClient()).thenReturn(httpClient);
        when(logger.isInfoEnabled()).thenReturn(true);
        when(logger.isDebugEnabled()).thenReturn(true);
        when(logger.isWarnEnabled()).thenReturn(true);
        when(logger.isErrorEnabled()).thenReturn(true);
        when(vnfmInfoProvider.getVnfmInfo(VNFM_ID)).thenReturn(vnfmInfo);
        vnfmInfo.setUrl(HTTP_AUTH_URL + SEPARATOR + HTTP_LCM_URL + SEPARATOR + HTTP_LCN_URL + SEPARATOR + HTTP_CATLOG_URL);
        vnfmInfo.setUserName("myUsername" + SEPARATOR + "myClientId");
        vnfmInfo.setPassword("myPassword" + SEPARATOR + "myClientSecret");
        when(selfRegistrationManagerForSo.getVnfmId(SUBCRIPTION_ID)).thenReturn(VNFM_ID);
        when(selfRegistrationManagerForVfc.getVnfmId(SUBCRIPTION_ID)).thenReturn(VNFM_ID);

    }

    @After
    public void tearGeneric() {
        ReflectionTestUtils.setField(SystemFunctions.class, "singletonInstance", null);
    }

    protected void assertFileInZip(byte[] zip, String path, byte[] expectedContent) throws Exception {
        assertTrue(Arrays.equals(expectedContent, getFileInZip(new ByteArrayInputStream(zip), path).toByteArray()));
    }

    protected void assertItenticalZips(byte[] expected, byte[] actual) throws Exception {
        assertEquals(build(expected), build(actual));
    }

    byte[] getContent(RequestBody requestBody) {
        try {
            Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            buffer.copyTo(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    protected void setFieldWithPropertyAnnotation(Object obj, String key, Object value) {
        for (Field field : obj.getClass().getDeclaredFields()) {
            for (Value fieldValue : field.getAnnotationsByType(Value.class)) {
                if (fieldValue.value().equals(key)) {
                    try {
                        field.setAccessible(true);
                        field.set(obj, value);
                        return;
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        throw new NoSuchElementException("The " + obj.getClass() + " does not have a filed with " + key + " annotation");
    }

    protected void assertBean(Class<?> clazz) {
        assertEquals(1, clazz.getDeclaredConstructors().length);
        Autowired annotation = clazz.getDeclaredConstructors()[0].getAnnotation(Autowired.class);
        assertNotNull(annotation);
        assertNotNull(clazz.getAnnotation(Component.class));
    }

    public static class VoidObservable {
        boolean called = false;
        ObservableFromCallable<Void> s = new ObservableFromCallable(new Callable() {
            @Override
            public Object call() throws Exception {
                called = true;
                return "";
            }
        });

        public void assertCalled() {
            TestCase.assertTrue(called);
        }

        public Observable<Void> value() {
            return s;
        }
    }
}
