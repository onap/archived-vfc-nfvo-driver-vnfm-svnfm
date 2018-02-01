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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nokia.cbam.lcm.v32.ApiException;
import com.nokia.cbam.lcm.v32.model.VnfInfo;
import com.nokia.cbam.lcm.v32.model.VnfcResourceInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.RestApiProvider;
import org.onap.vnfmdriver.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.RestApiProvider.NOKIA_LCM_API_VERSION;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Responsible for handling granting before the execution of a VNF operation
 */
@Component
public class GrantManager {
    private static org.slf4j.Logger logger = getLogger(GrantManager.class);

    @Autowired
    private CbamCatalogManager catalogManager;
    @Autowired
    private RestApiProvider restApiProvider;

    /**
     * Request grant for healing
     * - the affected virtual machine is added twice to the grant request (add & remove) to
     * signal that it is temporary removed
     * - the grant response is only used make a binary decision
     *
     * @param vnfmId  the identifier of the VNFM
     * @param vnfId   the identifier of the VNF
     * @param vimId   the identifier of the VIM
     * @param request the heal request
     * @param jobId   the identifier of the job that triggered the grant
     */
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

    private GrantVNFRequest buildGrantRequest(String vnfmId, String vimId, String onapCsarId, String jobId, OperationType operationType) {
        //FIXME the vimId should not be required for grant request see VFC-603 issue
        GrantVNFRequest grantVNFRequest = new GrantVNFRequest();
        grantVNFRequest.setAdditionalParam(new AdditionalGrantParams(vnfmId, vimId));
        grantVNFRequest.setVnfDescriptorId(onapCsarId);
        grantVNFRequest.setJobId(jobId);
        grantVNFRequest.setLifecycleOperation(operationType);
        return grantVNFRequest;
    }

    /**
     * Request grant for scaling
     * - the affected virtual machines are calculated from the Heat mapping section of the corresponding aspect
     * - the grant response is only used make a binary decision
     *
     * @param vnfmId     the identifier of the VNFM
     * @param vnfId      the identifier of the VNF
     * @param vimId      the identifier of the VIM
     * @param onapCsarId the CSAR ID of the ONAP
     * @param request    the scaling request
     * @param jobId      the identifier of the job that triggered the grant
     */
    public void requestGrantForScale(String vnfmId, String vnfId, String vimId, String onapCsarId, VnfScaleRequest request, String jobId) {
        try {
            OperationType operationType = ScaleDirection.IN.equals(request.getType()) ? OperationType.SCALEIN : OperationType.SCALEOUT;
            GrantVNFRequest grantRequest = buildGrantRequest(vnfmId, vimId, onapCsarId, jobId, operationType);
            com.nokia.cbam.lcm.v32.model.VnfInfo vnf = restApiProvider.getCbamLcmApi(vnfmId).vnfsVnfInstanceIdGet(vnfId, NOKIA_LCM_API_VERSION);
            String vnfdContent = catalogManager.getCbamVnfdContent(vnfmId, vnf.getVnfdId());
            Set<ResourceChange> resourceChanges = calculateResourceChangeDuringScaling(vnfdContent, request.getAspectId(), Integer.parseInt(request.getNumberOfSteps()));
            switch (request.getType()) {
                case IN:
                    grantRequest.getRemoveResource().addAll(resourceChanges);
                    break;
                case OUT:
                    grantRequest.getAddResource().addAll(resourceChanges);
                    break;
            }
            grantRequest.setVnfInstanceId(vnfId);
            requestGrant(grantRequest);
        } catch (ApiException e) {
            logger.error("Unable to query VNF " + vnfId, e);
            throw new RuntimeException("Unable to query VNF " + vnfId, e);
        }
    }

    /**
     * Request grant for termination
     * - the resources removed is the previously deployed resources based on VNF query
     * - the grant response is only used make a binary decision
     *
     * @param vnfmId the identifier of the VNFM
     * @param vnfId  the identifier of the VNF
     * @param vimId  the identifier of the VIM
     */
    public void requestGrantForTerminate(String vnfmId, String vnfId, String vimId, String onapVnfdId, VnfInfo vnf, String jobId) {
        switch (vnf.getInstantiationState()) {
            case NOT_INSTANTIATED:
                break;
            case INSTANTIATED:
                GrantVNFRequest grantRequest;
                try {
                    grantRequest = buildGrantRequest(vnfmId, vimId, onapVnfdId, jobId, OperationType.TERMINAL);
                    for (VnfcResourceInfo vnfc : vnf.getInstantiatedVnfInfo().getVnfcResourceInfo()) {
                        ResourceChange resourceChange = new ResourceChange();
                        grantRequest.getRemoveResource().add(resourceChange);
                        resourceChange.setVdu(vnfc.getVduId());
                        resourceChange.setType(ChangeType.VDU);
                        resourceChange.setResourceDefinitionId(UUID.randomUUID().toString());
                    }
                    grantRequest.setVnfInstanceId(vnfId);
                } catch (Exception e) {
                    logger.error("Unable to prepare grant request for termination", e);
                    throw new RuntimeException("Unable to prepare grant request for termination", e);
                }
                requestGrant(grantRequest);
                break;
        }
    }

