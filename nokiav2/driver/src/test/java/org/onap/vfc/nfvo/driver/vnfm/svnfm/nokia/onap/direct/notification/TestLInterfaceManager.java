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

import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.onap.aai.api.CloudInfrastructureApi;
import org.onap.aai.model.LInterface;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification.ReportedAffectedCp;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.notification.AbstractManager.buildRelationshipData;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.notification.TestGenericVnfManager.assertRelation;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class TestLInterfaceManager extends TestBase {
    private ArgumentCaptor<LInterface> payload = ArgumentCaptor.forClass(LInterface.class);

    @Mock
    private AAIRestApiProvider aaiRestApiProvider;
    private LInterfaceManager lInterfaceManager;
    @Mock
    private CloudInfrastructureApi cloudInfrastructureApi;

    @Before
    public void init() {
        lInterfaceManager = new LInterfaceManager(aaiRestApiProvider, cbamRestApiProvider, driverProperties);
        setField(LInterfaceManager.class, "logger", logger);
        when(aaiRestApiProvider.getCloudInfrastructureApi()).thenReturn(cloudInfrastructureApi);
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
        LInterface lInterface = new LInterface();
        lInterface.setResourceVersion("v3");
        lInterface.setRelationshipList(new ArrayList<>());
        when(cloudInfrastructureApi.getCloudInfrastructureCloudRegionsCloudRegionTenantsTenantVserversVserverLInterfacesLInterface("myCloudOwnerId", "myRegionName", "myTenantId", "serverProviderId", "cpId", null, null, null, null, null, null, null, null, null, null, null, null)).thenReturn(buildObservable(lInterface));
        when(cloudInfrastructureApi.createOrUpdateCloudInfrastructureCloudRegionsCloudRegionTenantsTenantVserversVserverLInterfacesLInterface(eq("myCloudOwnerId"), eq("myRegionName"), eq("myTenantId"), eq("serverProviderId"), eq("cpId"), payload.capture())).thenReturn(VOID_OBSERVABLE.value());
        //when
        lInterfaceManager.update(VNF_ID, VIM_ID, affectedCp, true);
        //verify
        LInterface actualInterface = payload.getValue();
        assertEquals(TRUE, actualInterface.isInMaint());
        assertEquals(FALSE, actualInterface.isIsIpUnnumbered());
        assertEquals(FALSE, actualInterface.isIsPortMirrored());
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
        VOID_OBSERVABLE.assertCalled();
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
        LInterface lInterface = new LInterface();
        lInterface.setResourceVersion("v3");
        when(cloudInfrastructureApi.getCloudInfrastructureCloudRegionsCloudRegionTenantsTenantVserversVserverLInterfacesLInterface("myCloudOwnerId", "myRegionName", "myTenantId", "serverProviderId", "cpId", null, null, null, null, null, null, null, null, null, null, null, null)).thenReturn(buildObservable(lInterface));
        when(cloudInfrastructureApi.createOrUpdateCloudInfrastructureCloudRegionsCloudRegionTenantsTenantVserversVserverLInterfacesLInterface(eq("myCloudOwnerId"), eq("myRegionName"), eq("myTenantId"), eq("serverProviderId"), eq("cpId"), payload.capture())).thenReturn(VOID_OBSERVABLE.value());
        //when
        lInterfaceManager.update(VNF_ID, VIM_ID, affectedCp, true);
        //verify
        LInterface actualInterface = payload.getValue();
        assertEquals(TRUE, actualInterface.isInMaint());
        assertEquals(FALSE, actualInterface.isIsIpUnnumbered());
        assertEquals(FALSE, actualInterface.isIsPortMirrored());
        assertEquals("name", actualInterface.getInterfaceName());
        assertEquals("cpId", actualInterface.getInterfaceId());
        assertEquals("cpdId", actualInterface.getInterfaceRole());
        assertEquals("mac", actualInterface.getMacaddr());
        assertEquals("active", actualInterface.getProvStatus());
        assertEquals(0, actualInterface.getL3InterfaceIpv6AddressList().size());
        assertEquals(0, actualInterface.getL3InterfaceIpv4AddressList().size());
        assertRelation(actualInterface.getRelationshipList(), "generic-vnf", buildRelationshipData("generic-vnf.vnf-id", VNF_ID));
        VOID_OBSERVABLE.assertCalled();
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
        LInterface lInterface = new LInterface();
        lInterface.setResourceVersion("v3");
        lInterface.setRelationshipList(new ArrayList<>());
        lInterface.getRelationshipList().add(VserverManager.linkTo(VIM_ID, "b", "c"));
        when(cloudInfrastructureApi.getCloudInfrastructureCloudRegionsCloudRegionTenantsTenantVserversVserverLInterfacesLInterface("myCloudOwnerId", "myRegionName", "myTenantId", "serverProviderId", "cpId", null, null, null, null, null, null, null, null, null, null, null, null)).thenReturn(buildObservable(lInterface));
        when(cloudInfrastructureApi.createOrUpdateCloudInfrastructureCloudRegionsCloudRegionTenantsTenantVserversVserverLInterfacesLInterface(eq("myCloudOwnerId"), eq("myRegionName"), eq("myTenantId"), eq("serverProviderId"), eq("cpId"), payload.capture())).thenReturn(VOID_OBSERVABLE.value());
        //when
        lInterfaceManager.update(VNF_ID, VIM_ID, affectedCp, true);
        //verify
        LInterface actualInterface = payload.getValue();
        assertEquals(TRUE, actualInterface.isInMaint());
        assertEquals(FALSE, actualInterface.isIsIpUnnumbered());
        assertEquals(FALSE, actualInterface.isIsPortMirrored());
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
        assertEquals(2, lInterface.getRelationshipList().size());
        VOID_OBSERVABLE.assertCalled();
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
        LInterface lInterface = new LInterface();
        lInterface.setResourceVersion("v3");
        lInterface.setRelationshipList(new ArrayList<>());
        when(cloudInfrastructureApi.getCloudInfrastructureCloudRegionsCloudRegionTenantsTenantVserversVserverLInterfacesLInterface("myCloudOwnerId", "myRegionName", "myTenantId", "serverProviderId", "cpId", null, null, null, null, null, null, null, null, null, null, null, null)).thenReturn(buildObservable(lInterface));
        when(cloudInfrastructureApi.createOrUpdateCloudInfrastructureCloudRegionsCloudRegionTenantsTenantVserversVserverLInterfacesLInterface(eq("myCloudOwnerId"), eq("myRegionName"), eq("myTenantId"), eq("serverProviderId"), eq("cpId"), payload.capture())).thenReturn(VOID_OBSERVABLE.value());
        //when
        lInterfaceManager.update(VNF_ID, VIM_ID, affectedCp, false);
        //verify
        LInterface actualInterface = payload.getValue();
        assertEquals(FALSE, actualInterface.isInMaint());
        assertEquals(FALSE, actualInterface.isIsIpUnnumbered());
        assertEquals(FALSE, actualInterface.isIsPortMirrored());
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
        LInterface lInterface = new LInterface();
        lInterface.setResourceVersion("v3");
        lInterface.setRelationshipList(new ArrayList<>());
        when(cloudInfrastructureApi.getCloudInfrastructureCloudRegionsCloudRegionTenantsTenantVserversVserverLInterfacesLInterface("myCloudOwnerId", "myRegionName", "myTenantId", "serverProviderId", "cpId", null, null, null, null, null, null, null, null, null, null, null, null)).thenReturn(buildObservable(lInterface));
        //when
        lInterfaceManager.delete(VIM_ID, affectedCp);
        //verify
        cloudInfrastructureApi.deleteCloudInfrastructureCloudRegionsCloudRegionTenantsTenantVserversVserverLInterfacesLInterface("myCloudOwnerId", "myRegionName", "myTenantId", "serverProviderId", "cpId", "v3");
        VOID_OBSERVABLE.assertCalled();
    }

    /**
     * test inheritence
     */
    @Test
    public void testInheritence() {
        assertEquals(logger, lInterfaceManager.getLogger());
    }


}
