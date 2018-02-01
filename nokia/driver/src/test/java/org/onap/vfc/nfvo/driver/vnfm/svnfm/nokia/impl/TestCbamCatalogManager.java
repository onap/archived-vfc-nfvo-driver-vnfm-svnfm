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

import com.nokia.cbam.catalog.v1.model.CatalogAdapterVnfpackage;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpGet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.TestUtil;
import org.onap.vfccatalog.ApiException;
import org.onap.vfccatalog.model.VnfPkgDetailInfo;
import org.onap.vfccatalog.model.VnfPkgInfo;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static junit.framework.TestCase.*;
import static org.mockito.Mockito.*;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.impl.CbamCatalogManager.getFileInZip;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class TestCbamCatalogManager extends TestBase {

    private static final String CSAR_ID = "csarId";
    private static final String CBAM_VNFD_ID = "CBAM_VNFD_ID";
    @InjectMocks
    private CbamCatalogManager cbamCatalogManager;
    private List<CatalogAdapterVnfpackage> existingVnfPackages = new ArrayList<>();
    private ArgumentCaptor<File> uploadedFile = ArgumentCaptor.forClass(File.class);

    @Before
    public void initMocks() throws Exception {
        setField(CbamCatalogManager.class, "logger", logger);
        when(cbamCatalogApi.list()).thenReturn(existingVnfPackages);
    }

    /**
     * the package is transferred from VF-C catalog to CBAM catalog
     */
    @Test
    public void testPackageTransfer() throws Exception {
        CatalogAdapterVnfpackage existingPackage = new CatalogAdapterVnfpackage();
        existingPackage.setVnfdId("unknownId");
        existingVnfPackages.add(existingPackage);
        VnfPkgDetailInfo vnfPackageDetails = new VnfPkgDetailInfo();
        vnfPackageDetails.setCsarId(CSAR_ID);
        vnfPackageDetails.setPackageInfo(new VnfPkgInfo());

        vnfPackageDetails.getPackageInfo().setVnfdModel("{ \"metadata\" : { \"resourceVendorModelNumber\" : \"" + CBAM_VNFD_ID + "\" }}");
        vnfPackageDetails.getPackageInfo().setDownloadUrl("http://127.0.0.1/a.csar");
        when(vfcCatalogApi.queryVnfPackage(CSAR_ID)).thenReturn(vnfPackageDetails);
        CatalogAdapterVnfpackage createdPackage = new CatalogAdapterVnfpackage();
        createdPackage.setVnfdId(CBAM_VNFD_ID);
        when(cbamCatalogApi.create(uploadedFile.capture())).thenAnswer(new Answer<CatalogAdapterVnfpackage>() {
            @Override
            public CatalogAdapterVnfpackage answer(InvocationOnMock invocationOnMock) throws Throwable {
                File content = invocationOnMock.getArgumentAt(0, File.class);
                return createdPackage;
            }
        });
        byte[] onapPackageContent = TestUtil.loadFile("unittests/TestCbamCatalogManager.sample.csar");
        ByteArrayInputStream pack = new ByteArrayInputStream(onapPackageContent);
        when(entity.getContent()).thenReturn(pack);
        //when
        CatalogAdapterVnfpackage cbamPackage = cbamCatalogManager.preparePackageInCbam(VNFM_ID, CSAR_ID);
        //verify
        //the correct portion of the package is extracted and uploaded to CBAM
        byte[] expectedContentToUpload = getFileInZip(new ByteArrayInputStream(onapPackageContent), "Artifacts/Deployment/OTHER/cbam.package.zip").toByteArray();
        assertTrue(Arrays.equals(expectedContentToUpload, Files.readAllBytes(uploadedFile.getValue().toPath())));
        assertEquals(createdPackage, cbamPackage);
        assertEquals(HttpGet.class, request.getValue().getClass());
        assertEquals("http://127.0.0.1/a.csar", request.getValue().getURI().toString());
        assertEquals("application/octet-stream", request.getValue().getFirstHeader(HttpHeaders.ACCEPT).getValue());


    }

    /**
     * the package is transfer fails, but the package has been uploaded (possibly by other thread / work flow)
     * the transfer succeeds
     */
    @Test
    public void testPackageTransferConcurrency() throws Exception {
        CatalogAdapterVnfpackage existingPackage = new CatalogAdapterVnfpackage();
        existingPackage.setVnfdId("unknownId");
        existingVnfPackages.add(existingPackage);
        VnfPkgDetailInfo vnfPackageDetails = new VnfPkgDetailInfo();
        vnfPackageDetails.setCsarId(CSAR_ID);
        vnfPackageDetails.setPackageInfo(new VnfPkgInfo());

        vnfPackageDetails.getPackageInfo().setVnfdModel("{ \"metadata\" : { \"resourceVendorModelNumber\" : \"" + CBAM_VNFD_ID + "\" }}");
        vnfPackageDetails.getPackageInfo().setDownloadUrl("http://127.0.0.1/a.csar");
        when(vfcCatalogApi.queryVnfPackage(CSAR_ID)).thenReturn(vnfPackageDetails);
        CatalogAdapterVnfpackage createdPackage = new CatalogAdapterVnfpackage();
        createdPackage.setVnfdId(CBAM_VNFD_ID);
        RuntimeException can_not_upload_package = new RuntimeException("Can not upload package");
        when(cbamCatalogApi.create(uploadedFile.capture())).thenAnswer(new Answer<CatalogAdapterVnfpackage>() {
            @Override
            public CatalogAdapterVnfpackage answer(InvocationOnMock invocationOnMock) throws Throwable {
                File content = invocationOnMock.getArgumentAt(0, File.class);
                //this is done by an other thread
                existingVnfPackages.add(createdPackage);
                when(cbamCatalogApi.getById(CBAM_VNFD_ID)).thenReturn(createdPackage);
                throw can_not_upload_package;
            }
        });
        byte[] onapPackageContent = TestUtil.loadFile("unittests/TestCbamCatalogManager.sample.csar");
        ByteArrayInputStream pack = new ByteArrayInputStream(onapPackageContent);
        when(entity.getContent()).thenReturn(pack);
        //when
        CatalogAdapterVnfpackage cbamPackage = cbamCatalogManager.preparePackageInCbam(VNFM_ID, CSAR_ID);
        //verify
        //the correct portion of the package is extracted and uploaded to CBAM
        byte[] expectedContentToUpload = getFileInZip(new ByteArrayInputStream(onapPackageContent), "Artifacts/Deployment/OTHER/cbam.package.zip").toByteArray();
        assertTrue(Arrays.equals(expectedContentToUpload, Files.readAllBytes(uploadedFile.getValue().toPath())));
        assertEquals(createdPackage, cbamPackage);
        assertEquals(HttpGet.class, request.getValue().getClass());
        assertEquals("http://127.0.0.1/a.csar", request.getValue().getURI().toString());
        assertEquals("application/octet-stream", request.getValue().getFirstHeader(HttpHeaders.ACCEPT).getValue());
        verify(logger).debug("Probably concurrent package uploads", can_not_upload_package);
    }

    /**
     * If the package already exists in CBAM catalog it is not re-uploaded
     */
    @Test
    public void testIdempotentPackageUpload() throws Exception {
        CatalogAdapterVnfpackage existingPackage = new CatalogAdapterVnfpackage();
        existingPackage.setVnfdId(CBAM_VNFD_ID);
        existingVnfPackages.add(existingPackage);
        when(cbamCatalogApi.getById(CBAM_VNFD_ID)).thenReturn(existingPackage);

        VnfPkgDetailInfo vnfPackageDetails = new VnfPkgDetailInfo();
        vnfPackageDetails.setCsarId(CSAR_ID);
        vnfPackageDetails.setPackageInfo(new VnfPkgInfo());
        vnfPackageDetails.getPackageInfo().setVnfdModel("{ \"metadata\" : { \"resourceVendorModelNumber\" : \"" + CBAM_VNFD_ID + "\" }}");
        vnfPackageDetails.getPackageInfo().setDownloadUrl("http://127.0.0.1/a.csar");
        when(vfcCatalogApi.queryVnfPackage(CSAR_ID)).thenReturn(vnfPackageDetails);

       // CatalogAdapterVnfpackage createdPackage = new CatalogAdapterVnfpackage();
        //createdPackage.setVnfdId(CBAM_VNFD_ID);
        //when
        CatalogAdapterVnfpackage cbamPackage = cbamCatalogManager.preparePackageInCbam(VNFM_ID, CSAR_ID);
        //verify
        verify(cbamCatalogApi, never()).create(Mockito.any());
        assertEquals(existingPackage, cbamPackage);
    }

    /**
     * the download URL is remapped
     */
    @Test
    public void testDownloadUrl() throws Exception {
        //given
        VnfPkgDetailInfo vnfPackageDetails = new VnfPkgDetailInfo();
        vnfPackageDetails.setCsarId(CSAR_ID);
        vnfPackageDetails.setPackageInfo(new VnfPkgInfo());
        vnfPackageDetails.getPackageInfo().setDownloadUrl("http://127.0.0.1/a.csar");
        when(restApiProvider.mapPrivateIpToPublicIp("127.0.0.1")).thenReturn("128.0.0.1");

        vnfPackageDetails.getPackageInfo().setVnfdModel("{ \"metadata\" : { \"resourceVendorModelNumber\" : \"" + CBAM_VNFD_ID + "\" }}");
        when(vfcCatalogApi.queryVnfPackage(CSAR_ID)).thenReturn(vnfPackageDetails);
        CatalogAdapterVnfpackage createdPackage = new CatalogAdapterVnfpackage();
        createdPackage.setVnfdId(CBAM_VNFD_ID);
        when(cbamCatalogApi.create(uploadedFile.capture())).thenAnswer(new Answer<CatalogAdapterVnfpackage>() {
            @Override
            public CatalogAdapterVnfpackage answer(InvocationOnMock invocationOnMock) throws Throwable {
                File content = invocationOnMock.getArgumentAt(0, File.class);
                return createdPackage;
            }
        });
        byte[] onapPackageContent = TestUtil.loadFile("unittests/TestCbamCatalogManager.sample.csar");
        ByteArrayInputStream pack = new ByteArrayInputStream(onapPackageContent);
        when(entity.getContent()).thenReturn(pack);
        //when
        CatalogAdapterVnfpackage cbamPackage = cbamCatalogManager.preparePackageInCbam(VNFM_ID, CSAR_ID);
        //verify
        assertEquals("http://128.0.0.1/a.csar", request.getValue().getURI().toString());
    }

    /**
     * failure to query package from VF-C is propagated
     */
    @Test
    public void testFailureUnableToQueryPackageFromVFC() throws Exception {
        ApiException expectedException = new ApiException();
        when(vfcCatalogApi.queryVnfPackage(Mockito.any())).thenThrow(expectedException);
        //when
        try {
            cbamCatalogManager.preparePackageInCbam(VNFM_ID, CSAR_ID);
            fail();
        } catch (RuntimeException e) {
            assertEquals(expectedException, e.getCause());
            verify(logger).error("Unable to query VNF package with csarId from VF-C", e.getCause());
        }
    }

    /**
     * failure to download package from VF-C results in error
     */
    @Test
    public void testFailureUnableToDownloadPackageFromVFC() throws Exception {
        CatalogAdapterVnfpackage existingPackage = new CatalogAdapterVnfpackage();
        existingPackage.setVnfdId("unknownId");
        existingVnfPackages.add(existingPackage);
        VnfPkgDetailInfo vnfPackageDetails = new VnfPkgDetailInfo();
        vnfPackageDetails.setCsarId(CSAR_ID);
        vnfPackageDetails.setPackageInfo(new VnfPkgInfo());

        vnfPackageDetails.getPackageInfo().setVnfdModel("{ \"metadata\" : { \"resourceVendorModelNumber\" : \"" + CBAM_VNFD_ID + "\" }}");
        vnfPackageDetails.getPackageInfo().setDownloadUrl("http://127.0.0.1/a.csar");
        when(vfcCatalogApi.queryVnfPackage(CSAR_ID)).thenReturn(vnfPackageDetails);
        CatalogAdapterVnfpackage createdPackage = new CatalogAdapterVnfpackage();
        createdPackage.setVnfdId(CBAM_VNFD_ID);
        IOException expectedException = new IOException();
        when(httpClient.execute(Mockito.any(HttpGet.class))).thenThrow(expectedException);
        //when
        try {
            cbamCatalogManager.preparePackageInCbam(VNFM_ID, CSAR_ID);
            fail();
        } catch (Exception e) {
            verify(logger).error("Unable to download package from http://127.0.0.1/a.csar from VF-C", expectedException);
            assertEquals(expectedException, e.getCause());
        }
    }

    /**
     * failure to list package in CBAM results in error
     */
    @Test
    public void testFailureToListVnfPackagesInCbam() throws Exception {
        CatalogAdapterVnfpackage existingPackage = new CatalogAdapterVnfpackage();
        existingPackage.setVnfdId("unknownId");
        existingVnfPackages.add(existingPackage);
        VnfPkgDetailInfo vnfPackageDetails = new VnfPkgDetailInfo();
        vnfPackageDetails.setCsarId(CSAR_ID);
        vnfPackageDetails.setPackageInfo(new VnfPkgInfo());

        vnfPackageDetails.getPackageInfo().setVnfdModel("{ \"metadata\" : { \"resourceVendorModelNumber\" : \"" + CBAM_VNFD_ID + "\" }}");
        vnfPackageDetails.getPackageInfo().setDownloadUrl("http://127.0.0.1/a.csar");
        when(vfcCatalogApi.queryVnfPackage(CSAR_ID)).thenReturn(vnfPackageDetails);
        CatalogAdapterVnfpackage createdPackage = new CatalogAdapterVnfpackage();
        createdPackage.setVnfdId(CBAM_VNFD_ID);
        com.nokia.cbam.catalog.v1.ApiException expectedException = new com.nokia.cbam.catalog.v1.ApiException();
        when(cbamCatalogApi.list()).thenThrow(expectedException);
        //when
        try {
            cbamCatalogManager.preparePackageInCbam(VNFM_ID, CSAR_ID);
            fail();
        } catch (Exception e) {
            verify(logger).error("Unable to determine if the VNF package has been replicated in CBAM", expectedException);
            assertEquals(expectedException, e.getCause());
        }
    }

    /**
     * failure to query package from CBAM results in error
     */
    @Test
    public void testFailureToQueryVnfPackagesFromCbam() throws Exception {
        CatalogAdapterVnfpackage existingPackage = new CatalogAdapterVnfpackage();
        existingPackage.setVnfdId(CBAM_VNFD_ID);
        existingVnfPackages.add(existingPackage);
        VnfPkgDetailInfo vnfPackageDetails = new VnfPkgDetailInfo();
        vnfPackageDetails.setCsarId(CSAR_ID);
        vnfPackageDetails.setPackageInfo(new VnfPkgInfo());
        vnfPackageDetails.getPackageInfo().setVnfdModel("{ \"metadata\" : { \"resourceVendorModelNumber\" : \"" + CBAM_VNFD_ID + "\" }}");
        vnfPackageDetails.getPackageInfo().setDownloadUrl("http://127.0.0.1/a.csar");
        when(vfcCatalogApi.queryVnfPackage(CSAR_ID)).thenReturn(vnfPackageDetails);
        com.nokia.cbam.catalog.v1.ApiException expectedException = new com.nokia.cbam.catalog.v1.ApiException();
        when(cbamCatalogApi.getById(CBAM_VNFD_ID)).thenThrow(expectedException);
        //when
        try {
            cbamCatalogManager.preparePackageInCbam(VNFM_ID, CSAR_ID);
            fail();
        } catch (Exception e) {
            verify(logger).error("Unable to query VNF package with CBAM_VNFD_ID from CBAM", expectedException);
            assertEquals(expectedException, e.getCause());
        }
    }

    /**
     * failure to create package in CBAM results in error
     */
    @Test
    public void testFailureToCreatePackageInCbam() throws Exception {
        CatalogAdapterVnfpackage existingPackage = new CatalogAdapterVnfpackage();
        existingPackage.setVnfdId("unknownId");
        existingVnfPackages.add(existingPackage);
        VnfPkgDetailInfo vnfPackageDetails = new VnfPkgDetailInfo();
        vnfPackageDetails.setCsarId(CSAR_ID);
        vnfPackageDetails.setPackageInfo(new VnfPkgInfo());

        vnfPackageDetails.getPackageInfo().setVnfdModel("{ \"metadata\" : { \"resourceVendorModelNumber\" : \"" + CBAM_VNFD_ID + "\" }}");
        vnfPackageDetails.getPackageInfo().setDownloadUrl("http://127.0.0.1/a.csar");
        when(vfcCatalogApi.queryVnfPackage(CSAR_ID)).thenReturn(vnfPackageDetails);
        CatalogAdapterVnfpackage createdPackage = new CatalogAdapterVnfpackage();
        createdPackage.setVnfdId(CBAM_VNFD_ID);

        byte[] onapPackageContent = TestUtil.loadFile("unittests/TestCbamCatalogManager.sample.csar");
        ByteArrayInputStream pack = new ByteArrayInputStream(onapPackageContent);
        when(entity.getContent()).thenReturn(pack);
        com.nokia.cbam.catalog.v1.ApiException expectedException = new com.nokia.cbam.catalog.v1.ApiException();
        when(cbamCatalogApi.create(Mockito.any())).thenThrow(expectedException);
        try {
            cbamCatalogManager.preparePackageInCbam(VNFM_ID, CSAR_ID);
            fail();
        } catch (Exception e) {
            verify(logger).error("Unable to create VNF with csarId CSAR identifier in package in CBAM downloaded from http://127.0.0.1/a.csar", expectedException);
            assertEquals(expectedException, e.getCause());
        }
    }

    /**
     * the VNFD is extracted from zip
     */
    @Test
    public void testExtractVnfdFromPackage() throws Exception {
        Path csar = Files.createTempFile(UUID.randomUUID().toString(), "csar");
        Files.write(csar, TestUtil.loadFile("unittests/cbam.package.zip"));
        when(cbamCatalogApi.content(CBAM_VNFD_ID)).thenReturn(csar.toFile());
        //when
        String content = cbamCatalogManager.getCbamVnfdContent(VNFM_ID, CBAM_VNFD_ID);
        //verify
        assertEquals("dummy vnfd\n", content);
    }

    /**
     * if VNFD the Tosca meta can not be extracted sensible error is returned
     */
    @Test
    public void testEmptyCbamPackage() throws Exception {
        Path csar = Files.createTempFile(UUID.randomUUID().toString(), "csar");
        Files.write(csar, TestUtil.loadFile("unittests/empty.zip"));
        when(cbamCatalogApi.content(CBAM_VNFD_ID)).thenReturn(csar.toFile());
        //when
        try {
            cbamCatalogManager.getCbamVnfdContent(VNFM_ID, CBAM_VNFD_ID);
            fail();
        } catch (RuntimeException e) {
            verify(logger).error("Unable to get package with (CBAM_VNFD_ID)", e.getCause());
            assertEquals("Unable to find the TOSCA-Metadata/TOSCA.meta in archive found: []", e.getCause().getMessage());
        }
    }

    /**
     * if VNFD can not be extracted sensible error is returned
     */
    @Test
    public void testMissingVnfdCbamPackage() throws Exception {
        Path csar = Files.createTempFile(UUID.randomUUID().toString(), "csar");
        Files.write(csar, TestUtil.loadFile("unittests/missing.vnfd.zip"));
        when(cbamCatalogApi.content(CBAM_VNFD_ID)).thenReturn(csar.toFile());
        //when
        try {
            cbamCatalogManager.getCbamVnfdContent(VNFM_ID, CBAM_VNFD_ID);
            fail();
        } catch (RuntimeException e) {
            verify(logger).error("Unable to get package with (" + CBAM_VNFD_ID + ")", e.getCause());
            assertTrue("Unable to find the vnfdloc/a.yaml in archive found: [TOSCA-Metadata/, TOSCA-Metadata/TOSCA.meta]".equals(e.getCause().getMessage())
                    || "Unable to find the vnfdloc/a.yaml in archive found: [TOSCA-Metadata/TOSCA.meta, TOSCA-Metadata/]".equals(e.getCause().getMessage())
            );
        }
    }
}
