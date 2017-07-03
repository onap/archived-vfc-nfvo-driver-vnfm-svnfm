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

package org.openo.nfvo.vnfmadapter.service.csm.vnf;

import net.sf.json.JSONArray;
import org.openo.nfvo.vnfmadapter.common.ResultRequestUtil;
import org.openo.nfvo.vnfmadapter.service.constant.Constant;
import org.openo.nfvo.vnfmadapter.service.constant.ParamConstants;
import org.openo.nfvo.vnfmadapter.service.csm.inf.InterfaceVnfMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import javax.print.attribute.standard.ReferenceUriSchemesSupported;

/**
 * create or terminate VNF to M
 * <br/>
 *
 * @author
 * @version NFVO 0.5 Aug 24, 2016
 */
public class VnfMgrVnfm implements InterfaceVnfMgr {

    private static final Logger LOG = LoggerFactory.getLogger(VnfMgrVnfm.class);

    @Override
    public JSONObject scaleVnf(JSONObject vnfObject, JSONObject vnfmObject, String vnfmId, String vnfInstanceId) {
        LOG.warn("function=scaleVnf, msg=enter to scale a vnf");
        JSONObject restJson = new JSONObject();
        restJson.put(Constant.RETCODE, Constant.REST_FAIL);
        String path = String.format(ParamConstants.VNF_SCALE, vnfInstanceId);

        int scaleType = getScaleType(vnfObject.getString("type"));
        //build request json object
        JSONObject paramJson = new JSONObject();
        JSONObject scaleInfo = new JSONObject();
        JSONArray vduList = new JSONArray();
        JSONObject vdu = new JSONObject();
        vdu.put("vdu_type",this.getVduType(vnfmObject,vnfInstanceId));//TODO:set vdu_type
        vdu.put("h_steps",vnfObject.get("numberOfSteps"));
        vduList.add(vdu);
        scaleInfo.put("vnf_id",vnfInstanceId);
        scaleInfo.put("scale_type",0);
        scaleInfo.put("scale_action",scaleType);
        scaleInfo.put("vdu_list",vduList);
        if(scaleType == 0){//scale_in
            JSONArray vmList = new JSONArray();
            try {
                JSONObject additionalParam = vnfObject.getJSONObject("additionalParam");
                vmList = additionalParam.getJSONArray("vm_list");
            }catch (JSONException e) {
              LOG.error("the param 'additionalParam' or 'vm_list' not found,please check it",e);
            }
            scaleInfo.put("vm_list",vmList);
        }
        paramJson.put("scale_info",scaleInfo);
        JSONObject queryResult = ResultRequestUtil.call(vnfmObject, path, Constant.PUT, paramJson.toString(),Constant.CERTIFICATE);
        LOG.info("SCALE execute result:"+queryResult.toString());
        try {
            int statusCode = queryResult.getInt(Constant.RETCODE);

            if(statusCode == Constant.HTTP_CREATED || statusCode == Constant.HTTP_OK) {
                restJson.put(Constant.RETCODE, Constant.REST_SUCCESS);
                JSONObject resultObj = new JSONObject();
                resultObj.put("jobId", vnfInstanceId + "_" + Constant.PUT);
                restJson.put("data", resultObj);
            } else {
                LOG.error("function=scaleVnf, msg=send create vnf msg to csm get wrong status: " + statusCode);
            }

        } catch(JSONException e) {
            LOG.error("function=scaleVnf, msg=parse scale vnf return data occoured JSONException, e={}.", e);
        }

        return restJson;
    }


