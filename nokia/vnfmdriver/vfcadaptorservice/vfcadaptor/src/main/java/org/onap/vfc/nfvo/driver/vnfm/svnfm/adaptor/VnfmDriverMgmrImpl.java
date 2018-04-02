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

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.aai.bo.AaiVnfmInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.aai.bo.entity.EsrSystemInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.aai.inf.AaiMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.catalog.inf.CatalogMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMCreateSubscriptionRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMCreateSubscriptionResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMCreateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMCreateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMQueryVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.OperationExecution;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.OperationExecution.OperationType;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.inf.CbamMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.common.bo.AdaptorEnv;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonConstants;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonEnum;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.ScaleType;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.bean.VnfmJobExecutionInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.bean.VnfmSubscriptionInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.mapper.VnfcResourceInfoMapper;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.mapper.VnfmJobExecutionMapper;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.mapper.VnfmSubscriptionsMapper;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.exception.VnfmDriverException;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.inf.NslcmMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.CreateSubscriptionRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.CreateSubscriptionResponse;
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
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.inf.VnfContinueProcessorInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.inf.VnfmDriverMgmrInf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

@Component
public class VnfmDriverMgmrImpl implements VnfmDriverMgmrInf {
	private static final Logger logger = LoggerFactory.getLogger(VnfmDriverMgmrImpl.class);

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
	private AaiMgmrInf aaiMgmr;

	@Autowired
	private VnfmJobExecutionMapper jobDbManager;

	@Autowired
	private VnfcResourceInfoMapper vnfcDbMgmr;
	
	@Autowired
	private VnfmSubscriptionsMapper subscriptionsMapper;

	@Autowired
	AdaptorEnv adaptorEnv;

	@Autowired
	private VnfContinueProcessorInf vnfContinueProcessorInf;

	private Gson gson = new Gson();

	public InstantiateVnfResponse instantiateVnf(InstantiateVnfRequest driverRequest, String vnfmId)
			throws VnfmDriverException {
		InstantiateVnfResponse driverResponse;
		try {
			driverRequest.setVnfdId(adaptorEnv.getVnfdId());
			buildVnfmHttpPathById(vnfmId);

			// String dirPath = "/etc/vnfmpkg";
			// String cbamDirName = CommonUtil.getAppRoot() + dirPath;
			// File cbamDirFile = new File(cbamDirName);
			// String cbamPackageName = cbamDirFile.listFiles()[0].getAbsolutePath();
			// cbamMgmr.uploadVnfPackage(cbamPackageName);

			// step 3: create vnf
			CBAMCreateVnfRequest cbamRequest = requestConverter.createReqConvert(driverRequest);
			logger.info("VnfmDriverMgmrImpl --> instantiateVnf, ready to create vnf on CBAM. ");
			CBAMCreateVnfResponse cbamResponse = cbamMgmr.createVnf(cbamRequest);
			String vnfInstanceId = cbamResponse.getId();
			logger.info("VnfmDriverMgmrImpl --> instantiateVnf, vnfInstanceId is " + vnfInstanceId);
			Long jobId = saveCreateVnfJob(vnfInstanceId);
			driverResponse = responseConverter.createRspConvert(cbamResponse, jobId);

			vnfContinueProcessorInf.continueInstantiateVnf(vnfmId, driverRequest, vnfInstanceId, jobId.toString(),
					nslcmMgmr, catalogMgmr, cbamMgmr, requestConverter, jobDbManager, vnfcDbMgmr);

		} catch (Exception e) {
			logger.error("error VnfmDriverMgmrImpl --> instantiateVnf. ", e);
			throw new VnfmDriverException(HttpStatus.SC_INTERNAL_SERVER_ERROR, CommonConstants.HTTP_ERROR_DESC_500);
		}

		return driverResponse;
	}

