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

package com.nokia.vfcadaptor.cbam.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nokia.vfcadaptor.cbam.bo.CBAMCreateVnfResponse;
import com.nokia.vfcadaptor.cbam.bo.CBAMHealVnfRequest;
import com.nokia.vfcadaptor.cbam.bo.CBAMHealVnfResponse;
import com.nokia.vfcadaptor.cbam.bo.CBAMInstantiateVnfRequest;
import com.nokia.vfcadaptor.cbam.bo.CBAMInstantiateVnfResponse;
import com.nokia.vfcadaptor.cbam.bo.CBAMOperExecutVnfRequest;
import com.nokia.vfcadaptor.cbam.bo.CBAMOperExecutVnfResponse;
import com.nokia.vfcadaptor.cbam.bo.CBAMQueryVnfResponse;
import com.nokia.vfcadaptor.cbam.bo.CBAMScaleVnfRequest;
import com.nokia.vfcadaptor.cbam.bo.CBAMScaleVnfResponse;
import com.nokia.vfcadaptor.cbam.bo.CBAMTerminateVnfRequest;
import com.nokia.vfcadaptor.cbam.bo.CBAMTerminateVnfResponse;
import com.nokia.vfcadaptor.cbam.bo.entity.OperationExecution;
import com.nokia.vfcadaptor.cbam.bo.entity.ProblemDetails;
import com.nokia.vfcadaptor.cbam.bo.entity.VnfInfo;

@Controller
@RequestMapping(value = "/vnfm/lcm/v3")
public class CbamController {
	
	@RequestMapping(value = "/vnfs", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CBAMCreateVnfResponse createVnf(CBAMInstantiateVnfRequest request)
    {
		 CBAMCreateVnfResponse response = new  CBAMCreateVnfResponse();
		 VnfInfo op=new VnfInfo();
		op.setName("VNF_001");
		ProblemDetails pr=new ProblemDetails();
		pr.setStatus(200);
		response.setVnfInfo(op);
		response.setProblemDetails(pr);
        return response;
    }
	@RequestMapping(value = "/vnfs/{vnfInstanceId}/instantiate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CBAMInstantiateVnfResponse initiateVnf(CBAMInstantiateVnfRequest request, @PathVariable("vnfInstanceId") String vnfInstanceId)
    {
		CBAMInstantiateVnfResponse response = new CBAMInstantiateVnfResponse();
//		response.setJobid("11234");
		
        return response;
    }
	
	@RequestMapping(value = "/vnfs/{vnfInstanceId}/terminate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CBAMTerminateVnfResponse terminateVnf(CBAMTerminateVnfRequest request, @PathVariable("vnfInstanceId") String vnfInstanceId)
    {
		CBAMTerminateVnfResponse response = new CBAMTerminateVnfResponse();
		OperationExecution op=new OperationExecution();
		op.setGrantId("89");
		ProblemDetails pr=new ProblemDetails();
		pr.setStatus(200);
		response.setOperationExecution(op);
		response.setProblemDetails(pr);
        return response;
    }
	
	@RequestMapping(value = "/vnfs/{vnfInstanceId}/scale", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CBAMScaleVnfResponse scaleVnf(CBAMScaleVnfRequest request, @PathVariable("vnfInstanceId") String vnfInstanceId)
    {
		CBAMScaleVnfResponse response = new CBAMScaleVnfResponse();
		OperationExecution op=new OperationExecution();
		op.setGrantId("89");
		ProblemDetails pr=new ProblemDetails();
		pr.setStatus(200);
		response.setOperationExecution(op);
		response.setProblemDetails(pr);
        return response;
    }

	@RequestMapping(value = "/vnfs/{vnfInstanceId}/heal", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CBAMHealVnfResponse healVnf(CBAMHealVnfRequest request, @PathVariable("vnfInstanceId") String vnfInstanceId)
    {
		 CBAMHealVnfResponse response = new  CBAMHealVnfResponse();
		OperationExecution op=new OperationExecution();
		op.setGrantId("89");
		ProblemDetails pr=new ProblemDetails();
		pr.setStatus(200);
		response.setOperationExecution(op);
		response.setProblemDetails(pr);
        return response;
    }
	
	
	@RequestMapping(value = "/vnfs/{vnfInstanceId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CBAMQueryVnfResponse queryVnf(@PathVariable("vnfInstanceId") String vnfInstanceId)
    {
		CBAMQueryVnfResponse response = new  CBAMQueryVnfResponse();
		VnfInfo op=new VnfInfo();
		op.setName("VNF_001");
		ProblemDetails pr=new ProblemDetails();
		pr.setStatus(200);
		response.setVnfInfo(op);
		response.setProblemDetails(pr);
        return response;
    }
	
	
	@RequestMapping(value = "/operation_executions/{operationExecutionId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CBAMOperExecutVnfResponse operVnf(@PathVariable("operationExecutionId") String operationExecutionId)
    {
		CBAMOperExecutVnfResponse response = new  CBAMOperExecutVnfResponse();
		ProblemDetails pr=new ProblemDetails();
		pr.setStatus(200);
		response.setProblemDetails(pr);
        return response;
    }
	
	@RequestMapping(value = "/operation_executions/{operationExecutionId}/cancel", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE,consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CBAMOperExecutVnfResponse operCancelVnf(CBAMOperExecutVnfRequest request,@PathVariable("operationExecutionId") String operationExecutionId)
    {
		CBAMOperExecutVnfResponse response = new  CBAMOperExecutVnfResponse();
		ProblemDetails pr=new ProblemDetails();
		pr.setStatus(200);
		response.setProblemDetails(pr);
        return response;
    }
	

}
