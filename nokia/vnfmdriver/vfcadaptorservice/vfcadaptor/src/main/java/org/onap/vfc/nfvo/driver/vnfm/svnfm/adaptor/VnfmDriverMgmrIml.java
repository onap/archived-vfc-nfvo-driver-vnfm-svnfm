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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.adaptor;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.catalog.inf.CatalogMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMCreateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMCreateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMHealVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMHealVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMQueryVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMScaleVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMScaleVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.inf.CbamMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonConstants;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.bean.VnfmJobExecutionInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.repository.VnfmJobExecutionRepository;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.exception.VnfmDriverException;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.VnfmInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.inf.NslcmMgmrInf;
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
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.entity.ResponseDescriptor;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.entity.ResponseHistoryList;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.inf.VnfmDriverMgmrInf;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;


@Component
public class VnfmDriverMgmrIml implements VnfmDriverMgmrInf{
	private static final Logger logger = LogManager.getLogger("VnfmDriverMgmrIml");
	
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
	
	@Autowired
	private VnfmJobExecutionRepository jobDbManager;
	
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
			CBAMCreateVnfRequest cbamRequest = requestConverter.createReqConvert(driverRequest);
			CBAMCreateVnfResponse cbamResponse = cbamMgmr.createVnf(cbamRequest);
			driverResponse = responseConverter.createRspConvert(cbamResponse);
			
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
				nslcmMgmr, catalogMgmr, cbamMgmr, requestConverter, jobDbManager);
		
		Thread thread = new Thread(runnable);
		
		thread.run();
	}

	public TerminateVnfResponse terminateVnf(TerminateVnfRequest driverRequest, String vnfmId, String vnfInstanceId) {
		TerminateVnfResponse driverResponse;
		try {
			VnfmInfo vnfmInfo = nslcmMgmr.queryVnfm(vnfmId);
			
			if(vnfmInfo == null || vnfmId.equalsIgnoreCase(vnfmInfo.getVnfmId()))
			{
				throw new VnfmDriverException(HttpStatus.SC_INTERNAL_SERVER_ERROR, CommonConstants.HTTP_ERROR_DESC_500);
			}
			driverResponse = generateTerminateVnfResponse(vnfInstanceId);
			String jobId = driverResponse.getJobId();
			continueTerminateVnf(driverRequest, vnfInstanceId, jobId);
			
		} catch (Exception e) {
			throw new VnfmDriverException(HttpStatus.SC_INTERNAL_SERVER_ERROR, CommonConstants.HTTP_ERROR_DESC_500);
		}
		
        return driverResponse;
	}

	private TerminateVnfResponse generateTerminateVnfResponse(String vnfInstanceId) {
		VnfmJobExecutionInfo jobInfo = new VnfmJobExecutionInfo();
		jobInfo.setVnfInstanceId(vnfInstanceId);
		jobInfo.setVnfmInterfceName(CommonConstants.NSLCM_OPERATION_TERMINATE);
		jobInfo.setStatus(CommonConstants.CBAM_OPERATION_STATUS_START);
		
		VnfmJobExecutionInfo jobInfo1=  jobDbManager.save(jobInfo);
		Long jobId = jobInfo1.getJobId();
		
		TerminateVnfResponse response = new TerminateVnfResponse();
		response.setJobId("" + jobId);
		return response;
	}

	public void continueTerminateVnf(TerminateVnfRequest driverRequest, String vnfInstanceId, String jobId) {
		TerminateVnfContinueRunnable runnable = new TerminateVnfContinueRunnable(driverRequest, vnfInstanceId, jobId,
				nslcmMgmr, cbamMgmr, requestConverter, jobDbManager);
		
		Thread thread = new Thread(runnable);
		
		thread.run();
	}


	public QueryVnfResponse queryVnf(String vnfmId, String vnfInstanceId) {
		QueryVnfResponse driverResponse;
		try {
			nslcmMgmr.queryVnfm(vnfmId);
			VnfmInfo vnfmInfo = nslcmMgmr.queryVnfm(vnfmId);
			
			if(vnfmInfo == null || vnfmId.equalsIgnoreCase(vnfmInfo.getVnfmId()))
			{
				throw new VnfmDriverException(HttpStatus.SC_INTERNAL_SERVER_ERROR, CommonConstants.HTTP_ERROR_DESC_500);
			}
			CBAMQueryVnfResponse cbamResponse = cbamMgmr.queryVnf(vnfInstanceId);
			driverResponse = responseConverter.queryRspConvert(cbamResponse);
		} catch (Exception e) {
			throw new VnfmDriverException(HttpStatus.SC_INTERNAL_SERVER_ERROR, CommonConstants.HTTP_ERROR_DESC_500);
		}
		
        return driverResponse;
	}

	public OperStatusVnfResponse getOperStatus(String vnfmId, String jobId)  throws VnfmDriverException {
		VnfmInfo vnfmInfo;
		try {
			vnfmInfo = nslcmMgmr.queryVnfm(vnfmId);
		}  catch (Exception e) {
			throw new VnfmDriverException(HttpStatus.SC_INTERNAL_SERVER_ERROR, CommonConstants.HTTP_ERROR_DESC_500);
		}
		
		if(vnfmInfo == null || vnfmId.equalsIgnoreCase(vnfmInfo.getVnfmId()))
		{
			throw new VnfmDriverException(HttpStatus.SC_INTERNAL_SERVER_ERROR, CommonConstants.HTTP_ERROR_DESC_500);
		}
		
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
			VnfmInfo vnfmInfo = nslcmMgmr.queryVnfm(vnfmId);
			
			if(vnfmInfo == null || vnfmId.equalsIgnoreCase(vnfmInfo.getVnfmId()))
			{
				throw new VnfmDriverException(HttpStatus.SC_INTERNAL_SERVER_ERROR, CommonConstants.HTTP_ERROR_DESC_500);
			}
			CBAMScaleVnfRequest cbamRequest = requestConverter.scaleReqconvert(driverRequest);
			CBAMScaleVnfResponse cbamResponse = cbamMgmr.scaleVnf(cbamRequest, vnfInstanceId);
			driverResponse = responseConverter.scaleRspConvert(cbamResponse);
		} catch (Exception e) {
			throw new VnfmDriverException(HttpStatus.SC_INTERNAL_SERVER_ERROR, CommonConstants.HTTP_ERROR_DESC_500);
		}
		
        return driverResponse;
	}

	public HealVnfResponse healVnf(HealVnfRequest driverRequest, String vnfmId, String vnfInstanceId) throws VnfmDriverException {
		HealVnfResponse driverResponse;
		try {
			VnfmInfo vnfmInfo = nslcmMgmr.queryVnfm(vnfmId);
			
			if(vnfmInfo == null || vnfmId.equalsIgnoreCase(vnfmInfo.getVnfmId()))
			{
				throw new VnfmDriverException(HttpStatus.SC_INTERNAL_SERVER_ERROR, CommonConstants.HTTP_ERROR_DESC_500);
			}
			CBAMHealVnfRequest cbamRequest = requestConverter.healReqConvert(driverRequest);
			CBAMHealVnfResponse cbamResponse = cbamMgmr.healVnf(cbamRequest, vnfInstanceId);
			driverResponse = responseConverter.healRspConvert(cbamResponse);
		} catch (Exception e) {
			throw new VnfmDriverException(HttpStatus.SC_INTERNAL_SERVER_ERROR, CommonConstants.HTTP_ERROR_DESC_500);
		}
		
        return driverResponse;
	}

}
