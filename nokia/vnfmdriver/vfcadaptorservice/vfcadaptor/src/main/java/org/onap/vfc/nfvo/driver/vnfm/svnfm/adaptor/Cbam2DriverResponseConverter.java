/*
* Copyright 2016-2017 Nokia Corporation
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

import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMCreateSubscriptionResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMCreateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMHealVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMQueryVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMScaleVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMTerminateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.OperationExecution;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.OperationExecution.OperationType;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.common.bo.AdaptorEnv;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonConstants;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonEnum;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.ScaleType;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.bean.VnfmJobExecutionInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.mapper.VnfmJobExecutionMapper;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.CreateSubscriptionResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.HealVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.InstantiateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.OperStatusVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.QueryVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.ScaleVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.TerminateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.entity.ResponseDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Cbam2DriverResponseConverter {
	private static final Logger logger = LoggerFactory.getLogger(Cbam2DriverResponseConverter.class);
	@Autowired
	private VnfmJobExecutionMapper jobDbManager;

	@Autowired
	private AdaptorEnv adaptorEnv;

	public InstantiateVnfResponse createRspConvert(CBAMCreateVnfResponse cbamResponse, Long jobId) {

		InstantiateVnfResponse response = new InstantiateVnfResponse();
		response.setJobId(jobId.longValue() + "");
		response.setVnfInstanceId(cbamResponse.getId());

		return response;
	}

	public TerminateVnfResponse terminateRspConvert(CBAMTerminateVnfResponse cbamResponse) {
		VnfmJobExecutionInfo jobInfo = new VnfmJobExecutionInfo();
		jobInfo.setVnfInstanceId(cbamResponse.getId());
		jobInfo.setVnfmInterfceName(CommonConstants.NSLCM_OPERATION_TERMINATE);
		jobInfo.setStatus(CommonConstants.CBAM_OPERATION_STATUS_START);

		jobDbManager.insert(jobInfo);
		VnfmJobExecutionInfo jobInfo1 = (VnfmJobExecutionInfo) jobDbManager.findNewestJobInfo();
		Long jobId = jobInfo1.getJobId();
		TerminateVnfResponse response = new TerminateVnfResponse();
		response.setJobId(jobId.longValue() + "");
		return response;
	}

	public QueryVnfResponse queryRspConvert(CBAMQueryVnfResponse cbamResponse) {
		QueryVnfResponse vnf = new QueryVnfResponse();
		vnf.setVnfdId(cbamResponse.getVnfdId());
		vnf.setVersion(cbamResponse.getVnfdVersion());
		vnf.setVnfInstanceId(cbamResponse.getId());
		vnf.setVnfInstanceName(cbamResponse.getName());
		vnf.setVnfInstanceDescription(cbamResponse.getDescription());
		vnf.setVnfPackageId(cbamResponse.getOnboardedVnfPkgInfoId());
		vnf.setVnfProvider(cbamResponse.getVnfProvider());
		vnf.setVnfStatus(cbamResponse.getInstantiationState());
		vnf.setVnfType(cbamResponse.getVnfSoftwareVersion());
		return vnf;
	}

	public OperStatusVnfResponse operRspConvert(OperationExecution oper, String jobId) {
		OperStatusVnfResponse response = new OperStatusVnfResponse();
		ResponseDescriptor er = new ResponseDescriptor();
		if (oper.getStatus() == CommonEnum.OperationStatus.STARTED) {
			er.setStatusDescription("Vim is processing");
			er.setStatus("started");
			int progress = calculateProgress(oper, jobId);

			er.setProgress("" + progress);
			er.setResponseId("" + +progress);
		} else if (oper.getStatus() == CommonEnum.OperationStatus.FINISHED) {
			er.setStatus("finished");
			er.setProgress("100");
			er.setResponseId("100");

		} else if (oper.getStatus() == CommonEnum.OperationStatus.OTHER) {
			er.setStatus("processing");
			er.setStatusDescription("Vim is processing");

			int progress = calculateProgress(oper, jobId);

			er.setProgress("" + progress);
			er.setResponseId("" + +progress);

		} else {
			er.setStatus("error");
			er.setStatus("finished");
			er.setProgress("100");
			er.setResponseId("100");
		}

		er.setErrorCode("null");

		response.setResponseDescriptor(er);
		return response;
	}

	public HealVnfResponse healRspConvert(CBAMHealVnfResponse cbamResponse) {
		HealVnfResponse response = new HealVnfResponse();
		response.setJobId("1");
		return response;
	}

	public ScaleVnfResponse scaleRspConvert(CBAMScaleVnfResponse cbamResponse,ScaleType type) {
		VnfmJobExecutionInfo jobInfo = new VnfmJobExecutionInfo();
		jobInfo.setVnfInstanceId(cbamResponse.getId());
		if (type.equals(ScaleType.SCALE_OUT)) {
			jobInfo.setVnfmInterfceName(CommonConstants.NSLCM_OPERATION_SCALE_OUT);
		} else {
			jobInfo.setVnfmInterfceName(CommonConstants.NSLCM_OPERATION_SCALE_IN);
		}
		jobInfo.setStatus(CommonConstants.CBAM_OPERATION_STATUS_START);

		jobDbManager.insert(jobInfo);
		VnfmJobExecutionInfo jobInfo1 = (VnfmJobExecutionInfo) jobDbManager.findNewestJobInfo();
		Long jobId = jobInfo1.getJobId();
		ScaleVnfResponse response = new ScaleVnfResponse();

		response.setJobId(jobId.longValue() + "");
		return response;
	}

	public int calculateProgress(OperationExecution oper, String jobId) {
		long nowTime = System.currentTimeMillis();
		VnfmJobExecutionInfo jobInfo = jobDbManager.findOne(Long.parseLong(jobId));
		int initialProgress = adaptorEnv.getInitialProgress();

		if (OperationType.INSTANTIATE == oper.getOperationType()) {
			double instantiateProgress = (nowTime - jobInfo.getOperateStartTime())
					/ adaptorEnv.getInstantiateTimeInterval();
			initialProgress = (int) (instantiateProgress + initialProgress);
		} else if (OperationType.TERMINATE == oper.getOperationType()) {
			double terminateProgress = (nowTime - jobInfo.getOperateStartTime())
					/ adaptorEnv.getTerminateTimeInterval();
			initialProgress = (int) (terminateProgress + initialProgress);
		} else {
			initialProgress = 0;
		}
		return initialProgress;

	}

	public void setAdaptorEnv(AdaptorEnv adaptorEnv) {
		this.adaptorEnv = adaptorEnv;
	}

	public CreateSubscriptionResponse queryRspConvert(CBAMCreateSubscriptionResponse cbamResponse) {
		CreateSubscriptionResponse response = new CreateSubscriptionResponse();
		
		response.set_links(cbamResponse.get_links());
		response.setId(cbamResponse.getId());
		response.setCallbackUri(cbamResponse.getCallbackUrl());
		response.setCallbackUrl(cbamResponse.getCallbackUrl());
		response.setFilter(cbamResponse.getFilter());
		
		return response;
	}

}
