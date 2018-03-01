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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core;

import org.junit.Before;
import org.junit.Test;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;
import org.springframework.core.env.Environment;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.when;

public class TestIpMappingProvider extends TestBase {

    private IpMappingProvider ipMappingProvider;

    @Before
    public void init() {
        ipMappingProvider = new TestClass(environment);
    }

    /**
     * the IP addresses are correctly mapped
     */
    @Test
    public void testIpMapping() throws Exception {
        when(environment.getProperty(IpMappingProvider.IP_MAP, String.class, "")).thenReturn(" 1.2.3.4 -> 2.3.4.5 , 1.2.3.5 -> 3.4.5.6");
        //when
        ipMappingProvider.afterPropertiesSet();
        //verify
        assertEquals("2.3.4.5", ipMappingProvider.mapPrivateIpToPublicIp("1.2.3.4"));
        assertEquals(".......", ipMappingProvider.mapPrivateIpToPublicIp("......."));
        assertEquals("3.4.5.6", ipMappingProvider.mapPrivateIpToPublicIp("1.2.3.5"));
        assertEquals("1.2.3.5.", ipMappingProvider.mapPrivateIpToPublicIp("1.2.3.5."));

    }

    class TestClass extends IpMappingProvider {

        TestClass(Environment environment) {
            super(environment);
        }

    }


}