    /**
     * Request grant for instantiation
     * - the added resources are calculated from the VNFD by counting the VDUs in the selected the instantiation level
     * - the only parameter used from the grant response in the VIM to which the VNF is to be deployed to
     *
     * @param vnfmId               the identifier of the VNFM
     * @param vnfId                the identifier of the VNF
     * @param vimId                the identifier of the VIM
     * @param onapVnfdId           the identifier of the VNF package in ONAP
     * @param instantiationLevelId the instantiation level
     * @param cbamVnfdContent      the content of the CBAM VNFD
     * @return the grant response
     */
    public GrantVNFResponseVim requestGrantForInstantiate(String vnfmId, String vnfId, String vimId, String onapVnfdId, String instantiationLevelId, String cbamVnfdContent, String jobId) {
        GrantVNFRequest grantRequest;
        try {
            grantRequest = buildGrantRequest(vnfmId, vimId, onapVnfdId, jobId, OperationType.INSTANTIATE);
            grantRequest.setVnfInstanceId(vnfId);
            grantRequest.getAddResource().addAll(calculateResourceChangeDuringInstantiate(cbamVnfdContent, instantiationLevelId));
        } catch (Exception e) {
            logger.error("Unable to prepare grant request for instantiation", e);
            throw new RuntimeException("Unable to prepare grant request for instantiation", e);
        }
        return requestGrant(grantRequest);
    }

    private GrantVNFResponseVim requestGrant(GrantVNFRequest grantRequest) {
        try {
            return restApiProvider.getNsLcmApi().grantvnf(grantRequest).getVim();
        } catch (org.onap.vnfmdriver.ApiException e) {
            logger.error("Unable to request grant", e);
            throw new RuntimeException(e);
        }
    }

    private Set<ResourceChange> calculateResourceChangeDuringInstantiate(String vnfdContent, String instantiationLevelId) {
        JsonObject root = new Gson().toJsonTree(new Yaml().load(vnfdContent)).getAsJsonObject();
        JsonObject capabilities = CbamUtils.child(CbamUtils.child(CbamUtils.child(root, "topology_template"), "substitution_mappings"), "capabilities");
        JsonObject deploymentFlavorProperties = CbamUtils.child(CbamUtils.child(capabilities, "deployment_flavour"), "properties");
        JsonObject instantiationLevels = CbamUtils.child(deploymentFlavorProperties, "instantiation_levels");
        Set<ResourceChange> resourceChanges = new HashSet<>();
        for (Map.Entry<String, JsonElement> vdu_level : CbamUtils.child(CbamUtils.child(instantiationLevels, instantiationLevelId), ("vdu_levels")).entrySet()) {
            JsonElement number_of_instances = vdu_level.getValue().getAsJsonObject().get("number_of_instances");
            for (int i = 0; i < number_of_instances.getAsLong(); i++) {
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
        JsonArray policies = CbamUtils.child(root, "topology_template").getAsJsonObject().get("policies").getAsJsonArray();
        for (JsonElement policy : policies) {
            if (policy.getAsJsonObject().entrySet().iterator().next().getKey().equals("heat_mapping")) {
                JsonObject aspects = policy.getAsJsonObject().entrySet().iterator().next().getValue().getAsJsonObject().get("properties").getAsJsonObject().get("aspects").getAsJsonObject();
                JsonObject aspect = aspects.get(aspectId).getAsJsonObject();
                if (aspect.has("vdus")) {
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
            }
        }
        return resourceChanges;
    }

    /**
     * Represents the mandatory parameters that must be sent during grant request to VF-C
     */
    private static class AdditionalGrantParams {
        private final String vnfmId;
        private final String vimId;

        AdditionalGrantParams(String vnfmId, String vimId) {
            this.vnfmId = vnfmId;
            this.vimId = vimId;
        }
    }
}
