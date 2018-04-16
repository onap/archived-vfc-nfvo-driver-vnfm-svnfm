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

import com.nokia.cbam.lcm.v32.model.AffectedVnfc;
import com.nokia.cbam.lcm.v32.model.ResourceHandle;
import io.reactivex.Observable;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.onap.aai.api.NetworkApi;
import org.onap.aai.model.Vnfc;
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

public class TestVnfcManager extends TestBase {
    private ArgumentCaptor<Vnfc> payload = ArgumentCaptor.forClass(Vnfc.class);

    @Mock
    private AAIRestApiProvider aaiRestApiProvider;
    private VnfcManager vnfcManager;
    @Mock
    private NetworkApi networkApi;

    @Before
    public void init() {
        vnfcManager = new VnfcManager(aaiRestApiProvider, cbamRestApiProvider, driverProperties);
        setField(VnfcManager.class, "logger", logger);
        when(aaiRestApiProvider.getNetworkApi()).thenReturn(networkApi);
    }

    /**
     * test create
     */
    @Test
    public void testCreate() throws Exception {
        AffectedVnfc affectedVnfc = new AffectedVnfc();
        affectedVnfc.setComputeResource(new ResourceHandle());
        affectedVnfc.getComputeResource().setResourceId("serverProviderId");
        affectedVnfc.setId("vnfcId");
        when(networkApi.getNetworkVnfcsVnfc("myVnfId_vnfcId", null, null, null, null, null, null, null, null, null)).thenReturn(Observable.error(new NoSuchElementException()));
        when(networkApi.createOrUpdateNetworkVnfcsVnfc(eq("myVnfId_vnfcId"), payload.capture())).thenReturn(VOID_OBSERVABLE.value());
        //when
        vnfcManager.update(VIM_ID, "myTenantPrivderId", VNF_ID, affectedVnfc, true);
        //verify
        Vnfc vnfc = payload.getValue();
        assertEquals("myVnfId_vnfcId", vnfc.getVnfcName());
        assertEquals("vnfcId", vnfc.getNfcFunction());
        assertEquals("vnfcId", vnfc.getNfcNamingCode());
        assertRelation(payload.getValue().getRelationshipList(), "generic-vnf", buildRelationshipData("generic-vnf.vnf-id", VNF_ID));

        assertRelation(vnfc.getRelationshipList(), "vserver",
                buildRelationshipData("cloud-region.cloud-owner", getCloudOwner(VIM_ID)),
                buildRelationshipData("cloud-region.cloud-region-id", getRegionName(VIM_ID)),
                buildRelationshipData("tenant.tenant-id", "myTenantPrivderId"),
                buildRelationshipData("vserver.vserver-id", "serverProviderId"));
        assertEquals(2, vnfc.getRelationshipList().size());
        VOID_OBSERVABLE.assertCalled();
    }

    /**
     * test update
     */
    @Test
    public void testUpdate() throws Exception {
        AffectedVnfc affectedVnfc = new AffectedVnfc();
        affectedVnfc.setComputeResource(new ResourceHandle());
        affectedVnfc.getComputeResource().setResourceId("serverProviderId");
        affectedVnfc.setId("vnfcId");
        Vnfc existingVnfc = new Vnfc();
        existingVnfc.setRelationshipList(new ArrayList<>());
        existingVnfc.getRelationshipList().add(GenericVnfManager.linkTo("any"));
        when(networkApi.getNetworkVnfcsVnfc("myVnfId_vnfcId", null, null, null, null, null, null, null, null, null)).thenReturn(buildObservable(existingVnfc));
        when(networkApi.createOrUpdateNetworkVnfcsVnfc(eq("myVnfId_vnfcId"), payload.capture())).thenReturn(VOID_OBSERVABLE.value());
        //when
        vnfcManager.update(VIM_ID, "myTenantPrivderId", VNF_ID, affectedVnfc, true);
        //verify
        Vnfc vnfc = payload.getValue();
        assertEquals("myVnfId_vnfcId", vnfc.getVnfcName());
        assertEquals("vnfcId", vnfc.getNfcFunction());
        assertEquals("vnfcId", vnfc.getNfcNamingCode());
        assertRelation(payload.getValue().getRelationshipList(), "generic-vnf", buildRelationshipData("generic-vnf.vnf-id", VNF_ID));

        assertRelation(vnfc.getRelationshipList(), "vserver",
                buildRelationshipData("cloud-region.cloud-owner", getCloudOwner(VIM_ID)),
                buildRelationshipData("cloud-region.cloud-region-id", getRegionName(VIM_ID)),
                buildRelationshipData("tenant.tenant-id", "myTenantPrivderId"),
                buildRelationshipData("vserver.vserver-id", "serverProviderId"));
        assertEquals(2, vnfc.getRelationshipList().size());
        VOID_OBSERVABLE.assertCalled();
    }

    /**
     * test delete
     */
    @Test
    public void testDelete() throws Exception {
        AffectedVnfc affectedVnfc = new AffectedVnfc();
        affectedVnfc.setComputeResource(new ResourceHandle());
        affectedVnfc.getComputeResource().setResourceId("serverProviderId");
        affectedVnfc.setId("vnfcId");
        Vnfc existingVnfc = new Vnfc();
        existingVnfc.setResourceVersion("v3");
        existingVnfc.setVnfcName("myVnfId_vnfcId");
        when(networkApi.getNetworkVnfcsVnfc("myVnfId_vnfcId", null, null, null, null, null, null, null, null, null)).thenReturn(buildObservable(existingVnfc));
        when(networkApi.deleteNetworkVnfcsVnfc("myVnfId_vnfcId", "v3")).thenReturn(VOID_OBSERVABLE.value());
        //when
        vnfcManager.delete(VNF_ID, affectedVnfc);
        //verify
        verify(networkApi).deleteNetworkVnfcsVnfc("myVnfId_vnfcId", "v3");
        VOID_OBSERVABLE.assertCalled();
    }

    /**
     * test VNFC id conversion
     */
    @Test
    public void testCbamId() {
        assertEquals("b", VnfcManager.buildCbamId("a_b"));
    }


    /**
     * test inheritence
     */
    @Test
    public void testInheritence() {
        assertEquals(logger, vnfcManager.getLogger());
    }


}