	private Long saveCreateVnfJob(String vnfInstanceId) {
		VnfmJobExecutionInfo jobInfo = new VnfmJobExecutionInfo();
		long nowTime = System.currentTimeMillis();
		jobInfo.setVnfInstanceId(vnfInstanceId);
		jobInfo.setVnfmInterfceName(CommonConstants.NSLCM_OPERATION_INSTANTIATE);
		jobInfo.setStatus(CommonConstants.CBAM_OPERATION_STATUS_START);
		jobInfo.setOperateStartTime(nowTime);

		jobDbManager.insert(jobInfo);
		VnfmJobExecutionInfo jobInfo1 = (VnfmJobExecutionInfo) jobDbManager.findNewestJobInfo();
		Long jobId = jobInfo1.getJobId();
		return jobId;
	}

	public TerminateVnfResponse terminateVnf(TerminateVnfRequest driverRequest, String vnfmId, String vnfInstanceId) {
		TerminateVnfResponse driverResponse;
		try {
			buildVnfmHttpPathById(vnfmId);
			driverResponse = generateTerminateVnfResponse(vnfInstanceId);
			String jobId = driverResponse.getJobId();

			vnfContinueProcessorInf.continueTerminateVnf(vnfmId, driverRequest, vnfInstanceId, jobId, nslcmMgmr,
					cbamMgmr, requestConverter, jobDbManager, vnfcDbMgmr);

		} catch (Exception e) {
			logger.error("error VnfmDriverMgmrImpl --> terminateVnf. ", e);
			throw new VnfmDriverException(HttpStatus.SC_INTERNAL_SERVER_ERROR, CommonConstants.HTTP_ERROR_DESC_500);
		}

		return driverResponse;
	}

	private TerminateVnfResponse generateTerminateVnfResponse(String vnfInstanceId) {
		VnfmJobExecutionInfo jobInfo = new VnfmJobExecutionInfo();
		long nowTime = System.currentTimeMillis();
		jobInfo.setVnfInstanceId(vnfInstanceId);
		jobInfo.setVnfmInterfceName(CommonConstants.NSLCM_OPERATION_TERMINATE);
		jobInfo.setStatus(CommonConstants.CBAM_OPERATION_STATUS_START);
		jobInfo.setOperateStartTime(nowTime);
		jobDbManager.insert(jobInfo);
		VnfmJobExecutionInfo jobInfo1 = jobDbManager.findNewestJobInfo();
		Long jobId = jobInfo1.getJobId();

		TerminateVnfResponse response = new TerminateVnfResponse();
		response.setJobId("" + jobId);
		return response;
	}

	public QueryVnfResponse queryVnf(String vnfmId, String vnfInstanceId) {
		QueryVnfResponse driverResponse;
		try {
			buildVnfmHttpPathById(vnfmId);
			CBAMQueryVnfResponse cbamResponse = cbamMgmr.queryVnf(vnfInstanceId);
			driverResponse = responseConverter.queryRspConvert(cbamResponse);
		} catch (Exception e) {
			logger.error("error VnfmDriverMgmrImpl --> queryVnf. ", e);
			throw new VnfmDriverException(HttpStatus.SC_INTERNAL_SERVER_ERROR, CommonConstants.HTTP_ERROR_DESC_500);
		}

		return driverResponse;
	}

