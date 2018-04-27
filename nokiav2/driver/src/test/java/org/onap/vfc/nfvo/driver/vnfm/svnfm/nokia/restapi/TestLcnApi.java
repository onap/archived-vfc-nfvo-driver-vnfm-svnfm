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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.restapi;

import com.nokia.cbam.lcm.v32.model.VnfLifecycleChangeNotification;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification.LifecycleChangeNotificationManagerForSo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification.LifecycleChangeNotificationManagerForVfc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;


public class TestLcnApi extends TestBase {

    @Mock
    private VnfLifecycleChangeNotification lcn;
    @Mock
    private LifecycleChangeNotificationManagerForVfc lifecycleChangeNotificationManagerForVfc;
    @Mock
    private LifecycleChangeNotificationManagerForSo lifecycleChangeNotificationManagerForSo;

    private LcnApi lcnApi;

    @Before
    public void initMocks() throws Exception {
        setField(LcnApi.class, "logger", logger);
        lcnApi = new LcnApi(lifecycleChangeNotificationManagerForSo, lifecycleChangeNotificationManagerForVfc);
    }

    /**
     * test REST "ping" from CBAM to driver
     */
    @Test
    public void testPing() {
        lcnApi.testLcnConnectivity(null);
        //verify no exception is thrown
    }

    /**
     * test LCN is handled by LCN manager
     */
    @Test
    public void testHandleLcn() {
        //when
        lcnApi.handleLcn(lcn);
        //verify
        verify(lifecycleChangeNotificationManagerForVfc).handleLcn(lcn);
        verify(logger).info("REST: handle LCN");
    }
}
