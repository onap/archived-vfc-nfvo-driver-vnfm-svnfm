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
package org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.restclient;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;

import mockit.integration.junit4.JMockit;
@RunWith(JMockit.class)
public class ServiceExceptionTest {
	
	@Test
	public void test() {
		ServiceException serviceException = new ServiceException();
		ServiceException serviceWithThrowable = new ServiceException("id",new Throwable());
		ServiceException serviceWithThrowableMessage = new ServiceException("id","message",new Throwable());

		ServiceException serviceWithMessage = new ServiceException("message");
		ServiceException serviceWithID = new ServiceException("id","message");
		ServiceException serviceWithHttpCode = new ServiceException("id",200);
		ServiceException serviceWithHttpCodeAndMessage = new ServiceException(200,"message");
		ServiceException serviceWithexceptionArgs = new ServiceException("id",200,new ExceptionArgs());
		ServiceException serviceWithMessageThowable =new ServiceException("id","message","args","args");
		ServiceException serviceWithThowableAndArgs =new ServiceException("id","message",new Throwable(),"args");
		assertNotNull(serviceWithThowableAndArgs.getArgs());
		assertNotNull(serviceWithID.getId());
		assertNotNull(serviceWithexceptionArgs.getExceptionArgs());
		assertNotNull(serviceWithThowableAndArgs.getArgs());
	}
}
