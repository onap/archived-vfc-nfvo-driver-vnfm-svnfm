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

package com.nokia.vfcadaptor.adaptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nokia.vfcadaptor.catalog.inf.CatalogMgmrInf;
import com.nokia.vfcadaptor.cbam.bo.CBAMCreateVnfRequest;
import com.nokia.vfcadaptor.cbam.bo.CBAMCreateVnfResponse;
import com.nokia.vfcadaptor.cbam.bo.CBAMHealVnfRequest;
import com.nokia.vfcadaptor.cbam.bo.CBAMHealVnfResponse;
import com.nokia.vfcadaptor.cbam.bo.CBAMQueryVnfResponse;
import com.nokia.vfcadaptor.cbam.bo.CBAMScaleVnfRequest;
import com.nokia.vfcadaptor.cbam.bo.CBAMScaleVnfResponse;
import com.nokia.vfcadaptor.cbam.bo.CBAMTerminateVnfRequest;
import com.nokia.vfcadaptor.cbam.bo.CBAMTerminateVnfResponse;
import com.nokia.vfcadaptor.cbam.inf.CbamMgmrInf;
import com.nokia.vfcadaptor.constant.CommonConstants;
import com.nokia.vfcadaptor.exception.VnfmDriverException;
import com.nokia.vfcadaptor.nslcm.bo.NslcmGrantVnfRequest;
import com.nokia.vfcadaptor.nslcm.bo.NslcmGrantVnfResponse;
import com.nokia.vfcadaptor.nslcm.bo.VnfmInfo;
import com.nokia.vfcadaptor.nslcm.inf.NslcmMgmrInf;
import com.nokia.vfcadaptor.vnfmdriver.bo.HealVnfRequest;
import com.nokia.vfcadaptor.vnfmdriver.bo.HealVnfResponse;
import com.nokia.vfcadaptor.vnfmdriver.bo.InstantiateVnfRequest;
import com.nokia.vfcadaptor.vnfmdriver.bo.InstantiateVnfResponse;
import com.nokia.vfcadaptor.vnfmdriver.bo.OperStatusVnfResponse;
import com.nokia.vfcadaptor.vnfmdriver.bo.QueryVnfResponse;
import com.nokia.vfcadaptor.vnfmdriver.bo.ScaleVnfRequest;
import com.nokia.vfcadaptor.vnfmdriver.bo.ScaleVnfResponse;
import com.nokia.vfcadaptor.vnfmdriver.bo.TerminateVnfRequest;
import com.nokia.vfcadaptor.vnfmdriver.bo.TerminateVnfResponse;
import com.nokia.vfcadaptor.vnfmdriver.bo.entity.ResponseDescriptor;
import com.nokia.vfcadaptor.vnfmdriver.bo.entity.ResponseHistoryList;
import com.nokia.vfcadaptor.vnfmdriver.inf.VnfmDriverMgmrInf;

@Component
public class VnfmDriverMgmrIml implements VnfmDriverMgmrInf{
	private Logger logger = Logger.getLogger(VnfmDriverMgmrIml.class);
	
	@Autowired
	Driver2CbamRequestConverter requestConverter;
	
	@Autowired
	Cbam2DriverResponseConverter responseConverter;
	
	@Autowired
	private CbamMgmrInf cbamMgmr;
	
	@Autowired
	private CatalogMgmrInf catalogMgmr;
	
	@Autowired
	private NslcmMgmrInf nslcmMgmr;
	
	public InstantiateVnfResponse instantiateVnf(InstantiateVnfRequest driverRequest, String vnfmId) throws VnfmDriverException {
		InstantiateVnfResponse driverResponse;
		try {
			//step 1: query vnfm info
			VnfmInfo vnfmInfo = nslcmMgmr.queryVnfm(vnfmId);
			
			if(vnfmInfo == null || vnfmId.equalsIgnoreCase(vnfmInfo.getVnfmId()))
			{
				throw new VnfmDriverException(HttpStatus.SC_INTERNAL_SERVER_ERROR, CommonConstants.HTTP_ERROR_DESC_500);
			}
			
			//step 3: create vnf
			CBAMCreateVnfRequest cbamRequest = requestConverter.createrqConvert(driverRequest);
			CBAMCreateVnfResponse cbamResponse = cbamMgmr.createVnf(cbamRequest);
			driverResponse = responseConverter.createspConvert(cbamResponse);
			
			String vnfInstanceId = driverResponse.getVnfInstanceId();
			String jobId = driverResponse.getJobId();
			continueInstantiateVnf(driverRequest, vnfInstanceId, jobId);
			
			
		} catch (Exception e) {
			throw new VnfmDriverException(HttpStatus.SC_INTERNAL_SERVER_ERROR, CommonConstants.HTTP_ERROR_DESC_500);
		}
		
        return driverResponse;
	}

