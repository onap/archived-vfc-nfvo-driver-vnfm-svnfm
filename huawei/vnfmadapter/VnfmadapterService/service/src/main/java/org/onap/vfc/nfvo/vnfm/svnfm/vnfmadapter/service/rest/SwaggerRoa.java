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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swagger API Doc.<br/>
 *
 * @author
 * @version VFC 1.0 Oct 24, 2016
 */
@Path("/api/huaweivnfmdriver/v1")
@Produces({MediaType.APPLICATION_JSON})
public class SwaggerRoa {

	private static final Logger LOG = LoggerFactory.getLogger(SwaggerRoa.class);
    /**
     * API doc.
     * 
     * @return
     * @throws IOException
     */
    @GET
    @Path("/swagger.json")
    public String apidoc() throws IOException {
    	LOG.warn("function=apidoc, msg=enter to get a swagger: {}");
        ClassLoader classLoader = getClass().getClassLoader();
        return IOUtils.toString(classLoader.getResourceAsStream("swagger.json"));
    }
}
