/*
 * Copyright 2016 Huawei Technologies Co., Ltd.
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

package org.openo.nfvo.vnfmadapter.service.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.openo.nfvo.vnfmadapter.common.VnfmJsonUtil;
import org.openo.nfvo.vnfmadapter.service.constant.Constant;
import org.openo.nfvo.vnfmadapter.service.constant.ParamConstants;
import org.openo.nfvo.vnfmadapter.service.process.AuthMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONObject;

/**
 * Provide interfaces for authInfo
 * <br/>
 * <p>
 * </p>
 *
 * @author
 * @version NFVO 0.5 Aug 24, 2016
 */
@Path("/rest/plat/smapp/v1")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthRoa {

    private static final Logger LOG = LoggerFactory.getLogger(AuthRoa.class);

    private AuthMgr authMgr;

    public void setAuthMgr(AuthMgr authMgr) {
        this.authMgr = authMgr;
    }

    /**
     * Provide interface for add authInfo
     * <br/>
     *
     * @param context
     * @return
     * @since NFVO 0.5
     */
    @PUT
    @Path("/oauth/token")
    public String authToken(@Context HttpServletRequest context, @Context HttpServletResponse resp) {
        LOG.warn("function=login, msg=enter to get token.");
        JSONObject subJsonObject = VnfmJsonUtil.getJsonFromContexts(context);
        LOG.warn("subJsonObject: {}", subJsonObject);

        if(null == subJsonObject) {
            LOG.error("function=login, msg=params are insufficient");
            String resultStr = "Login params insufficient";
            resp.setStatus(Constant.HTTP_BAD_REQUEST);

            return resultStr;
        }

        JSONObject authResult = authMgr.authToken(subJsonObject);
        LOG.warn("authResult: {}", authResult);
        if(authResult.getInt("retCode") == Constant.REST_SUCCESS) {
            JSONObject data = authResult.getJSONObject("data");

            resp.setStatus(Constant.HTTP_OK);
            // resp.setHeader("accessSession", data.getString("accessSession"));
            return data.toString();// String.format(ParamConstants.GET_TOKEN_SUC_RESP,
            // data.getString("userName"),
            // data.getString("userName"), data.getString("roaRand"));
        } else if(authResult.getInt("retCode") == Constant.HTTP_INNERERROR) {
            Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(authResult.getString("data")).build();

            return String.format(ParamConstants.GET_TOKEN_FAIL_RESP, authResult.getString("data"));
        } else {
            Response.status(Response.Status.UNAUTHORIZED).entity(authResult.getString("data")).build();

            return String.format(ParamConstants.GET_TOKEN_FAIL_RESP, authResult.getString("data"));
        }
    }

    /**
     * Provide interface for delete authInfo
     * <br/>
     *
     * @param context
     * @param userName
     * @param roarand
     * @return
     * @since NFVO 0.5
     */
    @DELETE
    @Path("/auth/tokens/{userName}/{roarand}")
    public String delAuthToken(@Context HttpServletRequest context, @PathParam("userName") String userName,
                               @PathParam("roarand") String roarand, @Context HttpServletResponse resp) {
        LOG.warn("function=logout, msg=enter to logout");
        JSONObject resultJson = new JSONObject();

        resultJson.put("Information", "Operation success");
        resp.setStatus(Constant.HTTP_NOCONTENT);
        LOG.warn("function=logout, msg=end to logout");
        return resultJson.toString();
    }

    /**
     * Provide interface for handshake authInfo
     * <br/>
     *
     * @param context
     * @param roattr
     * @return
     * @since NFVO 0.5
     */
    @GET
    @Path("/nfvo/shakehand")
    public String shakehand(@Context HttpServletRequest context, @QueryParam("roattr") String roattr,
                            @Context HttpServletResponse resp) {
        JSONObject resultJson = new JSONObject();
        resultJson.put("status", "running");
        resultJson.put("description", "Operation success");
        resp.setStatus(Constant.HTTP_OK);

        return resultJson.toString();
    }
}
