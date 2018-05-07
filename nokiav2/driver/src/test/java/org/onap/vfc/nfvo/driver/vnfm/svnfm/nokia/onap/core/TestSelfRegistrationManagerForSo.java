package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core;

import org.junit.Test;
import org.mockito.Mockito;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIExternalSystemInfoProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.vfc.VfcNotificationSender;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification.LifecycleChangeNotificationManagerForVfc;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.*;

public class TestSelfRegistrationManagerForSo extends TestBase {
    @Test
    public void testBean() {
        VfcNotificationSender vfcNotificationSender = Mockito.mock(VfcNotificationSender.class);
        AAIExternalSystemInfoProvider aaiExternalSystemInfoProvider = Mockito.mock(AAIExternalSystemInfoProvider.class);
        SelfRegistrationManagerForSo selfRegistrationManagerForSo = new SelfRegistrationManagerForSo(aaiExternalSystemInfoProvider, msbApiProvider, cbamRestApiProviderForSo);
        assertNotNull(selfRegistrationManagerForSo);
        assertBean(SelfRegistrationManagerForSo.class);
    }
}