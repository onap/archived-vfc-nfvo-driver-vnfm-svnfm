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

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;

/**
 * Swagger API Doc.<br/>
 *
 * @author
 * @version NFVO 0.5 Oct 24, 2016
 */
@Path("/openoapi/hwvnfm/v1")
@Produces({MediaType.APPLICATION_JSON})
public class SwaggerRoa {
    /**
     * API doc.
     * @param filename
     * @return
     * @throws IOException
     */
    @GET
    @Path("/swagger.json")
    public String apidoc() throws IOException{
        ClassLoader classLoader = getClass().getClassLoader();
        return IOUtils.toString(classLoader.getResourceAsStream("swagger.json"));
    }
}
