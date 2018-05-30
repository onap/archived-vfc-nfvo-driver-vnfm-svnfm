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

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.packagetransformer.OnapR1VnfPackageBuilder;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.packagetransformer.SupportedOnapPackageVersions;
import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.buildFatalFailure;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.SystemFunctions.systemFunctions;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.Constants.BASE_URL;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;
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
    private OnapR1VnfPackageBuilder vnfPackageConverter = new OnapR1VnfPackageBuilder();

    /**
     * Return the converted ONAP package
     *
     * @param httpResponse the HTTP response
     * @return the converted ONAP package
     */
    @RequestMapping(value = "/convert", method = POST)
    @ResponseBody
    public void convert(HttpServletResponse httpResponse, HttpServletRequest request) throws IOException {
        logger.info("REST: convert package");
        SupportedOnapPackageVersions version;
        try {
            request.getPart("version");
            version = SupportedOnapPackageVersions.valueOf(new String(ByteStreams.toByteArray(request.getPart("version").getInputStream()), Charsets.UTF_8));
        } catch (Exception e) {
            throw buildFatalFailure(logger, "Unable to determin`e the desired ONAP package version", e);
        }
        byte[] content;
        try {
            Part uploadedFile = request.getPart("fileToUpload");
            content = ByteStreams.toByteArray(uploadedFile.getInputStream());
        } catch (Exception e) {
            throw buildFatalFailure(logger, "Unable to extract package from REST parameters", e);
        }
        byte[] convertedPackage;
        try {
            convertedPackage = vnfPackageConverter.covert(new ByteArrayInputStream(content), version);
        } catch (Exception e) {
            throw buildFatalFailure(logger, "Unable to convert VNF package", e);
        }
        httpResponse.addHeader(CONTENT_TYPE, APPLICATION_OCTET_STREAM.toString());
        httpResponse.setStatus(OK.value());
        httpResponse.addHeader(CONTENT_LENGTH, Integer.toString(convertedPackage.length));
        httpResponse.addHeader(CONTENT_DISPOSITION, "attachment; filename=\"" + "core.csar" + "\"");
        httpResponse.getOutputStream().write(convertedPackage);
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
    public void getUploadPageForConvertingVnfd(HttpServletResponse httpResponse) throws IOException {
        logger.info("REST: get converter main page");
        byte[] bytes = systemFunctions().loadFile("upload.html");
        httpResponse.addHeader(CONTENT_LENGTH, Integer.toString(bytes.length));
        httpResponse.getOutputStream().write(bytes);
    }
}
