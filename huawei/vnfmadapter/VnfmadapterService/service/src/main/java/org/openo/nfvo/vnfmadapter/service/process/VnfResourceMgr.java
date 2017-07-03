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

package org.openo.nfvo.vnfmadapter.service.process;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openo.baseservice.roa.util.restclient.RestfulResponse;
import org.openo.nfvo.vnfmadapter.common.servicetoken.VnfmRestfulUtil;
import org.openo.nfvo.vnfmadapter.service.constant.Constant;
import org.openo.nfvo.vnfmadapter.service.constant.ParamConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/**
 * Provide function of resource for VNFM.
 * <br/>
 * <p>
 * </p>
 *
 * @author
 * @version NFVO 0.5 Aug 25, 2016
 */
public class VnfResourceMgr {

    private static final Logger LOG = LoggerFactory.getLogger(VnfResourceMgr.class);

    /**
     * Provide function of grant resource for VNFM.
     * <br/>
     *
     * @param vnfObj
     * @param vnfId
     * @param vnfmId
     * @return
     * @since NFVO 0.5
     */
    public JSONObject grantVnfResource(JSONObject vnfObj, String vnfId, String vnfmId) {
        LOG.warn("function=grantVnfResource, msg=enter to grant vnf resource, params: {}", vnfObj);
        JSONObject resultJson = new JSONObject();
        resultJson.put("retCode", Constant.REST_FAIL);
        try {
            String type = vnfObj.getString("type");
            String requestType = vnfObj.getString("operation_right");
            String vnfName = vnfObj.getString("vnf_name");

            if(StringUtils.isEmpty(type) || StringUtils.isEmpty(requestType) || StringUtils.isEmpty(vnfName)
                    || StringUtils.isEmpty(vnfId)) {
                LOG.error("function=grantVnfResource, msg=grant basic params error");
                resultJson.put("errorMsg", "basic params error");
                return resultJson;
            }

            JSONArray vmList = vnfObj.getJSONArray("vm_list");

            Map<String, Integer> resMap = calculateGrantRes(vmList);

            if(null == resMap) {
                LOG.error("function=grantVnfResource, msg=grant resource params error");
                resultJson.put("errorMsg", "resource params error");
                return resultJson;
            }

            JSONObject grantObj = new JSONObject();
            grantObj.put("vimId", vnfObj.getString("vim_id"));
            grantObj.put("vnfId", vnfId);
            grantObj.put("vnfName", vnfName);
            grantObj.put("vnfmId", vnfmId);
            String action = getGrantAction(type, requestType);
            grantObj.put("action", action);

            JSONObject grantParam = parseGrantParam(resMap, grantObj);
            resultJson = sendGrantToResmgr(grantParam);
            LOG.error("function=grantVnfResource, resultJson={}.", resultJson);
        } catch(JSONException e) {
            LOG.error("function=grantVnfResource, msg=parse params occoured JSONException e={}.", e);
            resultJson.put("errorMsg", "params parse exception");
        }

        return resultJson;
    }

    /**
     * <br>
     *
     * @param grantParam
     * @return
     * @since NFVO 0.5
     */
    private JSONObject sendGrantToResmgr(JSONObject grantParam) {
        RestfulResponse rsp = VnfmRestfulUtil.getRemoteResponse(ParamConstants.GRANT_RES_URL, VnfmRestfulUtil.TYPE_PUT,
                grantParam.toString());
        if(rsp == null || rsp.getStatus() != Constant.HTTP_OK) {
            return null;
        }
        LOG.error("funtion=sendGrantToResmgr, status={}", rsp.getStatus());
        return JSONObject.fromObject(rsp.getResponseContent());
    }

