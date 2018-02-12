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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.impl;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.nokia.cbam.lcm.v32.ApiException;
import com.nokia.cbam.lcm.v32.model.*;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.onap.vnfmdriver.model.JobDetailInfo;
import org.onap.vnfmdriver.model.JobResponseInfo;
import org.onap.vnfmdriver.model.JobStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.threeten.bp.OffsetDateTime;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static junit.framework.TestCase.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.impl.JobManager.SEPARATOR;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.impl.JobManager.extractOnapJobId;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.rest.CbamRestApiProvider.NOKIA_LCM_API_VERSION;
import static org.onap.vnfmdriver.model.JobStatus.*;

public class TestJobManager extends TestBase {

    @Mock
    private HttpServletResponse httpResponse;

    @InjectMocks
    private JobManager jobManager;
    private List<VnfInfo> vnfs = new ArrayList<>();

    @Before
    public void initMocks() throws Exception {
        ReflectionTestUtils.setField(JobManager.class, "logger", logger);
        when(vnfApi.vnfsGet(NOKIA_LCM_API_VERSION)).thenReturn(vnfs);
        when(selfRegistrationManager.isReady()).thenReturn(true);
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
     * The operation result must contain the ONAP job identier under the jobId field
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
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(detailedVnf);
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
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(detailedVnf);
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
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(detailedVnf);
        OperationExecution operation = new OperationExecution();
        operation.setId(UUID.randomUUID().toString());
        operation.setStartTime(OffsetDateTime.now());
        operation.setStatus(OperationStatus.STARTED);
        detailedVnf.setOperationExecutions(new ArrayList<>());
        detailedVnf.getOperationExecutions().add(operation);
        JsonElement operationParams = new JsonParser().parse("{ \"additionalParams\" : { \"jobId\" : \"" + jobId + "\"}}");
        when(operationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet(operation.getId(), NOKIA_LCM_API_VERSION)).thenReturn(operationParams);
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
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenAnswer(new Answer<VnfInfo>() {
            @Override
            public VnfInfo answer(InvocationOnMock invocation) throws Throwable {
                vnfs.clear();
                return detailedVnf;
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
        when(operationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet(operation.getId(), NOKIA_LCM_API_VERSION)).thenReturn(operationParams);
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
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(detailedVnf);
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
        when(operationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet(operation.getId(), NOKIA_LCM_API_VERSION)).thenReturn(operationParams);
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
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(detailedVnf);
        OperationExecution operation = new OperationExecution();
        operation.setId(UUID.randomUUID().toString());
        operation.setStartTime(OffsetDateTime.now());
        operation.setStatus(OperationStatus.FINISHED);
        operation.setOperationType(OperationType.SCALE);
        detailedVnf.setOperationExecutions(new ArrayList<>());
        detailedVnf.getOperationExecutions().add(operation);
        JsonElement operationParams = new JsonParser().parse("{ \"additionalParams\" : { \"jobId\" : \"" + jobId + "\"}}");
        when(operationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet(operation.getId(), NOKIA_LCM_API_VERSION)).thenReturn(operationParams);
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
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(detailedVnf);
        OperationExecution operation = new OperationExecution();
        operation.setId(UUID.randomUUID().toString());
        operation.setStartTime(OffsetDateTime.now());
        operation.setStatus(OperationStatus.FINISHED);
        operation.setOperationType(OperationType.TERMINATE);
        detailedVnf.setOperationExecutions(new ArrayList<>());
        detailedVnf.getOperationExecutions().add(operation);
        JsonElement operationParams = new JsonParser().parse("{ \"additionalParams\" : { \"jobId\" : \"" + jobId + "\"}}");
        when(operationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet(operation.getId(), NOKIA_LCM_API_VERSION)).thenReturn(operationParams);
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
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(detailedVnf);
        OperationExecution operation = new OperationExecution();
        operation.setId(UUID.randomUUID().toString());
        detailedVnf.setOperationExecutions(new ArrayList<>());
        detailedVnf.getOperationExecutions().add(operation);
        ApiException expectedException = new ApiException();
        when(operationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet(operation.getId(), NOKIA_LCM_API_VERSION)).thenThrow(expectedException);
        //verify
        try {
            JobDetailInfo job = jobManager.getJob(VNFM_ID, jobId);
            fail();
        } catch (RuntimeException e) {
            assertEquals(expectedException, e.getCause());
            verify(logger).error("Unable to retrieve operation parameters", expectedException);
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
        ApiException expectedException = new ApiException();
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenThrow(expectedException);
        //verify
        try {
            JobDetailInfo job = jobManager.getJob(VNFM_ID, jobId);
            fail();
        } catch (RuntimeException e) {
            assertEquals(expectedException, e.getCause());
            verify(logger).error("Unable to retrieve VNF", expectedException);
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
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(detailedVnf);
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
                .then(new Answer<Object>() {
                    @Override
                    public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                        queriedOperaionsInOrder.add(invocationOnMock.getArguments()[0].toString());
                        if (invocationOnMock.getArguments()[0].equals(olderOperation.getId())) {
                            return new JsonParser().parse("{ \"additionalParams\" : { \"jobId\" : \"" + jobId + "\"}}");
                        } else {
                            return new JsonParser().parse("{ \"additionalParams\" : { \"jobId\" : \"" + "nonMatching" + "\"}}");
                        }
                    }
                });
        JobDetailInfo job = jobManager.getJob(VNFM_ID, jobId);
        //verify
        assertEquals(Lists.newArrayList(newerOperation.getId(), olderOperation.getId()), queriedOperaionsInOrder);
        assertTrue(jobManager.hasOngoingJobs());
    }

    /**
     * if the registration process has not finished it is prevented to spawn jobs
     */
    @Test
    public void noJobCanBeStartedIfRegistrationNotFinished() throws Exception {
        //given
        when(selfRegistrationManager.isReady()).thenReturn(false);
        //when
        try {
            jobManager.spawnJob(VNF_ID, httpResponse);
            fail();
        } catch (RuntimeException e) {
            assertEquals("The serivce is not yet ready", e.getMessage());
        }
    }

}
