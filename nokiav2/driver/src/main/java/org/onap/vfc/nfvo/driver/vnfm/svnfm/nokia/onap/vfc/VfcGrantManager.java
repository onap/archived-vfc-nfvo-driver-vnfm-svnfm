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

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nokia.cbam.lcm.v32.model.VnfInfo;
import com.nokia.cbam.lcm.v32.model.VnfcResourceInfo;
import java.util.*;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api.IGrantManager;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.spring.Conditions;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CatalogManager;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProvider;
import org.onap.vnfmdriver.model.*;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import static com.nokia.cbam.lcm.v32.model.InstantiationState.INSTANTIATED;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.buildFatalFailure;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.child;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProvider.NOKIA_LCM_API_VERSION;
import static org.onap.vnfmdriver.model.OperationType.TERMINAL;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Responsible for handling granting before the execution of a VNF operation
 */
@Component
@Conditional(value = Conditions.UseForVfc.class)
public class VfcGrantManager implements IGrantManager {
    private static Logger logger = getLogger(VfcGrantManager.class);
    private final CatalogManager catalogManager;
    private final CbamRestApiProvider cbamRestApiProvider;
    private final VfcRestApiProvider vfcRestApiProvider;

    @Autowired
    VfcGrantManager(CatalogManager catalogManager, CbamRestApiProvider cbamRestApiProvider, VfcRestApiProvider vfcRestApiProvider) {
        this.catalogManager = catalogManager;
        this.cbamRestApiProvider = cbamRestApiProvider;
        this.vfcRestApiProvider = vfcRestApiProvider;
    }

    @Override
    public void requestGrantForHeal(String vnfmId, String vnfId, String vimId, String onapCsarId, VnfHealRequest request, String jobId) {
        GrantVNFRequest grantRequest = buildGrantRequest(vnfmId, vimId, onapCsarId, jobId, OperationType.HEAL);
        ResourceChange resourceChange = new ResourceChange();
        resourceChange.setType(ChangeType.VDU);
        resourceChange.setVdu(request.getAffectedvm().getVduid());
        resourceChange.setResourceDefinitionId(UUID.randomUUID().toString());
        grantRequest.getRemoveResource().add(resourceChange);
        grantRequest.getAddResource().add(resourceChange);
        grantRequest.setVnfInstanceId(vnfId);
        requestGrant(grantRequest);
    }

    @Override
    public void requestGrantForScale(String vnfmId, String vnfId, String vimId, String onapCsarId, VnfScaleRequest request, String jobId) {
        String cbamVnfdId;
        try {
            com.nokia.cbam.lcm.v32.model.VnfInfo vnf = cbamRestApiProvider.getCbamLcmApi(vnfmId).vnfsVnfInstanceIdGet(vnfId, NOKIA_LCM_API_VERSION).blockingFirst();
            cbamVnfdId = vnf.getVnfdId();
        } catch (Exception e) {
            throw buildFatalFailure(logger, "Unable to query VNF " + vnfId, e);
        }
        OperationType operationType = ScaleDirection.IN.equals(request.getType()) ? OperationType.SCALEIN : OperationType.SCALEOUT;
        GrantVNFRequest grantRequest = buildGrantRequest(vnfmId, vimId, onapCsarId, jobId, operationType);
        String vnfdContent = catalogManager.getCbamVnfdContent(vnfmId, cbamVnfdId);
        Set<ResourceChange> resourceChanges = calculateResourceChangeDuringScaling(vnfdContent, request.getAspectId(), Integer.parseInt(request.getNumberOfSteps()));
        if (request.getType() == ScaleDirection.IN) {
            grantRequest.getRemoveResource().addAll(resourceChanges);

        } else {
            grantRequest.getAddResource().addAll(resourceChanges);
        }
        grantRequest.setVnfInstanceId(vnfId);
        requestGrant(grantRequest);
    }

    @Override
    public void requestGrantForTerminate(String vnfmId, String vnfId, String vimId, String onapVnfdId, VnfInfo vnf, String jobId) {
        if (vnf.getInstantiationState() == INSTANTIATED) {
            GrantVNFRequest grantRequest;
            try {
                grantRequest = buildGrantRequest(vnfmId, vimId, onapVnfdId, jobId, TERMINAL);
                grantRequest.setVnfInstanceId(vnfId);
                addVnfcsToGrant(vnf, grantRequest);
            } catch (Exception e) {
                throw buildFatalFailure(logger, "Unable to prepare grant request for termination", e);
            }
            requestGrant(grantRequest);
        }
    }

    private void addVnfcsToGrant(VnfInfo vnf, GrantVNFRequest grantRequest) {
        //VNF is instantiated but has no VNFC
        if (vnf.getInstantiatedVnfInfo().getVnfcResourceInfo() != null) {
            for (VnfcResourceInfo vnfc : vnf.getInstantiatedVnfInfo().getVnfcResourceInfo()) {
                ResourceChange resourceChange = new ResourceChange();
                grantRequest.getRemoveResource().add(resourceChange);
                resourceChange.setVdu(vnfc.getVduId());
                resourceChange.setType(ChangeType.VDU);
                resourceChange.setResourceDefinitionId(UUID.randomUUID().toString());
            }
        }
    }

