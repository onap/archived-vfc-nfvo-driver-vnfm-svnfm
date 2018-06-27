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

import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.nokia.cbam.lcm.v32.api.OperationExecutionsApi;
import com.nokia.cbam.lcm.v32.api.VnfsApi;
import com.nokia.cbam.lcm.v32.model.OperationExecution;
import com.nokia.cbam.lcm.v32.model.OperationType;
import com.nokia.cbam.lcm.v32.model.VnfInfo;
import java.util.*;
import javax.servlet.http.HttpServletResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core.SelfRegistrationManager;
import org.onap.vnfmdriver.model.*;
import org.slf4j.Logger;

import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import static com.google.common.base.Splitter.on;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Iterables.tryFind;
import static com.google.common.collect.Lists.newArrayList;
import static com.nokia.cbam.lcm.v32.model.OperationStatus.FAILED;
import static com.nokia.cbam.lcm.v32.model.OperationStatus.STARTED;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.SEPARATOR;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.buildFatalFailure;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.SystemFunctions.systemFunctions;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProvider.NOKIA_LCM_API_VERSION;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification.LifecycleChangeNotificationManager.NEWEST_OPERATIONS_FIRST;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.StringUtils.isEmpty;

/**
 * Responsible for providing the status of jobs
 * The job id is a composite field of the VNF identifier and an UUID.
 * The second UUID is passed as mandatory parameter to each executed operation.
 * This UUID is used to locate the operation execution from the ONAP job identifier
 */
public class JobManager {
    public static final String OPERATION_STARTED_DESCRIPTION = "Operation started";
    private static final Ordering<JobResponseInfo> OLDEST_FIRST = new Ordering<JobResponseInfo>() {
        @Override
        public int compare(JobResponseInfo left, JobResponseInfo right) {
            return Long.valueOf(left.getResponseId()).compareTo(Long.valueOf(right.getResponseId()));
        }
    };
    private static Logger logger = getLogger(JobManager.class);
    private final Set<String> ongoingJobs = Sets.newConcurrentHashSet();
    private final CbamRestApiProvider cbamRestApiProvider;
    private final SelfRegistrationManager selfRegistrationManager;
    private volatile boolean preparingForShutDown = false;

    JobManager(CbamRestApiProvider cbamRestApiProvider, SelfRegistrationManager selfRegistrationManager) {
        this.cbamRestApiProvider = cbamRestApiProvider;
        this.selfRegistrationManager = selfRegistrationManager;
    }

    /**
     * @param operationParams the operation execution
     * @return the ONAP job identifier of belonging to the operation execution
     */
    public static String extractOnapJobId(Object operationParams) {
        JsonElement operationParamsAsJson = new Gson().toJsonTree(operationParams);
        JsonElement additionalParams = operationParamsAsJson.getAsJsonObject().get("additionalParams");
        if (additionalParams == null) {
            throw new NoSuchElementException("The operation result " + operationParamsAsJson + " does not contain the mandatory additionalParams structure");
        }
        JsonElement jobId = additionalParams.getAsJsonObject().get("jobId");
        if (jobId == null) {
            throw new NoSuchElementException("The operation result " + operationParamsAsJson + " does not contain the mandatory jobId in the additionalParams structure");
        }
        return jobId.getAsString();
    }

    /**
     * @return is the component preparing for shutdown
     */
    public boolean isPreparingForShutDown() {
        return preparingForShutDown;
    }

    /**
     * Throws an exception in case the service is not ready to serve requests due to
     * not being able to register to MSB or to subscribe to CBAM LCNs
     *
     * @param vnfId    the identifier of the VNF
     * @param response the HTTP response of the current sVNFM incomming request
     * @return the identifier of the job
     */
    public String spawnJob(String vnfId, HttpServletResponse response) {
        String jobId = vnfId + SEPARATOR + UUID.randomUUID().toString();
        synchronized (this) {
            if (preparingForShutDown) {
                response.setStatus(SC_SERVICE_UNAVAILABLE);
                throw buildFatalFailure(logger, "The service is preparing to shut down");
            }
            if (!selfRegistrationManager.isReady()) {
                response.setStatus(SC_SERVICE_UNAVAILABLE);
                throw buildFatalFailure(logger, "The service is not yet ready");
            }
        }
        ongoingJobs.add(jobId);
        return jobId;
    }

    /**
     * Signal that a job has finished
     *
     * @param jobId the identifier of the job
     */
    public void jobFinished(String jobId) {
        ongoingJobs.remove(jobId);
    }

