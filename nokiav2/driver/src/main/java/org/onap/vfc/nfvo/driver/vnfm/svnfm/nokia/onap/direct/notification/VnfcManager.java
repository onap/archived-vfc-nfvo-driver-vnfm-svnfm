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

import io.reactivex.Observable;
import java.util.ArrayList;
import org.onap.aai.model.Vnfc;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProviderForSo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.google.common.base.Splitter.on;
import static com.google.common.collect.Lists.newArrayList;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.SEPARATOR;

/**
 * Responsible for managing {@link Vnfc} in AAI
 */
@Component
public class VnfcManager extends AbstractManager {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(VnfcManager.class);

    @Autowired
    VnfcManager(AAIRestApiProvider aaiRestApiProvider, CbamRestApiProviderForSo cbamRestApiProvider) {
        super(aaiRestApiProvider, cbamRestApiProvider);
    }

    private static String buildId(String vnfId, String cbamVnfcId) {
        return vnfId + SEPARATOR + cbamVnfcId;
    }

    /**
     * @param onapVnfcId the identifier of the VNFC in AAI
     * @return the identifier of the VNFC in CBAM
     */
    public static String buildCbamId(String onapVnfcId) {
        return newArrayList(on(SEPARATOR).split(onapVnfcId)).get(1);
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    void delete(String vnfId, com.nokia.cbam.lcm.v32.model.AffectedVnfc cbamVnfc) {
        Vnfc vnfc = getVnfc(buildId(vnfId, cbamVnfc.getId())).blockingFirst();
        aaiRestApiProvider.getNetworkApi().deleteNetworkVnfcsVnfc(vnfc.getVnfcName(), vnfc.getResourceVersion()).blockingFirst();
    }

    private Observable<Vnfc> getVnfc(String vnfcId) {
        return aaiRestApiProvider.getNetworkApi().getNetworkVnfcsVnfc(vnfcId, null, null, null, null, null, null, null, null, null);
    }

    void update(String vimId, String tenantId, String vnfId, com.nokia.cbam.lcm.v32.model.AffectedVnfc cbamVnfc, boolean inMaintenance) {
        Vnfc vnfc = createOrGet(getVnfc(buildId(vnfId, cbamVnfc.getId())), new Vnfc());
        updateFields(vimId, tenantId, vnfc, cbamVnfc, vnfId, inMaintenance);
    }

    private void updateFields(String vimId, String tenantId, Vnfc aaiVnfc, com.nokia.cbam.lcm.v32.model.AffectedVnfc cbamVnfc, String vnfId, boolean inMaintenance) {
        aaiVnfc.setInMaint(inMaintenance);
        aaiVnfc.setIsClosedLoopDisabled(inMaintenance);
        //FIXME would be good to know what is this mandatory parameter
        aaiVnfc.setNfcFunction(cbamVnfc.getId());
        //FIXME would be good to know what is this mandatory parameter
        aaiVnfc.setNfcNamingCode(cbamVnfc.getId());
        aaiVnfc.setVnfcName(buildId(vnfId, cbamVnfc.getId()));
        if (aaiVnfc.getRelationshipList() == null) {
            aaiVnfc.setRelationshipList(new ArrayList<>());
        }
        addSingletonRelation(aaiVnfc.getRelationshipList(), VserverManager.linkTo(vimId, tenantId, cbamVnfc.getComputeResource().getResourceId()));
        addSingletonRelation(aaiVnfc.getRelationshipList(), GenericVnfManager.linkTo(vnfId));
        aaiRestApiProvider.getNetworkApi().createOrUpdateNetworkVnfcsVnfc(aaiVnfc.getVnfcName(), aaiVnfc).blockingFirst();
    }
}