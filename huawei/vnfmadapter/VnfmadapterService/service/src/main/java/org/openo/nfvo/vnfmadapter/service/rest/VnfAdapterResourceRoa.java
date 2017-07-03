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
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.openo.nfvo.vnfmadapter.service.adapter.impl.AdapterResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONObject;


/**
 * Provide interfaces of resource for VNFM.
 * <br/>
 *
 * @author
 * @version NFVO 0.5 Aug 24, 2016
 */
@Path("/rest/v2/computeservice/getAllCloud")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class VnfAdapterResourceRoa {

    private static final Logger LOG = LoggerFactory.getLogger(VnfAdapterResourceRoa.class);


    /**
     * Provide function of grant resource.
     * <br/>
     *
     * @param context
     * @return
     * @since NFVO 0.5
     */
    @GET
    public String getAllCloudInfo(@Context HttpServletRequest context) {
        LOG.info("function=getAllCloudInfo, msg=getAllCloudInfo resource");

        //Find a way to get url and pass it getAllCloud(); //NOSONAR

        AdapterResourceManager arm = new AdapterResourceManager();
        JSONObject resultObj = arm.getAllCloud("","");

        return resultObj.getString("vim_id");
    }
}
