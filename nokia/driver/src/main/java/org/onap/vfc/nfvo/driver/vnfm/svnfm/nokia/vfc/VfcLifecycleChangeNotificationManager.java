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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.nokia.cbam.lcm.v32.ApiException;
import com.nokia.cbam.lcm.v32.api.OperationExecutionsApi;
import com.nokia.cbam.lcm.v32.api.VnfsApi;
import com.nokia.cbam.lcm.v32.model.AffectedVirtualLink;
import com.nokia.cbam.lcm.v32.model.AffectedVnfc;
import com.nokia.cbam.lcm.v32.model.ChangeType;
import com.nokia.cbam.lcm.v32.model.*;
import com.nokia.cbam.lcm.v32.model.OperationType;
import com.nokia.cbam.lcm.v32.model.VnfInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api.ILifecycleChangeNotificationManager;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.impl.DriverProperties;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.rest.CbamRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.SystemFunctions;
import org.onap.vnfmdriver.model.*;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.tryFind;
import static com.google.common.collect.Sets.newHashSet;
import static com.nokia.cbam.lcm.v32.model.OperationType.INSTANTIATE;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.impl.JobManager.extractOnapJobId;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.rest.CbamRestApiProvider.NOKIA_LCM_API_VERSION;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.rest.CbamRestApiProvider.NOKIA_LCN_API_VERSION;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.childElement;
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
public class VfcLifecycleChangeNotificationManager implements ILifecycleChangeNotificationManager {

