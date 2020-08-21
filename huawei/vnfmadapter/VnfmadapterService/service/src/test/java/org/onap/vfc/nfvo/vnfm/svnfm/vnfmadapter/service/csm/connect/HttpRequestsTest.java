/*
 * Copyright 2016 Huawei Technologies Co., Ltd.
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

package org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.csm.connect;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.Header;
import org.junit.Test;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.VnfmException;

public class HttpRequestsTest {

	String authenticateMode = "test";
	HttpRequests.Builder builder = new HttpRequests.Builder(authenticateMode);

	@Test
	public void addHeaderTest() {
		builder.addHeader("id", "1234");
		assertTrue(true);
	}

	@Test
	public void addHeadersTest() {
		Header header = new Header();
		builder.addHeaders(header, header);
		assertNotNull(builder);
	}

	@Test
	public void addHeadersListTest() {
		List<Header> list = new ArrayList<>();
		Header header = new Header();
		list.add(header);
		builder.addHeaders(list);
		assertNotNull(builder);
	}

	@Test(expected = VnfmException.class)
	public void setUrlTestException() throws VnfmException {
		String url = null;
		String path = null;
		builder.setUrl(url, path);
	}

	@Test(expected = VnfmException.class)
	public void setUrlTestException2() throws VnfmException {
		String url = "";
		String path = null;
		builder.setUrl(url, path);
	}

	@Test(expected = VnfmException.class)
	public void setUrlTestNormal() throws VnfmException {
		String url = "/test/123";
		String path = "http://localhost:8080";
		builder.setUrl(url, path);
	}

	@Test(expected = VnfmException.class)
	public void setUrl2TestException() throws VnfmException {
		String url = null;
		String path = null;
		builder.setUrl(url, path, 101);
	}

	@Test(expected = VnfmException.class)
	public void setUrl2TestException2() throws VnfmException {
		String url = "";
		String path = null;
		builder.setUrl(url, path, 101);
	}

	@Test(expected = VnfmException.class)
	public void setUrl2TestNormal() throws VnfmException {
		String url = "/test/123";
		String path = "http://localhost:8080";
		builder.setUrl(url, path, 101);
	}

	@Test(expected = Exception.class)
	public void requestTestException() {
		String res = builder.request();
		assertNotNull(res);
	}

	@Test
	public void postTest() throws VnfmException {
		assertNotNull(builder.post());
	}

	@Test
	public void putTest() throws VnfmException {
		assertNotNull(builder.put());
	}

	@Test
	public void getTest() throws VnfmException {
		assertNotNull(builder.get());
	}

	@Test
	public void deleteTest() throws VnfmException {
		assertNotNull(builder.delete());
	}

	@Test
	public void setParamsTest() throws VnfmException {
		String json = "test";
		assertNotNull(builder.setParams(json));
	}
	
	@Test
	public void setEncodingTest() throws VnfmException {
		String json = "test";
		assertNotNull(builder.setEncoding(json));
	}

}
