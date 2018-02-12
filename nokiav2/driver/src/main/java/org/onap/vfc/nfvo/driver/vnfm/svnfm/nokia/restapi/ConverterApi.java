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

import org.apache.http.entity.ContentType;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.packagetransformer.OnapVnfPackageBuilder;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.DriverProperties.BASE_URL;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.SystemFunctions.systemFunctions;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Responsible for providing the converter utilities for CBAM package format
 */
@Controller
@RequestMapping(value = BASE_URL)
public class ConverterApi {
    private static Logger logger = getLogger(ConverterApi.class);
    private OnapVnfPackageBuilder vnfPackageConverter = new OnapVnfPackageBuilder();

    /**
     * Return the converted ONAP package
     *
     * @param httpResponse the HTTP response
     * @return the converted ONAP package
     */
    @RequestMapping(value = "/convert", method = POST)
    @ResponseBody
    public void convert(HttpServletResponse httpResponse, HttpServletRequest request) throws Exception {
        logger.info("REST: convert package");
        Part part = request.getParts().iterator().next();
        byte[] bytes = vnfPackageConverter.covert(part.getInputStream());
        httpResponse.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_OCTET_STREAM.getMimeType());
        httpResponse.setStatus(HttpStatus.OK.value());
        httpResponse.addHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(bytes.length));
        httpResponse.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + "core.csar" + "\"");
        httpResponse.getOutputStream().write(bytes);
        httpResponse.getOutputStream().flush();
    }

    /**
     * Return the HTTP page to upload the package
     * Can be removed after the generated swagger API in ONAP is fixed.
     *
     * @param httpResponse the HTTP response
     */
    @RequestMapping(value = "/convert", method = GET, produces = TEXT_HTML_VALUE)
    @ResponseBody
    public void getUploadPageForConvertingVnfd(HttpServletResponse httpResponse) throws Exception {
        logger.info("REST: get converter main page");
        byte[] bytes = systemFunctions().loadFile("upload.html");
        httpResponse.addHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(bytes.length));
        httpResponse.getOutputStream().write(bytes);
    }
}
