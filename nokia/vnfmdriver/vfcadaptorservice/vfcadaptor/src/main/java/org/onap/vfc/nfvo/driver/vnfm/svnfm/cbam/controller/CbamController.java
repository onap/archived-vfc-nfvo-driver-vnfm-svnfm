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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.controller;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.catalog.bo.CatalogQueryVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMCreateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMHealVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMHealVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMInstantiateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMInstantiateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMOperExecutVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMOperExecutVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMQueryVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMScaleVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMScaleVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMTerminateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMTerminateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.OperationExecution;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.ProblemDetails;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.VnfInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.common.bo.AdaptorEnv;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonConstants;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.http.client.HttpRequestProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

@Controller
@RequestMapping(value = "/vnfm/lcm/v3")
public class CbamController {
	private static final Logger logger = LogManager.getLogger("CbamController");
	@Autowired 
	private AdaptorEnv adaptorEnv;
	
	@Autowired
	private HttpClientBuilder httpClientBuilder;
	
	private Gson gson = new Gson();
	
	@RequestMapping(value = "/vnfs", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CBAMCreateVnfResponse createVnf(CBAMInstantiateVnfRequest request) throws ClientProtocolException, IOException
    {
		 CBAMCreateVnfResponse response = new  CBAMCreateVnfResponse();
			String url=adaptorEnv.getCbamApiUriFront() + String.format(CommonConstants.CbamCreateVnfPath);
			HttpRequestProcessor processor = new HttpRequestProcessor(httpClientBuilder, RequestMethod.GET);
			
			String responseStr = processor.process(url);
			
			logger.info("CbamMgmrImpl -> createVnf, responseStr is " + responseStr);
			
			CatalogQueryVnfResponse resp = gson.fromJson(responseStr, CatalogQueryVnfResponse.class);
			
			
	        return response;
    }
	@RequestMapping(value = "/vnfs/{vnfInstanceId}/instantiate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CBAMInstantiateVnfResponse initiateVnf(CBAMInstantiateVnfRequest request, @PathVariable("vnfInstanceId") String vnfInstanceId) throws ClientProtocolException, IOException
    {
		CBAMInstantiateVnfResponse response = new CBAMInstantiateVnfResponse();
		String url=adaptorEnv.getCbamApiUriFront() + String.format(CommonConstants.CbamInstantiateVnfPath,vnfInstanceId);
		HttpRequestProcessor processor = new HttpRequestProcessor(httpClientBuilder, RequestMethod.GET);
		
		String responseStr = processor.process(url);
		
		logger.info("CbamMgmrImpl -> initiateVnf, responseStr is " + responseStr);
		
		CatalogQueryVnfResponse resp = gson.fromJson(responseStr, CatalogQueryVnfResponse.class);
		
		
        return response;
    }
	
	@RequestMapping(value = "/vnfs/{vnfInstanceId}/terminate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CBAMTerminateVnfResponse terminateVnf(CBAMTerminateVnfRequest request, @PathVariable("vnfInstanceId") String vnfInstanceId) throws ClientProtocolException, IOException
    {
		CBAMTerminateVnfResponse response = new CBAMTerminateVnfResponse();
		String url=adaptorEnv.getCbamApiUriFront() + String.format(CommonConstants.CbamTerminateVnfPath,vnfInstanceId);
		HttpRequestProcessor processor = new HttpRequestProcessor(httpClientBuilder, RequestMethod.GET);
		
		String responseStr = processor.process(url);
		
		logger.info("CbamMgmrImpl -> terminateVnf, responseStr is " + responseStr);
		
		CatalogQueryVnfResponse resp = gson.fromJson(responseStr, CatalogQueryVnfResponse.class);
        return response;
    }
	
	@RequestMapping(value = "/vnfs/{vnfInstanceId}/scale", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CBAMScaleVnfResponse scaleVnf(CBAMScaleVnfRequest request, @PathVariable("vnfInstanceId") String vnfInstanceId) throws ClientProtocolException, IOException
    {
		CBAMScaleVnfResponse response = new CBAMScaleVnfResponse();
		String url=adaptorEnv.getCbamApiUriFront() + String.format(CommonConstants.CbamScaleVnfPath,vnfInstanceId);
		HttpRequestProcessor processor = new HttpRequestProcessor(httpClientBuilder, RequestMethod.GET);
		
		String responseStr = processor.process(url);
		
		logger.info("CbamMgmrImpl -> scaleVnf, responseStr is " + responseStr);
		
		CatalogQueryVnfResponse resp = gson.fromJson(responseStr, CatalogQueryVnfResponse.class);
        return response;
    }

	@RequestMapping(value = "/vnfs/{vnfInstanceId}/heal", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CBAMHealVnfResponse healVnf(CBAMHealVnfRequest request, @PathVariable("vnfInstanceId") String vnfInstanceId) throws ClientProtocolException, IOException
    {
		 CBAMHealVnfResponse response = new  CBAMHealVnfResponse();
		 String url=adaptorEnv.getCbamApiUriFront() + String.format(CommonConstants.CbamHealVnfPath,vnfInstanceId);
			HttpRequestProcessor processor = new HttpRequestProcessor(httpClientBuilder, RequestMethod.GET);
			
			String responseStr = processor.process(url);
			
			logger.info("CbamMgmrImpl -> healVnf, responseStr is " + responseStr);
			
			CatalogQueryVnfResponse resp = gson.fromJson(responseStr, CatalogQueryVnfResponse.class);
        return response;
    }
	
	
	@RequestMapping(value = "/vnfs/{vnfInstanceId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CBAMQueryVnfResponse queryVnf(@PathVariable("vnfInstanceId") String vnfInstanceId) throws ClientProtocolException, IOException
    {
		CBAMQueryVnfResponse response = new  CBAMQueryVnfResponse();
		String url=adaptorEnv.getCbamApiUriFront() + String.format(CommonConstants.CbamQueryVnfPath, vnfInstanceId);
		HttpRequestProcessor processor = new HttpRequestProcessor(httpClientBuilder, RequestMethod.GET);
		
		String responseStr = processor.process(url);
		
		logger.info("CbamMgmrImpl -> queryVnfPackage, responseStr is " + responseStr);
		
		CatalogQueryVnfResponse resp = gson.fromJson(responseStr, CatalogQueryVnfResponse.class);
		
		
        return response;
    }
	
	
	@RequestMapping(value = "/operation_executions/{operationExecutionId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CBAMOperExecutVnfResponse operVnf(@PathVariable("operationExecutionId") String operationExecutionId) throws ClientProtocolException, IOException
    {
		CBAMOperExecutVnfResponse response = new  CBAMOperExecutVnfResponse();
		 String url=adaptorEnv.getCbamApiUriFront() + String.format(CommonConstants.CbamGetOperStatusPath,operationExecutionId);
			HttpRequestProcessor processor = new HttpRequestProcessor(httpClientBuilder, RequestMethod.GET);
			
			String responseStr = processor.process(url);
			
			logger.info("CbamMgmrImpl -> operVnf, responseStr is " + responseStr);
			
			CatalogQueryVnfResponse resp = gson.fromJson(responseStr, CatalogQueryVnfResponse.class);
        return response;
    }
	
	
	

}
