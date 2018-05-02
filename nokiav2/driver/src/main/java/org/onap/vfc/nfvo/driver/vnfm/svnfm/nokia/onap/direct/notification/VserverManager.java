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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nokia.cbam.lcm.v32.model.AffectedVirtualStorage;
import com.nokia.cbam.lcm.v32.model.AffectedVnfc;
import io.reactivex.Observable;
import java.util.ArrayList;
import java.util.List;
import org.onap.aai.model.Relationship;
import org.onap.aai.model.Volume;
import org.onap.aai.model.Vserver;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProviderForSo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.google.common.collect.Iterables.find;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.childElement;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.LifecycleManager.getCloudOwner;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.LifecycleManager.getRegionName;

/**
 * Responsible for managing {@link Vserver} in AAI
 */
@Component
class VserverManager extends AbstractManager {
    private static Logger logger = LoggerFactory.getLogger(AbstractManager.class);

    @Autowired
    VserverManager(AAIRestApiProvider aaiRestApiProvider, CbamRestApiProviderForSo cbamRestApiProvider) {
        super(aaiRestApiProvider, cbamRestApiProvider);
    }

    static Relationship linkTo(String vimId, String tenantId, String serverProviderId) {
        Relationship relationship = new Relationship();
        relationship.setRelatedTo("vserver");
        relationship.setRelationshipData(new ArrayList<>());
        relationship.getRelationshipData().add(buildRelationshipData("cloud-region.cloud-owner", getCloudOwner(vimId)));
        relationship.getRelationshipData().add(buildRelationshipData("cloud-region.cloud-region-id", getRegionName(vimId)));
        relationship.getRelationshipData().add(buildRelationshipData("tenant.tenant-id", tenantId));
        relationship.getRelationshipData().add(buildRelationshipData("vserver.vserver-id", serverProviderId));
        return relationship;
    }

    static String getTenantId(AffectedVnfc cbamVnfc) {
        return extractMandatoryValue(cbamVnfc.getComputeResource().getAdditionalData(), "tenantId");
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    void update(String vimId, String vnfId, AffectedVnfc cbamVnfc, List<AffectedVirtualStorage> affectedVirtualStorages, boolean inMaintenance) {
        Vserver vserver = createOrGet(getVserver(vimId, cbamVnfc), new Vserver());
        updateFields(vimId, vserver, cbamVnfc, vnfId, affectedVirtualStorages, inMaintenance);
    }

    void delete(String vimId, AffectedVnfc deletedVnfc) {
        String tenantId = getTenantId(deletedVnfc);
        String cloudOwner = getCloudOwner(vimId);
        String regionName = getRegionName(vimId);
        Vserver vserver = getVserver(vimId, deletedVnfc).blockingFirst();
        aaiRestApiProvider.getCloudInfrastructureApi().deleteCloudInfrastructureCloudRegionsCloudRegionTenantsTenantVserversVserver(cloudOwner, regionName, tenantId, vserver.getVserverId(), vserver.getResourceVersion());
    }

    private Observable<Vserver> getVserver(String vimId, AffectedVnfc cbamVnfc) {
        String tenantId = getTenantId(cbamVnfc);
        String cloudOwner = getCloudOwner(vimId);
        String regionName = getRegionName(vimId);
        return aaiRestApiProvider.getCloudInfrastructureApi().getCloudInfrastructureCloudRegionsCloudRegionTenantsTenantVserversVserver(cloudOwner, regionName, tenantId, cbamVnfc.getComputeResource().getResourceId(), null, null, null, null, null, null, null, null, null);
    }

    private void updateFields(String vimId, Vserver server, AffectedVnfc cbamVnfc, String vnfId, List<AffectedVirtualStorage> affectedVirtualStorages, boolean inMaintenance) {
        server.setInMaint(inMaintenance);
        server.setIsClosedLoopDisabled(inMaintenance);
        JsonElement additionalData = new Gson().toJsonTree(cbamVnfc.getComputeResource().getAdditionalData());
        server.setVserverName(additionalData.getAsJsonObject().get("name").getAsString());
        server.setVserverId(cbamVnfc.getComputeResource().getResourceId());
        server.setProvStatus("active");
        server.setVserverId(cbamVnfc.getComputeResource().getResourceId());
        server.setVserverSelflink(extractSelfLink(cbamVnfc.getComputeResource().getAdditionalData()));
        if (server.getRelationshipList() == null) {
            server.setRelationshipList(new ArrayList<>());
        }
        addSingletonRelation(server.getRelationshipList(), GenericVnfManager.linkTo(vnfId));
        server.setVolumes(new ArrayList<>());
        if (cbamVnfc.getStorageResourceIds() != null) {
            for (String virtualStorageId : cbamVnfc.getStorageResourceIds()) {
                Volume volume = new Volume();
                AffectedVirtualStorage affectedStorage = find(affectedVirtualStorages, storage -> virtualStorageId.equals(storage.getId()));
                volume.setVolumeId(affectedStorage.getResource().getResourceId());
                server.getVolumes().add(volume);
            }
        }
        String tenantId = getTenantId(cbamVnfc);
        String cloudOwner = getCloudOwner(vimId);
        String regionName = getRegionName(vimId);
        aaiRestApiProvider.getCloudInfrastructureApi().createOrUpdateCloudInfrastructureCloudRegionsCloudRegionTenantsTenantVserversVserver(cloudOwner, regionName, tenantId, server.getVserverId(), server).blockingFirst();
    }

    private String extractSelfLink(Object additionalData) {
        try {
            JsonObject root = new Gson().toJsonTree(additionalData).getAsJsonObject();
            for (JsonElement link : childElement(root, "links").getAsJsonArray()) {
                if (link.getAsJsonObject().has("rel") && "self".equals(link.getAsJsonObject().get("rel").getAsString())) {
                    return link.getAsJsonObject().get("href").getAsString();
                }
            }
            return "unknown";
        } catch (Exception e) {
            logger.debug("Missing links in the server", e);
            return "unknown";
        }
    }
}
