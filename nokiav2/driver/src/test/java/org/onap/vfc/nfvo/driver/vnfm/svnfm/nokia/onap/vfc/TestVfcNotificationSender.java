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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.vfc;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.DriverProperties;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.LifecycleManager;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;
import org.onap.vfccatalog.api.VnfpackageApi;
import org.onap.vnfmdriver.ApiException;
import org.onap.vnfmdriver.api.NslcmApi;
import org.onap.vnfmdriver.model.VNFLCMNotification;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class TestVfcNotificationSender extends TestBase {
    private VNFLCMNotification notificationToSend = new VNFLCMNotification();
    private VfcNotificationSender vfcNotificationSender;


    @Before
    public void init() {
        vfcNotificationSender = new VfcNotificationSender(driverProperties, vfcRestApiProvider);
        notificationToSend.setVnfInstanceId(VNF_ID);
        setField(VfcNotificationSender.class, "logger", logger);
    }

    /**
     * Notification is sent to VF-C
     */
    @Test
    public void testSuccess()throws Exception{
        //when
        vfcNotificationSender.sendNotification(notificationToSend);
        //verify
        verify(nsLcmApi).vNFLCMNotification(VNFM_ID, VNF_ID, notificationToSend);
    }

    /**
     * Failure to send notification to VF-C results in error
     */
    @Test
    public void testFailuire()throws Exception{
        ApiException expectedException = new ApiException();
        doThrow(expectedException).when(nsLcmApi).vNFLCMNotification(any(), any(), any());
        //when
        try {
            vfcNotificationSender.sendNotification(notificationToSend);
        //verify
            fail();
        }
        catch (Exception e){
            verify(logger).error("Unable to send LCN to VF-C", expectedException);
            assertEquals(expectedException, e.getCause());
        }
    }

}
