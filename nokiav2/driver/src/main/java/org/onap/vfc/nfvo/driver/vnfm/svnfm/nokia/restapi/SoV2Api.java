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

import javax.servlet.http.HttpServletResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.so.SoV2LifecycleManager;
import org.onap.vnfmadapter.so.v2.model.*;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;

import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.Constants.BASE_URL;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * Responsible for providing the Nokia sVNFM REST APIs
 */
@Controller
@RequestMapping(value = BASE_URL + "/so/v2")
public class SoV2Api {
    private static Logger logger = getLogger(SoV2Api.class);

    private final SoV2LifecycleManager soLifecycleManager;

    @Autowired
    SoV2Api(SoV2LifecycleManager lifecycleManager) {
        this.soLifecycleManager = lifecycleManager;
    }

    /**
     * Create the VNF
     *
     * @param request      the creation request
     * @param vnfIdInAai   the identifier of the VNF in A&AI
     * @param httpResponse the HTTP response
     * @return the descriptor of the created VNF
     */
    @RequestMapping(value = "/vnfs/{vnfIdInAai}", method = POST, consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    public void createVnf(@RequestBody SoV2VnfCreateRequest request, @PathVariable("vnfIdInAai") String vnfIdInAai, HttpServletResponse httpResponse) {
        logger.info("REST: Create the VNF");
        soLifecycleManager.createVnf(vnfIdInAai, request, httpResponse);
        httpResponse.setStatus(SC_NO_CONTENT);
    }

    /**
     * Query the VNF
     *
     * @param request      the creation request
     * @param vnfIdInAai   the identifier of the VNF in A&AI
     * @param httpResponse the HTTP response
     * @return the descriptor of the created VNF
     */
    @RequestMapping(value = "/vnfs/{vnfIdInAai}", method = POST, consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    public SoV2VnfQueryResponse queryVnf(@RequestBody SoV2VnfQueryRequest request, @PathVariable("vnfIdInAai") String vnfIdInAai, HttpServletResponse httpResponse) {
        logger.info("REST: Create the VNF");
        return soLifecycleManager.queryVnf(vnfIdInAai, request, httpResponse);
    }

    /**
     * Terminate the VNF
     *
     * @param request      the termination request
     * @param vnfIdInAai   the identifier of the VNF in A&AI
     * @param httpResponse the HTTP response
     * @return the job representing the VNF termination operation
     */
    @RequestMapping(value = "/vnfs/{vnfIdInAai}", method = DELETE, consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    public void delete(@RequestBody SoV2VnfDeleteRequest request, @PathVariable("vnfIdInAai") String vnfIdInAai, HttpServletResponse httpResponse) {
        logger.info("REST: Deactivate the VNF");
        soLifecycleManager.delete(vnfIdInAai, request, httpResponse);
        httpResponse.setStatus(SC_NO_CONTENT);
    }

    /**
     * Update the VNF
     *
     * @param request      the creation request
     * @param vnfIdInAai   the identifier of the VNF in A&AI
     * @param httpResponse the HTTP response
     * @return the descriptor of the created VNF
     */
    @RequestMapping(value = "/vnfs/{vnfIdInAai}", method = PUT, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    public SoV2VnfUpdateResponse updateVnf(@RequestBody SoV2VnfUpdateRequest request, @PathVariable("vnfIdInAai") String vnfIdInAai, HttpServletResponse httpResponse) {
        logger.info("REST: Update the VNF");
        return soLifecycleManager.updateVnf(vnfIdInAai, request, httpResponse);
    }

    /**
     * Rollback update VNF
     *
     * @param request      the rollback request
     * @param httpResponse the HTTP response
     */
    @RequestMapping(value = "/vnfs/{vnfIdInAai}/rollback", method = POST, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    public void rollback(@RequestBody SoV2RollbackVnfUpdate request, @PathVariable("vnfIdInAai") String vnfIdInAai, HttpServletResponse httpResponse) {
        logger.info("REST: Create the VF");
        soLifecycleManager.rollback(vnfIdInAai, request, httpResponse);
    }

    /**
     * Create the VF module
     *
     * @param request      the creation request
     * @param vnfIdInAai   the identifier of the VNF in A&AI
     * @param httpResponse the HTTP response
     */
    @RequestMapping(value = "/vfmodule/{vnfIdInAai}/{vfModuleId}", method = POST, consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    public void createVfModule(@RequestBody SoV2VfModuleCreateRequest request, @PathVariable("vnfIdInAai") String vnfIdInAai, @PathVariable("vfModuleId") String vfModuleId, HttpServletResponse httpResponse) {
        logger.info("REST: Create the VF");
        soLifecycleManager.createVfModule(vnfIdInAai, vfModuleId, request, httpResponse);
        httpResponse.setStatus(SC_CREATED);
    }

    /**
     * Terminate the VF module
     *
     * @param request      the termination request
     * @param vnfIdInAai   the identifier of the VNF in A&AI
     * @param httpResponse the HTTP response
     */
    @RequestMapping(value = "/vfmodule/{vnfIdInAai}/{vfModuleId}", method = DELETE, consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    public void deleteVfModule(@RequestBody SoV2VnfDeleteRequest request, @PathVariable("vnfIdInAai") String vnfIdInAai, @PathVariable("vfModuleId") String vfModuleId, HttpServletResponse httpResponse) {
        logger.info("REST: Deactivate the VNF");
        soLifecycleManager.deleteVfModule(vnfIdInAai, vfModuleId, request, httpResponse);
        httpResponse.setStatus(SC_NO_CONTENT);
    }

    /**
     * Update the VF module
     *
     * @param request      the creation request
     * @param vnfIdInAai   the identifier of the VNF in A&AI
     * @param httpResponse the HTTP response
     * @return the descriptor of the created VNF
     */
    @RequestMapping(value = "/vfmodule/{vnfIdInAai}/{vfModuleId}", method = PUT, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    public SoV2VnfUpdateResponse updateVfModule(@RequestBody SoV2VnfUpdateRequest request, @PathVariable("vnfIdInAai") String vnfIdInAai, @PathVariable("vfModuleId") String vfModuleId, HttpServletResponse httpResponse) {
        logger.info("REST: Update the VNF");
        return soLifecycleManager.updateVfModule(vnfIdInAai, vfModuleId, request, httpResponse);
    }

    /**
     * Rollback update VNF
     *
     * @param request      the rollback request
     * @param httpResponse the HTTP response
     */
    @RequestMapping(value = "/vfmodule/{vnfIdInAai}/{vfModuleId}/rollback", method = PUT, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    public void rollbackVfModuleUpdate(@RequestBody SoV2RollbackVnfUpdate request, @PathVariable("vnfIdInAai") String vnfIdInAai, HttpServletResponse httpResponse) {
        logger.info("REST: Roll back VF module update");
        soLifecycleManager.rollback(vnfIdInAai, request, httpResponse);
    }

    /**
     * Provides a probe for SO to test health of VNFM adapter
     *
     * @param httpResponse the HTTP response
     */
    @RequestMapping(value = "/ping", method = GET)
    public void testLcnConnectivity(HttpServletResponse httpResponse) {
        httpResponse.setStatus(HttpServletResponse.SC_OK);
    }
}
