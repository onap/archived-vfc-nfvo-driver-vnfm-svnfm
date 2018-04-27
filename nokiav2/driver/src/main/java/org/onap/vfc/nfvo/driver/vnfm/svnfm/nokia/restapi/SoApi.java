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
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.so.SoLifecycleManager;
import org.onap.vnfmadapter.so.model.*;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static javax.servlet.http.HttpServletResponse.SC_CREATED;

import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.Constants.BASE_URL;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * Responsible for providing the Nokia sVNFM REST APIs
 */
@Controller
@RequestMapping(value = BASE_URL + "/so")
public class SoApi {
    private static Logger logger = getLogger(SoApi.class);

    private final SoLifecycleManager soLifecycleManager;

    @Autowired
    SoApi(SoLifecycleManager lifecycleManager) {
        this.soLifecycleManager = lifecycleManager;
    }

    /**
     * Create the VNF
     *
     * @param request      the creation request
     * @param vnfmId       the identifier of the VNFM
     * @param httpResponse the HTTP response
     * @return the descriptor of the created VNF
     */
    @RequestMapping(value = "/{vnfmId}/vnfs", method = POST, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    public SoVnfCreationResponse createVnf(@RequestBody SoVnfCreationRequest request, @PathVariable("vnfmId") String vnfmId, HttpServletResponse httpResponse) {
        logger.info("REST: Create the VNF");
        SoVnfCreationResponse response = soLifecycleManager.create(vnfmId, request);
        httpResponse.setStatus(SC_CREATED);
        return response;
    }

    /**
     * Activate the VNF
     *
     * @param request      the activation request
     * @param vnfmId       the identifier of the VNFM
     * @param httpResponse the HTTP response
     * @return the descriptor of the created VNF
     */
    @RequestMapping(value = "/{vnfmId}/vnfs/{vnfId}", method = POST, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    public SoJobHandler activateVnf(@RequestBody SoVnfActivationRequest request, @PathVariable("vnfmId") String vnfmId, @PathVariable("vnfId") String vnfId, HttpServletResponse httpResponse) {
        logger.info("REST: Activate the VNF");
        return soLifecycleManager.activate(vnfmId, vnfId, request, httpResponse);
    }

    /**
     * Execute custom operation on the VNF
     *
     * @param request      the custom operation request
     * @param vnfmId       the identifier of the VNFM
     * @param httpResponse the HTTP response
     * @return the descriptor of the created VNF
     */
    @RequestMapping(value = "/{vnfmId}/vnfs/{vnfId}/customOperation", method = POST, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    public SoJobHandler executeCustomOperation(@RequestBody SoVnfCustomOperation request, @PathVariable("vnfmId") String vnfmId, @PathVariable("vnfId") String vnfId, HttpServletResponse httpResponse) {
        logger.info("REST: Execute custom operation on the VNF");
        return soLifecycleManager.customOperation(vnfmId, vnfId, request, httpResponse);
    }

    /**
     * Terminate the VNF
     *
     * @param request      the termination request
     * @param vnfmId       the identifier of the VNFM
     * @param vnfId        the identifier of the VNF
     * @param httpResponse the HTTP response
     * @return the job representing the VNF termination operation
     */
    @RequestMapping(value = "/{vnfmId}/vnfs/{vnfId}/terminate", method = POST, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public SoJobHandler deactivateVnf(@RequestBody SoVnfTerminationRequest request, @PathVariable("vnfmId") String vnfmId, @PathVariable("vnfId") String vnfId, HttpServletResponse httpResponse) {
        logger.info("REST: Deactivate the VNF");
        return soLifecycleManager.deactivate(vnfmId, vnfId, request, httpResponse);
    }

    /**
     * Delete the VNF
     *
     * @param vnfmId       the identifier of the VNFM
     * @param vnfId        the identifier of the VNF
     * @param httpResponse the HTTP response
     */
    @RequestMapping(value = "/{vnfmId}/vnfs/{vnfId}", method = DELETE)
    public void deleteVnf(@PathVariable("vnfmId") String vnfmId, @PathVariable("vnfId") String vnfId, HttpServletResponse httpResponse) {
        logger.info("REST: Delete the VNF");
        soLifecycleManager.delete(vnfmId, vnfId);
        httpResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    /**
     * Query the job
     *
     * @param jobId        the identifier of the job
     * @param vnfmId       the identifier of the VNFM
     * @param httpResponse the HTTP response
     * @return the instantiated VNF info
     */
    @RequestMapping(value = "/{vnfmId}/jobs/{jobId}", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public SoJobDetail getJob(@PathVariable("vnfmId") String vnfmId, @PathVariable("jobId") String jobId, HttpServletResponse httpResponse) {
        logger.trace("REST: Query the job");
        return soLifecycleManager.getJobDetails(vnfmId, jobId);
    }

    /**
     * Scale the VNF (defined further in the VF-C driver integration documentation)
     *
     * @param request      the scaling request
     * @param vnfmId       the identifier of the VNFM
     * @param vnfId        the identifier of the VNF
     * @param httpResponse the HTTP response
     * @return the job representing the scaling operation
     */
    @RequestMapping(value = "/{vnfmId}/vnfs/{vnfId}/scale", method = POST, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    public SoJobHandler scaleVnf(@RequestBody SoVnfScaleRequest request, @PathVariable("vnfmId") String vnfmId, @PathVariable("vnfId") String vnfId, HttpServletResponse httpResponse) {
        logger.info("REST: Scale the VNF");
        return soLifecycleManager.scale(vnfmId, vnfId, request, httpResponse);
    }

    /**
     * Heal the VNF (defined further in the VF-C driver integration documentation)
     *
     * @param request       the healing request
     * @param vnfmId        the identifier of the VNFM
     * @param vnfInstanceId the identifier of the VNF
     * @param httpResponse  the HTTP response
     * @return the job representing the healing operation
     */
    @RequestMapping(value = "/{vnfmId}/vnfs/{vnfId}/heal", method = POST, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    public SoJobHandler healVnf(@RequestBody SoVnfHealRequest request, @PathVariable("vnfmId") String vnfmId, @PathVariable("vnfId") String vnfInstanceId, HttpServletResponse httpResponse) {
        logger.info("REST: Heal the VNF");
        return soLifecycleManager.heal(vnfmId, vnfInstanceId, request, httpResponse);
    }
}
