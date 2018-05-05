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
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.JobManager;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.JobManagerForVfc;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.LifecycleManager;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.LifecycleManagerForVfc;
import org.onap.vnfmdriver.model.*;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static java.util.Optional.empty;

import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.Constants.BASE_URL;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Responsible for providing the Nokia sVNFM REST APIs
 */
@Controller
@RequestMapping(value = BASE_URL)
public class LcmApi {
    public static final String IDENTIFIER = " identifier";
    private static Logger logger = getLogger(LcmApi.class);

    private final LifecycleManager lifecycleManager;
    private final JobManager jobManager;

    @Autowired
    LcmApi(LifecycleManagerForVfc lifecycleManager, JobManagerForVfc jobManager) {
        this.lifecycleManager = lifecycleManager;
        this.jobManager = jobManager;
    }

    /**
     * Instantiate the VNF (defined further in the VF-C driver integration documentation)
     *
     * @param request      the instantiation request
     * @param vnfmId       the identifier of the VNFM
     * @param httpResponse the HTTP response
     * @return the instantiated VNF info
     */
    @RequestMapping(value = "/{vnfmId}/vnfs", method = POST, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    public VnfInstantiateResponse instantiateVnf(@RequestBody VnfInstantiateRequest request, @PathVariable("vnfmId") String vnfmId, HttpServletResponse httpResponse) {
        logger.info("REST: Instantiate VNF with " + request.getVnfPackageId() + " CSAR identifier");
        VnfInstantiateResponse response = lifecycleManager.createAndInstantiate(vnfmId, request, httpResponse);
        httpResponse.setStatus(SC_CREATED);
        return response;
    }

    /**
     * Terminate the VNF (defined further in the VF-C driver integration documentation)
     *
     * @param request      the termination request
     * @param vnfmId       the identifier of the VNFM
     * @param vnfId        the identifier of the VNF
     * @param httpResponse the HTTP response
     * @return the job representing the VNF termination operation
     */
    @RequestMapping(value = "/{vnfmId}/vnfs/{vnfId}/terminate", method = POST, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public JobInfo terminateVnf(@RequestBody VnfTerminateRequest request, @PathVariable("vnfmId") String vnfmId, @PathVariable("vnfId") String vnfId, HttpServletResponse httpResponse) {
        logger.info("REST: Terminate VNF with " + vnfId + IDENTIFIER);
        return lifecycleManager.terminateAndDelete(vnfmId, vnfId, request, httpResponse);
    }

    /**
     * Query the VNF (defined further in the VF-C driver integration documentation)
     *
     * @param vnfmId       the identifier of the VNFM
     * @param vnfId        the identifier of the VNF
     * @param httpResponse the HTTP response
     * @return the VNF info
     */
    @RequestMapping(value = "/{vnfmId}/vnfs/{vnfId}", method = GET, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    public VnfInfo queryVnf(@PathVariable("vnfmId") String vnfmId, @PathVariable("vnfId") String vnfId, HttpServletResponse httpResponse) {
        logger.info("REST: Query VNF with " + vnfId + IDENTIFIER);
        return lifecycleManager.queryVnf(vnfmId, vnfId);
    }

    /**
     * Query the job (defined further in the VF-C driver integration documentation)
     *
     * @param jobId        the identifier of the job
     * @param vnfmId       the identifier of the VNFM
     * @param httpResponse the HTTP response
     * @return the instantiated VNF info
     */
    @RequestMapping(value = "/{vnfmId}/jobs/{jobId}", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public JobDetailInfo getJob(@PathVariable("vnfmId") String vnfmId, @PathVariable("jobId") String jobId, HttpServletResponse httpResponse) {
        logger.debug("REST: Query job with " + jobId + IDENTIFIER);
        return jobManager.getJob(vnfmId, jobId);
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
    public JobInfo scaleVnf(@RequestBody VnfScaleRequest request, @PathVariable("vnfmId") String vnfmId, @PathVariable("vnfId") String vnfId, HttpServletResponse httpResponse) {
        logger.info("REST: Scale VNF with " + vnfId + IDENTIFIER);
        return lifecycleManager.scaleVnf(vnfmId, vnfId, request, httpResponse);
    }

    /**
     * Heal the VNF (defined further in the VF-C driver integration documentation)
     *
     * @param request      the healing request
     * @param vnfmId       the identifier of the VNFM
     * @param vnfId        the identifier of the VNF
     * @param httpResponse the HTTP response
     * @return the job representing the healing operation
     */
    @RequestMapping(value = "/{vnfmId}/vnfs/{vnfId}/heal", method = POST, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    public JobInfo healVnf(@RequestBody VnfHealRequest request, @PathVariable("vnfmId") String vnfmId, @PathVariable("vnfId") String vnfId, HttpServletResponse httpResponse) {
        logger.info("REST: Heal VNF with " + vnfId + IDENTIFIER);
        return lifecycleManager.healVnf(vnfmId, vnfId, request, empty(), httpResponse);
    }
}
