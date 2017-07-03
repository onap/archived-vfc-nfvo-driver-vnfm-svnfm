/*
 * Copyright 2016 Huawei Technologies Co., Ltd.
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
package org.openo.nfvo.vnfmadapter.service.csm.api;

import org.openo.nfvo.vnfmadapter.service.constant.Constant;

/**
 * Connection Information
 * .</br>
 *
 * @author
 * @version     NFVO 0.5  Sep 14, 2016
 */
public class ConnectInfo {

    private String url;

    private String userName;

    private String userPwd;

    private String authenticateMode;

    /**
     *
     * Constructor<br>
     *
     * @param url
     * @since  NFVO 0.5
     */
    public ConnectInfo(String url) {
        this.url = url == null ? "" : url;
        this.authenticateMode = Constant.ANONYMOUS;
    }

    /**
     *
     * Constructor<br>
     *
     * @param url
     * @param userName
     * @param userPwd
     * @param authenticateMode
     * @since  NFVO 0.5
     */
    public ConnectInfo(String url, String userName, String userPwd, String authenticateMode) {
        this.url = url == null ? "" : url;
        this.userName = userName == null ? "" : userName;
        this.userPwd = userPwd == null ? "" : userPwd;
        this.authenticateMode = authenticateMode == null ? "" : authenticateMode;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String vnfUrl) {
        this.url = vnfUrl;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String vnfUserName) {
        this.userName = vnfUserName;
    }

    public String getUserPwd() {
        return this.userPwd;
    }

    public void setUserPwd(String vnfUserPwd) {
        this.userPwd = vnfUserPwd;
    }

    public String getAuthenticateMode() {
        return this.authenticateMode;
    }

    public void setAuthenticateMode(String vnfAuthenticateMode) {
        this.authenticateMode = vnfAuthenticateMode;
    }

    @Override
    public String toString() {
        return "ConnectInfo [AuthenticateMode: " + authenticateMode + ",url=" + url + ", userName=" + userName + ']';
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }
        if(obj == null) {
            return false;
        }

        if(!(obj instanceof ConnectInfo)) {
            return false;
        }

        if(getClass() != obj.getClass()) {
            return false;
        }
        ConnectInfo other = (ConnectInfo)obj;
        if(url == null) {
            if(other.url != null) {
                return false;
            }
        } else if(!url.equals(other.url)) {
            return false;
        }
        return true;
    }
}
