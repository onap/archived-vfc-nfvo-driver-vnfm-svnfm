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

import io.reactivex.Observable;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.msb.ApiClient;
import org.onap.msb.api.ServiceResourceApi;
import org.onap.msb.model.MicroServiceFullInfo;
import org.onap.msb.model.NodeInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamTokenProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
    private List<NodeInfo> nodes = new ArrayList<>();
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
        ApiClient msbClient = msbApiProvider.buildApiClient();
        //verify
        assertEquals("http://mymessagebusip:123/api/msdiscover/v1/", msbClient.getAdapterBuilder().build().baseUrl().toString());
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
            public ServiceResourceApi getMsbApi() {
                return msbClient;
            }
        };
        when(msbClient.getMicroService_0("serviceName", "v1", null, null, null, null, null)).thenReturn(buildObservable(microServiceInfo));
        //when
        try {
            msbApiProvider.getMicroServiceUrl("serviceName", "v1");
            fail();
        } catch (Exception e) {
            String msg = "The serviceName service with v1 does not have any valid nodes[class NodeInfo {\n" +
                    "    ip: 172.1.2.3\n" +
                    "    port: null\n" +
                    "    lbServerParams: null\n" +
                    "    checkType: null\n" +
                    "    checkUrl: null\n" +
                    "    checkInterval: null\n" +
                    "    checkTimeOut: null\n" +
                    "    ttl: null\n" +
                    "    haRole: null\n" +
                    "    nodeId: null\n" +
                    "    status: null\n" +
                    "    expiration: null\n" +
                    "    createdAt: null\n" +
                    "    updatedAt: null\n" +
                    "}]";
            assertEquals(msg, e.getMessage());
            verify(logger).error(msg);
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
            public ServiceResourceApi getMsbApi() {
                return msbClient;
            }
        };
        when(msbClient.getMicroService_0("serviceName", "v1", null, null, null, null, null)).thenReturn(buildObservable(microServiceInfo));
        msbApiProvider.afterPropertiesSet();
        //when
        assertEquals("http://1.2.3.4:234/lead/nslcm/v1", msbApiProvider.getMicroServiceUrl("serviceName", "v1"));
    }


    /**
     * use HTTPS for known ports
     */
    @Test
    public void testMsbServiceOverssl() throws Exception {
        NodeInfo nonDocker = new NodeInfo();
        nonDocker.setIp("173.1.2.3");
        nonDocker.setPort("123");
        microServiceInfo.setServiceName("serviceName");
        microServiceInfo.setVersion("v1");
        microServiceInfo.setUrl("/lead/nslcm/v1");
        microServiceInfo.setEnableSsl(true);
        when(environment.getProperty(IpMappingProvider.IP_MAP, String.class, "")).thenReturn("173.1.2.3->1.2.3.4");
        nodes.add(nonDocker);
        msbApiProvider = new MsbApiProvider(environment) {
            @Override
            public ServiceResourceApi getMsbApi() {
                return msbClient;
            }
        };
        when(msbClient.getMicroService_0("serviceName", "v1", null, null, null, null, null)).thenReturn(buildObservable(microServiceInfo));
        msbApiProvider.afterPropertiesSet();
        //when
        assertEquals("https://1.2.3.4:123/lead/nslcm/v1", msbApiProvider.getMicroServiceUrl("serviceName", "v1"));
    }

    /**
     * if unable to get micro service info the error is propagated
     */
    @Test
    public void testUnableQueryMicroserviInfo() throws Exception {
        msbApiProvider = new MsbApiProvider(environment) {
            @Override
            public ServiceResourceApi getMsbApi() {
                return msbClient;
            }
        };
        RuntimeException expectedException = new RuntimeException();
        when(msbClient.getMicroService_0("serviceName", "v1", null, null, null, null, null)).thenReturn( Observable.error(expectedException));
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
