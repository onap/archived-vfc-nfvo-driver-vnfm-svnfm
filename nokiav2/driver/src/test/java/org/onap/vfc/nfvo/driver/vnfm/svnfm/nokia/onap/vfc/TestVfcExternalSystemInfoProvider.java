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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.vfc;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;
import org.onap.vnfmdriver.model.VimInfo;
import org.onap.vnfmdriver.model.VnfmInfo;
import org.springframework.test.util.ReflectionTestUtils;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestVfcExternalSystemInfoProvider extends TestBase {
    private VfcExternalSystemInfoProvider vfcExternalSystemInfoProvider;

    @Before
    public void init() {
        vfcExternalSystemInfoProvider = new VfcExternalSystemInfoProvider(environment, vfcRestApiProvider);
        ReflectionTestUtils.setField(VfcExternalSystemInfoProvider.class, "logger", logger);
    }

    /**
     * VIM is queried using VF-C APIs
     */
    @Test
    public void testVimRetrieval() throws Exception {
        VimInfo expectedVimInfo = new VimInfo();
        when(nsLcmApi.queryVIMInfo(VIM_ID)).thenReturn(buildObservable(expectedVimInfo));
        //when
        VimInfo vimInfo = vfcExternalSystemInfoProvider.getVimInfo(VIM_ID);
        //verify
        assertEquals(expectedVimInfo, vimInfo);
    }

    /**
     * failure to retrieve VIM from VF-C is propagated
     */
    @Test
    public void testUnableToQueryVim() throws Exception {
        RuntimeException expectedException = new RuntimeException();
        when(nsLcmApi.queryVIMInfo(VIM_ID)).thenThrow(expectedException);
        //when
        try {
            vfcExternalSystemInfoProvider.getVimInfo(VIM_ID);
            fail();
        } catch (Exception e) {
            assertEquals("Unable to query VIM from VF-C with " + VIM_ID + " identifier", e.getMessage());
            verify(logger).error("Unable to query VIM from VF-C with " + VIM_ID + " identifier", expectedException);
        }
    }

    /**
     * VNFM is queried using VF-C APIs
     */
    @Test
    public void testVnfmRetrieval() throws Exception {
        VnfmInfo expectedVimInfo = new VnfmInfo();
        when(nsLcmApi.queryVnfmInfo(VNFM_ID)).thenReturn(buildObservable(expectedVimInfo));
        //when
        VnfmInfo vimInfo = vfcExternalSystemInfoProvider.queryVnfmInfoFromSource(VNFM_ID);
        //verify
        assertEquals(expectedVimInfo, vimInfo);
    }

    /**
     * failure to retrieve VNFM from VF-C is propagated
     */
    @Test
    public void testUnableToQueryVnfm() throws Exception {
        RuntimeException expectedException = new RuntimeException();
        when(nsLcmApi.queryVnfmInfo(VNFM_ID)).thenThrow(expectedException);
        //when
        try {
            vfcExternalSystemInfoProvider.queryVnfmInfoFromSource(VNFM_ID);
            fail();
        } catch (Exception e) {
            assertEquals("Unable to query VNFM from VF-C with myVnfmId identifier", e.getMessage());
            verify(logger).error("Unable to query VNFM from VF-C with myVnfmId identifier", expectedException);
        }
    }

    /**
     * The VNFM identifier is loaded from property files
     */
    @Test
    public void testGetVnfms() {
        setFieldWithPropertyAnnotation(vfcExternalSystemInfoProvider, "${vnfmId}", "myVnfmId");
        assertEquals(Sets.newHashSet("myVnfmId"), vfcExternalSystemInfoProvider.getVnfms());
    }
}