	public OperStatusVnfResponse getOperStatus(String vnfmId, String jobId) throws VnfmDriverException {

		OperationExecution cbamResponse = null;

		try {
			buildVnfmHttpPathById(vnfmId);

			VnfmJobExecutionInfo jobInfo = jobDbManager.findOne(Long.parseLong(jobId));
			cbamResponse = new OperationExecution();

			if ("Instantiate".equalsIgnoreCase(jobInfo.getVnfmInterfceName())) {
				cbamResponse.setOperationType(OperationType.INSTANTIATE);
			} else if ("Terminal".equalsIgnoreCase(jobInfo.getVnfmInterfceName())) {
				cbamResponse.setOperationType(OperationType.TERMINATE);
			} else if ("Scalein".equalsIgnoreCase(jobInfo.getVnfmInterfceName())
					|| "Scaleout".equalsIgnoreCase(jobInfo.getVnfmInterfceName())) {
				cbamResponse.setOperationType(OperationType.SCALE);
			} else {
				cbamResponse.setOperationType(OperationType.HEAL);
			}

			if (jobInfo.getStatus().equalsIgnoreCase(CommonConstants.CBAM_OPERATION_STATUS_FINISH)) {
				cbamResponse.setStatus(CommonEnum.OperationStatus.FINISHED);
			} else if (jobInfo.getStatus().equalsIgnoreCase(CommonConstants.CBAM_OPERATION_STATUS_ERROR)) {
				cbamResponse.setStatus(CommonEnum.OperationStatus.FINISHED);
			} else {
				cbamResponse.setStatus(CommonEnum.OperationStatus.OTHER);
				// String execId = jobInfo.getVnfmExecutionId();
				// logger.info(" VnfmDriverMgmrImpl --> getOperStatus execId is " + execId);
				// cbamResponse = cbamMgmr.queryOperExecution(execId);
			}

		} catch (Exception e) {
			logger.error("error VnfmDriverMgmrImpl --> getOperStatus. ", e);
			throw new VnfmDriverException(HttpStatus.SC_INTERNAL_SERVER_ERROR, CommonConstants.HTTP_ERROR_DESC_500);
		}

		OperStatusVnfResponse response = responseConverter.operRspConvert(cbamResponse, jobId);
		response.setJobId(jobId);

		return response;
	}

	public ScaleVnfResponse scaleVnf(ScaleVnfRequest driverRequest, String vnfmId, String vnfInstanceId)
			throws VnfmDriverException {
		ScaleVnfResponse driverResponse;
		try {
			buildVnfmHttpPathById(vnfmId);
			driverResponse = generateScaleVnfResponse(vnfInstanceId, driverRequest.getType());
			String jobId = driverResponse.getJobId();

			vnfContinueProcessorInf.continueScaleVnf(vnfmId, driverRequest, vnfInstanceId, jobId, nslcmMgmr, cbamMgmr,
					requestConverter, jobDbManager);
		} catch (Exception e) {
			logger.error("error VnfmDriverMgmrImpl --> scaleVnf. ", e);
			throw new VnfmDriverException(HttpStatus.SC_INTERNAL_SERVER_ERROR, CommonConstants.HTTP_ERROR_DESC_500);
		}

		return driverResponse;
	}

	private ScaleVnfResponse generateScaleVnfResponse(String vnfInstanceId, ScaleType type) {
		VnfmJobExecutionInfo jobInfo = new VnfmJobExecutionInfo();
		long nowTime = System.currentTimeMillis();
		jobInfo.setVnfInstanceId(vnfInstanceId);
		if (type.equals(ScaleType.SCALE_OUT)) {
			jobInfo.setVnfmInterfceName(CommonConstants.NSLCM_OPERATION_SCALE_OUT);
		} else {
			jobInfo.setVnfmInterfceName(CommonConstants.NSLCM_OPERATION_SCALE_IN);
		}
		jobInfo.setStatus(CommonConstants.CBAM_OPERATION_STATUS_START);
		jobInfo.setOperateStartTime(nowTime);
		jobDbManager.insert(jobInfo);
		VnfmJobExecutionInfo jobInfo1 = jobDbManager.findNewestJobInfo();
		Long jobId = jobInfo1.getJobId();

		ScaleVnfResponse response = new ScaleVnfResponse();
		response.setJobId("" + jobId);
		return response;

	}

	public HealVnfResponse healVnf(HealVnfRequest driverRequest, String vnfmId, String vnfInstanceId)
			throws VnfmDriverException {
		HealVnfResponse driverResponse;
		try {
			buildVnfmHttpPathById(vnfmId);
			driverResponse = generateHealVnfResponse(vnfInstanceId);
			String jobId = driverResponse.getJobId();

			vnfContinueProcessorInf.continueHealVnf(vnfmId, driverRequest, vnfInstanceId, jobId, nslcmMgmr, cbamMgmr,
					requestConverter, jobDbManager);
		} catch (Exception e) {
			logger.error("error VnfmDriverMgmrImpl --> healVnf. ", e);
			throw new VnfmDriverException(HttpStatus.SC_INTERNAL_SERVER_ERROR, CommonConstants.HTTP_ERROR_DESC_500);
		}

		return driverResponse;
	}

