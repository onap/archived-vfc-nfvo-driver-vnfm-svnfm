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
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api.INotificationSender;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.spring.Conditions;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.DriverProperties;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification.ReportedAffectedConnectionPoints;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification.ReportedAffectedCp;
import org.onap.vnfmdriver.model.*;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.fatalFailure;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.ILifecycleChangeNotificationManager.SEPARATOR;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.JobManager.extractOnapJobId;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Responsible for sending notifications to VF-C
 */
@Component
@Conditional(value = Conditions.UseForVfc.class)
public class VfcNotificationSender implements INotificationSender {
    private static Logger logger = getLogger(VfcNotificationSender.class);
    private final DriverProperties driverProperties;
    private final VfcRestApiProvider vfcRestApiProvider;

    @Autowired
    VfcNotificationSender(DriverProperties driverProperties, VfcRestApiProvider vfcRestApiProvider) {
        this.driverProperties = driverProperties;
        this.vfcRestApiProvider = vfcRestApiProvider;
    }

    @Override
    public void processNotification(VnfLifecycleChangeNotification recievedNotification, OperationExecution operationExecution, ReportedAffectedConnectionPoints affectedCps, String vimId) {
        VNFLCMNotification notificationToSend = new VNFLCMNotification();
        notificationToSend.setJobId(extractOnapJobId(operationExecution.getOperationParams()));
        notificationToSend.setOperation(getOperation(driverProperties.getVnfmId(), recievedNotification.getVnfInstanceId(), operationExecution, recievedNotification.getOperation(), recievedNotification.getAffectedVnfcs()));
        notificationToSend.setVnfInstanceId(recievedNotification.getVnfInstanceId());
        switch (recievedNotification.getStatus()) {
            case FINISHED:
            case FAILED:
                notificationToSend.setStatus(VnfLcmNotificationStatus.RESULT);
                addAffectedVirtualLinks(recievedNotification, notificationToSend);
                addAffectedVnfcs(vimId, recievedNotification.getVnfInstanceId(), notificationToSend, recievedNotification);
                addAffectedCps(vimId, notificationToSend, affectedCps);
                break;
            default:
                notificationToSend.setStatus(VnfLcmNotificationStatus.START);
                break;
        }
        sendNotification(notificationToSend);
    }

    private void sendNotification(VNFLCMNotification notification) {
        try {
            logger.info("Sending LCN: " + new Gson().toJson(notification));
            vfcRestApiProvider.getNsLcmApi().vNFLCMNotification(driverProperties.getVnfmId(), notification.getVnfInstanceId(), notification);
        } catch (Exception e) {
            fatalFailure(logger, "Unable to send LCN to VF-C", e);
        }
    }

    private AffectedCp buildAffectedCp(String vimId, String vnfId, ReportedAffectedCp affectedCp) {
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
        onapAffectedCp.setChangeType(transform(affectedCp.getChangeType()));
        //owner id & type can be left empty it will default to VNF id on VF-C
        return onapAffectedCp;
    }

    private VnfCpNotificationType transform(com.nokia.cbam.lcm.v32.model.ChangeType changeType) {
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
        if (request.getAffectedVnfcs() != null) {
            notificationToSend.setAffectedVnfc(new ArrayList<>());
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
            notification.setAffectedVl(new ArrayList<>());
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

    private void addAffectedCps(String vimId, VNFLCMNotification notificationToSend, ReportedAffectedConnectionPoints affectedCps) {
        if (affectedCps != null) {
            notificationToSend.setAffectedCp(new ArrayList<>());
            for (ReportedAffectedCp affectedCp : affectedCps.getPost()) {
                if (affectedCp.getCpdId() != null) {
                    AffectedCp onapAffectedCp = buildAffectedCp(vimId, notificationToSend.getVnfInstanceId(), affectedCp);
                    onapAffectedCp.setCpdid(affectedCp.getCpdId());
                    notificationToSend.getAffectedCp().add(onapAffectedCp);
                }
                if (affectedCp.getEcpdId() != null) {
                    AffectedCp onapAffectedCp = buildAffectedCp(vimId, notificationToSend.getVnfInstanceId(), affectedCp);
                    onapAffectedCp.setCpdid(affectedCp.getEcpdId());
                    notificationToSend.getAffectedCp().add(onapAffectedCp);
                }
            }
        }
    }

    private org.onap.vnfmdriver.model.OperationType getOperation(String vnfmId, String vnfId, OperationExecution operationExecution, com.nokia.cbam.lcm.v32.model.OperationType type, List<com.nokia.cbam.lcm.v32.model.AffectedVnfc> affectedVnfcs) {
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

    private org.onap.vnfmdriver.model.VnfNotificationType getChangeType(com.nokia.cbam.lcm.v32.model.ChangeType changeType) {
        switch (changeType) {
            case ADDED:
                return org.onap.vnfmdriver.model.VnfNotificationType.ADDED;
            case REMOVED:
                return org.onap.vnfmdriver.model.VnfNotificationType.REMOVED;
            default: //case MODIFIED:
                return org.onap.vnfmdriver.model.VnfNotificationType.MODIFIED;
        }
    }

}
