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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.exception.VnfmDriverException;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.HealVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.HealVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.InstantiateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.InstantiateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.OperStatusVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.QueryVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.ScaleVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.ScaleVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.TerminateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.TerminateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.inf.VnfmDriverMgmrInf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

@Controller
@RequestMapping(value = "/api/nokiavnfmdriver/v1")
public class VnfmDriverController {
	private static final Logger logger = LogManager.getLogger("VnfmDriverController");
	
	@Autowired
	private VnfmDriverMgmrInf vnfmDriverMgmr;
	
	private Gson gson = new Gson();
	
	@RequestMapping(value = "/{vnfmId}/vnfs", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public InstantiateVnfResponse instantiateVnf(@RequestBody InstantiateVnfRequest request, @PathVariable("vnfmId") String vnfmId, HttpServletResponse httpResponse)
    {
		String jsonString = gson.toJson(request);
		logger.info("instantiateVnf request: vnfmId = " + vnfmId + ", bodyMessage is " + jsonString);
		
		InstantiateVnfResponse response = vnfmDriverMgmr.instantiateVnf(request, vnfmId);
		
		httpResponse.setStatus(HttpStatus.SC_CREATED);
		
		return response;
    }
	
	@RequestMapping(value = "/{vnfmId}/vnfs/{vnfInstanceId}/terminate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public TerminateVnfResponse terminateVnf(@RequestBody TerminateVnfRequest request, @PathVariable("vnfmId") String vnfmId, @PathVariable("vnfInstanceId") String vnfInstanceId, HttpServletResponse httpResponse)
    {
		String jsonString = gson.toJson(request);
		logger.info("terminateVnf request: vnfmId = " + vnfmId + ", vnfInstanceId = " + vnfInstanceId + ", bodyMessage is " + jsonString);
		
		try {
			TerminateVnfResponse response = vnfmDriverMgmr.terminateVnf(request, vnfmId, vnfInstanceId);
			httpResponse.setStatus(HttpStatus.SC_CREATED);
			return response;
		}
		catch(VnfmDriverException e)
		{
			try {
				httpResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
				httpResponse.sendError(e.getHttpStatus(), e.getMessage());
			} catch (IOException e1) {
				
			}
		}
		
		return null;
    }
	
	
	@RequestMapping(value = "/{vnfmId}/vnfs/{vnfInstanceId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public QueryVnfResponse queryVnf(@PathVariable("vnfmId") String vnfmId, @PathVariable("vnfInstanceId") String vnfInstanceId, HttpServletResponse httpResponse)
    {
		logger.info("queryVnf request: vnfmId = " + vnfmId + ", vnfInstanceId = " + vnfInstanceId);
		
		try {
			QueryVnfResponse response = vnfmDriverMgmr.queryVnf(vnfmId, vnfInstanceId);
			httpResponse.setStatus(HttpStatus.SC_CREATED);
			return response;
		}
		catch(VnfmDriverException e)
		{
			try {
				httpResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
				httpResponse.sendError(e.getHttpStatus(), e.getMessage());
			} catch (IOException e1) {
				
			}
		}
		
		return null;
    }
	@RequestMapping(value = "/{vnfmId}/jobs/{jobId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public OperStatusVnfResponse getOperStatus(@PathVariable("vnfmId") String vnfmId,@PathVariable("jobId") String jobId, HttpServletResponse httpResponse)
    {
		logger.info("getOperStatus request: vnfmId = " + vnfmId + ", jobId = " + jobId);
		
		try {
			OperStatusVnfResponse response = vnfmDriverMgmr.getOperStatus(vnfmId, jobId);
			httpResponse.setStatus(HttpStatus.SC_CREATED);
			return response;
		}
		catch(VnfmDriverException e)
		{
			try {
				httpResponse.sendError(e.getHttpStatus(), e.getMessage());
			} catch (IOException e1) {
				
			}
		}
		
		return null;
    }
	
	
	@RequestMapping(value = "/{vnfmId}/vnfs/{vnfInstanceId}/scale", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ScaleVnfResponse scaleVnf(@RequestBody ScaleVnfRequest request, @PathVariable("vnfmId") String vnfmId, @PathVariable("vnfInstanceId") String vnfInstanceId, HttpServletResponse httpResponse)
    {
		String jsonString = gson.toJson(request);
		logger.info("scaleVnf request: vnfmId = " + vnfmId + ", vnfInstanceId = " + vnfInstanceId + ", bodyMessage is " + jsonString);
		
		try {
			ScaleVnfResponse response = vnfmDriverMgmr.scaleVnf(request, vnfmId, vnfInstanceId);
			httpResponse.setStatus(HttpStatus.SC_CREATED);
			return response;
		}
		catch(VnfmDriverException e)
		{
			try {
				httpResponse.sendError(e.getHttpStatus(), e.getMessage());
			} catch (IOException e1) {
				
			}
		}
		
		return null;
    }
	
	
	@RequestMapping(value = "/{vnfmId}/vnfs/{vnfInstanceId}/heal", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HealVnfResponse healVnf(@RequestBody HealVnfRequest request, @PathVariable("vnfmId") String vnfmId, @PathVariable("vnfInstanceId") String vnfInstanceId, HttpServletResponse httpResponse)
    {
		String jsonString = gson.toJson(request);
		logger.info("healVnf request: vnfmId = " + vnfmId + ", vnfInstanceId = " + vnfInstanceId + ", bodyMessage is " + jsonString);
		
		try {
			HealVnfResponse response = vnfmDriverMgmr.healVnf(request, vnfmId, vnfInstanceId);
			httpResponse.setStatus(HttpStatus.SC_CREATED);
			return response;
		}
		catch(VnfmDriverException e)
		{
			try {
				httpResponse.sendError(e.getHttpStatus(), e.getMessage());
			} catch (IOException e1) {
				
			}
		}
		
		return null;
    }


}
