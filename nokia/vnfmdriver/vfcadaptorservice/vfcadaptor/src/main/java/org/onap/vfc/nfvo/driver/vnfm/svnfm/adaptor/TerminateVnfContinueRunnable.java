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

import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMQueryVnfResponse;
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
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.AffectedVnfc;
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
			boolean vnfAllowDelete = false;
			int i = 0;
			while(!vnfAllowDelete) {
				CBAMQueryVnfResponse queryResponse = cbamMgmr.queryVnf(vnfInstanceId);
				if(CommonEnum.InstantiationState.NOT_INSTANTIATED == queryResponse.getInstantiationState())
				{
					vnfAllowDelete = true;
					break;
				}
				i++;
				logger.info(i + ": The vnf's current status is " + queryResponse.getInstantiationState().name() + " is not ready for deleting, please wait ... ");
				Thread.sleep(30000);
			}
			prepareDelete(jobId);
			cbamMgmr.deleteVnf(vnfInstanceId);
		} catch (Exception e) {
			logger.error("TerminateVnfContinueRunnable --> handleDelete error.", e);
		}
	}
	
	private void prepareDelete(String jobId) {
		OperateTaskProgress.stopTerminateTimerTask();
		
		VnfmJobExecutionInfo jobInfo = jobDbMgmr.findOne(Long.parseLong(jobId));
		jobInfo.setStatus(CommonConstants.CBAM_OPERATION_STATUS_FINISH);
		jobDbMgmr.save(jobInfo);
		
		try {
			NslcmNotifyLCMEventsRequest nslcmNotifyReq = buildNslcmNotifyLCMEventsRequest();
			nslcmMgmr.notifyVnf(nslcmNotifyReq, vnfmId, vnfInstanceId);
		} catch (Exception e) {
			logger.error("TerminateVnfContinueRunnable --> handleNotify error.", e);
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
		
		return cbamResponse;
	}

	private void handleCbamTerminateResponse(CBAMTerminateVnfResponse cbamResponse, String jobId) {
		VnfmJobExecutionInfo jobInfo = jobDbMgmr.findOne(Long.parseLong(jobId));
		
		jobInfo.setVnfmExecutionId(cbamResponse.getId());
		if(CommonEnum.OperationStatus.FAILED == cbamResponse.getStatus()) {
			jobInfo.setStatus(CommonConstants.CBAM_OPERATION_STATUS_ERROR);
		}
		else {
			jobInfo.setStatus(CommonConstants.CBAM_OPERATION_STATUS_PROCESSING);
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

	private NslcmNotifyLCMEventsRequest buildNslcmNotifyLCMEventsRequest() {
		NslcmNotifyLCMEventsRequest request = new NslcmNotifyLCMEventsRequest();
		request.setStatus(CommonEnum.status.result);
		
		List<AffectedVnfc> vnfcs = modifyResourceTypeAsRemove(OperateTaskProgress.getAffectedVnfc());
		
		request.setAffectedVnfc(vnfcs);
		request.setVnfInstanceId(vnfInstanceId);
		request.setOperation(CommonConstants.NSLCM_OPERATION_TERMINATE);
		request.setJobId(jobId);
		return request;
	}

	private List<AffectedVnfc> modifyResourceTypeAsRemove(List<AffectedVnfc> affectedVnfc) {
		List<AffectedVnfc> vnfcs = affectedVnfc;
		if(vnfcs != null && !vnfcs.isEmpty()) {
			for(AffectedVnfc vnfc : vnfcs)
			{
				vnfc.setChangeType(CommonEnum.AffectchangeType.removed);
			}
		}
		
		return vnfcs;
	}

	private void handleNslcmGrantResponse(NslcmGrantVnfResponse grantResponse) {
		// TODO Auto-generated method stub
		
	}
}
