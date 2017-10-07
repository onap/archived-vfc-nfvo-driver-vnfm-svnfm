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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.msb.impl;

import java.io.IOException;

import org.onap.msb.sdk.discovery.common.RouteException;
import org.onap.msb.sdk.discovery.entity.MicroServiceFullInfo;
import org.onap.msb.sdk.discovery.entity.MicroServiceInfo;
import org.onap.msb.sdk.discovery.entity.RouteResult;
import org.onap.msb.sdk.httpclient.msb.MSBServiceClient;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.common.bo.AdaptorEnv;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.common.util.CommonUtil;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.msb.inf.IMsbMgmr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

@Component
public class MsbMgmrImpl implements IMsbMgmr {
	private static final Logger logger = LoggerFactory.getLogger(MsbMgmrImpl.class);
	
	@Autowired
	AdaptorEnv adaptorEnv;
	
	private Gson gson = new Gson();
	
	@Override
	public void register() {
		try {
			String vfcAdaptorInfoJsonStr = readVfcAdaptorInfoFromJsonFile();
			MicroServiceInfo msinfo = gson.fromJson(vfcAdaptorInfoJsonStr, MicroServiceInfo.class);
			
			MSBServiceClient msbClient = new MSBServiceClient(adaptorEnv.getMsbIp(), adaptorEnv.getMsbPort());
			MicroServiceFullInfo microServiceInfo = msbClient.registerMicroServiceInfo(msinfo);
			logger.info("Registered service response info is " + microServiceInfo.toString());
			
		} catch (IOException e) {
			logger.error("Failed to read vfcadaptor info! ", e);
		} catch (RouteException e) {
			logger.error("Failed to register nokia vnfm driver! ", e);
		}
			
	}
	
	private String readVfcAdaptorInfoFromJsonFile() throws IOException {
        String filePath = "/etc/adapterInfo/vnfmadapterinfo.json";
		String fileContent = CommonUtil.getJsonStrFromFile(filePath);

        return fileContent;
    }

	@Override
	public void unregister() {
		try {
			String jsonStr = readVfcAdaptorInfoFromJsonFile();
			MicroServiceInfo msinfo = gson.fromJson(jsonStr, MicroServiceInfo.class);
			
			MSBServiceClient msbClient = new MSBServiceClient(adaptorEnv.getMsbIp(), adaptorEnv.getMsbPort());
			RouteResult routeResult = msbClient.cancelMicroServiceInfo(msinfo.getServiceName(), msinfo.getVersion());
			logger.info("unregistered service response info is " + routeResult.toString());
			
		} catch (IOException e) {
			logger.error("Failed to read vfcadaptor info! ", e);
		} catch (RouteException e) {
			logger.error("Failed to register nokia vnfm driver! ", e);
		}
	}
	
   public String getServiceUrlInMsbBySeriveNameAndPort(String serviceName, String version) throws RouteException
   {
	   String serviceUrl = null;
	   MSBServiceClient msbClient = new MSBServiceClient(adaptorEnv.getMsbIp(), adaptorEnv.getMsbPort());
	   MicroServiceFullInfo microServiceInfo = msbClient.queryMicroServiceInfo(serviceName, version);
	   if(null == microServiceInfo)
	   {
		   logger.error("There is no service in MSB for serviceName = {} and version = {}", serviceName, version);
	   }
	   else
	   {
		   serviceUrl = microServiceInfo.getUrl();
		   logger.info("Service Url in MSB for serviceName = {} and version = {} is {}", serviceName, version, serviceUrl);
	   }
	   return serviceUrl;
		
   }

	public void setAdaptorEnv(AdaptorEnv env) {
		this.adaptorEnv = env;
	}

}
