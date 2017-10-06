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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonConstants;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.repository.VnfmJobExecutionRepository;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.exception.VnfmDriverException;
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
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.inf.VnfmDriverMgmrInf;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class VnfmDriverControllerTest {
	@Mock
	private VnfmDriverMgmrInf vnfmDriverMgmr;

	@InjectMocks
	private VnfmDriverController controller;

	private MockMvc mockMvc;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	}

	@Test
	public void testInstantiateVnf() throws Exception {
		InstantiateVnfResponse mockResponse = new InstantiateVnfResponse();
		mockResponse.setJobId("job_001");
		mockResponse.setVnfInstanceId("vnfInstanceId_001");
		String jsonString = "{\"vnfInstanceName\":\"vnfInstanceName_001\",\"vnfPackageId\":\"1\"}";
		
		when(vnfmDriverMgmr.instantiateVnf(Mockito.any(InstantiateVnfRequest.class), Mockito.anyString())).thenReturn(mockResponse);
		
		String responseString = mockMvc.perform(
				post("/api/nokiavnfmdriver/v1/vnfmId_001/vnfs").
				characterEncoding("UTF-8").
				accept(MediaType.APPLICATION_JSON).
				contentType(MediaType.APPLICATION_JSON).
				content(jsonString))
		.andDo(print())
		.andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
		
		JSONObject jsonObj = new JSONObject(responseString);
		Assert.assertEquals("jobId is ", mockResponse.getJobId(), jsonObj.get("jobId"));
		Assert.assertEquals("vnfInstanceId is ", mockResponse.getVnfInstanceId(), jsonObj.get("vnfInstanceId"));
	}
	
	@Test
	public void testTerminateVnf() throws Exception {
		TerminateVnfResponse mockResponse = new TerminateVnfResponse();
		mockResponse.setJobId("job_002");
		String jsonString = "{\"vnfInstanceName\":\"vnfInstanceName_001\",\"vnfPackageId\":\"1\"}";
		
		when(vnfmDriverMgmr.terminateVnf(Mockito.any(TerminateVnfRequest.class), Mockito.anyString(), Mockito.anyString())).thenReturn(mockResponse);
		
		String responseString = mockMvc.perform(
				post("/api/nokiavnfmdriver/v1/vnfmId_002/vnfs/vnfInstanceId_002/terminate").
				characterEncoding("UTF-8").
				accept(MediaType.APPLICATION_JSON).
				contentType(MediaType.APPLICATION_JSON).
				content(jsonString))
				.andDo(print())
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		
		JSONObject jsonObj = new JSONObject(responseString);
		Assert.assertEquals("jobId is ", mockResponse.getJobId(), jsonObj.get("jobId"));
	}
	
	@Test
	public void testTerminateVnfException() throws Exception {
		String jsonString = "{\"vnfInstanceName\":\"vnfInstanceName_001\",\"vnfPackageId\":\"1\"}";
		VnfmDriverException exception = new VnfmDriverException(HttpStatus.SC_BAD_REQUEST, "vnfInstanceId is wrong");
		when(vnfmDriverMgmr.terminateVnf(Mockito.any(TerminateVnfRequest.class), Mockito.anyString(), Mockito.anyString())).thenThrow(exception);
		
		String erroMsg = mockMvc.perform(
				post("/api/nokiavnfmdriver/v1/vnfmId_002/vnfs/vnfInstanceId_002/terminate").
				characterEncoding("UTF-8").
				accept(MediaType.APPLICATION_JSON).
				contentType(MediaType.APPLICATION_JSON).
				content(jsonString))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andReturn().getResponse().getErrorMessage();
		
		Assert.assertEquals("Error Message is ", exception.getMessage(), erroMsg);
	}
	
	@Test
	public void testGetOperStatus() throws Exception {
		OperStatusVnfResponse mockResponse = new OperStatusVnfResponse();
		mockResponse.setJobId("jobId_003");
		when(vnfmDriverMgmr.getOperStatus(Mockito.anyString(), Mockito.anyString())).thenReturn(mockResponse);
		
		String responseString = mockMvc.perform(
				get("/api/nokiavnfmdriver/v1/vnfmId_002/jobs/jobId_001").
				characterEncoding("UTF-8").
				accept(MediaType.APPLICATION_JSON).
				contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		
				JSONObject jsonObj = new JSONObject(responseString);
				Assert.assertEquals("jobId is ", mockResponse.getJobId(), jsonObj.get("jobId"));
	}
	
	@Test
	public void testGetOperStatusException() throws Exception {
		VnfmDriverException exception = new VnfmDriverException(HttpStatus.SC_INTERNAL_SERVER_ERROR, CommonConstants.HTTP_ERROR_DESC_500);
		when(vnfmDriverMgmr.getOperStatus( Mockito.anyString(), Mockito.anyString())).thenThrow(exception);
		
		String erroMsg = mockMvc.perform(
				get("/api/nokiavnfmdriver/v1/vnfmId_002/jobs/jobId_002").
				characterEncoding("UTF-8").
				accept(MediaType.APPLICATION_JSON).
				contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isInternalServerError())
				.andReturn().getResponse().getErrorMessage();
		
		Assert.assertEquals("Error Message is ", exception.getMessage(), erroMsg);
	}
	
	@Test
	public void testQueryVnf() throws Exception {
		QueryVnfResponse mockResponse = new QueryVnfResponse();
		mockResponse.setVnfdId("vnfdId_001");
		when(vnfmDriverMgmr.queryVnf(Mockito.anyString(), Mockito.anyString())).thenReturn(mockResponse);
		
		String responseString = mockMvc.perform(
				get("/api/nokiavnfmdriver/v1/vnfmId_002/vnfs/vnfInstanceId_001").
				characterEncoding("UTF-8").
				accept(MediaType.APPLICATION_JSON).
				contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		
				JSONObject jsonObj = new JSONObject(responseString);
				Assert.assertEquals("VnfdId is ", mockResponse.getVnfdId(), jsonObj.get("vnfdId"));
	}
	
	@Test
	public void testQueryVnfException() throws Exception {
		VnfmDriverException exception = new VnfmDriverException(HttpStatus.SC_INTERNAL_SERVER_ERROR, CommonConstants.HTTP_ERROR_DESC_500);
		when(vnfmDriverMgmr.queryVnf( Mockito.anyString(), Mockito.anyString())).thenThrow(exception);
		
		String erroMsg = mockMvc.perform(
				get("/api/nokiavnfmdriver/v1/vnfmId_002/vnfs/vnfInstanceId_001").
				characterEncoding("UTF-8").
				accept(MediaType.APPLICATION_JSON).
				contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isInternalServerError())
				.andReturn().getResponse().getErrorMessage();
		
		Assert.assertEquals("Error Message is ", exception.getMessage(), erroMsg);
	}
	
	@Test
	public void testScaleVnf() throws Exception {
		ScaleVnfResponse mockResponse = new ScaleVnfResponse();
		mockResponse.setJobId("job_002");
		String jsonString = "{\"vnfInstanceId\":\"vnfInstanceId_003\",\"vnfPackageId\":\"1\"}";
		
		when(vnfmDriverMgmr.scaleVnf(Mockito.any(ScaleVnfRequest.class), Mockito.anyString(), Mockito.anyString())).thenReturn(mockResponse);
		
		String responseString = mockMvc.perform(
				post("/api/nokiavnfmdriver/v1/vnfmId_002/vnfs/vnfInstanceId_003/scale").
				characterEncoding("UTF-8").
				accept(MediaType.APPLICATION_JSON).
				contentType(MediaType.APPLICATION_JSON).
				content(jsonString))
				.andDo(print())
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		
		JSONObject jsonObj = new JSONObject(responseString);
		Assert.assertEquals("jobId is ", mockResponse.getJobId(), jsonObj.get("jobId"));
	}
	
	@Test
	public void testScaleVnfException() throws Exception {
		String jsonString = "{\"vnfInstanceName\":\"vnfInstanceName_001\",\"vnfPackageId\":\"1\"}";
		VnfmDriverException exception = new VnfmDriverException(HttpStatus.SC_INTERNAL_SERVER_ERROR, CommonConstants.HTTP_ERROR_DESC_500);
		when(vnfmDriverMgmr.scaleVnf(Mockito.any(ScaleVnfRequest.class), Mockito.anyString(), Mockito.anyString())).thenThrow(exception);
		
		String erroMsg = mockMvc.perform(
				post("/api/nokiavnfmdriver/v1/vnfmId_002/vnfs/vnfInstanceId_002/scale").
				characterEncoding("UTF-8").
				accept(MediaType.APPLICATION_JSON).
				contentType(MediaType.APPLICATION_JSON).
				content(jsonString))
				.andDo(print())
				.andExpect(status().isInternalServerError())
				.andReturn().getResponse().getErrorMessage();
		
		Assert.assertEquals("Error Message is ", exception.getMessage(), erroMsg);
	}
	@Test
	public void testHealVnf() throws Exception {
		HealVnfResponse mockResponse = new HealVnfResponse();
		mockResponse.setJobId("job_002");
		String jsonString = "{\"vnfInstanceId\":\"vnfInstanceId_003\",\"vnfPackageId\":\"1\"}";
		
		when(vnfmDriverMgmr.healVnf(Mockito.any(HealVnfRequest.class), Mockito.anyString(), Mockito.anyString())).thenReturn(mockResponse);
		
		String responseString = mockMvc.perform(
				post("/api/nokiavnfmdriver/v1/vnfmId_002/vnfs/vnfInstanceId_003/heal").
				characterEncoding("UTF-8").
				accept(MediaType.APPLICATION_JSON).
				contentType(MediaType.APPLICATION_JSON).
				content(jsonString))
				.andDo(print())
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		
		JSONObject jsonObj = new JSONObject(responseString);
		Assert.assertEquals("jobId is ", mockResponse.getJobId(), jsonObj.get("jobId"));
	}
	
	@Test
	public void testHealVnfException() throws Exception {
		String jsonString = "{\"vnfInstanceName\":\"vnfInstanceName_001\",\"vnfPackageId\":\"1\"}";
		VnfmDriverException exception = new VnfmDriverException(HttpStatus.SC_INTERNAL_SERVER_ERROR, CommonConstants.HTTP_ERROR_DESC_500);
		when(vnfmDriverMgmr.healVnf(Mockito.any(HealVnfRequest.class), Mockito.anyString(), Mockito.anyString())).thenThrow(exception);
		
		String erroMsg = mockMvc.perform(
				post("/api/nokiavnfmdriver/v1/vnfmId_002/vnfs/vnfInstanceId_002/heal").
				characterEncoding("UTF-8").
				accept(MediaType.APPLICATION_JSON).
				contentType(MediaType.APPLICATION_JSON).
				content(jsonString))
				.andDo(print())
				.andExpect(status().isInternalServerError())
				.andReturn().getResponse().getErrorMessage();
		
		Assert.assertEquals("Error Message is ", exception.getMessage(), erroMsg);
	}

}