    private String getVduType(JSONObject vnfmObject, String vnfInstanceId){
        String vduType = "";
        try {
            JSONObject queryResult = ResultRequestUtil.call(vnfmObject, String.format(ParamConstants.VNF_GET_VMINFO, vnfInstanceId), Constant.GET, null,Constant.CERTIFICATE);
            LOG.info("getVduType result="+queryResult);
            vduType = queryResult.getJSONObject("data").getJSONArray("vms").getJSONObject(0).getString("vdu_type");
        } catch (Exception e) {
            LOG.error("get vdu_type failed.",e);
        }
        LOG.info("vdu_type="+vduType);
        return vduType;
    }
    private int getScaleType(String type){
        if("SCALE_OUT".equalsIgnoreCase(type)){
            return 1;
        }else if("SCALE_IN".equalsIgnoreCase(type)){
            return 0;
        }
        return -1;
    }
    @Override
    public JSONObject createVnf(JSONObject subJsonObject, JSONObject vnfmObject) {
        LOG.info("function=createVnf, msg=enter to create a vnf");
        LOG.info("createVnf csm request body :"+subJsonObject);
        JSONObject restJson = new JSONObject();
        restJson.put(Constant.RETCODE, Constant.REST_FAIL);
        String path = ParamConstants.VNF_INSTANCE + Constant.ROARAND;

        JSONObject queryResult = ResultRequestUtil.call(vnfmObject, path, Constant.POST, subJsonObject.toString(),Constant.CERTIFICATE);
        LOG.info("createVnf csm response content:"+queryResult);
        try {
            int statusCode = queryResult.getInt(Constant.RETCODE);

            if(statusCode == Constant.HTTP_CREATED) {
                restJson.put(Constant.RETCODE, Constant.REST_SUCCESS);
                JSONObject appInfo = JSONObject.fromObject(queryResult.getString("data")).getJSONObject("app_info");
                JSONObject resultObj = new JSONObject();
                resultObj.put("vnfInstanceId", appInfo.getString("id"));
                resultObj.put("jobId", appInfo.getString("id") + "_" + Constant.POST);
                restJson.put("data", resultObj);
            } else {
                LOG.error("function=createVnf, msg=send create vnf msg to csm get wrong status: " + statusCode);
            }

        } catch(JSONException e) {
            LOG.error("function=createVnf, msg=parse create vnf return data occoured JSONException, e={}.", e);
        }

        return restJson;
    }

    @Override
    public JSONObject removeVnf(JSONObject vnfmObject, String vnfId, JSONObject vnfObject) {
        LOG.warn("function=removeVnf, msg=enter to remove a vnf: {}", vnfId);
        JSONObject restJson = new JSONObject();
        restJson.put(Constant.RETCODE, Constant.REST_FAIL);

        JSONObject queryResult = ResultRequestUtil.call(vnfmObject,
                String.format(ParamConstants.VNF_INSTANCE_DEL, vnfId) + Constant.ROARAND, Constant.DELETE, null,Constant.CERTIFICATE);

        int statusCode = queryResult.getInt(Constant.RETCODE);

        if(statusCode == Constant.HTTP_NOCONTENT) {
            restJson.put(Constant.RETCODE, Constant.REST_SUCCESS);
            JSONObject resultObj = new JSONObject();
            resultObj.put("jobId", vnfId + "_" + Constant.DELETE);
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
                String.format(ParamConstants.VNF_INSTANCE_GET, vnfId) + Constant.ROARAND + "&type=status", Constant.GET,
                null,Constant.CERTIFICATE);

        int statusCode = queryResult.getInt("retCode");

        if(statusCode == Constant.HTTP_OK || statusCode == Constant.HTTP_CREATED) {
            if(null == (queryResult.get("data"))) {
                LOG.warn("function=getVnf, msg=query is null {}", queryResult.get("data"));
                return restJson;
            }
            restJson.put(Constant.RETCODE, Constant.REST_SUCCESS);
            restJson.put("data", JSONObject.fromObject(queryResult.getString("data")).getJSONArray("basic"));
        } else {
            LOG.error("function=getVnf, msg=send get vnf msg to csm get wrong status: {}", statusCode);
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
                null,Constant.CERTIFICATE);

        int statusCode = queryResult.getInt("retCode");

        if(statusCode == Constant.HTTP_OK || statusCode == Constant.HTTP_CREATED) {

            if((queryResult.get("data")) == null) {
                LOG.warn("function=getJob, msg=query is null {}", queryResult.get("data"));
                return restJson;
            }
            restJson.put(Constant.RETCODE, Constant.REST_SUCCESS);
            restJson.put("data", JSONObject.fromObject(queryResult.getString("data")).getJSONArray("basic"));
        } else {
            LOG.error("function=getJob, msg=send get vnf msg to csm get wrong status: {}", statusCode);
        }

        return restJson;
    }
}
