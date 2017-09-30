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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.onap.msb.sdk.discovery.common.RouteException;
import org.onap.msb.sdk.discovery.entity.MicroServiceFullInfo;
import org.onap.msb.sdk.discovery.entity.MicroServiceInfo;
import org.onap.msb.sdk.discovery.entity.RouteResult;
import org.onap.msb.sdk.httpclient.msb.MSBServiceClient;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.msb.inf.IMsbMgmr;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

@Component
public class MsbMgmrImpl implements IMsbMgmr {
	private static final Logger logger = LoggerFactory.getLogger(MsbMgmrImpl.class);
	
	private Gson gson = new Gson();
	
	private String msb_ip;
	
	private int msb_port;
	
	@Override
	public void register() {
		try {
			String msbInfoJsonStr = readMsbInfoFromJsonFile();
			JSONObject totalJsonObj = new JSONObject(msbInfoJsonStr);
			JSONObject serverJsonObj = totalJsonObj.getJSONObject("defaultServer");
			msb_ip = serverJsonObj.getString("host");
			msb_port = serverJsonObj.getInt("port");
			
			String vfcAdaptorInfoJsonStr = readVfcAdaptorInfoFromJsonFile();
			MicroServiceInfo msinfo = gson.fromJson(vfcAdaptorInfoJsonStr, MicroServiceInfo.class);
			
			MSBServiceClient msbClient = new MSBServiceClient(msb_ip, msb_port);
			MicroServiceFullInfo microServiceInfo = msbClient.registerMicroServiceInfo(msinfo);
			logger.info("Registered service response info is " + microServiceInfo.toString());
			
		} catch (IOException e) {
			logger.error("Failed to read vfcadaptor info! ", e);
		} catch (RouteException e) {
			logger.error("Failed to register nokia vnfm driver! ", e);
		} catch (JSONException e) {
			logger.error("Failed to retrieve json info! ", e);
		}
			
	}
	
	private String readMsbInfoFromJsonFile() throws IOException {
		String filePath = "/etc/conf/restclient.json";
		String fileContent = getJsonStrFromFile(filePath);

        return fileContent;
	}

	private String readVfcAdaptorInfoFromJsonFile() throws IOException {
        String filePath = "/etc/adapterInfo/vnfmadapterinfo.json";
		String fileContent = getJsonStrFromFile(filePath);

        return fileContent;
    }

	public String getJsonStrFromFile(String filePath) throws IOException {
		InputStream ins = null;
        BufferedInputStream bins = null;
        String fileContent = "";
        String fileName = getAppRoot() + filePath;

        try {
            ins = new FileInputStream(fileName);
            bins = new BufferedInputStream(ins);

            byte[] contentByte = new byte[ins.available()];
            int num = bins.read(contentByte);

            if(num > 0) {
                fileContent = new String(contentByte);
            }
        } catch(FileNotFoundException e) {
        	logger.error(fileName + "is not found!", e);
        } finally {
            if(ins != null) {
                ins.close();
            }
            if(bins != null) {
                bins.close();
            }
        }
		return fileContent;
	}

	@Override
	public void unregister() {
		try {
			String jsonStr = readVfcAdaptorInfoFromJsonFile();
			MicroServiceInfo msinfo = gson.fromJson(jsonStr, MicroServiceInfo.class);
			
			MSBServiceClient msbClient = new MSBServiceClient(msb_ip, msb_port);
			RouteResult routeResult = msbClient.cancelMicroServiceInfo(msinfo.getServiceName(), msinfo.getVersion());
			logger.info("unregistered service response info is " + routeResult.toString());
			
		} catch (IOException e) {
			logger.error("Failed to read vfcadaptor info! ", e);
		} catch (RouteException e) {
			logger.error("Failed to register nokia vnfm driver! ", e);
		}
	}
	
    public String getAppRoot() {
        String appRoot = null;
        appRoot = System.getProperty("catalina.base");
        if(appRoot != null) {
            appRoot = getCanonicalPath(appRoot);
        }
        return appRoot;
    }

    private String getCanonicalPath(final String inPath) {
        String path = null;
        try {
            if(inPath != null) {
                final File file = new File(inPath);
                path = file.getCanonicalPath();
            }
        } catch(final IOException e) {
            logger.error("file.getCanonicalPath() IOException:", e);
        }
        return path;
    }

}