    /**
     * @return the system has any ongoing jobs
     */
    public boolean hasOngoingJobs() {
        return !ongoingJobs.isEmpty();
    }


    /**
     * Wait for all jobs to be cleared from the system the refuses to let additional request in
     */
    public void prepareForShutdown() {
        preparingForShutDown = true;
        while (true) {
            synchronized (this) {
                if (!hasOngoingJobs()) {
                    return;
                }
            }
            systemFunctions().sleep(500L);
        }
    }

    /**
     * @param vnfmId the identifier of the VNFM
     * @param jobId  the identifier of the job
     * @return detailed information of the job
     */
    public JobDetailInfo getJob(String vnfmId, String jobId) {
        logger.debug("Retrieving the details for job with {} identifier", jobId);
        ArrayList<String> jobParts = newArrayList(on(SEPARATOR).split(jobId));
        if (jobParts.size() != 2) {
            throw new IllegalArgumentException("The jobId should be in the <vnfId>" + SEPARATOR + "<UUID> format, but was " + jobId);
        }
        String vnfId = jobParts.get(0);
        if (isEmpty(vnfId)) {
            throw new IllegalArgumentException("The vnfId in the jobId (" + jobId + ") can not be empty");
        }
        String operationExecutionId = jobParts.get(1);
        if (isEmpty(operationExecutionId)) {
            throw new IllegalArgumentException("The UUID in the jobId (" + jobId + ") can not be empty");
        }
        Optional<VnfInfo> vnf = getVnf(vnfmId, vnfId);
        if (!vnf.isPresent()) {
            return getJobDetailInfoForMissingVnf(jobId);
        } else {
            return getJobInfoForExistingVnf(vnfmId, jobId, vnfId, vnf.get());
        }
    }

    public void waitForJobToFinish(JobInfo jobInfo) {
        while(true){
            if(ongoingJobs.contains(jobInfo.getJobId())){
                return;
            }
            systemFunctions().sleep(500L);
        }
    }

    private JobDetailInfo getJobDetailInfoForMissingVnf(String jobId) {
        if (ongoingJobs.contains(jobId)) {
            return reportOngoing(jobId);
        } else {
            return reportFinished(jobId);
        }
    }

    private JobDetailInfo getJobInfoForExistingVnf(String vnfmId, String jobId, String vnfId, VnfInfo vnf) {
        try {
            OperationExecution operation = findOperationByJobId(vnfmId, vnf, jobId);
            return getJobDetailInfo(vnfmId, jobId, vnfId, operation);
        } catch (NoSuchElementException e) {
            logger.warn("No operation could be identified for job with {} identifier", jobId, e);
            if (ongoingJobs.contains(jobId)) {
                return reportOngoing(jobId);
            } else {
                return reportFailed(jobId, "The requested operation was not able to start on CBAM");
            }
        }
    }

    private JobDetailInfo getJobDetailInfo(String vnfmId, String jobId, String vnfId, OperationExecution operation) {
        if (operation.getStatus() == STARTED) {
            return reportOngoing(jobId);
        } else if (operation.getStatus() == FAILED) {
            return reportFailed(jobId, operation.getError().getTitle() + ": " + operation.getError().getDetail());
        } else {
            return getJobForTerminalOperationState(vnfmId, jobId, vnfId, operation);
        }
    }

    private JobDetailInfo getJobForTerminalOperationState(String vnfmId, String jobId, String vnfId, OperationExecution operation) {
        //termination includes VNF deletion in ONAP terminology
        if (operation.getOperationType() == com.nokia.cbam.lcm.v32.model.OperationType.TERMINATE) {
            if (ongoingJobs.contains(jobId)) {
                return reportOngoing(jobId);
            } else {
                //the VNF must be queried again since it could have been deleted since the VNF has been terminated
                if (getVnf(vnfmId, vnfId).isPresent()) {
                    return reportFailed(jobId, "unable to delete VNF");
                } else {
                    return reportFinished(jobId);
                }
            }
        } else {
            return reportFinished(jobId);
        }
    }

