/*
 * Copyright 2017 Huawei Technologies Co., Ltd.
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
package org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
@RunWith(MockitoJUnitRunner.class)
public class RegisterConfigInfoTest {

	@InjectMocks
	RegisterConfigInfo registerConfigInfo;
	
	
	
	@Test
	public void test() {
		assertNotNull(registerConfigInfo.getIp());
		assertNotNull(registerConfigInfo.getPort());
		assertNotNull(registerConfigInfo.getProtocol());
		assertNotNull(registerConfigInfo.getServiceName());
		assertNotNull(registerConfigInfo.getTtl());
		assertNotNull(registerConfigInfo.getInstance());
		assertNotNull(registerConfigInfo.getVersion());
		assertNotNull(registerConfigInfo.getUrl());
	}

	
}
