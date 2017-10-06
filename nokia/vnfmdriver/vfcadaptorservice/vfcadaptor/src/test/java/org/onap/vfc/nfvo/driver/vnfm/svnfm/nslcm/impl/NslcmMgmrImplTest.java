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
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.common.bo.AdaptorEnv;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.http.client.HttpClientProcessorInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.NslcmGrantVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.NslcmGrantVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.NslcmNotifyLCMEventsRequest;
import org.springframework.web.bind.annotation.RequestMethod;

public class NslcmMgmrImplTest {
	@InjectMocks
	private NslcmMgmrImpl nslcmMgmr;
	
	@Mock
	private HttpClientProcessorInf httpClientProcessor;
	
	private String vnfInstanceId = "vnfInstanceId_001";
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		AdaptorEnv env = new AdaptorEnv();
		nslcmMgmr.setAdaptorEnv(env);
		MockitoAnnotations.initMocks(this);
		
		String json = "{\"access_token\":\"1234567\"}";
		
		when(httpClientProcessor.process(Mockito.anyString(), Mockito.any(RequestMethod.class), Mockito.any(HashMap.class), Mockito.anyString())).thenReturn(json);
	}
	
	@Test
	public void testGrantVnf() throws ClientProtocolException, IOException
	{
		NslcmGrantVnfRequest cbamRequest = new NslcmGrantVnfRequest();
		NslcmGrantVnfResponse response = nslcmMgmr.grantVnf(cbamRequest);
	}
	
	@Test
	public void testNotifyVnf() throws ClientProtocolException, IOException
	{
		NslcmNotifyLCMEventsRequest cbamRequest = new NslcmNotifyLCMEventsRequest();
		nslcmMgmr.notifyVnf(cbamRequest, vnfInstanceId);
	}
}
