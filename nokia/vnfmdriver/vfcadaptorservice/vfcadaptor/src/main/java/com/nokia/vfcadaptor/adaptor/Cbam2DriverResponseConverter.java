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
package com.nokia.vfcadaptor.adaptor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.nokia.vfcadaptor.cbam.bo.CBAMCreateVnfResponse;

import com.nokia.vfcadaptor.cbam.bo.CBAMInstantiateVnfResponse;
import com.nokia.vfcadaptor.cbam.bo.CBAMOperExecutVnfResponse;
import com.nokia.vfcadaptor.cbam.bo.CBAMQueryVnfResponse;
import com.nokia.vfcadaptor.cbam.bo.CBAMScaleVnfResponse;
import com.nokia.vfcadaptor.cbam.bo.CBAMTerminateVnfResponse;
import com.nokia.vfcadaptor.cbam.bo.entity.OperationExecution;
import com.nokia.vfcadaptor.cbam.bo.CBAMHealVnfResponse;
import com.nokia.vfcadaptor.vnfmdriver.bo.HealVnfResponse;

import com.nokia.vfcadaptor.vnfmdriver.bo.InstantiateVnfResponse;
import com.nokia.vfcadaptor.vnfmdriver.bo.OperStatusVnfResponse;
import com.nokia.vfcadaptor.vnfmdriver.bo.QueryVnfResponse;
import com.nokia.vfcadaptor.vnfmdriver.bo.ScaleVnfResponse;
import com.nokia.vfcadaptor.vnfmdriver.bo.TerminateVnfResponse;
import com.nokia.vfcadaptor.vnfmdriver.bo.entity.ResponseDescriptor;
import com.nokia.vfcadaptor.vnfmdriver.bo.entity.ResponseHistoryList;
import com.nokia.vfcadaptor.vnfmdriver.bo.entity.VnfInfo;

@Component
public class Cbam2DriverResponseConverter {
	

	public InstantiateVnfResponse createspConvert(CBAMCreateVnfResponse cbamResponse) {

		InstantiateVnfResponse response = new InstantiateVnfResponse();
		response.setJobId("1");
		response.setVnfInstanceId(cbamResponse.getVnfInfo().getId());

		return response;
	}

	public InstantiateVnfResponse instantspConvert(CBAMInstantiateVnfResponse cbamResponse) {
		InstantiateVnfResponse response = new InstantiateVnfResponse();
		response.setJobId("1");
		response.setVnfInstanceId(cbamResponse.getProblemDetails().getInstance());
		return response;
	}

	public TerminateVnfResponse terminaterspConvert(CBAMTerminateVnfResponse cbamResponse) {

		TerminateVnfResponse response = new TerminateVnfResponse();
		response.setJobId("1");
		return response;
	}

	public QueryVnfResponse queryspConvert(CBAMQueryVnfResponse cbamResponse) {
		QueryVnfResponse response = new QueryVnfResponse();
		VnfInfo vnf = new VnfInfo();
		vnf.setVnfdId(cbamResponse.getVnfInfo().getVnfdId());
		vnf.setVersion(cbamResponse.getVnfInfo().getVnfdVersion());
		vnf.setVnfInstanceId(cbamResponse.getVnfInfo().getId());
		vnf.setVnfInstanceName(cbamResponse.getVnfInfo().getName());
		vnf.setVnfInstanceDescription(cbamResponse.getVnfInfo().getDescription());
		vnf.setVnfPackageId(cbamResponse.getVnfInfo().getOnboardedVnfPkgInfoId());
		vnf.setVnfProvider(cbamResponse.getVnfInfo().getVnfProvider());
		vnf.setVnfStatus(cbamResponse.getVnfInfo().getInstantiationState());
		vnf.setVnfType(cbamResponse.getVnfInfo().getVnfSoftwareVersion());
		response.setVnfInfo(vnf);
		return response;
	}

	public OperStatusVnfResponse operspConvert(CBAMOperExecutVnfResponse cbamResponse) {

		OperStatusVnfResponse response = new OperStatusVnfResponse();
		List<OperationExecution> oper=cbamResponse.getOperationExecution();
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

	public HealVnfResponse healspconvert(CBAMHealVnfResponse cbamResponse) {
		 HealVnfResponse response=new  HealVnfResponse();
		 response.setJobId("1");
		return response;
	}
	
	public ScaleVnfResponse scalespconvert(CBAMScaleVnfResponse cbamResponse) {
		ScaleVnfResponse response=new  ScaleVnfResponse();
		 response.setJobId("1");
		return response;
	}
}
