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
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.mapper.VnfcResourceInfoMapper;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.mapper.VnfmJobExecutionMapper;
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
import org.springframework.beans.factory.annotation.Autowired;

public class TerminateVnfContinueRunnable implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(TerminateVnfContinueRunnable.class);

	@Autowired
	private CbamMgmrInf cbamMgmr;
	@Autowired
	private NslcmMgmrInf nslcmMgmr;
	
	private TerminateVnfRequest driverRequest;
	private String vnfInstanceId;
	private String jobId;
	private String vnfmId;
	@Autowired
	private VnfmJobExecutionMapper jobDbMgmr;
	@Autowired
	private VnfcResourceInfoMapper vnfcDbMgmr;
	
	private Driver2CbamRequestConverter requestConverter;


	////////////////////////// Builder class

	public static class TerminateVnfContinueRunnableBuilder {
		private String vnfmId;
		private TerminateVnfRequest driverRequest;
		private String vnfInstanceId;
		private String jobId;
		private NslcmMgmrInf nslcmMgmr;
		private CbamMgmrInf cbamMgmr;
		private Driver2CbamRequestConverter requestConverter;
		private VnfmJobExecutionMapper dbManager;
		private VnfcResourceInfoMapper vnfcDbMgmr;

		public TerminateVnfContinueRunnableBuilder setVnfmId(String vnfmId) {
			this.vnfmId = vnfmId;
			return this;
		}

		public TerminateVnfContinueRunnableBuilder setDriverRequest(TerminateVnfRequest driverRequest) {
			this.driverRequest = driverRequest;
			return this;
		}

		public TerminateVnfContinueRunnableBuilder setVnfInstanceId(String vnfInstanceId) {
			this.vnfInstanceId = vnfInstanceId;
			return this;
		}

		public TerminateVnfContinueRunnableBuilder setJobId(String jobId) {
			this.jobId = jobId;
			return this;
		}

		public TerminateVnfContinueRunnableBuilder setNslcmMgmr(NslcmMgmrInf nslcmMgmr) {
			this.nslcmMgmr = nslcmMgmr;
			return this;
		}

		public TerminateVnfContinueRunnableBuilder setCbamMgmr(CbamMgmrInf cbamMgmr) {
			this.cbamMgmr = cbamMgmr;
			return this;
		}

		public TerminateVnfContinueRunnableBuilder setRequestConverter(Driver2CbamRequestConverter requestConverter) {
			this.requestConverter = requestConverter;
			return this;
		}

		public TerminateVnfContinueRunnableBuilder setDbManager(VnfmJobExecutionMapper dbManager) {
			this.dbManager = dbManager;
			return this;
		}

		public TerminateVnfContinueRunnableBuilder setVnfcDbMgmr(VnfcResourceInfoMapper vnfcDbMgmr) {
			this.vnfcDbMgmr = vnfcDbMgmr;
			return this;
		}

		public TerminateVnfContinueRunnable build(){
			return new TerminateVnfContinueRunnable(this);
		}


	}


	public TerminateVnfContinueRunnable(TerminateVnfContinueRunnableBuilder builder)
	{
		this.driverRequest = builder.driverRequest;
		this.vnfInstanceId =  builder.vnfInstanceId;
		this.nslcmMgmr =  builder.nslcmMgmr;
		this.cbamMgmr =  builder.cbamMgmr;
		this.requestConverter =  builder.requestConverter;
		this.jobId =  builder.jobId;
		this.jobDbMgmr =  builder.dbManager;
		this.vnfmId =  builder.vnfmId;
		this.vnfcDbMgmr =  builder.vnfcDbMgmr;
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
            while (!vnfAllowDelete) {
                CBAMQueryVnfResponse queryResponse = cbamMgmr.queryVnf(vnfInstanceId);
                if (CommonEnum.InstantiationState.NOT_INSTANTIATED == queryResponse.getInstantiationState()) {
                    vnfAllowDelete = true;
                    break;
                }
                i++;
                logger.info(i + ": The vnf's current status is " + queryResponse.getInstantiationState().name()
                        + " is not ready for deleting, please wait ... ");
                Thread.sleep(30000);
            }
            prepareDelete(jobId);
            cbamMgmr.deleteVnf(vnfInstanceId);
        } catch (Exception e) {
            logger.error("TerminateVnfContinueRunnable --> handleDelete error.", e);
        }
    }
	
	private void prepareDelete(String jobId) {
		long nowTime = System.currentTimeMillis();
		VnfmJobExecutionInfo jobInfo = jobDbMgmr.findOne(Long.parseLong(jobId));
		jobInfo.setStatus(CommonConstants.CBAM_OPERATION_STATUS_FINISH);
		jobInfo.setOperateEndTime(nowTime);
		jobDbMgmr.update(jobInfo);
		
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
		jobDbMgmr.update(jobInfo);
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
		List<AffectedVnfc> vnfcsFromDb = vnfcDbMgmr.getAllByInstanceId(vnfInstanceId);
		List<AffectedVnfc> vnfcs = modifyResourceTypeAsRemove(vnfcsFromDb);
		
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
		
		if(vnfcs == null) {
			vnfcs = new ArrayList<>();
		}
		
		return vnfcs;
	}

	public void setDriverRequest(TerminateVnfRequest driverRequest) {
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

	public void setRequestConverter(Driver2CbamRequestConverter requestConverter) {
		this.requestConverter = requestConverter;
	}
}
