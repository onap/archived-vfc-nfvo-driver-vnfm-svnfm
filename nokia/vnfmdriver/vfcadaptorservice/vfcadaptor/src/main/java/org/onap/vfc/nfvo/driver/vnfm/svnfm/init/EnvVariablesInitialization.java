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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.init;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.onap.msb.sdk.discovery.common.RouteException;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.common.bo.AdaptorEnv;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.common.util.CommonUtil;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonConstants;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.msb.inf.IMsbMgmr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class EnvVariablesInitialization implements ApplicationRunner {
	private static final Logger logger = LoggerFactory.getLogger(EnvVariablesInitialization.class);

	@Autowired
	AdaptorEnv adaptorEnv;
	
	@Autowired
	private IMsbMgmr msbMgmr;
	
	@Override
	public void run(ApplicationArguments args){
		try {
			getMsbIpAndPort();
		} catch (Exception e) {
			logger.error("getMsbIpAndPort error", e);
			return;
		}
		
		try {
			handleAaiMsbServiceInfo();
		} catch (RouteException e) {
			logger.error("handleAaiMsbServiceInfo error", e);
		}
		
		try {
			handLcmMsbServiceInfo();
		} catch (RouteException e) {
			logger.error("handLcmMsbServiceInfo error", e);
		}
		
		try {
			handCatalogMsbServiceInfo();
		} catch (RouteException e) {
			logger.error("handCatalogMsbServiceInfo error", e);
		}
		
	}

	private void handleAaiMsbServiceInfo() throws RouteException {
		String urlInMsb = msbMgmr.getServiceUrlInMsbBySeriveNameAndPort(adaptorEnv.getAaiServiceNameInMsb(), adaptorEnv.getAaiVersionInMsb());
		adaptorEnv.setAaiUrlInMsb(urlInMsb);
		adaptorEnv.setAaiApiUriFront(generateApiUriFront(urlInMsb));
	}

	private String generateApiUriFront(String urlInMsb) {
		return CommonConstants.SCHEMA_HTTP + "://" + adaptorEnv.getMsbIp() + ":" + adaptorEnv.getMsbPort() + urlInMsb;
	}
	
	private void handLcmMsbServiceInfo() throws RouteException {
		String urlInMsb = msbMgmr.getServiceUrlInMsbBySeriveNameAndPort(adaptorEnv.getLcmServiceNameInMsb(), adaptorEnv.getLcmVersionInMsb());
		adaptorEnv.setLcmUrlInMsb(urlInMsb);
		adaptorEnv.setLcmApiUriFront(generateApiUriFront(urlInMsb));
	}
	
	private void handCatalogMsbServiceInfo() throws RouteException {
		String urlInMsb = msbMgmr.getServiceUrlInMsbBySeriveNameAndPort(adaptorEnv.getCatalogServiceNameInMsb(), adaptorEnv.getCatalogVersionInMsb());
		adaptorEnv.setCatalogUrlInMsb(urlInMsb);
		adaptorEnv.setCatalogApiUriFront(generateApiUriFront(urlInMsb));
	}

	private void getMsbIpAndPort() throws IOException, JSONException {
		String msbInfoJsonStr = readMsbInfoFromJsonFile();
		JSONObject totalJsonObj = new JSONObject(msbInfoJsonStr);
		JSONObject serverJsonObj = totalJsonObj.getJSONObject("defaultServer");
		String msb_ip = serverJsonObj.getString("host");
		int msb_port = serverJsonObj.getInt("port");
		
		adaptorEnv.setMsbIp(msb_ip);
		adaptorEnv.setMsbPort(msb_port);
	}
	
	private String readMsbInfoFromJsonFile() throws IOException {
		String filePath = "/etc/conf/restclient.json";
		String fileContent = CommonUtil.getJsonStrFromFile(filePath);

        return fileContent;
	}
}
