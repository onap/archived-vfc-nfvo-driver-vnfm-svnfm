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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMTerminateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMTerminateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.inf.CbamMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonConstants;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonEnum;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonEnum.LifecycleOperation;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.repository.VnfmJobExecutionRepository;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.NslcmGrantVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.NslcmGrantVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.NslcmNotifyLCMEventsRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.ResourceDefinition;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.inf.NslcmMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.TerminateVnfRequest;



public class TerminateVnfContinueRunnable implements Runnable {
	private static final Logger logger = LogManager.getLogger("TerminateVnfContinueRunnable");

	private CbamMgmrInf cbamMgmr;
	private NslcmMgmrInf nslcmMgmr;
	
	private TerminateVnfRequest driverRequest;
	private String vnfInstanceId;
	private String jobId;
	private VnfmJobExecutionRepository jobDbManager;
	
	private Driver2CbamRequestConverter requestConverter;
	
	public TerminateVnfContinueRunnable(TerminateVnfRequest driverRequest, String vnfInstanceId, String jobId,
			NslcmMgmrInf nslcmMgmr, CbamMgmrInf cbamMgmr, Driver2CbamRequestConverter requestConverter, VnfmJobExecutionRepository dbManager)
	{
		this.driverRequest = driverRequest;
		this.vnfInstanceId = vnfInstanceId;
		this.nslcmMgmr = nslcmMgmr; 
		this.cbamMgmr = cbamMgmr;
		this.requestConverter = requestConverter;
		this.jobId = jobId;
		this.jobDbManager = dbManager;
	}
	
	public void run() {
		try {
			NslcmGrantVnfRequest grantRequest = buildNslcmGrantVnfRequest();
			NslcmGrantVnfResponse grantResponse = nslcmMgmr.grantVnf(grantRequest);
			handleNslcmGrantResponse(grantResponse);
			
			CBAMTerminateVnfRequest cbamRequest = requestConverter.terminateReqConvert(driverRequest);
			CBAMTerminateVnfResponse cbamResponse = cbamMgmr.terminateVnf(cbamRequest, vnfInstanceId);
			
			cbamMgmr.deleteVnf(vnfInstanceId);
			
			NslcmNotifyLCMEventsRequest nslcmNotifyReq = buildNslcmNotifyLCMEventsRequest(cbamResponse);
			nslcmMgmr.notifyVnf(nslcmNotifyReq, vnfInstanceId);
			
		} catch (ClientProtocolException e) {
			logger.error("TerminateVnfContinueRunnable run error ClientProtocolException", e);
		} catch (IOException e) {
			logger.error("TerminateVnfContinueRunnable run error IOException", e);
		}
		
	}
	
	private NslcmGrantVnfRequest buildNslcmGrantVnfRequest() {
		NslcmGrantVnfRequest request = new NslcmGrantVnfRequest();
		
		request.setVnfInstanceId(vnfInstanceId);
		request.setLifecycleOperation(LifecycleOperation.Instantiate);
		request.setJobId(jobId);
		
		ResourceDefinition resource = getFreeVnfResource();
		List<ResourceDefinition> resourceList = new ArrayList<ResourceDefinition>();
		resourceList.add(resource);
		request.setRemoveResource(resourceList);
		
		return request;
	}
	
	private ResourceDefinition getFreeVnfResource() {
		// TODO Auto-generated method stub
		return null;
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
