/*
 * Copyright 2016-2017, Nokia Corporation
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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.restapi;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core.SelfRegistrationManager;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core.SelfRegistrationManagerForVfc;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.Constants.BASE_URL;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpHeaders.CONTENT_LENGTH;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Responsible for providing the Nokia S-VNFM REST APIs for accessing the swagger definitions
 */
@Controller
@RequestMapping(value = BASE_URL)
public class SwaggerApi {
    private static Logger logger = getLogger(SwaggerApi.class);
    //FIXME this should not depened on self registration manager
    private final SelfRegistrationManager selfRegistrationManager;

    @Autowired
    SwaggerApi(SelfRegistrationManagerForVfc selfRegistrationManager) {
        this.selfRegistrationManager = selfRegistrationManager;
    }

    /**
     * Return the swagger definition
     *
     * @param httpResponse the HTTP response
     * @return the job representing the healing operation
     */
    @RequestMapping(value = "/swagger.json", method = GET)
    @ResponseBody
    public void getSwaggerApiDefinition(HttpServletResponse httpResponse) throws IOException {
        logger.info("REST: get swagger definition");
        httpResponse.addHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE);
        byte[] bytes = selfRegistrationManager.getSwaggerApiDefinition();
        httpResponse.addHeader(CONTENT_LENGTH, Integer.toString(bytes.length));
        httpResponse.getOutputStream().write(bytes);
    }
}
