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

import static com.google.common.hash.Hashing.sha512;

/**
 * Describes the VNFM credentials
 */
public class VnfmCredentials {
    private final String username;
    private final String password;
    private final String clientId;
    private final String clientSecret;

    /**
     * @param username     the username for the VNFM
     * @param password     the password for the VNFM
     * @param clientId     the client identifier
     * @param clientSecret the client secret
     */
    VnfmCredentials(String username, String password, String clientId, String clientSecret) {
        this.username = username;
        this.password = password;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    /**
     * @return the username for the VNFM
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return the password for the VNFM
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return the client identifier
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * @return the client secret
     */
    public String getClientSecret() {
        return clientSecret;
    }

    @Override
    @SuppressWarnings("squid:S2068") //the password is hashed
    public String toString() {
        return "VnfmCredentials{" +
                "username='" + username + '\'' +
                ", password='" + sha512().hashBytes(password.getBytes()).toString() + '\'' +
                ", clientId='" + clientId + '\'' +
                ", clientSecret='" + sha512().hashBytes(clientSecret.getBytes()).toString() + '\'' +
                '}';
    }
}
