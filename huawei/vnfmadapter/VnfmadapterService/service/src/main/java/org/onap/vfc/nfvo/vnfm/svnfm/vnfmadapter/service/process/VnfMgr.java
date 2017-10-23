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
     * @throws IOException
     * @throws InterruptedException
     * @since VFC 1.0
     */
    public JSONObject addVnf(JSONObject vnfObject, String vnfmId) throws IOException, InterruptedException {
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
            String vnfDescriptorId = vnfObject.getString("vnfDescriptorId");
            Map<String, String> conMap = new ConcurrentHashMap<>(Constant.DEFAULT_COLLECTION_SIZE);
            conMap.put("csarid", vnfObject.getString("vnfPackageId"));
            conMap.put("vnfmid", vnfmId);
            conMap.put("vnfDescriptorId", vnfDescriptorId);
            AdapterResourceManager adapterResourceManager = new AdapterResourceManager();
            JSONObject uploadPkg = adapterResourceManager.uploadVNFPackage(null, conMap);
            JSONObject resObjcet = adapterResourceManager.operateVnfm(vnfmId, uploadPkg.getJSONObject("csarTempObj"),
                    uploadPkg.getJSONObject("vnfpkg"), vnfDescriptorId);
            if(resObjcet.getInt(Constant.RETCODE) == Constant.REST_FAIL) {
                return restJson;
            }
            JSONObject csmBody = transferVnfBody(vnfObject, resObjcet, vnfmId);
            restJson = (new VnfMgrVnfm()).createVnf(csmBody, vnfmObjcet);
            if(vnfDescriptorId.contains("HSS")) {
                if(waitForCgpFinished(vnfmObjcet, restJson)) {
                    createChildVnf("HSS1", vnfmId, vnfmObjcet);
                    createChildVnf("HSS2", vnfmId, vnfmObjcet);
                    createChildVnf("HSS3", vnfmId, vnfmObjcet);
                } else {
                    restJson.put(Constant.ERRORMSG, "create VNF failed.");
                }
            }
            if(vnfDescriptorId.contains("PCRF")) {
                if(waitForCgpFinished(vnfmObjcet, restJson)) {
                    createChildVnf("PCRF1", vnfmId, vnfmObjcet);
                } else {
                    restJson.put(Constant.ERRORMSG, "create VNF failed.");
                }
            }
            if(vnfDescriptorId.contains("SBC")) {
                if(waitForCgpFinished(vnfmObjcet, restJson)) {
                    createChildVnf("PCRF1", vnfmId, vnfmObjcet);
                } else {
                    restJson.put(Constant.ERRORMSG, "create VNF failed.");
                }
            }
            saveVnfInfo(restJson, resObjcet);
        } catch(JSONException e) {
            LOG.error("function=addVnf, msg=JSONException occurs, e={}.", e);
        }

        return restJson;
    }

    private boolean waitForCgpFinished(JSONObject vnfmObjcet, JSONObject restJson) throws InterruptedException {
        JSONObject queryVnf = (new VnfMgrVnfm()).getJob(vnfmObjcet, restJson.getString(Constant.JOBID));
        LOG.info("queryVnf: {}", queryVnf);
        String status = queryVnf.getJSONArray("data").getJSONObject(0).getString(Constant.STATUS);
        if("Active".equals(status.trim())) {
            return true;
        } else if("Building".equals(status.trim())) {
            Thread.sleep(Constant.REPEAT_REG_TIME);
            waitForCgpFinished(vnfmObjcet, restJson);
        }
        return false;
    }

    private void createChildVnf(String vnfdId, String vnfmId, JSONObject vnfmObjcet) throws IOException {
        AdapterResourceManager adapterResourceManager = new AdapterResourceManager();
        JSONObject vfnPkgInfoFromFile = JSONObject.fromObject(AdapterResourceManager.readVfnPkgInfoFromJson());
        JSONObject csarTempObj = vfnPkgInfoFromFile.getJSONObject(vnfdId).getJSONObject("template");
        JSONObject upload = adapterResourceManager.operateVnfm(vnfmId, csarTempObj, new JSONObject(), "HSS1");
        LOG.info("createChildVnf upload: {}", upload);
        JSONObject vnfObject = new JSONObject();
        vnfObject.put("vnfInstanceName", csarTempObj.getString("name"));
        JSONObject requestBody = transferVnfBody(vnfObject, upload, vnfmId);
        LOG.info("createChildVnf requestBody: {}", requestBody);
        (new VnfMgrVnfm()).createVnf(requestBody, vnfmObjcet);
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
    public JSONObject getVnf(String vnfId, String vnfmId) {
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

            return restJson.getInt(Constant.RETCODE) == Constant.REST_FAIL ? restJson : getVnfBody(restJson);

        } catch(JSONException e) {
            LOG.error("function=getVnf, msg=JSONException occurs, e={}.", e);
            restJson.put(Constant.RETCODE, Constant.REST_FAIL);
        }
        return restJson;
    }

    private JSONObject getVnfBody(JSONObject restJson) {
        try {
            JSONObject vnfInfoJson = new JSONObject();
            JSONObject basicInfoJson = new JSONObject();

            JSONObject retJson = restJson.getJSONArray("data").getJSONObject(0);

            basicInfoJson.put("vnfInstanceId", retJson.getString("id"));
            basicInfoJson.put("vnfInstanceName", retJson.getString("vapp_name"));
            basicInfoJson.put("vnfInstanceDescription", "vFW");

            Vnfm vnfm = vnfmDao.getVnfmById(retJson.getString("id"));
            basicInfoJson.put(Constant.VNFDID, vnfm == null ? "" : vnfm.getVnfdId());
            basicInfoJson.put("vnfdPackageId", vnfm == null ? "" : vnfm.getVnfPackageId());
            basicInfoJson.put("version", vnfm == null ? "" : vnfm.getVersion());
            basicInfoJson.put("vnfProvider", "hw");
            basicInfoJson.put("vnfType", retJson.get("vapp_type"));
            basicInfoJson.put("vnfStatus", retJson.getString(Constant.STATUS));

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
}
