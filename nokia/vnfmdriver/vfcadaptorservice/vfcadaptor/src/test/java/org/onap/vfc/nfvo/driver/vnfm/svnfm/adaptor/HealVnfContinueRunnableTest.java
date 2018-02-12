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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.adaptor;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMHealVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMHealVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.inf.CbamMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.bean.VnfmJobExecutionInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.mapper.VnfmJobExecutionMapper;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.NslcmGrantVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.NslcmGrantVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.inf.NslcmMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.HealVnfRequest;

public class HealVnfContinueRunnableTest {
	@InjectMocks
	private HealVnfContinueRunnable healVnfContinueRunnable;
	
	@Mock
	private CbamMgmrInf cbamMgmr;
	
	@Mock
	private NslcmMgmrInf nslcmMgmr;
	
	@Mock
	private VnfmJobExecutionMapper jobDbMgmr;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		HealVnfRequest driverRequest = new HealVnfRequest();
		Driver2CbamRequestConverter requestConverter = new Driver2CbamRequestConverter();
		String vnfInstanceId = "vnfInstanceId_001";
		String jobId = "001";
		String vnfmId = "vnfmId_001";
		
		healVnfContinueRunnable.setDriverRequest(driverRequest);
		healVnfContinueRunnable.setRequestConverter(requestConverter);
		healVnfContinueRunnable.setVnfInstanceId(vnfInstanceId);
		healVnfContinueRunnable.setJobId(jobId);
		healVnfContinueRunnable.setVnfmId(vnfmId);
		
		NslcmGrantVnfResponse grantResponse = new NslcmGrantVnfResponse();
		CBAMHealVnfResponse cbamResponse = new CBAMHealVnfResponse();
		cbamResponse.setId("1");
		VnfmJobExecutionInfo execInfo = new VnfmJobExecutionInfo();
		execInfo.setJobId(1L);
		
		when(nslcmMgmr.grantVnf(Mockito.any(NslcmGrantVnfRequest.class))).thenReturn(grantResponse);
		when(cbamMgmr.healVnf(Mockito.any(CBAMHealVnfRequest.class), Mockito.anyString())).thenReturn(cbamResponse);
		when(jobDbMgmr.findOne(Mockito.anyLong())).thenReturn(execInfo);
		doNothing().when(jobDbMgmr).update(Mockito.any(VnfmJobExecutionInfo.class));
	
	}
	
	@Test
	public void testRun()
	{
		healVnfContinueRunnable.run();
	}
}
