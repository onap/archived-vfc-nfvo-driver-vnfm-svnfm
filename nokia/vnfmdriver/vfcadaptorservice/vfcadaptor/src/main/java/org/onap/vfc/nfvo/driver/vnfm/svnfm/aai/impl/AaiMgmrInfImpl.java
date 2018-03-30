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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.aai.impl;

import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.aai.bo.AaiVnfmInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.aai.inf.AaiMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.common.bo.AdaptorEnv;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonConstants;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.http.client.HttpClientProcessorInf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.Gson;

@Component
public class AaiMgmrInfImpl implements AaiMgmrInf{
	private static final Logger logger = LoggerFactory.getLogger(AaiMgmrInfImpl.class);
	
	@Autowired 
	private AdaptorEnv adaptorEnv;
	
	@Autowired
	HttpClientProcessorInf httpClientProcessor;
	
	private Gson gson = new Gson();
	@Override
	public AaiVnfmInfo queryVnfm(String vnfmId) throws IOException {
		String httpPath = String.format(CommonConstants.RetrieveVnfmListPath, vnfmId);
		RequestMethod method = RequestMethod.GET;
		
		String responseStr = operateHttpTask(null, httpPath, method);
		
		logger.info("AaiMgmrInfImpl->queryVnfm, the vnfmInfo is {}", responseStr);
		
		AaiVnfmInfo response = gson.fromJson(responseStr, AaiVnfmInfo.class);
		
		return response;
	}
	
	private String operateHttpTask(Object httpBodyObj, String httpPath, RequestMethod method) throws IOException {
		String url=adaptorEnv.getAaiApiUriFront() + httpPath;
		
		HashMap<String, String> headerMap = new HashMap<>();
		headerMap.put("Content-Type", "application/json");
        headerMap.put("Accept", "application/json");
        headerMap.put("X-TransactionId", "9999");
        headerMap.put("X-FromAppId", "esr-server");

        Base64 token = new Base64();
        String authen = new String(token.encode(("AAI:AAI").getBytes()));
        headerMap.put("Authorization", "Basic " + authen);
        logger.info("getVimById headerMap: {}", headerMap.toString());
		
		String responseStr = httpClientProcessor.process(url, method, headerMap, gson.toJson(httpBodyObj)).getContent();
		
		return responseStr;
	}

	public void setAdaptorEnv(AdaptorEnv env) {
		this.adaptorEnv = env;
	}
}