    private JobDetailInfo buildJob(String jobId, JobResponseInfo... history) {
        JobDetailInfo job = new JobDetailInfo();
        job.setJobId(jobId);
        JobDetailInfoResponseDescriptor jobDetailInfoResponseDescriptor = new JobDetailInfoResponseDescriptor();
        job.setResponseDescriptor(jobDetailInfoResponseDescriptor);
        List<JobResponseInfo> oldestFirst = OLDEST_FIRST.sortedCopy(newArrayList(history));
        JobResponseInfo newestJob = oldestFirst.get(oldestFirst.size() - 1);
        jobDetailInfoResponseDescriptor.setResponseId(newestJob.getResponseId());
        jobDetailInfoResponseDescriptor.setStatus(JobStatus.valueOf(newestJob.getStatus()));
        jobDetailInfoResponseDescriptor.setProgress(newestJob.getProgress());
        jobDetailInfoResponseDescriptor.setStatusDescription(newestJob.getStatusDescription());
        jobDetailInfoResponseDescriptor.setErrorCode(newestJob.getErrorCode());
        jobDetailInfoResponseDescriptor.setResponseHistoryList(oldestFirst);
        return job;
    }

    private JobResponseInfo buildJobPart(String description, JobStatus status, Integer progress, Integer responseId) {
        JobResponseInfo currentJob = new JobResponseInfo();
        currentJob.setProgress(progress.toString());
        currentJob.setResponseId(responseId.toString());
        currentJob.setStatus(status.name());
        currentJob.setStatusDescription(description);
        return currentJob;
    }

    private JobDetailInfo reportOngoing(String jobId) {
        return buildJob(jobId, buildJobPart(OPERATION_STARTED_DESCRIPTION, JobStatus.STARTED, 50, 1));
    }

    private JobDetailInfo reportFailed(String jobId, String reason) {
        return buildJob(jobId,
                buildJobPart(OPERATION_STARTED_DESCRIPTION, JobStatus.STARTED, 50, 1),
                buildJobPart("Operation failed due to " + reason, JobStatus.ERROR, 100, 2)
        );
    }

    private JobDetailInfo reportFinished(String jobId) {
        return buildJob(jobId,
                buildJobPart(OPERATION_STARTED_DESCRIPTION, JobStatus.STARTED, 50, 1),
                buildJobPart("Operation finished", JobStatus.FINISHED, 100, 2)
        );
    }

    private OperationExecution findOperationByJobId(String vnfmId, VnfInfo vnf, String jobId) {
        OperationExecutionsApi cbamOperationExecutionApi = cbamRestApiProvider.getCbamOperationExecutionApi(vnfmId);
        //the operations are sorted so that the newest operations are queried first
        //performance optimization that usually the core system is interested in the operations executed last
        if (vnf.getOperationExecutions() != null) {
            List<OperationExecution> sortedOperation = NEWEST_OPERATIONS_FIRST.sortedCopy(vnf.getOperationExecutions());
            return find(sortedOperation, operation -> isCurrentOperationTriggeredByJob(jobId, cbamOperationExecutionApi, operation));
        }
        throw new NoSuchElementException();
    }

    private boolean isCurrentOperationTriggeredByJob(String jobId, OperationExecutionsApi cbamOperationExecutionApi, OperationExecution operationExecution) {
        if (OperationType.MODIFY_INFO.equals(operationExecution.getOperationType())) {
            //the modify info is never triggered by an external job
            return false;
        }
        try {
            Object operationParams = cbamOperationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet(operationExecution.getId(), NOKIA_LCM_API_VERSION).blockingFirst();
            if (extractOnapJobId(operationParams).equals(jobId)) {
                return true;
            }
        } catch (Exception e) {
            throw buildFatalFailure(logger, "Unable to retrieve operation parameters of operation with " + operationExecution.getId() + " identifier", e);
        }
        return false;
    }

    private Optional<VnfInfo> getVnf(String vnfmId, String vnfId) {
        try {
            //test if the VNF exists (required to be able to distingush between failed request )
            VnfsApi cbamLcmApi = cbamRestApiProvider.getCbamLcmApi(vnfmId);
            logger.debug("Listing VNFs");
            List<VnfInfo> vnfs = cbamLcmApi.vnfsGet(NOKIA_LCM_API_VERSION).blockingSingle();
            com.google.common.base.Optional<VnfInfo> vnf = tryFind(vnfs, vnfInfo -> vnfId.equals(vnfInfo.getId()));
            if (!vnf.isPresent()) {
                logger.debug("VNF with {} identifier is missing", vnfId);
                return empty();
            } else {
                logger.debug("VNF with {} identifier still exists", vnfId);
                //query the VNF again to get operation execution result
                return of(cbamLcmApi.vnfsVnfInstanceIdGet(vnfId, NOKIA_LCM_API_VERSION).blockingFirst());
            }
        } catch (Exception e) {
            throw buildFatalFailure(logger, "Unable to retrieve VNF with " + vnfId + " identifier", e);
        }
    }
}
