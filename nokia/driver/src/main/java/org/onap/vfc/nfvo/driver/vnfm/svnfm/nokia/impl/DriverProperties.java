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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Wraps the properties supplied to the servlet
 */
@Component
public class DriverProperties {
    public static final String BASE_SUFFIX = "/" + SelfRegistrationManager.SERVICE_NAME + "/v1";
    public static final String BASE_URL = "/api" + BASE_SUFFIX;
    public static final String LCN_PATH = "/lcn";

    @Value("${server.address}")
    private String driverIp;
    @Value("${server.context-path}")
    private String driverPath;

    @Value("${cbamCatalogUrl}")
    private String cbamCatalogUrl;
    @Value("${cbamLcnUrl}")
    private String cbamLcnUrl;

    @Value("${vnfmId}")
    private String vnfmId;

    /**
     * @return the IP address of the server on which the server listens
     */
    public String getDriverIp() {
        return driverIp;
    }

    /**
     * @param driverIp the IP address of the server on which the server listens
     */
    public void setDriverIp(String driverIp) {
        this.driverIp = driverIp;
    }

    /**
     * @return the REST base path (can be an empty string)
     */
    public String getDriverPath() {
        return driverPath;
    }

    /**
     * @param driverPath the REST base path (can be an empty string)
     */
    public void setDriverPath(String driverPath) {
        this.driverPath = driverPath;
    }

    /**
     * @return the URL on which the CBAM catalog API can be accessed (ex. https://1.2.3.4:443/api/catalog/adapter )
     */
    public String getCbamCatalogUrl() {
        return cbamCatalogUrl;
    }

    /**
     * @param cbamCatalogUrl the URL on which the CBAM catalog API can be accessed (ex. https://1.2.3.4:443/api/catalog/adapter )
     */
    public void setCbamCatalogUrl(String cbamCatalogUrl) {
        this.cbamCatalogUrl = cbamCatalogUrl;
    }

    /**
     * @return the URL on which the CBAM LCN subscription API can be accessed (ex. https://1.2.3.4:443/vnfm/lcn/v3 )
     */
    public String getCbamLcnUrl() {
        return cbamLcnUrl;
    }

    /**
     * @param cbamLcnUrl the URL on which the CBAM LCN subscription API can be accessed (ex. https://1.2.3.4:443/vnfm/lcn/v3 )
     */
    public void setCbamLcnUrl(String cbamLcnUrl) {
        this.cbamLcnUrl = cbamLcnUrl;
    }

    /**
     * @return the identifier of the VNFM
     */
    public String getVnfmId() {
        return vnfmId;
    }

    /**
     * @param vnfmId the identifier of the VNFM
     */
    public void setVnfmId(String vnfmId) {
        this.vnfmId = vnfmId;
    }

    @Override
    public String toString() {
        return "DriverProperties{" +
                "driverIp='" + driverIp + '\'' +
                ", driverPath='" + driverPath + '\'' +
                ", cbamCatalogUrl='" + cbamCatalogUrl + '\'' +
                ", cbamLcnUrl='" + cbamLcnUrl + '\'' +
                ", vnfmId='" + vnfmId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DriverProperties that = (DriverProperties) o;
        return Objects.equals(driverIp, that.driverIp) &&
                Objects.equals(driverPath, that.driverPath) &&
                Objects.equals(cbamCatalogUrl, that.cbamCatalogUrl) &&
                Objects.equals(cbamLcnUrl, that.cbamLcnUrl) &&
                Objects.equals(vnfmId, that.vnfmId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(driverIp, driverPath, cbamCatalogUrl, cbamLcnUrl, vnfmId);
    }
}
