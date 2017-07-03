/*
 * Copyright 2016 Huawei Technologies Co., Ltd.
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

package org.openo.nfvo.vnfmadapter.common.servicetoken;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.openo.baseservice.roa.util.restclient.Restful;

/**
 * <br/>
 * <p>
 * </p>
 *
 * @author
 * @version NFVO 0.5 Aug 10, 2016
 */
public class HttpRestfulHelpTest {

    @Test
    public void testGetRestInstance() {
        Restful rest = HttpRestfulHelp.getRestInstance(null, null);
        assertNotNull(rest);
    }

    @Test
    public void testGetRestInstance1() {
        HttpRestfulHelp.getRestInstance(null, null);
        Restful rest = HttpRestfulHelp.getRestInstance(null, null);
        assertNotNull(rest);
    }
}
