/*
 * Copyright 2016-2017 Huawei Technologies Co., Ltd.
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

package org.openo.nfvo.vnfmadapter.common;

import org.openo.baseservice.roa.util.restclient.RestfulResponse;
import org.openo.nfvo.vnfmadapter.common.servicetoken.VnfmRestfulUtil;
import org.openo.nfvo.vnfmadapter.service.constant.Constant;
import org.openo.nfvo.vnfmadapter.service.constant.ParamConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Provide function of getting vnfmInfo
 * <br/>
 *
 * @author
 * @version     NFVO 0.5  Aug 25, 2016
 */
public final class VnfmUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(VnfmUtil.class);

    private VnfmUtil() {

    }

    /**
     * Get vnfmInfo by ip
     * <br/>
     *
     * @param vnfmId
     * @return
     * @since  NFVO 0.5
     */
    public static JSONObject getVnfmById(String vnfmId) {
        RestfulResponse rsp = VnfmRestfulUtil.getRemoteResponse(String.format(ParamConstants.ESR_GET_VNFM_URL, vnfmId),
                VnfmRestfulUtil.TYPE_GET, null);
        if(rsp == null || rsp.getStatus() != Constant.HTTP_OK) {
            LOGGER.error("funtion=getVnfmById, status={}", rsp.getStatus());
            return null;
        }
        return JSONObject.fromObject(rsp.getResponseContent());
    }

    public static JSONObject mockForTest(String vnfmId){
        String vInfo = "{\"vnfmId\":\"1234\", \"name\":\"vnfm\", \"type\":\"Tacker\", \"vimId\":\"\", \"vendor\":\"huawei\", \"version\":\"v1.0\", \"description\":\"vnfm\", \"certificateUrl\":\"\", \"url\":\"https://192.168.44.126:30001\", \"userName\":\"manoadmin\", \"password\":\"User@12345\", \"createTime\":\"2016-07-06 15:33:18\"}";
        JSONObject json = JSONObject.fromObject(vInfo);
        json.put("vnfmId",vnfmId);
        return json;
    }

    /**
     * Get vnfmInfo by id
     * <br/>
     *
     * @param ip
     * @return
     * @since  NFVO 0.5
     */
    public static String getVnfmIdByIp(String ip) {
        RestfulResponse rsp =
                VnfmRestfulUtil.getRemoteResponse(ParamConstants.ESR_GET_VNFMS_URL, VnfmRestfulUtil.TYPE_GET, null);
        if(rsp == null || rsp.getStatus() != Constant.HTTP_OK) {
            return "";
        }

        JSONArray vnfmList = JSONArray.fromObject(rsp.getResponseContent());
        LOGGER.info("vnfm ip: {}, vnfmList: {}", ip, vnfmList);
        for(int i = 0; i < vnfmList.size(); i++) {
            if(vnfmList.getJSONObject(i).getString("url").contains(ip)) {
                return vnfmList.getJSONObject(i).getString("vnfmId");
            }
        }

        return "";
    }
}
