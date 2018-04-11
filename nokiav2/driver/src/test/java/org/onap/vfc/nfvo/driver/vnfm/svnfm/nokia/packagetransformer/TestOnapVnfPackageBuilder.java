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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.packagetransformer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Base64;
import org.junit.Test;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.TestUtil;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.when;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CatalogManager.getFileInZip;


public class TestOnapVnfPackageBuilder extends TestBase {

    /**
     * The the main reads from standard in and writes to standard out
     */
    @Test
    public void testInputStreams() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream actualOut = new PrintStream(bos, true);
        when(systemFunctions.out()).thenReturn(actualOut);
        when(systemFunctions.in()).thenReturn(new ByteArrayInputStream(TestUtil.loadFile("unittests/packageconverter/cbam.package.zip")));
        when(systemFunctions.loadFile("cbam.pre.collectConnectionPoints.js")).thenCallRealMethod();
        when(systemFunctions.loadFile("cbam.collectConnectionPoints.js")).thenCallRealMethod();
        when(systemFunctions.loadFile("cbam.post.collectConnectionPoints.js")).thenCallRealMethod();
        when(systemFunctions.loadFile("TOSCA.meta")).thenCallRealMethod();
        when(systemFunctions.loadFile("MainServiceTemplate.mf")).thenCallRealMethod();

        String cbamVnfd = new String(TestUtil.loadFile("unittests/packageconverter/cbam.package.zip.vnfd"));
        String expectedOnapVnfd = new OnapVnfdBuilder().toOnapVnfd(cbamVnfd);

        //when
        OnapVnfPackageBuilder.main(null);
        //verify
        assertFileInZip(bos.toByteArray(), "TOSCA-Metadata/TOSCA.meta", TestUtil.loadFile("TOSCA.meta"));
        assertFileInZip(bos.toByteArray(), "MainServiceTemplate.yaml", expectedOnapVnfd.getBytes());
        assertFileInZip(bos.toByteArray(), "MainServiceTemplate.mf", TestUtil.loadFile("MainServiceTemplate.mf"));
        ByteArrayOutputStream actualModifiedCbamVnfPackage = getFileInZip(new ByteArrayInputStream(bos.toByteArray()), "Artifacts/Deployment/OTHER/cbam.package.zip");
        byte[] expectedModifiedCbamPackage = new CbamVnfPackageBuilder().toModifiedCbamVnfPackage(TestUtil.loadFile("unittests/packageconverter/cbam.package.zip"), "vnfdloc/a.yaml", new CbamVnfdBuilder().build(cbamVnfd));
        assertItenticalZips(expectedModifiedCbamPackage, actualModifiedCbamVnfPackage.toByteArray());
    }


    /**
     * Prevents moving the class (customer documentation) must be updated
     */
    @Test
    public void testPreventMove() {
        assertEquals("b3JnLm9uYXAudmZjLm5mdm8uZHJpdmVyLnZuZm0uc3ZuZm0ubm9raWEucGFja2FnZXRyYW5zZm9ybWVyLk9uYXBWbmZQYWNrYWdlQnVpbGRlcg==", Base64.getEncoder().encodeToString(OnapVnfPackageBuilder.class.getCanonicalName().getBytes()));
    }


}
