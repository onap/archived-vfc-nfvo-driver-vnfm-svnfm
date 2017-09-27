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

import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMCreateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMHealVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMInstantiateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMScaleVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMTerminateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.EndpointInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.ExtVirtualLinkData;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.OPENSTACK_V3_INFO;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.OpenStackAccessInfoV3;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.VimInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.VimInfoType;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.VnfExtCpData;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonEnum;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.NslcmGrantVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.GrantInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.VimComputeResourceFlavour;

import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.HealVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.InstantiateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.ScaleVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.TerminateVnfRequest;

import org.springframework.stereotype.Component;

@Component
public class Driver2CbamRequestConverter {
	
	public CBAMCreateVnfRequest createReqConvert(InstantiateVnfRequest driverRequest) {
		CBAMCreateVnfRequest request = new CBAMCreateVnfRequest();

		request.setVnfdId("vnfd_001");
		request.setName(driverRequest.getVnfInstanceName());
		request.setDescription(driverRequest.getVnfInstanceDescription());
		return request;
	}

	public CBAMInstantiateVnfRequest InstantiateReqConvert(InstantiateVnfRequest driverRequest,
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

	public CBAMTerminateVnfRequest terminateReqConvert(TerminateVnfRequest driverRequest) {
		CBAMTerminateVnfRequest request = new CBAMTerminateVnfRequest();
		request.setTerminationType(driverRequest.getTerminationType());
		request.setGracefulTerminationTimeout(driverRequest.getGracefulTerminationTimeout());
		return request;
	}

	public CBAMHealVnfRequest healReqConvert(HealVnfRequest driverRequest) {
		CBAMHealVnfRequest request = new CBAMHealVnfRequest();
		request.setCause("");
		request.setAdditionalParams("");
		return request;
	}

	public CBAMScaleVnfRequest scaleReqconvert(ScaleVnfRequest driverRequest) {
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
