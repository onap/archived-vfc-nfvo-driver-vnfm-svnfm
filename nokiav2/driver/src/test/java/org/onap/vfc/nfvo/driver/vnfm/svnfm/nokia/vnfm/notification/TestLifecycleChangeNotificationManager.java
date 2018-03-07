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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification;

import com.google.gson.*;
import com.nokia.cbam.lcm.v32.ApiException;
import com.nokia.cbam.lcm.v32.model.*;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.LifecycleManager;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;
import org.threeten.bp.OffsetDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.nokia.cbam.lcm.v32.model.OperationType.*;
import static junit.framework.TestCase.*;
import static org.mockito.Mockito.*;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProvider.NOKIA_LCM_API_VERSION;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class TestLifecycleChangeNotificationManager extends TestBase {

    public static final String OPERATION_EXECUTION_ID = "myOperationExecutionId";

    @InjectMocks
    private LifecycleChangeNotificationManager lifecycleChangeNotificationManager;
    private VnfLifecycleChangeNotification recievedLcn = new VnfLifecycleChangeNotification();
    private List<OperationExecution> operationExecutions = new ArrayList<>();
    private OperationExecution instantiationOperation = new OperationExecution();
    private OperationExecution scaleOperation = new OperationExecution();
    private OperationExecution healOperation = new OperationExecution();
    private OperationExecution terminationOperation = new OperationExecution();

    private ArgumentCaptor<OperationExecution> currentOperationExecution = ArgumentCaptor.forClass(OperationExecution.class);
    private ArgumentCaptor<ReportedAffectedConnectionPoints> affectedConnectionPoints = ArgumentCaptor.forClass(ReportedAffectedConnectionPoints.class);

    private List<VnfInfo> vnfs = new ArrayList<>();
    private VnfInfo vnf = new VnfInfo();

    @Before
    public void initMocks() throws Exception {
        setField(LifecycleChangeNotificationManager.class, "logger", logger);
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
        recievedLcn.setLifecycleOperationOccurrenceId("instantiationOperationExecutionId");
        healOperation.setStartTime(OffsetDateTime.now().plusDays(1));
        recievedLcn.setVnfInstanceId(VNF_ID);
        when(vnfApi.vnfsVnfInstanceIdOperationExecutionsGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(operationExecutions);
        prepOperation(instantiationOperation);
        prepOperation(scaleOperation);
        prepOperation(healOperation);
        prepOperation(terminationOperation);
        doNothing().when(notificationSender).processNotification(eq(recievedLcn), currentOperationExecution.capture(), affectedConnectionPoints.capture(), eq(VIM_ID));
        InstantiateVnfRequest instantiateVnfRequest = new InstantiateVnfRequest();
        VimInfo vimInfo = new VimInfo();
        vimInfo.setId(VIM_ID);
        instantiateVnfRequest.getVims().add(vimInfo);
        when(operationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet(instantiationOperation.getId(), NOKIA_LCM_API_VERSION)).thenReturn(new Gson().toJsonTree(instantiateVnfRequest));
        when(vnfApi.vnfsGet(NOKIA_LCM_API_VERSION)).thenReturn(vnfs);
        vnfs.add(vnf);
        vnf.setId(VNF_ID);
        VnfProperty prop = new VnfProperty();
        prop.setName(LifecycleManager.EXTERNAL_VNFM_ID);
        prop.setValue(VNFM_ID);
        vnf.setExtensions(new ArrayList<>());
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
        operationResult.operationResult = new ReportedAffectedConnectionPoints();
        JsonElement additionalData = new Gson().toJsonTree(operationResult);
        operationExecution.setAdditionalData(additionalData);
    }

    /**
     * The first instantiation before the current operation is selected
     */
    @Test
    public void testLastInstantiationSelection() {
        List<OperationExecution> operations = new ArrayList<>();

        OperationExecution operation = buildOperation(OffsetDateTime.now(), TERMINATE);
        OperationExecution operationScale = buildOperation(OffsetDateTime.now().minusDays(1), SCALE);
        OperationExecution operationClosestInstantiate = buildOperation(OffsetDateTime.now().minusDays(2), INSTANTIATE);
        OperationExecution operationFurthers = buildOperation(OffsetDateTime.now().minusDays(3), INSTANTIATE);

        operations.add(operation);
        operations.add(operationScale);
        operations.add(operationClosestInstantiate);
        operations.add(operationFurthers);
        assertEquals(operationClosestInstantiate, LifecycleChangeNotificationManager.findLastInstantiationBefore(operations, operation));
    }

    /**
     * The instantiation operation itself is valid as the last instantiation operation
     */
    @Test
    public void testInstantiationSufficesTheLastInstantiation() {
        DateTime baseTime = DateTime.now();
        List<OperationExecution> operations = new ArrayList<>();

        OperationExecution operation = buildOperation(OffsetDateTime.now(), INSTANTIATE);
        OperationExecution operationScale = buildOperation(OffsetDateTime.now().minusDays(1), SCALE);
        OperationExecution operationFurthers = buildOperation(OffsetDateTime.now().minusDays(2), INSTANTIATE);

        operations.add(operation);
        operations.add(operationScale);
        operations.add(operationFurthers);
        assertEquals(operation, LifecycleChangeNotificationManager.findLastInstantiationBefore(operations, operation));
    }

    /**
     * If no instantiation operation is found for before the selected operation
     */
    @Test
    public void testNoInstantiation() {
        DateTime baseTime = DateTime.now();
        List<OperationExecution> operations = new ArrayList<>();

        OperationExecution operation = buildOperation(OffsetDateTime.now(), TERMINATE);
        OperationExecution operationScale = buildOperation(OffsetDateTime.now().minusDays(1), SCALE);

        operations.add(operation);
        operations.add(operationScale);
        try {
            LifecycleChangeNotificationManager.findLastInstantiationBefore(operations, operation);
            fail();
        } catch (NoSuchElementException e) {

        }
    }

    /**
     * the operations are ordered from newest (first) to oldest (last)
     */
    @Test
    public void testOperationOrdering() {
        List<OperationExecution> operationExecutions = new ArrayList<>();
        OperationExecution before = buildOperation(OffsetDateTime.now(), OperationType.INSTANTIATE);
        operationExecutions.add(before);
        OperationExecution after = buildOperation(OffsetDateTime.now().plusDays(1), OperationType.SCALE);
        operationExecutions.add(after);
        List<OperationExecution> sorted1 = LifecycleChangeNotificationManager.NEWEST_OPERATIONS_FIRST.sortedCopy(operationExecutions);
        assertEquals(after, sorted1.get(0));
        assertEquals(before, sorted1.get(1));
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
        verify(logger).warn("The VNF with " + VNF_ID + " identifier is not a managed VNF");
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
        verify(logger).warn("The VNF with " + VNF_ID + " identifier is not a managed by the VNFM with id unknownVnfmId");
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
        verify(logger).warn("The VNF with " + VNF_ID + " identifier disappeared before being able to process the LCN");
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

    /**
     * Forceful termination results in an empty affected connection points
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
        assertNull(affectedConnectionPoints.getValue());
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
        assertEquals(0, affectedConnectionPoints.getValue().getPost().size());
        assertEquals(0, affectedConnectionPoints.getValue().getPre().size());
        verify(logger).warn("The operation failed and the affected connection points were not reported");
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
     * missing connection points are tolerated in case of failed operations
     */
    @Test
    public void testMissingConnectionPoints() {
        //given
        recievedLcn.setOperation(OperationType.INSTANTIATE);
        recievedLcn.setStatus(OperationStatus.FAILED);
        recievedLcn.setLifecycleOperationOccurrenceId(instantiationOperation.getId());
        instantiationOperation.setAdditionalData(null);
        instantiationOperation.setStatus(OperationStatus.FAILED);
        //when
        lifecycleChangeNotificationManager.handleLcn(recievedLcn);
        assertEquals(0, affectedConnectionPoints.getValue().getPost().size());
        assertEquals(0, affectedConnectionPoints.getValue().getPre().size());
        verify(logger).warn("The operation failed and the affected connection points were not reported");
    }

    private JsonObject buildTerminationParams() {
        JsonObject root = new JsonObject();
        root.add("terminationType", new JsonPrimitive("GRACEFULL"));
        return root;
    }

    private OperationExecution buildOperation(OffsetDateTime baseTime, OperationType operationType) {
        OperationExecution operation = new OperationExecution();
        operation.setStartTime(baseTime);
        operation.setOperationType(operationType);
        return operation;
    }

    class OperationResult {
        ReportedAffectedConnectionPoints operationResult;
    }

}
