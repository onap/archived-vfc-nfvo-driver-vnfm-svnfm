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
import org.onap.vfccatalog.ApiClient;
import org.onap.vfccatalog.api.VnfpackageApi;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static org.mockito.Mockito.when;

public class TestVfcRestApiProvider extends TestBase {
    private VfcRestApiProvider vfcRestApiProvider;

    @Before
    public void init() {
        vfcRestApiProvider = new VfcRestApiProvider(msbApiProvider);
    }

    /**
     * the base URL of the LCM API is set
     */
    @Test
    public void testNsLcmApi() throws Exception {
        when(msbApiProvider.getMicroServiceUrl(VfcRestApiProvider.NSLCM_API_SERVICE_NAME, VfcRestApiProvider.NSLCM_API_VERION)).thenReturn("http://1.2.3.4:1234/nslcm/v1/lead/");
        //when
        org.onap.vnfmdriver.ApiClient apiClient = vfcRestApiProvider.buildNslcmApiClient();
        //verify
        assertEquals("http://1.2.3.4:1234/lead/", apiClient.getAdapterBuilder().build().baseUrl().toString());
    }

    /**
     * the base URL of the Catalog API is set
     */
    @Test
    public void testNsCatalogApi() throws Exception {
        when(msbApiProvider.getMicroServiceUrl(VfcRestApiProvider.NSCATALOG_SERVICE_NAME, VfcRestApiProvider.NSCATALOG_API_VERSION)).thenReturn("http://1.2.3.4:1234/lead/");
        //when
        ApiClient apiClient = vfcRestApiProvider.buildCatalogApiClient();
        //verify
        assertEquals("http://1.2.3.4:1234/lead/", apiClient.getAdapterBuilder().build().baseUrl().toString());
    }

    @Test
    public void testNsLcm(){
        when(msbApiProvider.getMicroServiceUrl(VfcRestApiProvider.NSLCM_API_SERVICE_NAME, VfcRestApiProvider.NSLCM_API_VERION)).thenReturn("http://1.2.3.4:1234/nslcm/v1/lead/");
        //when
        //verify
        assertNotNull(vfcRestApiProvider.getNsLcmApi());
    }

    @Test
    public void testNsCatalog(){
        when(msbApiProvider.getMicroServiceUrl(VfcRestApiProvider.NSCATALOG_SERVICE_NAME, VfcRestApiProvider.NSCATALOG_API_VERSION)).thenReturn("http://1.2.3.4:1234/lead/");
        //when
        VnfpackageApi catalogApi = vfcRestApiProvider.getVfcCatalogApi();
        //verify
        assertNotNull(catalogApi);
    }
}
