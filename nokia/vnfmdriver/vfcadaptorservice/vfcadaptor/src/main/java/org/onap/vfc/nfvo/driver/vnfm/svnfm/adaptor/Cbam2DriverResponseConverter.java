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

import java.util.ArrayList;
import java.util.List;

import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMCreateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMHealVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMInstantiateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMQueryVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMScaleVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMTerminateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.OperationExecution;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonConstants;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.bean.VnfmJobExecutionInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.repository.VnfmJobExecutionRepository;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.HealVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.InstantiateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.OperStatusVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.QueryVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.ScaleVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.TerminateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.entity.ResponseDescriptor;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.entity.ResponseHistoryList;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.entity.VnfInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Cbam2DriverResponseConverter {
	
	@Autowired
	private VnfmJobExecutionRepository jobDbManager;
	
	public InstantiateVnfResponse createRspConvert(CBAMCreateVnfResponse cbamResponse) {

		VnfmJobExecutionInfo jobInfo = new VnfmJobExecutionInfo();
		jobInfo.setVnfInstanceId(cbamResponse.getId());
		jobInfo.setVnfmInterfceName(CommonConstants.NSLCM_OPERATION_INSTANTIATE);
		jobInfo.setStatus(CommonConstants.CBAM_OPERATION_STATUS_START);
		
		VnfmJobExecutionInfo jobInfo1 = (VnfmJobExecutionInfo)jobDbManager.save(jobInfo);
		Long jobId = jobInfo1.getJobId();
		
		InstantiateVnfResponse response = new InstantiateVnfResponse();
		response.setJobId(jobId.longValue() + "");
		response.setVnfInstanceId(cbamResponse.getId());

		return response;
	}

	public InstantiateVnfResponse instantiateRspConvert(CBAMInstantiateVnfResponse cbamResponse) {
		InstantiateVnfResponse response = new InstantiateVnfResponse();
		response.setJobId("1");
		response.setVnfInstanceId("");
		return response;
	}

	public TerminateVnfResponse terminateRspConvert(CBAMTerminateVnfResponse cbamResponse) {

		TerminateVnfResponse response = new TerminateVnfResponse();
		response.setJobId("1");
		return response;
	}

	public QueryVnfResponse queryRspConvert(CBAMQueryVnfResponse cbamResponse) {
		QueryVnfResponse response = new QueryVnfResponse();
		VnfInfo vnf = new VnfInfo();
		vnf.setVnfdId(cbamResponse.getVnfdId());
		vnf.setVersion(cbamResponse.getVnfdVersion());
		vnf.setVnfInstanceId(cbamResponse.getId());
		vnf.setVnfInstanceName(cbamResponse.getName());
		vnf.setVnfInstanceDescription(cbamResponse.getDescription());
		vnf.setVnfPackageId(cbamResponse.getOnboardedVnfPkgInfoId());
		vnf.setVnfProvider(cbamResponse.getVnfProvider());
		vnf.setVnfStatus(cbamResponse.getInstantiationState());
		vnf.setVnfType(cbamResponse.getVnfSoftwareVersion());
		return response;
	}

	public OperStatusVnfResponse operRspConvert(List<OperationExecution> cbamResponse) {

		OperStatusVnfResponse response = new OperStatusVnfResponse();
		List<OperationExecution> oper=cbamResponse;
	    for(int i=0;i<oper.size();i++) {
		 response.setJobId(oper.get(i).getId());
		 ResponseDescriptor er=new  ResponseDescriptor();
		 er.setProgress(i);
		 if(oper.get(i).getStatus().equals("STARTED")) {
		 er.setStatus("started");
		 }else
		 if(oper.get(i).getStatus().equals("FINISHED")) {
			 er.setStatus("finished");
		 }else
		 if(oper.get(i).getStatus().equals("FAILED")) {
			 er.setStatus("error");
		 }else
		 if(oper.get(i).getStatus().equals("OTHER")) {
			 er.setStatus("processing");
		 }else {
			 er.setStatus("error");
		 }
		 er.setStatusDescription("");
		 er.setErrorCode(null);
		 er.setResponseId(oper.get(i).getGrantId().hashCode());
		 List<ResponseHistoryList> list=new ArrayList<ResponseHistoryList>();
		 ResponseHistoryList relist=new ResponseHistoryList();
		 relist.setProgress(i);
		 relist.setStatus(er.getStatus());
		 relist.setStatusDescription("");
		 relist.setErrorCode(null);
		 relist.setResponseId(er.getResponseId());
		 list.add(relist);
		 er.setResponseHistoryList(list);
		 response.setResponseDescriptor(er);
	    }
		return response;
	}

	public HealVnfResponse healRspConvert(CBAMHealVnfResponse cbamResponse) {
		 HealVnfResponse response=new  HealVnfResponse();
		 response.setJobId("1");
		return response;
	}
	
	public ScaleVnfResponse scaleRspConvert(CBAMScaleVnfResponse cbamResponse) {
		ScaleVnfResponse response=new  ScaleVnfResponse();
		 response.setJobId("1");
		return response;
	}
}
