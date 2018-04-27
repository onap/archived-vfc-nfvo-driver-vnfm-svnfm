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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestVnfmCredentials {

    @Test
    public void testPojo() {
        VnfmCredentials vnfmCredentials = new VnfmCredentials("myUsername", "myPassword", "myClientId", "myClientSecret");
        assertEquals("myUsername", vnfmCredentials.getUsername());
        assertEquals("myClientId", vnfmCredentials.getClientId());
        assertEquals("myPassword", vnfmCredentials.getPassword());
        assertEquals("myClientSecret", vnfmCredentials.getClientSecret());
        assertEquals("VnfmCredentials{username='myUsername', password='450ad03db9395dfccb5e03066fd7f16cfba2b61e23d516373714471459052ec90a9a4bf3a151e600ea8aaed36e3b8c21a3d38ab1705839749d130da4380f1448', clientId='myClientId', clientSecret='ce1b1f932289546075ea7f98928cf9948181c1b72e12f61a244e1a49d85f52afa74dcb3b290b8eae2b7e26c3bebcd798a641e43533144e9624be741f8827065c'}", vnfmCredentials.toString());
        assertTrue(!vnfmCredentials.toString().contains("myPassword"));
        assertTrue(!vnfmCredentials.toString().contains("myClientSecret"));
    }
}