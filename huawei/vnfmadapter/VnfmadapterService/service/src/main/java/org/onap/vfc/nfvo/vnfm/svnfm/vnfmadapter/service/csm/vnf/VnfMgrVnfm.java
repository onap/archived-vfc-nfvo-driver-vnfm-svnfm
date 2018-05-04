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

package org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.csm.vnf;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.ResultRequestUtil;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.constant.Constant;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.constant.ParamConstants;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.constant.UrlConstant;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.csm.inf.InterfaceVnfMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/**
 * create or terminate VNF to M
 * <br/>
 *
 * @author
 * @version VFC 1.0 Aug 24, 2016
 */
public class VnfMgrVnfm implements InterfaceVnfMgr {

    private static final Logger LOG = LoggerFactory.getLogger(VnfMgrVnfm.class);

    private static final int PARAM_ZERO = 0;

    private static final int PARAM_ONE = 1;

    @Override
    public JSONObject scaleVnf(JSONObject vnfObject, JSONObject vnfmObject, String vnfmId, String vnfInstanceId) {
        LOG.warn("function=scaleVnf, msg=enter to scale a vnf");
        JSONObject restJson = new JSONObject();
        restJson.put(Constant.RETCODE, Constant.REST_FAIL);
        String path = String.format(ParamConstants.VNF_SCALE, vnfInstanceId);

        int scaleType = getScaleType(vnfObject.getString("type"));
        // build request json object
        JSONObject paramJson = new JSONObject();
        JSONObject scaleInfo = new JSONObject();
        JSONArray vduList = new JSONArray();
        JSONObject vdu = new JSONObject();
        vdu.put("vdu_type", this.getVduType(vnfmObject, vnfInstanceId));
        vdu.put("h_steps", vnfObject.get("numberOfSteps"));
        vduList.add(vdu);
        scaleInfo.put("vnf_id", vnfInstanceId);
        scaleInfo.put("scale_pattern", "without_plan");
        scaleInfo.put("scale_type", PARAM_ZERO);
        scaleInfo.put("scale_action", scaleType);
        scaleInfo.put("scale_step", PARAM_ZERO);
        scaleInfo.put("scale_step_value", PARAM_ONE);
        scaleInfo.put("scale_group", vdu.getString("vdu_type"));
        scaleInfo.put("vdu_list", vduList);
        if(scaleType == PARAM_ZERO) {
            // scale_in
            JSONArray vmList = new JSONArray();
            try {
                JSONObject additionalParam = vnfObject.getJSONObject("additionalParam");
                vmList = additionalParam.getJSONArray("vm_list");
            } catch(JSONException e) {
                LOG.error("the param 'additionalParam' or 'vm_list' not found,please check it", e);
            }
            scaleInfo.put("vm_list", vmList);
        }
        paramJson.put("scale_info", scaleInfo);
        JSONObject queryResult =
                ResultRequestUtil.call(vnfmObject, path, Constant.PUT, paramJson.toString(), Constant.CERTIFICATE);
        LOG.info("SCALE execute result:" + queryResult.toString());
        try {
            int statusCode = queryResult.getInt(Constant.RETCODE);

            if(statusCode == Constant.HTTP_CREATED || statusCode == Constant.HTTP_OK) {
                restJson.put(Constant.RETCODE, Constant.REST_SUCCESS);
                // restJson.put("data",
                // queryResult.getJSONObject("data").getJSONObject("scale_info"));
                JSONObject appInfo = queryResult.getJSONObject("data").getJSONObject("scale_info");
                JSONObject resultObj = new JSONObject();
                // resultObj.put(Constant.JOBID, vnfInstanceId + "_" + Constant.PUT);
                handleResponse(resultObj, appInfo, vnfInstanceId, Constant.PUT);
                restJson.put("data", resultObj);
            } else {
                LOG.error("function=scaleVnf, msg=send create vnf msg to csm get wrong status: " + statusCode);
            }

        } catch(JSONException e) {
            LOG.error("function=scaleVnf, msg=parse scale vnf return data occoured JSONException, e={}.", e);
        }

        return restJson;
    }

