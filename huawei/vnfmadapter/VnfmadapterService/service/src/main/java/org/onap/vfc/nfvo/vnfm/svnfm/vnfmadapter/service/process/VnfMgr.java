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

package org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.process;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.ResultRequestUtil;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.VnfmUtil;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.adapter.impl.AdapterResourceManager;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.constant.Constant;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.csm.vnf.VnfMgrVnfm;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.dao.inf.VnfmDao;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.entity.Vnfm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/**
 * Provide function for instantiate or terminate VNF
 * <br/>
 *
 * @author
 * @version VFC 1.0 Aug 24, 2016
 */
public class VnfMgr {

    private static final Logger LOG = LoggerFactory.getLogger(VnfMgr.class);

    private VnfmDao vnfmDao;

    public void setVnfmDao(VnfmDao vnfmDao) {
        this.vnfmDao = vnfmDao;
    }

    /**
     * Scale vnf
     * 
     * @param vnfObject
     *            {
     *            "vnfInstanceId":"5",
     *            "type":"SCALE_OUT",
     *            "aspectId":"101",
     *            "numberOfSteps":"1",
     *            "additionalParam":{}
     *            }
     * @param vnfmId
     * @param vnfInstanceId
     * @return
     */
    public JSONObject scaleVNF(JSONObject vnfObject, String vnfmId, String vnfInstanceId) {
        JSONObject restJson = new JSONObject();
        restJson.put(Constant.RETCODE, Constant.REST_FAIL);
        try {

            if(vnfObject.isNullObject() || vnfObject.isEmpty()) {
                return restJson;
            }

            JSONObject vnfmObjcet = VnfmUtil.getVnfmById(vnfmId);
            LOG.info("vnfm info:" + vnfmObjcet);
            if(vnfmObjcet.isNullObject()) {
                LOG.error("function=scaleVNF,can't find vnfm from db by vnfmId=" + vnfmId);
                return restJson;
            }
            restJson = (new VnfMgrVnfm()).scaleVnf(vnfObject, vnfmObjcet, vnfmId, vnfInstanceId);
        } catch(JSONException e) {
            LOG.error("function=scaleVNF, msg=JSONException occurs, e={}.", e);
        }

        return restJson;
    }

    /**
     * Provide function for instantiate VNF
     * <br/>
     *
     * @param vnfObject
     * @param vnfmId
     * @return
     * @since VFC 1.0
     */
    public JSONObject addVnf(JSONObject vnfObject, String vnfmId) {
        JSONObject restJson = new JSONObject();
        restJson.put(Constant.RETCODE, Constant.REST_FAIL);
        try {

            if(vnfObject.isNullObject() || vnfObject.isEmpty()) {
                return restJson;
            }

            JSONObject vnfmObjcet = VnfmUtil.getVnfmById(vnfmId);

            if(vnfmObjcet.isNullObject()) {
                return restJson;
            }

            Map<String, String> conMap = new ConcurrentHashMap<>(Constant.DEFAULT_COLLECTION_SIZE);
            conMap.put("csarid", vnfObject.getString("vnfPackageId"));
            conMap.put("vnfmid", vnfmId);
            conMap.put("vnfDescriptorId", vnfObject.getString("vnfDescriptorId"));

            JSONObject resObjcet = (new AdapterResourceManager()).uploadVNFPackage(null, conMap);

            if(resObjcet.getInt(Constant.RETCODE) == Constant.REST_FAIL) {
                return restJson;
            }

            JSONObject csmBody = transferVnfBody(vnfObject, resObjcet, vnfmId);
            restJson = (new VnfMgrVnfm()).createVnf(csmBody, vnfmObjcet);
            saveVnfInfo(restJson, resObjcet);
        } catch(JSONException e) {
            LOG.error("function=addVnf, msg=JSONException occurs, e={}.", e);
        }

        return restJson;
    }

    /**
     * Provide function for terminate VNF
     * <br/>
     *
     * @param vnfId
     * @param vnfmId
     * @param vnfObject
     * @return
     * @since VFC 1.0
     */
    public JSONObject deleteVnf(String vnfId, String vnfmId, JSONObject vnfObject) {
        LOG.warn("function=deleteVnf ,msg=enter to delete a vnf, vnfId:{}, vnfmId:{}", vnfId, vnfmId);
        JSONObject restJson = new JSONObject();
        restJson.put(Constant.RETCODE, Constant.REST_FAIL);
        try {
            JSONObject vnfmObjcet = VnfmUtil.getVnfmById(vnfmId);
            if(vnfmObjcet.isNullObject()) {
                LOG.error("function=deleteVnf, msg=vnfm not exists, vnfmId: {}", vnfmId);
                return restJson;
            }

            restJson = (new VnfMgrVnfm()).removeVnf(vnfmObjcet, vnfId, vnfObject);
        } catch(JSONException e) {
            LOG.error("function=deleteVnf, msg=JSONException occurs, e={}.", e);
        }
        return restJson;
    }

