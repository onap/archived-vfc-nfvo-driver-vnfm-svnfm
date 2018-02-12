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
 
package org.onap.vfc.nfvo.driver.vnfm.svnfm.db.bean;

import java.io.Serializable;

//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.GeneratedValue;
//import javax.persistence.GenerationType;
//import javax.persistence.Id;
//import javax.persistence.Table;


//@Entity
//@Table(name = "vnfm_job_execution_info")
public class VnfmJobExecutionInfo implements Serializable {
	private static final long serialVersionUID = -288015953900428312L;

//	@Id
//	@GeneratedValue(strategy=GenerationType.AUTO)
//	@Column(name = "job_id")
	private long jobId;
	
//	@Column(name = "vnfm_execution_id")
	private String vnfmExecutionId;
	
//	@Column(name = "vnf_instance_id")
	private String vnfInstanceId;
	
//	@Column(name = "vnfm_interface_name")
	private String vnfmInterfceName;
	
//	@Column(name = "status")
	private String status;
	
//	@Column(name = "operate_start_time")
	private long operateStartTime;
	
//	@Column(name = "operate_end_time")
	private long operateEndTime;

	public long getJobId() {
		return jobId;
	}

	public void setJobId(long jobId) {
		this.jobId = jobId;
	}

	public String getVnfmExecutionId() {
		return vnfmExecutionId;
	}

	public void setVnfmExecutionId(String vnfmExecutionId) {
		this.vnfmExecutionId = vnfmExecutionId;
	}

	public String getVnfInstanceId() {
		return vnfInstanceId;
	}

	public void setVnfInstanceId(String vnfInstanceId) {
		this.vnfInstanceId = vnfInstanceId;
	}

	public String getVnfmInterfceName() {
		return vnfmInterfceName;
	}

	public void setVnfmInterfceName(String vnfmInterfceName) {
		this.vnfmInterfceName = vnfmInterfceName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	
   

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	
	public long getOperateStartTime() {
		return operateStartTime;
	}

	public void setOperateStartTime(long operateStartTime) {
		this.operateStartTime = operateStartTime;
	}

	public long getOperateEndTime() {
		return operateEndTime;
	}

	public void setOperateEndTime(long operateEndTime) {
		this.operateEndTime = operateEndTime;
	}

	@Override
	public String toString()
	{
		return " VnfmJobRecord: [ "
				+ super.toString()      
				+ ", jobId = " + jobId      
				+ ", vnfInstanceId = " + vnfInstanceId       
				+ ", vnfmExecutionId = " + vnfmExecutionId        
				+ ", vnfmInterfceName = " + vnfmInterfceName  
				+ ", status = " + status 
				+ ", operateStartTime = "+ operateStartTime
				+ ", operateEndTime = "+ operateEndTime
				+ "]";
	
	}
	
	
}
