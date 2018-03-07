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

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static org.mockito.Mockito.verify;

public class TestCbamUtils {

    /**
     * test child of json object
     */
    @Test
    public void testChild() throws Exception {
        JsonObject parent = new JsonObject();
        JsonObject child = new JsonObject();
        parent.add("x", child);
        assertEquals(child, CbamUtils.child(parent, "x"));
    }

    /**
     * if child is not a json object error is propagated
     */
    @Test
    public void testNonJsonObjectChild() throws Exception {
        JsonObject parent = new JsonObject();
        JsonPrimitive child = new JsonPrimitive("y");
        parent.add("x", child);
        try {
            CbamUtils.child(parent, "x");
            fail();
        } catch (RuntimeException e) {
            assertEquals("Not a JSON Object: \"y\"", e.getMessage());
        }
    }

    /**
     * if no child is present error is propagated
     */
    @Test
    public void testMissingChild() throws Exception {
        JsonObject parent = new JsonObject();
        try {
            CbamUtils.child(parent, "z");
            fail();
        } catch (RuntimeException e) {
            assertEquals("Missing child z", e.getMessage());
        }
    }

    /**
     * test child of json object
     */
    @Test
    public void testChildElement() throws Exception {
        JsonObject parent = new JsonObject();
        JsonPrimitive child = new JsonPrimitive("y");
        parent.add("x", child);
        assertEquals(child, CbamUtils.childElement(parent, "x"));
    }

    /**
     * if no child is present error is propagated
     */
    @Test
    public void testMissingChildElement() throws Exception {
        JsonObject parent = new JsonObject();
        try {
            CbamUtils.childElement(parent, "z");
            fail();
        } catch (RuntimeException e) {
            assertEquals("Missing child z", e.getMessage());
        }
    }


    /**
     * test fatal failure handling
     */
    @Test
    public void testFatalFailure() throws Exception {
        Exception expectedException = new Exception();
        Logger logger = Mockito.mock(Logger.class);
        RuntimeException e = CbamUtils.buildFatalFailure(logger, "msg", expectedException);
        assertEquals("msg", e.getMessage());
        assertEquals(expectedException, e.getCause());
        verify(logger).error("msg", expectedException);
    }

    /**
     * test fatal failure handling with no wrapped exception
     */
    @Test
    public void testFatalFailureWithNoException() throws Exception {
        Logger logger = Mockito.mock(Logger.class);
        RuntimeException e = CbamUtils.buildFatalFailure(logger, "msg");
        assertEquals("msg", e.getMessage());
        verify(logger).error("msg");
    }

    @Test
    public void useStaticWay() {
        TestUtil.coveragePrivateConstructorForClassesWithStaticMethodsOnly(CbamUtils.class);
    }


}