	private HealVnfResponse generateHealVnfResponse(String vnfInstanceId) {
		VnfmJobExecutionInfo jobInfo = new VnfmJobExecutionInfo();
		long nowTime = System.currentTimeMillis();
		jobInfo.setVnfInstanceId(vnfInstanceId);
		jobInfo.setVnfmInterfceName(CommonConstants.NSLCM_OPERATION_HEAL);
		jobInfo.setStatus(CommonConstants.CBAM_OPERATION_STATUS_START);
		jobInfo.setOperateStartTime(nowTime);
		jobDbManager.insert(jobInfo);
		VnfmJobExecutionInfo jobInfo1 = jobDbManager.findNewestJobInfo();
		Long jobId = jobInfo1.getJobId();

		HealVnfResponse response = new HealVnfResponse();
		response.setJobId("" + jobId);
		return response;
	}

	public String buildVnfmHttpPathById(String vnfmId) {
		String result = "";
		try {
			result = buildVnfmHttpPathByRealId(vnfmId);
		} catch (Exception e) {
			logger.error("buildVnfmHttpPathById Error.", e);
		}
		return result;
	}

	public String buildVnfmHttpPathByRealId(String vnfmId)
			throws ClientProtocolException, IOException, VnfmDriverException {
		AaiVnfmInfo vnfmInfo = aaiMgmr.queryVnfm(vnfmId);
		logger.info("vnfmInfo in AAI is " + gson.toJson(vnfmInfo));
		if (isVnfmInfoValid(vnfmId, vnfmInfo)) {
			throw new VnfmDriverException(HttpStatus.SC_INTERNAL_SERVER_ERROR, CommonConstants.HTTP_ERROR_DESC_500);
		}

		EsrSystemInfo systemInfo = vnfmInfo.getEsrSystemInfoList().get(0);

		String urlHead = systemInfo.getServiceUrl();
		// adaptorEnv.setCbamApiUriFront(urlHead);
		// adaptorEnv.setCbamUserName(systemInfo.getUserName());
		// adaptorEnv.setCbamPassword(systemInfo.getPassword());

		return urlHead;
	}

	private boolean isVnfmInfoValid(String vnfmId, AaiVnfmInfo vnfmInfo) {
		return vnfmInfo == null || vnfmInfo.getEsrSystemInfoList() == null || vnfmInfo.getEsrSystemInfoList().isEmpty();
	}

	public void setRequestConverter(Driver2CbamRequestConverter requestConverter) {
		this.requestConverter = requestConverter;
	}

	public void setResponseConverter(Cbam2DriverResponseConverter responseConverter) {
		this.responseConverter = responseConverter;
	}

	@Override
	public CreateSubscriptionResponse createSubscription(CreateSubscriptionRequest request) throws VnfmDriverException {
		CreateSubscriptionResponse driverResponse;
		try {
			CBAMCreateSubscriptionRequest cbamRequest = new CBAMCreateSubscriptionRequest();
			cbamRequest.setCallbackUrl(request.getCallbackUri());
			cbamRequest.setAuthentication(request.getAuthentication());
			cbamRequest.setFilter(request.getFilter());
			CBAMCreateSubscriptionResponse cbamResponse = cbamMgmr.createSubscription(cbamRequest);
			driverResponse = responseConverter.queryRspConvert(cbamResponse);
			VnfmSubscriptionInfo subscriptionInfo = new VnfmSubscriptionInfo();
			subscriptionInfo.setId(cbamResponse.getId());
			subscriptionInfo.setDriverCallbackUrl(cbamResponse.getId());
			subscriptionInfo.setNslcmCallbackUrl(request.getCallbackUri());
			subscriptionsMapper.insert(subscriptionInfo);
		} catch (Exception e) {
			logger.error("error VnfmDriverMgmrImpl --> createSubscripiton. ", e);
			throw new VnfmDriverException(HttpStatus.SC_INTERNAL_SERVER_ERROR, CommonConstants.HTTP_ERROR_DESC_500);
		}

		return driverResponse;
	}

}