    /**
     * Provide function for get VNF
     * <br/>
     *
     * @param vnfId
     * @param vnfmId
     * @return
     * @since VFC 1.0
     */
    public JSONObject getVnf(String vnfId, String vnfmId) throws IOException {
        LOG.warn("function=getVnf ,msg=enter to get a vnf, vnfId:{}, vnfmId:{}", vnfId, vnfmId);
        JSONObject restJson = new JSONObject();
        restJson.put(Constant.RETCODE, Constant.REST_FAIL);
        try {
            JSONObject vnfmObjcet = VnfmUtil.getVnfmById(vnfmId);
            if(vnfmObjcet.isNullObject()) {
                LOG.error("function=getVnf, msg=vnfm not exists, vnfmId: {}", vnfmId);
                return restJson;
            }

            restJson = (new VnfMgrVnfm()).getVnf(vnfmObjcet, vnfId);
            JSONObject ipObj = (new VnfMgrVnfm()).getIp(vnfmObjcet, vnfId);

            return restJson.getInt(Constant.RETCODE) == Constant.REST_FAIL ? restJson : getVnfBody(restJson, ipObj);

        } catch(JSONException e) {
            LOG.error("function=getVnf, msg=JSONException occurs, e={}.", e);
            restJson.put(Constant.RETCODE, Constant.REST_FAIL);
        }
        return restJson;
    }

    private JSONObject getVnfBody(JSONObject restJson, JSONObject ipObj) {
        try {
            JSONObject vnfInfoJson = new JSONObject();
            JSONObject basicInfoJson = new JSONObject();

            JSONObject retJson = restJson.getJSONArray("data").getJSONObject(0);

            basicInfoJson.put("vnfInstanceId", retJson.getString("vnf_id"));
            basicInfoJson.put("vnfInstanceName", retJson.getString("vnf_name"));
            basicInfoJson.put("vnfInstanceDescription", "");

            basicInfoJson.put(Constant.VNFDID, retJson.getString("vnfd_id"));
            basicInfoJson.put("vnfdPackageId", retJson.getString("vnfd_id"));
            basicInfoJson.put("version", "1.0");
            basicInfoJson.put("vnfProvider", "hw");
            basicInfoJson.put("vnfType", retJson.get("vnf_type"));
            basicInfoJson.put("vnfStatus", retJson.getString(Constant.STATUS));
            if(ipObj.getInt(Constant.RETCODE) == Constant.REST_SUCCESS) {
                basicInfoJson.put("ipInfo", ipObj.getJSONObject("data"));
            }
            vnfInfoJson.put("vnfInfo", basicInfoJson);
            vnfInfoJson.put(Constant.RETCODE, Constant.REST_SUCCESS);
            return vnfInfoJson;
        } catch(JSONException e) {
            LOG.error("function=getVnf, msg=JSONException occurs, e={}.", e);
            restJson.put(Constant.RETCODE, Constant.REST_FAIL);
            return restJson;
        } catch(IndexOutOfBoundsException e) {
            LOG.error("function=getVnf, msg=IndexOutOfBoundsException occurs, e={}.", e);
            restJson.put(Constant.RETCODE, Constant.REST_FAIL);
            return restJson;
        }
    }

    @SuppressWarnings("unchecked")
    private JSONObject transferVnfBody(JSONObject vnfObject, JSONObject resObject, String vnfmId) {
        JSONObject restJson = new JSONObject();
        JSONObject vappIfno = new JSONObject();
        restJson.put("vnfd_id", resObject.getString(Constant.VNFDID));
        restJson.put("plan_id", resObject.getOrDefault("planId", ""));
        restJson.put("plan_name", resObject.getOrDefault("planName", ""));
        restJson.put("vapp_name", vnfObject.get("vnfInstanceName"));
        restJson.put("project_id", vnfmId);
        restJson.put("parameters", resObject.getJSONObject("parameters"));
        if(resObject.containsKey("emsUuid")) {
            restJson.put("emsUuid", resObject.getString("emsUuid"));
        }
        restJson.put("nfvo_id", "");
        restJson.put("location", "");
        restJson.put("vnfm_id", vnfmId);
        vappIfno.put("vapp_info", restJson);
        return vappIfno;
    }

    /**
     * Provide function for get job
     * <br/>
     *
     * @param jobId
     * @param vnfmId
     * @return
     * @since VFC 1.0
     */
    public JSONObject getJob(String jobId, String vnfmId) {
        LOG.warn("function=getJob ,msg=enter to get a job, vnfId:{}", jobId);
        JSONObject restJson = new JSONObject();
        restJson.put(Constant.RETCODE, Constant.REST_FAIL);
        try {
            JSONObject vnfmObjcet = VnfmUtil.getVnfmById(vnfmId);
            if(vnfmObjcet.isNullObject()) {
                LOG.error("function=getJob, msg=vnfm not exists, vnfmId: {}", vnfmId);
                return restJson;
            }

            restJson = (new VnfMgrVnfm()).getJob(vnfmObjcet, jobId);

        } catch(JSONException e) {
            LOG.error("function=getJob, msg=JSONException occurs, e={}.", e);
            restJson.put(Constant.RETCODE, Constant.REST_FAIL);
        }
        return restJson;
    }

