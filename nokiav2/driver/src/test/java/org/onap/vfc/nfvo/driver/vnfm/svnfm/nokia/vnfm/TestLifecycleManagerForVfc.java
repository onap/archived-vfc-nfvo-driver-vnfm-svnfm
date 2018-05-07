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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm;

import org.junit.Test;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIExternalSystemInfoProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.GrantlessGrantManager;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.vfc.VfcExternalSystemInfoProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.vfc.VfcGrantManager;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification.LifecycleChangeNotificationManagerForSo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification.LifecycleChangeNotificationManagerForVfc;

import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Mockito.mock;

public class TestLifecycleManagerForVfc extends TestBase{
    /**
     * Test bean
     */
    @Test
    public void testBean(){
        CatalogManagerForVfc catalogManager = mock(CatalogManagerForVfc.class);
        VfcGrantManager vfcGrantManager = mock(VfcGrantManager.class);
        VfcExternalSystemInfoProvider vfcExternalSystemInfoProvider = mock(VfcExternalSystemInfoProvider.class);
        JobManagerForVfc lifecycleManagerForVfc = mock(JobManagerForVfc.class);
        LifecycleChangeNotificationManagerForVfc lifecycleChangeNotificationManagerForVfc = mock(LifecycleChangeNotificationManagerForVfc.class);
        LifecycleManagerForVfc lifecycleManagerForSo = new LifecycleManagerForVfc(catalogManager, vfcGrantManager, cbamRestApiProviderForVfc, vfcExternalSystemInfoProvider, lifecycleManagerForVfc, lifecycleChangeNotificationManagerForVfc);
        assertNotNull(lifecycleManagerForSo);
        assertBean(LifecycleChangeNotificationManagerForVfc.class);
    }
}