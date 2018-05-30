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

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.nokia.cbam.lcm.v32.model.AffectedVirtualLink;
import com.nokia.cbam.lcm.v32.model.AffectedVirtualStorage;
import com.nokia.cbam.lcm.v32.model.AffectedVnfc;
import com.nokia.cbam.lcm.v32.model.ChangeType;
import com.nokia.cbam.lcm.v32.model.*;
import com.nokia.cbam.lcm.v32.model.OperationType;
import com.nokia.cbam.lcm.v32.model.ScaleDirection;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification.ReportedAffectedConnectionPoints;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification.ReportedAffectedCp;
import org.onap.vnfmdriver.model.*;
import org.threeten.bp.OffsetDateTime;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static org.mockito.Mockito.*;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProvider.NOKIA_LCM_API_VERSION;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class TestVfcNotificationSender extends TestBase {
    private VfcNotificationSender vfcNotificationSender;
    private ArgumentCaptor<VNFLCMNotification> sentLcnToVfc = ArgumentCaptor.forClass(VNFLCMNotification.class);
    private VnfLifecycleChangeNotification recievedLcn = new VnfLifecycleChangeNotification();
    private List<OperationExecution> operationExecutions = new ArrayList<>();
    private OperationExecution instantiationOperation = new OperationExecution();
    private OperationExecution scaleOperation = new OperationExecution();
    private OperationExecution healOperation = new OperationExecution();
    private OperationExecution terminationOperation = new OperationExecution();
    private ReportedAffectedConnectionPoints affectedCp;


    @Before
    public void init() throws Exception {
        vfcNotificationSender = new VfcNotificationSender(vfcRestApiProvider);
        setField(VfcNotificationSender.class, "logger", logger);
        when(nsLcmApi.vNFLCMNotification(eq(VNFM_ID), eq(VNF_ID), sentLcnToVfc.capture())).thenReturn(VOID_OBSERVABLE.value());
        instantiationOperation.setId("instantiationOperationExecutionId");
        instantiationOperation.setStartTime(OffsetDateTime.now());
        instantiationOperation.setOperationType(OperationType.INSTANTIATE);
        scaleOperation.setId("scaleOperationExecutionId");
        scaleOperation.setStartTime(OffsetDateTime.now().plusDays(1));
        scaleOperation.setOperationType(OperationType.SCALE);
        terminationOperation.setId("terminationExecutionId");
        terminationOperation.setStartTime(OffsetDateTime.now().plusDays(1));
        terminationOperation.setOperationType(OperationType.TERMINATE);
        healOperation.setId("healOperaitonExecutionId");
        healOperation.setOperationType(OperationType.HEAL);
        healOperation.setStartTime(OffsetDateTime.now().plusDays(1));
        when(vnfApi.vnfsVnfInstanceIdOperationExecutionsGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(operationExecutions));
        prepOperation(instantiationOperation);
        prepOperation(scaleOperation);
        prepOperation(healOperation);
        prepOperation(terminationOperation);
        recievedLcn.setVnfInstanceId(VNF_ID);
    }

    private void prepOperation(OperationExecution operationExecution) {
        addEmptyModifiedConnectionPoints(operationExecution);
        JsonElement root = new JsonParser().parse("{ \"additionalParams\" : { \"jobId\" : \"" + JOB_ID + "\"}}");
        operationExecution.setOperationParams(root);
        switch (operationExecution.getOperationType()) {
            case TERMINATE:
                root.getAsJsonObject().addProperty("terminationType", "GRACEFULL");
        }
        when(operationExecutionApi.operationExecutionsOperationExecutionIdGet(operationExecution.getId(), NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(operationExecution));
        operationExecutions.add(operationExecution);
    }

    private void addEmptyModifiedConnectionPoints(OperationExecution operationExecution) {
        OperationResult operationResult = new OperationResult();
        operationResult.operationResult = new ReportedAffectedConnectionPoints();
        JsonElement additionalData = new Gson().toJsonTree(operationResult);
        operationExecution.setAdditionalData(additionalData);
    }

    /**
     * test start notification success scenario
     * - the affected resources are not processed even if present
     * - LCN is sent to VF-C
     */
    @Test
    public void testStartLcn() {
        recievedLcn.setStatus(OperationStatus.STARTED);
        recievedLcn.setOperation(OperationType.INSTANTIATE);
        //when
        vfcNotificationSender.processNotification(recievedLcn, instantiationOperation, empty(), VIM_ID, VNFM_ID);
        //verify
        assertEquals(1, sentLcnToVfc.getAllValues().size());
        assertEquals(0, sentLcnToVfc.getValue().getAffectedVl().size());
        assertEquals(0, sentLcnToVfc.getValue().getAffectedVnfc().size());
        assertEquals(0, sentLcnToVfc.getValue().getAffectedCp().size());
        assertEquals(0, sentLcnToVfc.getValue().getAffectedVirtualStorage().size());

        assertEquals(JOB_ID, sentLcnToVfc.getValue().getJobId());
        assertEquals(org.onap.vnfmdriver.model.OperationType.INSTANTIATE, sentLcnToVfc.getValue().getOperation());
        assertEquals(VnfLcmNotificationStatus.START, sentLcnToVfc.getValue().getStatus());
        assertEquals(VNF_ID, sentLcnToVfc.getValue().getVnfInstanceId());
        VOID_OBSERVABLE.assertCalled();
    }

    /**
     * test end notification success scenario
     * - LCN is sent to VF-C
     */
    @Test
    public void testFinishLcn() {
        //given
        recievedLcn.setOperation(OperationType.INSTANTIATE);
        recievedLcn.setStatus(OperationStatus.FINISHED);
        AffectedVnfc affectedVnfc = new AffectedVnfc();
        affectedVnfc.setChangeType(ChangeType.ADDED);
        affectedVnfc.setId("myVnfcId");
        affectedVnfc.setVduId("myVduId");
        affectedVnfc.setStorageResourceIds(Lists.newArrayList("storageId1"));
        affectedVnfc.setComputeResource(new ResourceHandle());
        affectedVnfc.getComputeResource().setResourceId("serverProviderId");
        affectedVnfc.getComputeResource().setVimId(VIM_ID);
        affectedVnfc.getComputeResource().setAdditionalData(new JsonParser().parse("{ \"name\" : \"myVmName\" } "));
        recievedLcn.setAffectedVnfcs(new ArrayList<>());
        recievedLcn.getAffectedVnfcs().add(affectedVnfc);

        AffectedVirtualLink affectedVirtualLink = new AffectedVirtualLink();
        affectedVirtualLink.setChangeType(ChangeType.ADDED);
        affectedVirtualLink.setId("vlId");
        affectedVirtualLink.setVirtualLinkDescId("vlVnfdId");
        affectedVirtualLink.setResource(new ResourceHandle());
        affectedVirtualLink.getResource().setVimId(VIM_ID);
        affectedVirtualLink.getResource().setResourceId("networkProviderId");
        recievedLcn.setAffectedVirtualLinks(new ArrayList<>());
        recievedLcn.getAffectedVirtualLinks().add(affectedVirtualLink);

        AffectedVirtualStorage affectedStorage = new AffectedVirtualStorage();
        affectedStorage.setChangeType(ChangeType.ADDED);
        affectedStorage.setId("storageId");
        affectedStorage.setVirtualStorageDescId("storageVnfdId");
        affectedStorage.setResource(new ResourceHandle());
        affectedStorage.getResource().setVimId(VIM_ID);
        affectedStorage.getResource().setResourceId("storageProviderId");
        recievedLcn.setAffectedVirtualStorages(new ArrayList<>());
        recievedLcn.getAffectedVirtualStorages().add(affectedStorage);

        ReportedAffectedConnectionPoints affectedConnectionPoints = new ReportedAffectedConnectionPoints();
        ReportedAffectedCp affectedCp = new ReportedAffectedCp();
        affectedCp.setCpdId("cpVnfdId");
        affectedCp.setIpAddress("1.2.3.4");
        affectedCp.setMacAddress("myMac");
        affectedCp.setName("myPortName");
        affectedCp.setCpId("cpId");

        affectedCp.setNetworkProviderId("networkProviderId");
        affectedCp.setProviderId("portProviderId");
        affectedCp.setServerProviderId("serverProviderId");
        affectedCp.setTenantId("tenantId");
        affectedConnectionPoints.getPost().add(affectedCp);

        OperationResult operationResult = new OperationResult();
        operationResult.operationResult = affectedConnectionPoints;
        JsonElement additionalData = new Gson().toJsonTree(operationResult);
        instantiationOperation.setAdditionalData(additionalData);
        //when
        vfcNotificationSender.processNotification(recievedLcn, instantiationOperation, of(affectedConnectionPoints), VIM_ID, VNFM_ID);
        //verify
        assertEquals(1, sentLcnToVfc.getAllValues().size());

        assertEquals(1, sentLcnToVfc.getValue().getAffectedVl().size());
        org.onap.vnfmdriver.model.AffectedVirtualLink actualAffectedVl = sentLcnToVfc.getValue().getAffectedVl().get(0);
        assertEquals(org.onap.vnfmdriver.model.VnfNotificationType.ADDED, actualAffectedVl.getChangeType());
        assertEquals("vlVnfdId", actualAffectedVl.getVldid());
        assertEquals("myVnfId_vlId", actualAffectedVl.getVlInstanceId());
        assertEquals("networkProviderId", actualAffectedVl.getNetworkResource().getResourceId());
        assertEquals(AffectedVirtualLinkType.NETWORK, actualAffectedVl.getNetworkResource().getResourceType());

        assertEquals(1, sentLcnToVfc.getValue().getAffectedVnfc().size());
        org.onap.vnfmdriver.model.AffectedVnfc actualAffectdVnfc = sentLcnToVfc.getValue().getAffectedVnfc().get(0);
        assertEquals("myVduId", actualAffectdVnfc.getVduId());
        assertEquals(VIM_ID, actualAffectdVnfc.getVimid());
        assertEquals("myVmName", actualAffectdVnfc.getVmname());
        assertEquals("serverProviderId", actualAffectdVnfc.getVmid());
        assertEquals(org.onap.vnfmdriver.model.VnfNotificationType.ADDED, actualAffectdVnfc.getChangeType());
        assertEquals("myVnfId_myVnfcId", actualAffectdVnfc.getVnfcInstanceId());

        assertEquals(1, sentLcnToVfc.getValue().getAffectedCp().size());
        AffectedCp actualAffectedCp = sentLcnToVfc.getValue().getAffectedCp().get(0);
        assertEquals("cpVnfdId", actualAffectedCp.getCpdid());
        assertEquals("myVnfId_cpId", actualAffectedCp.getCpinstanceid());
        assertEquals(null, actualAffectedCp.getOwnerId());
        assertEquals(null, actualAffectedCp.getOwnerType());
        assertEquals("networkProviderId", actualAffectedCp.getVirtualLinkInstanceId());
        assertEquals("1.2.3.4", actualAffectedCp.getPortResource().getIpAddress());
        assertEquals("myMac", actualAffectedCp.getPortResource().getMacAddress());
        assertEquals("tenantId", actualAffectedCp.getPortResource().getTenant());
        assertEquals(VIM_ID, actualAffectedCp.getPortResource().getVimid());
        assertEquals("serverProviderId", actualAffectedCp.getPortResource().getInstId());
        assertEquals("portProviderId", actualAffectedCp.getPortResource().getResourceid());
        assertEquals("myPortName", actualAffectedCp.getPortResource().getResourceName());
        assertEquals(VnfCpNotificationType.ADDED, actualAffectedCp.getChangeType());

        assertEquals(0, sentLcnToVfc.getValue().getAffectedVirtualStorage().size());
        assertEquals(JOB_ID, sentLcnToVfc.getValue().getJobId());
        assertEquals(org.onap.vnfmdriver.model.OperationType.INSTANTIATE, sentLcnToVfc.getValue().getOperation());
        assertEquals(VnfLcmNotificationStatus.RESULT, sentLcnToVfc.getValue().getStatus());
        assertEquals(VNF_ID, sentLcnToVfc.getValue().getVnfInstanceId());
        VOID_OBSERVABLE.assertCalled();
    }

    /**
     * test end notification success scenario for ECP
     */
    @Test
    public void testFinishLcnForEcp() {
        //given
        recievedLcn.setOperation(OperationType.INSTANTIATE);
        recievedLcn.setStatus(OperationStatus.FINISHED);
        AffectedVnfc affectedVnfc = new AffectedVnfc();
        affectedVnfc.setChangeType(ChangeType.ADDED);
        affectedVnfc.setId("myVnfcId");
        affectedVnfc.setVduId("myVduId");
        affectedVnfc.setStorageResourceIds(Lists.newArrayList("storageId1"));
        affectedVnfc.setComputeResource(new ResourceHandle());
        affectedVnfc.getComputeResource().setResourceId("serverProviderId");
        affectedVnfc.getComputeResource().setVimId(VIM_ID);
        affectedVnfc.getComputeResource().setAdditionalData(new JsonParser().parse("{ \"name\" : \"myVmName\" } "));
        recievedLcn.setAffectedVnfcs(new ArrayList<>());
        recievedLcn.getAffectedVnfcs().add(affectedVnfc);

        ReportedAffectedConnectionPoints affectedConnectionPoints = new ReportedAffectedConnectionPoints();
        ReportedAffectedCp affectedCp = new ReportedAffectedCp();
        //affectedCp.setCpdId("cpVnfdId");
        affectedCp.setIpAddress("1.2.3.4");
        affectedCp.setMacAddress("myMac");
        affectedCp.setName("myPortName");
        affectedCp.setCpId("cpId");
        affectedCp.setEcpdId("ecpdId");
        affectedCp.setNetworkProviderId("networkProviderId");
        affectedCp.setProviderId("portProviderId");
        affectedCp.setServerProviderId("serverProviderId");
        affectedCp.setTenantId("tenantId");
        affectedConnectionPoints.getPost().add(affectedCp);

        OperationResult operationResult = new OperationResult();
        operationResult.operationResult = affectedConnectionPoints;
        JsonElement additionalData = new Gson().toJsonTree(operationResult);
        instantiationOperation.setAdditionalData(additionalData);
        //when
        vfcNotificationSender.processNotification(recievedLcn, instantiationOperation, of(affectedConnectionPoints), VIM_ID, VNFM_ID);
        //verify
        assertEquals(1, sentLcnToVfc.getAllValues().size());

        assertEquals(1, sentLcnToVfc.getValue().getAffectedCp().size());
        AffectedCp actualAffectedCp = sentLcnToVfc.getValue().getAffectedCp().get(0);
        assertEquals("ecpdId", actualAffectedCp.getCpdid());
        assertEquals("myVnfId_cpId", actualAffectedCp.getCpinstanceid());
        assertEquals(null, actualAffectedCp.getOwnerId());
        assertEquals(null, actualAffectedCp.getOwnerType());
        assertEquals("networkProviderId", actualAffectedCp.getVirtualLinkInstanceId());
        assertEquals("1.2.3.4", actualAffectedCp.getPortResource().getIpAddress());
        assertEquals("myMac", actualAffectedCp.getPortResource().getMacAddress());
        assertEquals("tenantId", actualAffectedCp.getPortResource().getTenant());
        assertEquals(VIM_ID, actualAffectedCp.getPortResource().getVimid());
        assertEquals("serverProviderId", actualAffectedCp.getPortResource().getInstId());
        assertEquals("portProviderId", actualAffectedCp.getPortResource().getResourceid());
        assertEquals("myPortName", actualAffectedCp.getPortResource().getResourceName());
        assertEquals(VnfCpNotificationType.ADDED, actualAffectedCp.getChangeType());
        VOID_OBSERVABLE.assertCalled();
    }

    /**
     * test end notification success scenario with termination
     */
    @Test
    public void testFinishLcnWithTerminate() {
        //given
        recievedLcn.setOperation(OperationType.TERMINATE);
        recievedLcn.setStatus(OperationStatus.FINISHED);
        AffectedVnfc affectedVnfc = new AffectedVnfc();
        affectedVnfc.setChangeType(ChangeType.REMOVED);
        affectedVnfc.setId("myVnfcId");
        affectedVnfc.setVduId("myVduId");
        affectedVnfc.setStorageResourceIds(Lists.newArrayList("storageId1"));
        affectedVnfc.setComputeResource(new ResourceHandle());
        affectedVnfc.getComputeResource().setResourceId("serverProviderId");
        affectedVnfc.getComputeResource().setVimId(VIM_ID);
        affectedVnfc.getComputeResource().setAdditionalData(new JsonParser().parse("{ \"name\" : \"myVmName\" } "));
        recievedLcn.setAffectedVnfcs(new ArrayList<>());
        recievedLcn.getAffectedVnfcs().add(affectedVnfc);

        AffectedVirtualLink affectedVirtualLink = new AffectedVirtualLink();
        affectedVirtualLink.setChangeType(ChangeType.REMOVED);
        affectedVirtualLink.setId("vlId");
        affectedVirtualLink.setVirtualLinkDescId("vlVnfdId");
        affectedVirtualLink.setResource(new ResourceHandle());
        affectedVirtualLink.getResource().setVimId(VIM_ID);
        affectedVirtualLink.getResource().setResourceId("networkProviderId");
        recievedLcn.setAffectedVirtualLinks(new ArrayList<>());
        recievedLcn.getAffectedVirtualLinks().add(affectedVirtualLink);

        AffectedVirtualStorage affectedStorage = new AffectedVirtualStorage();
        affectedStorage.setChangeType(ChangeType.REMOVED);
        affectedStorage.setId("storageId");
        affectedStorage.setVirtualStorageDescId("storageVnfdId");
        affectedStorage.setResource(new ResourceHandle());
        affectedStorage.getResource().setVimId(VIM_ID);
        affectedStorage.getResource().setResourceId("storageProviderId");
        recievedLcn.setAffectedVirtualStorages(new ArrayList<>());
        recievedLcn.getAffectedVirtualStorages().add(affectedStorage);

        ReportedAffectedConnectionPoints affectedConnectionPoints = new ReportedAffectedConnectionPoints();
        ReportedAffectedCp affectedCp = new ReportedAffectedCp();
        affectedCp.setCpdId("cpVnfdId");
        affectedCp.setIpAddress("1.2.3.4");
        affectedCp.setMacAddress("myMac");
        affectedCp.setName("myPortName");
        affectedCp.setCpId("cpId");

        // affectedCp.setEcpdId("ecpdId");
        affectedCp.setNetworkProviderId("networkProviderId");
        affectedCp.setProviderId("portProviderId");
        affectedCp.setServerProviderId("serverProviderId");
        affectedCp.setTenantId("tenantId");
        affectedConnectionPoints.getPre().add(affectedCp);

        OperationResult operationResult = new OperationResult();
        operationResult.operationResult = affectedConnectionPoints;
        JsonElement additionalData = new Gson().toJsonTree(operationResult);
        instantiationOperation.setAdditionalData(additionalData);
        //when
        vfcNotificationSender.processNotification(recievedLcn, terminationOperation, of(affectedConnectionPoints), VIM_ID, VNFM_ID);
        //verify
        assertEquals(1, sentLcnToVfc.getAllValues().size());

        assertEquals(1, sentLcnToVfc.getValue().getAffectedVl().size());
        org.onap.vnfmdriver.model.AffectedVirtualLink actualAffectedVl = sentLcnToVfc.getValue().getAffectedVl().get(0);
        assertEquals(org.onap.vnfmdriver.model.VnfNotificationType.REMOVED, actualAffectedVl.getChangeType());
        assertEquals("vlVnfdId", actualAffectedVl.getVldid());
        assertEquals("myVnfId_vlId", actualAffectedVl.getVlInstanceId());
        assertEquals("networkProviderId", actualAffectedVl.getNetworkResource().getResourceId());
        assertEquals(AffectedVirtualLinkType.NETWORK, actualAffectedVl.getNetworkResource().getResourceType());

        assertEquals(1, sentLcnToVfc.getValue().getAffectedVnfc().size());
        org.onap.vnfmdriver.model.AffectedVnfc actualAffectdVnfc = sentLcnToVfc.getValue().getAffectedVnfc().get(0);
        assertEquals("myVduId", actualAffectdVnfc.getVduId());
        assertEquals(VIM_ID, actualAffectdVnfc.getVimid());
        assertEquals("myVmName", actualAffectdVnfc.getVmname());
        assertEquals("serverProviderId", actualAffectdVnfc.getVmid());
        assertEquals(org.onap.vnfmdriver.model.VnfNotificationType.REMOVED, actualAffectdVnfc.getChangeType());
        assertEquals("myVnfId_myVnfcId", actualAffectdVnfc.getVnfcInstanceId());

        assertEquals(1, sentLcnToVfc.getValue().getAffectedCp().size());
        AffectedCp actualAffectedCp = sentLcnToVfc.getValue().getAffectedCp().get(0);
        assertEquals("cpVnfdId", actualAffectedCp.getCpdid());
        assertEquals("myVnfId_cpId", actualAffectedCp.getCpinstanceid());
        assertEquals(null, actualAffectedCp.getOwnerId());
        assertEquals(null, actualAffectedCp.getOwnerType());
        assertEquals("networkProviderId", actualAffectedCp.getVirtualLinkInstanceId());
        assertEquals("1.2.3.4", actualAffectedCp.getPortResource().getIpAddress());
        assertEquals("myMac", actualAffectedCp.getPortResource().getMacAddress());
        assertEquals("tenantId", actualAffectedCp.getPortResource().getTenant());
        assertEquals(VnfCpNotificationType.REMOVED, actualAffectedCp.getChangeType());
        assertEquals(VIM_ID, actualAffectedCp.getPortResource().getVimid());
        assertEquals("serverProviderId", actualAffectedCp.getPortResource().getInstId());
        assertEquals("portProviderId", actualAffectedCp.getPortResource().getResourceid());
        assertEquals("myPortName", actualAffectedCp.getPortResource().getResourceName());

        assertEquals(0, sentLcnToVfc.getValue().getAffectedVirtualStorage().size());
        assertEquals(JOB_ID, sentLcnToVfc.getValue().getJobId());
        assertEquals(org.onap.vnfmdriver.model.OperationType.TERMINAL, sentLcnToVfc.getValue().getOperation());
        assertEquals(VnfLcmNotificationStatus.RESULT, sentLcnToVfc.getValue().getStatus());
        assertEquals(VNF_ID, sentLcnToVfc.getValue().getVnfInstanceId());
        VOID_OBSERVABLE.assertCalled();
    }

    /**
     * test end notification success scenario for modifiction (heal)
     * - LCN is sent to VF-C
     */
    @Test
    public void testFinishLcnForModification() {
        //given
        recievedLcn.setOperation(OperationType.HEAL);
        recievedLcn.setStatus(OperationStatus.FINISHED);
        AffectedVnfc affectedVnfc = new AffectedVnfc();
        affectedVnfc.setChangeType(ChangeType.MODIFIED);
        affectedVnfc.setId("myVnfcId");
        affectedVnfc.setVduId("myVduId");
        affectedVnfc.setStorageResourceIds(Lists.newArrayList("storageId1"));
        affectedVnfc.setComputeResource(new ResourceHandle());
        affectedVnfc.getComputeResource().setResourceId("serverProviderId");
        affectedVnfc.getComputeResource().setVimId(VIM_ID);
        affectedVnfc.getComputeResource().setAdditionalData(new JsonParser().parse("{ \"name\" : \"myVmName\" } "));
        recievedLcn.setAffectedVnfcs(new ArrayList<>());
        recievedLcn.getAffectedVnfcs().add(affectedVnfc);

        AffectedVirtualLink affectedVirtualLink = new AffectedVirtualLink();
        affectedVirtualLink.setChangeType(ChangeType.MODIFIED);
        affectedVirtualLink.setId("vlId");
        affectedVirtualLink.setVirtualLinkDescId("vlVnfdId");
        affectedVirtualLink.setResource(new ResourceHandle());
        affectedVirtualLink.getResource().setVimId(VIM_ID);
        affectedVirtualLink.getResource().setResourceId("networkProviderId");
        recievedLcn.setAffectedVirtualLinks(new ArrayList<>());
        recievedLcn.getAffectedVirtualLinks().add(affectedVirtualLink);


        AffectedVirtualStorage affectedStorage = new AffectedVirtualStorage();
        affectedStorage.setChangeType(ChangeType.MODIFIED);
        affectedStorage.setId("storageId");
        affectedStorage.setVirtualStorageDescId("storageVnfdId");
        affectedStorage.setResource(new ResourceHandle());
        affectedStorage.getResource().setVimId(VIM_ID);
        affectedStorage.getResource().setResourceId("storageProviderId");
        recievedLcn.setAffectedVirtualStorages(new ArrayList<>());
        recievedLcn.getAffectedVirtualStorages().add(affectedStorage);

        ReportedAffectedConnectionPoints affectedConnectionPoints = new ReportedAffectedConnectionPoints();
        ReportedAffectedCp affectedCp = new ReportedAffectedCp();
        affectedCp.setCpdId("cpVnfdId");
        affectedCp.setIpAddress("1.2.3.4");
        affectedCp.setMacAddress("myMac");
        affectedCp.setName("myPortName");
        affectedCp.setCpId("cpId");

        // affectedCp.setEcpdId("ecpdId");
        affectedCp.setNetworkProviderId("networkProviderId");
        affectedCp.setProviderId("portProviderId");
        affectedCp.setServerProviderId("serverProviderId");
        affectedCp.setTenantId("tenantId");
        affectedConnectionPoints.getPre().add(affectedCp);

        ReportedAffectedCp after = new ReportedAffectedCp();
        after.setCpdId("cpVnfdId");
        after.setIpAddress("1.2.3.5");
        after.setMacAddress("myMac");
        after.setName("myPortName");
        after.setCpId("cpId");

        // affectedCp.setEcpdId("ecpdId");
        after.setNetworkProviderId("networkProviderId");
        after.setProviderId("portProviderId");
        after.setServerProviderId("serverProviderId");
        after.setTenantId("tenantId");
        affectedConnectionPoints.getPost().add(after);


        OperationResult operationResult = new OperationResult();
        operationResult.operationResult = affectedConnectionPoints;
        JsonElement additionalData = new Gson().toJsonTree(operationResult);
        instantiationOperation.setAdditionalData(additionalData);
        //when
        vfcNotificationSender.processNotification(recievedLcn, healOperation, of(affectedConnectionPoints), VIM_ID, VNFM_ID);
        //verify
        assertEquals(1, sentLcnToVfc.getAllValues().size());

        assertEquals(1, sentLcnToVfc.getValue().getAffectedVl().size());
        org.onap.vnfmdriver.model.AffectedVirtualLink actualAffectedVl = sentLcnToVfc.getValue().getAffectedVl().get(0);
        assertEquals(org.onap.vnfmdriver.model.VnfNotificationType.MODIFIED, actualAffectedVl.getChangeType());
        assertEquals("vlVnfdId", actualAffectedVl.getVldid());
        assertEquals("myVnfId_vlId", actualAffectedVl.getVlInstanceId());
        assertEquals("networkProviderId", actualAffectedVl.getNetworkResource().getResourceId());
        assertEquals(AffectedVirtualLinkType.NETWORK, actualAffectedVl.getNetworkResource().getResourceType());

        assertEquals(1, sentLcnToVfc.getValue().getAffectedVnfc().size());
        org.onap.vnfmdriver.model.AffectedVnfc actualAffectdVnfc = sentLcnToVfc.getValue().getAffectedVnfc().get(0);
        assertEquals("myVduId", actualAffectdVnfc.getVduId());
        assertEquals(VIM_ID, actualAffectdVnfc.getVimid());
        assertEquals("myVmName", actualAffectdVnfc.getVmname());
        assertEquals("serverProviderId", actualAffectdVnfc.getVmid());
        assertEquals(org.onap.vnfmdriver.model.VnfNotificationType.MODIFIED, actualAffectdVnfc.getChangeType());
        assertEquals("myVnfId_myVnfcId", actualAffectdVnfc.getVnfcInstanceId());

        assertEquals(1, sentLcnToVfc.getValue().getAffectedCp().size());
        AffectedCp actualAffectedCp = sentLcnToVfc.getValue().getAffectedCp().get(0);
        assertEquals("cpVnfdId", actualAffectedCp.getCpdid());
        assertEquals("myVnfId_cpId", actualAffectedCp.getCpinstanceid());
        assertEquals(null, actualAffectedCp.getOwnerId());
        assertEquals(null, actualAffectedCp.getOwnerType());
        assertEquals("networkProviderId", actualAffectedCp.getVirtualLinkInstanceId());
        assertEquals("1.2.3.5", actualAffectedCp.getPortResource().getIpAddress());
        assertEquals("myMac", actualAffectedCp.getPortResource().getMacAddress());
        assertEquals("tenantId", actualAffectedCp.getPortResource().getTenant());
        assertEquals(VIM_ID, actualAffectedCp.getPortResource().getVimid());
        assertEquals("serverProviderId", actualAffectedCp.getPortResource().getInstId());
        assertEquals("portProviderId", actualAffectedCp.getPortResource().getResourceid());
        assertEquals("myPortName", actualAffectedCp.getPortResource().getResourceName());
        assertEquals(VnfCpNotificationType.CHANGED, actualAffectedCp.getChangeType());

        assertEquals(0, sentLcnToVfc.getValue().getAffectedVirtualStorage().size());
        assertEquals(JOB_ID, sentLcnToVfc.getValue().getJobId());
        assertEquals(org.onap.vnfmdriver.model.OperationType.HEAL, sentLcnToVfc.getValue().getOperation());
        assertEquals(VnfLcmNotificationStatus.RESULT, sentLcnToVfc.getValue().getStatus());
        assertEquals(VNF_ID, sentLcnToVfc.getValue().getVnfInstanceId());
        VOID_OBSERVABLE.assertCalled();
    }

    /**
     * test end notification success scenario for scale-out
     * - LCN is sent to VF-C
     */
    @Test
    public void testFinishLcnForScaleout() {
        //given
        recievedLcn.setOperation(OperationType.SCALE);
        recievedLcn.setStatus(OperationStatus.FINISHED);
        recievedLcn.setLifecycleOperationOccurrenceId(scaleOperation.getId());
        ScaleVnfRequest request = new ScaleVnfRequest();
        request.setAdditionalParams(new JsonParser().parse("{ \"jobId\" : \"" + JOB_ID + "\" }"));
        request.setType(ScaleDirection.OUT);
        scaleOperation.setOperationParams(request);
        scaleOperation.setOperationType(OperationType.SCALE);
        AffectedVnfc affectedVnfc = new AffectedVnfc();
        affectedVnfc.setChangeType(ChangeType.ADDED);
        affectedVnfc.setId("myVnfcId");
        affectedVnfc.setVduId("myVduId");
        affectedVnfc.setStorageResourceIds(Lists.newArrayList("storageId1"));
        affectedVnfc.setComputeResource(new ResourceHandle());
        affectedVnfc.getComputeResource().setResourceId("serverProviderId");
        affectedVnfc.getComputeResource().setVimId(VIM_ID);
        affectedVnfc.getComputeResource().setAdditionalData(new JsonParser().parse("{ \"name\" : \"myVmName\" } "));
        recievedLcn.setAffectedVnfcs(new ArrayList<>());
        recievedLcn.getAffectedVnfcs().add(affectedVnfc);

        AffectedVirtualLink affectedVirtualLink = new AffectedVirtualLink();
        affectedVirtualLink.setChangeType(ChangeType.ADDED);
        affectedVirtualLink.setId("vlId");
        affectedVirtualLink.setVirtualLinkDescId("vlVnfdId");
        affectedVirtualLink.setResource(new ResourceHandle());
        affectedVirtualLink.getResource().setVimId(VIM_ID);
        affectedVirtualLink.getResource().setResourceId("networkProviderId");
        recievedLcn.setAffectedVirtualLinks(new ArrayList<>());
        recievedLcn.getAffectedVirtualLinks().add(affectedVirtualLink);


        AffectedVirtualStorage affectedStorage = new AffectedVirtualStorage();
        affectedStorage.setChangeType(ChangeType.ADDED);
        affectedStorage.setId("storageId");
        affectedStorage.setVirtualStorageDescId("storageVnfdId");
        affectedStorage.setResource(new ResourceHandle());
        affectedStorage.getResource().setVimId(VIM_ID);
        affectedStorage.getResource().setResourceId("storageProviderId");
        recievedLcn.setAffectedVirtualStorages(new ArrayList<>());
        recievedLcn.getAffectedVirtualStorages().add(affectedStorage);

        ReportedAffectedConnectionPoints affectedConnectionPoints = new ReportedAffectedConnectionPoints();
        ReportedAffectedCp affectedCp = new ReportedAffectedCp();
        affectedCp.setCpdId("cpVnfdId");
        affectedCp.setIpAddress("1.2.3.4");
        affectedCp.setMacAddress("myMac");
        affectedCp.setName("myPortName");
        affectedCp.setCpId("cpId");

        // affectedCp.setEcpdId("ecpdId");
        affectedCp.setNetworkProviderId("networkProviderId");
        affectedCp.setProviderId("portProviderId");
        affectedCp.setServerProviderId("serverProviderId");
        affectedCp.setTenantId("tenantId");
        affectedConnectionPoints.getPost().add(affectedCp);

        OperationResult operationResult = new OperationResult();
        operationResult.operationResult = affectedConnectionPoints;
        JsonElement additionalData = new Gson().toJsonTree(operationResult);
        scaleOperation.setAdditionalData(additionalData);
        //when
        vfcNotificationSender.processNotification(recievedLcn, scaleOperation, of(affectedConnectionPoints), VIM_ID, VNFM_ID);
        //verify
        assertEquals(1, sentLcnToVfc.getAllValues().size());

        assertEquals(1, sentLcnToVfc.getValue().getAffectedVl().size());
        org.onap.vnfmdriver.model.AffectedVirtualLink actualAffectedVl = sentLcnToVfc.getValue().getAffectedVl().get(0);
        assertEquals(org.onap.vnfmdriver.model.VnfNotificationType.ADDED, actualAffectedVl.getChangeType());
        assertEquals("vlVnfdId", actualAffectedVl.getVldid());
        assertEquals("myVnfId_vlId", actualAffectedVl.getVlInstanceId());
        assertEquals("networkProviderId", actualAffectedVl.getNetworkResource().getResourceId());
        assertEquals(AffectedVirtualLinkType.NETWORK, actualAffectedVl.getNetworkResource().getResourceType());

        assertEquals(1, sentLcnToVfc.getValue().getAffectedVnfc().size());
        org.onap.vnfmdriver.model.AffectedVnfc actualAffectdVnfc = sentLcnToVfc.getValue().getAffectedVnfc().get(0);
        assertEquals("myVduId", actualAffectdVnfc.getVduId());
        assertEquals(VIM_ID, actualAffectdVnfc.getVimid());
        assertEquals("myVmName", actualAffectdVnfc.getVmname());
        assertEquals("serverProviderId", actualAffectdVnfc.getVmid());
        assertEquals(org.onap.vnfmdriver.model.VnfNotificationType.ADDED, actualAffectdVnfc.getChangeType());
        assertEquals("myVnfId_myVnfcId", actualAffectdVnfc.getVnfcInstanceId());

        assertEquals(1, sentLcnToVfc.getValue().getAffectedCp().size());
        AffectedCp actualAffectedCp = sentLcnToVfc.getValue().getAffectedCp().get(0);
        assertEquals("cpVnfdId", actualAffectedCp.getCpdid());
        assertEquals("myVnfId_cpId", actualAffectedCp.getCpinstanceid());
        assertEquals(null, actualAffectedCp.getOwnerId());
        assertEquals(null, actualAffectedCp.getOwnerType());
        assertEquals("networkProviderId", actualAffectedCp.getVirtualLinkInstanceId());
        assertEquals("1.2.3.4", actualAffectedCp.getPortResource().getIpAddress());
        assertEquals("myMac", actualAffectedCp.getPortResource().getMacAddress());
        assertEquals("tenantId", actualAffectedCp.getPortResource().getTenant());
        assertEquals(VIM_ID, actualAffectedCp.getPortResource().getVimid());
        assertEquals("serverProviderId", actualAffectedCp.getPortResource().getInstId());
        assertEquals("portProviderId", actualAffectedCp.getPortResource().getResourceid());
        assertEquals("myPortName", actualAffectedCp.getPortResource().getResourceName());
        assertEquals(VnfCpNotificationType.ADDED, actualAffectedCp.getChangeType());

        assertEquals(0, sentLcnToVfc.getValue().getAffectedVirtualStorage().size());
        assertEquals(JOB_ID, sentLcnToVfc.getValue().getJobId());
        assertEquals(org.onap.vnfmdriver.model.OperationType.SCALEOUT, sentLcnToVfc.getValue().getOperation());
        assertEquals(VnfLcmNotificationStatus.RESULT, sentLcnToVfc.getValue().getStatus());
        assertEquals(VNF_ID, sentLcnToVfc.getValue().getVnfInstanceId());
        VOID_OBSERVABLE.assertCalled();
    }

    /**
     * test end notification success scenario for scale-out
     * - LCN is sent to VF-C
     */
    @Test
    public void testFinishLcnForScaleIn() {
        //given
        recievedLcn.setOperation(OperationType.SCALE);
        recievedLcn.setStatus(OperationStatus.FINISHED);
        recievedLcn.setLifecycleOperationOccurrenceId(scaleOperation.getId());
        ScaleVnfRequest request = new ScaleVnfRequest();
        request.setAdditionalParams(new JsonParser().parse("{ \"jobId\" : \"" + JOB_ID + "\" }"));
        request.setType(ScaleDirection.IN);
        scaleOperation.setOperationParams(request);
        AffectedVnfc affectedVnfc = new AffectedVnfc();
        affectedVnfc.setChangeType(ChangeType.REMOVED);
        affectedVnfc.setId("myVnfcId");
        affectedVnfc.setVduId("myVduId");
        affectedVnfc.setStorageResourceIds(Lists.newArrayList("storageId1"));
        affectedVnfc.setComputeResource(new ResourceHandle());
        affectedVnfc.getComputeResource().setResourceId("serverProviderId");
        affectedVnfc.getComputeResource().setVimId(VIM_ID);
        affectedVnfc.getComputeResource().setAdditionalData(new JsonParser().parse("{ \"name\" : \"myVmName\" } "));
        recievedLcn.setAffectedVnfcs(new ArrayList<>());
        recievedLcn.getAffectedVnfcs().add(affectedVnfc);

        AffectedVirtualLink affectedVirtualLink = new AffectedVirtualLink();
        affectedVirtualLink.setChangeType(ChangeType.REMOVED);
        affectedVirtualLink.setId("vlId");
        affectedVirtualLink.setVirtualLinkDescId("vlVnfdId");
        affectedVirtualLink.setResource(new ResourceHandle());
        affectedVirtualLink.getResource().setVimId(VIM_ID);
        affectedVirtualLink.getResource().setResourceId("networkProviderId");
        recievedLcn.setAffectedVirtualLinks(new ArrayList<>());
        recievedLcn.getAffectedVirtualLinks().add(affectedVirtualLink);


        AffectedVirtualStorage affectedStorage = new AffectedVirtualStorage();
        affectedStorage.setChangeType(ChangeType.REMOVED);
        affectedStorage.setId("storageId");
        affectedStorage.setVirtualStorageDescId("storageVnfdId");
        affectedStorage.setResource(new ResourceHandle());
        affectedStorage.getResource().setVimId(VIM_ID);
        affectedStorage.getResource().setResourceId("storageProviderId");
        recievedLcn.setAffectedVirtualStorages(new ArrayList<>());
        recievedLcn.getAffectedVirtualStorages().add(affectedStorage);

        ReportedAffectedConnectionPoints affectedConnectionPoints = new ReportedAffectedConnectionPoints();
        ReportedAffectedCp affectedCp = new ReportedAffectedCp();
        affectedCp.setCpdId("cpVnfdId");
        affectedCp.setIpAddress("1.2.3.4");
        affectedCp.setMacAddress("myMac");
        affectedCp.setName("myPortName");
        affectedCp.setCpId("cpId");

        // affectedCp.setEcpdId("ecpdId");
        affectedCp.setNetworkProviderId("networkProviderId");
        affectedCp.setProviderId("portProviderId");
        affectedCp.setServerProviderId("serverProviderId");
        affectedCp.setTenantId("tenantId");
        affectedConnectionPoints.getPre().add(affectedCp);


        OperationResult operationResult = new OperationResult();
        operationResult.operationResult = affectedConnectionPoints;
        JsonElement additionalData = new Gson().toJsonTree(operationResult);
        scaleOperation.setAdditionalData(additionalData);
        scaleOperation.setOperationType(OperationType.SCALE);
        //when
        vfcNotificationSender.processNotification(recievedLcn, scaleOperation, of(affectedConnectionPoints), VIM_ID, VNFM_ID);
        //verify
        assertEquals(1, sentLcnToVfc.getAllValues().size());

        assertEquals(1, sentLcnToVfc.getValue().getAffectedVl().size());
        org.onap.vnfmdriver.model.AffectedVirtualLink actualAffectedVl = sentLcnToVfc.getValue().getAffectedVl().get(0);
        assertEquals(org.onap.vnfmdriver.model.VnfNotificationType.REMOVED, actualAffectedVl.getChangeType());
        assertEquals("vlVnfdId", actualAffectedVl.getVldid());
        assertEquals("myVnfId_vlId", actualAffectedVl.getVlInstanceId());
        assertEquals("networkProviderId", actualAffectedVl.getNetworkResource().getResourceId());
        assertEquals(AffectedVirtualLinkType.NETWORK, actualAffectedVl.getNetworkResource().getResourceType());

        assertEquals(1, sentLcnToVfc.getValue().getAffectedVnfc().size());
        org.onap.vnfmdriver.model.AffectedVnfc actualAffectdVnfc = sentLcnToVfc.getValue().getAffectedVnfc().get(0);
        assertEquals("myVduId", actualAffectdVnfc.getVduId());
        assertEquals(VIM_ID, actualAffectdVnfc.getVimid());
        assertEquals("myVmName", actualAffectdVnfc.getVmname());
        assertEquals("serverProviderId", actualAffectdVnfc.getVmid());
        assertEquals(org.onap.vnfmdriver.model.VnfNotificationType.REMOVED, actualAffectdVnfc.getChangeType());
        assertEquals("myVnfId_myVnfcId", actualAffectdVnfc.getVnfcInstanceId());

        assertEquals(1, sentLcnToVfc.getValue().getAffectedCp().size());
        AffectedCp actualAffectedCp = sentLcnToVfc.getValue().getAffectedCp().get(0);
        assertEquals("cpVnfdId", actualAffectedCp.getCpdid());
        assertEquals("myVnfId_cpId", actualAffectedCp.getCpinstanceid());
        assertEquals(null, actualAffectedCp.getOwnerId());
        assertEquals(null, actualAffectedCp.getOwnerType());
        assertEquals("networkProviderId", actualAffectedCp.getVirtualLinkInstanceId());
        assertEquals("1.2.3.4", actualAffectedCp.getPortResource().getIpAddress());
        assertEquals("myMac", actualAffectedCp.getPortResource().getMacAddress());
        assertEquals("tenantId", actualAffectedCp.getPortResource().getTenant());
        assertEquals(VIM_ID, actualAffectedCp.getPortResource().getVimid());
        assertEquals("serverProviderId", actualAffectedCp.getPortResource().getInstId());
        assertEquals("portProviderId", actualAffectedCp.getPortResource().getResourceid());
        assertEquals("myPortName", actualAffectedCp.getPortResource().getResourceName());
        assertEquals(VnfCpNotificationType.REMOVED, actualAffectedCp.getChangeType());

        assertEquals(0, sentLcnToVfc.getValue().getAffectedVirtualStorage().size());
        assertEquals(JOB_ID, sentLcnToVfc.getValue().getJobId());
        assertEquals(org.onap.vnfmdriver.model.OperationType.SCALEIN, sentLcnToVfc.getValue().getOperation());
        assertEquals(VnfLcmNotificationStatus.RESULT, sentLcnToVfc.getValue().getStatus());
        assertEquals(VNF_ID, sentLcnToVfc.getValue().getVnfInstanceId());
        VOID_OBSERVABLE.assertCalled();
    }


    /**
     * en empty LCN is sent even if nothing has changed
     */
    @Test
    public void testNothingChanged() {
        //given
        recievedLcn.setOperation(OperationType.SCALE);
        recievedLcn.setStatus(OperationStatus.FINISHED);
        recievedLcn.setLifecycleOperationOccurrenceId(scaleOperation.getId());
        ScaleVnfRequest request = new ScaleVnfRequest();
        request.setAdditionalParams(new JsonParser().parse("{ \"jobId\" : \"" + JOB_ID + "\" }"));
        request.setType(ScaleDirection.IN);
        scaleOperation.setOperationParams(request);
        OperationResult operationResult = new OperationResult();
        JsonElement additionalData = new Gson().toJsonTree(operationResult);
        scaleOperation.setAdditionalData(additionalData);
        scaleOperation.setOperationType(OperationType.SCALE);
        when(logger.isInfoEnabled()).thenReturn(false);
        //when
        vfcNotificationSender.processNotification(recievedLcn, scaleOperation, empty(), VIM_ID, VNFM_ID);
        //verify
        assertEquals(1, sentLcnToVfc.getAllValues().size());

        assertEquals(0, sentLcnToVfc.getValue().getAffectedVl().size());
        assertEquals(0, sentLcnToVfc.getValue().getAffectedVnfc().size());
        assertEquals(0, sentLcnToVfc.getValue().getAffectedCp().size());
        assertEquals(0, sentLcnToVfc.getValue().getAffectedVirtualStorage().size());
        assertEquals(JOB_ID, sentLcnToVfc.getValue().getJobId());
        assertEquals(org.onap.vnfmdriver.model.OperationType.SCALEIN, sentLcnToVfc.getValue().getOperation());
        assertEquals(VnfLcmNotificationStatus.RESULT, sentLcnToVfc.getValue().getStatus());
        assertEquals(VNF_ID, sentLcnToVfc.getValue().getVnfInstanceId());
        verify(logger, never()).info(eq("Sending LCN: {}"), anyString());
        VOID_OBSERVABLE.assertCalled();
    }

    /**
     * If a connection point is not modified it is not contained in the LCN
     */
    @Test
    public void testNonModifiedCP() {
        //given
        recievedLcn.setOperation(OperationType.HEAL);
        recievedLcn.setStatus(OperationStatus.FINISHED);

        ReportedAffectedConnectionPoints affectedConnectionPoints = new ReportedAffectedConnectionPoints();
        ReportedAffectedCp affectedCp = new ReportedAffectedCp();
        affectedCp.setCpdId("cpVnfdId");
        affectedCp.setIpAddress("1.2.3.4");
        affectedCp.setMacAddress("myMac");
        affectedCp.setName("myPortName");
        affectedCp.setCpId("cpId");

        // affectedCp.setEcpdId("ecpdId");
        affectedCp.setNetworkProviderId("networkProviderId");
        affectedCp.setProviderId("portProviderId");
        affectedCp.setServerProviderId("serverProviderId");
        affectedCp.setTenantId("tenantId");
        affectedConnectionPoints.getPre().add(affectedCp);

        ReportedAffectedCp after = new ReportedAffectedCp();
        after.setCpdId("cpVnfdId");
        after.setIpAddress("1.2.3.4");
        after.setMacAddress("myMac");
        after.setName("myPortName");
        after.setCpId("cpId");

        // affectedCp.setEcpdId("ecpdId");
        after.setNetworkProviderId("networkProviderId");
        after.setProviderId("portProviderId");
        after.setServerProviderId("serverProviderId");
        after.setTenantId("tenantId");
        affectedConnectionPoints.getPost().add(after);


        OperationResult operationResult = new OperationResult();
        operationResult.operationResult = affectedConnectionPoints;
        JsonElement additionalData = new Gson().toJsonTree(operationResult);
        instantiationOperation.setAdditionalData(additionalData);
        //when
        vfcNotificationSender.processNotification(recievedLcn, healOperation, of(affectedConnectionPoints), VIM_ID, VNFM_ID);
        //verify
        assertEquals(1, sentLcnToVfc.getAllValues().size());

        assertEquals(0, sentLcnToVfc.getValue().getAffectedVl().size());
        assertEquals(0, sentLcnToVfc.getValue().getAffectedVnfc().size());
        assertEquals(0, sentLcnToVfc.getValue().getAffectedCp().size());
        assertEquals(0, sentLcnToVfc.getValue().getAffectedVirtualStorage().size());
        assertEquals(JOB_ID, sentLcnToVfc.getValue().getJobId());
        assertEquals(org.onap.vnfmdriver.model.OperationType.HEAL, sentLcnToVfc.getValue().getOperation());
        assertEquals(VnfLcmNotificationStatus.RESULT, sentLcnToVfc.getValue().getStatus());
        assertEquals(VNF_ID, sentLcnToVfc.getValue().getVnfInstanceId());
    }

    /**
     * Unable to send notification to VF-C results in error
     */
    @Test
    public void testUnableToSendNotificationToVfc() throws Exception {
        RuntimeException expectedException = new RuntimeException();
        doThrow(expectedException).when(nsLcmApi).vNFLCMNotification(any(), any(), any());
        recievedLcn.setStatus(OperationStatus.STARTED);
        recievedLcn.setOperation(OperationType.INSTANTIATE);
        //when
        try {
            vfcNotificationSender.processNotification(recievedLcn, instantiationOperation, empty(), VIM_ID, VNFM_ID);
            //verify
            fail();
        } catch (Exception e) {
            verify(logger).error("Unable to send LCN to VF-C", expectedException);
            assertEquals(expectedException, e.getCause());
        }
    }

    class OperationResult {
        ReportedAffectedConnectionPoints operationResult;
    }
}
