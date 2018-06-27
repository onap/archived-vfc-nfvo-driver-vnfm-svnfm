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
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.TestUtil;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;

import static junit.framework.TestCase.assertEquals;


public class TestCbamVnfdBuilder extends TestBase {
    private CbamVnfdBuilder packageTransformer = new CbamVnfdBuilder();

    /**
     * test package conversion on the most minimal VNFD possible
     */
    @Test
    public void testEmpty() throws Exception {
        String out = packageTransformer.build(new String(TestUtil.loadFile("unittests/packageconverter/cbam.minimal.original.vnfd.yaml")));
        String expected = new String(TestUtil.loadFile("unittests/packageconverter/cbam.minimal.modified.vnfd.yaml"));
        assertEquals(expected, out);
    }

    /**
     * test package conversion on the most full VNFD possible
     */
    @Test
    public void testFull() throws Exception {
        String out = packageTransformer.build(new String(TestUtil.loadFile("unittests/packageconverter/cbam.full.original.vnfd.yaml")));
        String expected = new String(TestUtil.loadFile("unittests/packageconverter/cbam.full.modified.vnfd.yaml"));
        assertEquals(expected, out);
    }
}
