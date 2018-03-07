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
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nokia.cbam.lcm.v32.ApiException;
import com.nokia.cbam.lcm.v32.api.OperationExecutionsApi;
import com.nokia.cbam.lcm.v32.api.VnfsApi;
import com.nokia.cbam.lcm.v32.model.*;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api.INotificationSender;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.SystemFunctions;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.DriverProperties;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.ILifecycleChangeNotificationManager;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.LifecycleManager;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.tryFind;
import static com.google.common.collect.Sets.newConcurrentHashSet;
import static com.nokia.cbam.lcm.v32.model.OperationType.INSTANTIATE;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.childElement;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.fatalFailure;
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
@Component
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
    private static final Set<OperationStatus> terminalStatus = Sets.newHashSet(OperationStatus.FINISHED, OperationStatus.FAILED);
    private static Logger logger = getLogger(LifecycleChangeNotificationManager.class);

    private final CbamRestApiProvider restApiProvider;
    private final DriverProperties driverProperties;
    private final INotificationSender notificationSender;
    private Set<ProcessedNotification> processedNotifications = newConcurrentHashSet();

    @Autowired
    LifecycleChangeNotificationManager(CbamRestApiProvider restApiProvider, DriverProperties driverProperties, INotificationSender notificationSender) {
        this.notificationSender = notificationSender;
        this.driverProperties = driverProperties;
        this.restApiProvider = restApiProvider;
    }

    @VisibleForTesting
    static OperationExecution findLastInstantiationBefore(List<OperationExecution> operationExecutions, OperationExecution currentOperation) {
        for (OperationExecution opExs : filter(NEWEST_OPERATIONS_FIRST.sortedCopy(operationExecutions), (OperationExecution opex2) -> !opex2.getStartTime().isAfter(currentOperation.getStartTime()))) {
            if (INSTANTIATE.equals(opExs.getOperationType()) &&
                    (opExs.getStartTime().toLocalDate().isBefore(currentOperation.getStartTime().toLocalDate()) ||
                            opExs.getStartTime().toLocalDate().isEqual(currentOperation.getStartTime().toLocalDate())
                    )) {
                return opExs;
            }
        }
        throw new NoSuchElementException();
    }

    @Override
    public void handleLcn(VnfLifecycleChangeNotification recievedNotification) {
        if (logger.isInfoEnabled()) {
            logger.info("Received LCN: " + new Gson().toJson(recievedNotification));
        }
        VnfsApi cbamLcmApi = restApiProvider.getCbamLcmApi(driverProperties.getVnfmId());
        try {
            List<VnfInfo> vnfs = cbamLcmApi.vnfsGet(NOKIA_LCM_API_VERSION);
            com.google.common.base.Optional<VnfInfo> currentVnf = tryFind(vnfs, vnf -> vnf.getId().equals(recievedNotification.getVnfInstanceId()));
            String vnfHeader = "The VNF with " + recievedNotification.getVnfInstanceId() + " identifier";
            if (!currentVnf.isPresent()) {
                logger.warn(vnfHeader + " disappeared before being able to process the LCN");
                //swallow LCN
                return;
            } else {
                VnfInfo vnf = cbamLcmApi.vnfsVnfInstanceIdGet(recievedNotification.getVnfInstanceId(), NOKIA_LCN_API_VERSION);
                com.google.common.base.Optional<VnfProperty> externalVnfmId = tryFind(vnf.getExtensions(), prop -> prop.getName().equals(LifecycleManager.EXTERNAL_VNFM_ID));
                if (!externalVnfmId.isPresent()) {
                    logger.warn(vnfHeader + " is not a managed VNF");
                    return;
                }
                if (!externalVnfmId.get().getValue().equals(driverProperties.getVnfmId())) {
                    logger.warn(vnfHeader + " is not a managed by the VNFM with id " + externalVnfmId.get().getValue());
                    return;
                }
            }
        } catch (Exception e) {
            fatalFailure(logger, "Unable to list VNFs / query VNF", e);
        }
        OperationExecutionsApi cbamOperationExecutionApi = restApiProvider.getCbamOperationExecutionApi(driverProperties.getVnfmId());
        try {
            List<OperationExecution> operationExecutions = cbamLcmApi.vnfsVnfInstanceIdOperationExecutionsGet(recievedNotification.getVnfInstanceId(), NOKIA_LCM_API_VERSION);
            OperationExecution operationExecution = cbamOperationExecutionApi.operationExecutionsOperationExecutionIdGet(recievedNotification.getLifecycleOperationOccurrenceId(), NOKIA_LCM_API_VERSION);
            OperationExecution closestInstantiationToOperation = findLastInstantiationBefore(operationExecutions, operationExecution);
            String vimId = getVimId(cbamOperationExecutionApi, closestInstantiationToOperation);
            notificationSender.processNotification(recievedNotification, operationExecution, buildAffectedCps(operationExecution), vimId);
            if (OperationType.TERMINATE.equals(recievedNotification.getOperation()) && terminalStatus.contains(recievedNotification.getStatus())) {
                processedNotifications.add(new ProcessedNotification(recievedNotification.getLifecycleOperationOccurrenceId(), recievedNotification.getStatus()));
            }
        } catch (ApiException e) {
            fatalFailure(logger, "Unable to retrieve the current VNF " + recievedNotification.getVnfInstanceId(), e);
        }
    }

    private String getVimId(OperationExecutionsApi cbamOperationExecutionApi, OperationExecution closestInstantiationToOperation) {
        try {
            Object operationParams = cbamOperationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet(closestInstantiationToOperation.getId(), NOKIA_LCM_API_VERSION);
            return getVimId(operationParams);
        } catch (Exception e) {
            throw fatalFailure(logger, "Unable to detect last instantiation operation", e);
        }
    }

    @Override
    public void waitForTerminationToBeProcessed(String operationExecutionId) {
        while (true) {
            com.google.common.base.Optional<ProcessedNotification> notification = Iterables.tryFind(processedNotifications, processedNotification -> processedNotification.getOperationExecutionId().equals(operationExecutionId));
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

    private ReportedAffectedConnectionPoints buildAffectedCps(OperationExecution operationExecution) {
        if (operationExecution.getOperationType() == OperationType.TERMINATE) {
            String terminationType = childElement(new Gson().toJsonTree(operationExecution.getOperationParams()).getAsJsonObject(), "terminationType").getAsString();
            if (TerminationType.FORCEFUL.name().equals(terminationType)) {
                //in case of force full termination the Ansible is not executed, so the connection points can not be
                //calculated from operation execution result
                logger.warn("Unable to send information related to affected connection points during forceful termination");
                return null;
            }
        }
        try {
            JsonElement root = new Gson().toJsonTree(operationExecution.getAdditionalData());
            if (root.getAsJsonObject().has("operationResult")) {
                JsonObject operationResult = root.getAsJsonObject().get("operationResult").getAsJsonObject();
                if (!isPresent(operationResult, "cbam_pre") || !isPresent(operationResult, "cbam_post")) {
                    handleFailure(operationExecution, null);
                }
                return new Gson().fromJson(operationResult, ReportedAffectedConnectionPoints.class);
            }
        } catch (Exception e) {
            handleFailure(operationExecution, e);
        }
        return new ReportedAffectedConnectionPoints();
    }

    private boolean isPresent(JsonObject operationResult, String key) {
        return operationResult.has(key) && operationResult.get(key).isJsonArray();
    }

    private void handleFailure(OperationExecution operationExecution, Exception e) {
        switch (operationExecution.getStatus()) {
            case FAILED:
            case OTHER:
                logger.warn("The operation failed and the affected connection points were not reported");
                break;
            case STARTED: //can not happen (the changed resources are only executed for terminal state
            case FINISHED:
                if (e != null) {
                    fatalFailure(logger, PROBLEM, e);
                }
                fatalFailure(logger, PROBLEM);
        }
    }
}
