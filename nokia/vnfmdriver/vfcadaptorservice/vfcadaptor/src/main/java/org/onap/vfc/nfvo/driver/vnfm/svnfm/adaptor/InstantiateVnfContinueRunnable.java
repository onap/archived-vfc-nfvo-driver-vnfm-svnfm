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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import org.onap.vfc.nfvo.driver.vnfm.svnfm.catalog.bo.entity.VnfPackageInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.catalog.inf.CatalogMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMInstantiateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMInstantiateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMModifyVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMQueryOperExecutionResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.OperationExecution;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.VnfcResourceInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.inf.CbamMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.common.util.CommonUtil;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonConstants;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonEnum;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonEnum.LifecycleOperation;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.bean.VnfmJobExecutionInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.mapper.VnfcResourceInfoMapper;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.mapper.VnfmJobExecutionMapper;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.http.client.HttpClientProcessorImpl;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.NslcmGrantVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.NslcmNotifyLCMEventsRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.AddResource;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.AffectedVnfc;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.ResourceDefinition;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.inf.NslcmMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.InstantiateVnfRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;


public class InstantiateVnfContinueRunnable implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(InstantiateVnfContinueRunnable.class);
	@Autowired
	private CbamMgmrInf cbamMgmr;
	@Autowired
	private CatalogMgmrInf catalogMgmr;
	@Autowired
	private NslcmMgmrInf nslcmMgmr;
	
	private InstantiateVnfRequest driverRequest;
	private String vnfInstanceId;
	private String jobId;
	private String vnfmId;
	
