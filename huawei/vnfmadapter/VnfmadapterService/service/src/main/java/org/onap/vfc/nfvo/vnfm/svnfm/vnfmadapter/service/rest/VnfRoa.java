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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.collections.map.UnmodifiableMap;
import org.apache.commons.lang3.StringUtils;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.VnfmJsonUtil;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.VnfmUtil;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.restclient.ServiceException;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.constant.Constant;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.process.VnfMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONObject;

/**
 * Provide interfaces for instantiate or terminate VNF.
 * <br/>
 *
 * @author
 * @version VFC 1.0 Aug 24, 2016
 */
@SuppressWarnings("unchecked")
@Path("/api/huaweivnfmdriver/v1")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class VnfRoa {

    private static final Logger LOG = LoggerFactory.getLogger(VnfRoa.class);

    private VnfMgr vnfMgr;

    private static Map<String, String> progressItem;

    private static Map<String, String> jobstatusItem;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("Building", "50");
        map.put("Active", "100");
        map.put("Stopped", "50");
        map.put("Error", "100");
        progressItem = UnmodifiableMap.decorate(map);

        map = new HashMap<>();
        map.put("Building", "processing");
        map.put("Active", "finished");
        map.put("Stopped", "processing");
        map.put("Error", "error");
        jobstatusItem = UnmodifiableMap.decorate(map);
    }

    public void setVnfMgr(VnfMgr vnfMgr) {
        this.vnfMgr = vnfMgr;
    }

    /**
     * Scale VNF
     * 
     * @param context
     *            * {
     *            "vnfInstanceId":"5",
     *            "type":"SCALE_OUT",
     *            "aspectId":"101",
     *            "numberOfSteps":"1",
     *            "additionalParam":{}
     *            }
     * @param resp
     * @param vnfmId
     * @return
     *         {
     *         "jobId":"1"
     *         }
     * @throws ServiceException
     */
    @POST
    @Path("/{vnfmId}/vnfs/{vnfInstanceId}/scale")
    public String scaleVnf(@Context HttpServletRequest context, @Context HttpServletResponse resp,
            @PathParam("vnfmId") String vnfmId, @PathParam("vnfInstanceId") String vnfInstanceId)
            throws ServiceException {
        JSONObject jsonObject = VnfmJsonUtil.getJsonFromContexts(context);
        LOG.info("function=scaleVNF, msg=enter to scale a vnf. request body:" + jsonObject);
        JSONObject result = new JSONObject();
        if(null == jsonObject) {
            String msg = "the parameters do not meet the requirements,please check it!";
            LOG.error("function=scalVnf," + msg);
            resp.setStatus(Constant.HTTP_NOT_ACCEPTABLE);
            result.put("msg", msg);
            return result.toString();
        }

        result = vnfMgr.scaleVNF(jsonObject, vnfmId, vnfInstanceId);
        LOG.info("function=scaleVNF,result=" + result.toString());
        if(result.getInt(Constant.RETCODE) == Constant.REST_FAIL) {
            LOG.error("function=scaleVNF, msg=scaleVnf fail");
            resp.setStatus(Constant.HTTP_INNERERROR);
            return result.toString();
        }
        return JSONObject.fromObject(result.getJSONObject("data")).toString();
    }

    /**
     * Provide function for instantiate VNF
     * <br/>
     *
     * @param context
     * @param resp
     * @param vnfmId
     * @return
     * @throws ServiceException
     * @since VFC 1.0
     */
    @POST
    @Path("/{vnfmId}/vnfs")
    public String addVnf(@Context HttpServletRequest context, @Context HttpServletResponse resp,
            @PathParam("vnfmId") String vnfmId) throws ServiceException {
        LOG.warn("function=addVnf, msg=enter to add a vnf");
        JSONObject subJsonObject = VnfmJsonUtil.getJsonFromContexts(context);
        JSONObject restJson = new JSONObject();

        if(null == subJsonObject) {
            LOG.error("function=addVnf, msg=params are insufficient");
            resp.setStatus(Constant.HTTP_INNERERROR);
            return restJson.toString();
        }
        LOG.info("addVnf request info from (LCM):" + subJsonObject);
        restJson = vnfMgr.addVnf(subJsonObject, vnfmId);

        if(restJson.getInt(Constant.RETCODE) == Constant.REST_FAIL) {
            LOG.error("function=addVnf, msg=addvnf fail");
            resp.setStatus(Constant.HTTP_INNERERROR);
            return restJson.toString();
        }

        return JSONObject.fromObject(restJson.getJSONObject("data")).toString();
    }

    /**
     * Provide function for terminate VNF
     * <br/>
     *
     * @param vnfmId
     * @param resp
     * @param vnfInstanceId
     * @param context
     * @return
     * @throws ServiceException
     * @since VFC 1.0
     */
    @POST
    @Path("/{vnfmId}/vnfs/{vnfInstanceId}/terminate")
    public String delVnf(@PathParam("vnfmId") String vnfmId, @Context HttpServletResponse resp,
            @PathParam("vnfInstanceId") String vnfInstanceId, @Context HttpServletRequest context)
            throws ServiceException {
        LOG.warn("function=delVnf, msg=enter to delete a vnf: vnfInstanceId: {}, vnfmId: {}", vnfInstanceId, vnfmId);
        JSONObject vnfObject = VnfmJsonUtil.getJsonFromContexts(context);
        JSONObject restJson = new JSONObject();

        if(StringUtils.isEmpty(vnfInstanceId) || StringUtils.isEmpty(vnfmId)) {
            resp.setStatus(Constant.HTTP_INNERERROR);
            return restJson.toString();
        }

        restJson = vnfMgr.deleteVnf(vnfInstanceId, vnfmId, vnfObject);
        if(restJson.getInt(Constant.RETCODE) == Constant.REST_FAIL) {
            LOG.error("function=delVnf, msg=delVnf fail");
            resp.setStatus(Constant.HTTP_INNERERROR);
            return restJson.toString();
        }

        return JSONObject.fromObject(restJson.getJSONObject("data")).toString();
    }

    /**
     * Provide function for get VNF
     * <br/>
     *
     * @param vnfmId
     * @param resp
     * @param vnfInstanceId
     * @param context
     * @return
     * @throws ServiceException
     * @since VFC 1.0
     */
    @GET
    @Path("/{vnfmId}/vnfs/{vnfInstanceId}")
    public String getVnf(@PathParam("vnfmId") String vnfmId, @Context HttpServletResponse resp,
            @PathParam("vnfInstanceId") String vnfInstanceId) throws ServiceException {
        LOG.warn("function=getVnf, msg=enter to get a vnf: vnfInstanceId: {}, vnfmId: {}", vnfInstanceId, vnfmId);
        JSONObject restJson = new JSONObject();

        if(StringUtils.isEmpty(vnfInstanceId) || StringUtils.isEmpty(vnfmId)) {
            resp.setStatus(Constant.HTTP_INNERERROR);
            return restJson.toString();
        }

        restJson = vnfMgr.getVnf(vnfInstanceId, vnfmId);
        if(restJson.getInt(Constant.RETCODE) == Constant.REST_FAIL) {
            LOG.error("function=getVnf, msg=getVnf fail");
            resp.setStatus(Constant.HTTP_INNERERROR);
            return restJson.toString();
        }

        restJson.remove(Constant.RETCODE);
        return restJson.toString();
    }

    /**
     * <br>
     * 
     * @param vnfmId
     * @param resp
     * @return
     * @throws ServiceException
     * @since VFC 1.0
     */
    @GET
    @Path("/{vnfmId}")
    public String getVnfmById(@PathParam("vnfmId") String vnfmId, @Context HttpServletResponse resp)
            throws ServiceException {
        LOG.warn("function=getVnfmById, vnfmId: {}", vnfmId);
        return VnfmUtil.getVnfmById(vnfmId).toString();
    }

    /**
     * Provide function for get job
     * <br/>
     *
     * @param jobId
     * @param vnfmId
     * @param resp
     * @param responseId
     * @return
     * @throws ServiceException
     * @since VFC 1.0
     */
    @GET
    @Path("/{vnfmId}/jobs/{jobId}")
    public String getJob(@PathParam("jobId") String jobId, @PathParam("vnfmId") String vnfmId,
            @Context HttpServletResponse resp, @QueryParam("@responseId") String responseId) throws ServiceException {
        LOG.warn("function=getJob, msg=enter to get a job: jobId: {}, responseId: {}", jobId, responseId);
        JSONObject restJson = new JSONObject();

        if(StringUtils.isEmpty(jobId) || StringUtils.isEmpty(vnfmId)) {
            resp.setStatus(Constant.HTTP_INNERERROR);
            return restJson.toString();
        }

        restJson = vnfMgr.getJob(jobId, vnfmId);
        if(restJson.getInt(Constant.RETCODE) == Constant.REST_FAIL) {
            LOG.error("function=getJob, msg=getJob fail");
            resp.setStatus(Constant.HTTP_INNERERROR);
            return restJson.toString();
        }

        return getJobBody(restJson);
    }

    /**
     * <br>
     * 
     * @param context
     *            {
     *            �action�: �vmReset�,
     *            �affectedvm�: {
     *            �vmid�: �804cca71 - 9ae9 - 4511 - 8e30 - d1387718caff�,
     *            �vduid�: �vdu_100�,
     *            �vmname�: �ZTE_SSS_111_PP_2_L�
     *            }
     *            }
     * @param resp
     * @param vnfmId
     * @param vnfInstanceId
     * @return
     * @throws ServiceException
     * @since VFC 1.0
     */
    @POST
    @Path("/{vnfmId}/vnfs/{vnfInstanceId}/heal")
    public String healVnf(@Context HttpServletRequest context, @Context HttpServletResponse resp,
            @PathParam("vnfmId") String vnfmId, @PathParam("vnfInstanceId") String vnfInstanceId)
            throws ServiceException {
        LOG.warn("function=healVnf, msg=enter to heal a vnf: vnfInstanceId: {}, vnfmId: {}", vnfInstanceId, vnfmId);
        JSONObject restJson = new JSONObject();
        JSONObject jsonObject = VnfmJsonUtil.getJsonFromContexts(context);

        if(StringUtils.isEmpty(vnfInstanceId) || StringUtils.isEmpty(vnfmId)) {
            resp.setStatus(Constant.HTTP_INNERERROR);
            restJson.put("message", "vnfmId is null or vnfInstanceId is null");
            return restJson.toString();
        }

        restJson = vnfMgr.healVnf(jsonObject, vnfInstanceId, vnfmId);
        if(restJson.getInt(Constant.RETCODE) == Constant.REST_FAIL) {
            LOG.error("function=healVnf, msg=healVnf fail");
            resp.setStatus(Constant.HTTP_INNERERROR);
            return restJson.toString();
        }

        restJson.remove(Constant.RETCODE);
        restJson.put("jobId", vnfInstanceId + "_post");
        return restJson.toString();
    }

    private String getJobBody(JSONObject restJson) {
        LOG.warn("function=getJobBody, restJson: {}", restJson);
        JSONObject responseJson = new JSONObject();
        JSONObject jobInfoJson = new JSONObject();
        JSONObject retJson = restJson.getJSONArray("data").getJSONObject(0);
        jobInfoJson.put("jobId", retJson.getString("id"));
        responseJson.put("progress", progressItem.get(retJson.getString(Constant.STATUS)));
        responseJson.put("status", jobstatusItem.get(retJson.getString(Constant.STATUS)));
        responseJson.put("errorCode", "null");
        responseJson.put("responseId", progressItem.get(retJson.getString(Constant.STATUS)));
        if(retJson.getString(Constant.STATUS) == null || retJson.getString(Constant.STATUS) == "null") {
            responseJson.put("progress", "100");
            responseJson.put("status", "finished");
            responseJson.put("errorCode", "null");
            responseJson.put("responseId", "100");
        }
        jobInfoJson.put("responsedescriptor", responseJson);
        LOG.warn("function=getJobBody, jobInfoJson: {}", jobInfoJson);
        return jobInfoJson.toString();
    }
}
