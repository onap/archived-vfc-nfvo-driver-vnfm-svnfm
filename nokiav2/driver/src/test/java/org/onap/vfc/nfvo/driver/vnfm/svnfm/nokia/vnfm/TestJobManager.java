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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.nokia.cbam.lcm.v32.model.*;
import io.reactivex.Observable;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.onap.vnfmdriver.model.JobDetailInfo;
import org.onap.vnfmdriver.model.JobResponseInfo;
import org.onap.vnfmdriver.model.JobStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.threeten.bp.OffsetDateTime;

import static junit.framework.TestCase.*;
import static org.mockito.Mockito.*;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.SEPARATOR;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProvider.NOKIA_LCM_API_VERSION;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.JobManager.extractOnapJobId;
import static org.onap.vnfmdriver.model.JobStatus.*;

public class TestJobManager extends TestBase {

    @Mock
    private HttpServletResponse httpResponse;

    private JobManager jobManager;
    private List<VnfInfo> vnfs = new ArrayList<>();

    @Before
    public void initMocks() throws Exception {
        ReflectionTestUtils.setField(JobManager.class, "logger", logger);
        when(vnfApi.vnfsGet(NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(vnfs));
        when(selfRegistrationManagerForVfc.isReady()).thenReturn(true);
        jobManager = new JobManager(cbamRestApiProviderForVfc, selfRegistrationManagerForVfc);
    }

    /**
     * Only the _ can be used as separator
     * . / % & handled specially in URLs
     * - used in CBAM for separation
     */
    @Test
    public void testSeparator() {
        assertEquals("_", SEPARATOR);
    }

    /**
     * The operation result must contain the ONAP job identifier under the jobId field
     */
    @Test
    public void testJobIdExtractionFromOperationResult() {
        assertEquals("1234", extractOnapJobId(new JsonParser().parse("{ \"additionalParams\" : { \"jobId\" : \"1234\"}}")));
        try {
            extractOnapJobId(new JsonParser().parse("{ }"));
            fail();
        } catch (NoSuchElementException e) {
            assertEquals("The operation result {} does not contain the mandatory additionalParams structure", e.getMessage());
        }
        try {
            extractOnapJobId(new JsonParser().parse("{ \"additionalParams\" : { } }"));
            fail();
        } catch (NoSuchElementException e) {
            assertEquals("The operation result {\"additionalParams\":{}} does not contain the mandatory jobId in the additionalParams structure", e.getMessage());
        }
    }

    /**
     * If the VNF does not exists but the job manager still runs the VNF manipulation process the job is reported to be running
     */
    @Test
    public void testJobForNonExistingVnfReportedRunningIfJobIsOngoing() throws Exception {
        String jobId = jobManager.spawnJob(VNF_ID, httpResponse);
        //when
        JobDetailInfo job = jobManager.getJob(VNFM_ID, jobId);
        //verify
        assertResult(jobId, job, STARTED, "50", "Operation started");
    }

    /**
     * If the VNF does not exists and the internal job is not running the job is reported to be finished
     */
    @Test
    public void testJobForExistingVnfReportedRunningIfJobIsFinished() throws Exception {
        String jobId = jobManager.spawnJob(VNF_ID, httpResponse);
        jobManager.jobFinished(jobId);
        //when
        JobDetailInfo job = jobManager.getJob(VNFM_ID, jobId);
        //verify
        assertResult(jobId, job, JobStatus.FINISHED, "100", "Operation finished");
        assertEquals(false, jobManager.isPreparingForShutDown());
    }

    /**
     * Spawning jobs after preparing for shutdown results in error
     */
    @Test
    public void testNoMoreJobsAreAllowedAfterPrepareForShutdown() throws Exception {
        jobManager.prepareForShutdown();
        //when
        try {
            jobManager.spawnJob(JOB_ID, httpResponse);
            fail();
        } catch (Exception e) {
            verify(logger).error("The service is preparing to shut down");
        }
        assertEquals(true, jobManager.isPreparingForShutDown());
    }

    /**
     * Verify if the jobId has valid format
     */
    @Test
    public void testJobIdValidation() throws Exception {
        try {
            //when
            jobManager.getJob(VNFM_ID, "bad");
            //verify
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("The jobId should be in the <vnfId>_<UUID> format, but was bad", e.getMessage());
        }
        try {
            //when
            jobManager.getJob(VNFM_ID, "vnfId_");
            //verify
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("The UUID in the jobId (vnfId_) can not be empty", e.getMessage());
        }
        try {
            //when
            jobManager.getJob(VNFM_ID, "_UUID");
            //verify
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("The vnfId in the jobId (_UUID) can not be empty", e.getMessage());
        }
    }

    /**
     * If the VNF exists but no operation execution is present with given internalJobId, than the state of the
     * job is ongoing if the internal job is ongoing
     */
    @Test
    public void testExistingVnfWithNotYetStartedOperation() throws Exception {
        String jobId = jobManager.spawnJob(VNF_ID, httpResponse);
        VnfInfo vnf = new VnfInfo();
        vnf.setId(VNF_ID);
        vnfs.add(vnf);
        VnfInfo detailedVnf = new VnfInfo();
        detailedVnf.setId(VNF_ID);
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(detailedVnf));
        JobDetailInfo job = jobManager.getJob(VNFM_ID, jobId);
        //verify
        assertResult(jobId, job, STARTED, "50", "Operation started");
        assertTrue(jobManager.hasOngoingJobs());
    }

