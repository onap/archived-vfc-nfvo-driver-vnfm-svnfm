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

package org.openo.nfvo.vnfmadapter.service.adapter.impl;

import java.util.Map;

import org.openo.baseservice.roa.util.restclient.RestfulResponse;
import org.openo.nfvo.vnfmadapter.common.servicetoken.VNFRestfulUtil;
import org.openo.nfvo.vnfmadapter.service.adapter.inf.IDriver2MSBManager;
import org.openo.nfvo.vnfmadapter.service.constant.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONObject;

/**
 *
 * Driver to MSB Manager Class.<br/>
 * <p>
 * </p>
 *
 * @author
 * @version     NFVO 0.5  Aug 29, 2016
 */
public class Driver2MSBManager implements IDriver2MSBManager {

    private static final Logger LOG = LoggerFactory.getLogger(Driver2MSBManager.class);

    @Override
    public JSONObject registerDriver(Map<String, String> paramsMap, JSONObject driverInfo) {
        JSONObject resultObj = new JSONObject();

        RestfulResponse rsp = VNFRestfulUtil.getRemoteResponse(paramsMap, driverInfo.toString());
        if(null == rsp) {
            LOG.error("function=registerDriver,  RestfulResponse is null");
            resultObj.put("reason", "RestfulResponse is null.");
            resultObj.put("retCode", Constant.REST_FAIL);
            return resultObj;
        }
        String resultCreate = rsp.getResponseContent();

        if(rsp.getStatus() == Constant.HTTP_CREATED) {
            LOG.warn("function=registerDriver, msg= status={}, result={}.", rsp.getStatus(), resultCreate);
            resultObj = JSONObject.fromObject(resultCreate);
            resultObj.put("retCode", Constant.HTTP_CREATED);
            return resultObj;
        } else if(rsp.getStatus() == Constant.HTTP_INVALID_PARAMETERS) {
            LOG.error("function=registerDriver, msg=MSB return fail,invalid parameters,status={}, result={}.",
                    rsp.getStatus(), resultCreate);
            resultObj.put("reason", "MSB return fail,invalid parameters.");
        } else if(rsp.getStatus() == Constant.HTTP_INNERERROR) {
            LOG.error("function=registerDriver, msg=MSB return fail,internal system error,status={}, result={}.",
                    rsp.getStatus(), resultCreate);
            resultObj.put("reason", "MSB return fail,internal system error.");
        }
        resultObj.put("retCode", Constant.REST_FAIL);
        return resultObj;
    }

    @Override
    public JSONObject unregisterDriver(Map<String, String> paramsMap) {
        JSONObject resultObj = new JSONObject();

        RestfulResponse rsp = VNFRestfulUtil.getRemoteResponse(paramsMap, "");
        if(null == rsp) {
            LOG.error("function=unregisterDriver,  RestfulResponse is null");
            resultObj.put("reason", "RestfulResponse is null.");
            resultObj.put("retCode", Constant.REST_FAIL);
            return resultObj;
        }
        String resultCreate = rsp.getResponseContent();

        if(rsp.getStatus() == Constant.HTTP_NOCONTENT) {
            LOG.warn("function=unregisterDriver, msg= status={}, result={}.", rsp.getStatus(), resultCreate);
            resultObj = JSONObject.fromObject(resultCreate);
            resultObj.put("retCode", Constant.HTTP_NOCONTENT);
            return resultObj;
        } else if(rsp.getStatus() == Constant.HTTP_NOTFOUND) {
            LOG.error(
                    "function=unregisterDriver, msg=MSB return fail,can't find the service instance.status={}, result={}.",
                    rsp.getStatus(), resultCreate);
            resultObj.put("reason", "MSB return fail,can't find the service instance.");
        } else if(rsp.getStatus() == Constant.HTTP_INVALID_PARAMETERS) {
            LOG.error("function=unregisterDriver, msg=MSB return fail,invalid parameters,status={}, result={}.",
                    rsp.getStatus(), resultCreate);
            resultObj.put("reason", "MSB return fail,invalid parameters.");
        } else if(rsp.getStatus() == Constant.HTTP_INNERERROR) {
            LOG.error("function=unregisterDriver, msg=MSB return fail,internal system error,status={}, result={}.",
                    rsp.getStatus(), resultCreate);
            resultObj.put("reason", "MSB return fail,internal system error.");
        }
        resultObj.put("retCode", Constant.REST_FAIL);
        return resultObj;
    }

}
