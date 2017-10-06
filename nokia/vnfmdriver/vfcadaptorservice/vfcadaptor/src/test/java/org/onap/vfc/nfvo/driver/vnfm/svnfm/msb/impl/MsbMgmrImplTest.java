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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.msb.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.msb.sdk.discovery.common.RouteException;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.common.bo.AdaptorEnv;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.http.client.HttpClientProcessorInf;

public class MsbMgmrImplTest {
	@InjectMocks
	private MsbMgmrImpl msbMgmr;
	
	@Mock
	private HttpClientProcessorInf httpClientProcessor;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		AdaptorEnv env = new AdaptorEnv();
		env.setMsbIp("127.0.0.1");
		env.setMsbPort(80);
		msbMgmr.setAdaptorEnv(env);
		MockitoAnnotations.initMocks(this);
	}

	@Test(expected = RuntimeException.class)
	public void testRegister()
	{
		msbMgmr.register();
	}
	
	@Test(expected = RuntimeException.class)
	public void testunRegister()
	{
		msbMgmr.unregister();
	}
	
	@Test(expected = RouteException.class)
	public void testGetServiceUrlInMsbBySeriveNameAndPort() throws RouteException
	{
		msbMgmr.getServiceUrlInMsbBySeriveNameAndPort("serviceName", "88");
	}
}
