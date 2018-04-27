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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;
import org.onap.vnfmdriver.model.VimInfo;
import org.onap.vnfmdriver.model.VnfmInfo;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import static java.lang.Long.valueOf;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static org.mockito.Mockito.*;

public class TestGenericExternalSystemInfoProvider extends TestBase {

    private GenericExternalSystemInfoProvider genericExternalSystemInfoProvider;

    @Before
    public void init() {
        when(environment.getProperty(IpMappingProvider.IP_MAP, String.class, "")).thenReturn("");
        ReflectionTestUtils.setField(GenericExternalSystemInfoProvider.class, "logger", logger);
        genericExternalSystemInfoProvider = Mockito.spy(new TestClass(environment));
    }

    /**
     * the VNFM info is not retrieved within the cache eviction period
     */
    @Test
    public void testQueryVnfmInfoWithin() throws Exception {
        VnfmInfo expectedVnfmInfo = Mockito.mock(VnfmInfo.class);
        when(genericExternalSystemInfoProvider.queryVnfmInfoFromSource(VNFM_ID)).thenReturn(expectedVnfmInfo);
        when(environment.getProperty(GenericExternalSystemInfoProvider.VNFM_INFO_CACHE_EVICTION_IN_MS, Long.class, valueOf(GenericExternalSystemInfoProvider.DEFAULT_CACHE_EVICTION_TIMEOUT_IN_MS))).thenReturn(Long.valueOf(1234));
        genericExternalSystemInfoProvider.afterPropertiesSet();
        //when
        VnfmInfo vnfmInfo = genericExternalSystemInfoProvider.getVnfmInfo(VNFM_ID);
        //verify
        verify(logger).info("Querying VNFM info from source with " + VNFM_ID + " identifier");
        assertEquals(expectedVnfmInfo, vnfmInfo);
        //when
        VnfmInfo vnfmInfo2 = genericExternalSystemInfoProvider.getVnfmInfo(VNFM_ID);
        //verify source system not called again
        verify(logger).info("Querying VNFM info from source with " + VNFM_ID + " identifier");
        verify(genericExternalSystemInfoProvider, Mockito.times(1)).queryVnfmInfoFromSource(VNFM_ID);
    }

    /**
     * the VNFM info is retrieved without the cache eviction period
     */
    @Test
    //sleeping is required to make time pass (for cache to notice the change)
    //cache is configured with 1 ms cache eviction without sleep it is not
    //deterministic that at least 1 ms time will pass between calls
    @SuppressWarnings("squid:S2925")
    public void testQueryVnfmInfoOutside() throws Exception {
        VnfmInfo expectedVnfmInfo = Mockito.mock(VnfmInfo.class);
        when(genericExternalSystemInfoProvider.queryVnfmInfoFromSource(VNFM_ID)).thenReturn(expectedVnfmInfo);
        when(environment.getProperty(GenericExternalSystemInfoProvider.VNFM_INFO_CACHE_EVICTION_IN_MS, Long.class, valueOf(GenericExternalSystemInfoProvider.DEFAULT_CACHE_EVICTION_TIMEOUT_IN_MS))).thenReturn(Long.valueOf(1));
        genericExternalSystemInfoProvider.afterPropertiesSet();
        //when
        VnfmInfo vnfmInfo = genericExternalSystemInfoProvider.getVnfmInfo(VNFM_ID);
        //verify
        assertEquals(expectedVnfmInfo, vnfmInfo);
        //when
        Thread.sleep(10);
        VnfmInfo vnfmInfo2 = genericExternalSystemInfoProvider.getVnfmInfo(VNFM_ID);
        //verify source system called again
        verify(logger, times(2)).info("Querying VNFM info from source with " + VNFM_ID + " identifier");
        verify(genericExternalSystemInfoProvider, Mockito.times(2)).queryVnfmInfoFromSource(VNFM_ID);
    }

    /**
     * Unable to query VNFM results is propagated
     */
    @Test
    public void testUnableToQueryVnfmInfoProvider() throws Exception {
        class TestClass extends GenericExternalSystemInfoProvider {

            TestClass(Environment environment) {
                super(environment);
            }

            @Override
            public VnfmInfo queryVnfmInfoFromSource(String vnfmId) {
                throw new RuntimeException();
            }

            @Override
            public VimInfo getVimInfo(String vimId) {
                return null;
            }

            @Override
            public Set<String> getVnfms() {
                return null;
            }
        }
        try {
            new TestClass(null).getVnfmInfo(VNFM_ID);
            fail();
        } catch (Exception e) {
            assertEquals("Unable to query VNFM info for myVnfmId", e.getMessage());
            verify(logger).error(eq("Unable to query VNFM info for myVnfmId"), any(RuntimeException.class));
        }
    }

    class TestClass extends GenericExternalSystemInfoProvider {

        TestClass(Environment environment) {
            super(environment);
        }

        @Override
        public VnfmInfo queryVnfmInfoFromSource(String vnfmId) {
            return null;
        }

        @Override
        public VimInfo getVimInfo(String vimId) {
            return null;
        }

        @Override
        public Set<String> getVnfms() {
            return null;
        }
    }
}
