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

import com.nokia.cbam.catalog.v1.model.CatalogAdapterVnfpackage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import okhttp3.Headers;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.internal.http.RealResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api.IPackageProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.TestUtil;

import static junit.framework.TestCase.*;
import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.*;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CatalogManager.getFileInZip;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class TestCbamCatalogManager extends TestBase {

    private static final String CSAR_ID = "csarId";
    private static final String CBAM_VNFD_ID = "CBAM_VNFD_ID";
    private CatalogManager cbamCatalogManager;
    @Mock
    private IPackageProvider packageProvider;

    private List<CatalogAdapterVnfpackage> existingVnfPackages = new ArrayList<>();
    private ArgumentCaptor<RequestBody> uploadedFile = ArgumentCaptor.forClass(RequestBody.class);

    @Before
    public void initMocks() throws Exception {
        setField(CatalogManager.class, "logger", logger);
        when(cbamCatalogApi.list()).thenReturn(buildObservable(existingVnfPackages));
        cbamCatalogManager = new CatalogManager(cbamRestApiProvider, packageProvider);
    }

    /**
     * the package is transferred from source to CBAM catalog
     */
    @Test
    public void testPackageTransfer() throws Exception {
        CatalogAdapterVnfpackage existingPackage = new CatalogAdapterVnfpackage();
        existingPackage.setVnfdId("unknownId");
        existingVnfPackages.add(existingPackage);
        CatalogAdapterVnfpackage createdPackage = new CatalogAdapterVnfpackage();
        createdPackage.setVnfdId(CBAM_VNFD_ID);
        when(cbamCatalogApi.create(uploadedFile.capture())).thenReturn(buildObservable(createdPackage));
        byte[] onapPackageContent = TestUtil.loadFile("unittests/TestCbamCatalogManager.sample.csar");
        when(packageProvider.getPackage(CSAR_ID)).thenReturn(onapPackageContent);
        when(packageProvider.getCbamVnfdId(CSAR_ID)).thenReturn(CBAM_VNFD_ID);
        //when
        CatalogAdapterVnfpackage cbamPackage = cbamCatalogManager.preparePackageInCbam(VNFM_ID, CSAR_ID);
        //verify
        byte[] a2 = getContent(uploadedFile.getValue());
        assertArrayEquals(getFileInZip(new ByteArrayInputStream(onapPackageContent), "Artifacts/Deployment/OTHER/cbam.package.zip").toByteArray(), a2);
        assertEquals(createdPackage, cbamPackage);
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
        CatalogAdapterVnfpackage createdPackage = new CatalogAdapterVnfpackage();
        createdPackage.setVnfdId(CBAM_VNFD_ID);
        byte[] onapPackageContent = TestUtil.loadFile("unittests/TestCbamCatalogManager.sample.csar");
        when(packageProvider.getPackage(CSAR_ID)).thenReturn(onapPackageContent);
        when(packageProvider.getCbamVnfdId(CSAR_ID)).thenReturn(CBAM_VNFD_ID);
        RuntimeException can_not_upload_package = new RuntimeException("Can not upload package");
        when(cbamCatalogApi.create(uploadedFile.capture())).thenAnswer(new Answer<CatalogAdapterVnfpackage>() {
            @Override
            public CatalogAdapterVnfpackage answer(InvocationOnMock invocationOnMock) throws Throwable {
                //this is done by an other thread
                existingVnfPackages.add(createdPackage);
                when(cbamCatalogApi.getById(CBAM_VNFD_ID)).thenReturn(buildObservable(createdPackage));
                throw can_not_upload_package;
            }
        });
        //when
        CatalogAdapterVnfpackage cbamPackage = cbamCatalogManager.preparePackageInCbam(VNFM_ID, CSAR_ID);
        //verify
        //the correct portion of the package is extracted and uploaded to CBAM
        byte[] expectedContentToUpload = getFileInZip(new ByteArrayInputStream(onapPackageContent), "Artifacts/Deployment/OTHER/cbam.package.zip").toByteArray();
        assertTrue(Arrays.equals(expectedContentToUpload, getContent(uploadedFile.getValue())));
        assertEquals(createdPackage, cbamPackage);
        verify(logger).debug("Probably concurrent package uploads", can_not_upload_package);
    }

    /**
     * If the package already exists in CBAM catalog it is not re-uploaded
     */
    @Test
    public void testIdempotentPackageUpload() throws Exception {
        CatalogAdapterVnfpackage createdPackage = new CatalogAdapterVnfpackage();
        createdPackage.setVnfdId(CBAM_VNFD_ID);
        when(cbamCatalogApi.create(uploadedFile.capture())).thenAnswer(new Answer<CatalogAdapterVnfpackage>() {
            @Override
            public CatalogAdapterVnfpackage answer(InvocationOnMock invocationOnMock) throws Throwable {
                return createdPackage;
            }
        });
        when(packageProvider.getCbamVnfdId(CSAR_ID)).thenReturn(CBAM_VNFD_ID);
        CatalogAdapterVnfpackage existingPackage = new CatalogAdapterVnfpackage();
        existingPackage.setVnfdId(CBAM_VNFD_ID);
        existingVnfPackages.add(existingPackage);
        when(cbamCatalogApi.getById(CBAM_VNFD_ID)).thenReturn(buildObservable(existingPackage));
        //when
        CatalogAdapterVnfpackage cbamPackage = cbamCatalogManager.preparePackageInCbam(VNFM_ID, CSAR_ID);
        //verify
        verify(cbamCatalogApi, never()).create(Mockito.any());
        assertEquals(existingPackage, cbamPackage);
        verify(packageProvider, never()).getPackage(CSAR_ID);
    }

    /**
     * failure to list package in CBAM results in error
     */
    @Test
    public void testFailureToListVnfPackagesInCbam() throws Exception {
        when(packageProvider.getCbamVnfdId(CSAR_ID)).thenReturn(CBAM_VNFD_ID);
        RuntimeException expectedException = new RuntimeException();
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
        when(packageProvider.getCbamVnfdId(CSAR_ID)).thenReturn(CBAM_VNFD_ID);
        CatalogAdapterVnfpackage existingPackage = new CatalogAdapterVnfpackage();
        existingPackage.setVnfdId(CBAM_VNFD_ID);
        existingVnfPackages.add(existingPackage);
        RuntimeException expectedException = new RuntimeException();
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
        when(packageProvider.getCbamVnfdId(CSAR_ID)).thenReturn(CBAM_VNFD_ID);
        byte[] onapPackageContent = TestUtil.loadFile("unittests/TestCbamCatalogManager.sample.csar");
        when(packageProvider.getPackage(CSAR_ID)).thenReturn(onapPackageContent);
        RuntimeException expectedException = new RuntimeException();
        when(cbamCatalogApi.create(Mockito.any())).thenThrow(expectedException);
        try {
            cbamCatalogManager.preparePackageInCbam(VNFM_ID, CSAR_ID);
            fail();
        } catch (Exception e) {
            verify(logger).error("Unable to create VNF with csarId CSAR identifier in package in CBAM", expectedException);
            assertEquals(expectedException, e.getCause());
        }
    }

    /**
     * the VNFD is extracted from zip
     */
    @Test
    public void testExtractVnfdFromPackage() throws Exception {
        when(cbamCatalogApi.content(CBAM_VNFD_ID)).thenReturn(buildObservable(buildResponse(TestUtil.loadFile("unittests/cbam.package.zip"))));
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
        when(cbamCatalogApi.content(CBAM_VNFD_ID)).thenReturn(buildObservable(buildResponse(TestUtil.loadFile("unittests/empty.zip"))));
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
        byte[] bytes = TestUtil.loadFile("unittests/missing.vnfd.zip");
        when(cbamCatalogApi.content(CBAM_VNFD_ID)).thenReturn(buildObservable(buildResponse(bytes)));
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

    /**
     * ETSI configuration extraction from the package
     */
    @Test
    public void testEtsiConfigurationDownload() throws Exception {
        //given
        byte[] onapPackageContent = TestUtil.loadFile("unittests/TestCbamCatalogManager.sample.csar");
        when(packageProvider.getPackage(CSAR_ID)).thenReturn(onapPackageContent);
        //when
        String etsiConfiguration = cbamCatalogManager.getEtsiConfiguration(CSAR_ID);

        assertEquals("{ \"a\" : \"b\" }\n", etsiConfiguration);
    }

    /**
     * ETSI configuration extraction from the package
     */
    @Test
    public void testEtsiConfigurationMissing() throws Exception {
        //given
        byte[] onapPackageContent = TestUtil.loadFile("unittests/missing.vnfd.zip");
        when(packageProvider.getPackage(CSAR_ID)).thenReturn(onapPackageContent);
        //when
        try {
            cbamCatalogManager.getEtsiConfiguration(CSAR_ID);
            fail();
        } catch (Exception e) {
            assertEquals("Unable to download the ETSI configuration file", e.getMessage());
            verify(logger).error("Unable to download the ETSI configuration file");
        }
    }

    private ResponseBody buildResponse(byte[] content) throws IOException {
        Headers headers = new Headers.Builder().build();
        Buffer buffer = new Buffer();
        buffer.write(content);
        BufferedSource response = buffer;
        return new RealResponseBody(headers, response);
    }
}
