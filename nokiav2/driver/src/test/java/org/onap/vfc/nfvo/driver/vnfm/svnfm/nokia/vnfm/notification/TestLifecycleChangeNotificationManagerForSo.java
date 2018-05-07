package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification;

import junit.framework.TestCase;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.notification.AAINotificationProcessor;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;

import static junit.framework.TestCase.assertNotNull;

public class TestLifecycleChangeNotificationManagerForSo extends TestBase {
    @Test
    public void testBean() {
        AAINotificationProcessor aaiNotificationProcessor = Mockito.mock(AAINotificationProcessor.class);
        LifecycleChangeNotificationManagerForSo lifecycleChangeNotificationManagerForSo = new LifecycleChangeNotificationManagerForSo(cbamRestApiProviderForSo, selfRegistrationManagerForSo, aaiNotificationProcessor);
        assertNotNull(lifecycleChangeNotificationManagerForSo);
        assertBean(LifecycleChangeNotificationManagerForSo.class);
    }
}