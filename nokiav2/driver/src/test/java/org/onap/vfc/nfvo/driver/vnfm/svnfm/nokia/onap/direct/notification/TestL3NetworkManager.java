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
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.onap.aai.domain.yang.v11.L3Network;
import org.onap.aai.domain.yang.v11.ObjectFactory;
import org.onap.aai.domain.yang.v11.RelationshipList;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;

import java.util.NoSuchElementException;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIRestApiProvider.AAIService.NETWORK;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.notification.AbstractManager.buildRelationshipData;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.notification.TestGenericVnfManager.assertRelation;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.LifecycleManager.getCloudOwner;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.LifecycleManager.getRegionName;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class TestL3NetworkManager extends TestBase {
    private ObjectFactory OBJECT_FACTORY = new ObjectFactory();
    private ArgumentCaptor<L3Network> payload = ArgumentCaptor.forClass(L3Network.class);
    private AffectedVirtualLink affectedVirtualLink = new AffectedVirtualLink();
    @Mock
    private AAIRestApiProvider aaiRestApiProvider;
    private L3NetworkManager l3NetworkManager;

    @Before
    public void init() {
        l3NetworkManager = new L3NetworkManager(aaiRestApiProvider, cbamRestApiProvider, driverProperties);
        setField(L3NetworkManager.class, "logger", logger);
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
        when(aaiRestApiProvider.get(logger, NETWORK, "/l3-networks/l3-network/myVnfId_vlId", L3Network.class)).thenThrow(new NoSuchElementException());
        when(aaiRestApiProvider.put(eq(logger), eq(NETWORK), eq("/l3-networks/l3-network/myVnfId_vlId"), payload.capture(), eq(Void.class))).thenReturn(null);
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
        L3Network l3Network = OBJECT_FACTORY.createL3Network();
        l3Network.setResourceVersion("v3");
        l3Network.setRelationshipList(new RelationshipList());
        when(aaiRestApiProvider.get(logger, NETWORK, "/l3-networks/l3-network/myVnfId_vlId", L3Network.class)).thenReturn(l3Network);
        when(aaiRestApiProvider.put(eq(logger), eq(NETWORK), eq("/l3-networks/l3-network/myVnfId_vlId"), payload.capture(), eq(Void.class))).thenReturn(null);
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
    }

    /**
     * test L3 network deletion
     */
    @Test
    public void testDelete() throws Exception {
        affectedVirtualLink.setId("vlId");
        //when
        l3NetworkManager.delete(VNF_ID, affectedVirtualLink);
        //verify
        verify(aaiRestApiProvider).delete(logger, NETWORK, "/l3-networks/l3-network/myVnfId_vlId");
    }

    /**
     * test inheritence
     */
    @Test
    public void testInheritence() {
        assertEquals(logger, l3NetworkManager.getLogger());
    }


}
