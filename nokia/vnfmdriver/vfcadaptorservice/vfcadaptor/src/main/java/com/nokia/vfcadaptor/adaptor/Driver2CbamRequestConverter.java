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

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

import com.nokia.vfcadaptor.cbam.bo.CBAMCreateVnfRequest;
import com.nokia.vfcadaptor.cbam.bo.CBAMHealVnfRequest;
import com.nokia.vfcadaptor.cbam.bo.CBAMInstantiateVnfRequest;
import com.nokia.vfcadaptor.cbam.bo.CBAMScaleVnfRequest;
import com.nokia.vfcadaptor.cbam.bo.CBAMTerminateVnfRequest;
import com.nokia.vfcadaptor.cbam.bo.entity.EndpointInfo;
import com.nokia.vfcadaptor.cbam.bo.entity.ExtVirtualLinkData;
import com.nokia.vfcadaptor.cbam.bo.entity.OPENSTACK_V3_INFO;
import com.nokia.vfcadaptor.cbam.bo.entity.OpenStackAccessInfoV3;
import com.nokia.vfcadaptor.cbam.bo.entity.VimInfo;
import com.nokia.vfcadaptor.cbam.bo.entity.VimInfoType;
import com.nokia.vfcadaptor.cbam.bo.entity.VnfExtCpData;
import com.nokia.vfcadaptor.constant.CommonEnum;
import com.nokia.vfcadaptor.nslcm.bo.NslcmGrantVnfResponse;
import com.nokia.vfcadaptor.nslcm.bo.entity.GrantInfo;
import com.nokia.vfcadaptor.nslcm.bo.entity.VimComputeResourceFlavour;
import com.nokia.vfcadaptor.vnfmdriver.bo.HealVnfRequest;
import com.nokia.vfcadaptor.vnfmdriver.bo.InstantiateVnfRequest;
import com.nokia.vfcadaptor.vnfmdriver.bo.ScaleVnfRequest;
import com.nokia.vfcadaptor.vnfmdriver.bo.TerminateVnfRequest;

@Component
public class Driver2CbamRequestConverter {
	
	public CBAMCreateVnfRequest createrqConvert(InstantiateVnfRequest driverRequest) {
		CBAMCreateVnfRequest request = new CBAMCreateVnfRequest();

		request.setVnfdId("vnfd_001");
		request.setName(driverRequest.getVnfInstanceName());
		request.setDescription(driverRequest.getVnfInstanceDescription());
		return request;
	}

	public CBAMInstantiateVnfRequest InstantiateCqonvert(InstantiateVnfRequest driverRequest,
			NslcmGrantVnfResponse nslc, GrantInfo grant, VimComputeResourceFlavour vimco) {
		CBAMInstantiateVnfRequest request = new CBAMInstantiateVnfRequest();
		List<VimInfo> vims = new ArrayList<VimInfo>();
		VimInfo vim = new VimInfo();
		VimInfoType type = new VimInfoType();
		EndpointInfo inter = new EndpointInfo();
		OPENSTACK_V3_INFO openstackV3 = new OPENSTACK_V3_INFO();
		
		vim.setId(nslc.getVim().getVimId());
		openstackV3.setId(nslc.getVim().getVimId());
		inter.setEndpoint(nslc.getVim().getInterfaceEndpoint());
		openstackV3.setInterfaceInfo(inter);
		openstackV3.setVimInfoType(type);
		OpenStackAccessInfoV3 v3 = new OpenStackAccessInfoV3();
		v3.setUsername(nslc.getVim().getAccessInfo().getUsername());
		v3.setPassword(nslc.getVim().getAccessInfo().getPassword());
		openstackV3.setAccessInfo(v3);
		type.setOPENSTACK_V3_INFO(openstackV3);
		vim.setVimInfoType(type);
		vims.add(vim);
		List<ExtVirtualLinkData> list = new ArrayList<ExtVirtualLinkData>();
		ExtVirtualLinkData ext = new ExtVirtualLinkData();

		ext.setResourceId(grant.getResourceDefinitionId());
		ext.setVimId(grant.getVimId());
		List<VnfExtCpData> cps = new ArrayList<VnfExtCpData>();
		VnfExtCpData cp = new VnfExtCpData();
		cp.setCpdId(vimco.getVduId());
		ext.setExtCps(cps);
		request.setVims(vims);
		request.setFlavourId(driverRequest.getFlavourId());
		request.setExtVirtualLinks(list);
		// resquest.setVnfInstanceId(driverRequest.getExtVirtualLink().get(0).getVlInstanceId());
		return request;
	}

	public CBAMTerminateVnfRequest terminaterqConvert(TerminateVnfRequest driverRequest) {
		CBAMTerminateVnfRequest request = new CBAMTerminateVnfRequest();
		request.setTerminationType(driverRequest.getTerminationType());
		request.setGracefulTerminationTimeout(driverRequest.getGracefulTerminationTimeout());
		return request;
	}

	public CBAMHealVnfRequest healconvert(HealVnfRequest driverRequest) {
		CBAMHealVnfRequest request = new CBAMHealVnfRequest();
		request.setCause("");
		request.setAdditionalParams("");
		return request;
	}

	public CBAMScaleVnfRequest scaleconvert(ScaleVnfRequest driverRequest) {
		CBAMScaleVnfRequest request = new CBAMScaleVnfRequest();
		if (driverRequest.getType().equals("SCALE_OUT")) {
			request.setType(CommonEnum.ScaleDirection.OUT);
		} else {
			request.setType(CommonEnum.ScaleDirection.IN);
		}
		request.setAspectId(driverRequest.getAspectId());
		request.setNumberOfSteps(driverRequest.getNumberOfSteps());
		request.setAdditionalParams(driverRequest.getAdditionalParam());
		return request;
	}

}
