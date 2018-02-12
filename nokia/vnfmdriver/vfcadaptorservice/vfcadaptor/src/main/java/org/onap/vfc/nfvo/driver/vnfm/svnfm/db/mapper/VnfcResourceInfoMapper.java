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
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.AffectedVnfc;

@Mapper
public interface VnfcResourceInfoMapper {
	@Select("SELECT * FROM vnfm_vnfc_resource_info where vnf_instance_id = #{vnfInstanceId}")
    @Results({
        @Result(property = "vnfInstanceId", column = "vnf_instance_id"),
        @Result(property = "vnfcInstanceId", column = "vnfc_instance_id"),
        @Result(property = "vduId", column = "vdu_id"),
        @Result(property = "vimid", column = "vim_id"),
        @Result(property = "vmid", column = "vm_id"),
        @Result(property = "changeType", column = "change_type")
    })
    List<AffectedVnfc> getAllByInstanceId(String vnfInstanceId);
	
	@Insert("INSERT INTO vnfm_vnfc_resource_info(vnf_instance_id,vnfc_instance_id,vdu_id,vim_id, vm_id, change_type) VALUES(#{vnfInstanceId}, #{vnfcInstanceId}, #{vduId}, #{vimid}, #{vmid}, #{changeType})")
    void insert(AffectedVnfc resource);

    @Delete("DELETE FROM vnfm_vnfc_resource_info WHERE vnf_instance_id = #{vnfcInstanceId}")
    void deleteByInstanceId(String vnfcInstanceId);
}