    private String getVduType(JSONObject vnfmObject, String vnfInstanceId) {
        String vduType = "";
        try {
            JSONObject queryResult =
                    ResultRequestUtil.call(vnfmObject, String.format(ParamConstants.VNF_GET_VMINFO, vnfInstanceId),
                            Constant.GET, null, Constant.CERTIFICATE);
            LOG.info("getVduType result=" + queryResult);
            vduType = queryResult.getJSONObject("data").getJSONArray("vms").getJSONObject(0).getString("vdu_type");
        } catch(Exception e) {
            LOG.error("get vdu_type failed.", e);
        }
        LOG.info("vdu_type=" + vduType);
        return vduType;
    }

    private int getScaleType(String type) {
        if("SCALE_OUT".equalsIgnoreCase(type)) {
            return 1;
        } else if("SCALE_IN".equalsIgnoreCase(type)) {
            return 0;
        }
        return -1;
    }

    @Override
    public JSONObject createVnf(JSONObject subJsonObject, JSONObject vnfmObject) {
        LOG.info("function=createVnf, msg=enter to create a vnf");
        LOG.info("createVnf csm request body :" + subJsonObject);
        JSONObject restJson = new JSONObject();
        restJson.put(Constant.RETCODE, Constant.REST_FAIL);
        String path = ParamConstants.VNF_INSTANCE + Constant.ROARAND;

        JSONObject queryResult =
                ResultRequestUtil.call(vnfmObject, path, Constant.POST, subJsonObject.toString(), Constant.CERTIFICATE);
        LOG.info("createVnf csm response content:" + queryResult);
        try {
            int statusCode = queryResult.getInt(Constant.RETCODE);

            if(statusCode == Constant.HTTP_CREATED) {
                restJson.put(Constant.RETCODE, Constant.REST_SUCCESS);
                JSONObject appInfo = JSONObject.fromObject(queryResult.getString("data")).getJSONObject("app_info");
                JSONObject resultObj = new JSONObject();
                // resultObj.put("vnfInstanceId", appInfo.getString("id"));
                // resultObj.put(Constant.JOBID, appInfo.getString("id") + "_" + Constant.POST);
                String vnfInstanceId = appInfo.getString("id");
                handleResponse(resultObj, appInfo, vnfInstanceId, Constant.POST);
                restJson.put("data", resultObj);
            } else {
                LOG.error("function=createVnf, msg=send create vnf msg to csm get wrong status: " + statusCode);
            }

        } catch(JSONException e) {
            LOG.error("function=createVnf, msg=parse create vnf return data occoured JSONException, e={}.", e);
        }

        return restJson;
    }

    private void handleResponse(JSONObject result, JSONObject returnObj, String vnfInstanceId, String type) {
        String jobId = "";
        if(returnObj.containsKey("job_id")) {
            jobId = returnObj.getString("job_id") + ":job";
        } else {
            jobId = vnfInstanceId + "_" + type + ":no";
        }
        result.put("vnfInstanceId", vnfInstanceId);
        result.put(Constant.JOBID, jobId);
    }

    @Override
    public JSONObject removeVnf(JSONObject vnfmObject, String vnfId, JSONObject vnfObject) {
        LOG.warn("function=removeVnf, msg=enter to remove a vnf: {}", vnfId);
        JSONObject restJson = new JSONObject();
        restJson.put(Constant.RETCODE, Constant.REST_FAIL);

        JSONObject queryResult = ResultRequestUtil.call(vnfmObject,
                String.format(ParamConstants.VNF_INSTANCE_DEL, vnfId) + Constant.ROARAND, Constant.DELETE, null,
                Constant.CERTIFICATE);

        int statusCode = queryResult.getInt(Constant.RETCODE);

        if(statusCode == Constant.HTTP_NOCONTENT || statusCode == Constant.HTTP_OK) {
            restJson.put(Constant.RETCODE, Constant.REST_SUCCESS);
            // restJson.put("data", JSONObject.fromObject(queryResult.getString("data")));
            JSONObject appInfo = new JSONObject();
            if(queryResult.containsKey("data") && StringUtils.isNotEmpty(queryResult.getString("data")))
            {
                appInfo = JSONObject.fromObject(queryResult.getString("data"));
            }
            JSONObject resultObj = new JSONObject();
            // resultObj.put(Constant.JOBID, vnfId + "_" + Constant.DELETE);
            handleResponse(resultObj, appInfo, vnfId, Constant.DELETE);
            restJson.put("data", resultObj);
        } else {
            LOG.error("function=removeVnf, msg=send remove vnf msg to csm get wrong status: {}", statusCode);
        }

        return restJson;
    }