    /**
     * If the VNF exists but no operation execution is present with given internalJobId, than the state of the
     * job is failed if the internal job is finished (the operation on CBAM was not able to start)
     */
    @Test
    public void testExistingVnfWithNotUnableToStartOperation() throws Exception {
        String jobId = jobManager.spawnJob(VNF_ID, httpResponse);
        VnfInfo vnf = new VnfInfo();
        vnf.setId(VNF_ID);
        vnfs.add(vnf);
        VnfInfo detailedVnf = new VnfInfo();
        detailedVnf.setId(VNF_ID);
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(detailedVnf));
        jobManager.jobFinished(jobId);
        JobDetailInfo job = jobManager.getJob(VNFM_ID, jobId);
        //verify
        assertResult(jobId, job, ERROR, "100", "Operation failed due to The requested operation was not able to start on CBAM");
        assertFalse(jobManager.hasOngoingJobs());
    }

    /**
     * If the VNF exists but and the operation execution is present with given internalJobId, than the state of the
     * job is ongoing if the operation is ongoing
     */
    @Test
    public void testExistingVnfWithStartedOperation() throws Exception {
        String jobId = jobManager.spawnJob(VNF_ID, httpResponse);
        VnfInfo vnf = new VnfInfo();
        vnf.setId(VNF_ID);
        vnfs.add(vnf);
        VnfInfo detailedVnf = new VnfInfo();
        detailedVnf.setId(VNF_ID);
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(detailedVnf));
        OperationExecution operation = new OperationExecution();
        operation.setId(UUID.randomUUID().toString());
        operation.setStartTime(OffsetDateTime.now());
        operation.setStatus(OperationStatus.STARTED);
        detailedVnf.setOperationExecutions(new ArrayList<>());
        detailedVnf.getOperationExecutions().add(operation);
        JsonElement operationParams = new JsonParser().parse("{ \"additionalParams\" : { \"jobId\" : \"" + jobId + "\"}}");
        when(operationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet(operation.getId(), NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(operationParams));
        JobDetailInfo job = jobManager.getJob(VNFM_ID, jobId);
        //verify
        assertResult(jobId, job, STARTED, "50", "Operation started");
        assertTrue(jobManager.hasOngoingJobs());
    }

    /**
     * If the VNF does not exists till the time the job queries the status of the operation
     */
    @Test
    public void testTerminatedVnf() throws Exception {
        //ddd
        String jobId = jobManager.spawnJob(VNF_ID, httpResponse);
        VnfInfo vnf = new VnfInfo();
        vnf.setId(VNF_ID);
        vnfs.add(vnf);
        VnfInfo detailedVnf = new VnfInfo();
        detailedVnf.setId(VNF_ID);
        List<Integer> vnfQueryCallCounter = new ArrayList<>();
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenAnswer(new Answer<Observable<VnfInfo>>() {
            @Override
            public Observable<VnfInfo> answer(InvocationOnMock invocation) throws Throwable {
                vnfs.clear();
                return buildObservable(detailedVnf);
            }
        });

        jobManager.jobFinished(jobId);

        OperationExecution operation = new OperationExecution();
        operation.setId(UUID.randomUUID().toString());
        operation.setStartTime(OffsetDateTime.now());
        operation.setStatus(OperationStatus.FINISHED);
        operation.setOperationType(OperationType.TERMINATE);
        detailedVnf.setOperationExecutions(new ArrayList<>());
        detailedVnf.getOperationExecutions().add(operation);

        JsonElement operationParams = new JsonParser().parse("{ \"additionalParams\" : { \"jobId\" : \"" + jobId + "\"}}");
        when(operationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet(operation.getId(), NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(operationParams));
        //when
        JobDetailInfo job = jobManager.getJob(VNFM_ID, jobId);
        //verify
        assertResult(jobId, job, FINISHED, "100", "Operation finished");
    }

    /**
     * If the VNF exists but and the operation execution is present with given internalJobId, than the state of the
     * job is error if the operation is failed
     */
    @Test
    public void testExistingVnfWithFailedOperation() throws Exception {
        String jobId = jobManager.spawnJob(VNF_ID, httpResponse);
        VnfInfo vnf = new VnfInfo();
        vnf.setId(VNF_ID);
        vnfs.add(vnf);
        VnfInfo detailedVnf = new VnfInfo();
        detailedVnf.setId(VNF_ID);
        List<Integer> vnfCounter = new ArrayList<>();
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(detailedVnf));
        OperationExecution operation = new OperationExecution();
        operation.setId(UUID.randomUUID().toString());
        operation.setStartTime(OffsetDateTime.now());
        operation.setStatus(OperationStatus.FAILED);
        ProblemDetails errorDetails = new ProblemDetails();
        errorDetails.setTitle("Title");
        errorDetails.setDetail("detail");
        operation.setError(errorDetails);
        detailedVnf.setOperationExecutions(new ArrayList<>());
        detailedVnf.getOperationExecutions().add(operation);
        JsonElement operationParams = new JsonParser().parse("{ \"additionalParams\" : { \"jobId\" : \"" + jobId + "\"}}");
        when(operationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet(operation.getId(), NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(operationParams));
        //when
        JobDetailInfo job = jobManager.getJob(VNFM_ID, jobId);
        //verify
        assertResult(jobId, job, ERROR, "100", "Operation failed due to Title: detail");
        assertTrue(jobManager.hasOngoingJobs());
    }

    private void assertResult(String jobId, JobDetailInfo job, JobStatus status, String progress, String descriptor) {
        assertEquals(jobId, job.getJobId());
        assertEquals(status, job.getResponseDescriptor().getStatus());
        assertEquals(progress, job.getResponseDescriptor().getProgress());
        assertNull(job.getResponseDescriptor().getErrorCode());
        boolean finalState = JobStatus.ERROR.equals(status) || JobStatus.FINISHED.equals(status);
        if (finalState) {
            assertEquals(2, job.getResponseDescriptor().getResponseHistoryList().size());
            JobResponseInfo startEvent = job.getResponseDescriptor().getResponseHistoryList().get(0);
            JobResponseInfo endEvent = job.getResponseDescriptor().getResponseHistoryList().get(1);
            assertNull(startEvent.getErrorCode());
            assertEquals("50", startEvent.getProgress());
            assertEquals(JobStatus.STARTED.name(), startEvent.getStatus());
            assertEquals("1", startEvent.getResponseId());
            assertEquals("Operation started", startEvent.getStatusDescription());

            assertNull(endEvent.getErrorCode());
            assertEquals("100", endEvent.getProgress());
            assertEquals(job.getResponseDescriptor().getStatus().name(), endEvent.getStatus());
            assertEquals("2", endEvent.getResponseId());
            assertEquals(descriptor, endEvent.getStatusDescription());
        } else {
            assertEquals(1, job.getResponseDescriptor().getResponseHistoryList().size());
            assertNull(job.getResponseDescriptor().getResponseHistoryList().get(0).getErrorCode());
            assertEquals(progress, job.getResponseDescriptor().getResponseHistoryList().get(0).getProgress());
            assertEquals(job.getResponseDescriptor().getStatus().name(), job.getResponseDescriptor().getResponseHistoryList().get(0).getStatus());
            assertEquals("1", job.getResponseDescriptor().getResponseHistoryList().get(0).getResponseId());
            assertEquals(descriptor, job.getResponseDescriptor().getResponseHistoryList().get(0).getStatusDescription());
        }
        assertEquals(Integer.toString(job.getResponseDescriptor().getResponseHistoryList().size()), job.getResponseDescriptor().getResponseId());
        assertEquals(descriptor, job.getResponseDescriptor().getStatusDescription());
    }

    /**
     * If the VNF exists but and the operation execution is present with given internalJobId, than the state of the
     * job is finished if the operation is finished, but is not a termination
     */
    @Test
    public void testExistingVnfWithFinishedOperation() throws Exception {
        String jobId = jobManager.spawnJob(VNF_ID, httpResponse);
        VnfInfo vnf = new VnfInfo();
        vnf.setId(VNF_ID);
        vnfs.add(vnf);
        VnfInfo detailedVnf = new VnfInfo();
        detailedVnf.setId(VNF_ID);
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(detailedVnf));
        OperationExecution operation = new OperationExecution();
        operation.setId(UUID.randomUUID().toString());
        operation.setStartTime(OffsetDateTime.now());
        operation.setStatus(OperationStatus.FINISHED);
        operation.setOperationType(OperationType.SCALE);
        detailedVnf.setOperationExecutions(new ArrayList<>());
        detailedVnf.getOperationExecutions().add(operation);
        JsonElement operationParams = new JsonParser().parse("{ \"additionalParams\" : { \"jobId\" : \"" + jobId + "\"}}");
        when(operationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet(operation.getId(), NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(operationParams));
        JobDetailInfo job = jobManager.getJob(VNFM_ID, jobId);
        //verify
        assertResult(jobId, job, JobStatus.FINISHED, "100", "Operation finished");
        assertTrue(jobManager.hasOngoingJobs());
    }

    /**
     * If the VNF exists but and the operation execution is present with given internalJobId, than the state of the
     * job is ongoing if the termination operation is finished. In ONAP terminology the termination includes
     * delete, so the ONAP operation is ongoing since the VNF is not yet deleted
     */
    @Test
    public void testExistingVnfWithFinishedTerminationOperation() throws Exception {
        String jobId = jobManager.spawnJob(VNF_ID, httpResponse);
        VnfInfo vnf = new VnfInfo();
        vnf.setId(VNF_ID);
        vnfs.add(vnf);
        VnfInfo detailedVnf = new VnfInfo();
        detailedVnf.setId(VNF_ID);
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(detailedVnf));
        OperationExecution operation = new OperationExecution();
        operation.setId(UUID.randomUUID().toString());
        operation.setStartTime(OffsetDateTime.now());
        operation.setStatus(OperationStatus.FINISHED);
        operation.setOperationType(OperationType.TERMINATE);
        detailedVnf.setOperationExecutions(new ArrayList<>());
        detailedVnf.getOperationExecutions().add(operation);
        JsonElement operationParams = new JsonParser().parse("{ \"additionalParams\" : { \"jobId\" : \"" + jobId + "\"}}");
        when(operationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet(operation.getId(), NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(operationParams));
        JobDetailInfo job = jobManager.getJob(VNFM_ID, jobId);
        //verify
        assertResult(jobId, job, STARTED, "50", "Operation started");
        //when
        jobManager.jobFinished(jobId);
        job = jobManager.getJob(VNFM_ID, jobId);
        //verify
        assertResult(jobId, job, ERROR, "100", "Operation failed due to unable to delete VNF");
        assertFalse(jobManager.hasOngoingJobs());

    }

    /**
     * Failuire to retrieve operation parameters (CBAM REST API fail) is logged and propagated
     */
    @Test
    public void failuresDuringOperationExecutionRetrievalIsLoggedAndPropagated() throws Exception {
        String jobId = jobManager.spawnJob(VNF_ID, httpResponse);
        VnfInfo vnf = new VnfInfo();
        vnf.setId(VNF_ID);
        vnfs.add(vnf);
        VnfInfo detailedVnf = new VnfInfo();
        detailedVnf.setId(VNF_ID);
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(detailedVnf));
        OperationExecution operation = new OperationExecution();
        operation.setId(UUID.randomUUID().toString());
        detailedVnf.setOperationExecutions(new ArrayList<>());
        detailedVnf.getOperationExecutions().add(operation);
        RuntimeException expectedException = new RuntimeException();
        when(operationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet(operation.getId(), NOKIA_LCM_API_VERSION)).thenThrow(expectedException);
        //verify
        try {
            JobDetailInfo job = jobManager.getJob(VNFM_ID, jobId);
            fail();
        } catch (RuntimeException e) {
            assertEquals(expectedException, e.getCause());
            verify(logger).error("Unable to retrieve operation parameters of operation with " + operation.getId() + " identifier", expectedException);
        }
        assertTrue(jobManager.hasOngoingJobs());
    }

    /**
     * Failure to retrieve VNF (CBAM REST API fail) is logged and propagated
     */
    @Test
    public void failuresDuringVnfRetrievalIsLoggedAndPropagated() throws Exception {
        String jobId = jobManager.spawnJob(VNF_ID, httpResponse);
        VnfInfo vnf = new VnfInfo();
        vnf.setId(VNF_ID);
        vnfs.add(vnf);
        RuntimeException expectedException = new RuntimeException();
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenThrow(expectedException);
        //verify
        try {
            JobDetailInfo job = jobManager.getJob(VNFM_ID, jobId);
            fail();
        } catch (RuntimeException e) {
            assertEquals(expectedException, e.getCause());
            verify(logger).error("Unable to retrieve VNF with myVnfId identifier", expectedException);
        }
        assertTrue(jobManager.hasOngoingJobs());
    }

    /**
     * When searching for the ONAP job by iterating the operation executions. The newest jobs
     * are inspected first (performance optimalization)
     */
    @Test
    public void testNewestOperationAreInspectedFirst() throws Exception {
        String jobId = jobManager.spawnJob(VNF_ID, httpResponse);
        VnfInfo vnf = new VnfInfo();
        vnf.setId(VNF_ID);
        vnfs.add(vnf);
        VnfInfo detailedVnf = new VnfInfo();
        detailedVnf.setId(VNF_ID);
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(detailedVnf));
        OperationExecution olderOperation = new OperationExecution();
        olderOperation.setId(UUID.randomUUID().toString());
        olderOperation.setStartTime(OffsetDateTime.now());
        olderOperation.setStatus(OperationStatus.FINISHED);
        olderOperation.setOperationType(OperationType.TERMINATE);
        OperationExecution newerOperation = new OperationExecution();
        newerOperation.setId(UUID.randomUUID().toString());
        newerOperation.setStartTime(OffsetDateTime.now().plusDays(1));
        newerOperation.setStatus(OperationStatus.FINISHED);
        newerOperation.setOperationType(OperationType.TERMINATE);
        detailedVnf.setOperationExecutions(new ArrayList<>());
        detailedVnf.getOperationExecutions().add(olderOperation);
        detailedVnf.getOperationExecutions().add(newerOperation);
        JsonElement operationParams = new JsonParser().parse("{ \"additionalParams\" : { \"jobId\" : \"" + jobId + "\"}}");
        List<String> queriedOperaionsInOrder = new ArrayList<>();
        when(operationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet(Mockito.anyString(), Mockito.eq(NOKIA_LCM_API_VERSION)))
                .then(new Answer<Observable<Object>>() {
                    @Override
                    public Observable<Object> answer(InvocationOnMock invocationOnMock) throws Throwable {
                        queriedOperaionsInOrder.add(invocationOnMock.getArguments()[0].toString());
                        if (invocationOnMock.getArguments()[0].equals(olderOperation.getId())) {
                            return buildObservable(new JsonParser().parse("{ \"additionalParams\" : { \"jobId\" : \"" + jobId + "\"}}"));
                        } else {
                            return buildObservable(new JsonParser().parse("{ \"additionalParams\" : { \"jobId\" : \"" + "nonMatching" + "\"}}"));
                        }
                    }
                });
        JobDetailInfo job = jobManager.getJob(VNFM_ID, jobId);
        //verify
        assertEquals(Lists.newArrayList(newerOperation.getId(), olderOperation.getId()), queriedOperaionsInOrder);
        assertTrue(jobManager.hasOngoingJobs());
    }

    /**
     * the modify attribute job is skipped, since it is not explicitly triggered by any external job
     */
    @Test
    public void testModifyAttributesOperationExecutionIsSkipped() throws Exception {
        String jobId = jobManager.spawnJob(VNF_ID, httpResponse);
        VnfInfo vnf = new VnfInfo();
        vnf.setId(VNF_ID);
        vnfs.add(vnf);
        VnfInfo detailedVnf = new VnfInfo();
        detailedVnf.setId(VNF_ID);
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(detailedVnf));
        OperationExecution olderOperation = new OperationExecution();
        olderOperation.setId(UUID.randomUUID().toString());
        olderOperation.setStartTime(OffsetDateTime.now());
        olderOperation.setStatus(OperationStatus.FINISHED);
        olderOperation.setOperationType(OperationType.TERMINATE);
        OperationExecution newerOperation = new OperationExecution();
        newerOperation.setId(UUID.randomUUID().toString());
        newerOperation.setStartTime(OffsetDateTime.now().plusDays(1));
        newerOperation.setStatus(OperationStatus.FINISHED);
        newerOperation.setOperationType(OperationType.MODIFY_INFO);
        detailedVnf.setOperationExecutions(new ArrayList<>());
        detailedVnf.getOperationExecutions().add(olderOperation);
        detailedVnf.getOperationExecutions().add(newerOperation);
        JsonElement operationParams = new JsonParser().parse("{ \"additionalParams\" : { \"jobId\" : \"" + jobId + "\"}}");
        List<String> queriedOperaionsInOrder = new ArrayList<>();
        when(operationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet(Mockito.anyString(), Mockito.eq(NOKIA_LCM_API_VERSION)))
                .then(new Answer<Observable<Object>>() {
                    @Override
                    public Observable<Object> answer(InvocationOnMock invocationOnMock) throws Throwable {
                        queriedOperaionsInOrder.add(invocationOnMock.getArguments()[0].toString());
                        if (invocationOnMock.getArguments()[0].equals(olderOperation.getId())) {
                            return buildObservable(new JsonParser().parse("{ \"additionalParams\" : { \"jobId\" : \"" + jobId + "\"}}"));
                        } else {
                            throw new RuntimeException(); //this should be never reached
                        }
                    }
                });
        JobDetailInfo job = jobManager.getJob(VNFM_ID, jobId);
        //verify
        assertEquals(Lists.newArrayList(olderOperation.getId()), queriedOperaionsInOrder);
        assertTrue(jobManager.hasOngoingJobs());
    }

    /**
     * if the registration process has not finished it is prevented to spawn jobs
     */
    @Test
    public void noJobCanBeStartedIfRegistrationNotFinished() throws Exception {
        //given
        when(selfRegistrationManagerForVfc.isReady()).thenReturn(false);
        //when
        try {
            jobManager.spawnJob(VNF_ID, httpResponse);
            fail();
        } catch (RuntimeException e) {
            assertEquals("The service is not yet ready", e.getMessage());
        }
    }

    /**
     * Ongoing job are out waited during the the preparation for shutdown
     */
    @Test
    //need to wait for an asynchronous execution to finish
    //this is the most optimal way to do it
    @SuppressWarnings("squid:S2925")
    public void onGoingJobsAreOutwaitedDuringShutdown() throws Exception {
        String firstJobId = jobManager.spawnJob(VNF_ID, httpResponse);
        ExecutorService executorService = Executors.newCachedThreadPool();
        ArgumentCaptor<Integer> sleeps = ArgumentCaptor.forClass(Integer.class);
        doNothing().when(systemFunctions).sleep(sleeps.capture());
        //when prepare job manager for shutdown
        Future<?> shutDown = executorService.submit(() -> jobManager.prepareForShutdown());
        while (sleeps.getAllValues().size() == 0) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }
        }
        assertFalse(shutDown.isDone());
        jobManager.jobFinished(firstJobId);
        //verify
        shutDown.get();
        verify(systemFunctions, times(sleeps.getAllValues().size())).sleep(500L);
    }
}
