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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct;

import java.util.ArrayList;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.aai.api.CloudInfrastructureApi;
import org.onap.aai.api.ExternalSystemApi;
import org.onap.aai.model.CloudRegion;
import org.onap.aai.model.EsrSystemInfo;
import org.onap.aai.model.EsrVnfm;
import org.onap.aai.model.EsrVnfmList;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;
import org.onap.vnfmdriver.model.VimInfo;
import org.onap.vnfmdriver.model.VnfmInfo;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class TestAAIExternalSystemInfoProvider extends TestBase {
    private AAIExternalSystemInfoProvider aaiExternalSystemInfoProvider;
    @Mock
    private AAIRestApiProvider aaiRestApiProvider;
    @Mock
    private ExternalSystemApi externalSystemApi;
    @Mock
    private CloudInfrastructureApi cloudInfrastructureApi;

    @Before
    public void init() {
        setField(AAIExternalSystemInfoProvider.class, "logger", logger);
        aaiExternalSystemInfoProvider = new AAIExternalSystemInfoProvider(environment, aaiRestApiProvider);
        when(aaiRestApiProvider.getExternalSystemApi()).thenReturn(externalSystemApi);
        when(aaiRestApiProvider.getCloudInfrastructureApi()).thenReturn(cloudInfrastructureApi);
    }

    /**
     * test query VIM success scenario
     */
    @Test
    public void testVim() throws Exception {
        CloudRegion cloudRegion = new CloudRegion();
        cloudRegion.setEsrSystemInfoList(new ArrayList<>());
        EsrSystemInfo vim = new EsrSystemInfo();
        cloudRegion.getEsrSystemInfoList().add(vim);
        vim.setPassword("myPassword");
        vim.setUserName("myUsername");
        vim.setServiceUrl("http://1.2.3.4:1234/a");
        vim.setVersion("v123");
        vim.setSystemStatus("active");
        vim.setSystemName("name");
        vim.setType("type");
        vim.setSslInsecure(true);
        vim.setVendor("vendor");
        when(cloudInfrastructureApi.getCloudInfrastructureCloudRegionsCloudRegion("myCloudOwnerId", "myRegionName", null, null)).thenReturn(buildObservable(cloudRegion));
        //when
        VimInfo vimInfo = aaiExternalSystemInfoProvider.getVimInfo(VIM_ID);
        assertEquals("myPassword", vimInfo.getPassword());
        assertEquals("true", vimInfo.getSslInsecure());
        assertEquals(null, vimInfo.getSslCacert());
        assertEquals("myUsername", vimInfo.getUserName());
        assertEquals("name", vimInfo.getDescription());
        assertEquals("name", vimInfo.getName());
        assertEquals("http://1.2.3.4:1234/a", vimInfo.getUrl());
        assertEquals("active", vimInfo.getStatus());
        assertEquals("type", vimInfo.getType());
        assertEquals("v123", vimInfo.getVersion());
        assertEquals(VIM_ID, vimInfo.getVimId());
        assertEquals(null, vimInfo.getCreateTime());

    }

    /**
     * test query VIM success scenario for SSL
     */
    @Test
    public void testVimSsl() throws Exception {
        CloudRegion cloudRegion = new CloudRegion();
        cloudRegion.setEsrSystemInfoList(new ArrayList<>());
        EsrSystemInfo vim = new EsrSystemInfo();
        cloudRegion.getEsrSystemInfoList().add(vim);
        vim.setPassword("myPassword");
        vim.setUserName("myUsername");
        vim.setServiceUrl("https://1.2.3.4:1234/a");
        vim.setVersion("v123");
        vim.setSystemStatus("active");
        vim.setSystemName("name");
        vim.setType("type");
        vim.setSslInsecure(false);
        vim.setSslCacert("cert");
        vim.setVendor("vendor");
        when(cloudInfrastructureApi.getCloudInfrastructureCloudRegionsCloudRegion("myCloudOwnerId", "myRegionName", null, null)).thenReturn(buildObservable(cloudRegion));
        //when
        VimInfo vimInfo = aaiExternalSystemInfoProvider.getVimInfo(VIM_ID);
        assertEquals("myPassword", vimInfo.getPassword());
        assertEquals("false", vimInfo.getSslInsecure());
        assertEquals("cert", vimInfo.getSslCacert());
        assertEquals("myUsername", vimInfo.getUserName());
        assertEquals("name", vimInfo.getDescription());
        assertEquals("name", vimInfo.getName());
        assertEquals("https://1.2.3.4:1234/a", vimInfo.getUrl());
        assertEquals("active", vimInfo.getStatus());
        assertEquals("type", vimInfo.getType());
        assertEquals("v123", vimInfo.getVersion());
        assertEquals(VIM_ID, vimInfo.getVimId());
        assertEquals(null, vimInfo.getCreateTime());
    }

    /**
     * unable to query VIM from AAI results in error
     */
    @Test
    public void testVimUnableToQuery() throws Exception {
        RuntimeException expectedException = new RuntimeException();
        when(cloudInfrastructureApi.getCloudInfrastructureCloudRegionsCloudRegion("myCloudOwnerId", "myRegionName", null, null)).thenThrow(expectedException);
        //when
        try {
            aaiExternalSystemInfoProvider.getVimInfo(VIM_ID);
            fail();
        } catch (Exception e) {
            verify(logger).error("Unable to query VIM with myCloudOwnerId_myRegionName identifier from AAI", expectedException);
            assertEquals(expectedException, e.getCause());
        }
    }

    /**
     * test VNFM query success scenario
     */
    @Test
    public void testVnfmQuery() throws Exception {
        EsrVnfm vnfm = new EsrVnfm();
        vnfm.setVimId(VIM_ID);
        vnfm.setEsrSystemInfoList(new ArrayList<>());
        EsrSystemInfo esrInfo = new EsrSystemInfo();
        vnfm.getEsrSystemInfoList().add(esrInfo);
        esrInfo.setPassword("myPassword");
        esrInfo.setUserName("myUsername");
        esrInfo.setServiceUrl("https://1.2.3.4:1234/a");
        esrInfo.setVersion("v123");
        esrInfo.setSystemStatus("active");
        esrInfo.setSystemName("name");
        esrInfo.setType("type");
        esrInfo.setSslInsecure(false);
        esrInfo.setSslCacert("cert");
        esrInfo.setVendor("vendor");
        vnfm.setVnfmId(VNFM_ID);
        when(externalSystemApi.getExternalSystemEsrVnfmListEsrVnfm(VNFM_ID)).thenReturn(buildObservable(vnfm));

        //when
        VnfmInfo actualVnfmInfo = aaiExternalSystemInfoProvider.queryVnfmInfoFromSource(VNFM_ID);
        //verify
        assertEquals("myPassword", actualVnfmInfo.getPassword());
        assertEquals("https://1.2.3.4:1234/a", actualVnfmInfo.getUrl());
        assertEquals("myUsername", actualVnfmInfo.getUserName());
        assertEquals(null, actualVnfmInfo.getCreateTime());
        assertEquals(null, actualVnfmInfo.getDescription());
        assertEquals("name", actualVnfmInfo.getName());
        assertEquals("type", actualVnfmInfo.getType());
        assertEquals("vendor", actualVnfmInfo.getVendor());
        assertEquals("v123", actualVnfmInfo.getVersion());
        assertEquals(VIM_ID, actualVnfmInfo.getVimId());
        assertEquals(VNFM_ID, actualVnfmInfo.getVnfmId());
    }

    /**
     * unable to query VNFM from AAI results in error
     */
    @Test
    public void testVnfmUnableToQuery() throws Exception {
        RuntimeException expectedException = new RuntimeException();
        when(externalSystemApi.getExternalSystemEsrVnfmListEsrVnfm(VNFM_ID)).thenThrow(expectedException);
        //when
        try {
            aaiExternalSystemInfoProvider.queryVnfmInfoFromSource(VNFM_ID);
            fail();
        } catch (Exception e) {
            verify(logger).error("Unable to query VNFM with " + VNFM_ID + " identifier from AAI", expectedException);
            assertEquals(expectedException, e.getCause());
        }
    }

    /**
     * the list of VNFMs is retrieved from AAI
     */
    @Test
    public void testQueryAAIExternaSystemProvider() throws Exception{
        EsrVnfmList e = new EsrVnfmList();
        EsrVnfm esrVnfmItem = new EsrVnfm();
        esrVnfmItem.setVnfmId(VNFM_ID);
        e.addEsrVnfmItem(esrVnfmItem);
        when(externalSystemApi.getExternalSystemEsrVnfmList()).thenReturn(buildObservable(e));
        //when
        Set<String> vnfms = aaiExternalSystemInfoProvider.getVnfms();
        //verify
        assertEquals(1, vnfms.size());
        assertEquals(VNFM_ID, vnfms.iterator().next());
    }
}