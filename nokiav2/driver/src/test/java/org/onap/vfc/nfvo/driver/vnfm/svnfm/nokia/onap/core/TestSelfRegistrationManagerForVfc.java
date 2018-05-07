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

import org.junit.Test;
import org.mockito.Mockito;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIExternalSystemInfoProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.vfc.VfcExternalSystemInfoProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.vfc.VfcNotificationSender;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;

import static junit.framework.TestCase.assertNotNull;

public class TestSelfRegistrationManagerForVfc extends TestBase {
    @Test
    public void testBean() {
        VfcExternalSystemInfoProvider vfcExternalSystemInfoProvider = Mockito.mock(VfcExternalSystemInfoProvider.class);
        SelfRegistrationManagerForVfc selfRegistrationManagerForVfc = new SelfRegistrationManagerForVfc(vfcExternalSystemInfoProvider, msbApiProvider, cbamRestApiProviderForVfc);
        assertNotNull(selfRegistrationManagerForVfc);
        assertBean(SelfRegistrationManagerForVfc.class);
    }
}