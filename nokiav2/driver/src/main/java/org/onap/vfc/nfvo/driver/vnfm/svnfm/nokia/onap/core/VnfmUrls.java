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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core;

/**
 * Describes the VNFM URLs
 */
public class VnfmUrls {
    private final String lcmUrl;
    private final String lcnUrl;
    private final String authUrl;
    private final String catalogUrl;

    /**
     * @param authUrl    the authentication server URL of the VNFM
     * @param lcmUrl     the LCM URL of the VNFM
     * @param lcnUrl     the LCN URL of the VNFM
     * @param catalogUrl the catalog URL of the VNFM
     */
    VnfmUrls(String authUrl, String lcmUrl, String lcnUrl, String catalogUrl) {
        this.lcmUrl = lcmUrl;
        this.lcnUrl = lcnUrl;
        this.authUrl = authUrl;
        this.catalogUrl = catalogUrl;
    }

    /**
     * @return the LCM URL of the VNFM
     */
    public String getLcmUrl() {
        return lcmUrl;
    }

    /**
     * @return the LCN URL of the VNFM
     */
    public String getLcnUrl() {
        return lcnUrl;
    }

    /**
     * @return the authentication server URL of the VNFM
     */
    public String getAuthUrl() {
        return authUrl;
    }

    /**
     * @return the catalog URL of the VNFM
     */
    public String getCatalogUrl() {
        return catalogUrl;
    }
}