    @Override
    public GrantVNFResponseVim requestGrantForInstantiate(String vnfmId, String vnfId, String vimId, String onapVnfdId, String instantiationLevelId, String cbamVnfdContent, String jobId) {
        GrantVNFRequest grantRequest;
        try {
            grantRequest = buildGrantRequest(vnfmId, vimId, onapVnfdId, jobId, OperationType.INSTANTIATE);
            grantRequest.setVnfInstanceId(vnfId);
            grantRequest.setAddResource(new ArrayList<>());
            grantRequest.getAddResource().addAll(calculateResourceChangeDuringInstantiate(cbamVnfdContent, instantiationLevelId));
        } catch (Exception e) {
            throw buildFatalFailure(logger, "Unable to prepare grant request for instantiation", e);
        }
        return requestGrant(grantRequest);
    }

    private GrantVNFRequest buildGrantRequest(String vnfmId, String vimId, String onapCsarId, String jobId, OperationType operationType) {
        GrantVNFRequest grantVNFRequest = new GrantVNFRequest();
        //FIXME
        //Currently the grant request sent to VF-C must contain the VIM identifier in the
        //grant response (normally in ETSI VIM identifier is received in the grant response
        //from ETSI orchestrator the vimId parameter should be removed from this POJO
        //to be able to fix this https://jira.onap.org/browse/VFC-603 must be solved
        //the vimId should be removed from the AdditionalGrantParams structure
        grantVNFRequest.setAdditionalParam(new AdditionalGrantParams(vnfmId, vimId));
        grantVNFRequest.setVnfDescriptorId(onapCsarId);
        grantVNFRequest.setJobId(jobId);
        grantVNFRequest.setLifecycleOperation(operationType);
        grantVNFRequest.setAddResource(new ArrayList<>());
        grantVNFRequest.setRemoveResource(new ArrayList<>());
        return grantVNFRequest;
    }

    private GrantVNFResponseVim requestGrant(GrantVNFRequest grantRequest) {
        try {
            logger.info("Requesting grant with ", grantRequest);
            GrantVNFResponse grantVNFResponse = vfcRestApiProvider.getNsLcmApi().grantvnf(grantRequest).blockingFirst();
            logger.info("Successfully received grant {}", grantVNFResponse);
            return grantVNFResponse.getVim();
        } catch (Exception e) {
            throw buildFatalFailure(logger, "Unable to request grant with " + grantRequest, e);
        }
    }

    private Set<ResourceChange> calculateResourceChangeDuringInstantiate(String cbamVnfdContent, String instantiationLevelId) {
        JsonObject root = new Gson().toJsonTree(new Yaml().load(cbamVnfdContent)).getAsJsonObject();
        JsonObject capabilities = child(child(child(root, "topology_template"), "substitution_mappings"), "capabilities");
        JsonObject deploymentFlavorProperties = child(child(capabilities, "deployment_flavour"), "properties");
        JsonObject instantiationLevels = child(deploymentFlavorProperties, "instantiation_levels");
        Set<ResourceChange> resourceChanges = new HashSet<>();
        for (Map.Entry<String, JsonElement> vdu_level : child(child(instantiationLevels, instantiationLevelId), ("vdu_levels")).entrySet()) {
            JsonElement numberOfInstances = vdu_level.getValue().getAsJsonObject().get("number_of_instances");
            for (int i = 0; i < numberOfInstances.getAsLong(); i++) {
                ResourceChange resourceChange = new ResourceChange();
                resourceChanges.add(resourceChange);
                resourceChange.setVdu(vdu_level.getKey());
                resourceChange.setType(ChangeType.VDU);
                resourceChange.setResourceDefinitionId(UUID.randomUUID().toString());
            }
        }
        return resourceChanges;
    }

    private Set<ResourceChange> calculateResourceChangeDuringScaling(String vnfdContent, String aspectId, int steps) {
        JsonObject root = new Gson().toJsonTree(new Yaml().load(vnfdContent)).getAsJsonObject();
        Set<ResourceChange> resourceChanges = new HashSet<>();
        JsonArray policies = child(root, "topology_template").getAsJsonObject().get("policies").getAsJsonArray();
        for (JsonElement policy : policies) {
            if ("heat_mapping".equals(policy.getAsJsonObject().entrySet().iterator().next().getKey())) {
                JsonObject aspects = policy.getAsJsonObject().entrySet().iterator().next().getValue().getAsJsonObject().get("properties").getAsJsonObject().get("aspects").getAsJsonObject();
                JsonObject aspect = child(aspects, aspectId);
                if (aspect.has("vdus")) {
                    addChangesForAspect(steps, resourceChanges, aspect);
                }
            }
        }
        return resourceChanges;
    }

    private void addChangesForAspect(int steps, Set<ResourceChange> resourceChanges, JsonObject aspect) {
        for (Map.Entry<String, JsonElement> vdu : aspect.get("vdus").getAsJsonObject().entrySet()) {
            String vduId = vdu.getKey();
            for (int step = 0; step < steps; step++) {
                for (int i = 0; i < vdu.getValue().getAsJsonArray().size(); i++) {
                    ResourceChange resourceChange = new ResourceChange();
                    resourceChange.setVdu(vduId);
                    resourceChange.setType(ChangeType.VDU);
                    resourceChange.setResourceDefinitionId(UUID.randomUUID().toString());
                    resourceChanges.add(resourceChange);
                }
            }
        }
    }

    /**
     * Represents the mandatory parameters that must be sent during grant request to VF-C
     */
    @VisibleForTesting
    static class AdditionalGrantParams {
        private final String vnfmId;
        private final String vimId;

        AdditionalGrantParams(String vnfmId, String vimId) {
            this.vnfmId = vnfmId;
            this.vimId = vimId;
        }

        /**
         * @return the identifier of the VNFM requesting the grant
         */
        public String getVnfmId() {
            return vnfmId;
        }

        /**
         * @return the identifier of the VIM for which the grant is requested
         */
        public String getVimId() {
            return vimId;
        }
    }
}
