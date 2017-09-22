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

package com.nokia.vfcadaptor.nslcm.impl;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.Gson;
import com.nokia.vfcadaptor.common.bo.AdaptorEnv;
import com.nokia.vfcadaptor.constant.CommonConstants;
import com.nokia.vfcadaptor.http.client.HttpRequestProcessor;
import com.nokia.vfcadaptor.nslcm.bo.NslcmGrantVnfRequest;
import com.nokia.vfcadaptor.nslcm.bo.NslcmGrantVnfResponse;
import com.nokia.vfcadaptor.nslcm.bo.NslcmNotifyLCMEventsRequest;
import com.nokia.vfcadaptor.nslcm.bo.VnfmInfo;
import com.nokia.vfcadaptor.nslcm.inf.NslcmMgmrInf;

@Component
public class NslcmMgmrImpl implements NslcmMgmrInf{
	private Logger logger = Logger.getLogger(NslcmMgmrImpl.class);
	
	@Autowired 
	private AdaptorEnv adaptorEnv;
	
	@Autowired 
	private HttpClientBuilder httpClientBuilder;
	
	private Gson gson = new Gson();
	
	public VnfmInfo queryVnfm(String vnfmId) throws ClientProtocolException, IOException
	{
		String httpPath = String.format(CommonConstants.RetrieveNvfmListPath, vnfmId);
		RequestMethod method = RequestMethod.GET;
		
		String responseStr = operateNslcmHttpTask(null, httpPath, method);
		
		logger.info("NslcmMgmrImpl->queryVnfm, the vnfmInfo is " + responseStr);
		
		VnfmInfo response = gson.fromJson(responseStr, VnfmInfo.class);
		
		return response;
	}

	public NslcmGrantVnfResponse grantVnf(NslcmGrantVnfRequest driverRequest) throws ClientProtocolException, IOException {
		String httpPath = CommonConstants.NslcmGrantPath;
		RequestMethod method = RequestMethod.POST;
			
		String responseStr = operateNslcmHttpTask(driverRequest, httpPath, method);
		
		logger.info("NslcmMgmrImpl->grantVnf, the NslcmGrantVnfResponse is " + responseStr);
		
		NslcmGrantVnfResponse response = gson.fromJson(responseStr, NslcmGrantVnfResponse.class);
		
		return response;
	}

	public void notifyVnf(NslcmNotifyLCMEventsRequest driverRequest, String vnfInstanceId) throws ClientProtocolException, IOException {
		String httpPath = String.format(CommonConstants.NslcmNotifyPath, vnfInstanceId);
		RequestMethod method = RequestMethod.POST;
			
		operateNslcmHttpTask(driverRequest, httpPath, method);
	}
	
	public String operateNslcmHttpTask(Object httpBodyObj, String httpPath, RequestMethod method) throws ClientProtocolException, IOException {
		String url="http://" + adaptorEnv.getNslcmIp() + ":" + adaptorEnv.getNslcmPort() + httpPath;
		HttpRequestProcessor processor = new HttpRequestProcessor(httpClientBuilder, method);
		processor.addHdeader(CommonConstants.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		
		processor.addPostEntity(gson.toJson(httpBodyObj));
		
		String responseStr = processor.process(url);
		
		return responseStr;
	}

}
