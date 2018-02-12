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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.impl;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMCreateSubscriptionRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMCreateSubscriptionResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMCreateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMCreateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMHealVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMHealVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMInstantiateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMInstantiateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMModifyVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMModifyVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMQueryOperExecutionResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMQueryVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMScaleVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMScaleVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMTerminateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMTerminateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMVnfNotificationRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMVnfNotificationResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.Subscription;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.VnfcResourceInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.common.bo.AdaptorEnv;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.http.client.HttpClientProcessorInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.http.client.HttpResult;
import org.springframework.web.bind.annotation.RequestMethod;

public class CbamMgmrImplTest {
	@InjectMocks
	private CbamMgmrImpl cbamMgmr;
	
	@Mock
	private HttpClientProcessorInf httpClientProcessor;
	
	private String vnfInstanceId = "vnfInstanceId_001";
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		AdaptorEnv env = new AdaptorEnv();
		cbamMgmr.setAdaptorEnv(env);
		
		String json = "{\"access_token\":\"1234567\"}";
		HttpResult httpResult = new HttpResult();
		httpResult.setStatusCode(200);
		httpResult.setContent(json);
		
		when(httpClientProcessor.process(Mockito.anyString(), Mockito.any(RequestMethod.class), Mockito.any(HashMap.class), Mockito.anyString())).thenReturn(httpResult);
	}
	
	@Test
	public void testCreateVnf() throws ClientProtocolException, IOException
	{
		CBAMCreateVnfRequest cbamRequest = new CBAMCreateVnfRequest();
		CBAMCreateVnfResponse response = cbamMgmr.createVnf(cbamRequest);
	}
	
	@Test
	public void testInstantiateVnf() throws ClientProtocolException, IOException
	{
		CBAMInstantiateVnfRequest cbamRequest = new CBAMInstantiateVnfRequest();
		CBAMInstantiateVnfResponse response = cbamMgmr.instantiateVnf(cbamRequest, vnfInstanceId);
	}
	
	@Test
	public void testModifyVnf() throws ClientProtocolException, IOException
	{
		CBAMModifyVnfRequest cbamRequest = new CBAMModifyVnfRequest();
		CBAMModifyVnfResponse response = cbamMgmr.modifyVnf(cbamRequest, vnfInstanceId);
	}
	
	@Test
	public void testTerminateVnf() throws ClientProtocolException, IOException
	{
		CBAMTerminateVnfRequest cbamRequest = new CBAMTerminateVnfRequest();
		CBAMTerminateVnfResponse response = cbamMgmr.terminateVnf(cbamRequest, vnfInstanceId);
	}
	
	@Test
	public void testDeleteVnf() throws ClientProtocolException, IOException
	{
		cbamMgmr.deleteVnf(vnfInstanceId);
	}
	
	@Test
	public void testScaleVnf() throws ClientProtocolException, IOException
	{
		CBAMScaleVnfRequest cbamRequest = new CBAMScaleVnfRequest();
		CBAMScaleVnfResponse response = cbamMgmr.scaleVnf(cbamRequest, vnfInstanceId);
	}
	
	@Test
	public void testHealVnf() throws ClientProtocolException, IOException
	{
		CBAMHealVnfRequest cbamRequest = new CBAMHealVnfRequest();
		CBAMHealVnfResponse response = cbamMgmr.healVnf(cbamRequest, vnfInstanceId);
	}
	
	@Test
	public void testQueryVnf() throws ClientProtocolException, IOException
	{
		CBAMQueryVnfResponse response = cbamMgmr.queryVnf(vnfInstanceId);
	}
	
	@Test
	public void testCreateSubscription() throws ClientProtocolException, IOException
	{
		CBAMCreateSubscriptionRequest cbamRequest = new CBAMCreateSubscriptionRequest();
		CBAMCreateSubscriptionResponse response = cbamMgmr.createSubscription(cbamRequest);
	}
	
	@Test
	public void testGetNotification() throws ClientProtocolException, IOException
	{
		CBAMVnfNotificationRequest cbamRequest = new CBAMVnfNotificationRequest();
		CBAMVnfNotificationResponse response = cbamMgmr.getNotification(cbamRequest);
	}
	
	@Test
	public void testGetSubscription() throws ClientProtocolException, IOException
	{
		String subscriptionId = "subscriptionId_001";
		Subscription response = cbamMgmr.getSubscription(subscriptionId);
	}
	
	@Test
	public void testQueryVnfcResource() throws ClientProtocolException, IOException
	{
		String json = "[{'id':'id_001'}]";
		HttpResult httpResult = new HttpResult();
		httpResult.setStatusCode(200);
		httpResult.setContent(json);
		
		when(httpClientProcessor.process(Mockito.anyString(), Mockito.any(RequestMethod.class), Mockito.any(HashMap.class), Mockito.anyString())).thenReturn(httpResult);
		List<VnfcResourceInfo> response = cbamMgmr.queryVnfcResource(vnfInstanceId);
	}
	
	@Test
	public void testQueryOperExecution() throws ClientProtocolException, IOException
	{
		String execId = "execId_001";
		CBAMQueryOperExecutionResponse response = cbamMgmr.queryOperExecution(execId);
	}
	
	@Test
	public void testUploadVnfPackage() throws ClientProtocolException, IOException
	{
		String cbamPackageFilePath = "cbamPackageFilePath_001";
		cbamMgmr.uploadVnfPackage(cbamPackageFilePath);
	}
}
