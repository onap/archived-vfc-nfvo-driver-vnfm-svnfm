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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.http.client;

import java.io.IOException;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

@Component
public class HttpClientProcessorImpl implements HttpClientProcessorInf{

	@Autowired
	private HttpClientBuilder httpClientBuilder;
	
	public String process(String url, RequestMethod methodType, HashMap<String, String> headerMap, String bodyString) throws ClientProtocolException, IOException
	{
		HttpRequestProcessor processor = new HttpRequestProcessor(httpClientBuilder, methodType);
		if(headerMap != null && !headerMap.isEmpty())
		{
			for(String key : headerMap.keySet())
			{
				processor.addHdeader(key, headerMap.get(key));
			}
			
			if(null != bodyString && bodyString.length() > 0)
			{
				processor.addPostEntity(bodyString);
			}
			
		}
		return processor.process(url);
	}
}
