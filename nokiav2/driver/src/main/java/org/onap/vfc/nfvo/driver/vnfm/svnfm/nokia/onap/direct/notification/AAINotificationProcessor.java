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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.notification;

import com.nokia.cbam.lcm.v32.model.AffectedVirtualLink;
import com.nokia.cbam.lcm.v32.model.OperationExecution;
import com.nokia.cbam.lcm.v32.model.VnfLifecycleChangeNotification;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api.INotificationSender;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification.ReportedAffectedConnectionPoints;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification.ReportedAffectedCp;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Optional.empty;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.tryFind;
import static com.nokia.cbam.lcm.v32.model.ChangeType.*;
import static com.nokia.cbam.lcm.v32.model.OperationStatus.STARTED;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.notification.LInterfaceManager.buildUrl;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.StringUtils.isEmpty;

/**
 * Responsible for providing information related to the VNFM from VF-C source
 */
@Component
public class AAINotificationProcessor implements INotificationSender {
    private static Logger logger = getLogger(AAINotificationProcessor.class);
    private final GenericVnfManager genericVnfManager;
    private final L3NetworkManager l3NetworkManager;
    private final LInterfaceManager lInterfaceManager;
    private final VnfcManager vnfcManager;
    private final VserverManager vserverManager;

    @Autowired
    AAINotificationProcessor(GenericVnfManager genericVnfManager, L3NetworkManager l3NetworkManager, LInterfaceManager lInterfaceManager, VnfcManager vnfcManager, VserverManager vserverManager) {
        this.genericVnfManager = genericVnfManager;
        this.l3NetworkManager = l3NetworkManager;
        this.lInterfaceManager = lInterfaceManager;
        this.vnfcManager = vnfcManager;
        this.vserverManager = vserverManager;
    }

    @Override
    public void processNotification(VnfLifecycleChangeNotification receivedNotification, OperationExecution operationExecution, Optional<ReportedAffectedConnectionPoints> affectedConnectionPoints, String vimId, String vnfmId) {
        boolean inMaintenance = STARTED.equals(receivedNotification.getStatus());
        genericVnfManager.createOrUpdate(receivedNotification.getVnfInstanceId(), inMaintenance, vnfmId, empty());
        addOrUpdateVls(receivedNotification, vimId);
        addOrUpdateVnfcs(receivedNotification, vimId, inMaintenance);
        processCps(receivedNotification, affectedConnectionPoints, vimId, inMaintenance);
        removeVnfcs(receivedNotification, vimId);
        removeVls(receivedNotification);
        logger.info("Notification processed successfully");
    }

    private void removeVls(VnfLifecycleChangeNotification receivedNotification) {
        for (AffectedVirtualLink removedVl : filter(receivedNotification.getAffectedVirtualLinks(), affectedVirtualLink -> affectedVirtualLink.getChangeType().equals(REMOVED))) {
            l3NetworkManager.delete(receivedNotification.getVnfInstanceId(), removedVl);
        }
    }

    private void removeVnfcs(VnfLifecycleChangeNotification receivedNotification, String vimId) {
        for (com.nokia.cbam.lcm.v32.model.AffectedVnfc removedVnfc : filter(receivedNotification.getAffectedVnfcs(), vnfc -> REMOVED.equals(vnfc.getChangeType()))) {
            vnfcManager.delete(receivedNotification.getVnfInstanceId(), removedVnfc);
            vserverManager.delete(vimId, removedVnfc);
        }
    }

    private void processCps(VnfLifecycleChangeNotification receivedNotification, Optional<ReportedAffectedConnectionPoints> affectedConnectionPoints, String vimId, boolean inMaintenance) {
        if (affectedConnectionPoints.isPresent()) {
            for (ReportedAffectedCp removedCp : collectCpsToBeDeleted(vimId, affectedConnectionPoints.get())) {
                lInterfaceManager.delete(vimId, removedCp);
            }
            //these can only be added or modified because if something is in the post CPS it can not be removed
            //since it is present after the operation
            for (ReportedAffectedCp affectedCp : affectedConnectionPoints.get().getPost()) {
                if (!isEmpty(affectedCp.getServerProviderId())) {
                    lInterfaceManager.update(receivedNotification.getVnfInstanceId(), vimId, affectedCp, inMaintenance);
                } else {
                    logger.warn("The changed {} connection point is not linked to any server", affectedCp.getCpId());
                }
            }
        } else {
            logger.warn("The changed connection points are not present in VNF with {} identifier", receivedNotification.getVnfInstanceId());
        }
    }

    private void addOrUpdateVnfcs(VnfLifecycleChangeNotification receivedNotification, String vimId, boolean inMaintenance) {
        for (com.nokia.cbam.lcm.v32.model.AffectedVnfc affectedVnfc : receivedNotification.getAffectedVnfcs()) {
            if (affectedVnfc.getChangeType() == MODIFIED || affectedVnfc.getChangeType() == ADDED) {
                vserverManager.update(vimId, receivedNotification.getVnfInstanceId(), affectedVnfc, receivedNotification.getAffectedVirtualStorages(), inMaintenance);
                vnfcManager.update(vimId, VserverManager.getTenantId(affectedVnfc), receivedNotification.getVnfInstanceId(), affectedVnfc, inMaintenance);
            }
        }
    }

    private void addOrUpdateVls(VnfLifecycleChangeNotification receivedNotification, String vimId) {
        for (AffectedVirtualLink affectedVirtualLink : receivedNotification.getAffectedVirtualLinks()) {
            if ((affectedVirtualLink.getChangeType() == MODIFIED) || (affectedVirtualLink.getChangeType() == ADDED)) {
                l3NetworkManager.update(vimId, receivedNotification.getVnfInstanceId(), affectedVirtualLink);
            }
        }
    }

    /**
     * The ports that are present in the pre, but not present in the post are
     * removed regardless of the "removed" flag being present in the pre, because
     * that only signals the remove intention, but does not actually mean that
     * the resource have been removed
     */
    private Collection<ReportedAffectedCp> collectCpsToBeDeleted(String vimId, ReportedAffectedConnectionPoints cps) {
        Set<ReportedAffectedCp> cpsToRemove = new HashSet<>();
        for (ReportedAffectedCp cpBeforeOperation : cps.getPre()) {
            if (!isEmpty(cpBeforeOperation.getServerProviderId())) {
                String originalResource = buildUrl(vimId, cpBeforeOperation);
                if (!tryFind(cps.getPost(), cpAfterOperation -> originalResource.equals(buildUrl(vimId, cpAfterOperation))).isPresent()) {
                    cpsToRemove.add(cpBeforeOperation);
                }
            }
        }
        return cpsToRemove;
    }
}
