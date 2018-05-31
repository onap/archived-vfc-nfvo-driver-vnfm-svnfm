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

import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.SystemFunctions;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.JobManagerForSo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.JobManagerForVfc;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.ConfigurableEnvironment;

import static junit.framework.TestCase.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;


public class TestNokiaSvnfmApplication extends TestBase {
    @Mock
    private JobManagerForVfc jobManagerForVfc;
    @Mock
    private JobManagerForSo jobManagerForSo;

    private NokiaSvnfmApplication.SelfRegistrationTrigger selfRegistrationTriggerer;
    private NokiaSvnfmApplication.SelfDeRegistrationTrigger selfUnregistrationTriggerer;


    @Before
    public void initMocks() throws Exception {
        selfRegistrationTriggerer = new NokiaSvnfmApplication.SelfRegistrationTrigger(selfRegistrationManagerForVfc, selfRegistrationManagerForSo, jobManagerForSo, jobManagerForVfc);
        selfUnregistrationTriggerer = new NokiaSvnfmApplication.SelfDeRegistrationTrigger(selfRegistrationManagerForVfc, selfRegistrationManagerForSo, jobManagerForSo, jobManagerForVfc);
        setField(NokiaSvnfmApplication.class, "logger", logger);
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
    @SuppressWarnings("squid:S2925") //the execution is asynchronous no other way to wait
    public void testRegistrationIsCalledAfterComponentIsUp() throws Exception {
        //given
        ApplicationReadyEvent event = Mockito.mock(ApplicationReadyEvent.class);
        useVfc(event);
        //when
        selfRegistrationTriggerer.onApplicationEvent(event);
        //verify
        boolean success = false;
        while (!success) {
            try {
                verify(selfRegistrationManagerForVfc).register();
                verify(logger).info("Self registration started");
                verify(logger).info("Self registration finished");
                success = true;
            } catch (Error e) {

            }
            Thread.sleep(10);
        }
        // this forces the event to be fired after the servlet is up (prevents refactor)
        assertTrue(ApplicationReadyEvent.class.isAssignableFrom(event.getClass()));
    }

    /**
     * Assert that the self registration process is started after the servlet is up and is able to answer REST requests.
     */
    @Test
    @SuppressWarnings("squid:S2925") //the execution is asynchronous no other way to wait
    public void testRegistrationIsNotCalledIfPreparingForShutdown() throws Exception {
        //given
        ApplicationReadyEvent event = Mockito.mock(ApplicationReadyEvent.class);
        useSo(event);
        when(jobManagerForSo.isPreparingForShutDown()).thenReturn(true);
        //when
        selfRegistrationTriggerer.onApplicationEvent(event);
        //verify
        boolean success = false;
        while (!success) {
            try {
                verify(logger).warn("Component is preparing for shutdown giving up component start");
                success = true;
            } catch (Error e) {

            }
            Thread.sleep(10);
        }
        verify(selfRegistrationManagerForSo, never()).register();
        // this forces the event to be fired after the servlet is up (prevents refactor)
        assertTrue(ApplicationReadyEvent.class.isAssignableFrom(event.getClass()));
    }


    /**
     * Assert that the self registration process is started after the servlet is up and is able to answer REST requests.
     */
    @Test
    @SuppressWarnings("squid:S2925") //the execution is asynchronous no other way to wait
    public void testRegistrationIsCalledAfterComponentIsUpForSo() throws Exception {
        //given
        ApplicationReadyEvent event = Mockito.mock(ApplicationReadyEvent.class);
        useSo(event);
        //when
        selfRegistrationTriggerer.onApplicationEvent(event);
        //verify
        boolean success = false;
        while (!success) {
            try {
                verify(selfRegistrationManagerForSo).register();
                verify(logger).info("Self registration started");
                verify(logger).info("Self registration finished");
                success = true;
            } catch (Error e) {

            }
            Thread.sleep(10);
        }
        // this forces the event to be fired after the servlet is up (prevents refactor)
        assertTrue(ApplicationReadyEvent.class.isAssignableFrom(event.getClass()));
    }

    /**
     * Failuires in the self registration process is retried for SO
     */
    @Test
    @SuppressWarnings("squid:S2925") //the execution is asynchronous no other way to wait
    public void testFailuresInRegistrationIsRetriedForSo() throws Exception {
        //given
        ApplicationReadyEvent event = Mockito.mock(ApplicationReadyEvent.class);
        RuntimeException e2 = new RuntimeException();
        useSo(event);
        Set<Boolean> calls = new HashSet<>();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                if (calls.size() == 0) {
                    calls.add(true);
                    throw e2;
                }
                return null;
            }
        }).when(selfRegistrationManagerForSo).register();
        //when
        selfRegistrationTriggerer.onApplicationEvent(event);
        //verify
        boolean success = false;
        while (!success) {
            try {
                verify(logger).info("Self registration finished");
                success = true;
            } catch (Error e) {

            }
            Thread.sleep(10);
        }
        verify(selfRegistrationManagerForSo, times(2)).register();
        verify(systemFunctions).sleep(5000);
        verify(logger, times(2)).info("Self registration started");
        verify(logger).error("Self registration failed", e2);
        // this forces the event to be fired after the servlet is up (prevents refactor)
        assertTrue(ApplicationReadyEvent.class.isAssignableFrom(event.getClass()));
    }

    /**
     * Failuires in the self registration process is retried for VFC
     */
    @Test
    @SuppressWarnings("squid:S2925") //the execution is asynchronous no other way to wait
    public void testFailuresInRegistrationIsRetriedForVfc() throws Exception {
        //given
        ApplicationReadyEvent event = Mockito.mock(ApplicationReadyEvent.class);
        RuntimeException e2 = new RuntimeException();
        useVfc(event);
        Set<Boolean> calls = new HashSet<>();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                if (calls.size() == 0) {
                    calls.add(true);
                    throw e2;
                }
                return null;
            }
        }).when(selfRegistrationManagerForVfc).register();
        //when
        selfRegistrationTriggerer.onApplicationEvent(event);
        //verify
        boolean success = false;
        while (!success) {
            try {
                verify(logger).info("Self registration finished");
                success = true;
            } catch (Error e) {

            }
            Thread.sleep(10);
        }
        verify(selfRegistrationManagerForVfc, times(2)).register();
        verify(systemFunctions).sleep(5000);
        verify(logger, times(2)).info("Self registration started");
        verify(logger).error("Self registration failed", e2);
        // this forces the event to be fired after the servlet is up (prevents refactor)
        assertTrue(ApplicationReadyEvent.class.isAssignableFrom(event.getClass()));
    }

    private void useSo(ApplicationReadyEvent event) {
        ConfigurableApplicationContext context = Mockito.mock(ConfigurableApplicationContext.class);
        ConfigurableEnvironment environment = Mockito.mock(ConfigurableEnvironment.class);
        when(context.getEnvironment()).thenReturn(environment);
        when(event.getApplicationContext()).thenReturn(context);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"direct"});
    }

    private void useVfc(ApplicationReadyEvent event) {
        ConfigurableApplicationContext context = Mockito.mock(ConfigurableApplicationContext.class);
        ConfigurableEnvironment environment = Mockito.mock(ConfigurableEnvironment.class);
        when(context.getEnvironment()).thenReturn(environment);
        when(event.getApplicationContext()).thenReturn(context);
        when(environment.getActiveProfiles()).thenReturn(new String[]{});
    }

    private void useVfc(ContextClosedEvent event) {
        ApplicationContext context = Mockito.mock(ApplicationContext.class);
        when(context.getEnvironment()).thenReturn(environment);
        when(event.getApplicationContext()).thenReturn(context);
        when(environment.getActiveProfiles()).thenReturn(new String[]{});
    }

    private void useSo(ContextClosedEvent event) {
        ApplicationContext context = Mockito.mock(ApplicationContext.class);
        when(context.getEnvironment()).thenReturn(environment);
        when(event.getApplicationContext()).thenReturn(context);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"direct"});
    }

    /**
     * Assert that the self de-registration process is started after the servlet has been ramped down
     */
    @Test
    public void testUnRegistrationIsCalledAfterComponentIsUp() throws Exception {
        //given
        ContextClosedEvent event = Mockito.mock(ContextClosedEvent.class);
        useVfc(event);
        //when
        selfUnregistrationTriggerer.onApplicationEvent(event);
        //verify
        InOrder inOrder = Mockito.inOrder(jobManagerForVfc, jobManagerForSo, selfRegistrationManagerForVfc);
        inOrder.verify(jobManagerForVfc).prepareForShutdown();
        inOrder.verify(jobManagerForSo).prepareForShutdown();
        inOrder.verify(selfRegistrationManagerForVfc).deRegister();
        verify(logger).info("Self de-registration started");
        verify(logger).info("Self de-registration finished");
        // this forces the event to be fired after the servlet is down (prevents refactor)
        assertTrue(ContextClosedEvent.class.isAssignableFrom(event.getClass()));
    }

    /**
     * Assert that the self de-registration process is started after the servlet has been ramped down
     */
    @Test
    public void testUnRegistrationIsCalledAfterComponentIsUpForSo() throws Exception {
        //given
        ContextClosedEvent event = Mockito.mock(ContextClosedEvent.class);
        useSo(event);
        //when
        selfUnregistrationTriggerer.onApplicationEvent(event);
        //verify
        InOrder inOrder = Mockito.inOrder(jobManagerForVfc, jobManagerForSo, selfRegistrationManagerForSo);
        inOrder.verify(jobManagerForVfc).prepareForShutdown();
        inOrder.verify(jobManagerForSo).prepareForShutdown();
        inOrder.verify(selfRegistrationManagerForSo).deRegister();
        verify(logger).info("Self de-registration started");
        verify(logger).info("Self de-registration finished");
        // this forces the event to be fired after the servlet is down (prevents refactor)
        assertTrue(ContextClosedEvent.class.isAssignableFrom(event.getClass()));
    }

    /**
     * Assert that the self registration process is started after the servlet is up and is able to answer REST requests.
     */
    @Test
    public void testPreparingForShutdownDoesNotStartRegistration() throws Exception {
        //given
        ApplicationReadyEvent event = Mockito.mock(ApplicationReadyEvent.class);
        when(jobManagerForVfc.isPreparingForShutDown()).thenReturn(true);
        //when
        selfRegistrationTriggerer.onApplicationEvent(event);
        //verify
        verify(selfRegistrationManagerForVfc, never()).register();
    }

    /**
     * Failures in registration is logged and propagated
     */
    @Test
    @SuppressWarnings("squid:S2925") //the execution is asynchronous no other way to wait

    public void failedFirstRegistration() {
        //given
        Set<RuntimeException> expectedException = new HashSet<>();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                if (expectedException.size() == 0) {
                    RuntimeException e = new RuntimeException();
                    expectedException.add(e);
                    throw e;
                }
                return null;
            }
        }).when(selfRegistrationManagerForVfc).register();
        ApplicationReadyEvent event = Mockito.mock(ApplicationReadyEvent.class);
        useVfc(event);
        //when
        selfRegistrationTriggerer.onApplicationEvent(event);
        //verify
        //wait for the registration to succeed
        boolean success = false;
        while (!success) {
            try {
                verify(logger).info("Self registration finished");
                success = true;
                Thread.sleep(10);
            } catch (Exception e2) {
            } catch (Error e) {
            }
        }
        verify(logger, times(2)).info("Self registration started");
        verify(logger).error("Self registration failed", expectedException.iterator().next());
    }

    /**
     * Failures in de-registration is logged and propagated
     */
    @Test
    public void failedDeRegistration() {
        //given
        RuntimeException expectedException = new RuntimeException();
        Mockito.doThrow(expectedException).when(selfRegistrationManagerForVfc).deRegister();
        ContextClosedEvent event = Mockito.mock(ContextClosedEvent.class);
        useVfc(event);
        //when
        try {
            selfUnregistrationTriggerer.onApplicationEvent(event);
            //verify
            fail();
        } catch (RuntimeException e) {
            assertEquals(e, expectedException);
        }
        verify(logger).error("Self de-registration failed", expectedException);
    }

    /**
     * Spring will instantiate using reflection
     */
    @Test
    public void testUseStaticWay() throws Exception {
        //verify
        //the constructor is public even if has no private fields
        new NokiaSvnfmApplication();
    }

    /**
     * test spring application bootstrapping
     */
    @Test
    public void useless() throws Exception {
        String[] args = new String[0];
        SpringApplication springApplicaiton = Mockito.mock(SpringApplication.class);
        SystemFunctions systemFunctions = SystemFunctions.systemFunctions();
        when(this.systemFunctions.newSpringApplication(NokiaSvnfmApplication.class)).thenReturn(springApplicaiton);
        //when
        NokiaSvnfmApplication.main(args);
        //verify
        verify(springApplicaiton).run(args);
    }
}
