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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.nokia.cbam.lcm.v32.model.AffectedVirtualStorage;
import com.nokia.cbam.lcm.v32.model.AffectedVnfc;
import com.nokia.cbam.lcm.v32.model.ResourceHandle;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.onap.aai.api.CloudInfrastructureApi;
import org.onap.aai.model.Relationship;
import org.onap.aai.model.Vserver;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.notification.AbstractManager.buildRelationshipData;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.notification.TestGenericVnfManager.assertRelation;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.LifecycleManager.getCloudOwner;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.LifecycleManager.getRegionName;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class TestVserverManager extends TestBase {
    private ArgumentCaptor<Vserver> payload = ArgumentCaptor.forClass(Vserver.class);

    @Mock
    private AAIRestApiProvider aaiRestApiProvider;
    private VserverManager vserverManager;
    @Mock
    private CloudInfrastructureApi cloudInfrastructureApi;

    @Before
    public void init() {
        vserverManager = new VserverManager(aaiRestApiProvider, cbamRestApiProvider, driverProperties);
        setField(VserverManager.class, "logger", logger);
        when(aaiRestApiProvider.getCloudInfrastructureApi()).thenReturn(cloudInfrastructureApi);
    }

    /**
     * test update
     */
    @Test
    public void testUpdate() throws Exception {
        AffectedVnfc affectedVnfc = new AffectedVnfc();
        affectedVnfc.setComputeResource(new ResourceHandle());
        affectedVnfc.getComputeResource().setResourceId("serverProviderId");
        JsonObject additionalData = new JsonObject();
        additionalData.addProperty("name", "serverName");
        additionalData.addProperty("tenantId", "myTenantId");
        JsonArray links = new JsonArray();
        links.add(new JsonObject());
        JsonObject nonRelSelfink = new JsonObject();
        nonRelSelfink.addProperty("rel", "self2");
        nonRelSelfink.addProperty("href", "url");
        links.add(nonRelSelfink);

        JsonObject link = new JsonObject();
        link.addProperty("rel", "self");
        link.addProperty("href", "url");
        links.add(link);
        additionalData.add("links", links);
        affectedVnfc.getComputeResource().setAdditionalData(additionalData);
        affectedVnfc.setId("vnfcId");
        List<AffectedVirtualStorage> affectedStorages = new ArrayList<>();
        affectedStorages.add(new AffectedVirtualStorage());
        affectedStorages.get(0).setId("sId");
        affectedStorages.get(0).setResource(new ResourceHandle());
        affectedStorages.get(0).getResource().setResourceId("storageProviderId");
        affectedVnfc.setStorageResourceIds(new ArrayList<>());
        affectedVnfc.getStorageResourceIds().add("sId");

        Vserver existingVserver = new Vserver();
        existingVserver.setRelationshipList(new ArrayList<>());
        existingVserver.getRelationshipList().add(VserverManager.linkTo(VIM_ID, "tenantId", "serverId2"));
        when(cloudInfrastructureApi.getCloudInfrastructureCloudRegionsCloudRegionTenantsTenantVserversVserver("myCloudOwnerId", "myRegionName", "myTenantId", "serverProviderId", null, null, null, null, null, null, null, null, null)).thenReturn(buildObservable(existingVserver));
        when(cloudInfrastructureApi.createOrUpdateCloudInfrastructureCloudRegionsCloudRegionTenantsTenantVserversVserver(eq("myCloudOwnerId"), eq("myRegionName"), eq("myTenantId"), eq("serverProviderId"), payload.capture())).thenReturn(VOID_OBSERVABLE.value());
        //when
        vserverManager.update(VIM_ID, VNF_ID, affectedVnfc, affectedStorages, true);
        //verify
        Vserver vserver = payload.getValue();
        assertEquals("serverProviderId", vserver.getVserverId());
        assertEquals("active", vserver.getProvStatus());
        assertEquals("serverName", vserver.getVserverName());
        assertEquals("url", vserver.getVserverSelflink());
        assertEquals(1, vserver.getVolumes().size());
        assertEquals("storageProviderId", vserver.getVolumes().get(0).getVolumeId());
        assertEquals(2, vserver.getRelationshipList().size());
        VOID_OBSERVABLE.assertCalled();
    }

    /**
     * test missing links
     */
    @Test
    public void testUpdateEmptyLinks() throws Exception {
        AffectedVnfc affectedVnfc = new AffectedVnfc();
        affectedVnfc.setComputeResource(new ResourceHandle());
        affectedVnfc.getComputeResource().setResourceId("serverProviderId");
        JsonObject additionalData = new JsonObject();
        additionalData.addProperty("name", "serverName");
        additionalData.addProperty("tenantId", "myTenantId");
        JsonArray links = new JsonArray();
        additionalData.add("links", links);
        affectedVnfc.getComputeResource().setAdditionalData(additionalData);
        affectedVnfc.setId("vnfcId");
        List<AffectedVirtualStorage> affectedStorages = new ArrayList<>();
        affectedStorages.add(new AffectedVirtualStorage());
        affectedStorages.get(0).setId("sId");
        affectedStorages.get(0).setResource(new ResourceHandle());
        affectedStorages.get(0).getResource().setResourceId("storageProviderId");
        affectedVnfc.setStorageResourceIds(new ArrayList<>());
        affectedVnfc.getStorageResourceIds().add("sId");

        Vserver existingVserver = new Vserver();
        existingVserver.setVolumes(new ArrayList<>());
        when(cloudInfrastructureApi.getCloudInfrastructureCloudRegionsCloudRegionTenantsTenantVserversVserver("myCloudOwnerId", "myRegionName", "myTenantId", "serverProviderId", null, null, null, null, null, null, null, null, null)).thenReturn(buildObservable(existingVserver));
        when(cloudInfrastructureApi.createOrUpdateCloudInfrastructureCloudRegionsCloudRegionTenantsTenantVserversVserver(eq("myCloudOwnerId"), eq("myRegionName"), eq("myTenantId"), eq("serverProviderId"), payload.capture())).thenReturn(VOID_OBSERVABLE.value());
        //when
        vserverManager.update(VIM_ID, VNF_ID, affectedVnfc, affectedStorages, true);
        //verify
        Vserver vserver = payload.getValue();
        assertEquals("serverProviderId", vserver.getVserverId());
        assertEquals("active", vserver.getProvStatus());
        assertEquals("serverName", vserver.getVserverName());
        assertEquals("unknown", vserver.getVserverSelflink());
        assertEquals(1, vserver.getVolumes().size());
        assertEquals("storageProviderId", vserver.getVolumes().get(0).getVolumeId());
        VOID_OBSERVABLE.assertCalled();
    }

    /**
     * test update when links is not present on vServer
     */
    @Test
    public void testUpdateWithNoLinks() throws Exception {
        AffectedVnfc affectedVnfc = new AffectedVnfc();
        affectedVnfc.setComputeResource(new ResourceHandle());
        affectedVnfc.getComputeResource().setResourceId("serverProviderId");
        JsonObject additionalData = new JsonObject();
        additionalData.addProperty("name", "serverName");
        additionalData.addProperty("tenantId", "myTenantId");
        affectedVnfc.getComputeResource().setAdditionalData(additionalData);
        affectedVnfc.setId("vnfcId");
        List<AffectedVirtualStorage> affectedStorages = new ArrayList<>();
        affectedStorages.add(new AffectedVirtualStorage());
        affectedStorages.get(0).setId("sId");
        affectedStorages.get(0).setResource(new ResourceHandle());
        affectedStorages.get(0).getResource().setResourceId("storageProviderId");
        affectedVnfc.setStorageResourceIds(new ArrayList<>());
        affectedVnfc.getStorageResourceIds().add("sId");

        Vserver existingVserver = new Vserver();
        existingVserver.setVolumes(new ArrayList<>());
        when(cloudInfrastructureApi.getCloudInfrastructureCloudRegionsCloudRegionTenantsTenantVserversVserver("myCloudOwnerId", "myRegionName", "myTenantId", "serverProviderId", null, null, null, null, null, null, null, null, null)).thenReturn(buildObservable(existingVserver));
        when(cloudInfrastructureApi.createOrUpdateCloudInfrastructureCloudRegionsCloudRegionTenantsTenantVserversVserver(eq("myCloudOwnerId"), eq("myRegionName"), eq("myTenantId"), eq("serverProviderId"), payload.capture())).thenReturn(VOID_OBSERVABLE.value());
        //when
        vserverManager.update(VIM_ID, VNF_ID, affectedVnfc, affectedStorages, true);
        //verify
        Vserver vserver = payload.getValue();
        assertEquals("serverProviderId", vserver.getVserverId());
        assertEquals("active", vserver.getProvStatus());
        assertEquals("serverName", vserver.getVserverName());
        assertEquals("unknown", vserver.getVserverSelflink());
        assertEquals(1, vserver.getVolumes().size());
        assertEquals("storageProviderId", vserver.getVolumes().get(0).getVolumeId());
        VOID_OBSERVABLE.assertCalled();
    }

    /**
     * test update when removing volumes
     */
    @Test
    public void testUpdateWithRemovingVolumes() throws Exception {
        AffectedVnfc affectedVnfc = new AffectedVnfc();
        affectedVnfc.setComputeResource(new ResourceHandle());
        affectedVnfc.getComputeResource().setResourceId("serverProviderId");
        JsonObject additionalData = new JsonObject();
        additionalData.addProperty("name", "serverName");
        additionalData.addProperty("tenantId", "myTenantId");
        affectedVnfc.getComputeResource().setAdditionalData(additionalData);
        affectedVnfc.setId("vnfcId");
        List<AffectedVirtualStorage> affectedStorages = new ArrayList<>();
        Vserver existingVserver = new Vserver();
        existingVserver.setVolumes(new ArrayList<>());
        when(cloudInfrastructureApi.getCloudInfrastructureCloudRegionsCloudRegionTenantsTenantVserversVserver("myCloudOwnerId", "myRegionName", "myTenantId", "serverProviderId", null, null, null, null, null, null, null, null, null)).thenReturn(buildObservable(existingVserver));
        when(cloudInfrastructureApi.createOrUpdateCloudInfrastructureCloudRegionsCloudRegionTenantsTenantVserversVserver(eq("myCloudOwnerId"), eq("myRegionName"), eq("myTenantId"), eq("serverProviderId"), payload.capture())).thenReturn(VOID_OBSERVABLE.value());
        //when
        vserverManager.update(VIM_ID, VNF_ID, affectedVnfc, affectedStorages, true);
        //verify
        Vserver vserver = payload.getValue();
        assertEquals("serverProviderId", vserver.getVserverId());
        assertEquals("active", vserver.getProvStatus());
        assertEquals("serverName", vserver.getVserverName());
        assertEquals("unknown", vserver.getVserverSelflink());
        assertEquals(0, vserver.getVolumes().size());
    }

    /**
     * test delete
     */
    @Test
    public void testDelete() throws Exception {
        AffectedVnfc affectedVnfc = new AffectedVnfc();
        affectedVnfc.setComputeResource(new ResourceHandle());
        affectedVnfc.getComputeResource().setResourceId("serverProviderId");
        JsonObject additionalData = new JsonObject();
        additionalData.addProperty("name", "serverName");
        additionalData.addProperty("tenantId", "myTenantId");
        affectedVnfc.getComputeResource().setAdditionalData(additionalData);
        affectedVnfc.setId("vnfcId");
        Vserver existingVserver = new Vserver();
        existingVserver.setResourceVersion("v3");
        existingVserver.setVserverId("serverProviderId");
        when(cloudInfrastructureApi.getCloudInfrastructureCloudRegionsCloudRegionTenantsTenantVserversVserver("myCloudOwnerId", "myRegionName", "myTenantId", "serverProviderId", null, null, null, null, null, null, null, null, null)).thenReturn(buildObservable(existingVserver));
        when(cloudInfrastructureApi.createOrUpdateCloudInfrastructureCloudRegionsCloudRegionTenantsTenantVserversVserver(eq("myCloudOwnerId"), eq("myRegionName"), eq("myTenantId"), eq("serverProviderId"), payload.capture())).thenReturn(null);

        //when
        vserverManager.delete(VIM_ID, affectedVnfc);
        //verify
        verify(cloudInfrastructureApi).deleteCloudInfrastructureCloudRegionsCloudRegionTenantsTenantVserversVserver("myCloudOwnerId", "myRegionName", "myTenantId", "serverProviderId", "v3");
        VOID_OBSERVABLE.assertCalled();
    }

    @Test
    public void testLinks() {
        Relationship relationship = VserverManager.linkTo(VIM_ID, "myTenantPrivderId", "serverProviderId");
        List<Relationship> relationships = new ArrayList<>();
        relationships.add(relationship);
        assertRelation(relationships, "vserver",
                buildRelationshipData("cloud-region.cloud-owner", getCloudOwner(VIM_ID)),
                buildRelationshipData("cloud-region.cloud-region-id", getRegionName(VIM_ID)),
                buildRelationshipData("tenant.tenant-id", "myTenantPrivderId"),
                buildRelationshipData("vserver.vserver-id", "serverProviderId"));
    }

    /**
     * test inheritence
     */
    @Test
    public void testInheritence() {
        assertEquals(logger, vserverManager.getLogger());
    }


}
