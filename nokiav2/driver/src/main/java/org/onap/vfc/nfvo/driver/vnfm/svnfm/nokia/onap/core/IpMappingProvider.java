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

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.google.common.base.Splitter.on;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Responsible for remapping IP/DNS names in URLs based on property file
 */
@Component
public class IpMappingProvider implements InitializingBean {
    public static final String IP_MAP = "ipMap";
    private final Environment environment;
    private final Map<String, String> ipMap = new HashMap<>();

    @Autowired
    IpMappingProvider(Environment environment) {
        this.environment = environment;
    }

    /**
     * After the Bean has been initialized the IP mapping and the VMFM cache is initialized
     * It is done in this phase because it requires the environment to be initialized
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        on(",").trimResults().omitEmptyStrings().split(environment.getProperty(IP_MAP, String.class, "")).forEach(new Consumer<String>() {
            @Override
            public void accept(String item) {
                ArrayList<String> ip = newArrayList(on("->").trimResults().split(item));
                ipMap.put(ip.get(0), ip.get(1));
            }
        });
    }

    /**
     * Map IP addresses based on configuration parameter ipMap
     *
     * @param ip the original IP address
     * @return the mapped IP address
     */
    public String mapPrivateIpToPublicIp(String ip) {
        return ipMap.getOrDefault(ip, ip);
    }
}
