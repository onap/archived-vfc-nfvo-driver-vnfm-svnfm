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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api;

import com.nokia.cbam.lcm.v32.model.VnfInfo;
import org.onap.vnfmdriver.model.GrantVNFResponseVim;
import org.onap.vnfmdriver.model.VnfHealRequest;
import org.onap.vnfmdriver.model.VnfScaleRequest;

/**
 * Responsible for requesting grants during various LCM operations
 */
public interface IGrantManager {

    /**
     * Request grant for healing
     * - the affected virtual machine is added twice to the grant request (add & remove) to
     * signal that it is temporary removed
     * - the grant response is only used make a binary decision
     *
     * @param vnfmId  the identifier of the VNFM
     * @param vnfId   the identifier of the VNF
     * @param vimId   the identifier of the VIM
     * @param request the heal request
     * @param jobId   the identifier of the job that triggered the grant
     */
    void requestGrantForHeal(String vnfmId, String vnfId, String vimId, String onapCsarId, VnfHealRequest request, String jobId);

    /**
     * Request grant for scaling
     * - the affected virtual machines are calculated from the Heat mapping section of the corresponding aspect
     * - the grant response is only used make a binary decision
     *
     * @param vnfmId     the identifier of the VNFM
     * @param vnfId      the identifier of the VNF
     * @param vimId      the identifier of the VIM
     * @param onapCsarId the CSAR ID of the ONAP
     * @param request    the scaling request
     * @param jobId      the identifier of the job that triggered the grant
     */
    void requestGrantForScale(String vnfmId, String vnfId, String vimId, String onapCsarId, VnfScaleRequest request, String jobId);

    /**
     * Request grant for termination
     * - the resources removed is the previously deployed resources based on VNF query
     * - the grant response is only used make a binary decision
     *
     * @param vnfmId the identifier of the VNFM
     * @param vnfId  the identifier of the VNF
     * @param vimId  the identifier of the VIM
     */
    void requestGrantForTerminate(String vnfmId, String vnfId, String vimId, String onapVnfdId, VnfInfo vnf, String jobId);

    /**
     * Request grant for instantiation
     * - the added resources are calculated from the VNFD by counting the VDUs in the selected the instantiation level
     * - the only parameter used from the grant response in the VIM to which the VNF is to be deployed to
     *
     * @param vnfmId               the identifier of the VNFM
     * @param vnfId                the identifier of the VNF
     * @param vimId                the identifier of the VIM
     * @param onapVnfdId           the identifier of the VNF package in ONAP
     * @param instantiationLevelId the instantiation level
     * @param cbamVnfdContent      the content of the CBAM VNFD
     * @return the grant response
     */
    GrantVNFResponseVim requestGrantForInstantiate(String vnfmId, String vnfId, String vimId, String onapVnfdId, String instantiationLevelId, String cbamVnfdContent, String jobId);
}
