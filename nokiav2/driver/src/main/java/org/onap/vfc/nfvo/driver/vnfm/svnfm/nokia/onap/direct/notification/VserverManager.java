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
import org.onap.aai.domain.yang.v11.*;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.spring.Conditions;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.DriverProperties;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.collect.Iterables.find;
import static java.lang.String.format;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIRestApiProvider.AAIService.CLOUD;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.childElement;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.LifecycleManager.getCloudOwner;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.LifecycleManager.getRegionName;

/**
 * Responsible for managing {@link Vserver} in AAI
 */
@Component
@Conditional(value = Conditions.UseForDirect.class)
class VserverManager extends AbstractManager {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(VserverManager.class);

    @Autowired
    VserverManager(AAIRestApiProvider aaiRestApiProvider, CbamRestApiProvider cbamRestApiProvider, DriverProperties driverProperties) {
        super(aaiRestApiProvider, cbamRestApiProvider, driverProperties);
    }

    static Relationship linkTo(String vimId, String tenantId, String serverProviderId) {
        Relationship relationship = new Relationship();
        relationship.setRelatedTo("vserver");
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
        String url = buildUrl(vimId, cbamVnfc);
        Vserver vserver = createOrGet(CLOUD, url, OBJECT_FACTORY.createVserver());
        updateFields(vserver, cbamVnfc, vnfId, affectedVirtualStorages, url, inMaintenance);
    }

    void delete(String vimId, com.nokia.cbam.lcm.v32.model.AffectedVnfc deletedVnfc) {
        aaiRestApiProvider.delete(logger, CLOUD, buildUrl(vimId, deletedVnfc));
    }

    private String buildUrl(String vimId, AffectedVnfc cbamVnfc) {
        String tenantId = getTenantId(cbamVnfc);
        String cloudOwner = getCloudOwner(vimId);
        String regionName = getRegionName(vimId);
        return format("/cloud-regions/cloud-region/%s/%s/tenants/tenant/%s/vservers/vserver/%s", cloudOwner, regionName, tenantId, cbamVnfc.getComputeResource().getResourceId());
    }

    private void updateFields(Vserver server, AffectedVnfc cbamVnfc, String vnfId, List<AffectedVirtualStorage> affectedVirtualStorages, String url, boolean inMaintenance) {
        server.setInMaint(inMaintenance);
        server.setIsClosedLoopDisabled(inMaintenance);
        JsonElement additionalData = new Gson().toJsonTree(cbamVnfc.getComputeResource().getAdditionalData());
        server.setVserverName(additionalData.getAsJsonObject().get("name").getAsString());
        server.setVserverId(cbamVnfc.getComputeResource().getResourceId());
        server.setProvStatus("active");
        server.setRelationshipList(new RelationshipList());
        server.setVserverId(cbamVnfc.getComputeResource().getResourceId());
        server.setVserverSelflink(extractSelfLink(cbamVnfc.getComputeResource().getAdditionalData()));
        addSingletonRelation(server.getRelationshipList(), GenericVnfManager.linkTo(vnfId));
        if (server.getVolumes() == null) {
            server.setVolumes(new Volumes());
        }
        if (cbamVnfc.getStorageResourceIds() != null) {
            for (String virtualStorageId : cbamVnfc.getStorageResourceIds()) {
                Volume volume = new Volume();
                AffectedVirtualStorage affectedStorage = find(affectedVirtualStorages, storage -> virtualStorageId.equals(storage.getId()));
                volume.setVolumeId(affectedStorage.getResource().getResourceId());
                server.getVolumes().getVolume().add(volume);
            }
        } else {
            server.setVolumes(OBJECT_FACTORY.createVolumes());
        }
        aaiRestApiProvider.put(logger, CLOUD, url, server, Void.class);
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
