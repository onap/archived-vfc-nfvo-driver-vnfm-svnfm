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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api;

import com.nokia.cbam.lcm.v32.model.OperationExecution;
import com.nokia.cbam.lcm.v32.model.VnfLifecycleChangeNotification;
import java.util.Optional;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification.ReportedAffectedConnectionPoints;

/**
 * Responsible for processing the preprocessed notification from CBAM and making the changes
 * based on the notification in various ONAP sub systems.
 */
@FunctionalInterface
public interface INotificationSender {
    /**
     * Execute changes in the ONAP subsystem based on the received notification
     *
     * @param receivedNotification     the notification from CBAM
     * @param operationExecution       the executed operation that triggered the LCN
     * @param affectedConnectionPoints the affected connection points during the operation
     * @param vimId                    the identifier of the VIM in ONAP
     * @param vnfmId                   the identifier of the VNFM
     */
    void processNotification(VnfLifecycleChangeNotification receivedNotification, OperationExecution operationExecution, Optional<ReportedAffectedConnectionPoints> affectedConnectionPoints, String vimId, String vnfmId);
}
