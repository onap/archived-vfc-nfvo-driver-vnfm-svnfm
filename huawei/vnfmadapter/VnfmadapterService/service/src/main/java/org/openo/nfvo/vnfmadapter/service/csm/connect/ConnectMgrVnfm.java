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

package org.openo.nfvo.vnfmadapter.service.csm.connect;

import java.io.IOException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.openo.nfvo.vnfmadapter.common.VnfmException;
import org.openo.nfvo.vnfmadapter.service.constant.Constant;
import org.openo.nfvo.vnfmadapter.service.constant.ParamConstants;
import org.openo.nfvo.vnfmadapter.service.csm.api.ConnectInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/**
 * VNFM connection manager
 * .</br>
 *
 * @author
 * @version     NFVO 0.5  Sep 14, 2016
 */
public class ConnectMgrVnfm {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectMgrVnfm.class);

    private String accessSession;

    private String roaRand;

    public String getAccessSession() {
        return accessSession;
    }

    public void setAccessSession(String accessSession) {
        this.accessSession = accessSession;
    }

    public String getRoaRand() {
        return roaRand;
    }

    public void setRoaRand(String roaRand) {
        this.roaRand = roaRand;
    }


    /**
     * Make connection
     * <br>
     *
     * @param vnfmObj
     * @return
     * @since  NFVO 0.5
     */
    public int connect(JSONObject vnfmObj,String authModel) {
        LOG.info("function=connect, msg=enter connect function.");

        ConnectInfo info = new ConnectInfo(vnfmObj.getString("url"), vnfmObj.getString("userName"),
                vnfmObj.getString("password"), authModel);
        HttpMethod httpMethod = null;
        int statusCode = Constant.INTERNAL_EXCEPTION;

        try {
            httpMethod = new HttpRequests.Builder(info.getAuthenticateMode())
                    .setUrl(info.getUrl(), ParamConstants.CSM_AUTH_CONNECT)
                    .setParams(String.format(ParamConstants.GET_TOKENS_V2, info.getUserName(), info.getUserPwd()))
                    .post().execute();
            statusCode = httpMethod.getStatusCode();

            String result = httpMethod.getResponseBodyAsString();
            LOG.info("connect result:"+result);
            if(statusCode == HttpStatus.SC_CREATED) {
                JSONObject accessObj = JSONObject.fromObject(result);
                JSONObject tokenObj = accessObj.getJSONObject("token");
                Header header = httpMethod.getResponseHeader("accessSession");
                setAccessSession(header.getValue());
                setRoaRand(tokenObj.getString("roa_rand"));
                statusCode = HttpStatus.SC_OK;
            } else {
                LOG.error("connect fail, code:" + statusCode + " re:" + result);
            }

        } catch(JSONException e) {
            LOG.error("function=connect, msg=connect JSONException e={}.", e);
        } catch(VnfmException e) {
            LOG.error("function=connect, msg=connect VnfmException e={}.", e);
        } catch(IOException e) {
            LOG.error("function=connect, msg=connect IOException e={}.", e);
        } finally {
            clearCSMPwd(info);
            if(httpMethod != null) {
                httpMethod.releaseConnection();
            }
        }
        return statusCode;

    }
    /**
     * Make connection
     * <br>
     *
     * @param vnfmObj
     * @return
     * @since  NFVO 0.5
     */
    public int connect(JSONObject vnfmObj) {
        LOG.info("function=connect, msg=enter connect function.");

        ConnectInfo info = new ConnectInfo(vnfmObj.getString("url"), vnfmObj.getString("userName"),
                vnfmObj.getString("password"), Constant.ANONYMOUS);
        HttpMethod httpMethod = null;
        int statusCode = Constant.INTERNAL_EXCEPTION;

        try {
            httpMethod = new HttpRequests.Builder(info.getAuthenticateMode())
                    .setUrl(info.getUrl(), ParamConstants.CSM_AUTH_CONNECT)
                    .setParams(String.format(ParamConstants.GET_TOKENS_V2, info.getUserName(), info.getUserPwd()))
                    .post().execute();
            statusCode = httpMethod.getStatusCode();

            String result = httpMethod.getResponseBodyAsString();

            if(statusCode == HttpStatus.SC_CREATED) {
                JSONObject accessObj = JSONObject.fromObject(result);
                JSONObject tokenObj = accessObj.getJSONObject("token");
                Header header = httpMethod.getResponseHeader("accessSession");
                setAccessSession(header.getValue());
                setRoaRand(tokenObj.getString("roa_rand"));
                statusCode = HttpStatus.SC_OK;
            } else {
                LOG.error("connect fail, code:" + statusCode + " re:" + result);
            }

        } catch(JSONException e) {
            LOG.error("function=connect, msg=connect JSONException e={}.", e);
        } catch(VnfmException e) {
            LOG.error("function=connect, msg=connect VnfmException e={}.", e);
        } catch(IOException e) {
            LOG.error("function=connect, msg=connect IOException e={}.", e);
        } finally {
            clearCSMPwd(info);
            if(httpMethod != null) {
                httpMethod.releaseConnection();
            }
        }
        return statusCode;

    }

    private void clearCSMPwd(ConnectInfo connectInfo) {
        if(null != connectInfo) {
            connectInfo.setUserPwd("");
        }
    }
}
