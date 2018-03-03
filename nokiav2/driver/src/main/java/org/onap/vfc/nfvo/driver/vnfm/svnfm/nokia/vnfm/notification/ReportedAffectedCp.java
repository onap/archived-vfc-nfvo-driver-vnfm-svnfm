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

import com.nokia.cbam.lcm.v32.model.ChangeType;

import java.util.Objects;

/**
 * Represent a single port change in the VNF
 */
public class ReportedAffectedCp {
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

    /**
     * @return the provider id of the port
     */
    public String getProviderId() {
        return providerId;
    }

    /**
     * @param providerId the provider id of the port
     */
    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    /**
     * @return the identifier of the connection point of the port (may be null)
     */
    public String getCpdId() {
        return cpdId;
    }

    /**
     * @param cpdId the identifier of the connection point of the port (may be null)
     */
    public void setCpdId(String cpdId) {
        this.cpdId = cpdId;
    }

    /**
     * @return the identifier of the external connection point of the port (may be null)
     */
    public String getEcpdId() {
        return ecpdId;
    }

    /**
     * @param ecpdId the identifier of the connection point of the port (may be null)
     */
    public void setEcpdId(String ecpdId) {
        this.ecpdId = ecpdId;
    }

    /**
     * @return the identifier of the connection point instance of the port
     */
    public String getCpId() {
        return cpId;
    }

    /**
     * @param cpId the identifier of the connection point instance of the port
     */
    public void setCpId(String cpId) {
        this.cpId = cpId;
    }

    /**
     * @return the identifier of the tenant owning the port
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * @param tenantId the identifier of the tenant owning the port
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * @return the IP address of the port (may be null)
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * @param ipAddress the IP address of the port (may be null)
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * @return the MAC address of the port
     */
    public String getMacAddress() {
        return macAddress;
    }

    /**
     * @param macAddress the MAC address of the port
     */
    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    /**
     * @return the provider id of the server to which the port is attached to (may be null)
     */
    public String getServerProviderId() {
        return serverProviderId;
    }

    /**
     * @param serverProviderId the provider id of the server to which the port is attached to (may be null)
     */
    public void setServerProviderId(String serverProviderId) {
        this.serverProviderId = serverProviderId;
    }

    /**
     * @return the name of the port  (may be null)
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name of the port
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the provider id of the network of the port
     */
    public String getNetworkProviderId() {
        return networkProviderId;
    }

    /**
     * @param networkProviderId the provider id of the network of the port
     */
    public void setNetworkProviderId(String networkProviderId) {
        this.networkProviderId = networkProviderId;
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
                Objects.equals(networkProviderId, that.networkProviderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(providerId, cpdId, ecpdId, cpId, tenantId, ipAddress, macAddress, serverProviderId, name, networkProviderId);
    }
}