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
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <br/>
 * <p>
 * </p>
 * 
 * @author
 * @version
 */
public class TestRestfulParametes {

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
    public void testGet() {
        final RestfulParametes params = new RestfulParametes();
        assertNull(params.get("param"));
        final Map<String, String> paramMap = new HashMap<String, String>();
        params.setParamMap(paramMap);
        paramMap.put("param", "value");
        assertEquals("value", params.get("param"));
    }

    /**
     * <br/>
     * 
     * @since
     */
    @Test
    public void testSetRawData() {
        final RestfulParametes params = new RestfulParametes();
        final String data = "Sample data.";
        params.setRawData(data);
        assertEquals(data, params.getRawData());
    }

    /**
     * <br/>
     * 
     * @since
     */
    @Test
    public void testGetRawData() {
        final RestfulParametes params = new RestfulParametes();
        assertNull(params.getRawData());
        final String data = "Sample data.";
        params.setRawData(data);
        assertEquals(data, params.getRawData());
    }

    /**
     * <br/>
     * 
     * @since
     */
    @Test
    public void testPut() {
        final RestfulParametes params = new RestfulParametes();
        params.put("somekey", "somevalue");
        params.put("otherkey", "othervalue");
        assertEquals("somevalue", params.get("somekey"));
        assertEquals("othervalue", params.get("otherkey"));
    }

    /**
     * <br/>
     * 
     * @since
     */
    @Test
    public void testPutHttpContextHeaderStringString() {
        final RestfulParametes params = new RestfulParametes();
        params.putHttpContextHeader("Context-Encoding", "UTF-8");
        assertEquals("UTF-8", params.getHttpContextHeader("Context-Encoding"));
    }

    /**
     * <br/>
     * 
     * @since
     */
    @Test
    public void testPutHttpContextHeaderStringInt() {
        final RestfulParametes params = new RestfulParametes();
        params.putHttpContextHeader("Expire-At", 1000);
        assertEquals("1000", params.getHttpContextHeader("Expire-At"));
    }

    /**
     * <br/>
     * 
     * @since
     */
    @Test
    public void testGetHttpContextHeader() {
        final RestfulParametes params = new RestfulParametes();
        params.putHttpContextHeader("Expire-At", 1000);
        params.putHttpContextHeader("Context-Encoding", "UTF-8");
        assertEquals("1000", params.getHttpContextHeader("Expire-At"));
        assertEquals("UTF-8", params.getHttpContextHeader("Context-Encoding"));
    }

    /**
     * <br/>
     * 
     * @since
     */
    @Test
    public void testGetParamMap() {
        final RestfulParametes params = new RestfulParametes();
        params.put("key", "value");
        final Map<String, String> map = params.getParamMap();
        assertEquals("value", map.get("key"));
    }

    /**
     * <br/>
     * 
     * @since
     */
    @Test
    public void testSetParamMap() {
        final RestfulParametes params = new RestfulParametes();
        final Map<String, String> map = new HashMap<String, String>();
        map.put("key", "value");
        params.setParamMap(map);
        assertEquals("value", params.get("key"));

    }

    /**
     * <br/>
     * 
     * @since
     */
    @Test
    public void testGetHeaderMap() {
        final RestfulParametes params = new RestfulParametes();
        params.putHttpContextHeader("key", "value");
        final Map<String, String> map = params.getHeaderMap();
        assertEquals("value", map.get("key"));
    }

    /**
     * <br/>
     * 
     * @since
     */
    @Test
    public void testSetHeaderMap() {
        final RestfulParametes params = new RestfulParametes();
        final Map<String, String> map = new HashMap<String, String>();
        map.put("key", "value");
        params.setHeaderMap(map);
        assertEquals("value", params.getHttpContextHeader("key"));
    }
}
