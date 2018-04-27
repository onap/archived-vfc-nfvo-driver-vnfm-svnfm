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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.notification;

import com.google.gson.JsonObject;
import com.nokia.cbam.lcm.v32.model.AffectedVirtualLink;
import com.nokia.cbam.lcm.v32.model.ResourceHandle;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.onap.aai.api.NetworkApi;
import org.onap.aai.model.L3Network;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.notification.AbstractManager.buildRelationshipData;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.notification.TestGenericVnfManager.assertRelation;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.LifecycleManager.getCloudOwner;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.LifecycleManager.getRegionName;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class TestL3NetworkManager extends TestBase {
    private ArgumentCaptor<L3Network> payload = ArgumentCaptor.forClass(L3Network.class);
    private AffectedVirtualLink affectedVirtualLink = new AffectedVirtualLink();
    @Mock
    private AAIRestApiProvider aaiRestApiProvider;
    private L3NetworkManager l3NetworkManager;
    @Mock
    private NetworkApi networkApi;

    @Before
    public void init() {
        l3NetworkManager = new L3NetworkManager(aaiRestApiProvider, cbamRestApiProviderForSo);
        setField(L3NetworkManager.class, "logger", logger);
        when(aaiRestApiProvider.getNetworkApi()).thenReturn(networkApi);
    }

    /**
     * test L3 network creation
     */
    @Test
    public void testUpdate() throws Exception {
        affectedVirtualLink.setId("vlId");
        JsonObject additionalData = new JsonObject();
        additionalData.addProperty("name", "netName");
        additionalData.addProperty("tenantId", "myTenantId");
        affectedVirtualLink.setResource(new ResourceHandle());
        affectedVirtualLink.getResource().setAdditionalData(additionalData);
        affectedVirtualLink.getResource().setResourceId("netProviderId");
        L3Network existingNetwork = new L3Network();
        when(networkApi.getNetworkL3NetworksL3Network("myVnfId_vlId", null, null, null, null, null, null, null, null, null)).thenReturn(buildObservable(existingNetwork));
        when(networkApi.createOrUpdateNetworkL3NetworksL3Network(eq("myVnfId_vlId"), payload.capture())).thenReturn(VOID_OBSERVABLE.value());
        //when
        l3NetworkManager.update(VIM_ID, VNF_ID, affectedVirtualLink);
        //verify
        assertEquals("myVnfId_vlId", payload.getValue().getNetworkId());
        assertEquals("netName", payload.getValue().getNetworkName());
        assertEquals("netProviderId", payload.getValue().getNeutronNetworkId());
        assertFalse(payload.getValue().isIsBoundToVpn());
        assertFalse(payload.getValue().isIsProviderNetwork());
        assertFalse(payload.getValue().isIsSharedNetwork());
        assertFalse(payload.getValue().isIsExternalNetwork());
        assertEquals("active", payload.getValue().getOperationalStatus());
        assertRelation(payload.getValue().getRelationshipList(), "cloud-region", buildRelationshipData("cloud-region.cloud-owner", getCloudOwner(VIM_ID)), buildRelationshipData("cloud-region.cloud-region-id", getRegionName(VIM_ID)));
        assertRelation(payload.getValue().getRelationshipList(), "tenant", buildRelationshipData("cloud-region.cloud-owner", getCloudOwner(VIM_ID)), buildRelationshipData("cloud-region.cloud-region-id", getRegionName(VIM_ID)), buildRelationshipData("tenant.tenant-id", "myTenantId"));
        assertRelation(payload.getValue().getRelationshipList(), "generic-vnf", buildRelationshipData("generic-vnf.vnf-id", VNF_ID));
        VOID_OBSERVABLE.assertCalled();
    }

    /**
     * Test existing resource update
     */
    @Test
    public void testExistingUpdate() throws Exception {
        affectedVirtualLink.setId("vlId");
        JsonObject additionalData = new JsonObject();
        additionalData.addProperty("name", "netName");
        additionalData.addProperty("tenantId", "myTenantId");
        affectedVirtualLink.setResource(new ResourceHandle());
        affectedVirtualLink.getResource().setAdditionalData(additionalData);
        affectedVirtualLink.getResource().setResourceId("netProviderId");
        L3Network l3Network = new L3Network();
        l3Network.setResourceVersion("v3");
        l3Network.setRelationshipList(new ArrayList<>());
        when(networkApi.getNetworkL3NetworksL3Network("myVnfId_vlId", null, null, null, null, null, null, null, null, null)).thenReturn(buildObservable(l3Network));
        when(networkApi.createOrUpdateNetworkL3NetworksL3Network(eq("myVnfId_vlId"), payload.capture())).thenReturn(VOID_OBSERVABLE.value());
        //when
        l3NetworkManager.update(VIM_ID, VNF_ID, affectedVirtualLink);
        //verify
        assertEquals("myVnfId_vlId", payload.getValue().getNetworkId());
        assertEquals("netName", payload.getValue().getNetworkName());
        assertEquals("netProviderId", payload.getValue().getNeutronNetworkId());
        assertFalse(payload.getValue().isIsBoundToVpn());
        assertFalse(payload.getValue().isIsProviderNetwork());
        assertFalse(payload.getValue().isIsSharedNetwork());
        assertFalse(payload.getValue().isIsExternalNetwork());
        assertEquals("active", payload.getValue().getOperationalStatus());
        assertEquals("v3", payload.getValue().getResourceVersion());
        assertRelation(payload.getValue().getRelationshipList(), "cloud-region", buildRelationshipData("cloud-region.cloud-owner", getCloudOwner(VIM_ID)), buildRelationshipData("cloud-region.cloud-region-id", getRegionName(VIM_ID)));
        assertRelation(payload.getValue().getRelationshipList(), "tenant", buildRelationshipData("cloud-region.cloud-owner", getCloudOwner(VIM_ID)), buildRelationshipData("cloud-region.cloud-region-id", getRegionName(VIM_ID)), buildRelationshipData("tenant.tenant-id", "myTenantId"));
        assertRelation(payload.getValue().getRelationshipList(), "generic-vnf", buildRelationshipData("generic-vnf.vnf-id", VNF_ID));
        VOID_OBSERVABLE.assertCalled();
    }

    /**
     * test L3 network deletion
     */
    @Test
    public void testDelete() throws Exception {
        affectedVirtualLink.setId("vlId");
        L3Network l3Network = new L3Network();
        l3Network.setResourceVersion("v3");
        l3Network.setNetworkId("myVnfId_vlId");
        when(networkApi.getNetworkL3NetworksL3Network("myVnfId_vlId", null, null, null, null, null, null, null, null, null)).thenReturn(buildObservable(l3Network));
        when(networkApi.deleteNetworkL3NetworksL3Network("myVnfId_vlId", "v3")).thenReturn(VOID_OBSERVABLE.value());
        //when
        l3NetworkManager.delete(VNF_ID, affectedVirtualLink);
        //verify
        networkApi.deleteNetworkL3NetworksL3Network("myVnfId_vlId", "v3");
        VOID_OBSERVABLE.assertCalled();
    }

    /**
     * test inheritence
     */
    @Test
    public void testInheritence() {
        assertEquals(logger, l3NetworkManager.getLogger());
    }


}
