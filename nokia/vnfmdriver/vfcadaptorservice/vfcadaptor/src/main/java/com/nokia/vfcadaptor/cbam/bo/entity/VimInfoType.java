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
package com.nokia.vfcadaptor.cbam.bo.entity;

public class VimInfoType {
	
	public  OPENSTACK_V2_INFO  OPENSTACK_V2_INFO;
	public OPENSTACK_V3_INFO  OPENSTACK_V3_INFO;
	public VMWARE_VCLOUD_INFO VMWARE_VCLOUD_INFO;
	public OTHER_VIM_INFO OTHER_VIM_INFO;
	public OPENSTACK_V2_INFO getOPENSTACK_V2_INFO() {
		return OPENSTACK_V2_INFO;
	}
	public void setOPENSTACK_V2_INFO(OPENSTACK_V2_INFO oPENSTACK_V2_INFO) {
		OPENSTACK_V2_INFO = oPENSTACK_V2_INFO;
	}
	public OPENSTACK_V3_INFO getOPENSTACK_V3_INFO() {
		return OPENSTACK_V3_INFO;
	}
	public void setOPENSTACK_V3_INFO(OPENSTACK_V3_INFO oPENSTACK_V3_INFO) {
		OPENSTACK_V3_INFO = oPENSTACK_V3_INFO;
	}
	public VMWARE_VCLOUD_INFO getVMWARE_VCLOUD_INFO() {
		return VMWARE_VCLOUD_INFO;
	}
	public void setVMWARE_VCLOUD_INFO(VMWARE_VCLOUD_INFO vMWARE_VCLOUD_INFO) {
		VMWARE_VCLOUD_INFO = vMWARE_VCLOUD_INFO;
	}
	public OTHER_VIM_INFO getOTHER_VIM_INFO() {
		return OTHER_VIM_INFO;
	}
	public void setOTHER_VIM_INFO(OTHER_VIM_INFO oTHER_VIM_INFO) {
		OTHER_VIM_INFO = oTHER_VIM_INFO;
	}
		 
		 
		
	 	
	 
	 
	 

}
