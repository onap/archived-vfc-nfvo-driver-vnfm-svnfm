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

import com.nokia.cbam.lcm.v32.model.AffectedVirtualLink;
import io.reactivex.Observable;
import java.util.ArrayList;
import org.onap.aai.model.L3Network;
import org.onap.aai.model.Relationship;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProviderForSo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.SEPARATOR;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.LifecycleManager.getCloudOwner;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.LifecycleManager.getRegionName;

/**
 * Responsible for managing the {@link L3Network} in AAI
 */
@Component
class L3NetworkManager extends AbstractManager {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(L3NetworkManager.class);

    @Autowired
    L3NetworkManager(AAIRestApiProvider aaiRestApiProvider, CbamRestApiProviderForSo cbamRestApiProvider) {
        super(aaiRestApiProvider, cbamRestApiProvider);
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    void update(String vimId, String vnfId, AffectedVirtualLink affectedVirtualLink) {
        L3Network l3Network = createOrGet(getNetwork(vnfId, affectedVirtualLink), new L3Network());
        updateNetworkFields(vimId, vnfId, affectedVirtualLink, l3Network);
    }

    private Observable<L3Network> getNetwork(String vnfId, AffectedVirtualLink affectedVirtualLink) {
        return aaiRestApiProvider.getNetworkApi().getNetworkL3NetworksL3Network(buildNetworkId(vnfId, affectedVirtualLink), null, null, null, null, null, null, null, null, null);
    }

    void delete(String vnfId, AffectedVirtualLink removedVl) {
        L3Network l3Network = getNetwork(vnfId, removedVl).blockingFirst();
        aaiRestApiProvider.getNetworkApi().deleteNetworkL3NetworksL3Network(l3Network.getNetworkId(), l3Network.getResourceVersion()).blockingFirst();
    }

    private void updateNetworkFields(String vimId, String vnfId, AffectedVirtualLink affectedVirtualLink, L3Network network) {
        network.setNetworkId(buildNetworkId(vnfId, affectedVirtualLink));
        network.setNetworkName(extractMandatoryValue(affectedVirtualLink.getResource().getAdditionalData(), "name"));
        network.setNeutronNetworkId(affectedVirtualLink.getResource().getResourceId());
        network.setIsBoundToVpn(false);
        network.setIsExternalNetwork(false);
        network.setIsProviderNetwork(false);
        network.setIsSharedNetwork(false);
        network.setOperationalStatus("active");
        network.setOrchestrationStatus("active");
        if (network.getRelationshipList() == null) {
            network.setRelationshipList(new ArrayList<>());
        }
        addMissingRelation(network.getRelationshipList(), GenericVnfManager.linkTo(vnfId));
        addSingletonRelation(network.getRelationshipList(), getRegionLink(vimId));
        addSingletonRelation(network.getRelationshipList(), getTenantLink(vimId, extractMandatoryValue(affectedVirtualLink.getResource().getAdditionalData(), "tenantId")));
        aaiRestApiProvider.getNetworkApi().createOrUpdateNetworkL3NetworksL3Network(network.getNetworkId(), network).blockingFirst();
    }

    private String buildNetworkId(String vnfId, AffectedVirtualLink affectedVirtualLink) {
        return vnfId + SEPARATOR + affectedVirtualLink.getId();
    }

    private Relationship getRegionLink(String vimId) {
        Relationship relationship = new Relationship();
        relationship.setRelatedTo("cloud-region");
        relationship.setRelationshipData(new ArrayList<>());
        relationship.getRelationshipData().add(buildRelationshipData("cloud-region.cloud-owner", getCloudOwner(vimId)));
        relationship.getRelationshipData().add(buildRelationshipData("cloud-region.cloud-region-id", getRegionName(vimId)));
        return relationship;
    }

    private Relationship getTenantLink(String vimId, String tenantId) {
        Relationship relationship = new Relationship();
        relationship.setRelatedTo("tenant");
        relationship.setRelationshipData(new ArrayList<>());
        relationship.getRelationshipData().add(buildRelationshipData("cloud-region.cloud-owner", getCloudOwner(vimId)));
        relationship.getRelationshipData().add(buildRelationshipData("cloud-region.cloud-region-id", getRegionName(vimId)));
        relationship.getRelationshipData().add(buildRelationshipData("tenant.tenant-id", tenantId));
        return relationship;
    }
}