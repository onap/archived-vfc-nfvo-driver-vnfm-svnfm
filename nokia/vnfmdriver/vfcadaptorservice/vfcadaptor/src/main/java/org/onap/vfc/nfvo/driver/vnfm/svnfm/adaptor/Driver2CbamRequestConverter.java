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
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.OpenStackAccessInfoV2;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.OpenStackAccessInfoV3;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.OpenstackV2Info;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.OpenstackV3Info;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.OtherVimInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.VCloudAccessInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.VimInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.VimInfoType;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.VmwareVcloudInfo;
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

		request.setVnfdId(driverRequest.getVnfDescriptorId());
		request.setName(driverRequest.getVnfInstanceName());
		request.setDescription(driverRequest.getVnfInstanceDescription());
		return request;
	}

	public CBAMInstantiateVnfRequest InstantiateReqConvert(InstantiateVnfRequest driverRequest,
			NslcmGrantVnfResponse nslc, GrantInfo grant, VimComputeResourceFlavour vimco) {
		CBAMInstantiateVnfRequest request = new CBAMInstantiateVnfRequest();
		
		List<VimInfo> vims = new ArrayList<VimInfo>();
		VimInfo vim = new VimInfo();
		EndpointInfo inter = new EndpointInfo();
		List<ExtVirtualLinkData> list = new ArrayList<ExtVirtualLinkData>();
		ExtVirtualLinkData ext = new ExtVirtualLinkData();
		List<VnfExtCpData> cps = new ArrayList<VnfExtCpData>();
		VnfExtCpData cp = new VnfExtCpData();
		OpenStackAccessInfoV3 v3 = new OpenStackAccessInfoV3();
		OpenStackAccessInfoV2 v2 = new OpenStackAccessInfoV2();
		VCloudAccessInfo vcloudInfo = new VCloudAccessInfo();
		if(vim.getVimInfoType().equals(VimInfoType.OPENSTACK_V2_INFO)) {
			OpenstackV2Info openstackV2=new OpenstackV2Info();
			List<org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.entity.ExtVirtualLinkData>  link=driverRequest.getExtVirtualLink();
			for(int i=0;i<link.size();i++) {
				vim.setId(link.get(i).getVim().getVimId());
				inter.setEndpoint(link.get(i).getVim().getInterfaceEndpoint());
				openstackV2.setId(link.get(i).getVim().getVimId());
				openstackV2.setInterfaceInfo(inter);
				
				ext.setResourceId(link.get(i).getNetworkId());// todo resourceId
				
				cp.setCpdId(link.get(i).getCpdId());
				cps.add(cp);
				ext.setExtCps(cps);
				
				List<org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.entity.AccessInfo> accessInfo=link.get(i).getVim().getAccessInfo();
				for(int j=0;j<=accessInfo.size();j++) {
				v2.setUsername(accessInfo.get(j).getUsername());
				v2.setPassword(accessInfo.get(j).getPassword());
				v2.setTenant(accessInfo.get(j).getTenant());
				//todo region
				}
				openstackV2.setAccessInfo(v2);
				vims.add(vim);
				list.add(ext);
				}
		}else if(vim.getVimInfoType().equals(VimInfoType.OPENSTACK_V3_INFO)) {
			OpenstackV3Info openstackV3=new OpenstackV3Info();
			List<org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.entity.ExtVirtualLinkData>  link=driverRequest.getExtVirtualLink();
			for(int i=0;i<link.size();i++) {
				vim.setId(link.get(i).getVim().getVimId());
				inter.setEndpoint(link.get(i).getVim().getInterfaceEndpoint());
				openstackV3.setId(link.get(i).getVim().getVimId());
				openstackV3.setInterfaceInfo(inter);
				
                ext.setResourceId(link.get(i).getNetworkId());// todo resourceId
				
				cp.setCpdId(link.get(i).getCpdId());
				cps.add(cp);
				ext.setExtCps(cps);
					
				List<org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.entity.AccessInfo> accessInfo=link.get(i).getVim().getAccessInfo();
				for(int j=0;j<=accessInfo.size();j++) {
				v3.setUsername(accessInfo.get(j).getUsername());
				v3.setPassword(accessInfo.get(j).getPassword());
				//todo region project domain
				}
				openstackV3.setAccessInfo(v3);
				vims.add(vim);
				list.add(ext);
				}
				
		}else if(vim.getVimInfoType().equals(VimInfoType.OTHER_VIM_INFO)) {
			OtherVimInfo other=new OtherVimInfo();
			List<org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.entity.ExtVirtualLinkData>  link=driverRequest.getExtVirtualLink();
			for(int i=0;i<link.size();i++) {
				vim.setId(link.get(i).getVim().getVimId());
				inter.setEndpoint(link.get(i).getVim().getInterfaceEndpoint());
				other.setId(link.get(i).getVim().getVimId());
				
                ext.setResourceId(link.get(i).getNetworkId());// todo resourceId
				
				cp.setCpdId(link.get(i).getCpdId());
				cps.add(cp);
				ext.setExtCps(cps);
				vims.add(vim);
				list.add(ext);
				}
			
		}else if(vim.getVimInfoType().equals(VimInfoType.VMWARE_VCLOUD_INFO)) {
			VmwareVcloudInfo vcloud=new VmwareVcloudInfo();
			List<org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.entity.ExtVirtualLinkData>  link=driverRequest.getExtVirtualLink();
			for(int i=0;i<link.size();i++) {
				vim.setId(link.get(i).getVim().getVimId());
				inter.setEndpoint(link.get(i).getVim().getInterfaceEndpoint());
				vcloud.setId(link.get(i).getVim().getVimId());
				vcloud.setInterfaceInfo(inter);
				
                ext.setResourceId(link.get(i).getNetworkId());// todo resourceId
				
				cp.setCpdId(link.get(i).getCpdId());
				cps.add(cp);
				ext.setExtCps(cps);
				
				
				List<org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.entity.AccessInfo> accessInfo=link.get(i).getVim().getAccessInfo();
				for(int j=0;j<=accessInfo.size();j++) {
				vcloudInfo.setUsername(accessInfo.get(j).getUsername());
				vcloudInfo.setPassword(accessInfo.get(j).getPassword());
				}
				vcloud.setAccessInfo(vcloudInfo);
				vims.add(vim);
				list.add(ext);
				}
				
		}
		request.setFlavourId(driverRequest.getFlavourId());
		request.setVims(vims);
		request.setExtVirtualLinks(list);
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
