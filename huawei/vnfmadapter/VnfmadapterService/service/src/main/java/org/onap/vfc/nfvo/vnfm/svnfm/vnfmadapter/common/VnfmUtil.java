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

package org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common;

import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.restclient.RestfulResponse;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.servicetoken.VnfmRestfulUtil;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.constant.Constant;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.constant.ParamConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Provide function of getting vnfmInfo
 * <br/>
 *
 * @author
 * @version VFC 1.0 Aug 25, 2016
 */
public final class VnfmUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(VnfmUtil.class);

    private VnfmUtil() {

    }

    /**
     * <br>
     * 
     * @param vnfmId
     * @return
     * @since VFC 1.0
     */
    public static JSONObject getVnfmById(String vnfmId) {
        RestfulResponse rsp = VnfmRestfulUtil.getRemoteResponse(String.format(ParamConstants.ESR_GET_VNFM_URL, vnfmId),
                VnfmRestfulUtil.TYPE_GET, null);
        if(rsp == null) {
            LOGGER.error("funtion=getVnfmById, response is null.");
            return null;
        }
        if(rsp.getStatus() != Constant.HTTP_OK) {
            LOGGER.error("funtion=getVnfmById, status={}", rsp.getStatus());
            return null;
        }
        JSONObject esrVnfm = JSONObject.fromObject(rsp.getResponseContent());
        LOGGER.info("esrVnfm: {}", esrVnfm);
        JSONObject vnfmJson = parseEsrVnfm(esrVnfm);
        LOGGER.info("vnfmJson: {}", esrVnfm);
        return vnfmJson;
    }

    /**
     * <br>
     * 
     * @param
     *            esrVnfm
     *            {
     *            "vnfm-id": "",
     *            "vim-id": "",
     *            "certificate-url": "",
     *            "resource-version": "",
     *            "esr-system-info-list": [{
     *            "esr-system-info-id": "",
     *            "system-name": "",
     *            "type": "",
     *            "vendor": "",
     *            "version": "",
     *            "service-url": "",
     *            "user-name": "",
     *            "password": "",
     *            "system-type": "",
     *            "protocal": "",
     *            "ssl-cacert": "",
     *            "ssl-insecure": "",
     *            "ip-address": "",
     *            "port": "",
     *            "cloud-domain": "",
     *            "default-tenant": "",
     *            "resource-version": "",
     *            "relationship-list": [
     *            ]
     *            }
     *            ],
     *            "relationship-list": [{
     *            "related-to": "",
     *            "related-link": "",
     *            "relationship-data": [],
     *            "related-to-property": []
     *            }
     *            ]
     *            }
     * @return
     *         vnfmJson
     *         {
     *         "vnfmId": "1234",
     *         "name": "vnfm",
     *         "type": "Tacker",
     *         "vimId": "",
     *         "vendor": "huawei",
     *         "version": "v1.0",
     *         "description": "vnfm",
     *         "certificateUrl": "",
     *         "url": "https://192.168.44.126:30001",
     *         "userName": "manoadmin",
     *         "password": "User@12345",
     *         "createTime": "2016-07-06 15:33:18"
     *         }
     * @since VFC 1.0
     */
    private static JSONObject parseEsrVnfm(JSONObject esrVnfm) {
        JSONObject vnfmObj = new JSONObject();
        JSONObject esrSysInfo = esrVnfm.getJSONArray("esr-system-info-list").getJSONObject(0);
        vnfmObj.put(Constant.VNFMID, esrSysInfo.getString("esr-system-info-id"));
        vnfmObj.put("name", esrSysInfo.getString("system-name"));
        vnfmObj.put("type", esrSysInfo.getString("type"));
        vnfmObj.put("vimId", esrVnfm.getString("vim-id"));
        vnfmObj.put("vendor", esrSysInfo.getString("vendor"));
        vnfmObj.put("version", esrSysInfo.getString("version"));
        vnfmObj.put("description", "");
        vnfmObj.put("certificateUrl", esrVnfm.getString("certificate-url"));
        vnfmObj.put("url", esrSysInfo.getString("service-url"));
        vnfmObj.put("userName", esrSysInfo.getString("user-name"));
        vnfmObj.put("password", esrSysInfo.getString("password"));
        vnfmObj.put("createTime", "");
        return vnfmObj;
    }

    public static JSONObject mockForTest(String vnfmId) {
        String vInfo =
                "{\"vnfmId\":\"1234\", \"name\":\"vnfm\", \"type\":\"Tacker\", \"vimId\":\"\", \"vendor\":\"huawei\", \"version\":\"v1.0\", \"description\":\"vnfm\", \"certificateUrl\":\"\", \"url\":\"https://192.168.44.126:30001\", \"userName\":\"manoadmin\", \"password\":\"User@12345\", \"createTime\":\"2016-07-06 15:33:18\"}";
        JSONObject json = JSONObject.fromObject(vInfo);
        json.put(Constant.VNFMID, vnfmId);
        return json;
    }

    /**
     * Get vnfmInfo by ip
     * <br/>
     *
     * @param ip
     * @return
     * @since VFC 1.0
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
                return vnfmList.getJSONObject(i).getString(Constant.VNFMID);
            }
        }

        return "";
    }
}
