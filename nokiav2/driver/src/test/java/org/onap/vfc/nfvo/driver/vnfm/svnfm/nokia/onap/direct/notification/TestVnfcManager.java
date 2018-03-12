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
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.onap.aai.domain.yang.v11.ObjectFactory;
import org.onap.aai.domain.yang.v11.Vnfc;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;

import java.util.NoSuchElementException;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIRestApiProvider.AAIService.NETWORK;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.notification.AbstractManager.buildRelationshipData;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.notification.TestGenericVnfManager.assertRelation;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.LifecycleManager.getCloudOwner;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.LifecycleManager.getRegionName;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class TestVnfcManager extends TestBase {
    private ObjectFactory OBJECT_FACTORY = new ObjectFactory();
    private ArgumentCaptor<Vnfc> payload = ArgumentCaptor.forClass(Vnfc.class);

    @Mock
    private AAIRestApiProvider aaiRestApiProvider;
    private VnfcManager vnfcManager;

    @Before
    public void init() {
        vnfcManager = new VnfcManager(aaiRestApiProvider, cbamRestApiProvider, driverProperties);
        setField(VnfcManager.class, "logger", logger);
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
        when(aaiRestApiProvider.get(eq(logger), eq(NETWORK), eq("/vnfcs/vnfc/myVnfId_vnfcId"), eq(Vnfc.class))).thenThrow(new NoSuchElementException());
        when(aaiRestApiProvider.put(eq(logger), eq(NETWORK), eq("/vnfcs/vnfc/myVnfId_vnfcId"), payload.capture(), eq(Void.class))).thenReturn(null);
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
        when(aaiRestApiProvider.get(eq(logger), eq(NETWORK), eq("/vnfcs/vnfc/myVnfId_vnfcId"), eq(Vnfc.class))).thenThrow(new NoSuchElementException());
        when(aaiRestApiProvider.put(eq(logger), eq(NETWORK), eq("/vnfcs/vnfc/myVnfId_vnfcId"), payload.capture(), eq(Void.class))).thenReturn(null);
        //when
        vnfcManager.delete(VNF_ID, affectedVnfc);
        //verify
        aaiRestApiProvider.delete(logger, NETWORK, "/vnfcs/vnfc/myVnfId_vnfcId");
    }

    /**
     * test inheritence
     */
    @Test
    public void testInheritence() {
        assertEquals(logger, vnfcManager.getLogger());
    }


}
