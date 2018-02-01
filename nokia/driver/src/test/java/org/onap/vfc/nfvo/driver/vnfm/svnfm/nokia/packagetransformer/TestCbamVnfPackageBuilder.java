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

import org.junit.Test;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.impl.TestBase;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.TestUtil;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.when;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.impl.CbamCatalogManager.getFileInZip;


public class TestCbamVnfPackageBuilder extends TestBase {
    private CbamVnfPackageBuilder cbamVnfPackageBuilder = new CbamVnfPackageBuilder();

    /**
     *
     */
    @Test
    public void testEmpty() throws Exception {
        byte[] zipContent = TestUtil.loadFile("unittests/packageconverter/cbam.package.zip");
        when(systemFunctions.loadFile("cbam.pre.collectConnectionPoints.js")).thenCallRealMethod();
        when(systemFunctions.loadFile("cbam.post.collectConnectionPoints.js")).thenCallRealMethod();
        when(systemFunctions.loadFile("cbam.collectConnectionPoints.js")).thenCallRealMethod();

        //when
        byte[] modifiedContent = cbamVnfPackageBuilder.toModifiedCbamVnfPackage(zipContent, "vnfdloc/a.yaml", "modifiedContent");
        //verify
        assertFileInZip(modifiedContent, "keep/me", "kuku\n".getBytes());
        assertFileInZip(modifiedContent, "TOSCA-Metadata/TOSCA.meta", ("TOSCA-Meta-File-Version: 1.0\n" +
                "CSAR-Version: 1.1\n" +
                "Created-By: Nokia\n" +
                "Entry-Definitions: vnfdloc/a.yaml\n").getBytes());
        assertFileInZip(modifiedContent, "javascript/cbam.pre.collectConnectionPoints.js", TestUtil.loadFile("cbam.pre.collectConnectionPoints.js"));
        assertFileInZip(modifiedContent, "javascript/cbam.post.collectConnectionPoints.js", TestUtil.loadFile("cbam.post.collectConnectionPoints.js"));
        assertFileInZip(modifiedContent, "javascript/cbam.collectConnectionPoints.js", TestUtil.loadFile("cbam.collectConnectionPoints.js"));


    }

    

}
