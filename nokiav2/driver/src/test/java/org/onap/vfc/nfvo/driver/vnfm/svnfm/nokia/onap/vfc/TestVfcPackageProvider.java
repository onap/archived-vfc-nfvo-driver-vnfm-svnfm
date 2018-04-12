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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.vfc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpGet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core.IpMappingProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.TestUtil;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;
import org.onap.vfccatalog.model.VnfPkgDetailInfo;
import org.onap.vfccatalog.model.VnfPkgInfo;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class TestVfcPackageProvider extends TestBase {

    private static final String CSAR_ID = "csarId";
    private static final String CBAM_VNFD_ID = "CBAM_VNFD_ID";
    @Mock
    private IpMappingProvider ipMappingProvider;

    @Mock
    private VfcPackageProvider vfcPackageProvider;


    @Before
    public void initMocks() throws Exception {
        setField(VfcPackageProvider.class, "logger", logger);
        vfcPackageProvider = new VfcPackageProvider(vfcRestApiProvider, ipMappingProvider);
    }

    /**
     * query CBAM VNFD identifier from VF-C catalog
     */
    @Test
    public void testGetCbamVnfd() throws Exception {
        VnfPkgDetailInfo vnfPackageDetails = new VnfPkgDetailInfo();
        vnfPackageDetails.setCsarId(CSAR_ID);
        vnfPackageDetails.setPackageInfo(new VnfPkgInfo());
        vnfPackageDetails.getPackageInfo().setVnfdModel("{ \"metadata\" : { \"resourceVendorModelNumber\" : \"" + CBAM_VNFD_ID + "\" }}");
        vnfPackageDetails.getPackageInfo().setDownloadUrl("http://127.0.0.1/a.csar");
        when(vfcCatalogApi.queryVnfPackage(CSAR_ID)).thenReturn(buildObservable(vnfPackageDetails));
        //when
        String cbamVnfdId = vfcPackageProvider.getCbamVnfdId(CSAR_ID);
        //verify
        assertEquals(CBAM_VNFD_ID, cbamVnfdId);
    }

    /**
     * download ONAP VNFD from VF-C catalog
     */
    @Test
    public void testDownload() throws Exception {
        VnfPkgDetailInfo vnfPackageDetails = new VnfPkgDetailInfo();
        vnfPackageDetails.setCsarId(CSAR_ID);
        vnfPackageDetails.setPackageInfo(new VnfPkgInfo());
        vnfPackageDetails.getPackageInfo().setVnfdModel("{ \"metadata\" : { \"resourceVendorModelNumber\" : \"" + CBAM_VNFD_ID + "\" }}");
        vnfPackageDetails.getPackageInfo().setDownloadUrl("http://127.0.0.1/a.csar");
        when(vfcCatalogApi.queryVnfPackage(CSAR_ID)).thenReturn(buildObservable(vnfPackageDetails));
        byte[] onapPackageContent = TestUtil.loadFile("unittests/TestCbamCatalogManager.sample.csar");
        when(ipMappingProvider.mapPrivateIpToPublicIp("127.0.0.1")).thenReturn("1.2.3.4");
        when(entity.getContent()).thenReturn(new ByteArrayInputStream(onapPackageContent));
        //when
        byte[] actualContent = vfcPackageProvider.getPackage(CSAR_ID);
        //verify
        Assert.assertArrayEquals(onapPackageContent, actualContent);
        assertEquals(HttpGet.class, request.getValue().getClass());
        assertEquals("http://1.2.3.4/a.csar", request.getValue().getURI().toString());
        assertEquals("application/octet-stream", request.getValue().getFirstHeader(HttpHeaders.ACCEPT).getValue());
    }

    /**
     * failure to query package from VF-C is propagated
     */
    @Test
    public void unableToGetCbamVnfdFromCatalog() throws Exception {
        RuntimeException expectedException = new RuntimeException();
        when(vfcCatalogApi.queryVnfPackage(CSAR_ID)).thenThrow(expectedException);
        //when
        try {
            vfcPackageProvider.getCbamVnfdId(CSAR_ID);
            fail();
        } catch (Exception e) {
            verify(logger).error("Unable to query VNF package with csarId", expectedException);
            assertEquals(expectedException, e.getCause());
        }
    }

    /**
     * failure to download package from VF-C is propagated
     */
    @Test
    public void unableToDownloadFromCatalog() throws Exception {
        VnfPkgDetailInfo vnfPackageDetails = new VnfPkgDetailInfo();
        vnfPackageDetails.setCsarId(CSAR_ID);
        vnfPackageDetails.setPackageInfo(new VnfPkgInfo());
        vnfPackageDetails.getPackageInfo().setVnfdModel("{ \"metadata\" : { \"resourceVendorModelNumber\" : \"" + CBAM_VNFD_ID + "\" }}");
        vnfPackageDetails.getPackageInfo().setDownloadUrl("http://127.0.0.1/a.csar");
        when(vfcCatalogApi.queryVnfPackage(CSAR_ID)).thenReturn(buildObservable(vnfPackageDetails));
        byte[] onapPackageContent = TestUtil.loadFile("unittests/TestCbamCatalogManager.sample.csar");
        when(ipMappingProvider.mapPrivateIpToPublicIp("127.0.0.1")).thenReturn("1.2.3.4");
        IOException expectedException = new IOException();
        when(httpClient.execute(Mockito.any())).thenThrow(expectedException);
        //when
        try {
            vfcPackageProvider.getPackage(CSAR_ID);
            fail();
        } catch (Exception e) {
            verify(logger).error("Unable to download package from http://1.2.3.4/a.csar", expectedException);
            assertEquals(expectedException, e.getCause());
        }
    }

    /**
     * failure to query package for download package from VF-C is propagated
     */
    @Test
    public void unableToQueryPackageForDownloadFromCatalog() throws Exception {
        RuntimeException expectedException = new RuntimeException();
        when(vfcCatalogApi.queryVnfPackage(CSAR_ID)).thenThrow(expectedException);
        //when
        try {
            vfcPackageProvider.getPackage(CSAR_ID);
            fail();
        } catch (Exception e) {
            verify(logger).error("Unable to query VNF package with csarId", expectedException);
            assertEquals(expectedException, e.getCause());
        }
    }
}
