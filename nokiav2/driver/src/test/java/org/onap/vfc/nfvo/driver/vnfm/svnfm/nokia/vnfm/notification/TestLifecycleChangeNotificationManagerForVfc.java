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