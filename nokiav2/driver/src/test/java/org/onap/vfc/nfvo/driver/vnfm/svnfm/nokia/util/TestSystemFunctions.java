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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util;

import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.NokiaSvnfmApplication;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.Useless;
import org.springframework.boot.SpringApplication;

import static org.junit.Assert.*;

public class TestSystemFunctions {

    /**
     * test sleep
     */
    @Test
    public void testSleep() throws Exception {
        long start = System.currentTimeMillis();
        SystemFunctions.systemFunctions().sleep(123);
        long end = System.currentTimeMillis();
        assertTrue(end - start >= 123);
    }

    /**
     * test interrupted sleep
     */
    @Test
    @SuppressWarnings("squid:S2925") //testing asynchronous execution
    public void testInterruptedSleep() throws Exception {
        AtomicBoolean entered = new AtomicBoolean(false);
        long start = System.currentTimeMillis();
        Set<RuntimeException> exceptions = new HashSet<>();
        class Inter extends Thread {
            @Override
            public void run() {
                try {
                    entered.set(true);
                    SystemFunctions.systemFunctions().sleep(1000000);
                } catch (RuntimeException e) {
                    exceptions.add(e);
                    throw e;
                }
            }
        }
        Inter inter = new Inter();
        inter.start();
        //wait for thread to enter waiting
        while(!entered.get() && inter.getState() != Thread.State.TIMED_WAITING && (System.currentTimeMillis() < start + 60*1000) ){
            Thread.sleep(10);
        }
        if(!(System.currentTimeMillis() < start + 60*1000)){
            throw new RuntimeException("Thread did not enter waiting state");
        }
        //when
        inter.interrupt();
        //verify
        while (exceptions.size() != 1 && (System.currentTimeMillis() < start + 60*1000)) {
            Thread.sleep(10);
        }
        assertEquals(1, exceptions.size());
        assertEquals("Interrupted while sleeping", exceptions.iterator().next().getMessage());
    }

    /**
     * test current time
     */
    @Test
    public void testCurrentTime() {
        long now = System.currentTimeMillis();
        long now2 = SystemFunctions.systemFunctions().currentTimeMillis();
        assertTrue(Math.abs(now2 - now) < 1000);
    }

    /**
     * test file load
     */
    @Test
    public void testFileLoad() {
        byte[] bytes = SystemFunctions.systemFunctions().loadFile("unittests/empty.zip");
        assertEquals("UEsFBgAAAAAAAAAAAAAAAAAAAAAAAA==", Base64.getEncoder().encodeToString(bytes));
    }

    /**
     * missing file results in error
     */
    @Test
    public void testMissingFileLoad() {
        try {
            SystemFunctions.systemFunctions().loadFile("unittests/missing");
            fail();
        } catch (Exception e) {
            assertEquals("Unable to load unittests/missing", e.getMessage());
        }
    }

    /**
     * Test standard stream wrapping
     */
    @Test
    public void testStandardStreams() {
        assertEquals(System.err, SystemFunctions.systemFunctions().err());
        assertEquals(System.out, SystemFunctions.systemFunctions().out());
        assertEquals(System.in, SystemFunctions.systemFunctions().in());
    }

    /**
     * Test HTTP client wrapping
     */
    @Test
    @Useless //more less already ensured by Java type safety
    public void testHttp() {
        assertNotNull(SystemFunctions.systemFunctions().getHttpClient());
    }

    /**
     * Test spring application wrapping
     */
    @Test
    public void testSpring() {
        SpringApplication springApplication = SystemFunctions.systemFunctions().newSpringApplication(NokiaSvnfmApplication.class);

        assertEquals(1, springApplication.getAllSources().size());
    }
}
