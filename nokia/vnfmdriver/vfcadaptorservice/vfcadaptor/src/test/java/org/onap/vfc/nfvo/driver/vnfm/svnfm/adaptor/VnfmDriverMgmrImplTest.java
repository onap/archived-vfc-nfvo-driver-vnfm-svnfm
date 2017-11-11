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

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.aai.bo.AaiVnfmInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.aai.bo.entity.EsrSystemInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.aai.inf.AaiMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.catalog.inf.CatalogMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMCreateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMCreateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMHealVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMHealVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMQueryOperExecutionResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMQueryVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMScaleVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMScaleVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.inf.CbamMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.common.bo.AdaptorEnv;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonEnum;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.ScaleType;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.bean.VnfmJobExecutionInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.repository.VnfmJobExecutionRepository;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.exception.VnfmDriverException;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.inf.NslcmMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.HealVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.HealVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.InstantiateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.InstantiateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.OperStatusVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.QueryVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.ScaleVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.ScaleVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.TerminateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.TerminateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.inf.VnfContinueProcessorInf;

public class VnfmDriverMgmrImplTest {

	@InjectMocks
	private VnfmDriverMgmrImpl vnfmDriverMgmr = new VnfmDriverMgmrImpl();
	
	@Mock
	private CbamMgmrInf cbamMgmr;
	
	@Mock
	private CatalogMgmrInf catalogMgmr;
	
	@Mock
	private AaiMgmrInf aaiMgmr;
	
	@Mock
	private NslcmMgmrInf nslcmMgmr;;
	
	@Mock
	private VnfmJobExecutionRepository jobDbManager;
	
	@Mock
	private VnfContinueProcessorInf vnfContinueProcessorInf;
	
	@Mock
	AdaptorEnv adaptorEnv;
	
	@Rule
	public ExpectedException thrown= ExpectedException.none();
	
	private String vnfmId = "vnfmId_001";
	private String vnfInstanceId = "vnfInstanceId_001";
	
	private String protocol = "https";
	private String ip = "139.234.34.43";
	private String port = "99";
	private String cbamHttpHead;
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		cbamHttpHead = protocol + "://" + ip + ":" + port;
		
		EsrSystemInfo esrSystemInfo = new EsrSystemInfo();
		esrSystemInfo.setEsrSystemId("esrSystemId");
		esrSystemInfo.setPassword("password");
		esrSystemInfo.setServiceUrl(cbamHttpHead);
		esrSystemInfo.setType("type");
		esrSystemInfo.setUserName("userName");
		esrSystemInfo.setVendor("vendor");
		esrSystemInfo.setVersion("version");
		
		List<EsrSystemInfo> esrSystemInfoList = new ArrayList<EsrSystemInfo>();
		esrSystemInfoList.add(esrSystemInfo);
		AaiVnfmInfo mockVnfmInfo = new AaiVnfmInfo();
		mockVnfmInfo.setVnfmId(vnfmId);
		mockVnfmInfo.setVimId("vimId");
		mockVnfmInfo.setResourceVersion("resourceVersion");
		mockVnfmInfo.setCertificateUrl("certificateUrl");
		mockVnfmInfo.setEsrSystemInfoList(esrSystemInfoList);
		
		Driver2CbamRequestConverter reqConverter = new Driver2CbamRequestConverter();
		Cbam2DriverResponseConverter rspConverter = new Cbam2DriverResponseConverter();
		vnfmDriverMgmr.setRequestConverter(reqConverter);
		vnfmDriverMgmr.setResponseConverter(rspConverter);
		
		when(aaiMgmr.queryVnfm(vnfmId)).thenReturn(mockVnfmInfo);
		
		VnfmJobExecutionInfo execInfo = new VnfmJobExecutionInfo();
		execInfo.setJobId(100L);
		execInfo.setVnfmExecutionId("executionId_001");
		execInfo.setVnfInstanceId(vnfInstanceId);
		
