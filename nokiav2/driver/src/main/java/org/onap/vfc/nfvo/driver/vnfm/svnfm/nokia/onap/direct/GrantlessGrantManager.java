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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct;

import com.nokia.cbam.lcm.v32.model.VnfInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api.IGrantManager;
import org.onap.vnfmdriver.model.GrantVNFResponseVim;
import org.onap.vnfmdriver.model.VnfHealRequest;
import org.onap.vnfmdriver.model.VnfScaleRequest;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Responsible for handling granting before the execution of a VNF operation in case of direct integration
 */
@Component
@Qualifier("so")
public class GrantlessGrantManager implements IGrantManager {
    private static Logger logger = getLogger(GrantlessGrantManager.class);

    @Override
    public void requestGrantForHeal(String vnfmId, String vnfId, String vimId, String onapCsarId, VnfHealRequest request, String jobId) {
        noGrantRequested();
    }

    @Override
    public void requestGrantForScale(String vnfmId, String vnfId, String vimId, String onapCsarId, VnfScaleRequest request, String jobId) {
        noGrantRequested();
    }

    @Override
    public void requestGrantForTerminate(String vnfmId, String vnfId, String vimId, String onapVnfdId, VnfInfo vnf, String jobId) {
        noGrantRequested();
    }

    @Override
    public GrantVNFResponseVim requestGrantForInstantiate(String vnfmId, String vnfId, String vimId, String onapVnfdId, String instantiationLevelId, String cbamVnfdContent, String jobId) {
        noGrantRequested();
        GrantVNFResponseVim grantResponse = new GrantVNFResponseVim();
        grantResponse.setVimId(vimId);
        return grantResponse;
    }

    private void noGrantRequested() {
        logger.info("No grant is requested in direct mode");
    }
}
