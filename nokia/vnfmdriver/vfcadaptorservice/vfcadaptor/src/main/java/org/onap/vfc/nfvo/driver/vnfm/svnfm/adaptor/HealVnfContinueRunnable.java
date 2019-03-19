/**
 * Copyright 2016-2017, Nokia Corporation.
 * Modifications Copyright (C) 2019 Samsung Electronics Co., Ltd.
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

import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMHealVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMHealVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.inf.CbamMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonConstants;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonEnum;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonEnum.LifecycleOperation;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.bean.VnfmJobExecutionInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.mapper.VnfmJobExecutionMapper;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.NslcmGrantVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.AddResource;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.ResourceDefinition;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.inf.NslcmMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.HealVnfRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class HealVnfContinueRunnable implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(HealVnfContinueRunnable.class);

	@Autowired
	private CbamMgmrInf cbamMgmr;
	@Autowired
	private NslcmMgmrInf nslcmMgmr;
	
	private HealVnfRequest driverRequest;
	private String vnfInstanceId;
	private String jobId;
	private String vnfmId;
	@Autowired
	private VnfmJobExecutionMapper jobDbMgmr;
	
	private Driver2CbamRequestConverter requestConverter;

	///

	public static class HealVnfContinueRunnableBuilder {
		private String inVnfmId;
		private HealVnfRequest driverRequest;
		private String vnfInstanceId;
		private String jobId;
		private NslcmMgmrInf nslcmMgmr;
		private CbamMgmrInf cbamMgmr;
		private Driver2CbamRequestConverter requestConverter;
		private VnfmJobExecutionMapper dbManager;

		public HealVnfContinueRunnableBuilder setInVnfmId(String inVnfmId) {
			this.inVnfmId = inVnfmId;
			return this;
		}

		public HealVnfContinueRunnableBuilder setDriverRequest(HealVnfRequest driverRequest) {
			this.driverRequest = driverRequest;
			return this;
		}

		public HealVnfContinueRunnableBuilder setVnfInstanceId(String vnfInstanceId) {
			this.vnfInstanceId = vnfInstanceId;
			return this;
		}

		public HealVnfContinueRunnableBuilder setJobId(String jobId) {
			this.jobId = jobId;
			return this;
		}

		public HealVnfContinueRunnableBuilder setNslcmMgmr(NslcmMgmrInf nslcmMgmr) {
			this.nslcmMgmr = nslcmMgmr;
			return this;
		}

		public HealVnfContinueRunnableBuilder setCbamMgmr(CbamMgmrInf cbamMgmr) {
			this.cbamMgmr = cbamMgmr;
			return this;
		}

		public HealVnfContinueRunnableBuilder setRequestConverter(Driver2CbamRequestConverter requestConverter) {
			this.requestConverter = requestConverter;
			return this;
		}

		public HealVnfContinueRunnableBuilder setDbManager(VnfmJobExecutionMapper dbManager) {
			this.dbManager = dbManager;
			return this;
		}

		public HealVnfContinueRunnable build() {
			return new HealVnfContinueRunnable(this);
		}
	}

	///
	
	
	public HealVnfContinueRunnable(HealVnfContinueRunnableBuilder builder) {
	    this.driverRequest = builder.driverRequest;
        this.vnfInstanceId = builder.vnfInstanceId;
        this.nslcmMgmr = builder.nslcmMgmr; 
        this.cbamMgmr = builder.cbamMgmr;
        this.requestConverter = builder.requestConverter;
        this.jobId = builder.jobId;
        this.jobDbMgmr = builder.dbManager;
        this.vnfmId = builder.inVnfmId;
	}

    private void handleGrant(){
		try {
			NslcmGrantVnfRequest grantRequest = buildNslcmGrantVnfRequest();
			nslcmMgmr.grantVnf(grantRequest);
		} catch (Exception e) {
			logger.error("HealVnfContinueRunnable --> handleGrant error.", e);
		}
	}
	
	public void run() {
		handleGrant();
		handleHeal();
	}
	
	

	private CBAMHealVnfResponse handleHeal() {
		CBAMHealVnfResponse cbamResponse = null;
		try {
			CBAMHealVnfRequest  modifyReq = requestConverter.healReqConvert();
			cbamResponse = cbamMgmr.healVnf(modifyReq, vnfInstanceId);
			handleCbamHealResponse(cbamResponse, jobId);
		} catch (Exception e) {
			logger.error("HealVnfContinueRunnable --> handleHeal error.", e);
		}
		
		return cbamResponse;
	}

	private void handleCbamHealResponse(CBAMHealVnfResponse cbamResponse, String jobId) {
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
		request.setLifecycleOperation(LifecycleOperation.Heal);
		request.setJobId(jobId);
		
		ResourceDefinition resource = getFreeVnfResource();
		List<ResourceDefinition> resourceList = new ArrayList<>();
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

	public void setDriverRequest(HealVnfRequest driverRequest) {
		this.driverRequest = driverRequest;
	}

	public void setVnfInstanceId(String vnfInstanceId) {
		this.vnfInstanceId = vnfInstanceId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public void setRequestConverter(Driver2CbamRequestConverter requestConverter) {
		this.requestConverter = requestConverter;
	}

	public void setVnfmId(String inVnfmId) {
		this.vnfmId = inVnfmId;
	}
}
