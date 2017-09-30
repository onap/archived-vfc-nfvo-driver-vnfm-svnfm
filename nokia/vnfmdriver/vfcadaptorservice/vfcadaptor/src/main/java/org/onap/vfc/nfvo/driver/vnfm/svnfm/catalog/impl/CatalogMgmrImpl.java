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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.catalog.impl;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.catalog.bo.CatalogQueryVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.catalog.inf.CatalogMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.common.bo.AdaptorEnv;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonConstants;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.http.client.HttpRequestProcessor;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.VnfPackageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.Gson;

@Component
public class CatalogMgmrImpl implements CatalogMgmrInf{
	private static final Logger logger = LoggerFactory.getLogger(CatalogMgmrImpl.class);
	
	private Gson gson = new Gson();
	
	@Autowired 
	private AdaptorEnv adaptorEnv;
	
	@Autowired
	private HttpClientBuilder httpClientBuilder;
	
	public VnfPackageInfo queryVnfPackage(String vnfPackageId) throws ClientProtocolException, IOException {
		String url=adaptorEnv.getCatalogApiUriFront() + String.format(CommonConstants.RetrieveVnfPackagePath, vnfPackageId);
		HttpRequestProcessor processor = new HttpRequestProcessor(httpClientBuilder, RequestMethod.GET);
		
		String responseStr = processor.process(url);
		
		logger.info("CbamMgmrImpl -> queryVnfPackage, responseStr is " + responseStr);
		
		CatalogQueryVnfResponse resp = gson.fromJson(responseStr, CatalogQueryVnfResponse.class);
		
		return resp.getPackageInfo();
	}

	
}
