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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.vfc;

import org.junit.Before;
import org.junit.Test;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;
import org.onap.vfccatalog.api.VnfpackageApi;
import org.onap.vnfmdriver.api.NslcmApi;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;
import static org.mockito.Mockito.when;

public class TestVfcRestApiProvider extends TestBase {
    private VfcRestApiProvider vfcRestApiProvider;

    @Before
    public void init() {
        vfcRestApiProvider = new VfcRestApiProvider(msbApiProvider);
    }

    /**
     * test VF-C NSLCM API retrieval
     */
    @Test
    public void testNsLcmApi() throws Exception {
        when(msbApiProvider.getMicroServiceUrl(VfcRestApiProvider.NSLCM_API_SERVICE_NAME, VfcRestApiProvider.NSLCM_API_VERION)).thenReturn("http://1.2.3.4:1234/nslcm/v1/lead");
        //when
        NslcmApi nsLcmApi = vfcRestApiProvider.getNsLcmApi();
        //verify
        assertEquals("http://1.2.3.4:1234/lead", nsLcmApi.getApiClient().getBasePath());
        assertNull(nsLcmApi.getApiClient().getSslCaCert());
        assertEquals(0, nsLcmApi.getApiClient().getAuthentications().size());
    }

    /**
     * test VF-C catalog API retrieval
     */
    @Test
    public void testNsCatalogApi() throws Exception {
        when(msbApiProvider.getMicroServiceUrl(VfcRestApiProvider.NSCATALOG_SERVICE_NAME, VfcRestApiProvider.NSCATALOG_API_VERSION)).thenReturn("http://1.2.3.4:1234/lead");
        //when
        VnfpackageApi nsCatalogApi = vfcRestApiProvider.getOnapCatalogApi();
        //verify
        assertEquals("http://1.2.3.4:1234/lead", nsCatalogApi.getApiClient().getBasePath());
        assertNull(nsCatalogApi.getApiClient().getSslCaCert());
        assertEquals(0, nsCatalogApi.getApiClient().getAuthentications().size());
    }
}
