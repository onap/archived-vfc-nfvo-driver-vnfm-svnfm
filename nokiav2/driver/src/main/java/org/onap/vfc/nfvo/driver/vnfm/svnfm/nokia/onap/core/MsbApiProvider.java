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

import org.onap.msb.sdk.discovery.common.RouteException;
import org.onap.msb.sdk.discovery.entity.MicroServiceFullInfo;
import org.onap.msb.sdk.discovery.entity.NodeInfo;
import org.onap.msb.sdk.httpclient.msb.MSBServiceClient;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import static java.lang.Integer.valueOf;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.fatalFailure;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Responsible for providing REST client to access MSB API
 */
@Component
public class MsbApiProvider extends IpMappingProvider {
    private static Logger logger = getLogger(MsbApiProvider.class);
    @Value("${messageBusIp}")
    private String messageBusIp;
    @Value("${messageBusPort}")
    private String messageBusPort;

    @Autowired
    MsbApiProvider(Environment environment) {
        super(environment);
    }

    /**
     * @return API to access ONAP MSB
     */
    public MSBServiceClient getMsbClient() {
        return new MSBServiceClient(messageBusIp, valueOf(messageBusPort));
    }

    /**
     * @param name    the name of the micro service
     * @param version the version of the micro service
     * @return the base URL of the micro service (ex. https://1.2.3.4/path )
     */
    public String getMicroServiceUrl(String name, String version) {
        MicroServiceFullInfo microServiceFullInfo = getMicroServiceInfo(name, version);
        String ipAnPort = getNodeIpAnPort(microServiceFullInfo);
        //FIXME the enable_ssl field should be used, but it is not available in SDK depends on MSB-151 jira issue
        String protocol = (ipAnPort.endsWith(":8443") || ipAnPort.endsWith(":443")) ? "https://" : "http://";
        //the field name in A&AI is misleading the URL is relative path postfixed to http(s)://ip:port
        return protocol + ipAnPort + microServiceFullInfo.getUrl();
    }

    private MicroServiceFullInfo getMicroServiceInfo(String name, String version) {
        try {
            return getMsbClient().queryMicroServiceInfo(name, version);
        } catch (RouteException e) {
            throw fatalFailure(logger, "Unable to get micro service URL for " + name + " with version " + version, e);
        }
    }

    private String getNodeIpAnPort(MicroServiceFullInfo microServiceFullInfo) {
        for (NodeInfo nodeInfo : microServiceFullInfo.getNodes()) {
            if (isADokcerInternalAddress(nodeInfo)) {
                return mapPrivateIpToPublicIp(nodeInfo.getIp()) + ":" + nodeInfo.getPort();
            }
        }
        throw fatalFailure(logger, "The " + microServiceFullInfo.getServiceName() + " service with " + microServiceFullInfo.getVersion() + " does not have any valid nodes" + microServiceFullInfo.getNodes());
    }

    private boolean isADokcerInternalAddress(NodeInfo nodeInfo) {
        return !nodeInfo.getIp().startsWith("172.");
    }
}
