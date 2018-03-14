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

import com.google.common.base.Splitter;
import org.onap.aai.domain.yang.v11.RelationshipList;
import org.onap.aai.domain.yang.v11.Vnfc;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.spring.Conditions;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.DriverProperties;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import static java.lang.String.format;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIRestApiProvider.AAIService.NETWORK;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.SEPARATOR;

/**
 * Responsible for managing {@link Vnfc} in AAI
 */
@Component
@Conditional(value = Conditions.UseForDirect.class)
public class VnfcManager extends AbstractManager {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(VnfcManager.class);

    @Autowired
    VnfcManager(AAIRestApiProvider aaiRestApiProvider, CbamRestApiProvider cbamRestApiProvider, DriverProperties driverProperties) {
        super(aaiRestApiProvider, cbamRestApiProvider, driverProperties);
    }

    public static String buildUrl(String vnfId, String cbamVnfcId) {
        return format("/vnfcs/vnfc/%s", buildId(vnfId, cbamVnfcId));
    }

    public static String getCbamVnfcId(String vnfcId) {
        String vnfId = Splitter.on(CbamUtils.SEPARATOR).split(vnfcId).iterator().next();
        return vnfcId.replaceFirst(vnfId + SEPARATOR, "");
    }

    private static String buildId(String vnfId, String cbamVnfcId) {
        return vnfId + SEPARATOR + cbamVnfcId;
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    void delete(String vnfId, com.nokia.cbam.lcm.v32.model.AffectedVnfc cbamVnfc) {
        aaiRestApiProvider.delete(logger, NETWORK, buildUrl(vnfId, cbamVnfc.getId()));
    }

    void update(String vimId, String tenantId, String vnfId, com.nokia.cbam.lcm.v32.model.AffectedVnfc cbamVnfc, boolean inMaintenance) {
        String url = buildUrl(vnfId, cbamVnfc.getId());
        Vnfc vnfc = createOrGet(NETWORK, url, OBJECT_FACTORY.createVnfc());
        updateFields(vimId, tenantId, vnfc, cbamVnfc, vnfId, url, inMaintenance);
    }

    private void updateFields(String vimId, String tenantId, Vnfc aaiVnfc, com.nokia.cbam.lcm.v32.model.AffectedVnfc cbamVnfc, String vnfId, String url, boolean inMaintenance) {
        aaiVnfc.setInMaint(inMaintenance);
        aaiVnfc.setIsClosedLoopDisabled(inMaintenance);
        //FIXME would be good to know what is this mandatory parameter
        aaiVnfc.setNfcFunction(cbamVnfc.getId());
        //FIXME would be good to know what is this mandatory parameter
        aaiVnfc.setNfcNamingCode(cbamVnfc.getId());
        aaiVnfc.setVnfcName(buildId(vnfId, cbamVnfc.getId()));
        aaiVnfc.setRelationshipList(new RelationshipList());
        addSingletonRelation(aaiVnfc.getRelationshipList(), VserverManager.linkTo(vimId, tenantId, cbamVnfc.getComputeResource().getResourceId()));
        addSingletonRelation(aaiVnfc.getRelationshipList(), GenericVnfManager.linkTo(vnfId));
        aaiRestApiProvider.put(logger, NETWORK, url, aaiVnfc, Void.class);
    }
}