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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.NoSuchElementException;
import org.apache.http.client.methods.HttpGet;
import org.junit.Before;
import org.junit.Test;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.TestUtil;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core.SelfRegistrationManager.SERVICE_NAME;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class TestSdcPackageProvider extends TestBase {
    private SdcPackageProvider sdcPackageProvider;

    @Before
    public void init() {
        sdcPackageProvider = new SdcPackageProvider(msbApiProvider, driverProperties);
        setField(SdcPackageProvider.class, "logger", logger);
        setFieldWithPropertyAnnotation(sdcPackageProvider, "${sdcUsername}", "sdcUsername");
        setFieldWithPropertyAnnotation(sdcPackageProvider, "${sdcPassword}", "sdcPassword");
        when(msbApiProvider.getMicroServiceUrl("sdc", "v1")).thenReturn("https://1.2.3.4:456/g");
    }

    /**
     * test package download from SDC
     */
    @Test
    public void testPackageDownload() throws Exception {
        when(entity.getContent()).thenReturn(new ByteArrayInputStream("test".getBytes()));
        //when
        byte[] result = sdcPackageProvider.getPackage("csarId");
        //verify
        assertEquals("test", new String("test"));
        HttpGet httpGet = (HttpGet) request.getValue();
        assertEquals(VNFM_ID, httpGet.getFirstHeader("X-ECOMP-InstanceID").getValue());
        assertEquals(SERVICE_NAME, httpGet.getFirstHeader("X-FromAppId").getValue());
        assertEquals(APPLICATION_OCTET_STREAM_VALUE, httpGet.getFirstHeader(ACCEPT).getValue());
        assertEquals("https://1.2.3.4:456/g/sdc/v1/catalog/resources/csarId/toscaModel", httpGet.getURI().toASCIIString());
    }

    /**
     * failure to download package from SDC is propagated
     */
    @Test
    public void testFailedPackageDownload() throws Exception {
        IOException expectedException = new IOException();
        when(httpClient.execute(any())).thenThrow(expectedException);
        try {
            sdcPackageProvider.getPackage("csarId");
            fail();
        } catch (Exception e) {
            assertEquals("Unable to download csarId package from SDC", e.getMessage());
            assertEquals(expectedException, e.getCause());
            verify(logger).error("Unable to download csarId package from SDC", expectedException);
        }
    }

    /**
     * get VNFD from ONAP package
     */
    @Test
    public void testGetVnfd() throws Exception {
        byte[] onapPackageContent = TestUtil.loadFile("unittests/TestCbamCatalogManager.sample.csar");
        when(entity.getContent()).thenReturn(new ByteArrayInputStream(onapPackageContent));
        //when
        String cbamVnfdId = sdcPackageProvider.getCbamVnfdId("csarId");
        //verify
        assertEquals("Nokia~SimpleDual_scalable~1.0~1.0", cbamVnfdId);
    }

    /**
     * unable to download package from SDC during get CBAM VNFD id
     */
    @Test
    public void testUnableToDownloadPackageDuringVnfdIdGet() throws Exception {
        IOException expectedException = new IOException();
        when(httpClient.execute(any())).thenThrow(expectedException);
        try {
            sdcPackageProvider.getCbamVnfdId("csarId");
            fail();
        } catch (Exception e) {
            assertEquals("Unable to download csarId package from SDC", e.getMessage());
            assertEquals(expectedException, e.getCause());
            verify(logger).error("Unable to download csarId package from SDC", expectedException);
        }
    }

    /**
     * invalid VNF package results in error
     */
    @Test
    public void testInvalidVNFDContent() throws Exception {
        byte[] onapPackageContent = "invalidZip".getBytes();
        when(entity.getContent()).thenReturn(new ByteArrayInputStream(onapPackageContent));
        try {
            sdcPackageProvider.getCbamVnfdId("csarId");
            fail();
        } catch (Exception e) {
            assertEquals("Unable to extract CBAM VNFD id from ONAP package", e.getMessage());
            verify(logger).error(eq("Unable to extract CBAM VNFD id from ONAP package"), any(NoSuchElementException.class));
        }
    }
}
