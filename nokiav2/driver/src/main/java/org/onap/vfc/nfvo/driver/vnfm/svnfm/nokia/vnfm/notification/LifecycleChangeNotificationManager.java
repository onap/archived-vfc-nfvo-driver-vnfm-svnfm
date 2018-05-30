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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Ordering;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nokia.cbam.lcm.v32.api.OperationExecutionsApi;
import com.nokia.cbam.lcm.v32.api.VnfsApi;
import com.nokia.cbam.lcm.v32.model.*;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api.INotificationSender;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core.SelfRegistrationManager;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.OperationMustBeAborted;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.SystemFunctions;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.ILifecycleChangeNotificationManager;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.LifecycleManager;
import org.slf4j.Logger;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Iterables.tryFind;
import static com.google.common.collect.Sets.newConcurrentHashSet;
import static com.google.common.collect.Sets.newHashSet;
import static com.nokia.cbam.lcm.v32.model.OperationType.INSTANTIATE;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.buildFatalFailure;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.childElement;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProvider.NOKIA_LCM_API_VERSION;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProvider.NOKIA_LCN_API_VERSION;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Responsible for handling lifecycle change notifications from CBAM.
 * The received LCNs are transformed into ONAP LCNs.
 * The following CBAM LCNs are processed:
 * - HEAL
 * - INSTANTIATE
 * - SCALE
 * - TERMINATE
 * The current limitations
 * - if a LCN can not be be processed due to VNF having been deleted the problem is logged and CBAM is notified that
 * the LCN has been processed (even if not in reality) because the signaling of failed LCN delivery blocks the delivery
 * on all LCN deliveries. The consequence of this is that the information known by VF-C / A&AI may be inconsistent with
 * reality (VNF having been deleted)
 */
public class LifecycleChangeNotificationManager implements ILifecycleChangeNotificationManager {

    public static final String PROBLEM = "All operations must return the { \"operationResult\" : { \"cbam_pre\" : [<fillMeOut>], \"cbam_post\" : [<fillMeOut>] } } structure";
    /**
     * Order the operations by start time (latest first)
     */
    public static final Ordering<OperationExecution> NEWEST_OPERATIONS_FIRST = new Ordering<OperationExecution>() {
        @Override
        public int compare(OperationExecution left, OperationExecution right) {
            return right.getStartTime().toLocalDate().compareTo(left.getStartTime().toLocalDate());
        }
    };
    /**
     * < Separates the VNF id and the resource id within a VNF
     */
    private static final Set<OperationStatus> terminalStatus = newHashSet(OperationStatus.FINISHED, OperationStatus.FAILED);
    private static Logger logger = getLogger(LifecycleChangeNotificationManager.class);

    private final CbamRestApiProvider restApiProvider;
    private final INotificationSender notificationSender;
    private final SelfRegistrationManager selfRegistrationManager;
    private Set<ProcessedNotification> processedNotifications = newConcurrentHashSet();

    LifecycleChangeNotificationManager(CbamRestApiProvider restApiProvider, SelfRegistrationManager selfRegistrationManager, INotificationSender notificationSender) {
        this.notificationSender = notificationSender;
        this.restApiProvider = restApiProvider;
        this.selfRegistrationManager = selfRegistrationManager;
    }

    /**
     * @param status the status of the operation
     * @return has the operation finished
     */
    public static boolean isTerminal(OperationStatus status) {
        return terminalStatus.contains(status);
    }

    @VisibleForTesting
    static OperationExecution findLastInstantiationBefore(List<OperationExecution> operationExecutions, OperationExecution currentOperation) {
        return find(NEWEST_OPERATIONS_FIRST.sortedCopy(operationExecutions), (OperationExecution opex2) ->
                !opex2.getStartTime().isAfter(currentOperation.getStartTime())
                        && INSTANTIATE.equals(opex2.getOperationType()));
    }

