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

import java.util.NoSuchElementException;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;


public class TestOnapVnfdBuilder extends TestBase {
    private OnapVnfdBuilder packageTransformer = new OnapVnfdBuilder();

    /**
     * Test empty VNFD conversion
     */
    @Test
    public void testEmpty() {
        assertEquals(new String(TestUtil.loadFile("unittests/packageconverter/empty.vnfd.onap.yaml")), packageTransformer.toOnapVnfd(new String(TestUtil.loadFile("unittests/packageconverter/empty.vnfd.cbam.yaml"))));
    }

    /**
     * Test all Tosca nodes conversions for successful scenario
     */
    @Test
    public void testNodes() {
        assertEquals(new String(TestUtil.loadFile("unittests/packageconverter/nodes.vnfd.onap.yaml")), packageTransformer.toOnapVnfd(new String(TestUtil.loadFile("unittests/packageconverter/nodes.vnfd.cbam.yaml"))));
    }

    /**
     * if a node refers to a non existing node it results in a failure
     */
    @Test
    public void testInconsitentVnfd() {
        try {
            packageTransformer.toOnapVnfd(new String(TestUtil.loadFile("unittests/packageconverter/nodes.vnfd.inconsistent.cbam.yaml")));
            fail();
        } catch (NoSuchElementException e) {
            assertEquals("The VNFD does not have a node called myComputeMissing but required by an other node", e.getMessage());
        }
    }

}
