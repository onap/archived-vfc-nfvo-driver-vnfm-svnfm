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

import com.nokia.cbam.lcm.v32.model.OperationStatus;

import java.util.Objects;

/**
 * Represents a notification successfully processed by the {@link LifecycleChangeNotificationManager}
 */
class ProcessedNotification {
    private String operationExecutionId;
    //do not remove field the {@link LifecycleChangeNotificationManager} uses the equals
    // method to compare notifications
    private OperationStatus status;

    ProcessedNotification(String operationExecutionId, OperationStatus status) {
        this.operationExecutionId = operationExecutionId;
        this.status = status;
    }

    /**
     * @return the identifier of the operation
     */
    public String getOperationExecutionId() {
        return operationExecutionId;
    }

    /**
     * @param operationExecutionId the identifier of the operation
     */
    public void setOperationExecutionId(String operationExecutionId) {
        this.operationExecutionId = operationExecutionId;
    }

    /**
     * @return the status of the operation
     */
    public OperationStatus getStatus() {
        return status;
    }

    /**
     * @param status the status of the operation
     */
    public void setStatus(OperationStatus status) {
        this.status = status;
    }

    @Override
    //generated code. This is the recommended way to formulate equals
    @SuppressWarnings({"squid:S00122", "squid:S1067"})
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
