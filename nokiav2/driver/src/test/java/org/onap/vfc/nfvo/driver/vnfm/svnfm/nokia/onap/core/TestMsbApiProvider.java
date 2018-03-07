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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.msb.sdk.discovery.common.RouteException;
import org.onap.msb.sdk.discovery.entity.MicroServiceFullInfo;
import org.onap.msb.sdk.discovery.entity.NodeInfo;
import org.onap.msb.sdk.httpclient.msb.MSBServiceClient;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamTokenProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;
import org.springframework.core.env.Environment;

import java.util.HashSet;
import java.util.Set;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class TestMsbApiProvider extends TestBase {
    @Mock
    private Environment environment;
    @Mock
    private CbamTokenProvider cbamTokenProvider;
    private MicroServiceFullInfo microServiceInfo = new MicroServiceFullInfo();
    private Set<NodeInfo> nodes = new HashSet<>();
    private MsbApiProvider msbApiProvider;

    @Before
    public void init() {
        setField(MsbApiProvider.class, "logger", logger);
        msbApiProvider = new MsbApiProvider(environment);
        microServiceInfo.setNodes(nodes);
    }

    /**
     * test MSB client is created based on driver properties
     */
    @Test
    public void testMsbClient() {
        setFieldWithPropertyAnnotation(msbApiProvider, "${messageBusIp}", "mymessageBusIp");
        setFieldWithPropertyAnnotation(msbApiProvider, "${messageBusPort}", "123");
        //when
        MSBServiceClient msbClient = msbApiProvider.getMsbClient();
        //verify
        assertEquals("mymessageBusIp:123", msbClient.getMsbSvrAddress());
    }

    /**
     * error is propagated if no suitable micro service endpoint is found
     */
    @Test
    public void testNoSuitableMicroService() throws Exception {
        NodeInfo dockerAccessPoint = new NodeInfo();
        dockerAccessPoint.setIp("172.1.2.3");
        microServiceInfo.setServiceName("serviceName");
        microServiceInfo.setVersion("v1");
        microServiceInfo.setUrl("/lead/nslcm/v1");
        when(environment.getProperty(IpMappingProvider.IP_MAP, String.class, "")).thenReturn("");
        nodes.add(dockerAccessPoint);
        msbApiProvider = new MsbApiProvider(environment) {
            @Override
            public MSBServiceClient getMsbClient() {
                return msbClient;
            }
        };
        when(msbClient.queryMicroServiceInfo("serviceName", "v1")).thenReturn(microServiceInfo);
        //when
        try {
            msbApiProvider.getMicroServiceUrl("serviceName", "v1");
            fail();
        } catch (Exception e) {
            assertEquals("The serviceName service with v1 does not have any valid nodes[172.1.2.3:null  ttl:]", e.getMessage());
            verify(logger).error("The serviceName service with v1 does not have any valid nodes[172.1.2.3:null  ttl:]");
        }
    }

    /**
     * non Docker endpoint is selected
     */
    @Test
    public void testExistingValidEndpoint() throws Exception {
        NodeInfo nonDocker = new NodeInfo();
        nonDocker.setIp("173.1.2.3");
        nonDocker.setPort("234");
        microServiceInfo.setServiceName("serviceName");
        microServiceInfo.setVersion("v1");
        microServiceInfo.setUrl("/lead/nslcm/v1");
        when(environment.getProperty(IpMappingProvider.IP_MAP, String.class, "")).thenReturn("173.1.2.3->1.2.3.4");
        nodes.add(nonDocker);
        msbApiProvider = new MsbApiProvider(environment) {
            @Override
            public MSBServiceClient getMsbClient() {
                return msbClient;
            }
        };
        when(msbClient.queryMicroServiceInfo("serviceName", "v1")).thenReturn(microServiceInfo);
        msbApiProvider.afterPropertiesSet();
        //when
        assertEquals("http://1.2.3.4:234/lead/nslcm/v1", msbApiProvider.getMicroServiceUrl("serviceName", "v1"));
    }


    /**
     * use HTTPS for known ports (443) should be removed if https://jira.onap.org/browse/MSB-151 is solved
     */
    @Test
    public void testMsb151IssueHack() throws Exception {
        NodeInfo nonDocker = new NodeInfo();
        nonDocker.setIp("173.1.2.3");
        nonDocker.setPort("443");
        microServiceInfo.setServiceName("serviceName");
        microServiceInfo.setVersion("v1");
        microServiceInfo.setUrl("/lead/nslcm/v1");
        when(environment.getProperty(IpMappingProvider.IP_MAP, String.class, "")).thenReturn("173.1.2.3->1.2.3.4");
        nodes.add(nonDocker);
        msbApiProvider = new MsbApiProvider(environment) {
            @Override
            public MSBServiceClient getMsbClient() {
                return msbClient;
            }
        };
        when(msbClient.queryMicroServiceInfo("serviceName", "v1")).thenReturn(microServiceInfo);
        msbApiProvider.afterPropertiesSet();
        //when
        assertEquals("https://1.2.3.4:443/lead/nslcm/v1", msbApiProvider.getMicroServiceUrl("serviceName", "v1"));
    }

    /**
     * use HTTPS for known ports (443) should be removed if https://jira.onap.org/browse/MSB-151 is solved
     */
    @Test
    public void testMsb151IssueHack2() throws Exception {
        NodeInfo nonDocker = new NodeInfo();
        nonDocker.setIp("173.1.2.3");
        nonDocker.setPort("8443");
        microServiceInfo.setServiceName("serviceName");
        microServiceInfo.setVersion("v1");
        microServiceInfo.setUrl("/lead/nslcm/v1");
        when(environment.getProperty(IpMappingProvider.IP_MAP, String.class, "")).thenReturn("173.1.2.3->1.2.3.4");
        nodes.add(nonDocker);
        msbApiProvider = new MsbApiProvider(environment) {
            @Override
            public MSBServiceClient getMsbClient() {
                return msbClient;
            }
        };
        when(msbClient.queryMicroServiceInfo("serviceName", "v1")).thenReturn(microServiceInfo);
        msbApiProvider.afterPropertiesSet();
        //when
        assertEquals("https://1.2.3.4:8443/lead/nslcm/v1", msbApiProvider.getMicroServiceUrl("serviceName", "v1"));
    }

    /**
     * if unable to get micro service info the error is propagated
     */
    @Test
    public void testUnableQueryMicroserviInfo() throws Exception {
        msbApiProvider = new MsbApiProvider(environment) {
            @Override
            public MSBServiceClient getMsbClient() {
                return msbClient;
            }
        };
        RouteException expectedException = new RouteException();
        when(msbClient.queryMicroServiceInfo("serviceName", "v1")).thenThrow(expectedException);

        //when
        try {
            msbApiProvider.getMicroServiceUrl("serviceName", "v1");
            fail();
        } catch (Exception e) {
            assertEquals("Unable to get micro service URL for serviceName with version v1", e.getMessage());
            verify(logger).error("Unable to get micro service URL for serviceName with version v1", expectedException);
        }
    }

}
