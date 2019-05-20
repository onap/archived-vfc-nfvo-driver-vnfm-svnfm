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

package org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.csm.connect;

import java.io.IOException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.VnfmException;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.constant.Constant;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.constant.ParamConstants;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.csm.api.ConnectInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/**
 * VNFM connection manager
 * .</br>
 *
 * @author
 * @version VFC 1.0 Sep 14, 2016
 */
public class ConnectMgrVnfm {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectMgrVnfm.class);

    private static final String CONNECT_FAIL = "connect fail, code:";

    private static final String CONNECT_JSONEXCEPTION = "function=connect, msg=connect JSONException e={}.";

    private static final String CONNECT_VNFMEXCEPTION = "function=connect, msg=connect VnfmException e={}.";

    private static final String CONNECT_IOEXCEPTION = "function=connect, msg=connect IOException e={}.";

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

    public int connectVnfm(JSONObject vnfmObj, String authModel) {
        LOG.info("function=connectVnfm, msg=enter connect function.");

        ConnectInfo info = new ConnectInfo(vnfmObj.getString("url"), vnfmObj.getString(Constant.USERNAME),
                vnfmObj.getString(Constant.PASSWORD), authModel);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(info.getUrl());
        } catch (Exception e) {
            LOG.error("Exception while creating connection: {}", e.getMessage());
        }

        return 1;
    }

    /**
     * <br>
     * 
     * @param vnfmObj
     * @param authModel
     * @return
     * @since VFC 1.0
     */
    public int connect(JSONObject vnfmObj, String authModel) {
        LOG.info("function=connect, msg=enter connect function.");

        ConnectInfo info = new ConnectInfo(vnfmObj.getString("url"), vnfmObj.getString(Constant.USERNAME),
                vnfmObj.getString(Constant.PASSWORD), authModel);
        HttpMethod httpMethod = null;
        int statusCode = Constant.INTERNAL_EXCEPTION;

        try {
            httpMethod = new HttpRequests.Builder(info.getAuthenticateMode())
                    .setUrl(info.getUrl(), ParamConstants.CSM_AUTH_CONNECT)
                    .setParams(String.format(ParamConstants.GET_TOKENS_V2, info.getUserName(), info.getUserPwd()))
                    .post().execute();
            statusCode = httpMethod.getStatusCode();

            String result = httpMethod.getResponseBodyAsString();
            LOG.info("connect result:" + result);
            if(statusCode == HttpStatus.SC_CREATED) {
                JSONObject accessObj = JSONObject.fromObject(result);
                JSONObject tokenObj = accessObj.getJSONObject("token");
                Header header = httpMethod.getResponseHeader("accessSession");
                setAccessSession(header.getValue());
                setRoaRand(tokenObj.getString("roa_rand"));
                statusCode = HttpStatus.SC_OK;
            } else {
                LOG.error(CONNECT_FAIL + statusCode + " re:" + result);
            }

        } catch(JSONException e) {
            LOG.error(CONNECT_JSONEXCEPTION, e);
        } catch(VnfmException e) {
            LOG.error(CONNECT_VNFMEXCEPTION, e);
        } catch(IOException e) {
            LOG.error(CONNECT_IOEXCEPTION, e);
        } finally {
            clearCSMPwd(info);
            if(httpMethod != null) {
                httpMethod.releaseConnection();
            }
        }
        return statusCode;

    }

    /**
     * <br>
     * 
     * @param vnfmObj
     * @param authModel
     * @return
     * @since VFC 1.0
     */
    public int connectSouth(JSONObject vnfmObj, String authModel) {
        LOG.info("function=connectSouth, msg=enter connect function.");
        String oldUrl = vnfmObj.getString("url").trim();
        String newUrl = oldUrl.replaceAll("30001", "30000");
        LOG.info("function=connectSouth, url={}.", newUrl);
        ConnectInfo info = new ConnectInfo(newUrl, vnfmObj.getString(Constant.USERNAME),
                vnfmObj.getString(Constant.PASSWORD), authModel);
        HttpMethod httpMethod = null;
        int statusCode = Constant.INTERNAL_EXCEPTION;

        try {
            httpMethod = new HttpRequests.Builder(info.getAuthenticateMode())
                    .setUrl(info.getUrl(), ParamConstants.CSM_AUTH_CONNECT_SOUTH)
                    .setParams(String.format(ParamConstants.GET_TOKENS_V3, info.getUserName(), info.getUserPwd(),
                            info.getUserName()))
                    .post().execute();
            statusCode = httpMethod.getStatusCode();

            String result = httpMethod.getResponseBodyAsString();
            LOG.info("connect statusCode={}, result={}:", statusCode, result);
            if(statusCode == HttpStatus.SC_CREATED) {
                LOG.info("function=connectSouth, header={}.", httpMethod.getResponseHeaders());
                Header header = httpMethod.getResponseHeader("X-Subject-Token");
                LOG.info("function=connectSouth, header={}.", header.getValue());
                setAccessSession(header.getValue());
                statusCode = HttpStatus.SC_OK;
            } else {
                LOG.error(CONNECT_FAIL + statusCode + " re:" + result);
            }

        } catch(JSONException e) {
            LOG.error(CONNECT_JSONEXCEPTION, e);
        } catch(VnfmException e) {
            LOG.error(CONNECT_VNFMEXCEPTION, e);
        } catch(IOException e) {
            LOG.error(CONNECT_IOEXCEPTION, e);
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
     * @since VFC 1.0
     */
    public int connect(JSONObject vnfmObj) {
        LOG.info("function=connect, msg=enter connect function.");

        ConnectInfo info = new ConnectInfo(vnfmObj.getString("url"), vnfmObj.getString(Constant.USERNAME),
                vnfmObj.getString(Constant.PASSWORD), Constant.ANONYMOUS);
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
                LOG.error(CONNECT_FAIL + statusCode + " re:" + result);
            }

        } catch(JSONException e) {
            LOG.error(CONNECT_JSONEXCEPTION, e);
        } catch(VnfmException e) {
            LOG.error(CONNECT_VNFMEXCEPTION, e);
        } catch(IOException e) {
            LOG.error(CONNECT_IOEXCEPTION, e);
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
