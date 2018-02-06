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

import com.google.common.collect.Ordering;
import com.nokia.cbam.lcm.v32.model.OperationExecution;
import com.nokia.cbam.lcm.v32.model.VnfLifecycleChangeNotification;

import javax.annotation.Nullable;

public interface ILifecycleChangeNotificationManager {
    String EXTERNAL_VNFM_ID = "externalVnfmId";
    String SEPARATOR = "_";
    /**
     * Order the operations by start time (latest first)
     */
    Ordering<OperationExecution> NEWEST_OPERATIONS_FIRST = new Ordering<OperationExecution>() {
        @Override
        public int compare(@Nullable OperationExecution left, @Nullable OperationExecution right) {
            return right.getStartTime().toLocalDate().compareTo(left.getStartTime().toLocalDate());
        }
    };

    /**
     * Transform a CBAM LCN into ONAP LCN
     *
     * @param recievedNotification the CBAM LCN
     */
    void handleLcn(VnfLifecycleChangeNotification recievedNotification);

    /**
     * Wait for the termination finish notification to be processed
     *
     * @param operationExecutionId the identifier of the termination operation
     */
    void waitForTerminationToBeProcessed(String operationExecutionId);
}
