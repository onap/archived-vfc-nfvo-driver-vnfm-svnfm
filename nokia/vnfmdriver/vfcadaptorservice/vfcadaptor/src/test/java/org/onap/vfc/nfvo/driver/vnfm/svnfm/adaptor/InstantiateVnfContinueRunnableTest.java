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
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMInstantiateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMInstantiateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMModifyVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMModifyVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMQueryOperExecutionResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.ComputeResource;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.VnfcResourceInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.inf.CbamMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonEnum.OperationStatus;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.bean.VnfmJobExecutionInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.mapper.VnfcResourceInfoMapper;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.mapper.VnfmJobExecutionMapper;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.NslcmGrantVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.NslcmGrantVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.AffectedVnfc;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.inf.NslcmMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.InstantiateVnfRequest;

public class InstantiateVnfContinueRunnableTest{
	@InjectMocks
	private InstantiateVnfContinueRunnable instantiateVnfContinueRunnable;
	
	@Mock
	private CbamMgmrInf cbamMgmr;
	
	@Mock
	private NslcmMgmrInf nslcmMgmr;
	
	@Mock
	private VnfmJobExecutionMapper jobDbMgmr;
	
	@Mock
	private VnfcResourceInfoMapper vnfcDbMgmr;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		InstantiateVnfRequest driverRequest = new InstantiateVnfRequest();
		Driver2CbamRequestConverter requestConverter = new Driver2CbamRequestConverter();
		
		instantiateVnfContinueRunnable.setDriverRequest(driverRequest);
		instantiateVnfContinueRunnable.setJobId("1");
		instantiateVnfContinueRunnable.setVnfInstanceId("001");
		instantiateVnfContinueRunnable.setVnfmId("001");
		instantiateVnfContinueRunnable.setRequestConverter(requestConverter);
		
		NslcmGrantVnfResponse grantResponse = new NslcmGrantVnfResponse();
		CBAMInstantiateVnfResponse cbamResponse = new CBAMInstantiateVnfResponse();
		cbamResponse.setId("1");
		CBAMModifyVnfResponse modifyResponse = new CBAMModifyVnfResponse();
		modifyResponse.setId("2");
		VnfmJobExecutionInfo execInfo = new VnfmJobExecutionInfo();
		execInfo.setJobId(1L);
		
		CBAMQueryOperExecutionResponse exeResponse = new CBAMQueryOperExecutionResponse();
		exeResponse.setStatus(OperationStatus.FINISHED);
		
		List<VnfcResourceInfo> vnfcResources = new ArrayList<>();
		VnfcResourceInfo res = new VnfcResourceInfo();
		res.setId("1");
		res.setVduId("vduId");
		ComputeResource computeResource = new ComputeResource();
		computeResource.setResourceId("resourceId");
		computeResource.setResourceType("OS::Nova::Server");
		computeResource.setVimId("vimId");
		res.setComputeResource(computeResource);
		
		vnfcResources.add(res);
		
		when(nslcmMgmr.grantVnf(Mockito.any(NslcmGrantVnfRequest.class))).thenReturn(grantResponse);
		when(cbamMgmr.instantiateVnf(Mockito.any(CBAMInstantiateVnfRequest.class), Mockito.anyString())).thenReturn(cbamResponse);
		when(cbamMgmr.modifyVnf(Mockito.any(CBAMModifyVnfRequest.class), Mockito.anyString())).thenReturn(modifyResponse);
		when(cbamMgmr.queryOperExecution(Mockito.anyString())).thenReturn(exeResponse);
		when(cbamMgmr.queryVnfcResource(Mockito.anyString())).thenReturn(vnfcResources);
		when(jobDbMgmr.findOne(Mockito.anyLong())).thenReturn(execInfo);
		doNothing().when(jobDbMgmr).update(Mockito.any(VnfmJobExecutionInfo.class));
		doNothing().when(vnfcDbMgmr).insert(Mockito.any(AffectedVnfc.class));
	}
	
	@Test
	public void testRun()
	{
		instantiateVnfContinueRunnable.run();
	}
}