    /**
     * <br>
     *
     * @param resMap
     * @param grantParam
     * @return
     * @since NFVO 0.5
     */
    private JSONObject parseGrantParam(Map<String, Integer> resMap, JSONObject grantParam) {
        JSONObject result = new JSONObject();
        result.put("vnfInstanceId", grantParam.getString("vnfId"));
        result.put("vimId", grantParam.getString("vimId"));

        JSONArray resource = new JSONArray();
        JSONObject resourceObj = new JSONObject();
        resourceObj.put("type", "vdu");
        JSONObject resourceTemplate = new JSONObject();
        JSONObject storage = new JSONObject();
        storage.put("sizeOfStorage", resMap.get("diskNum"));
        storage.put("typeOfStorage", "");
        storage.put("swImageDescriptor", "");
        JSONObject compute = new JSONObject();
        JSONObject virtualMemory = new JSONObject();
        virtualMemory.put("virtualMemSize", resMap.get("memNum"));
        JSONObject virtualCpu = new JSONObject();
        virtualCpu.put("numVirtualCpu", resMap.get("cpuNum"));
        compute.put("virtualMemory", virtualMemory);
        compute.put("virtualCpu", virtualCpu);
        resourceTemplate.put("virtualStorageDescriptor", storage);
        resourceTemplate.put("virtualComputeDescriptor", compute);
        resourceObj.put("resourceTemplate", resourceTemplate);
        resourceObj.put("resourceDefinitionId", "");
        resourceObj.put("vdu", grantParam.getString("vnfName"));
        resource.add(resourceObj);

        if("online".equals(grantParam.getString("action")) || "scaleOut".equals(grantParam.getString("action"))) {
            result.put("addResource", resource);
        } else {
            result.put("removeResource", resource);
        }

        JSONObject additionalParam = new JSONObject();
        additionalParam.put("vnfmId", grantParam.getString("vnfmId"));
        additionalParam.put("vimId", grantParam.getString("vimId"));
        additionalParam.put("tenant", "");
        result.put("additionalParam", additionalParam);
        LOG.info("funtion=parseGrantParam, result={}", result);
        return result;
    }

    private Map<String, Integer> calculateGrantRes(JSONArray vmList) {
        Map<String, Integer> resMap = new HashMap<>(Constant.DEFAULT_COLLECTION_SIZE);
        int vmSize = vmList.size();
        int cpuNum = 0;
        int memNum = 0;
        int diskNum = 0;
        int diskSize = 0;
        int cpuTmp = 0;
        int memTmp = 0;
        int diskTmp = 0;
        int initNum = 0;

        try {
            for(int i = 0; i < vmSize; i++) {
                JSONObject resInfo = vmList.getJSONObject(i);
                JSONObject vmFlavor = resInfo.getJSONObject("vm_flavor");
                initNum = Integer.parseInt(resInfo.getString("init_number"));

                if(initNum == 0) {
                    continue;
                }

                JSONArray volumList = vmFlavor.getJSONArray("storage");
                diskSize = volumList.size();

                for(int j = 0; j < diskSize; j++) {
                    JSONObject volumeInfo = volumList.getJSONObject(j);
                    diskTmp += getDiskQuantity(volumeInfo);
                }

                cpuTmp = Integer.parseInt(vmFlavor.getString("num_cpus"));
                memTmp = Integer.parseInt(vmFlavor.getString("mem_size"));

                cpuNum += cpuTmp * initNum;
                memNum += memTmp * initNum;
                diskNum += diskTmp * initNum;

                diskTmp = 0;

            }
        } catch(JSONException e) {
            LOG.error("function=calculateGrantRes, msg=parse params occoured JSONException e={}.", e);
            return null;
        }

        resMap.put("cpuNum", cpuNum);
        resMap.put("memNum", memNum);
        resMap.put("diskNum", diskNum);
        return resMap;
    }

    private String getGrantAction(String type, String requestType) {
        String action = "unknown";

        if(("increase").equals(requestType)) {
            if(("instantiation").equals(type)) {
                action = "online";
            } else if(("scale").equals(type)) {
                action = "scaleOut";
            }

        } else if(("decrease").equals(requestType)) {
            if(("instantiation").equals(type)) {
                action = "offline";
            } else if(("scale").equals(type)) {
                action = "scaleIn";
            }
        }

        return action;
    }

    private int getDiskQuantity(JSONObject volumeObj) {
        int disk = 0;
        if(volumeObj.containsKey("vol_type")) {
            if("local_volume".equals(volumeObj.getString("vol_type"))) {
                disk = Integer.parseInt(volumeObj.getString("vol_size"));
            }
        } else if(volumeObj.containsKey("storage_type") && "local_image".equals(volumeObj.getString("storage_type"))) {

            disk = Integer.parseInt(volumeObj.getString("disk_size"));

        }
        return disk;
    }
}
