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

package org.openo.nfvo.vnfmadapter.service.rest.exceptionmapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.openo.baseservice.remoteservice.exception.ServiceException;

/**
 * ServiceException response provider.<br>
 *
 * @author
 * @version     NFVO 0.5 Sep 27, 2016
 */
@Provider
public class ServiceExceptionMapper implements ExceptionMapper<ServiceException>{

    @Override
    public Response toResponse(ServiceException exception) {
        ExceptionMessage message = new ExceptionMessage();
        message.setErrorCode(exception.getId());
        message.setHttpCode(exception.getHttpCode());
        message.setMessage(exception.getMessage());
        return Response.status(message.getHttpCode()).
                type(MediaType.APPLICATION_JSON).entity(message).build();
    }

}
