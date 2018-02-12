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
 
package org.onap.vfc.nfvo.driver.vnfm.svnfm.db.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.bean.VnfmJobExecutionInfo;

@Mapper
public interface VnfmJobExecutionMapper {
	@Select("SELECT * FROM vnfm_job_execution_info")
    @Results({
        @Result(property = "jobId",  column = "job_id"),
        @Result(property = "vnfmExecutionId", column = "vnfm_execution_id"),
        @Result(property = "vnfInstanceId", column = "vnf_instance_id"),
        @Result(property = "vnfmInterfceName", column = "vnfm_interface_name"),
        @Result(property = "status", column = "status"),
        @Result(property = "operateStartTime", column = "operate_start_time"),
        @Result(property = "operateEndTime", column = "operate_end_time")
    })
    List<VnfmJobExecutionInfo> getAll();

    @Select("SELECT * FROM vnfm_job_execution_info WHERE job_id = #{jobId}")
    @Results({
    	@Result(property = "jobId",  column = "job_id"),
        @Result(property = "vnfmExecutionId", column = "vnfm_execution_id"),
        @Result(property = "vnfInstanceId", column = "vnf_instance_id"),
        @Result(property = "vnfmInterfceName", column = "vnfm_interface_name"),
        @Result(property = "status", column = "status"),
        @Result(property = "operateStartTime", column = "operate_start_time"),
        @Result(property = "operateEndTime", column = "operate_end_time")
    })
    VnfmJobExecutionInfo findOne(Long jobId);

    @Insert("INSERT INTO vnfm_job_execution_info(vnfm_execution_id,vnf_instance_id,vnfm_interface_name,status,operate_start_time, operate_end_time) VALUES(#{vnfmExecutionId}, #{vnfInstanceId}, #{vnfmInterfceName}, #{status}, #{operateStartTime}, #{operateEndTime})")
    void insert(VnfmJobExecutionInfo user);

    @Update("UPDATE vnfm_job_execution_info SET vnfm_execution_id=#{vnfmExecutionId},vnf_instance_id=#{vnfInstanceId},vnfm_interface_name=#{vnfmInterfceName},status=#{status},operate_start_time=#{operateStartTime}, operate_end_time=#{operateEndTime} WHERE job_id =#{jobId}")
    void update(VnfmJobExecutionInfo user);

    @Delete("DELETE FROM vnfm_job_execution_info WHERE id =#{id}")
    void delete(Long id);
    
    @Select("select * from vnfm_job_execution_info where job_id = (select max(job_id) from vnfm_job_execution_info)")
    @Results({
    	@Result(property = "jobId",  column = "job_id"),
        @Result(property = "vnfmExecutionId", column = "vnfm_execution_id"),
        @Result(property = "vnfInstanceId", column = "vnf_instance_id"),
        @Result(property = "vnfmInterfceName", column = "vnfm_interface_name"),
        @Result(property = "status", column = "status"),
        @Result(property = "operateStartTime", column = "operate_start_time"),
        @Result(property = "operateEndTime", column = "operate_end_time")
    })
    VnfmJobExecutionInfo findNewestJobInfo();
}
