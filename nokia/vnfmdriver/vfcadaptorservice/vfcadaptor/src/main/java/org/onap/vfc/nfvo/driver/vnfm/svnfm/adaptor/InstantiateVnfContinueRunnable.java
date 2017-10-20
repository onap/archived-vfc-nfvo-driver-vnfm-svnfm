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
import org.onap.vfc.nfvo.driver.vnfm.svnfm.catalog.bo.entity.VnfPackageInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.catalog.inf.CatalogMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMInstantiateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMInstantiateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.inf.CbamMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonConstants;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonEnum;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonEnum.LifecycleOperation;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.bean.VnfmJobExecutionInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.repository.VnfmJobExecutionRepository;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.NslcmGrantVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.NslcmGrantVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.NslcmNotifyLCMEventsRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.ResourceDefinition;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.inf.NslcmMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.InstantiateVnfRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class InstantiateVnfContinueRunnable implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(InstantiateVnfContinueRunnable.class);
	private CbamMgmrInf cbamMgmr;
	private CatalogMgmrInf catalogMgmr;
	private NslcmMgmrInf nslcmMgmr;
	
	private InstantiateVnfRequest driverRequest;
	private String vnfInstanceId;
	private String jobId;
	
	private VnfmJobExecutionRepository jobDbMgmr;
	
	private Driver2CbamRequestConverter requestConverter;
	
	public InstantiateVnfContinueRunnable(InstantiateVnfRequest driverRequest, String vnfInstanceId, String jobId,
			NslcmMgmrInf nslcmMgmr, CatalogMgmrInf catalogMgmr, CbamMgmrInf cbamMgmr, Driver2CbamRequestConverter requestConverter, VnfmJobExecutionRepository dbManager)
	{
		this.driverRequest = driverRequest;
		this.vnfInstanceId = vnfInstanceId;
		this.jobId = jobId;
		this.nslcmMgmr = nslcmMgmr; 
		this.catalogMgmr = catalogMgmr;
		this.cbamMgmr = cbamMgmr;
		this.requestConverter = requestConverter;
		this.jobDbMgmr = dbManager;
	}
	
	public void run() {
		try {
			NslcmGrantVnfRequest grantRequest = buildNslcmGrantVnfRequest();
			NslcmGrantVnfResponse grantResponse = nslcmMgmr.grantVnf(grantRequest);
			handleNslcmGrantResponse(grantResponse);
			
			//step 2: query vnfPackage uri
			VnfPackageInfo vnfPackageInfo = catalogMgmr.queryVnfPackage(driverRequest.getVnfPackageId());
			
			//step 5: instantiate vnf
			CBAMInstantiateVnfRequest  instantiateReq = requestConverter.InstantiateReqConvert(driverRequest, grantResponse, null, null);
			CBAMInstantiateVnfResponse cbamInstantiateResponse = cbamMgmr.instantiateVnf(instantiateReq, vnfInstanceId);
			handleCbamInstantiateResponse(cbamInstantiateResponse, jobId);
			
			NslcmNotifyLCMEventsRequest nslcmNotifyReq = buildNslcmNotifyLCMEventsRequest(cbamInstantiateResponse);
			nslcmMgmr.notifyVnf(nslcmNotifyReq, vnfInstanceId);
			
		} catch (ClientProtocolException e) {
			logger.error("InstantiateVnfContinueRunnable run error ClientProtocolException", e);
		} catch (IOException e) {
			logger.error("InstantiateVnfContinueRunnable run error IOException", e);
		}
		
	}
	
	private NslcmNotifyLCMEventsRequest buildNslcmNotifyLCMEventsRequest(CBAMInstantiateVnfResponse cbamInstantiateResponse) {
		NslcmNotifyLCMEventsRequest request = new NslcmNotifyLCMEventsRequest();
		if(CommonEnum.OperationStatus.STARTED == cbamInstantiateResponse.getStatus())
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
		request.setOperation(CommonConstants.NSLCM_OPERATION_INSTANTIATE);
		request.setJobId(jobId);
		return request;
	}

	private NslcmGrantVnfRequest buildNslcmGrantVnfRequest() {
		NslcmGrantVnfRequest request = new NslcmGrantVnfRequest();
		
		request.setVnfInstanceId(vnfInstanceId);
		request.setLifecycleOperation(LifecycleOperation.Instantiate);
		request.setJobId(jobId);
		
		ResourceDefinition resource = getFreeVnfResource();
		List<ResourceDefinition> resourceList = new ArrayList<ResourceDefinition>();
		resourceList.add(resource);
		request.setAddResource(resourceList);
		
		return request;
	}

	private ResourceDefinition getFreeVnfResource() {
		// TODO Auto-generated method stub
		return null;
	}

	private void handleCbamInstantiateResponse(CBAMInstantiateVnfResponse cbamInstantiateResponse, String jobId) {
		VnfmJobExecutionInfo jobInfo = jobDbMgmr.findOne(Long.getLong(jobId));
		
		jobInfo.setVnfmExecutionId(cbamInstantiateResponse.getId());
		if(CommonEnum.OperationStatus.FAILED == cbamInstantiateResponse.getStatus()){
			jobInfo.setStatus(CommonConstants.CBAM_OPERATION_STATUS_ERROR);
		}
		jobDbMgmr.save(jobInfo);
	}

	private void handleNslcmGrantResponse(NslcmGrantVnfResponse grantResponse) {
		// TODO Auto-generated method stub
		
	}

}
