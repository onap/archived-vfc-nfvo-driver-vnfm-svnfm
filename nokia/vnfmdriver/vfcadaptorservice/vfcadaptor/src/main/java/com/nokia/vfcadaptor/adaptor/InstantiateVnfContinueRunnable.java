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

import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;

import com.nokia.vfcadaptor.catalog.inf.CatalogMgmrInf;
import com.nokia.vfcadaptor.cbam.bo.CBAMInstantiateVnfRequest;
import com.nokia.vfcadaptor.cbam.bo.CBAMInstantiateVnfResponse;
import com.nokia.vfcadaptor.cbam.inf.CbamMgmrInf;
import com.nokia.vfcadaptor.constant.CommonConstants;
import com.nokia.vfcadaptor.constant.CommonEnum;
import com.nokia.vfcadaptor.constant.CommonEnum.LifecycleOperation;
import com.nokia.vfcadaptor.nslcm.bo.NslcmGrantVnfRequest;
import com.nokia.vfcadaptor.nslcm.bo.NslcmGrantVnfResponse;
import com.nokia.vfcadaptor.nslcm.bo.NslcmNotifyLCMEventsRequest;
import com.nokia.vfcadaptor.nslcm.bo.entity.ResourceDefinition;
import com.nokia.vfcadaptor.nslcm.bo.entity.VnfPackageInfo;
import com.nokia.vfcadaptor.nslcm.inf.NslcmMgmrInf;
import com.nokia.vfcadaptor.vnfmdriver.bo.InstantiateVnfRequest;

public class InstantiateVnfContinueRunnable implements Runnable {
	private Logger logger = Logger.getLogger(InstantiateVnfContinueRunnable.class);

	private CbamMgmrInf cbamMgmr;
	private CatalogMgmrInf catalogMgmr;
	private NslcmMgmrInf nslcmMgmr;
	
	private InstantiateVnfRequest driverRequest;
	private String vnfInstanceId;
	private String jobId;
	
	private Driver2CbamRequestConverter requestConverter;
	
	public InstantiateVnfContinueRunnable(InstantiateVnfRequest driverRequest, String vnfInstanceId, String jobId,
			NslcmMgmrInf nslcmMgmr, CatalogMgmrInf catalogMgmr, CbamMgmrInf cbamMgmr, Driver2CbamRequestConverter requestConverter)
	{
		this.driverRequest = driverRequest;
		this.vnfInstanceId = vnfInstanceId;
		this.jobId = jobId;
		this.nslcmMgmr = nslcmMgmr; 
		this.catalogMgmr = catalogMgmr;
		this.cbamMgmr = cbamMgmr;
		this.requestConverter = requestConverter;
	}
	
	public void run() {
		try {
			NslcmGrantVnfRequest grantRequest = buildNslcmGrantVnfRequest();
			NslcmGrantVnfResponse grantResponse = nslcmMgmr.grantVnf(grantRequest);
			handleNslcmGrantResponse(grantResponse);
			
			//step 2: query vnfPackage uri
			VnfPackageInfo vnfPackageInfo = catalogMgmr.queryVnfPackage(driverRequest.getVnfPackageId());
			
			//step 5: instantiate vnf
			CBAMInstantiateVnfRequest  instantiateReq = requestConverter.InstantiateCqonvert(driverRequest, grantResponse, null, null);
			CBAMInstantiateVnfResponse cbamInstantiateResponse = cbamMgmr.instantiateVnf(instantiateReq, vnfInstanceId);
			handleCbamInstantiateResponse(cbamInstantiateResponse);
			
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
		if(CommonEnum.OperationStatus.STARTED == cbamInstantiateResponse.getOperationExecution().getStatus())
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
		request.setVnfDescriptorId(driverRequest.getVnfDescriptorId());
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

	private void handleCbamInstantiateResponse(CBAMInstantiateVnfResponse cbamInstantiateResponse) {
		// TODO 
		//update job id record according to the executionId
	}

	private void handleNslcmGrantResponse(NslcmGrantVnfResponse grantResponse) {
		// TODO Auto-generated method stub
		
	}

}
