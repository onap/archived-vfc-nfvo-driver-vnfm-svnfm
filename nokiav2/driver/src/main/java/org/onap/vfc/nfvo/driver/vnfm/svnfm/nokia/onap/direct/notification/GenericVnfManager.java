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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.notification;

import com.nokia.cbam.lcm.v32.model.VnfInfo;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.onap.aai.model.GenericVnf;
import org.onap.aai.model.Relationship;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProviderForSo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core.SelfRegistrationManager.SERVICE_NAME;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.buildFatalFailure;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.SystemFunctions.systemFunctions;

/**
 * Responsible for managing the {@link GenericVnf} in AAI
 */
@Component
public class GenericVnfManager extends AbstractManager {
    private static final long MAX_MS_TO_WAIT_FOR_VNF_TO_APPEAR = 30 * 1000L;
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(GenericVnfManager.class);

    @Autowired
    GenericVnfManager(AAIRestApiProvider aaiRestApiProvider, CbamRestApiProviderForSo cbamRestApiProvider) {
        super(aaiRestApiProvider, cbamRestApiProvider);
    }

    static Relationship linkTo(String vnfId) {
        Relationship relationship = new Relationship();
        relationship.setRelatedTo("generic-vnf");
        relationship.setRelationshipData(new ArrayList<>());
        relationship.getRelationshipData().add(buildRelationshipData("generic-vnf.vnf-id", vnfId));
        return relationship;
    }

    private static Relationship linkToNs(String nsId) {
        Relationship relationship = new Relationship();
        relationship.setRelatedTo("service-instance");
        relationship.setRelationshipData(new ArrayList<>());
        relationship.getRelationshipData().add(buildRelationshipData("service-instance.service-instance-id", nsId));
        return relationship;
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    public void createOrUpdate(String vnfId, boolean inMaintenance, String vnfmId, Optional<String> nsId) {
        try {
            GenericVnf vnf = waitForVnfToAppearInAai(vnfId);
            updateFields(vnf, vnfId, inMaintenance, vnfmId, nsId);
        } catch (NoSuchElementException e) {
            try {
                logger.warn("The VNF with " + vnfId + " identifier did not appear in time", e);
                updateFields(new GenericVnf(), vnfId, inMaintenance, vnfmId, nsId);
            } catch (Exception e2) {
                logger.warn("The VNF with " + vnfId + " identifier has been created since after the maximal wait for VNF to appear timeout", e2);
                //the VNF might have been created since the last poll
                updateFields(getExistingVnf(vnfId), vnfId, inMaintenance, vnfmId, nsId);
            }
        }
    }

    private void updateFields(GenericVnf vnf, String vnfId, boolean inMaintenance, String vnfmId, Optional<String> nsId) {
        try {
            VnfInfo vnfInfo = cbamRestApiProvider.getCbamLcmApi(vnfmId).vnfsVnfInstanceIdGet(vnfId, CbamRestApiProvider.NOKIA_LCM_API_VERSION).blockingFirst();
            vnf.setVnfName(vnfInfo.getName());
        } catch (RuntimeException e) {
            throw buildFatalFailure(logger, "Unable to query VNF with " + vnfId + " identifier from CBAM", e);
        }
        vnf.setVnfId(vnfId);
        vnf.setInMaint(inMaintenance);
        vnf.setNfType(SERVICE_NAME);
        //FIXME whould be good to know if this parameter is relevant or not? (mandatory)
        vnf.setVnfType("NokiaVNF");
        vnf.setIsClosedLoopDisabled(inMaintenance);
        if (vnf.getRelationshipList() == null) {
            vnf.setRelationshipList(new ArrayList<>());
        }
        if (nsId.isPresent()) {
            addSingletonRelation(vnf.getRelationshipList(), linkToNs(nsId.get()));
        }
        aaiRestApiProvider.getNetworkApi().createOrUpdateNetworkGenericVnfsGenericVnf(vnf.getVnfId(), vnf).blockingFirst();
    }

    private GenericVnf waitForVnfToAppearInAai(String vnfId) {
        long timeoutInMs = systemFunctions().currentTimeMillis() + MAX_MS_TO_WAIT_FOR_VNF_TO_APPEAR;
        while (timeoutInMs - systemFunctions().currentTimeMillis() > 0) {
            try {
                return getExistingVnf(vnfId);
            } catch (NoSuchElementException e) {
                logger.debug("Unable to get VNF with " + vnfId + " identifier", e);
            }
            systemFunctions().sleep(3 * 1000L);
        }
        throw new NoSuchElementException();
    }

    private GenericVnf getExistingVnf(String vnfId) {
        return aaiRestApiProvider.getNetworkApi().getNetworkGenericVnfsGenericVnf(vnfId, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null).blockingFirst();
    }
}