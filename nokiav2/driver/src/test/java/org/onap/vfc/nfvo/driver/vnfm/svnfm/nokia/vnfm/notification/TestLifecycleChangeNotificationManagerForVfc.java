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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification;

import org.junit.Test;
import org.mockito.Mockito;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.notification.AAINotificationProcessor;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.vfc.VfcNotificationSender;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;

import static junit.framework.TestCase.assertNotNull;

public class TestLifecycleChangeNotificationManagerForVfc extends TestBase {
    @Test
    public void testBean() {
        VfcNotificationSender vfcNotificationSender = Mockito.mock(VfcNotificationSender.class);
        LifecycleChangeNotificationManagerForVfc lifecycleChangeNotificationManagerForVfc = new LifecycleChangeNotificationManagerForVfc(cbamRestApiProviderForVfc, selfRegistrationManagerForVfc, vfcNotificationSender);
        assertNotNull(lifecycleChangeNotificationManagerForVfc);
        assertBean(LifecycleChangeNotificationManagerForVfc.class);
    }
}