		when(jobDbManager.save(Mockito.any(VnfmJobExecutionInfo.class))).thenReturn(execInfo);
	}
	
	@Test
	public void testBuildVnfmHttpPathById() throws ClientProtocolException, IOException {
		String vnfmHttpPathHead = vnfmDriverMgmr.buildVnfmHttpPathByRealId(vnfmId);
		Assert.assertEquals("result is ", cbamHttpHead, vnfmHttpPathHead);
	}
	
	@Test(expected = VnfmDriverException.class)
	public void testBuildVnfmHttpPathByIdException() throws ClientProtocolException, IOException{
		vnfmDriverMgmr.buildVnfmHttpPathByRealId(vnfmId + "001");
	}
	
	@Test
	public void testInstantiateVnf() throws ClientProtocolException, IOException {
		CBAMCreateVnfResponse mockCbamResponse = new CBAMCreateVnfResponse();
		mockCbamResponse.setId("executionId_001");
		
		when(cbamMgmr.createVnf(Mockito.any(CBAMCreateVnfRequest.class))).thenReturn(mockCbamResponse);
		InstantiateVnfRequest driverRequest = new InstantiateVnfRequest();
		InstantiateVnfResponse response = vnfmDriverMgmr.instantiateVnf(driverRequest, vnfmId);
	}
	
	@Test
	public void testTerminateVnf() throws ClientProtocolException, IOException {
		TerminateVnfRequest driverRequest = new TerminateVnfRequest();
		TerminateVnfResponse response = vnfmDriverMgmr.terminateVnf(driverRequest, vnfmId, vnfInstanceId);
	}
	
	@Test
	public void testHealVnf() throws ClientProtocolException, IOException {
		CBAMHealVnfResponse mockCbamResponse = new CBAMHealVnfResponse();
		mockCbamResponse.setId("executionId_001");
		when(cbamMgmr.healVnf(Mockito.any(CBAMHealVnfRequest.class), Mockito.anyString())).thenReturn(mockCbamResponse);
		HealVnfRequest request = new HealVnfRequest();
		HealVnfResponse response = vnfmDriverMgmr.healVnf(request, vnfmId, vnfInstanceId);
	}
	
	@Test
	public void testScaleVnf() throws ClientProtocolException, IOException {
		CBAMScaleVnfResponse mockCbamResponse = new CBAMScaleVnfResponse();
		mockCbamResponse.setId("executionId_001");
		when(cbamMgmr.scaleVnf(Mockito.any(CBAMScaleVnfRequest.class), Mockito.anyString())).thenReturn(mockCbamResponse);
		ScaleVnfRequest request = new ScaleVnfRequest();
		request.setType(ScaleType.SCALE_IN);
		ScaleVnfResponse response = vnfmDriverMgmr.scaleVnf(request, vnfmId, vnfInstanceId);
	}
	
	@Test
	public void testQueryVnf() throws ClientProtocolException, IOException {
		CBAMQueryVnfResponse mockCbamResponse = new CBAMQueryVnfResponse();
		mockCbamResponse.setId("executionId_001");
		mockCbamResponse.setVnfdId(vnfInstanceId);
		when(cbamMgmr.queryVnf(Mockito.anyString())).thenReturn(mockCbamResponse);
		QueryVnfResponse response = vnfmDriverMgmr.queryVnf(vnfmId, vnfInstanceId);
		Assert.assertEquals(vnfInstanceId, response.getVnfdId());
	}
	
	@Test
	public void testGetOperStatus() throws ClientProtocolException, IOException
	{
		VnfmJobExecutionInfo execInfo = new VnfmJobExecutionInfo();
		execInfo.setJobId(1L);
		execInfo.setVnfmExecutionId("executionId_001");
		when(jobDbManager.findOne(Mockito.anyLong())).thenReturn(execInfo);
		
		CBAMQueryOperExecutionResponse cbamResponse = new CBAMQueryOperExecutionResponse();
		cbamResponse.setId("executionId_001");
		cbamResponse.setStatus(CommonEnum.OperationStatus.STARTED);
		cbamResponse.setGrantId("001002001");
		
//		when(cbamMgmr.queryOperExecution(Mockito.anyString())).thenReturn(cbamResponse);
//		OperStatusVnfResponse response = vnfmDriverMgmr.getOperStatus(vnfmId, "1");
//		
//		Assert.assertEquals("executionId_001", response.getJobId());
	}

}
