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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.msb.service;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.msb.inf.IMsbMgmr;
import org.springframework.boot.ApplicationArguments;

public class MsbServiceRegisterTest {
	@InjectMocks
	private MsbServiceRegister msbServiceRegister;

	@Mock
	private IMsbMgmr msbMgmr;
	
	@Mock
	private ApplicationArguments argments;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testRegister() throws ClientProtocolException, IOException
	{
		msbServiceRegister.run(argments);
	}
}
