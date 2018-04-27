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


import com.google.common.collect.Lists;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class MultiExceptionTest {
    @Test
    public void testConstruction() {
        Exception c1 = new Exception();
        Exception c2 = new Exception();
        //when
        MultiException e = new MultiException("msg", c1, c2);
        //verify
        assertEquals("msg", e.getMessage());
        assertEquals(c1, e.getCauses().get(0));
        assertEquals(c2, e.getCauses().get(1));
        assertEquals(2, e.getCauses().size());
    }

    @Test
    public void testConstruction2() {
        Exception c1 = new Exception();
        Exception c2 = new Exception();
        //when
        MultiException e = new MultiException("msg", Lists.newArrayList(c1, c2));
        //verify
        assertEquals("msg", e.getMessage());
        assertEquals(c1, e.getCauses().get(0));
        assertEquals(c2, e.getCauses().get(1));
        assertEquals(2, e.getCauses().size());
    }
}