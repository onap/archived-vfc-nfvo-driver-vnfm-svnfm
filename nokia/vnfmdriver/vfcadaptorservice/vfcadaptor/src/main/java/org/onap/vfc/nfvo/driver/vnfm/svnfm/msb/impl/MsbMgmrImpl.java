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

import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.common.bo.AdaptorEnv;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonConstants;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.http.client.HttpRequestProcessor;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.msb.inf.IMsbMgmr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestMethod;

@Component
public class MsbMgmrImpl implements IMsbMgmr {
	private static final Logger logger = LogManager.getLogger("MsbMgmrImpl");
	@Autowired 
	private HttpClientBuilder httpClientBuilder;
	
	@Autowired 
	private AdaptorEnv adaptorEnv;
	
	@Value("${serviceName}")
	private String serviceName;
	
	@Value("${version}")
	private String version;
	
	@Value("${url}")
	private String url;
	
	@Value("${protocol}")
	private String protocol;
	
	@Value("${visualRange}")
	private String visualRange;
	
	@Value("${ip}")
	private String ip;
	
	@Value("${port}")
	private String port;
	
	@Value("${ttl}")
	private String ttl;

	@Override
	public void register() {
		String httpPath = CommonConstants.MSB_REGISTER_SERVICE_PATH;
		RequestMethod method = RequestMethod.POST;
		
		try {
			String jsonStr = readVfcAdaptorInfoFromJsonFile();
			String registerResponse = operateHttpTask(jsonStr, httpPath, method);
			logger.info("registerResponse is ", registerResponse); 
		} catch (IOException e) {
			logger.error("Failed to read vfcadaptor info! ", e);
		}
			
	}
	
	public String readVfcAdaptorInfoFromJsonFile()  throws IOException {
        InputStream ins = null;
        BufferedInputStream bins = null;
        String fileContent = "";
        String fileName = getAppRoot() + "/etc/adapterInfo/vnfmadapterinfo.json";

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
		String httpPath = String.format(CommonConstants.MSB_UNREGISTER_SERVICE_PATH, serviceName, version, ip, port);
		RequestMethod method = RequestMethod.DELETE;
		
		try {
			String jsonStr = readVfcAdaptorInfoFromJsonFile();
			String registerResponse = operateHttpTask(jsonStr, httpPath, method);
			logger.info("unregisterResponse is ", registerResponse); 
		} catch (IOException e) {
			logger.error("Failed to unregister! ", e);
		}

	}
	
	public String operateHttpTask(String httpBodyObj, String httpPath, RequestMethod method) throws ClientProtocolException, IOException {
		String url=adaptorEnv.getMsbApiUriFront() + httpPath;
		HttpRequestProcessor processor = new HttpRequestProcessor(httpClientBuilder, method);
		processor.addHdeader(CommonConstants.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		
		processor.addPostEntity(httpBodyObj);
		
		String responseStr = processor.process(url);
		
		return responseStr;
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
