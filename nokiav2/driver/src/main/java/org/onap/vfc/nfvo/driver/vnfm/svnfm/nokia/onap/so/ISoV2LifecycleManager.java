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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.so;

import javax.servlet.http.HttpServletResponse;
import org.onap.vnfmadapter.so.v2.model.*;

public interface ISoV2LifecycleManager {

    /**
     * Create the VNF in VNFM
     *
     * @param vnfIdInAai the identifier of the VNF in A&AI
     * @param request the SO VNF creation request
     * @param httpResponse the HTTP response
     */
    void createVnf(String vnfIdInAai, SoV2VnfCreateRequest request, HttpServletResponse httpResponse);

    /**
     * Delete the VNF in VNFM
     *
     * @param vnfIdInAai the identifier of the VNF in A&AI
     * @param request the VNF deletion request
     * @param httpServletResponse the HTTP response
     */
    void delete(String vnfIdInAai, SoV2VnfDeleteRequest request, HttpServletResponse httpServletResponse);

    /**
     * Update the VNF in VNFM
     *
     * - only the VNF modifiable attributes are updated
     *
     * @param vnfIdInAai the identifier of the VNF in A&AI
     * @param request the SO VNF update request
     * @param httpResponse the HTTP response
     * @return the response the HTTP response
     */
    SoV2VnfUpdateResponse updateVnf(String vnfIdInAai, SoV2VnfUpdateRequest request, HttpServletResponse httpResponse);


    /**
     * Rollback the operation on the VNFM
     * @param vnfIdInAai the identifier of the VNF in A&AI
     * @param rollback the rollback parameters
     */
    void rollback(String vnfIdInAai, SoV2RollbackVnfUpdate rollback, HttpServletResponse httpServletResponse);

    /**
     * Create VF module in VNFM
     *
     * @param vnfIdInAai the identifier of the VNF in A&AI
     * @param vfModuleId the identifier of the VF module in A&AI
     * @param request the creation request
     * @param httpResponse the HTTP response
     */
    void createVfModule(String vnfIdInAai, String vfModuleId, SoV2VfModuleCreateRequest request, HttpServletResponse httpResponse);

    /**
     * Delete VF module in VNFM
     *
     * @param vnfIdInAai the identifier of the VNF in A&AI
     * @param vfModuleId the identifier of the VF module in A&AI
     * @param request the deletion request
     * @param httpResponse the HTTP response
     */
    void deleteVfModule(String vnfIdInAai, String vfModuleId, SoV2VnfDeleteRequest request, HttpServletResponse httpResponse);

    /**
     * Update VF module
     *
     * @param vnfIdInAai the identifier of the VNF in A&AI
     * @param vfModuleId the identifier of the VF module in A&AI
     * @param request the VF module update request
     * @param httpResponse the HTTP response
     * @return the response
     */
    SoV2VnfUpdateResponse updateVfModule(String vnfIdInAai, String vfModuleId, SoV2VnfUpdateRequest request, HttpServletResponse httpResponse);
}
