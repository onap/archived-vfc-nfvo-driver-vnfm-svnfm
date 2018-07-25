/*
 * Copyright 2017 Huawei Technologies Co., Ltd.
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

package org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.restclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import mockit.integration.junit4.JMockit;

/**
 * <br/>
 * <p>
 * </p>
 * 
 * @author
 * @version
 */
@RunWith(JMockit.class)
public class TestRestfulOptions {

    @Rule
    final public ExpectedException thrown = ExpectedException.none();

    /**
     * <br/>
     * 
     * @throws java.lang.Exception
     * @since
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * <br/>
     * 
     * @throws java.lang.Exception
     * @since
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * <br/>
     * 
     * @throws java.lang.Exception
     * @since
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * <br/>
     * 
     * @throws java.lang.Exception
     * @since
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * <br/>
     * 
     * @since
     */
    @Test
    public void testGetPort() {
        final RestfulOptions options = new RestfulOptions();
        final int port = 9091;
        assertEquals(0, options.getPort());
        options.setPort(port);
        assertEquals(port, options.getPort());
    }

    /**
     * <br/>
     * 
     * @since
     */
    @Test
    public void testSetPort() {
        final RestfulOptions options = new RestfulOptions();
        final int port = 9091;
        assertTrue(options.setPort(port));
        assertEquals(port, options.getPort());
    }

    /**
     * <br/>
     * 
     * @since
     */
    @Test
    public void testGetHost() {
        final RestfulOptions options = new RestfulOptions();
        final String host = "localhost";
        assertEquals("", options.getHost());
        options.setHost(host);
        assertEquals(host, options.getHost());
    }

    /**
     * <br/>
     * 
     * @since
     */
    @Test
    public void testSetHost() {
        final RestfulOptions options = new RestfulOptions();
        final String host = "localhost";
        assertTrue(options.setHost(host));
        assertEquals(host, options.getHost());
    }

    /**
     * <br/>
     * 
     * @since
     */
    @Test
    public void testSetRestTimeout() {
        final RestfulOptions options = new RestfulOptions();
        int timeout = 0;
        assertFalse(options.setRestTimeout(timeout));
        assertEquals(0, options.getRestTimeout());

        timeout = 1;
        assertTrue(options.setRestTimeout(timeout));
        assertEquals(timeout, options.getRestTimeout());

        timeout = 10;
        assertTrue(options.setRestTimeout(timeout));
        assertEquals(timeout, options.getRestTimeout());

        timeout = RestfulOptions.REST_OPTIONS_TIMEOUT_MAXTIMEOUT - 1;
        assertTrue(options.setRestTimeout(timeout));
        assertEquals(timeout, options.getRestTimeout());

        timeout = RestfulOptions.REST_OPTIONS_TIMEOUT_MAXTIMEOUT;
        assertTrue(options.setRestTimeout(timeout));
        assertEquals(timeout, options.getRestTimeout());

        timeout = RestfulOptions.REST_OPTIONS_TIMEOUT_MAXTIMEOUT + 1;
        assertFalse(options.setRestTimeout(timeout));
    }

    /**
     * <br/>
     * 
     * @since
     */
    @Test
    public void testGetRestTimeout() {
        final RestfulOptions options = new RestfulOptions();
        int timeout = 0;
        assertEquals(0, options.getRestTimeout());

        timeout = 1;
        assertTrue(options.setRestTimeout(timeout));
        assertEquals(timeout, options.getRestTimeout());
    }

    /**
     * <br/>
     * 
     * @since
     */
    @Test
    public void testGetOption() {
        final RestfulOptions options = new RestfulOptions();
        assertNull(options.getOption("invalid"));

        options.setHost("localhost");
        Object obj = options.getOption(RestfulClientConst.HOST_KEY_NAME);
        assertNotNull(obj);
        assertTrue(obj instanceof String);
        assertEquals("localhost", obj);

        final List<String> list = new ArrayList<String>();
        list.add("data");
        options.setOption("list", list);
        obj = options.getOption("list");
        assertNotNull(obj);
        assertTrue(obj instanceof List);
        assertSame(list, obj);
    }

    /**
     * <br/>
     * 
     * @since
     */
    @Test
    public void testGetIntOption() {
        final RestfulOptions options = new RestfulOptions();

        assertEquals(0, options.getIntOption("count"));

        options.setOption("count", 1);
        assertEquals(1, options.getIntOption("count"));

        thrown.expect(RuntimeException.class);

        options.setOption("string-count", "two");
        final int value = options.getIntOption("string-count");
        assertEquals(2, value);

    }

    /**
     * <br/>
     * 
     * @since
     */
    @Test
    public void testGetStringOption() {
        final RestfulOptions options = new RestfulOptions();

        assertEquals("", options.getStringOption("count"));

        options.setOption("string-count", "one");
        assertEquals("one", options.getStringOption("string-count"));

        thrown.expect(RuntimeException.class);

        options.setOption("count", 2);
        final String value = options.getStringOption("count");
        assertEquals(2, value);
    }

    /**
     * <br/>
     * 
     * @since
     */
    @Test
    public void testSetOption() {
        final RestfulOptions options = new RestfulOptions();
        assertNull(options.getOption("invalid"));

        options.setHost("localhost");
        Object obj = options.getOption(RestfulClientConst.HOST_KEY_NAME);
        assertNotNull(obj);
        assertTrue(obj instanceof String);
        assertEquals("localhost", obj);

        final List<String> list = new ArrayList<String>();
        list.add("data");
        options.setOption("list", list);
        obj = options.getOption("list");
        assertNotNull(obj);
        assertTrue(obj instanceof List);
        assertSame(list, obj);
    }
}