    /**
     * Provide function for save vnfInfo
     * <br/>
     *
     * @param vnfObject
     * @param resObject
     * @since VFC 1.0
     */
    public void saveVnfInfo(JSONObject vnfObject, JSONObject resObject) {
        LOG.warn("function=saveVnfInfo , vnfObject:{}", vnfObject);
        if(vnfObject.getInt(Constant.RETCODE) == Constant.REST_SUCCESS) {
            Vnfm info = new Vnfm();
            info.setId(vnfObject.getJSONObject("data").getString("vnfInstanceId"));
            info.setVersion(resObject.getString("vnfdVersion"));
            info.setVnfdId(resObject.getString(Constant.VNFDID));
            info.setVnfPackageId("");
            try {
                vnfmDao.insertVnfm(info);
            } catch(Exception e) {
                LOG.error("function=saveVnfInfo, msg=ServiceException occurs, e={}.", e);
            }
        }
    }

    /**
     * <br>
     * 
     * @param jsonObject
     * @param vnfInstanceId
     * @param vnfmId
     * @return
     * @since VFC 1.0
     */
    public JSONObject healVnf(JSONObject jsonObject, String vnfInstanceId, String vnfmId) {
        JSONObject restJson = new JSONObject();
        restJson.put(Constant.RETCODE, Constant.REST_FAIL);

        if(jsonObject.isNullObject() || jsonObject.isEmpty()) {
            return restJson;
        }

        JSONObject vnfmObjcet = VnfmUtil.getVnfmById(vnfmId);
        LOG.info("vnfm info:" + vnfmObjcet);
        if(vnfmObjcet.isNullObject()) {
            LOG.error("function=scaleVNF,can't find vnfm from db by vnfmId=" + vnfmId);
            return restJson;
        }
        restJson = (new VnfMgrVnfm()).healVnf(jsonObject, vnfmObjcet, vnfmId, vnfInstanceId);
        return restJson;
    }

    public JSONObject getJobFromVnfm(String jobId, String vnfmId) {
        LOG.warn("function=getJobFromVnfm, jobId:{}, vnfmId:{}", jobId, vnfmId);
        JSONObject restJson = new JSONObject();
        JSONObject vnfmObjcet = VnfmUtil.getVnfmById(vnfmId);
        if(vnfmObjcet.isNullObject()) {
            LOG.error("function=getJobFromVnfm, msg=vnfm not exists, vnfmId: {}", vnfmId);
            return restJson;
        }
        restJson = (new VnfMgrVnfm()).getJobFromVnfm(vnfmObjcet, jobId);
        return restJson;
    }

    public String transferToLcm(JSONObject restJson) {
        LOG.warn("function=transferToLcm, restJson: {}", restJson);
        JSONObject responseJson = new JSONObject();
        JSONObject jobInfoJson = new JSONObject();
        JSONObject jobInfo = restJson.getJSONObject("data").getJSONObject("job_info");
        jobInfoJson.put("jobId", jobInfo.getString("job_id"));
        responseJson.put("progress", jobInfo.getString("task_progress_rate"));
        responseJson.put("status", jobInfo.getString("task_status"));
        responseJson.put("errorCode", jobInfo.getString("error_code"));
        responseJson.put("responseId", jobInfo.getString("task_progress_rate"));
        jobInfoJson.put("responsedescriptor", responseJson);
        LOG.warn("function=getJobBody, jobInfoJson: {}", jobInfoJson);
        return jobInfoJson.toString();
    }

    public JSONObject getVmsFromVnfm(String vnfmId, String vnfInstanceId) {
        JSONObject restJson = new JSONObject();
        JSONObject vnfmObjcet = VnfmUtil.getVnfmById(vnfmId);
        if(vnfmObjcet.isNullObject()) {
            LOG.error("function=getVmsFromVnfm, msg=vnfm not exists, vnfmId: {}", vnfmId);
            restJson.put("message", "vnfm not exists");
            return restJson;
        }
        String url = "";
        if(vnfInstanceId == null) {
            url = "/v2/vapps/instances/query/vms";
        } else {
            url = String.format("/v2/vapps/instances/%s/vm", vnfInstanceId);
        }
        restJson = ResultRequestUtil.call(vnfmObjcet, url, Constant.GET, null, Constant.CERTIFICATE);
        LOG.info("function=getVmsFromVnfm, restJson: {}", restJson);
        return restJson;
    }

}
