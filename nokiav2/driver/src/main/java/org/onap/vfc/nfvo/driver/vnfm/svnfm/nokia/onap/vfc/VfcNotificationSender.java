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

import com.google.gson.Gson;
import com.nokia.cbam.lcm.v32.model.OperationExecution;
import com.nokia.cbam.lcm.v32.model.ScaleVnfRequest;
import com.nokia.cbam.lcm.v32.model.VnfLifecycleChangeNotification;
import java.util.ArrayList;
import java.util.Optional;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api.INotificationSender;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification.LifecycleChangeNotificationManager;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification.ReportedAffectedConnectionPoints;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification.ReportedAffectedCp;
import org.onap.vnfmdriver.model.*;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Optional.of;

import static com.google.common.collect.Iterables.tryFind;
import static com.nokia.cbam.lcm.v32.model.ScaleDirection.IN;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.SEPARATOR;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.buildFatalFailure;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.JobManager.extractOnapJobId;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Responsible for sending notifications to VF-C
 */
@Component
public class VfcNotificationSender implements INotificationSender {
    private static Logger logger = getLogger(VfcNotificationSender.class);
    private final VfcRestApiProvider vfcRestApiProvider;

    @Autowired
    VfcNotificationSender(VfcRestApiProvider vfcRestApiProvider) {
        this.vfcRestApiProvider = vfcRestApiProvider;
    }

    @Override
    public void processNotification(VnfLifecycleChangeNotification recievedNotification, OperationExecution operationExecution, Optional<ReportedAffectedConnectionPoints> affectedCps, String vimId, String vnfmId) {
        VNFLCMNotification notificationToSend = buildNotification();
        notificationToSend.setJobId(extractOnapJobId(operationExecution.getOperationParams()));
        notificationToSend.setOperation(getOperation(operationExecution, recievedNotification.getOperation()));
        notificationToSend.setVnfInstanceId(recievedNotification.getVnfInstanceId());
        if (LifecycleChangeNotificationManager.isTerminal(recievedNotification.getStatus())) {
            notificationToSend.setStatus(VnfLcmNotificationStatus.RESULT);
            addAffectedVirtualLinks(recievedNotification, notificationToSend);
            addAffectedVnfcs(vimId, recievedNotification.getVnfInstanceId(), notificationToSend, recievedNotification);
            addAffectedCps(vimId, notificationToSend, affectedCps);
        } else {
            notificationToSend.setStatus(VnfLcmNotificationStatus.START);
        }
        sendNotification(vnfmId, notificationToSend);
    }

    private VNFLCMNotification buildNotification() {
        VNFLCMNotification notificationToSend = new VNFLCMNotification();
        notificationToSend.setAffectedVirtualStorage(new ArrayList<>());
        notificationToSend.setAffectedVnfc(new ArrayList<>());
        notificationToSend.setAffectedVl(new ArrayList<>());
        notificationToSend.setAffectedCp(new ArrayList<>());
        return notificationToSend;
    }

    private void sendNotification(String vnfmId, VNFLCMNotification notification) {
        try {
            if (logger.isInfoEnabled()) {
                logger.info("Sending LCN: {}", new Gson().toJson(notification));
            }
            vfcRestApiProvider.getNsLcmApi().vNFLCMNotification(vnfmId, notification.getVnfInstanceId(), notification).blockingFirst(null);
        } catch (Exception e) {
            throw buildFatalFailure(logger, "Unable to send LCN to VF-C", e);
        }
    }

    private AffectedCp buildAffectedCp(String vimId, String vnfId, VnfCpNotificationType changeType, ReportedAffectedCp affectedCp) {
        AffectedCp onapAffectedCp = new AffectedCp();
        AffectedCpPortResource port = new AffectedCpPortResource();
        port.setInstId(affectedCp.getServerProviderId());
        port.setIpAddress(affectedCp.getIpAddress());
        port.setMacAddress(affectedCp.getMacAddress());
        port.setResourceid(affectedCp.getProviderId());
        port.setResourceName(affectedCp.getName());
        port.setTenant(affectedCp.getTenantId());
        port.setVimid(vimId);
        onapAffectedCp.setPortResource(port);
        onapAffectedCp.setCpdid(affectedCp.getCpId());
        onapAffectedCp.setCpinstanceid(vnfId + SEPARATOR + affectedCp.getCpId());
        onapAffectedCp.setVirtualLinkInstanceId(affectedCp.getNetworkProviderId());
        onapAffectedCp.setChangeType(changeType);
        //owner id & type can be left empty it will default to VNF id on VF-C
        return onapAffectedCp;
    }


