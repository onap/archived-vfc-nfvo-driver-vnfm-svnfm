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

import java.util.NoSuchElementException;
import org.junit.Before;
import org.junit.Test;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.TestUtil;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static org.springframework.test.util.ReflectionTestUtils.setField;


public class TestOnapR2VnfdBuilder extends TestBase {
    private OnapR2VnfdBuilder packageTransformer = new OnapR2VnfdBuilder();


    @Before
    public void init() {
        setField(OnapVnfdBuilder.class, "logger", logger);
    }


    /**
     * Test empty VNFD conversion
     */
    @Test
    public void testEmpty() {
        assertEquals(new String(TestUtil.loadFile("unittests/packageconverter/empty.vnfd.onap.v2.yaml")), packageTransformer.toOnapVnfd(new String(TestUtil.loadFile("unittests/packageconverter/empty.vnfd.cbam.yaml"))));
    }

    /**
     * Test all Tosca nodes conversions for successful scenario
     */
    @Test
    public void testNodes() {
        assertEquals(new String(TestUtil.loadFile("unittests/packageconverter/nodes.vnfd.onap.v2.yaml")), packageTransformer.toOnapVnfd(new String(TestUtil.loadFile("unittests/packageconverter/nodes.vnfd.cbam.yaml"))));
       /* verify(logger).warn("The {} ecp does not have an internal connection point", "myEcpWithoutIcp");
        verify(logger).warn("The {} ecp does not have an requirements section", "ecpWithIcpWithOutRequirements");
        verify(logger).warn("The {} internal connection point of the {} ecp does not have a VDU", "icpWithoutVdu", "myEcpWithoutIcpWithoutVdu");
        verify(logger).warn("The {} internal connection point of the {} ecp does not have a requirements section", "icpWithOutRequiements", "myEcpWithoutIcpWithoutIcpReq");
        verify(logger).warn("The {} internal connection point does not have a VDU", "icpWithOutVdu");
        verify(logger).warn("The {} internal connection point does not have a requirements section", "icpWithOutRequiements");
        verify(logger).warn("The {} internal connection point does not have a VL", "icpWithOutVl");
        verify(logger).warn("The {} type is not converted", "tosca.nodes.nfv.Unknown")*/
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
