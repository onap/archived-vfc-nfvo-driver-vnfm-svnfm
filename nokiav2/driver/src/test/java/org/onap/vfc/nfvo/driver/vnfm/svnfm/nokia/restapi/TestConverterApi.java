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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.restapi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import org.apache.http.entity.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.packagetransformer.CbamVnfPackageBuilder;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.packagetransformer.CbamVnfdBuilder;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.packagetransformer.OnapVnfdBuilder;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.TestUtil;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.DelegatingServletOutputStream;

import static junit.framework.TestCase.*;
import static org.mockito.Mockito.*;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CatalogManager.getFileInZip;
import static org.springframework.test.util.ReflectionTestUtils.setField;


public class TestConverterApi extends TestBase {

    @InjectMocks
    private ConverterApi converterApi;
    @Mock
    private HttpServletRequest httpRequest;

    @Before
    public void initMocks() throws Exception {
        setField(ConverterApi.class, "logger", logger);
    }

    /**
     * test VNF package conversion success scenario
     */
    @Test
    public void testConversion() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream actualOut = new PrintStream(bos, true);
        when(systemFunctions.out()).thenReturn(actualOut);
        when(systemFunctions.loadFile("cbam.pre.collectConnectionPoints.js")).thenCallRealMethod();
        when(systemFunctions.loadFile("cbam.collectConnectionPoints.js")).thenCallRealMethod();
        when(systemFunctions.loadFile("cbam.post.collectConnectionPoints.js")).thenCallRealMethod();
        when(systemFunctions.loadFile("TOSCA.meta")).thenCallRealMethod();
        when(systemFunctions.loadFile("MainServiceTemplate.mf")).thenCallRealMethod();
        when(httpResponse.getOutputStream()).thenReturn(new DelegatingServletOutputStream(actualOut));
        Part part = Mockito.mock(Part.class);
        when(part.getInputStream()).thenReturn(new ByteArrayInputStream(TestUtil.loadFile("unittests/packageconverter/cbam.package.zip")));
        when(httpRequest.getPart("fileToUpload")).thenReturn(part);
        Part part2 = Mockito.mock(Part.class);
        when(httpRequest.getPart("version")).thenReturn(part2);
        when(part2.getInputStream()).thenReturn(new ByteArrayInputStream("V1".getBytes()));
        //when
        converterApi.convert(httpResponse, httpRequest);
        //verify
        verifyVnfPackageWritterToOutputStream(bos);
        verify(httpResponse).addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_OCTET_STREAM.getMimeType());
        verify(httpResponse).setStatus(HttpStatus.OK.value());
        verify(httpResponse).addHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(bos.toByteArray().length));
        verify(httpResponse).addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + "core.csar" + "\"");
    }

    private void verifyVnfPackageWritterToOutputStream(ByteArrayOutputStream bos) throws Exception {
        String cbamVnfd = new String(TestUtil.loadFile("unittests/packageconverter/cbam.package.zip.vnfd"));
        String expectedOnapVnfd = new OnapVnfdBuilder().toOnapVnfd(cbamVnfd);
        assertFileInZip(bos.toByteArray(), "TOSCA-Metadata/TOSCA.meta", TestUtil.loadFile("TOSCA.meta"));
        assertFileInZip(bos.toByteArray(), "MainServiceTemplate.yaml", expectedOnapVnfd.getBytes());
        assertFileInZip(bos.toByteArray(), "MainServiceTemplate.mf", TestUtil.loadFile("MainServiceTemplate.mf"));
        ByteArrayOutputStream actualModifiedCbamVnfPackage = getFileInZip(new ByteArrayInputStream(bos.toByteArray()), "Artifacts/Deployment/OTHER/cbam.package.zip");
        byte[] expectedModifiedCbamPackage = new CbamVnfPackageBuilder().toModifiedCbamVnfPackage(TestUtil.loadFile("unittests/packageconverter/cbam.package.zip"), "vnfdloc/a.yaml", new CbamVnfdBuilder().build(cbamVnfd));
        assertItenticalZips(expectedModifiedCbamPackage, actualModifiedCbamVnfPackage.toByteArray());
    }

    /**
     * the HTML based converted page works
     */
    @Test
    public void testConverterPage() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream actualOut = new PrintStream(bos, true);
        when(httpResponse.getOutputStream()).thenReturn(new DelegatingServletOutputStream(actualOut));
        when(systemFunctions.loadFile("upload.html")).thenCallRealMethod();
        //when
        converterApi.getUploadPageForConvertingVnfd(httpResponse);
        //verify
        assertTrue(Arrays.equals(TestUtil.loadFile("upload.html"), bos.toByteArray()));
        verify(httpResponse).addHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(bos.toByteArray().length));
    }

    /**
     * error is propagated if unable to extract package from HTTP request
     */
    @Test
    public void testUnableToExtractPackageToBeConverted() throws Exception {
        IOException expectedException = new IOException();
        when(httpRequest.getPart("fileToUpload")).thenThrow(expectedException);
        Part part = Mockito.mock(Part.class);
        when(httpRequest.getPart("version")).thenReturn(part);
        when(part.getInputStream()).thenReturn(new ByteArrayInputStream("V1".getBytes()));
        try {
            converterApi.convert(httpResponse, httpRequest);
            fail();
        } catch (Exception e) {
            verify(logger).error("Unable to extract package from REST parameters", expectedException);
            assertEquals("Unable to extract package from REST parameters", e.getMessage());
            assertEquals(expectedException, e.getCause());
        }
    }

    /**
     * error is propagated if unable to extract package from HTTP request
     */
    @Test
    public void testUnableToConvertPackage() throws Exception {
        Part part = Mockito.mock(Part.class);
        when(part.getInputStream()).thenReturn(new ByteArrayInputStream(TestUtil.loadFile("unittests/packageconverter/cbam.package.zip")));
        when(httpRequest.getPart("fileToUpload")).thenReturn(part);

        Part part2 = Mockito.mock(Part.class);
        when(httpRequest.getPart("version")).thenReturn(part2);
        when(part2.getInputStream()).thenReturn(new ByteArrayInputStream("V1".getBytes()));
        try {
            converterApi.convert(httpResponse, httpRequest);
            fail();
        } catch (Exception e) {
            verify(logger).error(eq("Unable to convert VNF package"), any(RuntimeException.class));
            assertEquals("Unable to convert VNF package", e.getMessage());
        }
    }
}
