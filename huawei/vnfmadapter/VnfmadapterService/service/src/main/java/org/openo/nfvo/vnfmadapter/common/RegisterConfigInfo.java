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

package org.openo.nfvo.vnfmadapter.common;

import java.util.Locale;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide function for reading register parameter.
 * <br/>
 * <p>
 * </p>
 *
 * @author
 * @version     NFVO 0.5  Aug 25, 2016
 */
public class RegisterConfigInfo {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterConfigInfo.class);

    private String serviceName;

    private String version;

    private String url;

    private String protocol;

    private String ip;

    private String port;

    private String ttl;

    private static RegisterConfigInfo regConfig = new RegisterConfigInfo();

    private RegisterConfigInfo() {
        ResourceBundle rb = ResourceBundle.getBundle("registerService", Locale.getDefault());
        serviceName = rb.getString("serviceName");
        version = rb.getString("version");
        url = rb.getString("url");
        protocol = rb.getString("protocol");
        ip = rb.getString("ip");
        port = rb.getString("port");
        ttl = rb.getString("ttl");
    }

    public static RegisterConfigInfo getInstance() {
        return regConfig;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getVersion() {
        return version;
    }

    public String getUrl() {
        return url;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getIp() {
        return ip;
    }

    public String getPort() {
        return port;
    }

    public String getTtl() {
        return ttl;
    }
}