//	private VnfmJobExecutionRepository jobDbMgmr;
	@Autowired
	private VnfmJobExecutionMapper jobDbMgmr;
	@Autowired
	private VnfcResourceInfoMapper vnfcDbMgmr;
	
	private Driver2CbamRequestConverter requestConverter;
	
	private Gson gson = new Gson();
	
	// Builder class
	
	public static class InstantiateVnfContinueRunnableBuilder {
	    private String vnfmId;
	    private InstantiateVnfRequest driverRequest;
	    private String vnfInstanceId;
	    private String jobId;
	    private NslcmMgmrInf nslcmMgmr;
	    private CatalogMgmrInf catalogMgmr;
	    private CbamMgmrInf cbamMgmr;
	    private Driver2CbamRequestConverter requestConverter;
	    private VnfmJobExecutionMapper dbManager;
	    private VnfcResourceInfoMapper vnfcDbMgmr;

	    public InstantiateVnfContinueRunnableBuilder setVnfmId(String vnfmId) {
	        this.vnfmId = vnfmId;
	        return this;
	    }

	    public InstantiateVnfContinueRunnableBuilder setDriverRequest(InstantiateVnfRequest driverRequest) {
	        this.driverRequest = driverRequest;
	        return this;
	    }

	    public InstantiateVnfContinueRunnableBuilder setVnfInstanceId(String vnfInstanceId) {
	        this.vnfInstanceId = vnfInstanceId;
	        return this;
	    }

	    public InstantiateVnfContinueRunnableBuilder setJobId(String jobId) {
	        this.jobId = jobId;
	        return this;
	    }

	    public InstantiateVnfContinueRunnableBuilder setNslcmMgmr(NslcmMgmrInf nslcmMgmr) {
	        this.nslcmMgmr = nslcmMgmr;
	        return this;
	    }

	    public InstantiateVnfContinueRunnableBuilder setCatalogMgmr(CatalogMgmrInf catalogMgmr) {
	        this.catalogMgmr = catalogMgmr;
	        return this;
	    }

	    public InstantiateVnfContinueRunnableBuilder setCbamMgmr(CbamMgmrInf cbamMgmr) {
	        this.cbamMgmr = cbamMgmr;
	        return this;
	    }

	    public InstantiateVnfContinueRunnableBuilder setRequestConverter(Driver2CbamRequestConverter requestConverter) {
	        this.requestConverter = requestConverter;
	        return this;
	    }

	    public InstantiateVnfContinueRunnableBuilder setDbManager(VnfmJobExecutionMapper dbManager) {
	        this.dbManager = dbManager;
	        return this;
	    }

	    public InstantiateVnfContinueRunnableBuilder setVnfcDbMgmr(VnfcResourceInfoMapper vnfcDbMgmr) {
	        this.vnfcDbMgmr = vnfcDbMgmr;
	        return this;
	    }

	    public InstantiateVnfContinueRunnable build() {
	        return new InstantiateVnfContinueRunnable(this);
	    }
	}
	
		
	private InstantiateVnfContinueRunnable(InstantiateVnfContinueRunnableBuilder builder) {
	    
	    this.driverRequest = builder.driverRequest;
        this.vnfInstanceId = builder.vnfInstanceId;
        this.jobId = builder.jobId;
        this.nslcmMgmr = builder.nslcmMgmr; 
        this.catalogMgmr = builder.catalogMgmr;
        this.cbamMgmr = builder.cbamMgmr;
        this.requestConverter = builder.requestConverter;
        this.jobDbMgmr = builder.dbManager;
        this.vnfmId = builder.vnfmId;
        this.vnfcDbMgmr = builder.vnfcDbMgmr;
	    
    }

    public void run() {
		//step 1 handle vnf package
		handleVnfPackage();
		
		handleGrant();
		
		handleModify();
		try {
			//step 5: instantiate vnf
			CBAMInstantiateVnfResponse cbamInstantiateResponse = handleInstantiate();
			
			handleNotify(cbamInstantiateResponse.getId());
		} catch (Exception e) {
			logger.error("InstantiateVnfContinueRunnable --> handleInstantiate or handleNotify error.", e);
		}
	}

	private void handleNotify(String execId) {
		boolean instantiateFinished = false;
		
		do {
			try {
				logger.info(" InstantiateVnfContinueRunnable --> handleNotify execId is " + execId);
				CBAMQueryOperExecutionResponse exeResponse = cbamMgmr.queryOperExecution(execId);
				if (exeResponse.getStatus() == CommonEnum.OperationStatus.FINISHED || exeResponse.getStatus() == CommonEnum.OperationStatus.FAILED)
				{
					instantiateFinished = true;
					handleCbamInstantiateResponse(exeResponse, jobId);
					if (exeResponse.getStatus() == CommonEnum.OperationStatus.FINISHED)
					{
						
						logger.info("Start to get vnfc resource");
						List<VnfcResourceInfo> vnfcResources = new ArrayList<>();
								
						try {
							vnfcResources = cbamMgmr.queryVnfcResource(vnfInstanceId);
						} catch (Exception e) {
							logger.error("Error to queryVnfcResource.", e);
						}
						
						logger.info("vnfc resource for vnfInstanceId " + vnfInstanceId + " is: " + gson.toJson(vnfcResources));
						logger.info("End to get vnfc resource");
						
						if(vnfcResources == null)
						{
							vnfcResources = new ArrayList<>();
						}
						logger.info("Start to notify LCM the instantiation result");
						NslcmNotifyLCMEventsRequest nslcmNotifyReq = buildNslcmNotifyLCMEventsRequest(vnfcResources);
						
//						OperateTaskProgress.setAffectedVnfc(nslcmNotifyReq.getAffectedVnfc());
						
						nslcmMgmr.notifyVnf(nslcmNotifyReq, vnfmId, vnfInstanceId);
						logger.info("End to notify LCM the instantiation result");
					}
				}
				else {
					Thread.sleep(60000);
				}
				
			} catch (Exception e) {
				logger.error("InstantiateVnfContinueRunnable --> handleNotify error.", e);
			}
		} while(!instantiateFinished);
		
	}

	private CBAMInstantiateVnfResponse handleInstantiate() throws IOException {
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
		return gson.fromJson(fileContent, CBAMModifyVnfRequest.class);
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
	
	private NslcmNotifyLCMEventsRequest buildNslcmNotifyLCMEventsRequest(List<VnfcResourceInfo> vnfcResources) {
		NslcmNotifyLCMEventsRequest request = new NslcmNotifyLCMEventsRequest();
	    request.setStatus(CommonEnum.status.result);
		request.setVnfInstanceId(vnfInstanceId);
		request.setOperation(CommonConstants.NSLCM_OPERATION_INSTANTIATE);
		request.setJobId(jobId);
		
		List<AffectedVnfc> affectedVnfcs = convertVnfcResourceToAffectecVnfc(vnfcResources);
		request.setAffectedVnfc(affectedVnfcs);
		return request;
	}

	private List<AffectedVnfc> convertVnfcResourceToAffectecVnfc(List<VnfcResourceInfo> vnfcResources) {
		List<AffectedVnfc> vnfcs = new ArrayList<>();
		for(VnfcResourceInfo resource : vnfcResources)
		{
			if(resource.getComputeResource() != null && "OS::Nova::Server".equalsIgnoreCase(resource.getComputeResource().getResourceType()))
			{
				AffectedVnfc vnfc = new AffectedVnfc();
				vnfc.setVnfcInstanceId(resource.getId());
				vnfc.setVduId(resource.getVduId());
				vnfc.setVimid(resource.getComputeResource().getVimId());
				vnfc.setVmid(resource.getComputeResource().getResourceId());
				
				vnfcs.add(vnfc);
				
				vnfcDbMgmr.insert(vnfc);
			}
		}
		return vnfcs;
	}

	private NslcmGrantVnfRequest buildNslcmGrantVnfRequest() {
		NslcmGrantVnfRequest request = new NslcmGrantVnfRequest();
		
		request.setVnfInstanceId(vnfInstanceId);
		request.setLifecycleOperation(LifecycleOperation.Instantiate);
		request.setJobId(jobId);
		
		ResourceDefinition resource = getFreeVnfResource();
		List<ResourceDefinition> resourceList = new ArrayList<>();
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

	private void handleCbamInstantiateResponse(OperationExecution cbamInstantiateResponse, String jobId) {
		VnfmJobExecutionInfo jobInfo = jobDbMgmr.findOne(Long.parseLong(jobId));
		
		jobInfo.setVnfmExecutionId(cbamInstantiateResponse.getId());
		long nowTime = System.currentTimeMillis();
		if(CommonEnum.OperationStatus.FAILED == cbamInstantiateResponse.getStatus()){
			jobInfo.setStatus(CommonConstants.CBAM_OPERATION_STATUS_ERROR);
//			jobInfo.setStatus(CommonConstants.CBAM_OPERATION_STATUS_FINISH);
		} else if(CommonEnum.OperationStatus.OTHER == cbamInstantiateResponse.getStatus()){
			jobInfo.setStatus(CommonConstants.CBAM_OPERATION_STATUS_PROCESSING);
		} else if(CommonEnum.OperationStatus.FINISHED == cbamInstantiateResponse.getStatus()){
			jobInfo.setStatus(CommonConstants.CBAM_OPERATION_STATUS_FINISH);
			jobInfo.setOperateEndTime(nowTime);
			
		}
		else{
			jobInfo.setStatus(CommonConstants.CBAM_OPERATION_STATUS_START);
		}
			
		jobDbMgmr.update(jobInfo);
	}

	public void setDriverRequest(InstantiateVnfRequest driverRequest) {
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
