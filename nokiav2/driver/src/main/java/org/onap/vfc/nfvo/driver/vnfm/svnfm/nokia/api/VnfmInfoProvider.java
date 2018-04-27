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

import java.util.Set;
import org.onap.vnfmdriver.model.VnfmInfo;

/**
 * Responsible for providing information from the VNFM itself
 */
public interface VnfmInfoProvider {

    /**
     * @param vnfmId the identifier of the VNFM
     * @return the description of the VNFM
     */
    VnfmInfo getVnfmInfo(String vnfmId);

    /**
     * @return the identifiers of the VNFMs that are managed by this driver
     */
    Set<String> getVnfms();
}
