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
import org.onap.aai.model.L3InterfaceIpv4AddressList;
import org.onap.aai.model.L3InterfaceIpv6AddressList;
import org.onap.aai.model.LInterface;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProviderForSo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification.ReportedAffectedCp;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.lang.String.format;

import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.LifecycleManager.getCloudOwner;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.LifecycleManager.getRegionName;

/**
 * Responsible for managing the {@link LInterface} in AAI
 */
@Component
class LInterfaceManager extends AbstractManager {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(LInterfaceManager.class);

    @Autowired
    LInterfaceManager(AAIRestApiProvider aaiRestApiProvider, CbamRestApiProviderForSo cbamRestApiProvider) {
        super(aaiRestApiProvider, cbamRestApiProvider);
    }

    static String buildUrl(String vimId, ReportedAffectedCp affectedCp) {
        String cloudOwner = getCloudOwner(vimId);
        String regionName = getRegionName(vimId);
        String tenantId = affectedCp.getTenantId();
        String vServerId = affectedCp.getServerProviderId();
        String cpId = affectedCp.getCpId();
        return format("/cloud-regions/cloud-region/%s/%s/tenants/tenant/%s/vservers/vserver/%s/l-interfaces/l-interface/%s", cloudOwner, regionName, tenantId, vServerId, cpId);
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    void update(String vnfId, String vimId, ReportedAffectedCp affectedCp, boolean inMaintenance) {
        LInterface lInterface = createOrGet(getLinterface(vimId, affectedCp), new LInterface());
        updateFields(vimId, lInterface, affectedCp, vnfId, inMaintenance);
    }

    void delete(String vimId, ReportedAffectedCp removedCp) {
        LInterface linterface = getLinterface(vimId, removedCp).blockingFirst();
        String cloudOwner = getCloudOwner(vimId);
        String regionName = getRegionName(vimId);
        String tenantId = removedCp.getTenantId();
        String vServerId = removedCp.getServerProviderId();
        String cpId = removedCp.getCpId();
        aaiRestApiProvider.getCloudInfrastructureApi().deleteCloudInfrastructureCloudRegionsCloudRegionTenantsTenantVserversVserverLInterfacesLInterface(cloudOwner, regionName, tenantId, vServerId, cpId, linterface.getResourceVersion());
    }

    private Observable<LInterface> getLinterface(String vimId, ReportedAffectedCp cp) {
        String cloudOwner = getCloudOwner(vimId);
        String regionName = getRegionName(vimId);
        String tenantId = cp.getTenantId();
        String vServerId = cp.getServerProviderId();
        String cpId = cp.getCpId();
        return aaiRestApiProvider.getCloudInfrastructureApi().getCloudInfrastructureCloudRegionsCloudRegionTenantsTenantVserversVserverLInterfacesLInterface(cloudOwner, regionName, tenantId, vServerId, cpId, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    private void updateFields(String vimId, LInterface logicalInterface, ReportedAffectedCp affectedCp, String vnfId, boolean inMaintenance) {
        logicalInterface.setInMaint(inMaintenance);
        logicalInterface.setIsIpUnnumbered(false);
        logicalInterface.setIsPortMirrored(false);
        logicalInterface.setInterfaceName(affectedCp.getName());
        logicalInterface.setInterfaceId(affectedCp.getCpId());
        logicalInterface.setInterfaceRole(affectedCp.getCpdId());
        logicalInterface.setMacaddr(affectedCp.getMacAddress());
        logicalInterface.setProvStatus("active");
        logicalInterface.setL3InterfaceIpv6AddressList(new ArrayList<>());
        logicalInterface.setL3InterfaceIpv4AddressList(new ArrayList<>());
        if (affectedCp.getIpAddress() != null) {
            if (affectedCp.getIpAddress().contains(":")) {
                L3InterfaceIpv6AddressList ipv6Address = new L3InterfaceIpv6AddressList();
                ipv6Address.setL3InterfaceIpv6Address(affectedCp.getIpAddress());
                ipv6Address.setNeutronNetworkId(affectedCp.getNetworkProviderId());
                logicalInterface.getL3InterfaceIpv6AddressList().add(ipv6Address);
            } else {
                L3InterfaceIpv4AddressList ipv4Address = new L3InterfaceIpv4AddressList();
                ipv4Address.setL3InterfaceIpv4Address(affectedCp.getIpAddress());
                ipv4Address.setNeutronNetworkId(affectedCp.getNetworkProviderId());
                logicalInterface.getL3InterfaceIpv4AddressList().add(ipv4Address);
            }
        }
        if (logicalInterface.getRelationshipList() == null) {
            logicalInterface.setRelationshipList(new ArrayList<>());
        }
        addSingletonRelation(logicalInterface.getRelationshipList(), GenericVnfManager.linkTo(vnfId));
        String cloudOwner = getCloudOwner(vimId);
        String regionName = getRegionName(vimId);
        String tenantId = affectedCp.getTenantId();
        String vServerId = affectedCp.getServerProviderId();
        String cpId = affectedCp.getCpId();
        aaiRestApiProvider.getCloudInfrastructureApi().createOrUpdateCloudInfrastructureCloudRegionsCloudRegionTenantsTenantVserversVserverLInterfacesLInterface(cloudOwner, regionName, tenantId, vServerId, cpId, logicalInterface).blockingFirst();
    }
}
