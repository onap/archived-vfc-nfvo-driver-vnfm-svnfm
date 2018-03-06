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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestUserInvisibleError {

    /**
     * test POJO
     */
    @Test
    public void testPojo() throws Exception {
        UserInvisibleError e = new UserInvisibleError("msg");
        assertEquals("msg", e.getMessage());
        Exception cause = new Exception();
        UserInvisibleError e2 = new UserInvisibleError("msg", cause);
        assertEquals("msg", e2.getMessage());
        assertEquals(cause, e2.getCause());
    }

}