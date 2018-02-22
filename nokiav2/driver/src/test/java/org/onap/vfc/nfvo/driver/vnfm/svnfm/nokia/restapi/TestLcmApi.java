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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.restapi;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.JobManager;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.LifecycleManager;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;
import org.onap.vnfmdriver.model.VnfHealRequest;
import org.onap.vnfmdriver.model.VnfInstantiateRequest;
import org.onap.vnfmdriver.model.VnfScaleRequest;
import org.onap.vnfmdriver.model.VnfTerminateRequest;

import static org.apache.http.HttpStatus.SC_CREATED;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;


public class TestLcmApi extends TestBase {

    @Mock
    private LifecycleManager lifecycleManager;
    @Mock
    private JobManager jobManager;
    @InjectMocks
    private LcmApi lcmApi;

    @Before
    public void initMocks() throws Exception {
        setField(LcmApi.class, "logger", logger);
    }

    /**
     * test instantiation handled by LCM
     */
    @Test
    public void testInstantiation() {
        VnfInstantiateRequest req = new VnfInstantiateRequest();
        //when
        lcmApi.instantiateVnf(req, VNFM_ID, httpResponse);
        //verify
        verify(lifecycleManager).instantiate(VNFM_ID, req, httpResponse);
        verify(httpResponse).setStatus(SC_CREATED);
        verify(logger).info("REST: Instantiate VNF");
    }

    /**
     * test heal handled by LCM
     */
    @Test
    public void testHeal() {
        VnfHealRequest req = new VnfHealRequest();
        //when
        lcmApi.healVnf(req, VNFM_ID, VNF_ID, httpResponse);
        //verify
        verify(lifecycleManager).healVnf(VNFM_ID, VNF_ID, req, httpResponse);
        verify(logger).info("REST: Heal VNF");
    }

    /**
     * test query handled by LCM
     */
    @Test
    public void testQuery() {
        //when
        lcmApi.queryVnf(VNFM_ID, VNF_ID, httpResponse);
        //verify
        verify(lifecycleManager).queryVnf(VNFM_ID, VNF_ID);
        verify(logger).info("REST: Query VNF");

    }

    /**
     * test scale handled by LCM
     */
    @Test
    public void testScale() {
        VnfScaleRequest req = new VnfScaleRequest();
        //when
        lcmApi.scaleVnf(req, VNFM_ID, VNF_ID, httpResponse);
        //verify
        verify(lifecycleManager).scaleVnf(VNFM_ID, VNF_ID, req, httpResponse);
        verify(logger).info("REST: Scale VNF");

    }

    /**
     * test terminate handled by LCM
     */
    @Test
    public void testTerminate() {
        VnfTerminateRequest req = new VnfTerminateRequest();
        //when
        lcmApi.terminateVnf(req, VNFM_ID, VNF_ID, httpResponse);
        //verify
        verify(lifecycleManager).terminateVnf(VNFM_ID, VNF_ID, req, httpResponse);
        verify(logger).info("REST: Terminate VNF");

    }

    /**
     * test job query handled by job manager
     */
    @Test
    public void testJob() {
        //when
        lcmApi.getJob(VNFM_ID, JOB_ID, httpResponse);
        //verify
        verify(jobManager).getJob(VNFM_ID, JOB_ID);
        verify(logger).debug("REST: Query job");

    }
}
