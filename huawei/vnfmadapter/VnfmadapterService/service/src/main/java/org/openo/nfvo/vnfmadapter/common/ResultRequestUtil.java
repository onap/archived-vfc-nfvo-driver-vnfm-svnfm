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

package org.openo.nfvo.vnfmadapter.common;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import org.apache.commons.httpclient.HttpMethod;
import org.openo.nfvo.vnfmadapter.service.constant.Constant;
import org.openo.nfvo.vnfmadapter.service.constant.ParamConstants;
import org.openo.nfvo.vnfmadapter.service.csm.connect.ConnectMgrVnfm;
import org.openo.nfvo.vnfmadapter.service.csm.connect.HttpRequests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONObject;

/**
 * <br/>
 * <p>
 * </p>
 *
 * @author
 * @version NFVO 0.5 Aug 25, 2016
 */
public final class ResultRequestUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ResultRequestUtil.class);

    private ResultRequestUtil() throws VnfmException {
        throw new VnfmException("can't be instanced.");
    }

    /**
     * common method
     * <br/>
     *
     * @param vnfmObject
     * @param path
     *            url defined
     * @param methodName
     *            [get, put, delete, post]
     * @param paramsJson
     *            raw data with json format, if <code>methodName</code> is get
     *            or delete, fill it with null
     * @return
     * @since NFVO 0.5
     */
    public static JSONObject call(JSONObject vnfmObject, String path, String methodName, String paramsJson) {
        JSONObject resultJson = new JSONObject();

        ConnectMgrVnfm mgrVcmm = new ConnectMgrVnfm();

        if(Constant.HTTP_OK != mgrVcmm.connect(vnfmObject)) {
            resultJson.put(Constant.RETCODE, Constant.HTTP_INNERERROR);
            resultJson.put("data", "connect fail.");
            return resultJson;
        }

        HttpMethod httpMethod = null;
        try {

            String result = null;
            String vnfPath = path.contains("%s") ? String.format(path, mgrVcmm.getRoaRand()) : path;
            LOG.info("function=call, msg=url is {}, session is {}", vnfmObject.getString("url") + vnfPath,
                    mgrVcmm.getAccessSession());
            HttpRequests.Builder builder = new HttpRequests.Builder(Constant.ANONYMOUS)
                    .addHeader(Constant.ACCESSSESSION, mgrVcmm.getAccessSession())
                    .setUrl(vnfmObject.getString("url"), vnfPath).setParams(paramsJson);
            MethodType methodType = MethodType.methodType(HttpRequests.Builder.class, new Class[0]);
            MethodHandle mt =
                    MethodHandles.lookup().findVirtual(builder.getClass(), methodName, methodType).bindTo(builder);

            builder = (HttpRequests.Builder)mt.invoke();
            httpMethod = builder.execute();
            result = httpMethod.getResponseBodyAsString();
            LOG.warn("function=call, msg=response status is {}. result is {}", httpMethod.getStatusCode(), result);
            resultJson.put(Constant.RETCODE, httpMethod.getStatusCode());
            resultJson.put("data", result);
        } catch(IOException e) {
            LOG.info("function=call, msg=IOException, e is {}", e);
        } catch(ReflectiveOperationException e) {
            LOG.info("function=call, msg=ReflectiveOperationException, e is {}", e);
        } catch(Throwable e) {
            LOG.info("function=call, msg=Throwable, e is {}", e);
        } finally {
            if(httpMethod != null) {
                httpMethod.releaseConnection();
            }
        }

        if(httpMethod == null) {
            resultJson.put(Constant.RETCODE, Constant.HTTP_INNERERROR);
            resultJson.put("data", "get connection error");
        }

        return resultJson;
    }

    /**
     * common method
     * <br/>
     *
     * @param vnfmObject
     * @param path
     *            url defined
     * @param methodName
     *            [get, put, delete, post]
     * @param paramsJson
     *            raw data with json format, if <code>methodName</code> is get
     *            or delete, fill it with null
     * @return
     * @since NFVO 0.5
     */
    public static JSONObject call(JSONObject vnfmObject, String path, String methodName, String paramsJson,
            String authModel) {
        LOG.info("request-param=" + paramsJson + ",authModel=" + authModel + ",path=" + path + ",vnfmInfo="
                + vnfmObject);
        JSONObject resultJson = new JSONObject();

        ConnectMgrVnfm mgrVcmm = new ConnectMgrVnfm();

        if(Constant.HTTP_OK != mgrVcmm.connect(vnfmObject, authModel)) {
            resultJson.put(Constant.RETCODE, Constant.HTTP_INNERERROR);
            resultJson.put("data", "connect fail.");
            return resultJson;
        }

        HttpMethod httpMethod = null;
        try {

            String result = null;
            String vnfPath = path.contains("%s") ? String.format(path, mgrVcmm.getRoaRand()) : path;
            LOG.info("function=call, msg=url is {}, session is {}", vnfmObject.getString("url") + vnfPath,
                    mgrVcmm.getAccessSession());
            HttpRequests.Builder builder =
                    new HttpRequests.Builder(authModel).addHeader(Constant.ACCESSSESSION, mgrVcmm.getAccessSession())
                            .setUrl(vnfmObject.getString("url"), vnfPath).setParams(paramsJson);
            MethodType methodType = MethodType.methodType(HttpRequests.Builder.class, new Class[0]);
            MethodHandle mt =
                    MethodHandles.lookup().findVirtual(builder.getClass(), methodName, methodType).bindTo(builder);

            builder = (HttpRequests.Builder)mt.invoke();
            httpMethod = builder.execute();
            result = httpMethod.getResponseBodyAsString();
            LOG.warn("function=call, msg=response status is {}. result is {}", httpMethod.getStatusCode(), result);
            resultJson.put(Constant.RETCODE, httpMethod.getStatusCode());
            resultJson.put("data", result);

            // logout delete tokens
            String token = mgrVcmm.getAccessSession();
            String roaRand = mgrVcmm.getRoaRand();
            String vnfmUrl = vnfmObject.getString("url");
            removeTokens(vnfmUrl, token, roaRand);
        } catch(IOException e) {
            LOG.info("function=call, msg=IOException, e is {}", e);
        } catch(ReflectiveOperationException e) {
            LOG.info("function=call, msg=ReflectiveOperationException, e is {}", e);
        } catch(Throwable e) {
            LOG.info("function=call, msg=Throwable, e is {}", e);
        } finally {
            if(httpMethod != null) {
                httpMethod.releaseConnection();
            }
        }

        if(httpMethod == null) {
            resultJson.put(Constant.RETCODE, Constant.HTTP_INNERERROR);
            resultJson.put("data", "get connection error");
        }

        return resultJson;
    }

    /**
     * <br>
     *
     * @since NFVO 0.5
     */
    private static void removeTokens(String vnfmUrl, String token, String roaRand) {
        HttpMethod httpMethodToken = null;
        String tokenUrl = String.format(ParamConstants.CSM_AUTH_DISCONNECT, "manoadmin", roaRand);
        LOG.info("removeTokens tokenUrl=" + tokenUrl);
        try {
            httpMethodToken = new HttpRequests.Builder(Constant.CERTIFICATE).setUrl(vnfmUrl.trim(), tokenUrl)
                    .setParams("").addHeader("X-Auth-Token", token).delete().execute();
            int statusCode = httpMethodToken.getStatusCode();
            String result = httpMethodToken.getResponseBodyAsString();
            LOG.info("removeTokens int=" + statusCode + ", result=" + result);
        } catch(IOException e) {
            LOG.info("function=call, msg=IOException, e is {}", e);
        } catch(Throwable e) {
            LOG.info("function=call, msg=Throwable, e is {}", e);
        } finally {
            if(httpMethodToken != null) {
                httpMethodToken.releaseConnection();
            }
        }
    }
}
