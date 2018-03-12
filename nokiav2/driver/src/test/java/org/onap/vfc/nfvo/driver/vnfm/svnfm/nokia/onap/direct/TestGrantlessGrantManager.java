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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct;

import org.junit.Before;
import org.junit.Test;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;
import org.onap.vnfmdriver.model.GrantVNFResponseVim;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class TestGrantlessGrantManager extends TestBase {
    private GrantlessGrantManager grantlessGrantManager;

    @Before
    public void init() {
        grantlessGrantManager = new GrantlessGrantManager();
        setField(GrantlessGrantManager.class, "logger", logger);
    }

    /**
     * grant is only logged for healing
     */
    @Test
    public void testGrantForHeal() throws Exception {
        //when
        grantlessGrantManager.requestGrantForHeal(null, null, null, null, null, null);
        //verify
        verify(logger).info("No grant is requested in direct mode");
    }

    /**
     * grant is only logged for instantiation
     */
    @Test
    public void testGrantForInstantiate() throws Exception {
        //when
        GrantVNFResponseVim grant = grantlessGrantManager.requestGrantForInstantiate(null, null, VIM_ID, null, null, null, null);
        //verify
        verify(logger).info("No grant is requested in direct mode");
        assertEquals(VIM_ID, grant.getVimId());
    }

    /**
     * grant is only logged for scaling
     */
    @Test
    public void testGrantForScaling() throws Exception {
        //when
        grantlessGrantManager.requestGrantForScale(null, null, null, null, null, null);
        //verify
        verify(logger).info("No grant is requested in direct mode");
    }

    /**
     * grant is only logged for termination
     */
    @Test
    public void testGrantForTerminate() throws Exception {
        //when
        grantlessGrantManager.requestGrantForTerminate(null, null, null, null, null, null);
        //verify
        verify(logger).info("No grant is requested in direct mode");
    }
}