    public static final String PROBLEM = "All operations must return the { \"operationResult\" : { \"cbam_pre\" : [<fillMeOut>], \"cbam_post\" : [<fillMeOut>] } } structure";
    /**
     * < Separates the VNF id and the resource id within a VNF
     */
    private static final Set<OperationStatus> terminalStatus = Sets.newHashSet(OperationStatus.FINISHED, OperationStatus.FAILED);
    private static Logger logger = getLogger(VfcLifecycleChangeNotificationManager.class);
    @Autowired
    private CbamRestApiProvider restApiProvider;
    @Autowired
    private VfcRestApiProvider vfcRestApiProvider;
    @Autowired
    private DriverProperties driverProperties;
    private Set<ProcessedNotification> processedNotifications = Sets.newConcurrentHashSet();

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
        logger.info("Recieved LCN: " + new Gson().toJson(recievedNotification));
        VnfsApi cbamLcmApi = restApiProvider.getCbamLcmApi(driverProperties.getVnfmId());
        try {
            List<VnfInfo> vnfs = cbamLcmApi.vnfsGet(NOKIA_LCM_API_VERSION);
            com.google.common.base.Optional<VnfInfo> currentVnf = tryFind(vnfs, vnf -> vnf.getId().equals(recievedNotification.getVnfInstanceId()));
            if (!currentVnf.isPresent()) {
                logger.warn("The VNF with " + recievedNotification.getVnfInstanceId() + " disapperaed before being able to process the LCN");
                //swallow LCN
                return;
            } else {
                VnfInfo vnf = cbamLcmApi.vnfsVnfInstanceIdGet(recievedNotification.getVnfInstanceId(), NOKIA_LCN_API_VERSION);
                com.google.common.base.Optional<VnfProperty> externalVnfmId = tryFind(vnf.getExtensions(), prop -> prop.getName().equals(EXTERNAL_VNFM_ID));
                if (!externalVnfmId.isPresent()) {
                    logger.warn("The VNF with " + vnf.getId() + " identifer is not a managed VNF");
                    return;
                }
                if (!externalVnfmId.get().getValue().equals(driverProperties.getVnfmId())) {
                    logger.warn("The VNF with " + vnf.getId() + " identifer is not a managed by the VNFM with id " + externalVnfmId.get().getValue());
                    return;
                }
            }
        } catch (Exception e) {
            logger.error("Unable to list VNFs / query VNF", e);
            throw new RuntimeException("Unable to list VNFs / query VNF", e);
        }
        OperationExecutionsApi cbamOperationExecutionApi = restApiProvider.getCbamOperationExecutionApi(driverProperties.getVnfmId());
        VNFLCMNotification notificationToSend = new VNFLCMNotification();
        try {
            List<OperationExecution> operationExecutions = cbamLcmApi.vnfsVnfInstanceIdOperationExecutionsGet(recievedNotification.getVnfInstanceId(), NOKIA_LCM_API_VERSION);
            OperationExecution operationExecution = cbamOperationExecutionApi.operationExecutionsOperationExecutionIdGet(recievedNotification.getLifecycleOperationOccurrenceId(), NOKIA_LCM_API_VERSION);
            notificationToSend.setJobId(extractOnapJobId(operationExecution.getOperationParams()));
            notificationToSend.setOperation(getOperation(driverProperties.getVnfmId(), recievedNotification.getVnfInstanceId(), operationExecution, recievedNotification.getOperation(), recievedNotification.getAffectedVnfcs()));
            notificationToSend.setVnfInstanceId(recievedNotification.getVnfInstanceId());
            switch (recievedNotification.getStatus()) {
                case FINISHED:
                case FAILED:
                    notificationToSend.setStatus(VnfLcmNotificationStatus.RESULT);
                    addAffectedVirtualLinks(recievedNotification, notificationToSend);
                    OperationExecution closestInstantiationToOperation = findLastInstantiationBefore(operationExecutions, operationExecution);
                    String vimId;
                    try {
                        Object operationParams = cbamOperationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet(closestInstantiationToOperation.getId(), NOKIA_LCM_API_VERSION);
                        vimId = getVimId(operationParams);
                    } catch (Exception e) {
                        logger.error("Unable to detect last instantiation operation", e);
                        throw new RuntimeException("Unable to detect last instantiation operation", e);
                    }
                    addAffectedVnfcs(vimId, recievedNotification.getVnfInstanceId(), notificationToSend, recievedNotification);
                    addAffectedCps(vimId, recievedNotification.getVnfInstanceId(), recievedNotification, notificationToSend, operationExecution);
                    break;
                default:
                    notificationToSend.setStatus(VnfLcmNotificationStatus.START);
                    break;
            }
            try {
                logger.info("Sending LCN: " + new Gson().toJson(notificationToSend));
                vfcRestApiProvider.getNsLcmApi().vNFLCMNotification(driverProperties.getVnfmId(), recievedNotification.getVnfInstanceId(), notificationToSend);
            } catch (Exception e) {
                logger.error("Unable to send LCN to ONAP", e);
                throw new RuntimeException(e);
            }
            if (OperationType.TERMINATE.equals(recievedNotification.getOperation()) && terminalStatus.contains(recievedNotification.getStatus())) {
                processedNotifications.add(new ProcessedNotification(recievedNotification.getLifecycleOperationOccurrenceId(), recievedNotification.getStatus()));
            }
        } catch (ApiException e) {
            logger.error("Unable to retrieve the current VNF " + recievedNotification.getVnfInstanceId(), e);
            throw new RuntimeException("Unable to retrieve the current VNF " + recievedNotification.getVnfInstanceId(), e);
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

    private void addAffectedCps(String vimId, String vnfId, VnfLifecycleChangeNotification receivedNotification, VNFLCMNotification notificationToSend, OperationExecution operationExecution) {
        switch (operationExecution.getOperationType()) {
            case TERMINATE:
                String terminationType = childElement(new Gson().toJsonTree(operationExecution.getOperationParams()).getAsJsonObject(), "terminationType").getAsString();
                if (TerminationType.FORCEFUL.name().equals(terminationType)) {
                    //in case of force full termination the Ansible is not executed, so the connection points can not be
                    //calculated from operation execution result
                    logger.warn("Unable to send information related to affected connection points during forceful termination");
                    return;
                }
        }
        try {
            JsonElement root = new Gson().toJsonTree(operationExecution.getAdditionalData());
            if (root.getAsJsonObject().has("operationResult")) {
                JsonObject operationResult = root.getAsJsonObject().get("operationResult").getAsJsonObject();
                if (!isPresent(operationResult, "cbam_pre") || !isPresent(operationResult, "cbam_post")) {
                    handleFailure(operationExecution, null);
                }
                ReportedAffectedConnectionPoints reportedAffectedConnectionPoints = new Gson().fromJson(operationResult, ReportedAffectedConnectionPoints.class);
                Set<ReportedAffectedCp> affectedCpsPost = newHashSet(reportedAffectedConnectionPoints.post);
                //adding the elements that might have been removed after the operation due to the
                //equals being specified
                affectedCpsPost.addAll(reportedAffectedConnectionPoints.pre);
                for (ReportedAffectedCp affectedCp : affectedCpsPost) {
                    if (affectedCp.cpdId != null) {
                        AffectedCp onapAffectedCp = buildAffectedCp(vimId, vnfId, affectedCp);
                        onapAffectedCp.setCpdid(affectedCp.cpdId);
                        notificationToSend.getAffectedCp().add(onapAffectedCp);
                    }
                    if (affectedCp.ecpdId != null) {
                        AffectedCp onapAffectedCp = buildAffectedCp(vimId, vnfId, affectedCp);
                        onapAffectedCp.setCpdid(affectedCp.ecpdId);
                        notificationToSend.getAffectedCp().add(onapAffectedCp);
                    }
                }
            }
        } catch (Exception e) {
            handleFailure(operationExecution, e);
        }
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
                    logger.error(PROBLEM, e);
                    throw new RuntimeException(PROBLEM, e);
                }
                logger.error(PROBLEM);
                throw new RuntimeException(PROBLEM);
        }
    }

    private AffectedCp buildAffectedCp(String vimId, String vnfId, ReportedAffectedCp affectedCp) {
        AffectedCp onapAffectedCp = new AffectedCp();
        AffectedCpPortResource port = new AffectedCpPortResource();
        port.setInstId(affectedCp.serverProviderId);
        port.setIpAddress(affectedCp.ipAddress);
        port.setMacAddress(affectedCp.macAddress);
        port.setResourceid(affectedCp.providerId);
        port.setResourceName(affectedCp.name);
        port.setTenant(affectedCp.tenantId);
        port.setVimid(vimId);
        onapAffectedCp.setPortResource(port);
        onapAffectedCp.setCpdid(affectedCp.cpdId);
        onapAffectedCp.setCpinstanceid(vnfId + SEPARATOR + affectedCp.cpId);
        onapAffectedCp.setVirtualLinkInstanceId(affectedCp.networkProviderId);
        onapAffectedCp.setChangeType(transform(affectedCp.changeType));
        //owner id & type can be left empty it will default to VNF id on VF-C
        return onapAffectedCp;
    }

    private VnfCpNotificationType transform(ChangeType changeType) {
        switch (changeType) {
            case ADDED:
                return VnfCpNotificationType.ADDED;
            case REMOVED:
                return VnfCpNotificationType.REMOVED;
            default: //can only be MODIFIED
                return VnfCpNotificationType.CHANGED;
        }
    }

    private void addAffectedVnfcs(String vimId, String vnfId, VNFLCMNotification notificationToSend, VnfLifecycleChangeNotification request) {
        for (AffectedVnfc affectedVnfc : request.getAffectedVnfcs()) {
            org.onap.vnfmdriver.model.AffectedVnfc onapVnfc = new org.onap.vnfmdriver.model.AffectedVnfc();
            onapVnfc.setChangeType(getChangeType(affectedVnfc.getChangeType()));
            onapVnfc.setVduId(affectedVnfc.getVduId());
            onapVnfc.setVmid(affectedVnfc.getComputeResource().getResourceId());
            onapVnfc.setVmname(extractServerName(affectedVnfc.getComputeResource().getAdditionalData()));
            onapVnfc.setVnfcInstanceId(vnfId + SEPARATOR + affectedVnfc.getId());
            onapVnfc.setVimid(vimId);
            notificationToSend.getAffectedVnfc().add(onapVnfc);
        }
    }

    private void addAffectedVirtualLinks(VnfLifecycleChangeNotification request, VNFLCMNotification notification) {
        for (AffectedVirtualLink affectedVirtualLink : request.getAffectedVirtualLinks()) {
            org.onap.vnfmdriver.model.AffectedVirtualLink onapVirtualLink = new org.onap.vnfmdriver.model.AffectedVirtualLink();
            onapVirtualLink.setVlInstanceId(request.getVnfInstanceId() + SEPARATOR + affectedVirtualLink.getId());
            onapVirtualLink.setChangeType(getChangeType(affectedVirtualLink.getChangeType()));
            onapVirtualLink.setVldid(affectedVirtualLink.getVirtualLinkDescId());
            AffectedVirtualLinkNetworkResource networkResource = new AffectedVirtualLinkNetworkResource();
            onapVirtualLink.setNetworkResource(networkResource);
            networkResource.setResourceId(affectedVirtualLink.getResource().getResourceId());
            networkResource.setResourceType(AffectedVirtualLinkType.NETWORK);
            notification.getAffectedVl().add(onapVirtualLink);
        }
    }

    private org.onap.vnfmdriver.model.OperationType getOperation(String vnfmId, String vnfId, OperationExecution operationExecution, com.nokia.cbam.lcm.v32.model.OperationType type, List<AffectedVnfc> affectedVnfcs) {
        switch (type) {
            case TERMINATE:
                return org.onap.vnfmdriver.model.OperationType.TERMINAL;
            case INSTANTIATE:
                return org.onap.vnfmdriver.model.OperationType.INSTANTIATE;
            case SCALE:
                ScaleVnfRequest originalRequest = new Gson().fromJson(new Gson().toJson(operationExecution.getOperationParams()), ScaleVnfRequest.class);
                switch (originalRequest.getType()) {
                    case IN:
                        return org.onap.vnfmdriver.model.OperationType.SCALEIN;
                    default: //OUT
                        return org.onap.vnfmdriver.model.OperationType.SCALEOUT;
                }
            default:
                return org.onap.vnfmdriver.model.OperationType.HEAL;
        }
    }

    private String extractServerName(Object additionalData) {
        return new Gson().toJsonTree(additionalData).getAsJsonObject().get("name").getAsString();
    }

    private org.onap.vnfmdriver.model.VnfNotificationType getChangeType(ChangeType changeType) {
        switch (changeType) {
            case ADDED:
                return org.onap.vnfmdriver.model.VnfNotificationType.ADDED;
            case REMOVED:
                return org.onap.vnfmdriver.model.VnfNotificationType.REMOVED;
            default: //case MODIFIED:
                return org.onap.vnfmdriver.model.VnfNotificationType.MODIFIED;
        }
    }

    @VisibleForTesting
    static class ProcessedNotification {
        private String operationExecutionId;
        private OperationStatus status;

        ProcessedNotification(String operationExecutionId, OperationStatus status) {
            this.operationExecutionId = operationExecutionId;
            this.status = status;
        }

        public String getOperationExecutionId() {
            return operationExecutionId;
        }

        public void setOperationExecutionId(String operationExecutionId) {
            this.operationExecutionId = operationExecutionId;
        }

        public OperationStatus getStatus() {
            return status;
        }

        public void setStatus(OperationStatus status) {
            this.status = status;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProcessedNotification that = (ProcessedNotification) o;
            return Objects.equals(operationExecutionId, that.operationExecutionId) &&
                    status == that.status;
        }

        @Override
        public int hashCode() {
            return Objects.hash(operationExecutionId, status);
        }

        @Override
        public String toString() {
            return "ProcessedNotification{" +
                    "operationExecutionId=" + operationExecutionId + '"' +
                    ", status=" + status +
                    '}';
        }
    }

    @VisibleForTesting
    static class ReportedAffectedConnectionPoints {
        @SerializedName("cbam_pre")
        Set<ReportedAffectedCp> pre = new HashSet<>();
        @SerializedName("cbam_post")
        Set<ReportedAffectedCp> post = new HashSet<>();
    }

    /**
     * Represent the information created from JS during every operation
     */
    @VisibleForTesting
    static class ReportedAffectedCp {
        private String providerId;
        private String cpdId;
        private String ecpdId;
        private String cpId; //the location of the resource in the Heat stack
        private String tenantId;
        private String ipAddress;
        private String macAddress;
        private String serverProviderId;
        private String name;
        private String networkProviderId;
        private ChangeType changeType;

        public String getProviderId() {
            return providerId;
        }

        public void setProviderId(String providerId) {
            this.providerId = providerId;
        }

        public String getCpdId() {
            return cpdId;
        }

        public void setCpdId(String cpdId) {
            this.cpdId = cpdId;
        }

        public String getEcpdId() {
            return ecpdId;
        }

        public void setEcpdId(String ecpdId) {
            this.ecpdId = ecpdId;
        }

        public String getCpId() {
            return cpId;
        }

        public void setCpId(String cpId) {
            this.cpId = cpId;
        }

        public String getTenantId() {
            return tenantId;
        }

        public void setTenantId(String tenantId) {
            this.tenantId = tenantId;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public String getMacAddress() {
            return macAddress;
        }

        public void setMacAddress(String macAddress) {
            this.macAddress = macAddress;
        }

        public String getServerProviderId() {
            return serverProviderId;
        }

        public void setServerProviderId(String serverProviderId) {
            this.serverProviderId = serverProviderId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNetworkProviderId() {
            return networkProviderId;
        }

        public void setNetworkProviderId(String networkProviderId) {
            this.networkProviderId = networkProviderId;
        }

        public ChangeType getChangeType() {
            return changeType;
        }

        public void setChangeType(ChangeType changeType) {
            this.changeType = changeType;
        }

        @Override
        public String toString() {
            return "ReportedAffectedCp{" +
                    "providerId='" + providerId + '\'' +
                    ", cpdId='" + cpdId + '\'' +
                    ", ecpdId='" + ecpdId + '\'' +
                    ", cpId='" + cpId + '\'' +
                    ", tenantId='" + tenantId + '\'' +
                    ", ipAddress='" + ipAddress + '\'' +
                    ", macAddress='" + macAddress + '\'' +
                    ", serverProviderId='" + serverProviderId + '\'' +
                    ", name='" + name + '\'' +
                    ", networkProviderId='" + networkProviderId + '\'' +
                    ", changeType=" + changeType +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ReportedAffectedCp that = (ReportedAffectedCp) o;
            return Objects.equals(providerId, that.providerId) &&
                    Objects.equals(cpdId, that.cpdId) &&
                    Objects.equals(ecpdId, that.ecpdId) &&
                    Objects.equals(cpId, that.cpId) &&
                    Objects.equals(tenantId, that.tenantId) &&
                    Objects.equals(ipAddress, that.ipAddress) &&
                    Objects.equals(macAddress, that.macAddress) &&
                    Objects.equals(serverProviderId, that.serverProviderId) &&
                    Objects.equals(name, that.name) &&
                    Objects.equals(networkProviderId, that.networkProviderId) &&
                    changeType == that.changeType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(providerId, cpdId, ecpdId, cpId, tenantId, ipAddress, macAddress, serverProviderId, name, networkProviderId, changeType);
        }
    }
}
