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
import com.nokia.cbam.lcm.v32.model.*;
import java.util.ArrayList;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification.ReportedAffectedConnectionPoints;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification.ReportedAffectedCp;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import static com.nokia.cbam.lcm.v32.model.ChangeType.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class TestAAINotificationProcessor extends TestBase {
    @Mock
    private GenericVnfManager genericVnfManager;
    @Mock
    private L3NetworkManager l3NetworkManager;
    @Mock
    private LInterfaceManager lInterfaceManager;
    @Mock
    private VnfcManager vnfcManager;
    @Mock
    private VserverManager vserverManager;
    private AAINotificationProcessor aaiNotificationProcessor;

    @Before
    public void init() {
        aaiNotificationProcessor = new AAINotificationProcessor(genericVnfManager, l3NetworkManager, lInterfaceManager, vnfcManager, vserverManager);
        setField(AAINotificationProcessor.class, "logger", logger);
    }

    /**
     * test objects are manipulated in correct order in AAA
     * (other is dependency between the objects though relations)
     */
    @Test
    public void testObjectManipulationOrder() throws Exception {
        VnfLifecycleChangeNotification recievenNotification = new VnfLifecycleChangeNotification();
        recievenNotification.setAffectedVnfcs(new ArrayList<>());
        recievenNotification.setAffectedVirtualLinks(new ArrayList<>());
        ArrayList<AffectedVirtualStorage> affectedVirtualStorages = new ArrayList<>();
        recievenNotification.setAffectedVirtualStorages(affectedVirtualStorages);
        recievenNotification.setVnfInstanceId(VNF_ID);
        recievenNotification.setStatus(OperationStatus.STARTED);
        AffectedVirtualLink addedLink = buildVl(recievenNotification, ADDED);
        AffectedVirtualLink removedLink = buildVl(recievenNotification, REMOVED);
        AffectedVirtualLink modifiedLink = buildVl(recievenNotification, MODIFIED);
        AffectedVnfc addedVnfc = buildVnfc(recievenNotification, ADDED, "tenantId1");
        AffectedVnfc removedVnfc = buildVnfc(recievenNotification, REMOVED, "tenantId2");
        AffectedVnfc modifedVnfc = buildVnfc(recievenNotification, MODIFIED, "tenantId3");
        boolean inMaintenance = true;
        ReportedAffectedConnectionPoints affectedConnectionPoints = new ReportedAffectedConnectionPoints();

        ReportedAffectedCp removedCp = buildCp();
        removedCp.setServerProviderId("serverId");
        affectedConnectionPoints.getPre().add(removedCp);

        ReportedAffectedCp removedCpWithoutServer = buildCp();
        removedCpWithoutServer.setServerProviderId(null);
        affectedConnectionPoints.getPre().add(removedCpWithoutServer);

        ReportedAffectedCp addedCp = buildCp();
        addedCp.setServerProviderId("serverId");
        affectedConnectionPoints.getPost().add(addedCp);
        //when
        aaiNotificationProcessor.processNotification(recievenNotification, null, of(affectedConnectionPoints), VIM_ID, VNFM_ID);
        //verify
        InOrder inOrder = Mockito.inOrder(genericVnfManager, l3NetworkManager, lInterfaceManager, vnfcManager, vserverManager);
        inOrder.verify(l3NetworkManager).update(VIM_ID, VNF_ID, addedLink);
        inOrder.verify(l3NetworkManager).update(VIM_ID, VNF_ID, modifiedLink);
        inOrder.verify(vserverManager).update(VIM_ID, VNF_ID, addedVnfc, affectedVirtualStorages, inMaintenance);
        inOrder.verify(vnfcManager).update(VIM_ID, "tenantId1", VNF_ID, addedVnfc, inMaintenance);
        inOrder.verify(vserverManager).update(VIM_ID, VNF_ID, modifedVnfc, affectedVirtualStorages, inMaintenance);
        inOrder.verify(vnfcManager).update(VIM_ID, "tenantId3", VNF_ID, modifedVnfc, inMaintenance);
        inOrder.verify(lInterfaceManager).delete(VIM_ID, removedCp);
        inOrder.verify(lInterfaceManager).update(VNF_ID, VIM_ID, addedCp, inMaintenance);
        inOrder.verify(vnfcManager).delete(VNF_ID, removedVnfc);
        inOrder.verify(vserverManager).delete(VIM_ID, removedVnfc);
        inOrder.verify(l3NetworkManager).delete(VNF_ID, removedLink);
        verify(lInterfaceManager, never()).update(VNF_ID, VIM_ID, removedCpWithoutServer, inMaintenance);
        verify(lInterfaceManager, never()).delete(VIM_ID, removedCpWithoutServer);
    }

    /**
     * - unchanged CP is updated
     * - changed CP is updated
     */
    @Test
    public void testCps() throws Exception {
        VnfLifecycleChangeNotification recievenNotification = new VnfLifecycleChangeNotification();
        recievenNotification.setAffectedVnfcs(new ArrayList<>());
        recievenNotification.setAffectedVirtualLinks(new ArrayList<>());
        recievenNotification.setVnfInstanceId(VNF_ID);
        recievenNotification.setStatus(OperationStatus.STARTED);
        ArrayList<AffectedVirtualStorage> affectedVirtualStorages = new ArrayList<>();
        recievenNotification.setAffectedVirtualStorages(affectedVirtualStorages);
        boolean inMaintenance = true;
        ReportedAffectedConnectionPoints affectedConnectionPoints = new ReportedAffectedConnectionPoints();

        ReportedAffectedCp unchangedCp = buildCp();
        unchangedCp.setCpId("unchanged");
        affectedConnectionPoints.getPre().add(unchangedCp);
        affectedConnectionPoints.getPost().add(unchangedCp);

        ReportedAffectedCp changedCpBefore = buildCp();
        changedCpBefore.setCpId("changedBefore");
        ReportedAffectedCp changedCpAfter = buildCp();
        changedCpAfter.setCpId("changedAfter");
        changedCpAfter.setCpId(changedCpBefore.getCpId());
        affectedConnectionPoints.getPre().add(changedCpBefore);
        affectedConnectionPoints.getPost().add(changedCpAfter);

        //when
        aaiNotificationProcessor.processNotification(recievenNotification, null, of(affectedConnectionPoints), VIM_ID, VNFM_ID);
        //verify
        verify(lInterfaceManager).update(VNF_ID, VIM_ID, unchangedCp, inMaintenance);
        verify(lInterfaceManager, never()).update(VNF_ID, VIM_ID, changedCpBefore, inMaintenance);
        verify(lInterfaceManager).update(VNF_ID, VIM_ID, changedCpAfter, inMaintenance);
    }

    /**
     * the end notification calls resource managers with not in maintenance state
     */
    @Test
    public void testEndNotification() throws Exception {
        VnfLifecycleChangeNotification recievenNotification = new VnfLifecycleChangeNotification();
        recievenNotification.setAffectedVnfcs(new ArrayList<>());
        recievenNotification.setAffectedVirtualLinks(new ArrayList<>());
        ArrayList<AffectedVirtualStorage> affectedVirtualStorages = new ArrayList<>();
        recievenNotification.setAffectedVirtualStorages(affectedVirtualStorages);
        recievenNotification.setVnfInstanceId(VNF_ID);
        recievenNotification.setStatus(OperationStatus.FINISHED);
        AffectedVirtualLink addedLink = buildVl(recievenNotification, ADDED);
        AffectedVirtualLink removedLink = buildVl(recievenNotification, REMOVED);
        AffectedVirtualLink modifiedLink = buildVl(recievenNotification, MODIFIED);
        AffectedVnfc addedVnfc = buildVnfc(recievenNotification, ADDED, "tenantId1");
        AffectedVnfc removedVnfc = buildVnfc(recievenNotification, REMOVED, "tenantId2");
        AffectedVnfc modifedVnfc = buildVnfc(recievenNotification, MODIFIED, "tenantId3");
        boolean inMaintenance = false;
        ReportedAffectedConnectionPoints affectedConnectionPoints = new ReportedAffectedConnectionPoints();

        ReportedAffectedCp removedCp = buildCp();
        removedCp.setServerProviderId("serverId");
        affectedConnectionPoints.getPre().add(removedCp);

        ReportedAffectedCp removedCpWithoutServer = buildCp();
        removedCpWithoutServer.setServerProviderId(null);
        affectedConnectionPoints.getPre().add(removedCpWithoutServer);

        ReportedAffectedCp addedCp = buildCp();
        addedCp.setServerProviderId("serverId");
        affectedConnectionPoints.getPost().add(addedCp);

        ReportedAffectedCp cpWithoutServer = buildCp();
        cpWithoutServer.setServerProviderId(null);
        affectedConnectionPoints.getPost().add(cpWithoutServer);

        //when
        aaiNotificationProcessor.processNotification(recievenNotification, null, of(affectedConnectionPoints), VIM_ID, VNFM_ID);
        //verify
        InOrder inOrder = Mockito.inOrder(genericVnfManager, l3NetworkManager, lInterfaceManager, vnfcManager, vserverManager);
        inOrder.verify(l3NetworkManager).update(VIM_ID, VNF_ID, addedLink);
        inOrder.verify(l3NetworkManager).update(VIM_ID, VNF_ID, modifiedLink);
        inOrder.verify(vserverManager).update(VIM_ID, VNF_ID, addedVnfc, affectedVirtualStorages, inMaintenance);
        inOrder.verify(vnfcManager).update(VIM_ID, "tenantId1", VNF_ID, addedVnfc, inMaintenance);
        inOrder.verify(vserverManager).update(VIM_ID, VNF_ID, modifedVnfc, affectedVirtualStorages, inMaintenance);
        inOrder.verify(vnfcManager).update(VIM_ID, "tenantId3", VNF_ID, modifedVnfc, inMaintenance);
        inOrder.verify(lInterfaceManager).delete(VIM_ID, removedCp);
        inOrder.verify(lInterfaceManager).update(VNF_ID, VIM_ID, addedCp, inMaintenance);
        inOrder.verify(vnfcManager).delete(VNF_ID, removedVnfc);
        inOrder.verify(vserverManager).delete(VIM_ID, removedVnfc);
        inOrder.verify(l3NetworkManager).delete(VNF_ID, removedLink);
        verify(lInterfaceManager, never()).update(VNF_ID, VIM_ID, removedCpWithoutServer, inMaintenance);
        verify(lInterfaceManager, never()).delete(VIM_ID, removedCpWithoutServer);
        verify(logger).warn("The changed {} connection point is not linked to any server", cpWithoutServer.getCpId());
    }


    /**
     * if changes connection points are not present a warning is logged
     */
    @Test
    public void testMissingChangedConnectionPoints() throws Exception {
        VnfLifecycleChangeNotification recievenNotification = new VnfLifecycleChangeNotification();
        recievenNotification.setAffectedVnfcs(new ArrayList<>());
        recievenNotification.setAffectedVirtualLinks(new ArrayList<>());
        recievenNotification.setVnfInstanceId(VNF_ID);
        //when
        aaiNotificationProcessor.processNotification(recievenNotification, null, empty(), VIM_ID, VNFM_ID);
        //verify
        verify(logger).warn("The changed connection points are not present in VNF with {} identifier", VNF_ID);
    }

    private ReportedAffectedCp buildCp() {
        ReportedAffectedCp cp = new ReportedAffectedCp();
        cp.setServerProviderId(UUID.randomUUID().toString());
        cp.setName(UUID.randomUUID().toString());
        cp.setEcpdId(UUID.randomUUID().toString());
        cp.setMacAddress(UUID.randomUUID().toString());
        cp.setNetworkProviderId(UUID.randomUUID().toString());
        cp.setCpdId(UUID.randomUUID().toString());
        cp.setCpId(UUID.randomUUID().toString());
        cp.setTenantId(UUID.randomUUID().toString());
        return cp;
    }

    private AffectedVirtualLink buildVl(VnfLifecycleChangeNotification recievenNotification, ChangeType changeType) {
        AffectedVirtualLink affectedVirtualLink = new AffectedVirtualLink();
        affectedVirtualLink.setChangeType(changeType);
        recievenNotification.getAffectedVirtualLinks().add(affectedVirtualLink);
        return affectedVirtualLink;
    }

    private AffectedVnfc buildVnfc(VnfLifecycleChangeNotification recievenNotification, ChangeType changeType, String tenantId) {
        AffectedVnfc addedVnfc = new AffectedVnfc();
        addedVnfc.setChangeType(changeType);
        JsonObject additionalData = new JsonObject();
        additionalData.addProperty("tenantId", tenantId);
        addedVnfc.setComputeResource(new ResourceHandle());
        addedVnfc.getComputeResource().setAdditionalData(additionalData);
        recievenNotification.getAffectedVnfcs().add(addedVnfc);
        return addedVnfc;
    }
}
