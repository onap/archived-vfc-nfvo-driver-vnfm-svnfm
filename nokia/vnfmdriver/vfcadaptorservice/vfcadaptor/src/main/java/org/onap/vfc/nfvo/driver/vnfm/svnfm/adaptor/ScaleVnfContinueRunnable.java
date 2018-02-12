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

import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMScaleVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMScaleVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.inf.CbamMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonConstants;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonEnum;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.ScaleType;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonEnum.LifecycleOperation;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.bean.VnfmJobExecutionInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.mapper.VnfmJobExecutionMapper;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.NslcmGrantVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.NslcmGrantVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.AddResource;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.ResourceDefinition;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.inf.NslcmMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.ScaleVnfRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ScaleVnfContinueRunnable implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(ScaleVnfContinueRunnableTest.class);

	@Autowired
	private CbamMgmrInf cbamMgmr;
	@Autowired
	private NslcmMgmrInf nslcmMgmr;
	
	private ScaleVnfRequest driverRequest;
	private String vnfInstanceId;
	private String jobId;
	private String vnfmId;
	private ScaleType type;
	@Autowired
	private VnfmJobExecutionMapper jobDbMgmr;
	
	private Driver2CbamRequestConverter requestConverter;
	
	public ScaleVnfContinueRunnable(String vnfmId, ScaleVnfRequest driverRequest, String vnfInstanceId, String jobId,
			NslcmMgmrInf nslcmMgmr, CbamMgmrInf cbamMgmr, Driver2CbamRequestConverter requestConverter, VnfmJobExecutionMapper dbManager)
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
			logger.error("ScaleVnfContinueRunnable --> handleGrant error.", e);
		}
	}
	
	public void run() {
		handleGrant();
		handleScale();
	}
	
	

	private CBAMScaleVnfResponse handleScale() {
		CBAMScaleVnfResponse cbamResponse = null;
		try {
			CBAMScaleVnfRequest scaleReq = requestConverter.scaleReqconvert(driverRequest);
			cbamResponse = cbamMgmr.scaleVnf(scaleReq, vnfInstanceId);
			handleCbamScaleResponse(cbamResponse, jobId);
		} catch (Exception e) {
			logger.error("ScaleVnfContinueRunnable --> handleScale error.", e);
		}
		
		return cbamResponse;
	}

	private void handleCbamScaleResponse(CBAMScaleVnfResponse cbamResponse, String jobId) {
		VnfmJobExecutionInfo jobInfo = jobDbMgmr.findOne(Long.parseLong(jobId));
		
		jobInfo.setVnfmExecutionId(cbamResponse.getId());
		if(CommonEnum.OperationStatus.FAILED == cbamResponse.getStatus()) {
			jobInfo.setStatus(CommonConstants.CBAM_OPERATION_STATUS_ERROR);
		}
		else {
			jobInfo.setStatus(CommonConstants.CBAM_OPERATION_STATUS_PROCESSING);
		}
		jobDbMgmr.update(jobInfo);
	}
	
	private NslcmGrantVnfRequest buildNslcmGrantVnfRequest() {
		NslcmGrantVnfRequest request = new NslcmGrantVnfRequest();
		
		request.setVnfInstanceId(vnfInstanceId);
		if(type.equals(ScaleType.SCALE_OUT)) {
			request.setLifecycleOperation(LifecycleOperation.Scaleout);
		}else {
			request.setLifecycleOperation(LifecycleOperation.Scalein);
		}
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

	public void setDriverRequest(ScaleVnfRequest driverRequest) {
		this.driverRequest = driverRequest;
	}

	public void setVnfInstanceId(String vnfInstanceId) {
		this.vnfInstanceId = vnfInstanceId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public void setVnfmId(String vnfmId) {
		this.vnfmId = vnfmId;
	}

	public void setType(ScaleType type) {
		this.type = type;
	}

	public void setRequestConverter(Driver2CbamRequestConverter requestConverter) {
		this.requestConverter = requestConverter;
	}

}
