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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vfc;

import com.google.common.collect.Lists;
import com.google.gson.*;
import com.nokia.cbam.lcm.v32.ApiException;
import com.nokia.cbam.lcm.v32.model.AffectedVirtualLink;
import com.nokia.cbam.lcm.v32.model.AffectedVirtualStorage;
import com.nokia.cbam.lcm.v32.model.AffectedVnfc;
import com.nokia.cbam.lcm.v32.model.ChangeType;
import com.nokia.cbam.lcm.v32.model.*;
import com.nokia.cbam.lcm.v32.model.OperationType;
import com.nokia.cbam.lcm.v32.model.ScaleDirection;
import com.nokia.cbam.lcm.v32.model.VimInfo;
import com.nokia.cbam.lcm.v32.model.VnfInfo;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api.ILifecycleChangeNotificationManager;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.impl.TestBase;
import org.onap.vnfmdriver.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.nokia.cbam.lcm.v32.model.OperationType.*;
import static junit.framework.TestCase.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.rest.CbamRestApiProvider.NOKIA_LCM_API_VERSION;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;

public class TestVfcLifecycleChangeNotificationManager extends TestBase {

    public static final String OPERATION_EXECUTION_ID = "myOperationExecutionId";

    @InjectMocks
    private VfcLifecycleChangeNotificationManager lifecycleChangeNotificationManager;
    private VnfLifecycleChangeNotification recievedLcn = new VnfLifecycleChangeNotification();
    private List<OperationExecution> operationExecutions = new ArrayList<>();
    private OperationExecution instantiationOperation = new OperationExecution();
    private OperationExecution scaleOperation = new OperationExecution();
    private OperationExecution healOperation = new OperationExecution();
    private OperationExecution terminationOperation = new OperationExecution();


    private ArgumentCaptor<VNFLCMNotification> sentLcnToVfc = ArgumentCaptor.forClass(VNFLCMNotification.class);
    private List<VnfInfo> vnfs = new ArrayList<>();
    private VnfInfo vnf = new VnfInfo();

