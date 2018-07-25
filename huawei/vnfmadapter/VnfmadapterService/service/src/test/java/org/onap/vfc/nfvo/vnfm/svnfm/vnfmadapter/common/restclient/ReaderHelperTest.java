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

import java.io.Reader;
import java.io.StringReader;

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
public class ReaderHelperTest {

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
     * Test method for {@link org.openo.baseservice.util.inf.ReaderHelper#getLine()}.
     */
    @Test
    public void testGetLine() {
        final String message = "hello.. how are you?";
        final Reader reader = new StringReader(message);
        final ReaderHelper helper = new ReaderHelper(reader);
        final String actual = helper.getLine();
        assertEquals(message, actual);
    }

    /**
     * Test method for {@link org.openo.baseservice.util.inf.ReaderHelper#getLine()}.
     */
    @Test
    public void testGetLineMultiLine() {
        final String line1 = "hello.. how are you?";
        final String line2 = "I am fine.";
        final Reader reader = new StringReader(line1 + System.lineSeparator() + line2);
        final ReaderHelper helper = new ReaderHelper(reader);
        String actual = helper.getLine();
        assertEquals(line1, actual);
        actual = helper.getLine();
        assertEquals(line2, actual);
        actual = helper.getLine();
        assertEquals(null, actual);
    }

    @Test
    public void testGetLineNull() {
        final ReaderHelper helper = new ReaderHelper(null);
        final String actual = helper.getLine();
        assertEquals(null, actual);

    }

}
