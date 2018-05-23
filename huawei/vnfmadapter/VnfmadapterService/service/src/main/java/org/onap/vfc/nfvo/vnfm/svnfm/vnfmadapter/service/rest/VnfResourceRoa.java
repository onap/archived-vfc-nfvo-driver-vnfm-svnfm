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

package org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.rest;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.VnfmJsonUtil;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.restclient.RestfulResponse;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.servicetoken.VnfmRestfulUtil;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.adapter.impl.AdapterResourceManager;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.constant.Constant;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.process.VnfResourceMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Provide interfaces of resource for VNFM.
 * <br/>
 *
 * @author
 * @version VFC 1.0 Aug 24, 2016
 */
@Path("/rest/vnfmmed/csm/v2/vapps")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class VnfResourceRoa {

    private static final Logger LOG = LoggerFactory.getLogger(VnfResourceRoa.class);

    private VnfResourceMgr vnfResourceMgr;

    public void setVnfResourceMgr(VnfResourceMgr vnfResourceMgr) {
        this.vnfResourceMgr = vnfResourceMgr;
    }

    /**
     * Provide function of grant resource.
     * <br/>
     *
     * @param context
     * @param vnfId
     * @return
     * @since VFC 1.0
     */
    @PUT
    @Path("/instances/{vnfId}/grant")
    public String grantVnfRes(@Context HttpServletRequest context, @PathParam("vnfId") String vnfId) {
        LOG.info("function=grantVnfRes, msg=enter to grant vnf resource.");
        JSONObject restJson = new JSONObject();
        restJson.put(Constant.RETCODE, Constant.REST_FAIL);

        JSONObject dataObject = VnfmJsonUtil.getJsonFromContexts(context);
        LOG.info("function=grantVnfRes, dataObject: {}", dataObject);
        if(null == dataObject) {
            LOG.error("function=grantVnfRes, msg=param error");
            restJson.put("data", "Params error");
            return restJson.toString();
        }

        JSONObject grantObj = dataObject.getJSONObject("grant");

        if(null == grantObj) {
            LOG.error("function=grantVnfRes, msg=param error");
            restJson.put("data", "Grant param error");
            return restJson.toString();
        }

        String vnfmId = grantObj.getString("project_id");

        JSONObject resultObj = vnfResourceMgr.grantVnfResource(grantObj, vnfId, vnfmId);
        LOG.info("grantVnfResource resultObj:", resultObj);
        JSONObject res = new JSONObject();
        res.put("msg", "grant success");
        return res.toString();
    }

    @PUT
    @Path("/lifecycle_changes_notification")
    public String notify(@Context HttpServletRequest context) throws IOException {
        LOG.info("function=notify, msg=enter to notify vnf resource");
        JSONObject dataObject = VnfmJsonUtil.getJsonFromContexts(context);
        LOG.info("function=notify, dataObject: {}", dataObject);
        callLcmNotify(dataObject);
        JSONObject restJson = new JSONObject();
        restJson.put(Constant.RETCODE, Constant.REST_SUCCESS);
        return restJson.toString();
    }

    private void callLcmNotify(JSONObject dataObject) throws IOException {
        String vnfPkgInfo = AdapterResourceManager.readVfnPkgInfoFromJson();
        JSONObject vnfpkgJson = JSONObject.fromObject(vnfPkgInfo);
        String vnfmId = vnfpkgJson.getString("vnfmid");
        String vimId = vnfpkgJson.getString("vimid");
        JSONArray affectedVnfc = new JSONArray();
        JSONArray vmList = dataObject.getJSONArray("vm_list");
        String changeType = "";
        String operation = "";
        if(1 == dataObject.getInt("event_type")) {
            changeType = "added";
            operation = "Instantiate";
        } else {
            changeType = "removed";
            operation = "Terminal";
        }
        String vnfInstanceId = dataObject.getString("vnf_id");
        for(int i = 0; i < vmList.size(); i++) {
            JSONObject vm = vmList.getJSONObject(i);
            LOG.info("function=callLcmNotify, vm: {}", vm);
            JSONObject affectedVm = new JSONObject();
            String vimVimId = vm.getString("vim_vm_id");
            affectedVm.put("vnfcInstanceId", vimVimId);
            affectedVm.put("changeType", changeType);
            affectedVm.put("vimid", vimId);
            affectedVm.put("vmid", vimVimId);
            affectedVm.put("vmname", vm.getString("vm_name"));
            affectedVm.put("vduid", vimVimId);
            LOG.info("function=callLcmNotify, affectedVm: {}", affectedVm);
            affectedVnfc.add(affectedVm);
        }
        JSONObject notification = new JSONObject();
        notification.put("status", dataObject.getString("vnf_status"));
        notification.put("vnfInstanceId", vnfInstanceId);
        notification.put("operation", operation);
        notification.put("affectedVnfc", affectedVnfc);
        LOG.info("function=callLcmNotify, notification: {}", notification);
        String url = "/api/nslcm/v1/ns/" + vnfmId + "/vnfs/" + vnfInstanceId + "/Notify";
        LOG.info("function=callLcmNotify, url: {}", url);
        RestfulResponse rsp =
                VnfmRestfulUtil.getRemoteResponse(url, VnfmRestfulUtil.TYPE_POST, notification.toString());
        if(rsp != null) {
            LOG.info("function=callLcmNotify, status: {}, content: {}", rsp.getStatus(), rsp.getResponseContent());
        }
    }
}
