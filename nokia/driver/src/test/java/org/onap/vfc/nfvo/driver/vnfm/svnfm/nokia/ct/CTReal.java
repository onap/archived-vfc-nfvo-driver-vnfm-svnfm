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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.ct;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.NokiaSvnfmApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

//@Ignore
@RunWith(value = SpringRunner.class)
@SpringBootTest(classes = NokiaSvnfmApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("real")
public class CTReal {

    @Test
    public void testBasicWorkflow() throws Exception {
        Thread.sleep(10000000 * 1000L);
    }

}
