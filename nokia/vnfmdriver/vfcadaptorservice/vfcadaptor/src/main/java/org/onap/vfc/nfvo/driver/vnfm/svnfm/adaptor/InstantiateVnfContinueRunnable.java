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
import java.util.concurrent.Executors;

import org.onap.vfc.nfvo.driver.vnfm.svnfm.catalog.bo.entity.VnfPackageInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.catalog.inf.CatalogMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMInstantiateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMInstantiateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMModifyVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.inf.CbamMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.common.util.CommonUtil;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonConstants;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonEnum;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonEnum.LifecycleOperation;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.bean.VnfmJobExecutionInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.repository.VnfmJobExecutionRepository;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.http.client.HttpClientProcessorImpl;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.NslcmGrantVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.NslcmNotifyLCMEventsRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.AddResource;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.ResourceDefinition;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.inf.NslcmMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.InstantiateVnfRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;


public class InstantiateVnfContinueRunnable implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(InstantiateVnfContinueRunnable.class);
	private CbamMgmrInf cbamMgmr;
	private CatalogMgmrInf catalogMgmr;
	private NslcmMgmrInf nslcmMgmr;
	
	private InstantiateVnfRequest driverRequest;
	private String vnfInstanceId;
	private String jobId;
	private String vnfmId;
	
	private VnfmJobExecutionRepository jobDbMgmr;
	
	private Driver2CbamRequestConverter requestConverter;
	
	private Gson gson = new Gson();
	
	public InstantiateVnfContinueRunnable(String vnfmId, InstantiateVnfRequest driverRequest, String vnfInstanceId, String jobId,
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
		this.vnfmId = vnfmId;
	}
	
	public void run() {
		//step 1 handle vnf package
		handleVnfPackage();
		
		handleGrant();
		
		handleModify();
		try {
			//step 5: instantiate vnf
			CBAMInstantiateVnfResponse cbamInstantiateResponse = handleInstantiate();
			
			handleNotify(cbamInstantiateResponse);
		} catch (Exception e) {
			logger.error("InstantiateVnfContinueRunnable --> handleInstantiate or handleNotify error.", e);
		}
	}

	private void handleNotify(CBAMInstantiateVnfResponse cbamInstantiateResponse) {
		try {
			logger.info("Start to notify LCM the instantiation result");
			NslcmNotifyLCMEventsRequest nslcmNotifyReq = buildNslcmNotifyLCMEventsRequest(cbamInstantiateResponse);
			nslcmMgmr.notifyVnf(nslcmNotifyReq, vnfmId, vnfInstanceId);
			logger.info("End to notify LCM the instantiation result");
		} catch (Exception e) {
			logger.error("InstantiateVnfContinueRunnable --> handleNotify error.", e);
		}
	}

	private CBAMInstantiateVnfResponse handleInstantiate() throws Exception {
		CBAMInstantiateVnfRequest  instantiateReq = requestConverter.instantiateRequestConvert(driverRequest, null, null, null);
		CBAMInstantiateVnfResponse cbamInstantiateResponse = cbamMgmr.instantiateVnf(instantiateReq, vnfInstanceId);
		handleCbamInstantiateResponse(cbamInstantiateResponse, jobId);
		return cbamInstantiateResponse;
	}

	private void handleModify() {
		try {
			CBAMModifyVnfRequest  modifyReq = generateModifyVnfRequest();
			cbamMgmr.modifyVnf(modifyReq, vnfInstanceId);
		} catch (Exception e) {
			logger.error("InstantiateVnfContinueRunnable --> handleModify error.", e);
		}
	}

	private void handleGrant(){
		try {
			NslcmGrantVnfRequest grantRequest = buildNslcmGrantVnfRequest();
			nslcmMgmr.grantVnf(grantRequest);
		} catch (Exception e) {
			logger.error("InstantiateVnfContinueRunnable --> handleGrant error.", e);
		}
	}

	private CBAMModifyVnfRequest generateModifyVnfRequest() throws IOException{
		String filePath = "/etc/vnfpkginfo/cbam_extension.json";
		String fileContent = CommonUtil.getJsonStrFromFile(filePath);
		CBAMModifyVnfRequest req = gson.fromJson(fileContent, CBAMModifyVnfRequest.class);
		
		return req;
	}

	private void handleVnfPackage() {
		Executors.newSingleThreadExecutor().execute(new Runnable() {
			@Override
			public void run() {
				try {
					//step 1: query vnfPackage uri -- download package -- extract it -- upload CBAM package to CBAM
					VnfPackageInfo vnfPackageInfo = catalogMgmr.queryVnfPackage(driverRequest.getVnfPackageId());
					String packageUrl = vnfPackageInfo.getDownloadUri();
					String saveDir = "/service/vnfPackage";
					String packageFileName = packageUrl.substring(packageUrl.lastIndexOf("/"));
					Process process = Runtime.getRuntime().exec("mkdir -p " + saveDir);
					process.waitFor();
					
					if (HttpClientProcessorImpl.downLoadFromUrl(packageUrl, packageFileName, saveDir)) {
						logger.info("handleVnfPackage download file " + packageUrl + " is successful.");
//						File csarFile = new File(saveDir + "/" + packageFileName);
//						//extract package
//						ZipUtil.explode(csarFile);
//						csarFile.delete();
					}
				} catch (Exception e) {
					logger.error("Error to handleVnfPackage from SDC", e);
				}
				
			}
			
		});
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

	private void handleCbamInstantiateResponse(CBAMInstantiateVnfResponse cbamInstantiateResponse, String jobId) {
		VnfmJobExecutionInfo jobInfo = jobDbMgmr.findOne(Long.parseLong(jobId));
		
		jobInfo.setVnfmExecutionId(cbamInstantiateResponse.getId());
		if(CommonEnum.OperationStatus.FAILED == cbamInstantiateResponse.getStatus()){
			jobInfo.setStatus(CommonConstants.CBAM_OPERATION_STATUS_ERROR);
		}
		jobDbMgmr.save(jobInfo);
	}

}