    @Override
    public void handleLcn(VnfLifecycleChangeNotification receivedNotification) {
        if (logger.isInfoEnabled()) {
            logger.info("Received LCN: {}", new Gson().toJson(receivedNotification));
        }
        String vnfmId = selfRegistrationManager.getVnfmId(receivedNotification.getSubscriptionId());
        VnfsApi cbamLcmApi = restApiProvider.getCbamLcmApi(vnfmId);
        try {
            List<VnfInfo> vnfs = cbamLcmApi.vnfsGet(NOKIA_LCM_API_VERSION).blockingFirst();
            com.google.common.base.Optional<VnfInfo> currentVnf = tryFind(vnfs, vnf -> vnf.getId().equals(receivedNotification.getVnfInstanceId()));
            String vnfHeader = "The VNF with " + receivedNotification.getVnfInstanceId() + " identifier";
            if (!currentVnf.isPresent()) {
                logger.warn(vnfHeader + " disappeared before being able to process the LCN");
                //swallow LCN
                return;
            } else {
                VnfInfo vnf = cbamLcmApi.vnfsVnfInstanceIdGet(receivedNotification.getVnfInstanceId(), NOKIA_LCN_API_VERSION).blockingFirst();
                com.google.common.base.Optional<VnfProperty> externalVnfmId = tryFind(vnf.getExtensions(), prop -> prop.getName().equals(LifecycleManager.EXTERNAL_VNFM_ID));
                if (!externalVnfmId.isPresent()) {
                    logger.warn(vnfHeader + " is not a managed VNF");
                    return;
                }
                if (!externalVnfmId.get().getValue().equals(vnfmId)) {
                    logger.warn(vnfHeader + " is not a managed by the VNFM with id " + externalVnfmId.get().getValue());
                    return;
                }
            }
        } catch (Exception e) {
            throw buildFatalFailure(logger, "Unable to list VNFs / query VNF", e);
        }
        OperationExecutionsApi cbamOperationExecutionApi = restApiProvider.getCbamOperationExecutionApi(vnfmId);
        List<OperationExecution> operationExecutions;
        try {
            operationExecutions = cbamLcmApi.vnfsVnfInstanceIdOperationExecutionsGet(receivedNotification.getVnfInstanceId(), NOKIA_LCM_API_VERSION).blockingFirst();
        } catch (Exception e) {
            throw buildFatalFailure(logger, "Unable to retrieve the operation executions for the VNF " + receivedNotification.getVnfInstanceId(), e);
        }
        OperationExecution operationExecution;
        try {
            operationExecution = cbamOperationExecutionApi.operationExecutionsOperationExecutionIdGet(receivedNotification.getLifecycleOperationOccurrenceId(), NOKIA_LCM_API_VERSION).blockingFirst();
        } catch (Exception e) {
            throw buildFatalFailure(logger, "Unable to retrieve the operation execution with " + receivedNotification.getLifecycleOperationOccurrenceId() + " identifier", e);
        }
        OperationExecution closestInstantiationToOperation = findLastInstantiationBefore(operationExecutions, operationExecution);
        String vimId = getVimId(cbamOperationExecutionApi, closestInstantiationToOperation);
        notificationSender.processNotification(receivedNotification, operationExecution, buildAffectedCps(operationExecution), vimId, vnfmId);
        if (isTerminationFinished(receivedNotification)) {
            //signal LifecycleManager to continue the deletion of the VNF
            processedNotifications.add(new ProcessedNotification(receivedNotification.getLifecycleOperationOccurrenceId(), receivedNotification.getStatus()));
        }
    }

    private boolean isTerminationFinished(VnfLifecycleChangeNotification receivedNotification) {
        return OperationType.TERMINATE.equals(receivedNotification.getOperation()) && terminalStatus.contains(receivedNotification.getStatus());
    }

    private String getVimId(OperationExecutionsApi cbamOperationExecutionApi, OperationExecution closestInstantiationToOperation) {
        try {
            Object operationParams = cbamOperationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet(closestInstantiationToOperation.getId(), NOKIA_LCM_API_VERSION).blockingFirst();
            return getVimId(operationParams);
        } catch (Exception e) {
            throw buildFatalFailure(logger, "Unable to detect last instantiation operation", e);
        }
    }

    @Override
    public void waitForTerminationToBeProcessed(String operationExecutionId) {
        while (true) {
            com.google.common.base.Optional<ProcessedNotification> notification = tryFind(processedNotifications, processedNotification -> processedNotification.getOperationExecutionId().equals(operationExecutionId));
            if (notification.isPresent()) {
                processedNotifications.remove(notification.get());
                return;
            }
            SystemFunctions.systemFunctions().sleep(500);
        }
    }

    private String getVimId(Object instantiationParameters) {
        InstantiateVnfRequest request = new Gson().fromJson(new Gson().toJson(instantiationParameters), InstantiateVnfRequest.class);
        return request.getVims().get(0).getId();
    }

    private Optional<ReportedAffectedConnectionPoints> buildAffectedCps(OperationExecution operationExecution) {
        if (!isTerminal(operationExecution.getStatus())) {
            //connection points can only be calculated after the operation has finished
            return Optional.empty();
        }
        if (operationExecution.getOperationType() == OperationType.TERMINATE) {
            String terminationType = childElement(new Gson().toJsonTree(operationExecution.getOperationParams()).getAsJsonObject(), "terminationType").getAsString();
            if (TerminationType.FORCEFUL.name().equals(terminationType)) {
                //in case of force full termination the Ansible is not executed, so the connection points can not be
                //calculated from operation execution result
                logger.warn("Unable to send information related to affected connection points during forceful termination");
                return empty();
            } else {
                //graceful termination should be handled as any other operation
            }
        }
        try {
            JsonElement root = new Gson().toJsonTree(operationExecution.getAdditionalData());
            if (root.getAsJsonObject().has("operationResult")) {
                JsonObject operationResult = root.getAsJsonObject().get("operationResult").getAsJsonObject();
                if (isAbsent(operationResult, "cbam_pre") ||
                        isAbsent(operationResult, "cbam_post")) {
                    return handleFailure(operationExecution);
                } else {
                    return of(new Gson().fromJson(operationResult, ReportedAffectedConnectionPoints.class));
                }
            } else {
                return handleFailure(operationExecution);
            }
        } catch (OperationMustBeAborted handledFailuire) {
            throw handledFailuire;
        } catch (Exception e) {
            logger.warn("Unable to build affected connection points", e);
            return toleratedFailure();
        }
    }

    private boolean isAbsent(JsonObject operationResult, String key) {
        return !operationResult.has(key) || !operationResult.get(key).isJsonArray();
    }

    private Optional<ReportedAffectedConnectionPoints> handleFailure(OperationExecution operationExecution) {
        if (operationExecution.getStatus() == OperationStatus.FAILED) {
            return toleratedFailure();
        } else {
            throw buildFatalFailure(logger, PROBLEM);
        }
    }

    private Optional<ReportedAffectedConnectionPoints> toleratedFailure() {
        logger.warn("The operation failed and the affected connection points were not reported");
        return empty();
    }
}
