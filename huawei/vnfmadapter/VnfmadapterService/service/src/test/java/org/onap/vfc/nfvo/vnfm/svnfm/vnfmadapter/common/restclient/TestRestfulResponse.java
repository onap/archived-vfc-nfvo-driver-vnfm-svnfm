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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * <br/>
 * <p>
 * </p>
 * 
 * @author
 * @version
 */
public class TestRestfulResponse {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

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
    public void testGetStatus() {
        final RestfulResponse response = new RestfulResponse();
        int actual = response.getStatus();
        int expected = -1;

        assertEquals(expected, actual);
        expected = 202;
        response.setStatus(expected);
        actual = response.getStatus();
        assertEquals(expected, actual);
    }

    /**
     * <br/>
     * 
     * @since
     */
    @Test
    public void testSetStatus() {
        final RestfulResponse response = new RestfulResponse();
        final int expected = 10;
        response.setStatus(expected);
        final int actual = response.getStatus();
        assertEquals(expected, actual);
    }

    /**
     * <br/>
     * 
     * @since
     */
    @Test
    public void testGetRespHeaderMap() {
        final RestfulResponse response = new RestfulResponse();
        Map<String, String> expected = response.getRespHeaderMap();
        assertNull(expected);
        expected = new HashMap<String, String>();
        expected.put("key", "value");
        response.setRespHeaderMap(expected);
        final Map<String, String> actual = response.getRespHeaderMap();
        assertNotNull(actual);
        assertSame(actual, expected);

    }

    /**
     * <br/>
     * 
     * @since
     */
    @Test
    public void testSetRespHeaderMap() {
        final RestfulResponse response = new RestfulResponse();
        response.setRespHeaderMap(null);
        Map<String, String> expected = response.getRespHeaderMap();
        assertNull(expected);
        expected = new HashMap<String, String>();
        expected.put("key", "value");
        response.setRespHeaderMap(expected);
        final Map<String, String> actual = response.getRespHeaderMap();
        assertNotNull(actual);
        assertSame(actual, expected);
    }

    /**
     * <br/>
     * 
     * @since
     */
    @Test
    public void testGetRespHeaderInt() {
        final RestfulResponse response = new RestfulResponse();
        response.setRespHeaderMap(null);
        int actual = response.getRespHeaderInt("somekey");
        assertEquals(-1, actual);
        final Map<String, String> headers = new HashMap<String, String>();
        headers.put("key", "value");
        headers.put("count", "1");
        response.setRespHeaderMap(headers);
        actual = response.getRespHeaderInt("somekey");
        assertEquals(-1, actual);

        actual = response.getRespHeaderInt("count");
        assertEquals(1, actual);

        thrown.expect(RuntimeException.class);
        actual = response.getRespHeaderInt("key");
        assertEquals(1, actual);

    }

    /**
     * <br/>
     * 
     * @since
     */
    @Test
    public void testGetRespHeaderLong() {
        final RestfulResponse response = new RestfulResponse();
        response.setRespHeaderMap(null);
        long actual = response.getRespHeaderLong("somekey");
        assertEquals(-1, actual);
        final Map<String, String> headers = new HashMap<String, String>();
        headers.put("key", "value");
        headers.put("count", "1");
        headers.put("max", "" + Long.MAX_VALUE);
        headers.put("max++", Long.MAX_VALUE + 1 + "");
        response.setRespHeaderMap(headers);
        actual = response.getRespHeaderLong("somekey");
        assertEquals(-1, actual);

        actual = response.getRespHeaderLong("count");
        assertEquals(1, actual);

        actual = response.getRespHeaderLong("max");
        assertEquals(Long.MAX_VALUE, actual);

        actual = response.getRespHeaderLong("max++");
        assertTrue(actual < 0);

        thrown.expect(RuntimeException.class);
        actual = response.getRespHeaderLong("key");
        assertEquals(1, actual);
    }

    /**
     * <br/>
     * 
     * @since
     */
    @Test
    public void testGetRespHeaderStr() {
        final RestfulResponse response = new RestfulResponse();
        response.setRespHeaderMap(null);
        String actual = response.getRespHeaderStr("somekey");
        assertEquals(null, actual);
        final Map<String, String> headers = new HashMap<String, String>();
        headers.put("key", "value");
        headers.put("count", "1");
        headers.put("max", "" + Long.MAX_VALUE);
        response.setRespHeaderMap(headers);
        actual = response.getRespHeaderStr("somekey");
        assertEquals(null, actual);

        actual = response.getRespHeaderStr("key");
        assertEquals("value", actual);

    }

    /**
     * <br/>
     * 
     * @since
     */
    @Test
    public void testGetResponseContent() {
        final RestfulResponse response = new RestfulResponse();
        assertEquals(null, response.getResponseContent());

        final String content = "{ \"content\" = \"The response content\" }";
        response.setResponseJson(content);
        assertEquals(content, response.getResponseContent());
    }

    /**
     * <br/>
     * 
     * @since
     */
    @Test
    public void testSetResponseJson() {
        final RestfulResponse response = new RestfulResponse();
        assertEquals(null, response.getResponseContent());

        final String content = "{ \"content\" = \"The response content\" }";
        response.setResponseJson(content);
        assertEquals(content, response.getResponseContent());
    }
}