    @Override
    public JSONObject getVnf(JSONObject vnfmObject, String vnfId) {
        LOG.warn("function=getVnf, msg=enter to get a vnf: {}", vnfId);
        JSONObject restJson = new JSONObject();
        restJson.put(Constant.RETCODE, Constant.REST_FAIL);

        JSONObject queryResult = ResultRequestUtil.call(vnfmObject,
                String.format(ParamConstants.VNF_INSTANCE_GET, vnfId), Constant.GET, null, Constant.CERTIFICATE);

        int statusCode = queryResult.getInt(Constant.RETCODE);

        if(statusCode == Constant.HTTP_OK || statusCode == Constant.HTTP_CREATED) {
            if(null == (queryResult.get("data"))) {
                LOG.warn("function=getVnf, msg=query is null {}", queryResult.get("data"));
                return restJson;
            }
            restJson.put(Constant.RETCODE, Constant.REST_SUCCESS);
            restJson.put("data", JSONObject.fromObject(queryResult.getString("data")).getJSONArray("vnf_list"));
        } else {
            LOG.error("function=getVnf, msg=send get vnf msg to csm get wrong status: {}", statusCode);
        }

        return restJson;
    }

    public JSONObject getIp(JSONObject vnfmObject, String vnfId) throws IOException {
        LOG.warn("function=getIp, msg=enter to getIp: {}", vnfId);
        JSONObject restJson = new JSONObject();
        restJson.put(Constant.RETCODE, Constant.REST_FAIL);

        JSONObject queryResult = ResultRequestUtil.call(vnfmObject,
                String.format(ParamConstants.VNF_CONFIGURATION_GET, vnfId), Constant.GET, null, Constant.CERTIFICATE);

        int statusCode = queryResult.getInt(Constant.RETCODE);

        if(statusCode == Constant.HTTP_OK || statusCode == Constant.HTTP_CREATED) {
            if(null == (queryResult.get("data"))) {
                LOG.warn("function=getIp, msg=query is null {}", queryResult.get("data"));
                return restJson;
            }
            JSONObject config = JSONObject.fromObject(queryResult.getString("data"));
            LOG.info("function=getIp, query configuration result: {}", config);
            JSONObject vnfInfo = config.getJSONArray("configuration").getJSONObject(0);
            JSONObject result = new JSONObject();
            result.put("vnf_id", vnfInfo.getString("vnf_id"));
            result.put("vnf_type", vnfInfo.getString("vnf_type"));
            JSONArray inputs = vnfInfo.getJSONArray("inputs");

            ClassLoader classLoader = getClass().getClassLoader();
            String ipConfig = IOUtils.toString(classLoader.getResourceAsStream("ipConfig.json"));
            LOG.info("ipConfig: {}", ipConfig);
            JSONObject ipCon = JSONObject.fromObject(ipConfig);
            String vnfType = vnfInfo.getString("vnf_type");
            if(ipCon.containsKey(vnfType)) {
                String ipKey = ipCon.getString(vnfInfo.getString("vnf_type"));
                LOG.info("ipKey: {}", ipKey);
                String ip = "";
                for(int i = 0; i < inputs.size(); i++) {
                    JSONObject obj = inputs.getJSONObject(i);
                    if(obj.getString("key_name").equals(ipKey)) {
                        ip = obj.getString("value");
                        break;
                    }
                }
                result.put("ip", ip);
                restJson.put(Constant.RETCODE, Constant.REST_SUCCESS);
                restJson.put("data", result);
            }

        } else {
            LOG.error("function=getIp, msg=send get vnf msg to csm get wrong status: {}", statusCode);
        }

        return restJson;
    }