    @Before
    public void initMocks() throws Exception {
        setField(VfcLifecycleChangeNotificationManager.class, "logger", logger);
        instantiationOperation.setId("instantiationOperationExecutionId");
        instantiationOperation.setStartTime(DateTime.now());
        instantiationOperation.setOperationType(OperationType.INSTANTIATE);
        scaleOperation.setId("scaleOperationExecutionId");
        scaleOperation.setStartTime(DateTime.now().plusDays(1));
        scaleOperation.setOperationType(OperationType.SCALE);
        terminationOperation.setId("terminationExecutionId");
        terminationOperation.setStartTime(DateTime.now().plusDays(1));
        terminationOperation.setOperationType(OperationType.TERMINATE);
        healOperation.setId("healOperaitonExecutionId");
        healOperation.setOperationType(OperationType.HEAL);
        recievedLcn.setLifecycleOperationOccurrenceId("instantiationOperationExecutionId");
        healOperation.setStartTime(DateTime.now().plusDays(1));
        recievedLcn.setVnfInstanceId(VNF_ID);
        when(vnfApi.vnfsVnfInstanceIdOperationExecutionsGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(operationExecutions);
        prepOperation(instantiationOperation);
        prepOperation(scaleOperation);
        prepOperation(healOperation);
        prepOperation(terminationOperation);
        doNothing().when(nsLcmApi).vNFLCMNotification(eq(VNFM_ID), eq(VNF_ID), sentLcnToVfc.capture());
        InstantiateVnfRequest instantiateVnfRequest = new InstantiateVnfRequest();
        VimInfo vimInfo = new VimInfo();
        vimInfo.setId(VIM_ID);
        instantiateVnfRequest.getVims().add(vimInfo);
        when(operationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet(instantiationOperation.getId(), NOKIA_LCM_API_VERSION)).thenReturn(new Gson().toJsonTree(instantiateVnfRequest));
        when(vnfApi.vnfsGet(NOKIA_LCM_API_VERSION)).thenReturn(vnfs);
        vnfs.add(vnf);
        vnf.setId(VNF_ID);
        VnfProperty prop = new VnfProperty();
        prop.setName(ILifecycleChangeNotificationManager.EXTERNAL_VNFM_ID);
        prop.setValue(VNFM_ID);
        vnf.getExtensions().add(prop);
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(vnf);
    }

    private void prepOperation(OperationExecution operationExecution) throws ApiException {
        addEmptyModifiedConnectionPoints(operationExecution);
        JsonElement root = new JsonParser().parse("{ \"additionalParams\" : { \"jobId\" : \"" + JOB_ID + "\"}}");
        operationExecution.setOperationParams(root);
        switch (operationExecution.getOperationType()) {
            case TERMINATE:
                root.getAsJsonObject().addProperty("terminationType", "GRACEFULL");
        }
        when(operationExecutionApi.operationExecutionsOperationExecutionIdGet(operationExecution.getId(), NOKIA_LCM_API_VERSION)).thenReturn(operationExecution);
        operationExecutions.add(operationExecution);
    }

    private void addEmptyModifiedConnectionPoints(OperationExecution operationExecution) {
        OperationResult operationResult = new OperationResult();
        operationResult.operationResult = new VfcLifecycleChangeNotificationManager.ReportedAffectedConnectionPoints();
        JsonElement additionalData = new Gson().toJsonTree(operationResult);
        operationExecution.setAdditionalData(additionalData);
    }

    /**
     * The first instantiation before the current operation is selected
     */
    @Test
    public void testLastInstantiationSelection() {
        DateTime baseTime = DateTime.now();
        List<OperationExecution> operations = new ArrayList<>();

        OperationExecution operation = buildOperation(new DateTime(baseTime), TERMINATE);
        OperationExecution operationScale = buildOperation(new DateTime(baseTime).minusDays(1), SCALE);
        OperationExecution operationClosestInstantiate = buildOperation(new DateTime(baseTime).minusDays(2), INSTANTIATE);
        OperationExecution operationFurthers = buildOperation(new DateTime(baseTime).minusDays(3), INSTANTIATE);

        operations.add(operation);
        operations.add(operationScale);
        operations.add(operationClosestInstantiate);
        operations.add(operationFurthers);
        assertEquals(operationClosestInstantiate, VfcLifecycleChangeNotificationManager.findLastInstantiationBefore(operations, operation));
    }

    /**
     * The instantiation operation itself is valid as the last instantiation operation
     */
    @Test
    public void testInstantiationSufficesTheLastInstantiation() {
        DateTime baseTime = DateTime.now();
        List<OperationExecution> operations = new ArrayList<>();

        OperationExecution operation = buildOperation(new DateTime(baseTime), INSTANTIATE);
        OperationExecution operationScale = buildOperation(new DateTime(baseTime).minusDays(1), SCALE);
        OperationExecution operationFurthers = buildOperation(new DateTime(baseTime).minusDays(2), INSTANTIATE);

        operations.add(operation);
        operations.add(operationScale);
        operations.add(operationFurthers);
        assertEquals(operation, VfcLifecycleChangeNotificationManager.findLastInstantiationBefore(operations, operation));
    }

    /**
     * If no instantiation operation is found for before the selected operation
     */
    @Test
    public void testNoInstantiation() {
        DateTime baseTime = DateTime.now();
        List<OperationExecution> operations = new ArrayList<>();

        OperationExecution operation = buildOperation(new DateTime(baseTime), TERMINATE);
        OperationExecution operationScale = buildOperation(new DateTime(baseTime).minusDays(1), SCALE);

        operations.add(operation);
        operations.add(operationScale);
        try {
            VfcLifecycleChangeNotificationManager.findLastInstantiationBefore(operations, operation);
            fail();
        } catch (NoSuchElementException e) {

        }
    }

    @Test
    public void testPojo() {
        assertPojoMethodsFor(VfcLifecycleChangeNotificationManager.ProcessedNotification.class).areWellImplemented();
        assertPojoMethodsFor(VfcLifecycleChangeNotificationManager.ReportedAffectedCp.class).areWellImplemented();
    }

    /**
     * the operations are ordered from newest (first) to oldest (last)
     */
    @Test
    public void testOperationOrdering() {
        List<OperationExecution> operationExecutions = new ArrayList<>();
        DateTime base = DateTime.now();
        OperationExecution before = buildOperation(base, OperationType.INSTANTIATE);
        operationExecutions.add(before);
        OperationExecution after = buildOperation(new DateTime(base).plusDays(1), OperationType.SCALE);
        operationExecutions.add(after);
        List<OperationExecution> sorted1 = VfcLifecycleChangeNotificationManager.NEWEST_OPERATIONS_FIRST.sortedCopy(operationExecutions);
        assertEquals(after, sorted1.get(0));
        assertEquals(before, sorted1.get(1));
    }

    /**
     * test start notification success scenario
     * - the affected resouces are not processed even if present
     * - LCN is sent to VF-C
     */
    @Test
    public void testStartLcn() {
        //given
        recievedLcn.setOperation(OperationType.INSTANTIATE);
        recievedLcn.setStatus(OperationStatus.STARTED);
        recievedLcn.getAffectedVnfcs().add(new AffectedVnfc());
        recievedLcn.getAffectedVirtualLinks().add(new AffectedVirtualLink());
        recievedLcn.getAffectedVirtualStorages().add(new AffectedVirtualStorage());
        //when
        lifecycleChangeNotificationManager.handleLcn(recievedLcn);
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
        recievedLcn.getAffectedVnfcs().add(affectedVnfc);

        AffectedVirtualLink affectedVirtualLink = new AffectedVirtualLink();
        affectedVirtualLink.setChangeType(ChangeType.ADDED);
        affectedVirtualLink.setId("vlId");
        affectedVirtualLink.setVirtualLinkDescId("vlVnfdId");
        affectedVirtualLink.setResource(new ResourceHandle());
        affectedVirtualLink.getResource().setVimId(VIM_ID);
        affectedVirtualLink.getResource().setResourceId("networkProviderId");
        recievedLcn.getAffectedVirtualLinks().add(affectedVirtualLink);


        AffectedVirtualStorage affectedStorage = new AffectedVirtualStorage();
        affectedStorage.setChangeType(ChangeType.ADDED);
        affectedStorage.setId("storageId");
        affectedStorage.setVirtualStorageDescId("storageVnfdId");
        affectedStorage.setResource(new ResourceHandle());
        affectedStorage.getResource().setVimId(VIM_ID);
        affectedStorage.getResource().setResourceId("storageProviderId");
        recievedLcn.getAffectedVirtualStorages().add(affectedStorage);

        VfcLifecycleChangeNotificationManager.ReportedAffectedConnectionPoints affectedConnectionPoints = new VfcLifecycleChangeNotificationManager.ReportedAffectedConnectionPoints();
        VfcLifecycleChangeNotificationManager.ReportedAffectedCp affectedCp = new VfcLifecycleChangeNotificationManager.ReportedAffectedCp();
        affectedCp.setChangeType(ChangeType.ADDED);
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
        affectedConnectionPoints.post.add(affectedCp);


        OperationResult operationResult = new OperationResult();
        operationResult.operationResult = affectedConnectionPoints;
        JsonElement additionalData = new Gson().toJsonTree(operationResult);
        instantiationOperation.setAdditionalData(additionalData);
        //when
        lifecycleChangeNotificationManager.handleLcn(recievedLcn);
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
        assertEquals(org.onap.vnfmdriver.model.VnfCpNotificationType.ADDED, actualAffectedCp.getChangeType());

        assertEquals(0, sentLcnToVfc.getValue().getAffectedVirtualStorage().size());
        assertEquals(JOB_ID, sentLcnToVfc.getValue().getJobId());
        assertEquals(org.onap.vnfmdriver.model.OperationType.INSTANTIATE, sentLcnToVfc.getValue().getOperation());
        assertEquals(VnfLcmNotificationStatus.RESULT, sentLcnToVfc.getValue().getStatus());
        assertEquals(VNF_ID, sentLcnToVfc.getValue().getVnfInstanceId());
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
        recievedLcn.getAffectedVnfcs().add(affectedVnfc);

        VfcLifecycleChangeNotificationManager.ReportedAffectedConnectionPoints affectedConnectionPoints = new VfcLifecycleChangeNotificationManager.ReportedAffectedConnectionPoints();
        VfcLifecycleChangeNotificationManager.ReportedAffectedCp affectedCp = new VfcLifecycleChangeNotificationManager.ReportedAffectedCp();
        affectedCp.setChangeType(ChangeType.ADDED);
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
        affectedConnectionPoints.post.add(affectedCp);

        OperationResult operationResult = new OperationResult();
        operationResult.operationResult = affectedConnectionPoints;
        JsonElement additionalData = new Gson().toJsonTree(operationResult);
        instantiationOperation.setAdditionalData(additionalData);
        //when
        lifecycleChangeNotificationManager.handleLcn(recievedLcn);
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
        assertEquals(org.onap.vnfmdriver.model.VnfCpNotificationType.ADDED, actualAffectedCp.getChangeType());

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
        recievedLcn.getAffectedVnfcs().add(affectedVnfc);

        AffectedVirtualLink affectedVirtualLink = new AffectedVirtualLink();
        affectedVirtualLink.setChangeType(ChangeType.REMOVED);
        affectedVirtualLink.setId("vlId");
        affectedVirtualLink.setVirtualLinkDescId("vlVnfdId");
        affectedVirtualLink.setResource(new ResourceHandle());
        affectedVirtualLink.getResource().setVimId(VIM_ID);
        affectedVirtualLink.getResource().setResourceId("networkProviderId");
        recievedLcn.getAffectedVirtualLinks().add(affectedVirtualLink);

        AffectedVirtualStorage affectedStorage = new AffectedVirtualStorage();
        affectedStorage.setChangeType(ChangeType.REMOVED);
        affectedStorage.setId("storageId");
        affectedStorage.setVirtualStorageDescId("storageVnfdId");
        affectedStorage.setResource(new ResourceHandle());
        affectedStorage.getResource().setVimId(VIM_ID);
        affectedStorage.getResource().setResourceId("storageProviderId");
        recievedLcn.getAffectedVirtualStorages().add(affectedStorage);

        VfcLifecycleChangeNotificationManager.ReportedAffectedConnectionPoints affectedConnectionPoints = new VfcLifecycleChangeNotificationManager.ReportedAffectedConnectionPoints();
        VfcLifecycleChangeNotificationManager.ReportedAffectedCp affectedCp = new VfcLifecycleChangeNotificationManager.ReportedAffectedCp();
        affectedCp.setChangeType(ChangeType.REMOVED);
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
        affectedConnectionPoints.post.add(affectedCp);

        OperationResult operationResult = new OperationResult();
        operationResult.operationResult = affectedConnectionPoints;
        JsonElement additionalData = new Gson().toJsonTree(operationResult);
        instantiationOperation.setAdditionalData(additionalData);
        //when
        lifecycleChangeNotificationManager.handleLcn(recievedLcn);
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
        assertEquals(org.onap.vnfmdriver.model.VnfCpNotificationType.REMOVED, actualAffectedCp.getChangeType());
        assertEquals(VIM_ID, actualAffectedCp.getPortResource().getVimid());
        assertEquals("serverProviderId", actualAffectedCp.getPortResource().getInstId());
        assertEquals("portProviderId", actualAffectedCp.getPortResource().getResourceid());
        assertEquals("myPortName", actualAffectedCp.getPortResource().getResourceName());

        assertEquals(0, sentLcnToVfc.getValue().getAffectedVirtualStorage().size());
        assertEquals(JOB_ID, sentLcnToVfc.getValue().getJobId());
        assertEquals(org.onap.vnfmdriver.model.OperationType.TERMINAL, sentLcnToVfc.getValue().getOperation());
        assertEquals(VnfLcmNotificationStatus.RESULT, sentLcnToVfc.getValue().getStatus());
        assertEquals(VNF_ID, sentLcnToVfc.getValue().getVnfInstanceId());
    }

    /**
     * if VNF listing fails the processing of the notifications is aborted
     */
    @Test
    public void testUnableToListVnfs() throws Exception {
        ApiException expectedException = new ApiException();
        when(vnfApi.vnfsGet(NOKIA_LCM_API_VERSION)).thenThrow(expectedException);
        //when
        try {
            lifecycleChangeNotificationManager.handleLcn(recievedLcn);
            fail();
        } catch (Exception e) {
            assertEquals(expectedException, e.getCause());
            verify(logger).error("Unable to list VNFs / query VNF", expectedException);
        }
    }

    /**
     * if VNF query fails the processing of the notifications is aborted
     */
    @Test
    public void testUnableToQueryVnf() throws Exception {
        ApiException expectedException = new ApiException();
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenThrow(expectedException);
        //when
        try {
            lifecycleChangeNotificationManager.handleLcn(recievedLcn);
            fail();
        } catch (Exception e) {
            assertEquals(expectedException, e.getCause());
            verify(logger).error("Unable to list VNFs / query VNF", expectedException);
        }
    }

    /**
     * if the VNF is not managed by this VNFM the LCN is dropped
     */
    @Test
    public void testNonManagedVnf() throws Exception {
        vnf.getExtensions().clear();
        //when
        lifecycleChangeNotificationManager.handleLcn(recievedLcn);
        //verify
        Mockito.verifyZeroInteractions(operationExecutionApi);
        verify(logger).warn("The VNF with " + VNF_ID + " identifer is not a managed VNF");
    }

    /**
     * if the VNF is not managed by this VNFM the LCN is dropped
     */
    @Test
    public void testManagedByOtherVnf() throws Exception {
        vnf.getExtensions().get(0).setValue("unknownVnfmId");
        //when
        lifecycleChangeNotificationManager.handleLcn(recievedLcn);
        //verify
        Mockito.verifyZeroInteractions(operationExecutionApi);
        verify(logger).warn("The VNF with " + VNF_ID + " identifer is not a managed by the VNFM with id unknownVnfmId");
    }

    /**
     * if the VNF disappeared before processing the LCN
     */
    @Test
    public void testDisappearedVnf() throws Exception {
        vnfs.clear();
        //when
        lifecycleChangeNotificationManager.handleLcn(recievedLcn);
        //verify
        Mockito.verifyZeroInteractions(operationExecutionApi);
        verify(logger).warn("The VNF with " + VNF_ID + " disapperaed before being able to process the LCN");
    }

    /**
     * if the operation parameters of the last instantiation is non querieable error is propagated
     */
    @Test
    public void testUnableToQueryOperationParams() throws Exception {
        recievedLcn.setOperation(OperationType.TERMINATE);
        recievedLcn.setStatus(OperationStatus.FINISHED);
        RuntimeException expectedException = new RuntimeException();
        when(operationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet(instantiationOperation.getId(), NOKIA_LCM_API_VERSION)).thenThrow(expectedException);
        //when
        try {
            lifecycleChangeNotificationManager.handleLcn(recievedLcn);
            fail();
        } catch (Exception e) {
            //verify
            Mockito.verifyZeroInteractions(nsLcmApi);
            assertEquals(expectedException, e.getCause());
            verify(logger).error("Unable to detect last instantiation operation", e.getCause());
        }
    }

    /**
     * if unable to send LCN to VF-C the error is propagated
     */
    @Test
    public void testUnableToSendLcnToVfc() throws Exception {
        recievedLcn.setOperation(OperationType.TERMINATE);
        recievedLcn.setStatus(OperationStatus.FINISHED);
        RuntimeException expectedException = new RuntimeException();
        Mockito.doThrow(expectedException).when(nsLcmApi).vNFLCMNotification(eq(VNFM_ID), eq(VNF_ID), Mockito.any(VNFLCMNotification.class));
        //when
        try {
            lifecycleChangeNotificationManager.handleLcn(recievedLcn);
            fail();
        } catch (Exception e) {
            //verify
            assertEquals(expectedException, e.getCause());
            verify(logger).error("Unable to send LCN to ONAP", e.getCause());
        }
    }

    /**
     * if unable to send LCN to VF-C the error is propagated
     */
    @Test
    public void testUnableToQueryCurrentOperation() throws Exception {
        recievedLcn.setOperation(OperationType.TERMINATE);
        recievedLcn.setStatus(OperationStatus.FINISHED);
        ApiException expectedException = new ApiException();
        when(vnfApi.vnfsVnfInstanceIdOperationExecutionsGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenThrow(expectedException);
        //when
        try {
            lifecycleChangeNotificationManager.handleLcn(recievedLcn);
            fail();
        } catch (Exception e) {
            //verify
            assertEquals(expectedException, e.getCause());
            verify(logger).error("Unable to retrieve the current VNF myVnfId", e.getCause());
        }
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
        recievedLcn.getAffectedVnfcs().add(affectedVnfc);

        AffectedVirtualLink affectedVirtualLink = new AffectedVirtualLink();
        affectedVirtualLink.setChangeType(ChangeType.MODIFIED);
        affectedVirtualLink.setId("vlId");
        affectedVirtualLink.setVirtualLinkDescId("vlVnfdId");
        affectedVirtualLink.setResource(new ResourceHandle());
        affectedVirtualLink.getResource().setVimId(VIM_ID);
        affectedVirtualLink.getResource().setResourceId("networkProviderId");
        recievedLcn.getAffectedVirtualLinks().add(affectedVirtualLink);


        AffectedVirtualStorage affectedStorage = new AffectedVirtualStorage();
        affectedStorage.setChangeType(ChangeType.MODIFIED);
        affectedStorage.setId("storageId");
        affectedStorage.setVirtualStorageDescId("storageVnfdId");
        affectedStorage.setResource(new ResourceHandle());
        affectedStorage.getResource().setVimId(VIM_ID);
        affectedStorage.getResource().setResourceId("storageProviderId");
        recievedLcn.getAffectedVirtualStorages().add(affectedStorage);

        VfcLifecycleChangeNotificationManager.ReportedAffectedConnectionPoints affectedConnectionPoints = new VfcLifecycleChangeNotificationManager.ReportedAffectedConnectionPoints();
        VfcLifecycleChangeNotificationManager.ReportedAffectedCp affectedCp = new VfcLifecycleChangeNotificationManager.ReportedAffectedCp();
        affectedCp.setChangeType(ChangeType.MODIFIED);
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
        affectedConnectionPoints.post.add(affectedCp);


        OperationResult operationResult = new OperationResult();
        operationResult.operationResult = affectedConnectionPoints;
        JsonElement additionalData = new Gson().toJsonTree(operationResult);
        instantiationOperation.setAdditionalData(additionalData);
        //when
        lifecycleChangeNotificationManager.handleLcn(recievedLcn);
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
        assertEquals("1.2.3.4", actualAffectedCp.getPortResource().getIpAddress());
        assertEquals("myMac", actualAffectedCp.getPortResource().getMacAddress());
        assertEquals("tenantId", actualAffectedCp.getPortResource().getTenant());
        assertEquals(VIM_ID, actualAffectedCp.getPortResource().getVimid());
        assertEquals("serverProviderId", actualAffectedCp.getPortResource().getInstId());
        assertEquals("portProviderId", actualAffectedCp.getPortResource().getResourceid());
        assertEquals("myPortName", actualAffectedCp.getPortResource().getResourceName());
        assertEquals(org.onap.vnfmdriver.model.VnfCpNotificationType.CHANGED, actualAffectedCp.getChangeType());

        assertEquals(0, sentLcnToVfc.getValue().getAffectedVirtualStorage().size());
        assertEquals(JOB_ID, sentLcnToVfc.getValue().getJobId());
        assertEquals(org.onap.vnfmdriver.model.OperationType.HEAL, sentLcnToVfc.getValue().getOperation());
        assertEquals(VnfLcmNotificationStatus.RESULT, sentLcnToVfc.getValue().getStatus());
        assertEquals(VNF_ID, sentLcnToVfc.getValue().getVnfInstanceId());
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
        recievedLcn.getAffectedVnfcs().add(affectedVnfc);

        AffectedVirtualLink affectedVirtualLink = new AffectedVirtualLink();
        affectedVirtualLink.setChangeType(ChangeType.ADDED);
        affectedVirtualLink.setId("vlId");
        affectedVirtualLink.setVirtualLinkDescId("vlVnfdId");
        affectedVirtualLink.setResource(new ResourceHandle());
        affectedVirtualLink.getResource().setVimId(VIM_ID);
        affectedVirtualLink.getResource().setResourceId("networkProviderId");
        recievedLcn.getAffectedVirtualLinks().add(affectedVirtualLink);


        AffectedVirtualStorage affectedStorage = new AffectedVirtualStorage();
        affectedStorage.setChangeType(ChangeType.ADDED);
        affectedStorage.setId("storageId");
        affectedStorage.setVirtualStorageDescId("storageVnfdId");
        affectedStorage.setResource(new ResourceHandle());
        affectedStorage.getResource().setVimId(VIM_ID);
        affectedStorage.getResource().setResourceId("storageProviderId");
        recievedLcn.getAffectedVirtualStorages().add(affectedStorage);

        VfcLifecycleChangeNotificationManager.ReportedAffectedConnectionPoints affectedConnectionPoints = new VfcLifecycleChangeNotificationManager.ReportedAffectedConnectionPoints();
        VfcLifecycleChangeNotificationManager.ReportedAffectedCp affectedCp = new VfcLifecycleChangeNotificationManager.ReportedAffectedCp();
        affectedCp.setChangeType(ChangeType.ADDED);
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
        affectedConnectionPoints.post.add(affectedCp);

        OperationResult operationResult = new OperationResult();
        operationResult.operationResult = affectedConnectionPoints;
        JsonElement additionalData = new Gson().toJsonTree(operationResult);
        scaleOperation.setAdditionalData(additionalData);
        //when
        lifecycleChangeNotificationManager.handleLcn(recievedLcn);
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
        assertEquals(org.onap.vnfmdriver.model.VnfCpNotificationType.ADDED, actualAffectedCp.getChangeType());

        assertEquals(0, sentLcnToVfc.getValue().getAffectedVirtualStorage().size());
        assertEquals(JOB_ID, sentLcnToVfc.getValue().getJobId());
        assertEquals(org.onap.vnfmdriver.model.OperationType.SCALEOUT, sentLcnToVfc.getValue().getOperation());
        assertEquals(VnfLcmNotificationStatus.RESULT, sentLcnToVfc.getValue().getStatus());
        assertEquals(VNF_ID, sentLcnToVfc.getValue().getVnfInstanceId());
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
        recievedLcn.getAffectedVnfcs().add(affectedVnfc);

        AffectedVirtualLink affectedVirtualLink = new AffectedVirtualLink();
        affectedVirtualLink.setChangeType(ChangeType.REMOVED);
        affectedVirtualLink.setId("vlId");
        affectedVirtualLink.setVirtualLinkDescId("vlVnfdId");
        affectedVirtualLink.setResource(new ResourceHandle());
        affectedVirtualLink.getResource().setVimId(VIM_ID);
        affectedVirtualLink.getResource().setResourceId("networkProviderId");
        recievedLcn.getAffectedVirtualLinks().add(affectedVirtualLink);


        AffectedVirtualStorage affectedStorage = new AffectedVirtualStorage();
        affectedStorage.setChangeType(ChangeType.REMOVED);
        affectedStorage.setId("storageId");
        affectedStorage.setVirtualStorageDescId("storageVnfdId");
        affectedStorage.setResource(new ResourceHandle());
        affectedStorage.getResource().setVimId(VIM_ID);
        affectedStorage.getResource().setResourceId("storageProviderId");
        recievedLcn.getAffectedVirtualStorages().add(affectedStorage);

        VfcLifecycleChangeNotificationManager.ReportedAffectedConnectionPoints affectedConnectionPoints = new VfcLifecycleChangeNotificationManager.ReportedAffectedConnectionPoints();
        VfcLifecycleChangeNotificationManager.ReportedAffectedCp affectedCp = new VfcLifecycleChangeNotificationManager.ReportedAffectedCp();
        affectedCp.setChangeType(ChangeType.REMOVED);
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
        affectedConnectionPoints.post.add(affectedCp);


        OperationResult operationResult = new OperationResult();
        operationResult.operationResult = affectedConnectionPoints;
        JsonElement additionalData = new Gson().toJsonTree(operationResult);
        scaleOperation.setAdditionalData(additionalData);
        scaleOperation.setOperationType(OperationType.SCALE);
        //when
        lifecycleChangeNotificationManager.handleLcn(recievedLcn);
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
    }

    /**
     * test end notification success scenario for scale-out
     * - LCN is sent to VF-C
     */
    @Test
    public void testMissingOperaionResult() {
        //given
        recievedLcn.setOperation(OperationType.SCALE);
        recievedLcn.setStatus(OperationStatus.FINISHED);
        recievedLcn.setLifecycleOperationOccurrenceId(scaleOperation.getId());
        ScaleVnfRequest request = new ScaleVnfRequest();
        request.setAdditionalParams(new JsonParser().parse("{ \"type\" : \"IN\", \"jobId\" : \"" + JOB_ID + "\" }"));
        request.setType(ScaleDirection.OUT);
        scaleOperation.setOperationParams(request);
        scaleOperation.setAdditionalData(null);
        scaleOperation.setStatus(OperationStatus.FINISHED);
        scaleOperation.setOperationType(OperationType.SCALE);
        //when
        try {
            lifecycleChangeNotificationManager.handleLcn(recievedLcn);
            fail();
        } catch (Exception e) {
            assertEquals("All operations must return the { \"operationResult\" : { \"cbam_pre\" : [<fillMeOut>], \"cbam_post\" : [<fillMeOut>] } } structure", e.getMessage());
        }
    }

    /**
     * test end notification success scenario for scale-out
     * - LCN is sent to VF-C
     */
    @Test
    public void testMissingPreResult() {
        //given
        recievedLcn.setOperation(OperationType.SCALE);
        recievedLcn.setStatus(OperationStatus.FINISHED);
        recievedLcn.setLifecycleOperationOccurrenceId(scaleOperation.getId());
        ScaleVnfRequest request = new ScaleVnfRequest();
        request.setAdditionalParams(new JsonParser().parse("{ \"type\" : \"IN\", \"jobId\" : \"" + JOB_ID + "\" }"));
        request.setType(ScaleDirection.OUT);
        scaleOperation.setOperationParams(request);
        JsonObject additionalData = new JsonObject();
        additionalData.add("operationResult", new JsonObject());
        scaleOperation.setAdditionalData(additionalData);
        scaleOperation.setStatus(OperationStatus.FINISHED);
        scaleOperation.setOperationType(OperationType.SCALE);
        JsonElement root = new JsonParser().parse("{ \"additionalParams\" : { \"jobId\" : \"" + JOB_ID + "\"}}");
        JsonObject operationParams = new JsonObject();
        //when
        try {
            lifecycleChangeNotificationManager.handleLcn(recievedLcn);
            fail();
        } catch (Exception e) {
            assertEquals("All operations must return the { \"operationResult\" : { \"cbam_pre\" : [<fillMeOut>], \"cbam_post\" : [<fillMeOut>] } } structure", e.getMessage());
        }
    }

    /**
     * test end notification success scenario for scale-out
     * - LCN is sent to VF-C
     */
    @Test
    public void testMissingPreResultForForcefullTermination() {
        //given
        recievedLcn.setOperation(OperationType.INSTANTIATE);
        recievedLcn.setStatus(OperationStatus.FINISHED);
        recievedLcn.setLifecycleOperationOccurrenceId(terminationOperation.getId());
        JsonObject additionalData = new JsonObject();
        additionalData.add("operationResult", new JsonObject());
        ((JsonObject) terminationOperation.getOperationParams()).addProperty("terminationType", "FORCEFUL");
        terminationOperation.setAdditionalData(additionalData);
        terminationOperation.setStatus(OperationStatus.FINISHED);
        terminationOperation.setOperationType(OperationType.TERMINATE);
        //when
        lifecycleChangeNotificationManager.handleLcn(recievedLcn);
        verify(logger).warn("Unable to send information related to affected connection points during forceful termination");
    }

    /**
     * test end notification scenario for failed scale-out
     * - LCN is sent to VF-C, but the
     */
    @Test
    public void testMissingPreResultForFailedOperation() {
        //given
        recievedLcn.setOperation(OperationType.SCALE);
        recievedLcn.setStatus(OperationStatus.FAILED);
        recievedLcn.setLifecycleOperationOccurrenceId(scaleOperation.getId());
        ScaleVnfRequest request = new ScaleVnfRequest();
        request.setAdditionalParams(new JsonParser().parse("{ \"type\" : \"IN\", \"jobId\" : \"" + JOB_ID + "\" }"));
        request.setType(ScaleDirection.OUT);
        scaleOperation.setOperationParams(request);
        scaleOperation.setAdditionalData(null);
        scaleOperation.setStatus(OperationStatus.FAILED);
        scaleOperation.setOperationType(OperationType.SCALE);
        //when
        lifecycleChangeNotificationManager.handleLcn(recievedLcn);
        verify(logger).warn("The operation failed and the affected connection points were not reported");
    }

    /**
     * heal?
     */


    private JsonObject buildTerminationParams() {
        JsonObject root = new JsonObject();
        root.add("terminationType", new JsonPrimitive("GRACEFULL"));
        return root;
    }

    /**
     * test that waitForTerminationToBeProcessed outwaits the successfull processing of the termination notification
     */
    @Test
    public void testWaitForTermination() throws Exception {
        //given
        //add an non processed notification
        VnfLifecycleChangeNotification nonProcessedEvent = new VnfLifecycleChangeNotification();
        nonProcessedEvent.setStatus(OperationStatus.FINISHED);
        nonProcessedEvent.setOperation(OperationType.TERMINATE);
        OperationExecution secondTerminationOperationExecution = new OperationExecution();
        secondTerminationOperationExecution.setOperationType(OperationType.TERMINATE);
        secondTerminationOperationExecution.setId("secondId");
        secondTerminationOperationExecution.setOperationParams(buildTerminationParams());
        nonProcessedEvent.setLifecycleOperationOccurrenceId(secondTerminationOperationExecution.getId());
        lifecycleChangeNotificationManager.handleLcn(nonProcessedEvent);
        //add second termination
        recievedLcn.setOperation(OperationType.TERMINATE);
        recievedLcn.setStatus(OperationStatus.FINISHED);
        recievedLcn.setLifecycleOperationOccurrenceId(terminationOperation.getId());
        ExecutorService executorService = Executors.newCachedThreadPool();
        Future<Boolean> waitExitedWithSuccess = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    lifecycleChangeNotificationManager.waitForTerminationToBeProcessed(terminationOperation.getId());
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        });
        //when
        lifecycleChangeNotificationManager.handleLcn(recievedLcn);
        //verify
        assertTrue(waitExitedWithSuccess.get());
    }

    private OperationExecution buildOperation(DateTime baseTime, OperationType operationType) {
        OperationExecution operation = new OperationExecution();
        operation.setStartTime(baseTime);
        operation.setOperationType(operationType);
        return operation;
    }

    class OperationResult {
        VfcLifecycleChangeNotificationManager.ReportedAffectedConnectionPoints operationResult;
    }
}
