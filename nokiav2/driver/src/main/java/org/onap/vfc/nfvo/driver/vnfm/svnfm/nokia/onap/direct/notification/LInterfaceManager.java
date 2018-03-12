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

import org.onap.aai.domain.yang.v11.L3InterfaceIpv4AddressList;
import org.onap.aai.domain.yang.v11.L3InterfaceIpv6AddressList;
import org.onap.aai.domain.yang.v11.LInterface;
import org.onap.aai.domain.yang.v11.RelationshipList;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.spring.Conditions;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.DriverProperties;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification.ReportedAffectedCp;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import static java.lang.String.format;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIRestApiProvider.AAIService.CLOUD;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.LifecycleManager.getCloudOwner;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.LifecycleManager.getRegionName;

/**
 * Responsible for managing the {@link LInterface} in AAI
 */
@Component
@Conditional(value = Conditions.UseForDirect.class)
class LInterfaceManager extends AbstractManager {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(LInterfaceManager.class);

    @Autowired
    LInterfaceManager(AAIRestApiProvider aaiRestApiProvider, CbamRestApiProvider cbamRestApiProvider, DriverProperties driverProperties) {
        super(aaiRestApiProvider, cbamRestApiProvider, driverProperties);
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
        LInterface lInterface = createOrGet(CLOUD, buildUrl(vimId, affectedCp), OBJECT_FACTORY.createLInterface());
        updateFields(lInterface, affectedCp, vnfId, buildUrl(vimId, affectedCp), inMaintenance);
    }

    void delete(String vimId, ReportedAffectedCp removedCp) {
        aaiRestApiProvider.delete(logger, AAIRestApiProvider.AAIService.CLOUD, buildUrl(vimId, removedCp));
    }

    private void updateFields(LInterface logicalInterface, ReportedAffectedCp affectedCp, String vnfId, String url, boolean inMaintenance) {
        logicalInterface.setInMaint(inMaintenance);
        logicalInterface.setIsIpUnnumbered(false);
        logicalInterface.setIsPortMirrored(false);
        logicalInterface.setInterfaceName(affectedCp.getName());
        logicalInterface.setInterfaceId(affectedCp.getCpId());
        logicalInterface.setInterfaceRole(affectedCp.getCpdId());
        logicalInterface.setMacaddr(affectedCp.getMacAddress());
        logicalInterface.setProvStatus("active");
        if (affectedCp.getIpAddress() != null) {
            if (affectedCp.getIpAddress().contains(":")) {
                L3InterfaceIpv6AddressList ipv6Address = OBJECT_FACTORY.createL3InterfaceIpv6AddressList();
                ipv6Address.setL3InterfaceIpv6Address(affectedCp.getIpAddress());
                ipv6Address.setNeutronNetworkId(affectedCp.getNetworkProviderId());
                logicalInterface.getL3InterfaceIpv6AddressList().add(ipv6Address);
            } else {
                L3InterfaceIpv4AddressList ipv4Address = OBJECT_FACTORY.createL3InterfaceIpv4AddressList();
                ipv4Address.setL3InterfaceIpv4Address(affectedCp.getIpAddress());
                ipv4Address.setNeutronNetworkId(affectedCp.getNetworkProviderId());
                logicalInterface.getL3InterfaceIpv4AddressList().add(ipv4Address);
            }
        }
        if (logicalInterface.getRelationshipList() == null) {
            logicalInterface.setRelationshipList(new RelationshipList());
        }
        addSingletonRelation(logicalInterface.getRelationshipList(), GenericVnfManager.linkTo(vnfId));
        aaiRestApiProvider.put(logger, CLOUD, url, logicalInterface, Void.class);
    }
}
