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

package com.nokia.vfcadaptor.vnfmdriver.controller;


import static org.mockito.Mockito.when;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.nokia.vfcadaptor.exception.VnfmDriverException;
import com.nokia.vfcadaptor.vnfmdriver.bo.InstantiateVnfRequest;
import com.nokia.vfcadaptor.vnfmdriver.bo.InstantiateVnfResponse;
import com.nokia.vfcadaptor.vnfmdriver.bo.TerminateVnfRequest;
import com.nokia.vfcadaptor.vnfmdriver.bo.TerminateVnfResponse;
import com.nokia.vfcadaptor.vnfmdriver.inf.VnfmDriverMgmrInf;

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations={"file:src/main/webapp/WEB-INF/mvc-servlet.xml"})
//@WebAppConfiguration(value = "src/main/webapp")
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
	public void testInstantiateVnf() throws Exception
	{
		InstantiateVnfResponse mockResponse = new InstantiateVnfResponse();
		mockResponse.setJobId("job_001");
		mockResponse.setVnfInstanceId("vnfInstanceId_001");
		String jsonString = "{\"vnfInstanceName\":\"vnfInstanceName_001\",\"vnfPackageId\":\"1\"}";
		when(vnfmDriverMgmr.instantiateVnf(Mockito.any(InstantiateVnfRequest.class), Mockito.anyString())).thenReturn(mockResponse);
		
		String responseString = mockMvc.perform(
				post("/nokiavnfm/v1/vnfmId_001/vnfs").
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
	public void testTerminateVnfSuccess() throws Exception
	{
		TerminateVnfResponse mockResponse = new TerminateVnfResponse();
		mockResponse.setJobId("job_002");
		String jsonString = "{\"vnfInstanceId\":\"vnfInstanceId_001\"}";
		when(vnfmDriverMgmr.terminateVnf(Mockito.any(TerminateVnfRequest.class), Mockito.anyString(), Mockito.anyString())).thenReturn(mockResponse);
		
		String responseString = mockMvc.perform(
				post("/nokiavnfm/v1/vnfmId_001/vnfs/vnfInstanceId_001/terminate").
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
	public void testTerminateVnfException() throws Exception
	{
		TerminateVnfResponse mockResponse = new TerminateVnfResponse();
		mockResponse.setJobId("job_002");
		String jsonString = "{\"vnfInstanceId\":\"vnfInstanceId_001\"}";
		VnfmDriverException exception = new VnfmDriverException(HttpStatus.SC_BAD_REQUEST, "vnfInstanceId is wrong");
		when(vnfmDriverMgmr.terminateVnf(Mockito.any(TerminateVnfRequest.class), Mockito.anyString(), Mockito.anyString())).thenThrow(exception);
		
		String erroMsg = mockMvc.perform(
				post("/nokiavnfm/v1/vnfmId_001/vnfs/vnfInstanceId_001/terminate").
				characterEncoding("UTF-8").
				accept(MediaType.APPLICATION_JSON).
				contentType(MediaType.APPLICATION_JSON).
				content(jsonString))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andReturn().getResponse().getErrorMessage()
				;
		Assert.assertEquals("Error Message is ", exception.getMessage(), erroMsg);
	}

}
