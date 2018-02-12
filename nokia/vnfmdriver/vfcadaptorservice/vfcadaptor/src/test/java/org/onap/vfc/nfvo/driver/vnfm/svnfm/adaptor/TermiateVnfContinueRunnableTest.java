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

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMModifyVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMQueryOperExecutionResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMQueryVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMTerminateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMTerminateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.ComputeResource;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.VnfcResourceInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.inf.CbamMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonEnum;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonEnum.OperationStatus;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.bean.VnfmJobExecutionInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.mapper.VnfcResourceInfoMapper;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.mapper.VnfmJobExecutionMapper;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.NslcmGrantVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.NslcmGrantVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.AffectedVnfc;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.inf.NslcmMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.TerminateVnfRequest;

public class TermiateVnfContinueRunnableTest{
	@InjectMocks
	private TerminateVnfContinueRunnable terminateVnfContinueRunnable;
	
	@Mock
	private CbamMgmrInf cbamMgmr;
	
	@Mock
	private NslcmMgmrInf nslcmMgmr;
	
	@Mock
	private VnfmJobExecutionMapper jobDbMgmr;
	
	@Mock
	private VnfcResourceInfoMapper vnfcDbMgmr;;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		TerminateVnfRequest driverRequest = new TerminateVnfRequest();
		Driver2CbamRequestConverter requestConverter = new Driver2CbamRequestConverter();
		
		terminateVnfContinueRunnable.setDriverRequest(driverRequest);
		terminateVnfContinueRunnable.setJobId("1");
		terminateVnfContinueRunnable.setVnfInstanceId("001");
		terminateVnfContinueRunnable.setVnfmId("001");
		terminateVnfContinueRunnable.setRequestConverter(requestConverter);
		
		NslcmGrantVnfResponse grantResponse = new NslcmGrantVnfResponse();
		CBAMTerminateVnfResponse cbamResponse = new CBAMTerminateVnfResponse();
		cbamResponse.setId("1");
		CBAMModifyVnfResponse modifyResponse = new CBAMModifyVnfResponse();
		modifyResponse.setId("2");
		VnfmJobExecutionInfo execInfo = new VnfmJobExecutionInfo();
		execInfo.setJobId(1L);
		
		CBAMQueryOperExecutionResponse exeResponse = new CBAMQueryOperExecutionResponse();
		exeResponse.setStatus(OperationStatus.FINISHED);
		
		List<AffectedVnfc> vnfcResources = new ArrayList<>();
		
		CBAMQueryVnfResponse cQueryVnfResponse = new CBAMQueryVnfResponse();
		cQueryVnfResponse.setInstantiationState(CommonEnum.InstantiationState.NOT_INSTANTIATED);
		
		when(nslcmMgmr.grantVnf(Mockito.any(NslcmGrantVnfRequest.class))).thenReturn(grantResponse);
		when(cbamMgmr.terminateVnf(Mockito.any(CBAMTerminateVnfRequest.class), Mockito.anyString())).thenReturn(cbamResponse);
		when(cbamMgmr.queryVnf(Mockito.anyString())).thenReturn(cQueryVnfResponse);
		doNothing().when(cbamMgmr).deleteVnf(Mockito.anyString());
		
		when(vnfcDbMgmr.getAllByInstanceId(Mockito.anyString())).thenReturn(vnfcResources);
		when(cbamMgmr.queryOperExecution(Mockito.anyString())).thenReturn(exeResponse);
		when(jobDbMgmr.findOne(Mockito.anyLong())).thenReturn(execInfo);
		doNothing().when(jobDbMgmr).update(Mockito.any(VnfmJobExecutionInfo.class));
	}
	
	@Test
	public void testRun()
	{
		terminateVnfContinueRunnable.run();
	}
}