    private void addAffectedVnfcs(String vimId, String vnfId, VNFLCMNotification notificationToSend, VnfLifecycleChangeNotification request) {
        if (request.getAffectedVnfcs() != null) {
            for (com.nokia.cbam.lcm.v32.model.AffectedVnfc affectedVnfc : request.getAffectedVnfcs()) {
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
    }

    private void addAffectedVirtualLinks(VnfLifecycleChangeNotification request, VNFLCMNotification notification) {
        if (request.getAffectedVirtualLinks() != null) {
            for (com.nokia.cbam.lcm.v32.model.AffectedVirtualLink affectedVirtualLink : request.getAffectedVirtualLinks()) {
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
    }

    private Optional<VnfCpNotificationType> getChangeType(ReportedAffectedConnectionPoints affectedCps, ReportedAffectedCp affectedCp) {
        com.google.common.base.Optional<ReportedAffectedCp> cpBeforeOperation = tryFind(affectedCps.getPre(), pre -> affectedCp.getCpId().equals(pre.getCpId()));
        com.google.common.base.Optional<ReportedAffectedCp> cpAfterOperation = tryFind(affectedCps.getPost(), post -> affectedCp.getCpId().equals(post.getCpId()));
        if (cpBeforeOperation.isPresent() && cpAfterOperation.isPresent()) {
            return cpAfterOperation.get().equals(cpBeforeOperation.get()) ? Optional.empty() : of(VnfCpNotificationType.CHANGED);
        } else {
            //the affected CP must be present in the pre or post
            return of((cpAfterOperation.isPresent() ? VnfCpNotificationType.ADDED : VnfCpNotificationType.REMOVED));
        }
    }

    private void addAffectedCps(String vimId, VNFLCMNotification notificationToSend, Optional<ReportedAffectedConnectionPoints> affectedCps) {
        if (affectedCps.isPresent()) {
            for (ReportedAffectedCp pre : affectedCps.get().getPre()) {
                Optional<VnfCpNotificationType> changeType = getChangeType(affectedCps.get(), pre);
                if (of(VnfCpNotificationType.REMOVED).equals(changeType)) {
                    addModifiedCp(vimId, notificationToSend, pre, changeType);
                }
            }
            for (ReportedAffectedCp post : affectedCps.get().getPost()) {
                Optional<VnfCpNotificationType> changeType = getChangeType(affectedCps.get(), post);
                if (of(VnfCpNotificationType.ADDED).equals(changeType)) {
                    addModifiedCp(vimId, notificationToSend, post, changeType);
                }
                if (of(VnfCpNotificationType.CHANGED).equals(changeType)) {
                    addModifiedCp(vimId, notificationToSend, post, changeType);
                }
            }
        }
    }

    private void addModifiedCp(String vimId, VNFLCMNotification notificationToSend, ReportedAffectedCp post, Optional<VnfCpNotificationType> changeType) {
        if (post.getCpdId() != null) {
            AffectedCp onapAffectedCp = buildAffectedCp(vimId, notificationToSend.getVnfInstanceId(), changeType.get(), post);
            onapAffectedCp.setCpdid(post.getCpdId());
            notificationToSend.getAffectedCp().add(onapAffectedCp);
        }
        if (post.getEcpdId() != null) {
            AffectedCp onapAffectedCp = buildAffectedCp(vimId, notificationToSend.getVnfInstanceId(), changeType.get(), post);
            onapAffectedCp.setCpdid(post.getEcpdId());
            notificationToSend.getAffectedCp().add(onapAffectedCp);
        }
    }

    private org.onap.vnfmdriver.model.OperationType getOperation(OperationExecution operationExecution, com.nokia.cbam.lcm.v32.model.OperationType type) {
        if (type == com.nokia.cbam.lcm.v32.model.OperationType.TERMINATE) {
            return OperationType.TERMINAL;
        } else if (type == com.nokia.cbam.lcm.v32.model.OperationType.INSTANTIATE) {
            return OperationType.INSTANTIATE;
        } else if (type == com.nokia.cbam.lcm.v32.model.OperationType.SCALE) {
            if (IN == new Gson().fromJson(new Gson().toJson(operationExecution.getOperationParams()), ScaleVnfRequest.class).getType()) {
                return OperationType.SCALEIN;
            } else {
                return OperationType.SCALEOUT;
            }
        } else {
            return OperationType.HEAL;
        }
    }

    private String extractServerName(Object additionalData) {
        return new Gson().toJsonTree(additionalData).getAsJsonObject().get("name").getAsString();
    }

    private org.onap.vnfmdriver.model.VnfNotificationType getChangeType(com.nokia.cbam.lcm.v32.model.ChangeType changeType) {
        if (changeType == com.nokia.cbam.lcm.v32.model.ChangeType.ADDED) {
            return VnfNotificationType.ADDED;
        } else if (changeType == com.nokia.cbam.lcm.v32.model.ChangeType.REMOVED) {
            return VnfNotificationType.REMOVED;
        } else {
            return VnfNotificationType.MODIFIED;
        }
    }

}
