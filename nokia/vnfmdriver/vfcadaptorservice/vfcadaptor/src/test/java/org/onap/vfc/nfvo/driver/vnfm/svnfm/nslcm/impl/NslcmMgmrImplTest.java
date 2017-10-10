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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.impl;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.common.bo.AdaptorEnv;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.common.bo.KeyValuePair;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonEnum;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonEnum.LifecycleOperation;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.http.client.HttpClientProcessorInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.http.client.HttpResult;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.NslcmGrantVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.NslcmGrantVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.NslcmNotifyLCMEventsRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.AccessInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.AffectedVirtualStorage;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.AffectedVnfc;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.InterfaceInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.ResourceDefinition;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.VimAssets;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.VimInfo;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.Gson;

public class NslcmMgmrImplTest {
	@InjectMocks
	private NslcmMgmrImpl nslcmMgmr;
	
	@Mock
	private HttpClientProcessorInf httpClientProcessor;
	
	private String vnfInstanceId = "vnfInstanceId_001";
	
	private Gson gson = new Gson();
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		AdaptorEnv env = new AdaptorEnv();
		nslcmMgmr.setAdaptorEnv(env);
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testGrantVnf() throws ClientProtocolException, IOException
	{
		ResourceDefinition resource = new ResourceDefinition();
		resource.setResourceDefinitionId("resourceDefinitionId");
		resource.setVdu("vdu");
		
		NslcmGrantVnfResponse gresponse = new NslcmGrantVnfResponse();
		List<KeyValuePair> additionalParam1 = new ArrayList<KeyValuePair>();
		KeyValuePair pair = new KeyValuePair();
		pair.setKey("key");
		pair.setValue("value");
		additionalParam1.add(pair);
		
		List<ResourceDefinition> additionalParam2 = new ArrayList<ResourceDefinition>();
		additionalParam2.add(resource);
		gresponse.setAdditionalParam(additionalParam2);
		
		VimInfo vim = new VimInfo();
		vim.setInterfaceEndpoint("interfaceEndpoint");
		vim.setVimId("vimId");
		AccessInfo accessInfo = null;
		vim.setAccessInfo(accessInfo );
		InterfaceInfo interfaceInfo = null;
		vim.setInterfaceInfo(interfaceInfo);
		
		VimAssets vimAssets = null;
		gresponse.setVimAssets(vimAssets);
		
		gresponse.setVim(vim );
		String json = gson.toJson(gresponse);
		HttpResult httpResult = new HttpResult();
		httpResult.setContent(json);
		
		when(httpClientProcessor.process(Mockito.anyString(), Mockito.any(RequestMethod.class), Mockito.any(HashMap.class), Mockito.anyString())).thenReturn(httpResult);
		
		NslcmGrantVnfRequest cbamRequest = new NslcmGrantVnfRequest();
		List<KeyValuePair> additionalParam = new ArrayList<KeyValuePair>();
		pair = new KeyValuePair();
		pair.setKey("key");
		pair.setValue("value");
		additionalParam.add(pair);
		cbamRequest.setAdditionalParam(additionalParam);
		
		
		List<ResourceDefinition> addResource = new ArrayList<ResourceDefinition>();
		addResource.add(resource);
		
		cbamRequest.setAddResource(addResource);
		cbamRequest.setVnfInstanceId("vnfInstanceId");
		cbamRequest.setJobId("jobId");
		LifecycleOperation lifecycleOperation = CommonEnum.LifecycleOperation.Instantiate;
		cbamRequest.setLifecycleOperation(lifecycleOperation);
		cbamRequest.setRemoveResource(addResource);
		
		NslcmGrantVnfResponse response = nslcmMgmr.grantVnf(cbamRequest);
	}
	
	@Test
	public void testNotifyVnf() throws ClientProtocolException, IOException
	{
		String json = "{}";
		HttpResult httpResult = new HttpResult();
		httpResult.setContent(json);
		
		when(httpClientProcessor.process(Mockito.anyString(), Mockito.any(RequestMethod.class), Mockito.any(HashMap.class), Mockito.anyString())).thenReturn(httpResult);
		NslcmNotifyLCMEventsRequest cbamRequest = new NslcmNotifyLCMEventsRequest();
		cbamRequest.setJobId("jobId");
		cbamRequest.setOperation("operation");
		cbamRequest.setVnfInstanceId(vnfInstanceId);
		AffectedVirtualStorage affectedVirtualStorage = new AffectedVirtualStorage();
		cbamRequest.setAffectedVirtualStorage(affectedVirtualStorage );
		AffectedVnfc affectedVnfc = new AffectedVnfc();
		cbamRequest.setAffectedVnfc(affectedVnfc );
		nslcmMgmr.notifyVnf(cbamRequest, vnfInstanceId);
	}
}
