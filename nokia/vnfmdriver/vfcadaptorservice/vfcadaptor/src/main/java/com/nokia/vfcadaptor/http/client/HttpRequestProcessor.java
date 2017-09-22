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

package com.nokia.vfcadaptor.http.client;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import com.nokia.vfcadaptor.constant.CommonConstants;

public class HttpRequestProcessor {
	private CloseableHttpClient httpClient;
	private HttpRequestBase httpRequest;
	
	public HttpRequestProcessor(HttpClientBuilder httpClientBuilder, RequestMethod requestMethod)
	{
		httpClient = httpClientBuilder.build();
		httpRequest = HttpClientUtils.getHttpRequest(requestMethod);
	}
	
	public String process(String url) throws ClientProtocolException, IOException
	{
		httpRequest.setURI(URI.create(url));
		
		HttpResponse response = httpClient.execute(httpRequest);
		HttpEntity resEntity = response.getEntity();
		String responseContent = "";
		if (resEntity != null) {
			responseContent = EntityUtils.toString(resEntity, CommonConstants.UTF_8);
			EntityUtils.consume(resEntity);
		}
		httpClient.close();
		
		return responseContent;
	}

	public void addHdeader(String key, String value) {
		httpRequest.setHeader(key, value);
		
	}

	public void addPostEntity(String bodyStr) {
		((HttpPost)httpRequest).setEntity(new StringEntity(bodyStr, CommonConstants.UTF_8));
		
	}
}
