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

import java.util.Set;
import org.onap.aai.model.EsrSystemInfo;
import org.onap.aai.model.EsrVnfm;
import org.onap.aai.model.EsrVnfmList;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core.GenericExternalSystemInfoProvider;
import org.onap.vnfmdriver.model.VimInfo;
import org.onap.vnfmdriver.model.VnfmInfo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Sets.newHashSet;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.buildFatalFailure;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.LifecycleManager.getCloudOwner;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.LifecycleManager.getRegionName;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Responsible for providing information related to the VNFM from VF-C source
 */
@Component
@Qualifier("so")
public class AAIExternalSystemInfoProvider extends GenericExternalSystemInfoProvider {
    private static Logger logger = getLogger(AAIExternalSystemInfoProvider.class);
    private final AAIRestApiProvider aaiRestApiProvider;

    @Autowired
    AAIExternalSystemInfoProvider(Environment environment, AAIRestApiProvider aaiRestApiProvider) {
        super(environment);
        this.aaiRestApiProvider = aaiRestApiProvider;
    }

    @Override
    public VnfmInfo queryVnfmInfoFromSource(String vnfmId) {
        return convertEsrToVnfmInfo(getEsrVnfm(vnfmId));
    }

    private EsrVnfm getEsrVnfm(String vnfmId) {
        try {
            return aaiRestApiProvider.getExternalSystemApi().getExternalSystemEsrVnfmListEsrVnfm(vnfmId).blockingFirst();
        } catch (Exception e) {
            throw buildFatalFailure(logger, "Unable to query VNFM with " + vnfmId + " identifier from AAI", e);
        }
    }

    @Override
    public VimInfo getVimInfo(String vimId) {
        return convertEsrToVim(getEsrSystemInfo(vimId), vimId);
    }

    /**
     * @param vimId the identifier of the VIM
     * @return the VIM details
     */
    public EsrSystemInfo getEsrSystemInfo(String vimId) {
        try {
            return aaiRestApiProvider.getCloudInfrastructureApi().getCloudInfrastructureCloudRegionsCloudRegion(getCloudOwner(vimId), getRegionName(vimId), null, null).blockingFirst().getEsrSystemInfoList().get(0);
        } catch (Exception e) {
            throw buildFatalFailure(logger, "Unable to query VIM with " + vimId + " identifier from AAI", e);
        }
    }

    private VimInfo convertEsrToVim(EsrSystemInfo esrSystemInfo, String vimId) {
        VimInfo vimInfo = new VimInfo();
        vimInfo.setDescription(esrSystemInfo.getSystemName());
        vimInfo.setName(esrSystemInfo.getSystemName());
        vimInfo.setPassword(esrSystemInfo.getPassword());
        vimInfo.setStatus(esrSystemInfo.getSystemStatus());
        vimInfo.setType(esrSystemInfo.getType());
        vimInfo.setUrl(esrSystemInfo.getServiceUrl());
        vimInfo.setVersion(esrSystemInfo.getVersion());
        if (esrSystemInfo.getSslCacert() == null) {
            vimInfo.setSslInsecure("true");
        } else {
            vimInfo.setSslInsecure("false");
            vimInfo.setSslCacert(esrSystemInfo.getSslCacert());
        }
        vimInfo.setUserName(esrSystemInfo.getUserName());
        vimInfo.setVendor(esrSystemInfo.getVendor());
        vimInfo.setVimId(vimId);
        return vimInfo;
    }


    private VnfmInfo convertEsrToVnfmInfo(EsrVnfm vnfmInAai) {
        EsrSystemInfo esrSystemInfo = vnfmInAai.getEsrSystemInfoList().get(0);
        VnfmInfo vnfmInfo = new VnfmInfo();
        vnfmInfo.setPassword(esrSystemInfo.getPassword());
        vnfmInfo.setDescription(esrSystemInfo.getEsrSystemInfoId());
        vnfmInfo.setName(esrSystemInfo.getSystemName());
        vnfmInfo.setType(esrSystemInfo.getType());
        vnfmInfo.setUrl(esrSystemInfo.getServiceUrl());
        vnfmInfo.setVersion(esrSystemInfo.getVersion());
        vnfmInfo.setVimId(vnfmInAai.getVimId());
        vnfmInfo.setVendor(esrSystemInfo.getVendor());
        vnfmInfo.setUserName(esrSystemInfo.getUserName());
        vnfmInfo.setVnfmId(vnfmInAai.getVnfmId());
        return vnfmInfo;
    }

    @Override
    public Set<String> getVnfms() {
        EsrVnfmList esrVnfmList = aaiRestApiProvider.getExternalSystemApi().getExternalSystemEsrVnfmList().blockingFirst();
        return newHashSet(transform(esrVnfmList.getEsrVnfm(), esr -> esr.getVnfmId()));
    }
}
