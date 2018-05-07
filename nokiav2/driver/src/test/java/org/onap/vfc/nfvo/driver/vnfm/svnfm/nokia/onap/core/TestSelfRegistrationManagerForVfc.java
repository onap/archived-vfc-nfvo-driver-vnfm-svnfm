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