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

import junit.framework.TestCase;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIExternalSystemInfoProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.vfc.VfcExternalSystemInfoProvider;


public class TestCbamRestApiProviderForVfc extends TestBase{
    @Test
    public void testBean(){
        CbamTokenProviderForVfc cbamTokenProvider = Mockito.mock(CbamTokenProviderForVfc.class);
        VfcExternalSystemInfoProvider c = Mockito.mock(VfcExternalSystemInfoProvider.class);
        CbamSecurityProvider cbamSecurityProvider = Mockito.mock(CbamSecurityProvider.class);
        CbamRestApiProviderForVfc cbamRestApiProviderForVfc = new CbamRestApiProviderForVfc(cbamTokenProvider, c, cbamSecurityProvider);
        TestCase.assertNotNull(cbamRestApiProviderForVfc);
        assertBean(CbamRestApiProviderForVfc.class);
    }
}