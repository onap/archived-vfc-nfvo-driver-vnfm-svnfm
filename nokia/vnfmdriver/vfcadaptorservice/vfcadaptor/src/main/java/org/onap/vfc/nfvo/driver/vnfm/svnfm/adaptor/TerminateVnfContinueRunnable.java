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

import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMTerminateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMTerminateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.inf.CbamMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonConstants;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonEnum;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonEnum.LifecycleOperation;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.bean.VnfmJobExecutionInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.repository.VnfmJobExecutionRepository;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.NslcmGrantVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.NslcmGrantVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.NslcmNotifyLCMEventsRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.AddResource;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.ResourceDefinition;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.inf.NslcmMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.TerminateVnfRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TerminateVnfContinueRunnable implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(TerminateVnfContinueRunnable.class);

	private CbamMgmrInf cbamMgmr;
	private NslcmMgmrInf nslcmMgmr;
	
	private TerminateVnfRequest driverRequest;
	private String vnfInstanceId;
	private String jobId;
	private String vnfmId;
	private VnfmJobExecutionRepository jobDbMgmr;
	
	private Driver2CbamRequestConverter requestConverter;
	
	public TerminateVnfContinueRunnable(String vnfmId, TerminateVnfRequest driverRequest, String vnfInstanceId, String jobId,
			NslcmMgmrInf nslcmMgmr, CbamMgmrInf cbamMgmr, Driver2CbamRequestConverter requestConverter, VnfmJobExecutionRepository dbManager)
	{
		this.driverRequest = driverRequest;
		this.vnfInstanceId = vnfInstanceId;
		this.nslcmMgmr = nslcmMgmr; 
		this.cbamMgmr = cbamMgmr;
		this.requestConverter = requestConverter;
		this.jobId = jobId;
		this.jobDbMgmr = dbManager;
		this.vnfmId = vnfmId;
	}
	
	private void handleGrant(){
		try {
			NslcmGrantVnfRequest grantRequest = buildNslcmGrantVnfRequest();
			nslcmMgmr.grantVnf(grantRequest);
		} catch (Exception e) {
			logger.error("TerminateVnfContinueRunnable --> handleGrant error.", e);
		}
	}
	
	public void run() {
		handleGrant();
		handleTerminate();
		handleDelete();
	}
	
	private void handleDelete() {
		try {
			cbamMgmr.deleteVnf(vnfInstanceId);
		} catch (Exception e) {
			logger.error("TerminateVnfContinueRunnable --> handleDelete error.", e);
		}
	}

	private CBAMTerminateVnfResponse handleTerminate() {
		CBAMTerminateVnfResponse cbamResponse = null;
		try {
			CBAMTerminateVnfRequest  modifyReq = requestConverter.terminateReqConvert(driverRequest);
			cbamResponse = cbamMgmr.terminateVnf(modifyReq, vnfInstanceId);
			handleCbamTerminateResponse(cbamResponse, jobId);
		} catch (Exception e) {
			logger.error("TerminateVnfContinueRunnable --> handleTerminate error.", e);
		}
		
		try {
			NslcmNotifyLCMEventsRequest nslcmNotifyReq = buildNslcmNotifyLCMEventsRequest(cbamResponse);
			nslcmMgmr.notifyVnf(nslcmNotifyReq, vnfmId, vnfInstanceId);
		} catch (Exception e) {
			logger.error("TerminateVnfContinueRunnable --> handleNotify error.", e);
		}
		
		
		return cbamResponse;
	}

	private void handleCbamTerminateResponse(CBAMTerminateVnfResponse cbamResponse, String jobId) {
		VnfmJobExecutionInfo jobInfo = jobDbMgmr.findOne(Long.parseLong(jobId));
		
		jobInfo.setVnfmExecutionId(cbamResponse.getId());
		if(CommonEnum.OperationStatus.FAILED == cbamResponse.getStatus()) {
			jobInfo.setStatus(CommonConstants.CBAM_OPERATION_STATUS_ERROR);
		}
		else {
			jobInfo.setStatus(cbamResponse.getStatus().toString());
		}
		jobDbMgmr.save(jobInfo);
	}
	
	private NslcmGrantVnfRequest buildNslcmGrantVnfRequest() {
		NslcmGrantVnfRequest request = new NslcmGrantVnfRequest();
		
		request.setVnfInstanceId(vnfInstanceId);
		request.setLifecycleOperation(LifecycleOperation.Terminal);
		request.setJobId(jobId);
		
		ResourceDefinition resource = getFreeVnfResource();
		List<ResourceDefinition> resourceList = new ArrayList<ResourceDefinition>();
		resourceList.add(resource);
		request.setRemoveResource(resourceList);
		
		return request;
	}
	
	private ResourceDefinition getFreeVnfResource() {
		ResourceDefinition def = new ResourceDefinition();
		def.setVnfInstanceId(vnfInstanceId);
		def.setVimId("001");
		List<AddResource> resources = new ArrayList<>();
		AddResource res = new AddResource();
		res.setVdu("1");
		res.setType("vdu");
		res.setResourceDefinitionId(2);
		resources.add(res);
		def.setAddResource(resources);
		return def;
	}

	private NslcmNotifyLCMEventsRequest buildNslcmNotifyLCMEventsRequest(CBAMTerminateVnfResponse cbamResponse) {
		NslcmNotifyLCMEventsRequest request = new NslcmNotifyLCMEventsRequest();
		if(CommonEnum.OperationStatus.STARTED == cbamResponse.getStatus())
		{
			request.setStatus(CommonEnum.status.start);
		}
		else
		{
			request.setStatus(CommonEnum.status.result);
			
			//TODO the following are for the result
//			request.setAffectedVnfc(affectedVnfc);
//			request.setAffectedVI(affectedVI);
//			request.setAffectedVirtualStorage(affectedVirtualStorage);
		}
		
		request.setVnfInstanceId(vnfInstanceId);
		request.setOperation(CommonConstants.NSLCM_OPERATION_TERMINATE);
		request.setJobId(jobId);
		return request;
	}

	private void handleNslcmGrantResponse(NslcmGrantVnfResponse grantResponse) {
		// TODO Auto-generated method stub
		
	}

}
