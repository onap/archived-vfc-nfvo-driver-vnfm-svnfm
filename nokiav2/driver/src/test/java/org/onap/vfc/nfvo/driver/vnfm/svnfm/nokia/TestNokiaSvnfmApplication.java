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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core.SelfRegistrationManager;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.test.util.ReflectionTestUtils;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.verify;


public class TestNokiaSvnfmApplication {
    @Mock
    private SelfRegistrationManager selfRegistrationManager;
    @Mock
    private Logger logger;
    @InjectMocks
    private NokiaSvnfmApplication.SelfRegistrationTriggerer selfRegistrationTriggerer;
    @InjectMocks
    private NokiaSvnfmApplication.SelfUnregistrationTriggerer selfUnregistrationTriggerer;


    @Before
    public void initMocks() throws Exception {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(NokiaSvnfmApplication.class, "logger", logger);
    }

    /**
     * Assert that the entry point of the application does not change
     */
    @Test
    public void doNotRename() {
        //verify
        //1. if the entry point is renamed the main class of spring boot in the driverwar must also be changed
        //2. all classes that use @Autowrire must be in a subpackage relative to this class
        assertEquals("org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.NokiaSvnfmApplication", NokiaSvnfmApplication.class.getCanonicalName());
    }

    /**
     * Assert that the self registration process is started after the servlet is up and is able to answer REST requests.
     */
    @Test
    public void testRegistrationIsCalledAfterComponentIsUp() throws Exception {
        //given
        ApplicationReadyEvent event = Mockito.mock(ApplicationReadyEvent.class);
        //when
        selfRegistrationTriggerer.onApplicationEvent(event);
        //verify
        verify(selfRegistrationManager).register();
        // this forces the event to be fired after the servlet is up (prevents refactor)
        assertTrue(ApplicationReadyEvent.class.isAssignableFrom(event.getClass()));
    }

    /**
     * Assert that the self unregistration process is started after the servlet has been ramped down
     */
    @Test
    public void testUnRegistrationIsCalledAfterComponentIsUp() throws Exception {
        //given
        ContextClosedEvent event = Mockito.mock(ContextClosedEvent.class);
        //when
        selfUnregistrationTriggerer.onApplicationEvent(event);
        //verify
        verify(selfRegistrationManager).unregister();
        // this forces the event to be fired after the servlet is down (prevents refactor)
        assertTrue(ContextClosedEvent.class.isAssignableFrom(event.getClass()));
    }

    /**
     * Failures in registration is logged and propagated
     */
    @Test
    public void failedRegistration() {
        //given
        RuntimeException expectedException = new RuntimeException();
        Mockito.doThrow(expectedException).when(selfRegistrationManager).register();
        ApplicationReadyEvent event = Mockito.mock(ApplicationReadyEvent.class);
        //when
        try {
            selfRegistrationTriggerer.onApplicationEvent(event);
            //verify
            TestCase.fail();
        } catch (RuntimeException e) {
            assertEquals(e, expectedException);
        }
        verify(logger).error("Self registration failed", expectedException);
    }

    /**
     * Failures in unregistration is logged and propagated
     */
    @Test
    public void failedUnRegistration() {
        //given
        RuntimeException expectedException = new RuntimeException();
        Mockito.doThrow(expectedException).when(selfRegistrationManager).unregister();
        ContextClosedEvent event = Mockito.mock(ContextClosedEvent.class);
        //when
        try {
            selfUnregistrationTriggerer.onApplicationEvent(event);
            //verify
            TestCase.fail();
        } catch (RuntimeException e) {
            assertEquals(e, expectedException);
        }
        verify(logger).error("Self unregistration failed", expectedException);
    }

    /**
     * static entry point calling an other static entry point can not be tested
     */
    @Test
    public void useless() throws Exception {
        try {
            NokiaSvnfmApplication.main(null);
        } catch (Exception e) {

        }
    }
}
