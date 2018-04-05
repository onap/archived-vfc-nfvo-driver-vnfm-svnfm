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

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.so.SoLifecycleManager;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;
import org.onap.vnfmadapter.so.model.*;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;


public class TestSoApi extends TestBase {

    @Mock
    private SoLifecycleManager soLifecycleManager;
    @InjectMocks
    private SoApi soApi;
    @Mock
    private SoJobHandler jobHandler;

    @Before
    public void initMocks() throws Exception {
        setField(SoApi.class, "logger", logger);
    }

    /**
     * test create
     */
    @Test
    public void testCreate() {
        SoVnfCreationRequest soRequest = Mockito.mock(SoVnfCreationRequest.class);
        SoVnfCreationResponse vnf = new SoVnfCreationResponse();
        when(soLifecycleManager.create(VNFM_ID, soRequest)).thenReturn(vnf);
        //when
        SoVnfCreationResponse actual = soApi.createVnf(soRequest, VNFM_ID, httpResponse);
        //verify
        verify(logger).info("REST: Create the VNF");
        TestCase.assertEquals(vnf, actual);
    }

    /**
     * test activation
     */
    @Test
    public void testActivate() {
        SoVnfActivationRequest soRequest = Mockito.mock(SoVnfActivationRequest.class);
        when(soLifecycleManager.activate(VNFM_ID, VNF_ID, soRequest, httpResponse)).thenReturn(jobHandler);
        //when
        SoJobHandler soJobHandler = soApi.activateVnf(soRequest, VNFM_ID, VNF_ID, httpResponse);
        //verify
        verify(logger).info("REST: Activate the VNF");
        TestCase.assertEquals(jobHandler, soJobHandler);
    }

    /**
     * test scale
     */
    @Test
    public void testScale() {
        SoVnfScaleRequest soRequest = Mockito.mock(SoVnfScaleRequest.class);
        when(soLifecycleManager.scale(VNFM_ID, VNF_ID, soRequest, httpResponse)).thenReturn(jobHandler);
        //when
        SoJobHandler soJobHandler = soApi.scaleVnf(soRequest, VNFM_ID, VNF_ID, httpResponse);
        //verify
        verify(logger).info("REST: Scale the VNF");
        TestCase.assertEquals(jobHandler, soJobHandler);
    }

    /**
     * test heal
     */
    @Test
    public void testHeal() {
        SoVnfHealRequest soRequest = Mockito.mock(SoVnfHealRequest.class);
        when(soLifecycleManager.heal(VNFM_ID, VNF_ID, soRequest, httpResponse)).thenReturn(jobHandler);
        //when
        SoJobHandler soJobHandler = soApi.healVnf(soRequest, VNFM_ID, VNF_ID, httpResponse);
        //verify
        verify(logger).info("REST: Heal the VNF");
        TestCase.assertEquals(jobHandler, soJobHandler);
    }


    /**
     * test custom
     */
    @Test
    public void testCustom() {
        SoVnfCustomOperation soRequest = Mockito.mock(SoVnfCustomOperation.class);
        when(soLifecycleManager.customOperation(VNFM_ID, VNF_ID, soRequest, httpResponse)).thenReturn(jobHandler);
        //when
        SoJobHandler soJobHandler = soApi.executeCustomOperation(soRequest, VNFM_ID, VNF_ID, httpResponse);
        //verify
        verify(logger).info("REST: Execute custom operation on the VNF");
        TestCase.assertEquals(jobHandler, soJobHandler);
    }

    /**
     * test deactivation
     */
    @Test
    public void testDeactivation() {
        SoVnfTerminationRequest soRequest = Mockito.mock(SoVnfTerminationRequest.class);
        when(soLifecycleManager.deactivate(VNFM_ID, VNF_ID, soRequest, httpResponse)).thenReturn(jobHandler);
        //when
        SoJobHandler soJobHandler = soApi.deactivateVnf(soRequest, VNFM_ID, VNF_ID, httpResponse);
        //verify
        verify(logger).info("REST: Deactivate the VNF");
        TestCase.assertEquals(jobHandler, soJobHandler);
    }

    /**
     * test delete
     */
    @Test
    public void testDelete() {
        //when
        soApi.deleteVnf(VNFM_ID, VNF_ID, httpResponse);
        //verify
        verify(logger).info("REST: Delete the VNF");
        verify(soLifecycleManager).delete(VNFM_ID, VNF_ID);
    }

    /**
     * test deactivation
     */
    @Test
    public void testGetJob() {
        SoJobDetail jobDetail = new SoJobDetail();
        when(soLifecycleManager.getJobDetails(VNFM_ID, JOB_ID)).thenReturn(jobDetail);
        //when
        SoJobDetail actial = soApi.getJob(VNFM_ID, JOB_ID, httpResponse);
        //verify
        verify(logger).trace("REST: Query the job");
        TestCase.assertEquals(jobDetail, actial);
    }

}
