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

import com.google.common.collect.Lists;
import junit.framework.TestCase;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
     */
    @Test
    public void test() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream actualOut = new PrintStream(bos, true);
        when(systemFunctions.out()).thenReturn(actualOut);
        when(systemFunctions.loadFile("cbam.pre.collectConnectionPoints.js")).thenCallRealMethod();
        when(systemFunctions.loadFile("cbam.collectConnectionPoints.js")).thenCallRealMethod();
        when(systemFunctions.loadFile("cbam.post.collectConnectionPoints.js")).thenCallRealMethod();
        when(systemFunctions.loadFile("TOSCA.meta")).thenCallRealMethod();
        when(systemFunctions.loadFile("MainServiceTemplate.meta")).thenCallRealMethod();
        when(httpResponse.getOutputStream()).thenReturn(new DelegatingServletOutputStream(actualOut));
        Part part = Mockito.mock(Part.class);
        when(part.getInputStream()).thenReturn(new ByteArrayInputStream(TestUtil.loadFile("unittests/packageconverter/cbam.package.zip")));
        when(httpRequest.getParts()).thenReturn(Lists.newArrayList(part));
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
        assertFileInZip(bos.toByteArray(), "Definitions/MainServiceTemplate.yaml", expectedOnapVnfd.getBytes());
        assertFileInZip(bos.toByteArray(), "MainServiceTemplate.yaml", expectedOnapVnfd.getBytes());
        assertFileInZip(bos.toByteArray(), "MainServiceTemplate.meta", TestUtil.loadFile("MainServiceTemplate.meta"));
        ByteArrayOutputStream actualModifiedCbamVnfPackage = getFileInZip(new ByteArrayInputStream(bos.toByteArray()), "Artifacts/Deployment/OTHER/cbam.package.zip");
        byte[] expectedModifiedCbamPackage = new CbamVnfPackageBuilder().toModifiedCbamVnfPackage(TestUtil.loadFile("unittests/packageconverter/cbam.package.zip"), "vnfdloc/a.yaml", new CbamVnfdBuilder().build(cbamVnfd));
        assertItenticalZips(expectedModifiedCbamPackage, actualModifiedCbamVnfPackage.toByteArray());
    }

    @Test
    public void testDownloaderPage() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream actualOut = new PrintStream(bos, true);
        when(httpResponse.getOutputStream()).thenReturn(new DelegatingServletOutputStream(actualOut));
        when(systemFunctions.loadFile("upload.html")).thenCallRealMethod();
        //when
        converterApi.getUploadPageForConvertingVnfd(httpResponse);
        //verify
        TestCase.assertTrue(Arrays.equals(TestUtil.loadFile("upload.html"), bos.toByteArray()));
        verify(httpResponse).addHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(bos.toByteArray().length));

    }

}
