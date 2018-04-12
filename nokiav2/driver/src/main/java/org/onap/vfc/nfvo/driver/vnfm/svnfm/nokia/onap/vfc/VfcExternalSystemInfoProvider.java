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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.vfc;

import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core.GenericExternalSystemInfoProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.spring.Conditions;
import org.onap.vnfmdriver.model.VimInfo;
import org.onap.vnfmdriver.model.VnfmInfo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.buildFatalFailure;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Responsible for providing information related to the VNFM from VF-C source
 */
@Component
@Conditional(value = Conditions.UseForVfc.class)
public class VfcExternalSystemInfoProvider extends GenericExternalSystemInfoProvider {
    private static Logger logger = getLogger(VfcExternalSystemInfoProvider.class);
    private final VfcRestApiProvider vfcRestApiProvider;

    @Autowired
    VfcExternalSystemInfoProvider(Environment environment, VfcRestApiProvider vfcRestApiProvider) {
        super(environment);
        this.vfcRestApiProvider = vfcRestApiProvider;
    }

    @Override
    public VnfmInfo queryVnfmInfoFromSource(String vnfmId) {
        try {
            return vfcRestApiProvider.getNsLcmApi().queryVnfmInfo(vnfmId).blockingFirst();
        } catch (Exception e) {
            throw buildFatalFailure(logger, "Unable to query VNFM from VF-C with " + vnfmId + " identifier", e);
        }
    }

    @Override
    public VimInfo getVimInfo(String vimId) {
        try {
            return vfcRestApiProvider.getNsLcmApi().queryVIMInfo(vimId).blockingFirst();
        } catch (Exception e) {
            throw buildFatalFailure(logger, "Unable to query VIM from VF-C with " + vimId + " identifier", e);
        }
    }
}
