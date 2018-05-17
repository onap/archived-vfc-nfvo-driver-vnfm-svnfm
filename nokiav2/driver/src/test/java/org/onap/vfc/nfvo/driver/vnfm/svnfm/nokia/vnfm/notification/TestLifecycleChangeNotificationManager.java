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
import com.nokia.cbam.lcm.v32.model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.LifecycleManager;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;
import org.threeten.bp.OffsetDateTime;

import static java.util.Optional.empty;

import static com.nokia.cbam.lcm.v32.model.OperationType.*;
import static junit.framework.TestCase.*;
import static org.mockito.Mockito.*;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProvider.NOKIA_LCM_API_VERSION;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class TestLifecycleChangeNotificationManager extends TestBase {

    @InjectMocks
    private LifecycleChangeNotificationManager lifecycleChangeNotificationManager;
    private VnfLifecycleChangeNotification recievedLcn = new VnfLifecycleChangeNotification();
    private List<OperationExecution> operationExecutions = new ArrayList<>();
    private OperationExecution instantiationOperation = new OperationExecution();
    private OperationExecution scaleOperation = new OperationExecution();
    private OperationExecution healOperation = new OperationExecution();
    private OperationExecution terminationOperation = new OperationExecution();

    private ArgumentCaptor<OperationExecution> currentOperationExecution = ArgumentCaptor.forClass(OperationExecution.class);
    private ArgumentCaptor<Optional> affectedConnectionPoints = ArgumentCaptor.forClass(Optional.class);

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
        recievedLcn.setSubscriptionId(SUBCRIPTION_ID);
        healOperation.setStartTime(OffsetDateTime.now().plusDays(1));
        recievedLcn.setVnfInstanceId(VNF_ID);
        when(vnfApi.vnfsVnfInstanceIdOperationExecutionsGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(operationExecutions));
        prepOperation(instantiationOperation);
        prepOperation(scaleOperation);
        prepOperation(healOperation);
        prepOperation(terminationOperation);
        doNothing().when(notificationSender).processNotification(eq(recievedLcn), currentOperationExecution.capture(), affectedConnectionPoints.capture(), eq(VIM_ID), eq(VNFM_ID));
        InstantiateVnfRequest instantiateVnfRequest = new InstantiateVnfRequest();
        VimInfo vimInfo = new VimInfo();
        vimInfo.setId(VIM_ID);
        instantiateVnfRequest.getVims().add(vimInfo);
        when(operationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet(instantiationOperation.getId(), NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(new Gson().toJsonTree(instantiateVnfRequest)));
        when(vnfApi.vnfsGet(NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(vnfs));
        vnfs.add(vnf);
        vnf.setId(VNF_ID);
        VnfProperty prop = new VnfProperty();
        prop.setName(LifecycleManager.EXTERNAL_VNFM_ID);
        prop.setValue(VNFM_ID);
        vnf.setExtensions(new ArrayList<>());
        vnf.getExtensions().add(prop);
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(vnf));
    }

    private void prepOperation(OperationExecution operationExecution) {
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
        OffsetDateTime baseTime = OffsetDateTime.now();
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
        OffsetDateTime baseTime = OffsetDateTime.now();
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
        RuntimeException expectedException = new RuntimeException();
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
        RuntimeException expectedException = new RuntimeException();
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
     * LCN is not logged in case of non info log level
     */
    @Test
    public void testNoLogging() throws Exception {
        vnf.getExtensions().clear();
        when(logger.isInfoEnabled()).thenReturn(false);
        //when
        lifecycleChangeNotificationManager.handleLcn(recievedLcn);
        //verify
        verify(logger, never()).info(eq("Received LCN: {}"), anyString());
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
     * if unable to query all operation executions from CBAM the error is propagated
     */
    @Test
    public void testUnableToQueryCurrentOperations() throws Exception {
        recievedLcn.setOperation(OperationType.TERMINATE);
        recievedLcn.setStatus(OperationStatus.FINISHED);
        RuntimeException expectedException = new RuntimeException();
        when(vnfApi.vnfsVnfInstanceIdOperationExecutionsGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenThrow(expectedException);
        //when
        try {
            lifecycleChangeNotificationManager.handleLcn(recievedLcn);
            fail();
        } catch (Exception e) {
            //verify
            assertEquals(expectedException, e.getCause());
            verify(logger).error("Unable to retrieve the operation executions for the VNF myVnfId", e.getCause());
        }
    }

    /**
     * if unable to query the given operation execution from CBAM the error is propagated
     */
    @Test
    public void testUnableToQueryCurrentOperation() throws Exception {
        recievedLcn.setOperation(OperationType.TERMINATE);
        recievedLcn.setStatus(OperationStatus.FINISHED);
        RuntimeException expectedException = new RuntimeException();
        when(operationExecutionApi.operationExecutionsOperationExecutionIdGet(recievedLcn.getLifecycleOperationOccurrenceId(), NOKIA_LCM_API_VERSION)).thenThrow(expectedException);
        //when
        try {
            lifecycleChangeNotificationManager.handleLcn(recievedLcn);
            fail();
        } catch (Exception e) {
            //verify
            assertEquals(expectedException, e.getCause());
            verify(logger).error("Unable to retrieve the operation execution with instantiationOperationExecutionId identifier", e.getCause());
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
        nonProcessedEvent.setSubscriptionId(SUBCRIPTION_ID);
        nonProcessedEvent.setStatus(OperationStatus.FINISHED);
        nonProcessedEvent.setOperation(OperationType.TERMINATE);
        OperationExecution secondTerminationOperationExecution = new OperationExecution();
        secondTerminationOperationExecution.setOperationType(OperationType.TERMINATE);
        secondTerminationOperationExecution.setId("secondId");
        secondTerminationOperationExecution.setOperationParams(buildTerminationParams());
        nonProcessedEvent.setLifecycleOperationOccurrenceId(secondTerminationOperationExecution.getId());
        lifecycleChangeNotificationManager.handleLcn(nonProcessedEvent);
        addEmptyModifiedConnectionPoints(terminationOperation);
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
     * the processing of the start notification does not trigger the deletion of the VNF
     */
    @Test
    public void testStartLcnForTerminate() throws Exception {
        recievedLcn.setOperation(OperationType.TERMINATE);
        recievedLcn.setStatus(OperationStatus.STARTED);
        recievedLcn.setLifecycleOperationOccurrenceId(terminationOperation.getId());
        ExecutorService executorService = Executors.newCachedThreadPool();
        Future<Boolean> waitExitedWithSuccess = executorService.submit(() -> {
            try {
                lifecycleChangeNotificationManager.waitForTerminationToBeProcessed(terminationOperation.getId());
                return true;
            } catch (Exception e) {
                return false;
            }
        });
        //processing the start notification
        lifecycleChangeNotificationManager.handleLcn(recievedLcn);
        //verify
        try {
            waitExitedWithSuccess.get(10, TimeUnit.MILLISECONDS);
            fail();
        } catch (Exception e) {
        }
        recievedLcn.setStatus(OperationStatus.FINISHED);
        //when
        lifecycleChangeNotificationManager.handleLcn(recievedLcn);
        //verify
        assertTrue(waitExitedWithSuccess.get());
        assertEquals(empty(), affectedConnectionPoints.getValue());
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
        assertFalse(affectedConnectionPoints.getValue().isPresent());
        verify(logger).warn("Unable to send information related to affected connection points during forceful termination");
    }

    /**
     * Forceful termination results in an empty affected connection points
     */
    @Test
    public void testGracefullTermination() {
        //given
        recievedLcn.setOperation(OperationType.INSTANTIATE);
        recievedLcn.setStatus(OperationStatus.FINISHED);
        recievedLcn.setLifecycleOperationOccurrenceId(terminationOperation.getId());
        ((JsonObject) terminationOperation.getOperationParams()).addProperty("terminationType", "GRACEFUL");
        addEmptyModifiedConnectionPoints(terminationOperation);
        terminationOperation.setStatus(OperationStatus.FINISHED);
        terminationOperation.setOperationType(OperationType.TERMINATE);
        //when
        lifecycleChangeNotificationManager.handleLcn(recievedLcn);
        assertTrue(affectedConnectionPoints.getValue().isPresent());
    }

    /**
     * Failures in affected connection point processing are tolerated for failed operation
     * (because the POST script was not able to run)
     */
    @Test
    public void testFailedOperations() throws Exception {
        //given
        recievedLcn.setOperation(OperationType.INSTANTIATE);
        recievedLcn.setStatus(OperationStatus.FAILED);
        recievedLcn.setLifecycleOperationOccurrenceId(instantiationOperation.getId());
        instantiationOperation.setAdditionalData(null);
        instantiationOperation.setStatus(OperationStatus.FAILED);
        //when
        lifecycleChangeNotificationManager.handleLcn(recievedLcn);
        //verify
        assertFalse(affectedConnectionPoints.getValue().isPresent());
        verify(logger).warn("The operation failed and the affected connection points were not reported");
    }


    /**
     * affected connection points are passed to the actual notification processor
     */
    @Test
    public void testAffectedConnectionPointProcessing() throws Exception {
        //given
        recievedLcn.setOperation(OperationType.INSTANTIATE);
        recievedLcn.setStatus(OperationStatus.FINISHED);
        recievedLcn.setLifecycleOperationOccurrenceId(instantiationOperation.getId());
        instantiationOperation.setStatus(OperationStatus.FAILED);
        addEmptyModifiedConnectionPoints(instantiationOperation);
        OperationResult operationResult = new OperationResult();
        ReportedAffectedConnectionPoints affectedCp = new ReportedAffectedConnectionPoints();
        ReportedAffectedCp cp = new ReportedAffectedCp();
        cp.setCpId("cpId");
        affectedCp.getPost().add(cp);
        operationResult.operationResult = affectedCp;
        instantiationOperation.setAdditionalData(new Gson().toJsonTree(operationResult));

        //when
        lifecycleChangeNotificationManager.handleLcn(recievedLcn);
        //verify
        assertTrue(affectedConnectionPoints.getValue().isPresent());
        ReportedAffectedConnectionPoints actualCps = new Gson().fromJson(new Gson().toJsonTree(affectedConnectionPoints.getValue().get()), ReportedAffectedConnectionPoints.class);
        assertEquals(1, actualCps.getPost().size());
    }

    /**
     * Failures in affected connection point processing are tolerated for failed operation
     * (because the POST script was not able to run)
     */
    @Test
    public void testMissingOperationResult() throws Exception {
        //given
        recievedLcn.setOperation(OperationType.INSTANTIATE);
        recievedLcn.setStatus(OperationStatus.FAILED);
        recievedLcn.setLifecycleOperationOccurrenceId(instantiationOperation.getId());
        instantiationOperation.setStatus(OperationStatus.FAILED);
        addEmptyModifiedConnectionPoints(instantiationOperation);
        JsonObject additionalData = (JsonObject) instantiationOperation.getAdditionalData();
        additionalData.remove("operationResult");
        //when
        lifecycleChangeNotificationManager.handleLcn(recievedLcn);
        //verify
        assertFalse(affectedConnectionPoints.getValue().isPresent());
        verify(logger).warn("The operation failed and the affected connection points were not reported");
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
        assertFalse(affectedConnectionPoints.getValue().isPresent());
        verify(logger).warn("The operation failed and the affected connection points were not reported");
    }

    /**
     * if the cbam_post is missing error handling should be applied
     */
    @Test
    public void testMissingPostResultForFailedOperation() {
        //given
        recievedLcn.setOperation(OperationType.SCALE);
        recievedLcn.setStatus(OperationStatus.FAILED);
        recievedLcn.setLifecycleOperationOccurrenceId(scaleOperation.getId());
        ScaleVnfRequest request = new ScaleVnfRequest();
        request.setAdditionalParams(new JsonParser().parse("{ \"type\" : \"IN\", \"jobId\" : \"" + JOB_ID + "\" }"));
        request.setType(ScaleDirection.OUT);
        scaleOperation.setOperationParams(request);
        scaleOperation.setStatus(OperationStatus.FAILED);
        addEmptyModifiedConnectionPoints(scaleOperation);
        ((JsonObject) scaleOperation.getAdditionalData()).get("operationResult").getAsJsonObject().remove("cbam_post");
        scaleOperation.setOperationType(OperationType.SCALE);

        //when
        lifecycleChangeNotificationManager.handleLcn(recievedLcn);
        assertFalse(affectedConnectionPoints.getValue().isPresent());
        verify(logger).warn("The operation failed and the affected connection points were not reported");
    }

    /**
     * if invalid type is specified for cbam_post error handling should be applied
     */
    @Test
    public void testInvalidPost() {
        //given
        recievedLcn.setOperation(OperationType.SCALE);
        recievedLcn.setStatus(OperationStatus.FAILED);
        recievedLcn.setLifecycleOperationOccurrenceId(scaleOperation.getId());
        ScaleVnfRequest request = new ScaleVnfRequest();
        request.setAdditionalParams(new JsonParser().parse("{ \"type\" : \"IN\", \"jobId\" : \"" + JOB_ID + "\" }"));
        request.setType(ScaleDirection.OUT);
        scaleOperation.setOperationParams(request);
        scaleOperation.setStatus(OperationStatus.FAILED);
        addEmptyModifiedConnectionPoints(scaleOperation);
        JsonObject operationResult = ((JsonObject) scaleOperation.getAdditionalData()).get("operationResult").getAsJsonObject();
        operationResult.remove("cbam_post");
        operationResult.addProperty("cbam_post", "");
        scaleOperation.setOperationType(OperationType.SCALE);
        //when
        lifecycleChangeNotificationManager.handleLcn(recievedLcn);
        assertFalse(affectedConnectionPoints.getValue().isPresent());
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
