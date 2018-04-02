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
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.bean.VnfmSubscriptionInfo;

@Mapper
public interface VnfmSubscriptionsMapper {
	@Select("SELECT * FROM vnfm_subscription_info")
    @Results({
        @Result(property = "id",  column = "id"),
        @Result(property = "driverCallbackUrl",  column = "driver_callback_url"),
        @Result(property = "nslcmCallbackUrl",  column = "nslcm_callback_url")
    })
    List<VnfmSubscriptionInfo> getAll();

    @Select("SELECT * FROM vnfm_subscription_info WHERE id = #{id}")
    @Results({
    	@Result(property = "id",  column = "id"),
        @Result(property = "driverCallbackUrl",  column = "driver_callback_url"),
        @Result(property = "nslcmCallbackUrl",  column = "nslcm_callback_url")
    })
    VnfmSubscriptionInfo findOne(String id);

    @Insert("INSERT IGNORE INTO vnfm_subscription_info(id, driver_callback_url, nslcm_callback_url) VALUES(#{id}, #{driverCallbackUrl}, #{nslcmCallbackUrl})")
    void insert(VnfmSubscriptionInfo info);
    
    @Insert("update vnfm_subscription_info set driver_callback_url = #{driverCallbackUrl}, nslcm_callback_url = #{nslcmCallbackUrl}) WHERE id = #{id}")
    void update(VnfmSubscriptionInfo info);
    

    @Delete("DELETE FROM vnfm_subscription_info WHERE id =#{id}")
    void delete(String id);
}