	public void continueInstantiateVnf(InstantiateVnfRequest driverRequest, String vnfInstanceId, String jobId) {
		InstantiateVnfContinueRunnable runnable = new InstantiateVnfContinueRunnable(driverRequest, vnfInstanceId, jobId,
				nslcmMgmr, catalogMgmr, cbamMgmr, requestConverter);
		
		Thread thread = new Thread(runnable);
		
		thread.run();
	}

	public TerminateVnfResponse terminateVnf(TerminateVnfRequest driverRequest, String vnfmId, String vnfInstanceId) {
		TerminateVnfResponse driverResponse;
		try {
			nslcmMgmr.queryVnfm(vnfmId);
			driverResponse = generateTerminateVnfResponse(vnfInstanceId);
			continueTerminateVnf(driverRequest, vnfInstanceId);
			
		} catch (Exception e) {
			throw new VnfmDriverException(HttpStatus.SC_INTERNAL_SERVER_ERROR, CommonConstants.HTTP_ERROR_DESC_500);
		}
		
        return driverResponse;
	}

	private TerminateVnfResponse generateTerminateVnfResponse(String vnfInstanceId) {
		TerminateVnfResponse response = new TerminateVnfResponse();
		//TODO
		response.setJobId("");
		return response;
	}

	public void continueTerminateVnf(TerminateVnfRequest driverRequest, String vnfInstanceId) {
		TerminateVnfContinueRunnable runnable = new TerminateVnfContinueRunnable(driverRequest, vnfInstanceId,
				nslcmMgmr, cbamMgmr, requestConverter);
		
		Thread thread = new Thread(runnable);
		
		thread.run();
	}


	public QueryVnfResponse queryVnf(String vnfmId, String vnfInstanceId) {
		QueryVnfResponse driverResponse;
		try {
			nslcmMgmr.queryVnfm(vnfmId);
			CBAMQueryVnfResponse cbamResponse = cbamMgmr.queryVnf(vnfInstanceId);
			driverResponse = responseConverter.queryspConvert(cbamResponse);
		} catch (Exception e) {
			throw new VnfmDriverException(HttpStatus.SC_INTERNAL_SERVER_ERROR, CommonConstants.HTTP_ERROR_DESC_500);
		}
		
        return driverResponse;
	}

	public OperStatusVnfResponse getOperStatus(String vnfmId, String jobId) {
		OperStatusVnfResponse response = new OperStatusVnfResponse();
		
		List<ResponseHistoryList> list=new ArrayList<ResponseHistoryList>();
		ResponseHistoryList relist=new ResponseHistoryList();
		relist.setErrorCode(41);
		relist.setProgress(40);
		relist.setResponseId(1);
		relist.setStatus("proccessing");
		relist.setStatusDescription("OMC VMs are decommissioned in VIM");
		ResponseDescriptor res=new ResponseDescriptor();
		res.setErrorCode(41);
		res.setProgress(40);
		res.setResponseId(1);
		res.setStatus("proccessing");
		res.setStatusDescription("OMC VMs are decommissioned in VIM");
		for(int i=0; i<2;i++) {
			relist.setProgress(4+i);	
			list.add(relist);
		}
		res.setResponseHistoryList(list);
		
		response.setJobId("Jobid="+jobId);
		response.setResponseDescriptor(res);
		
		return response;
	}

	public ScaleVnfResponse scaleVnf(ScaleVnfRequest driverRequest, String vnfmId, String vnfInstanceId) throws VnfmDriverException {
		ScaleVnfResponse driverResponse;
		try {
			nslcmMgmr.queryVnfm(vnfmId);
			CBAMScaleVnfRequest cbamRequest = requestConverter.scaleconvert(driverRequest);
			CBAMScaleVnfResponse cbamResponse = cbamMgmr.scaleVnf(cbamRequest, vnfInstanceId);
			driverResponse = responseConverter.scalespconvert(cbamResponse);
		} catch (Exception e) {
			throw new VnfmDriverException(HttpStatus.SC_INTERNAL_SERVER_ERROR, CommonConstants.HTTP_ERROR_DESC_500);
		}
		
        return driverResponse;
	}

	public HealVnfResponse healVnf(HealVnfRequest driverRequest, String vnfmId, String vnfInstanceId) throws VnfmDriverException {
		HealVnfResponse driverResponse;
		try {
			nslcmMgmr.queryVnfm(vnfmId);
			CBAMHealVnfRequest cbamRequest = requestConverter.healconvert(driverRequest);
			CBAMHealVnfResponse cbamResponse = cbamMgmr.healVnf(cbamRequest, vnfInstanceId);
			driverResponse = responseConverter.healspconvert(cbamResponse);
		} catch (Exception e) {
			throw new VnfmDriverException(HttpStatus.SC_INTERNAL_SERVER_ERROR, CommonConstants.HTTP_ERROR_DESC_500);
		}
		
        return driverResponse;
	}

}