    @Override
    public JSONObject getJob(JSONObject vnfmObject, String jobId) {
        LOG.warn("function=getJob, msg=enter to get a job: {}", jobId);
        JSONObject restJson = new JSONObject();
        restJson.put(Constant.RETCODE, Constant.REST_FAIL);

        String vnfId = jobId.split("_")[0];
        JSONObject queryResult = ResultRequestUtil.call(vnfmObject,
                String.format(ParamConstants.VNF_INSTANCE_GET, vnfId) + Constant.ROARAND + "&type=status", Constant.GET,
                null, Constant.CERTIFICATE);

        int statusCode = queryResult.getInt(Constant.RETCODE);

        if(statusCode == Constant.HTTP_OK || statusCode == Constant.HTTP_CREATED) {

            if((queryResult.get("data")) == null) {
                LOG.warn("function=getJob, msg=query is null {}", queryResult.get("data"));
                return restJson;
            }
            restJson.put(Constant.RETCODE, Constant.REST_SUCCESS);
            restJson.put("data", JSONObject.fromObject(queryResult.getString("data")).getJSONArray("vnf_list"));
        } else {
            LOG.error("function=getJob, msg=send get vnf msg to csm get wrong status: {}", statusCode);
        }

        return restJson;
    }

    /**
     * <br>
     * 
     * @param jsonObject
     * @param vnfmObjcet
     * @param vnfmId
     * @param vnfInstanceId
     * @return
     * @since VFC 1.0
     */
    public JSONObject healVnf(JSONObject jsonObject, JSONObject vnfmObjcet, String vnfmId, String vnfInstanceId) {
        LOG.info("healVnf request body :" + jsonObject);
        JSONObject restJson = new JSONObject();
        restJson.put(Constant.RETCODE, Constant.REST_FAIL);

        String action = jsonObject.getString("action");
        JSONObject affectedVm = jsonObject.getJSONObject("affectedvm");
        String vmId = affectedVm.getString("vmid");
        String path = String.format(ParamConstants.HEAL_VNF, vmId);

        JSONObject subJsonObject = new JSONObject();
        subJsonObject.put("type", "hard");
        subJsonObject.put("boot_mode", "");
        if("vmReset".equals(action)) {
            subJsonObject.put("action", "reset");
        }
        LOG.info("healVnf subJsonObject :" + subJsonObject);
        JSONObject healResult = ResultRequestUtil.callSouth(vnfmObjcet, path, Constant.PUT, subJsonObject.toString(),
                Constant.CERTIFICATE);

        int statusCode = healResult.getInt(Constant.RETCODE);
        if(statusCode == Constant.HTTP_OK) {
            LOG.info("healResult:{}", healResult);
            restJson.put(Constant.RETCODE, Constant.REST_SUCCESS);
        } else {
            LOG.error("function=healVnf, msg=send heal vnf msg to csm get wrong status: {}", statusCode);
        }

        return restJson;
    }

    public JSONObject getJobFromVnfm(JSONObject vnfmObjcet, String jobId) {
        LOG.warn("function=getJobFromVnfm, jobId: {}", jobId);

        JSONObject restJson = new JSONObject();
        restJson.put(Constant.RETCODE, Constant.REST_FAIL);

        JSONObject queryResult = ResultRequestUtil.call(vnfmObjcet, String.format(UrlConstant.URL_JOBSTATUS_GET, jobId),
                Constant.GET, null, Constant.CERTIFICATE);

        int statusCode = queryResult.getInt(Constant.RETCODE);
        if(statusCode == Constant.HTTP_OK || statusCode == Constant.HTTP_CREATED) {
            if((queryResult.get("data")) == null) {
                LOG.warn("function=getJobFromVnfm, msg=query is null {}", queryResult.get("data"));
                return restJson;
            }
            restJson.put(Constant.RETCODE, Constant.REST_SUCCESS);
            restJson.put("data", JSONObject.fromObject(queryResult.getString("data")));
        } else {
            LOG.error("function=getJobFromVnfm, msg=query job from vnfm wrong status: {}", statusCode);
        }

        return restJson;
    }

}
