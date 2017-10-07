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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.impl;

import java.io.IOException;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMCreateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMCreateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMHealVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMHealVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMInstantiateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMInstantiateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMQueryOperExecutionResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMQueryVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMScaleVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMScaleVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMTerminateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMTerminateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.inf.CbamMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.common.bo.AdaptorEnv;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonConstants;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.http.client.HttpClientProcessorInf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.Gson;

@Component
public class CbamMgmrImpl implements CbamMgmrInf {
	private static final Logger logger = Logger.getLogger(CbamMgmrImpl.class);
	private Gson gson = new Gson();
	
	@Autowired 
	private AdaptorEnv adaptorEnv;
	
	@Autowired
	HttpClientProcessorInf httpClientProcessor;
	
	private String retrieveToken() throws ClientProtocolException, IOException, JSONException {
		String result = null;
		String url= adaptorEnv.getCbamApiUriFront() + CommonConstants.RetrieveCbamTokenPath;
		HashMap<String, String> map = new HashMap<>();
		map.put(CommonConstants.ACCEPT, "*/*");
		map.put(CommonConstants.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
		
		String bodyPostStr = String.format(CommonConstants.RetrieveCbamTokenPostStr, adaptorEnv.getGrantType(), adaptorEnv.getClientId(), adaptorEnv.getClientSecret());
		
		String responseStr = httpClientProcessor.process(url, RequestMethod.GET, map, bodyPostStr).getContent();
		
		logger.info("CbamMgmrImpl -> retrieveToken, responseStr is " + responseStr);
		
		JSONObject tokenJsonObject = new JSONObject(responseStr);
		
		result = tokenJsonObject.getString(CommonConstants.CBAM_TOKEN_KEY);
		
		return result;
	}
	
	public CBAMCreateVnfResponse createVnf(CBAMCreateVnfRequest cbamRequest) throws ClientProtocolException, IOException {
		String httpPath = CommonConstants.CbamCreateVnfPath;
		RequestMethod method = RequestMethod.POST;
			
		String responseStr = operateCbamHttpTask(cbamRequest, httpPath, method);
		
		logger.info("CbamMgmrImpl -> createVnf, responseStr is " + responseStr);
		
		CBAMCreateVnfResponse response = gson.fromJson(responseStr, CBAMCreateVnfResponse.class);
		
		return response;
	}
	
	/* (non-Javadoc)
	 * @see com.nokia.vfcadaptor.cbam.impl.CbamMgmrInf#instantiateVnf(com.nokia.vfcadaptor.cbam.bo.CBAMInstantiateVnfRequest, java.lang.String)
	 */
	public CBAMInstantiateVnfResponse instantiateVnf(CBAMInstantiateVnfRequest cbamRequest, String vnfInstanceId) throws ClientProtocolException, IOException {
		String httpPath = String.format(CommonConstants.CbamInstantiateVnfPath, vnfInstanceId);
		RequestMethod method = RequestMethod.POST;
			
		String responseStr = operateCbamHttpTask(cbamRequest, httpPath, method);
		
		logger.info("CbamMgmrImpl -> instantiateVnf, responseStr is " + responseStr);
		
		CBAMInstantiateVnfResponse response = gson.fromJson(responseStr, CBAMInstantiateVnfResponse.class);
		
		return response;
	}
	
	public CBAMTerminateVnfResponse terminateVnf(CBAMTerminateVnfRequest cbamRequest, String vnfInstanceId) throws ClientProtocolException, IOException {
		String httpPath = String.format(CommonConstants.CbamTerminateVnfPath, vnfInstanceId);
		RequestMethod method = RequestMethod.POST;
		
		String responseStr = operateCbamHttpTask(cbamRequest, httpPath, method);
		
		logger.info("CbamMgmrImpl -> terminateVnf, responseStr is " + responseStr);
		
		CBAMTerminateVnfResponse response = gson.fromJson(responseStr, CBAMTerminateVnfResponse.class);
		
		return response;
	}
	
	public void deleteVnf(String vnfInstanceId) throws ClientProtocolException, IOException {
		String httpPath = String.format(CommonConstants.CbamDeleteVnfPath, vnfInstanceId);
		RequestMethod method = RequestMethod.DELETE;
		
		operateCbamHttpTask(null, httpPath, method);
		
		logger.info("CbamMgmrImpl -> deleteVnf.");
	}
	
	public CBAMScaleVnfResponse scaleVnf(CBAMScaleVnfRequest cbamRequest, String vnfInstanceId) throws ClientProtocolException, IOException {
		String httpPath = String.format(CommonConstants.CbamScaleVnfPath, vnfInstanceId);
		RequestMethod method = RequestMethod.POST;
			
		String responseStr = operateCbamHttpTask(cbamRequest, httpPath, method);
		
		CBAMScaleVnfResponse response = gson.fromJson(responseStr, CBAMScaleVnfResponse.class);
		
		return response;
	}

	public CBAMHealVnfResponse healVnf(CBAMHealVnfRequest cbamRequest, String vnfInstanceId) throws ClientProtocolException, IOException {
		String httpPath = String.format(CommonConstants.CbamHealVnfPath, vnfInstanceId);
		RequestMethod method = RequestMethod.POST;
			
		String responseStr = operateCbamHttpTask(cbamRequest, httpPath, method);
		
		logger.info("CbamMgmrImpl -> healVnf, responseStr is " + responseStr);
		
		CBAMHealVnfResponse response = gson.fromJson(responseStr, CBAMHealVnfResponse.class);
		
		return response;
	}
	
	public CBAMQueryVnfResponse queryVnf(String vnfInstanceId) throws ClientProtocolException, IOException {
		String httpPath = String.format(CommonConstants.CbamQueryVnfPath, vnfInstanceId);
		RequestMethod method = RequestMethod.GET;
		
		String responseStr = operateCbamHttpTask(null, httpPath, method);
		
		logger.info("CbamMgmrImpl -> queryVnf, responseStr is " + responseStr);
		
		CBAMQueryVnfResponse response = gson.fromJson(responseStr, CBAMQueryVnfResponse.class);
		
		return response;
	}

	public String operateCbamHttpTask(Object httpBodyObj, String httpPath, RequestMethod method) throws ClientProtocolException, IOException {
		String token = null;
		try {
			token = retrieveToken();
		} catch (JSONException e) {
			logger.error("retrieveTokenError ", e);
		}
	
		String url= adaptorEnv.getCbamApiUriFront() + httpPath;
		
		HashMap<String, String> map = new HashMap<>();
		map.put(CommonConstants.AUTHORIZATION, "bearer " + token);
		map.put(CommonConstants.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		
		String responseStr = httpClientProcessor.process(url, method, map, gson.toJson(httpBodyObj)).getContent();
		
		return responseStr;
	}

	public CBAMQueryOperExecutionResponse queryOperExecution(String execId) throws ClientProtocolException, IOException{
		String httpPath = String.format(CommonConstants.CbamGetOperStatusPath, execId);
		RequestMethod method = RequestMethod.GET;
		
		String responseStr = operateCbamHttpTask(null, httpPath, method);
		
		logger.info("CbamMgmrImpl -> CBAMQueryOperExecutionResponse, responseStr is " + responseStr);
		
		CBAMQueryOperExecutionResponse response = gson.fromJson(responseStr, CBAMQueryOperExecutionResponse.class);
		
		return response;
	}

	public void setAdaptorEnv(AdaptorEnv adaptorEnv) {
		this.adaptorEnv = adaptorEnv;
	}
	
}
