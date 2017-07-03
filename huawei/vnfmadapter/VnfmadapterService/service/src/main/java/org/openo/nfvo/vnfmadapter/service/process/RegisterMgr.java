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

package org.openo.nfvo.vnfmadapter.service.process;

import java.util.HashMap;
import java.util.Map;

import org.openo.baseservice.roa.util.restclient.RestfulResponse;
import org.openo.nfvo.vnfmadapter.common.RegisterConfigInfo;
import org.openo.nfvo.vnfmadapter.common.servicetoken.VnfmRestfulUtil;
import org.openo.nfvo.vnfmadapter.service.constant.ParamConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONObject;

/**
 * Provide function for register or unregister service to Bus.
 * <br/>
 *
 * @author
 * @version NFVO 0.5 Aug 24, 2016
 */
public class RegisterMgr {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterMgr.class);

    /**
     * Register service to the Bus
     * <br/>
     *
     * @since NFVO 0.5
     */
    public void register() {
        RestfulResponse rsp = VnfmRestfulUtil.getRemoteResponse(ParamConstants.MSB_REGISTER_URL,
                VnfmRestfulUtil.TYPE_POST, getRegsiterBody());

        LOG.error("funtion=register, status={}", rsp.getStatus());
    }

    /**
     * UnRegister service to the Bus
     * <br/>
     *
     * @since NFVO 0.5
     */
    public void unRegister() {
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("url", ParamConstants.MSB_UNREGISTER_URL);
        paramsMap.put("methodType", VnfmRestfulUtil.TYPE_DEL);
        RestfulResponse rsp = VnfmRestfulUtil.getRemoteResponse(ParamConstants.MSB_UNREGISTER_URL,
                VnfmRestfulUtil.TYPE_DEL, null);

        LOG.error("funtion=register, status={}", rsp.getStatus());
    }

    private String getRegsiterBody() {
        JSONObject body = new JSONObject();
        body.put("serviceName", RegisterConfigInfo.getInstance().getServiceName());
        body.put("version", RegisterConfigInfo.getInstance().getVersion());
        body.put("url", RegisterConfigInfo.getInstance().getUrl());
        body.put("protocol", RegisterConfigInfo.getInstance().getProtocol());
        body.put("port", RegisterConfigInfo.getInstance().getPort());
        body.put("ip", RegisterConfigInfo.getInstance().getIp());
        body.put("ttl", RegisterConfigInfo.getInstance().getTtl());

        return body.toString();
    }
}
