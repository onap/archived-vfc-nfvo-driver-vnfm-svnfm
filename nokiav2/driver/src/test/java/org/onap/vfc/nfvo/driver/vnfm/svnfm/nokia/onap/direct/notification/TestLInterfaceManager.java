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

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.onap.aai.domain.yang.v11.LInterface;
import org.onap.aai.domain.yang.v11.ObjectFactory;
import org.onap.aai.domain.yang.v11.RelationshipList;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification.ReportedAffectedCp;

import java.util.NoSuchElementException;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIRestApiProvider.AAIService.CLOUD;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.notification.AbstractManager.buildRelationshipData;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.notification.TestGenericVnfManager.assertRelation;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class TestLInterfaceManager extends TestBase {
    private ObjectFactory OBJECT_FACTORY = new ObjectFactory();
    private ArgumentCaptor<LInterface> payload = ArgumentCaptor.forClass(LInterface.class);

    @Mock
    private AAIRestApiProvider aaiRestApiProvider;
    private LInterfaceManager lInterfaceManager;

    @Before
    public void init() {
        lInterfaceManager = new LInterfaceManager(aaiRestApiProvider, cbamRestApiProvider, driverProperties);
        setField(LInterfaceManager.class, "logger", logger);
    }

    /**
     * test update success scenario
     */
    @Test
    public void testUpdate() throws Exception {
        ReportedAffectedCp affectedCp = new ReportedAffectedCp();
        affectedCp.setCpdId("cpdId");
        affectedCp.setTenantId("myTenantId");
        affectedCp.setProviderId("portProviderId");
        affectedCp.setServerProviderId("serverProviderId");
        affectedCp.setNetworkProviderId("networkProviderId");
        affectedCp.setMacAddress("mac");
        affectedCp.setIpAddress("1.2.3.4");
        affectedCp.setEcpdId("ecpdId");
        affectedCp.setName("name");
        affectedCp.setCpId("cpId");
        when(aaiRestApiProvider.get(eq(logger), eq(CLOUD), eq("/cloud-regions/cloud-region/myCloudOwnerId/myRegionName/tenants/tenant/myTenantId/vservers/vserver/serverProviderId/l-interfaces/l-interface/cpId"), eq(LInterface.class))).thenThrow(new NoSuchElementException());
        when(aaiRestApiProvider.put(eq(logger), eq(CLOUD), eq("/cloud-regions/cloud-region/myCloudOwnerId/myRegionName/tenants/tenant/myTenantId/vservers/vserver/serverProviderId/l-interfaces/l-interface/cpId"), payload.capture(), eq(Void.class))).thenReturn(null);
        //when
        lInterfaceManager.update(VNF_ID, VIM_ID, affectedCp, true);
        //verify
        LInterface actualInterface = payload.getValue();
        assertEquals(true, actualInterface.isInMaint());
        assertEquals(false, actualInterface.isIsIpUnnumbered());
        assertEquals(false, actualInterface.isIsPortMirrored());
        assertEquals("name", actualInterface.getInterfaceName());
        assertEquals("cpId", actualInterface.getInterfaceId());
        assertEquals("cpdId", actualInterface.getInterfaceRole());
        assertEquals("mac", actualInterface.getMacaddr());
        assertEquals("active", actualInterface.getProvStatus());
        assertEquals(1, actualInterface.getL3InterfaceIpv4AddressList().size());
        assertEquals(0, actualInterface.getL3InterfaceIpv6AddressList().size());
        assertEquals("networkProviderId", actualInterface.getL3InterfaceIpv4AddressList().get(0).getNeutronNetworkId());
        assertEquals("1.2.3.4", actualInterface.getL3InterfaceIpv4AddressList().get(0).getL3InterfaceIpv4Address());
        assertRelation(actualInterface.getRelationshipList(), "generic-vnf", buildRelationshipData("generic-vnf.vnf-id", VNF_ID));
    }

    /**
     * test update success scenario without IP
     */
    @Test
    public void testUpdateWithoutIp() throws Exception {
        ReportedAffectedCp affectedCp = new ReportedAffectedCp();
        affectedCp.setCpdId("cpdId");
        affectedCp.setTenantId("myTenantId");
        affectedCp.setProviderId("portProviderId");
        affectedCp.setServerProviderId("serverProviderId");
        affectedCp.setNetworkProviderId("networkProviderId");
        affectedCp.setMacAddress("mac");
        affectedCp.setEcpdId("ecpdId");
        affectedCp.setName("name");
        affectedCp.setCpId("cpId");
        when(aaiRestApiProvider.get(eq(logger), eq(CLOUD), eq("/cloud-regions/cloud-region/myCloudOwnerId/myRegionName/tenants/tenant/myTenantId/vservers/vserver/serverProviderId/l-interfaces/l-interface/cpId"), eq(LInterface.class))).thenThrow(new NoSuchElementException());
        when(aaiRestApiProvider.put(eq(logger), eq(CLOUD), eq("/cloud-regions/cloud-region/myCloudOwnerId/myRegionName/tenants/tenant/myTenantId/vservers/vserver/serverProviderId/l-interfaces/l-interface/cpId"), payload.capture(), eq(Void.class))).thenReturn(null);
        //when
        lInterfaceManager.update(VNF_ID, VIM_ID, affectedCp, true);
        //verify
        LInterface actualInterface = payload.getValue();
        assertEquals(true, actualInterface.isInMaint());
        assertEquals(false, actualInterface.isIsIpUnnumbered());
        assertEquals(false, actualInterface.isIsPortMirrored());
        assertEquals("name", actualInterface.getInterfaceName());
        assertEquals("cpId", actualInterface.getInterfaceId());
        assertEquals("cpdId", actualInterface.getInterfaceRole());
        assertEquals("mac", actualInterface.getMacaddr());
        assertEquals("active", actualInterface.getProvStatus());
        assertEquals(0, actualInterface.getL3InterfaceIpv6AddressList().size());
        assertEquals(0, actualInterface.getL3InterfaceIpv4AddressList().size());
        assertRelation(actualInterface.getRelationshipList(), "generic-vnf", buildRelationshipData("generic-vnf.vnf-id", VNF_ID));
    }

    /**
     * test update success scenario
     */
    @Test
    public void testExistingUpdate() throws Exception {
        ReportedAffectedCp affectedCp = new ReportedAffectedCp();
        affectedCp.setCpdId("cpdId");
        affectedCp.setTenantId("myTenantId");
        affectedCp.setProviderId("portProviderId");
        affectedCp.setServerProviderId("serverProviderId");
        affectedCp.setNetworkProviderId("networkProviderId");
        affectedCp.setMacAddress("mac");
        affectedCp.setIpAddress("1.2.3.4");
        affectedCp.setEcpdId("ecpdId");
        affectedCp.setName("name");
        affectedCp.setCpId("cpId");
        LInterface lInterface = OBJECT_FACTORY.createLInterface();
        lInterface.setResourceVersion("v3");
        lInterface.setRelationshipList(new RelationshipList());
        when(aaiRestApiProvider.get(eq(logger), eq(CLOUD), eq("/cloud-regions/cloud-region/myCloudOwnerId/myRegionName/tenants/tenant/myTenantId/vservers/vserver/serverProviderId/l-interfaces/l-interface/cpId"), eq(LInterface.class))).thenReturn(lInterface);
        when(aaiRestApiProvider.put(eq(logger), eq(CLOUD), eq("/cloud-regions/cloud-region/myCloudOwnerId/myRegionName/tenants/tenant/myTenantId/vservers/vserver/serverProviderId/l-interfaces/l-interface/cpId"), payload.capture(), eq(Void.class))).thenReturn(null);
        //when
        lInterfaceManager.update(VNF_ID, VIM_ID, affectedCp, true);
        //verify
        LInterface actualInterface = payload.getValue();
        assertEquals(true, actualInterface.isInMaint());
        assertEquals(false, actualInterface.isIsIpUnnumbered());
        assertEquals(false, actualInterface.isIsPortMirrored());
        assertEquals("name", actualInterface.getInterfaceName());
        assertEquals("cpId", actualInterface.getInterfaceId());
        assertEquals("cpdId", actualInterface.getInterfaceRole());
        assertEquals("mac", actualInterface.getMacaddr());
        assertEquals("active", actualInterface.getProvStatus());
        assertEquals(1, actualInterface.getL3InterfaceIpv4AddressList().size());
        assertEquals(0, actualInterface.getL3InterfaceIpv6AddressList().size());
        assertEquals("networkProviderId", actualInterface.getL3InterfaceIpv4AddressList().get(0).getNeutronNetworkId());
        assertEquals("1.2.3.4", actualInterface.getL3InterfaceIpv4AddressList().get(0).getL3InterfaceIpv4Address());
        assertEquals("v3", lInterface.getResourceVersion());
        assertRelation(actualInterface.getRelationshipList(), "generic-vnf", buildRelationshipData("generic-vnf.vnf-id", VNF_ID));
    }

    /**
     * test update success scenario for IPv6 address
     */
    @Test
    public void testUpdateForIpv6() throws Exception {
        ReportedAffectedCp affectedCp = new ReportedAffectedCp();
        affectedCp.setCpdId("cpdId");
        affectedCp.setTenantId("myTenantId");
        affectedCp.setProviderId("portProviderId");
        affectedCp.setServerProviderId("serverProviderId");
        affectedCp.setNetworkProviderId("networkProviderId");
        affectedCp.setMacAddress("mac");
        affectedCp.setIpAddress("::");
        affectedCp.setEcpdId("ecpdId");
        affectedCp.setName("name");
        affectedCp.setCpId("cpId");
        when(aaiRestApiProvider.get(eq(logger), eq(CLOUD), eq("/cloud-regions/cloud-region/myCloudOwnerId/myRegionName/tenants/tenant/myTenantId/vservers/vserver/serverProviderId/l-interfaces/l-interface/cpId"), eq(LInterface.class))).thenThrow(new NoSuchElementException());
        when(aaiRestApiProvider.put(eq(logger), eq(CLOUD), eq("/cloud-regions/cloud-region/myCloudOwnerId/myRegionName/tenants/tenant/myTenantId/vservers/vserver/serverProviderId/l-interfaces/l-interface/cpId"), payload.capture(), eq(Void.class))).thenReturn(null);
        //when
        lInterfaceManager.update(VNF_ID, VIM_ID, affectedCp, false);
        //verify
        LInterface actualInterface = payload.getValue();
        assertEquals(false, actualInterface.isInMaint());
        assertEquals(false, actualInterface.isIsIpUnnumbered());
        assertEquals(false, actualInterface.isIsPortMirrored());
        assertEquals("name", actualInterface.getInterfaceName());
        assertEquals("cpId", actualInterface.getInterfaceId());
        assertEquals("cpdId", actualInterface.getInterfaceRole());
        assertEquals("mac", actualInterface.getMacaddr());
        assertEquals("active", actualInterface.getProvStatus());
        assertEquals(0, actualInterface.getL3InterfaceIpv4AddressList().size());
        assertEquals(1, actualInterface.getL3InterfaceIpv6AddressList().size());
        assertEquals("networkProviderId", actualInterface.getL3InterfaceIpv6AddressList().get(0).getNeutronNetworkId());
        assertEquals("::", actualInterface.getL3InterfaceIpv6AddressList().get(0).getL3InterfaceIpv6Address());
        assertRelation(actualInterface.getRelationshipList(), "generic-vnf", buildRelationshipData("generic-vnf.vnf-id", VNF_ID));
    }

    /**
     * test L3 network deletion
     */
    @Test
    public void testDelete() throws Exception {
        ReportedAffectedCp affectedCp = new ReportedAffectedCp();
        affectedCp.setCpId("cpId");
        affectedCp.setCpdId("cpdId");
        affectedCp.setTenantId("myTenantId");
        affectedCp.setProviderId("portProviderId");
        affectedCp.setServerProviderId("serverProviderId");
        affectedCp.setNetworkProviderId("networkProviderId");
        //when
        lInterfaceManager.delete(VIM_ID, affectedCp);
        //verify
        verify(aaiRestApiProvider).delete(logger, CLOUD, "/cloud-regions/cloud-region/myCloudOwnerId/myRegionName/tenants/tenant/myTenantId/vservers/vserver/serverProviderId/l-interfaces/l-interface/cpId");
    }

    /**
     * test inheritence
     */
    @Test
    public void testInheritence() {
        assertEquals(logger, lInterfaceManager.getLogger());
    }


}
