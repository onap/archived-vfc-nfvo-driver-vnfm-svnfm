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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.so;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nokia.cbam.lcm.v32.model.*;
import com.nokia.cbam.lcm.v32.model.VnfInfo;
import java.util.*;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import org.onap.aai.model.GenericVnf;
import org.onap.aai.model.VfModule;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api.IPackageProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIExternalSystemInfoProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.SdcPackageProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.packagetransformer.OnapR2HeatPackageBuilder;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.*;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification.LifecycleChangeNotificationManager;
import org.onap.vnfmadapter.so.v2.model.*;
import org.onap.vnfmdriver.model.ExtVirtualLinkInfo;
import org.onap.vnfmdriver.model.*;
import org.onap.vnfmdriver.model.ScaleDirection;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.packagetransformer.OnapR2HeatPackageBuilder.*;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.*;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Responsible for providing access to AAI APIs.
 * Handles authentication and mandatory parameters.
 */
@Component
public class SoV2LifecycleManager implements ISoV2LifecycleManager{
    private static Logger logger = getLogger(SoV2LifecycleManager.class);
    private final LifecycleManager lifecycleManager;
    private final CbamRestApiProvider cbamRestApiProvider;
    private final JobManager jobManager;
    private final AAIRestApiProvider aaiRestApiProvider;
    private final AAIExternalSystemInfoProvider aaiExternalSystemInfoProvider;
    private final IPackageProvider packageProvider;

    @Autowired
    SoV2LifecycleManager(LifecycleManagerForSo lifecycleManager, CbamRestApiProviderForSo cbamRestApiProvider, JobManagerForSo jobManager, AAIRestApiProvider aaiRestApiProvider, AAIExternalSystemInfoProvider aaiExternalSystemInfoProvider, SdcPackageProvider packageProvider) {
        this.lifecycleManager = lifecycleManager;
        this.cbamRestApiProvider = cbamRestApiProvider;
        this.jobManager = jobManager;
        this.aaiRestApiProvider = aaiRestApiProvider;
        this.aaiExternalSystemInfoProvider = aaiExternalSystemInfoProvider;
        this.packageProvider = packageProvider;
    }

    @Override
    public void createVnf(String vnfIdInAai, SoV2VnfCreateRequest request, HttpServletResponse httpResponse) {
        GenericVnf genericVnf = aaiRestApiProvider.getNetworkApi().getNetworkGenericVnfsGenericVnf(vnfIdInAai, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null).blockingFirst();
        VnfmInfo vnfmInfo = locateVnfm(genericVnf);
        Optional<com.nokia.cbam.lcm.v32.model.VnfInfo> matchingVnf = locateVnfBasedOnAaiVnfId(vnfIdInAai, vnfmInfo);
        if (request.isFailIfExists() != null && request.isFailIfExists()) {
            if (matchingVnf.isPresent()) {
                throw buildFatalFailure(logger, "The VNF with " + vnfIdInAai + " identifier in A&AI can not be found in the VNFM");
            }
        }
        String vnfIdInVnfm = null;
        if (!matchingVnf.isPresent()) {
            try {
                LifecycleManager.VnfCreationResult creationResult = lifecycleManager.create(vnfmInfo.getVnfmId(), genericVnf.getModelVersionId(), request.getName(), vnfIdInAai);
                vnfIdInVnfm = creationResult.getVnfInfo().getId();
            } catch (Exception e) {
                logger.warn("Unable to create VNF with " + vnfIdInAai + " identifier in AAI", e);
                cleanUpVnf(vnfIdInAai, vnfmInfo, request, httpResponse);
            }
        } else {
            vnfIdInVnfm = matchingVnf.get().getId();
        }
        try {
            com.nokia.cbam.lcm.v32.model.VnfInfo vnfAfterCreation = cbamRestApiProvider.getCbamLcmApi(vnfmInfo.getVnfmId()).vnfsVnfInstanceIdGet(vnfIdInVnfm, CbamRestApiProvider.NOKIA_LCM_API_VERSION).blockingFirst();
            updateModifiableAttributes(vnfmInfo.getVnfmId(), vnfAfterCreation, request.getInputs());
            String vimId = request.getCloudOwner() + SEPARATOR + request.getRegionName();
            List<ExtVirtualLinkInfo> externalVirtualLinks = addExtVirtualLinks(vimId, request.getInputs());
            JsonObject operationAdditionalParameters = new JsonObject();
            operationAdditionalParameters.addProperty("vimId", vimId);
            AdditionalParameters additionalParameters = buildAdditionalParameters(request, genericVnf, vimId);
            String etsiVnfdId = packageProvider.getCbamVnfdId(genericVnf.getModelVersionId());
            if (!vnfAfterCreation.getInstantiationState().equals(InstantiationState.INSTANTIATED)) {
                lifecycleManager.instantiate(vnfmInfo.getVnfmId(), externalVirtualLinks, httpResponse, operationAdditionalParameters, additionalParameters, vnfIdInVnfm, genericVnf.getModelVersionId(), etsiVnfdId);
                logger.info("The VNF in VNFM with " + vnfIdInAai + " identifier in A&AI and " + vnfIdInVnfm + " identifier in VNFM has been instantiated");
            } else {
                logger.info("The VNF in VNFM with " + vnfIdInAai + " identifier in A&AI and " + vnfIdInVnfm + " identifier in VNFM is already instantiated");
            }
        } catch (Exception e) {
            logger.warn("The VNF in VNFM with " + vnfIdInAai + " identifier in A&AI and " + vnfIdInVnfm + " identifier in VNFM can not be instantiated", e);
            cleanUpVnf(vnfIdInAai, vnfmInfo, request, httpResponse);
        }
    }

    @Override
    public SoV2VnfQueryResponse queryVnf(String vnfIdInAai, SoV2VnfQueryRequest request, HttpServletResponse httpResponse) {
        GenericVnf genericVnf = aaiRestApiProvider.getNetworkApi().getNetworkGenericVnfsGenericVnf(vnfIdInAai, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null).blockingFirst();
        VnfmInfo vnfmInfo = locateVnfm(genericVnf);
        Optional<com.nokia.cbam.lcm.v32.model.VnfInfo> matchingVnf = locateVnfBasedOnAaiVnfId(vnfIdInAai, vnfmInfo);
        SoV2VnfQueryResponse response = new SoV2VnfQueryResponse();
        response.setHeatStackId("base");
        response.setVnfExists(matchingVnf.isPresent());
        response.setStatus(getStatus(vnfmInfo, matchingVnf));
        return response;
    }

    private SoVnfStatus getStatus(VnfmInfo vnfmInfo, Optional<VnfInfo> matchingVnf) {
        if(matchingVnf.isPresent()){
            OperationExecution lastOperation = findLastOperation(vnfmInfo.getVnfmId(), matchingVnf.get().getId());
            if(lastOperation.getStatus().equals(OperationStatus.FINISHED)){
                return SoVnfStatus.ACTIVE;
            }
            else if(lastOperation.getStatus().equals(OperationStatus.FAILED)){
                return SoVnfStatus.FAILED;
            }
            else {
                return SoVnfStatus.UNKNOWN;
            }
        }
        else{
            return SoVnfStatus.NOTFOUND;
        }
    }

    private void cleanUpVnf(String vnfIdInAai, VnfmInfo vnfmInfo, SoV2VnfCreateRequest request, HttpServletResponse httpServletResponse) {
        if(request.isDeleteUponFailure() != null && request.isDeleteUponFailure()) {
            logger.info("Cleaning up the VNF in VNFM with " + vnfIdInAai + " identifier in A&AI");
            SoV2VnfDeleteRequest deleteRequest = new SoV2VnfDeleteRequest();
            deleteRequest.setMsoRequest(request.getMsoRequest());
            delete(vnfIdInAai, deleteRequest, httpServletResponse);
        }
    }

    private AdditionalParameters buildAdditionalParameters(SoV2VnfCreateRequest request, GenericVnf genericVnf, String vimId) {
        AdditionalParameters additionalParameters = new AdditionalParameters();
        buildZones(additionalParameters, vimId, request.getInputs());
        JsonObject cbamVnfd = new Gson().toJsonTree(new Yaml().load(packageProvider.getCbamVnfdId(genericVnf.getModelVersionId()))).getAsJsonObject();
        VduMappings vduMappings = getVduToVirtualComputeDescriptoId(cbamVnfd);
        buildFlavours(vimId, additionalParameters, request.getInputs(), vduMappings.vduIdToVirtualComputeId);
        buildExtenstions(request, additionalParameters);
        buildImages(vimId, vduMappings, additionalParameters, request.getInputs());
        setInstantiationLevel(cbamVnfd, additionalParameters);
        return additionalParameters;
    }

    private List<ExtVirtualLinkInfo> addExtVirtualLinks(String vimId, SoInput inputs) {
        List<ExtVirtualLinkInfo> externalVirtualLinks = new ArrayList<>();
        for (Map.Entry<String, String> netInput : filterInput(inputs, ".*" + NET_ID)) {
            ExtVirtualLinkInfo vl = new ExtVirtualLinkInfo();
            String ecpId = netInput.getKey().replaceAll(NET_ID + "$", "");
            vl.setVim(new ExtVirtualLinkInfoVim());
            vl.getVim().setVimid(vimId);
            vl.setResourceId(netInput.getValue());
            vl.setCpdId(ecpId);
            externalVirtualLinks.add(vl);
        }
        return externalVirtualLinks;
    }

    private void setInstantiationLevel(JsonObject cbamVnfd, AdditionalParameters additionalParameters) {
        JsonObject topologyTemplate = child(cbamVnfd, "topology_template");
        JsonObject substitutionMappings = child(topologyTemplate, "substitution_mappings");
        JsonObject deploymentFlavor = child(child(substitutionMappings, "capabilities"), "deployment_flavour");
        String defaultInstantiationLevel = childElement(child(deploymentFlavor, "properties"), "default_instantiation_level_id").getAsString();
        additionalParameters.setInstantiationLevel(defaultInstantiationLevel);
    }

    private void buildImages(String vimId, VduMappings vduMappings, AdditionalParameters additionalParameters, SoInput inputs) {
        for (Map.Entry<String, String> imageInput : filterInput(inputs, ".*" + IMAGE_NAME)) {
            String vduName = imageInput.getKey().replace(IMAGE_NAME, "");
            if (vduMappings.vduIdToVirtualSoftwareId.containsKey(vduName)) {
                VimSoftwareImage image = new VimSoftwareImage();
                image.setVimId(vimId);
                image.setResourceId(imageInput.getKey());
                image.setVnfdSoftwareImageId(vduMappings.vduIdToVirtualSoftwareId.get(vduName));
                additionalParameters.getSoftwareImages().add(image);
            }
        }
    }

    private void buildExtenstions(SoV2VnfCreateRequest request, AdditionalParameters additionalParameters) {
        for (Map.Entry<String, String> extenstion : filterInput(request.getInputs(), ".*" + ETSI_MODIFIABLE_ATTRIBUTES_EXTENSTION)) {
            VnfProperty property = new VnfProperty();
            property.setName(extenstion.getKey().replace(ETSI_MODIFIABLE_ATTRIBUTES_EXTENSTION, ""));
            property.setValue(extenstion.getValue());
            additionalParameters.getExtensions().add(property);
        }
    }

    private static class VduMappings {
        HashMap<String, String> vduIdToVirtualComputeId = new HashMap<>();
        HashMap<String, String> vduIdToVirtualSoftwareId = new HashMap<>();
    }

    private VduMappings getVduToVirtualComputeDescriptoId(JsonObject root) {
        VduMappings vduMappings = new VduMappings();
        JsonObject topologyTemplate = child(root, "topology_template");
        JsonObject nodeTemplates = child(topologyTemplate, "node_templates");
        for (Map.Entry<String, JsonElement> vdu : OnapR2HeatPackageBuilder.filterType(nodeTemplates, "tosca.nodes.nfv.VDU")) {
            boolean found = false;
            for (JsonElement requirement : childElement(vdu.getValue().getAsJsonObject(), "requirements").getAsJsonArray()) {
                if (requirement.getAsJsonObject().has("virtual_compute")) {
                    vduMappings.vduIdToVirtualComputeId.put(vdu.getKey(), requirement.getAsJsonObject().get("virtual_compute").getAsString());
                    found = true;
                }
                if (requirement.getAsJsonObject().has("sw_image")) {
                    vduMappings.vduIdToVirtualSoftwareId.put(vdu.getKey(), requirement.getAsJsonObject().get("sw_image").getAsString());
                }
            }
            if (!found) {
                throw buildFatalFailure(logger, "Unable to find virtualComputeDescriptor for " + vdu.getKey() + " identifier");
            }
        }
        return vduMappings;
    }

    private void buildFlavours(String vimId, AdditionalParameters additionalParameters, SoInput inputs, Map<String, String> vduToVirtualComputeDescriptoId) {
        for (Map.Entry<String, String> input : filterInput(inputs, ".*_flavor_name")) {
            String vduName = input.getKey().replaceAll("_flavor_name$", "");
            VimComputeResourceFlavour flavor = new VimComputeResourceFlavour();
            flavor.setVimId(vimId);
            flavor.setVnfdVirtualComputeDescId(vduToVirtualComputeDescriptoId.get(vduName));
            flavor.setResourceId(input.getValue());
            additionalParameters.getComputeResourceFlavours().add(flavor);
        }
    }

    private Set<Map.Entry<String, String>> filterInput(SoInput input, String pattern) {
        return input.entrySet().stream().filter(i -> i.getKey().matches(pattern)).collect(Collectors.toSet());
    }

    private void buildZones(AdditionalParameters additionalParameters, String vimId, SoInput inputs) {
        for (Map.Entry<String, String> input : filterInput(inputs, "availability_zone_[0-9]*")) {
            ZoneInfo zone = new ZoneInfo();
            zone.setId(input.getKey());
            zone.setResourceId(input.getValue());
            zone.setVimId(vimId);
            additionalParameters.getZones().add(zone);
        }
    }

    private OperationExecution findLastOperation(String vnfmId, String vnfIdInVnfm) {
        List<OperationExecution> operationExecutions = cbamRestApiProvider.getCbamLcmApi(vnfmId).vnfsVnfInstanceIdOperationExecutionsGet(vnfIdInVnfm, CbamRestApiProvider.NOKIA_LCM_API_VERSION).blockingFirst();
        return LifecycleChangeNotificationManager.NEWEST_OPERATIONS_FIRST.sortedCopy(operationExecutions).get(0);
    }

    @Override
    public void delete(String vnfIdInAai, SoV2VnfDeleteRequest request, HttpServletResponse httpServletResponse) {
        GenericVnf genericVnf = aaiRestApiProvider.getNetworkApi().getNetworkGenericVnfsGenericVnf(vnfIdInAai, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null).blockingFirst();
        VnfmInfo vnfmInfo = locateVnfm(genericVnf);
        Optional<com.nokia.cbam.lcm.v32.model.VnfInfo> matchingVnf = locateVnfBasedOnAaiVnfId(vnfIdInAai, vnfmInfo);
        if (matchingVnf.isPresent()) {
            VnfTerminateRequest terminateRequest = new VnfTerminateRequest();
            terminateRequest.setTerminationType(VnfTerminationType.GRACEFUL);
            terminateRequest.setGracefulTerminationTimeout(Long.valueOf(60 * 60 * 1000L).toString());
            jobManager.waitForJobToFinish(lifecycleManager.terminateAndDelete(vnfmInfo.getVnfmId(), matchingVnf.get().getId(), terminateRequest, httpServletResponse));
        }
    }

    @Override
    public void rollback(String vnfIdInAai, SoV2RollbackVnfUpdate rollback, HttpServletResponse httpServletResponse) {
        GenericVnf genericVnf = aaiRestApiProvider.getNetworkApi().getNetworkGenericVnfsGenericVnf(vnfIdInAai, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null).blockingFirst();
        VnfmInfo vnfmInfo = locateVnfm(genericVnf);
        VnfInfo vnfInfo = expectVnfBasedOnAaiVnfId(vnfIdInAai, vnfmInfo);
        ModifyVnfInfoRequest request= new ModifyVnfInfoRequest();
        request.setExtensions(buildExtension(rollback.getOriginalVnfProperties()));
        lifecycleManager.executeModifyVnfInfo(vnfmInfo.getVnfmId(), vnfInfo.getId(), request);
    }

    @Override
    public void createVfModule(String vnfIdInAai, String vfModuleId, SoV2VfModuleCreateRequest request, HttpServletResponse httpResponse) {
        GenericVnf genericVnf = aaiRestApiProvider.getNetworkApi().getNetworkGenericVnfsGenericVnf(vnfIdInAai, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null).blockingFirst();
        VnfmInfo vnfmInfo = locateVnfm(genericVnf);
        VnfInfo vnfInfo = expectVnfBasedOnAaiVnfId(vnfIdInAai, vnfmInfo);
        healVnfIfRequired(vnfIdInAai, httpResponse, vnfmInfo, vnfInfo);
        executeScale(request.getScalingAspectId(), httpResponse, genericVnf, vnfmInfo, vnfInfo);
    }

    @Override
    public void deleteVfModule(String vnfIdInAai, String vfModuleId, SoV2VnfDeleteRequest request, HttpServletResponse httpResponse) {
        GenericVnf genericVnf = aaiRestApiProvider.getNetworkApi().getNetworkGenericVnfsGenericVnf(vnfIdInAai, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null).blockingFirst();
        VnfmInfo vnfmInfo = locateVnfm(genericVnf);
        VnfInfo vnfInfo = expectVnfBasedOnAaiVnfId(vnfIdInAai, vnfmInfo);
        VfModule vfModule = aaiRestApiProvider.getNetworkApi().getNetworkGenericVnfsGenericVnfVfModulesVfModule(genericVnf.getVnfId(), vfModuleId, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null).blockingFirst();
        String aspectId = vfModule.getHeatStackId();
        executeScale(aspectId, httpResponse, genericVnf, vnfmInfo, vnfInfo);
        healVnfIfRequired(vnfIdInAai, httpResponse, vnfmInfo, vnfInfo);
    }

    @Override
    public SoV2VnfUpdateResponse updateVfModule(String vnfIdInAai, String vfModuleId, SoV2VnfUpdateRequest request, HttpServletResponse httpResponse) {
        return updateVnf(vnfIdInAai, request, httpResponse);
    }

    private void executeScale(String aspectId, HttpServletResponse httpResponse, GenericVnf genericVnf, VnfmInfo vnfmInfo, VnfInfo vnfInfo) {
        ScaleInfo scaleInfo = vnfInfo.getInstantiatedVnfInfo().getScaleStatus().stream().filter(s -> s.getAspect().equals(aspectId)).findFirst().get();
        long expectedStepCount = genericVnf.getVfModules().stream().filter(m -> m.getHeatStackId().equals(aspectId)).count();
        if(expectedStepCount != scaleInfo.getScaleLevel().longValue()){
            VnfScaleRequest scaleRequest = new VnfScaleRequest();
            scaleRequest.setAspectId(aspectId);
            scaleRequest.setType(expectedStepCount > scaleInfo.getScaleLevel().longValue() ? ScaleDirection.OUT : ScaleDirection.IN);
            scaleRequest.setNumberOfSteps(Long.valueOf(Math.abs(expectedStepCount - scaleInfo.getScaleLevel().longValue())).toString());
            JobInfo jobInfo = lifecycleManager.scaleVnf(vnfmInfo.getVnfmId(), vnfInfo.getId(), scaleRequest, httpResponse);
            jobManager.waitForJobToFinish(jobInfo);
        }
    }

    private void healVnfIfRequired(String vnfIdInAai, HttpServletResponse httpResponse, VnfmInfo vnfmInfo, VnfInfo vnfInfo) {
        OperationExecution lastOperation = findLastOperation(vnfmInfo.getVnfmId(), vnfIdInAai);
        if(lastOperation.getStatus().equals(OperationStatus.FAILED)){
            VnfHealRequest healRequest = new VnfHealRequest();
            healRequest.setAffectedvm(new VnfHealRequestAffectedvm());
            healRequest.setAction("reboot");
            JobInfo jobInfo = lifecycleManager.healVnf(vnfmInfo.getVnfmId(), vnfInfo.getId(), healRequest, empty(), httpResponse);
            jobManager.waitForJobToFinish(jobInfo);
        }
    }

    @Override
    public SoV2VnfUpdateResponse updateVnf(String vnfIdInAai, SoV2VnfUpdateRequest request, HttpServletResponse httpResponse) {
        GenericVnf genericVnf = aaiRestApiProvider.getNetworkApi().getNetworkGenericVnfsGenericVnf(vnfIdInAai, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null).blockingFirst();
        VnfmInfo vnfmInfo = locateVnfm(genericVnf);
        Optional<com.nokia.cbam.lcm.v32.model.VnfInfo> matchingVnf = locateVnfBasedOnAaiVnfId(vnfIdInAai, vnfmInfo);
        Map<String, String> specifiedExtensions = collecSpecifiedExtensions(request.getInputs());
        SoV2VnfUpdateResponse response = new SoV2VnfUpdateResponse();
        Map<String, String> existingProperties = matchingVnf.get().getExtensions().stream().filter(p -> specifiedExtensions.containsKey(p.getName())).collect(Collectors.toMap(vnfProperty -> vnfProperty.getName(), vnfProperty -> vnfProperty.getValue().toString()));
        response.setOriginalVnfProperties(new OriginalVnfProperties());
        response.getOriginalVnfProperties().putAll(existingProperties);
        if(matchingVnf.isPresent()){
            try{
                ModifyVnfInfoRequest modifyRequest = new ModifyVnfInfoRequest();
                modifyRequest.setExtensions(buildExtension(specifiedExtensions));
                lifecycleManager.executeModifyVnfInfo(vnfmInfo.getVnfmId(), matchingVnf.get().getId(), modifyRequest);
                response.setSuccessful(true);
            }
            catch (Exception e){
                response.setSuccessful(false);
            }
            return response;
        }
        else{
            throw buildFatalFailure(logger, "No VNF with " + vnfIdInAai + " identifier in A&AI exists in the VNFM");
        }
    }

    private List<VnfProperty> buildExtension(Map<String, String> specifiedExtensions) {
        return specifiedExtensions.entrySet().stream().map(e -> {
            VnfProperty p = new VnfProperty();
            p.setName(e.getKey());
            p.setValue(e.getValue());
            return p;
        }).collect(toList());
    }

    private void updateModifiableAttributes(String vnfmId, com.nokia.cbam.lcm.v32.model.VnfInfo vnfInfo, SoInput inputs) {
        ModifyVnfInfoRequest request = new ModifyVnfInfoRequest();
        request.setExtensions(new ArrayList<>());
        Map<String, String> specifedExtensions = collecSpecifiedExtensions(inputs);
        for (Map.Entry<String, String> specifiedExtension : specifedExtensions.entrySet()) {
            Optional<VnfProperty> exactProperty = vnfInfo.getExtensions().stream().filter(p -> p.getName().equals(specifiedExtension.getKey()) && p.getValue().equals(specifiedExtension.getValue())).findFirst();
            if (!exactProperty.isPresent()) {
                VnfProperty onapCsarIdProperty = new VnfProperty();
                onapCsarIdProperty.setName(specifiedExtension.getKey());
                onapCsarIdProperty.setValue(specifiedExtension.getValue());
                request.getExtensions().add(onapCsarIdProperty);
            }
        }
        if (!request.getExtensions().isEmpty()) {
            lifecycleManager.executeModifyVnfInfo(vnfmId, vnfInfo.getId(), request);
        }
    }

    private Map<String, String> collecSpecifiedExtensions(SoInput inputs) {
        return inputs.entrySet().stream().filter(i -> i.getKey().startsWith("etsi.modifiableAttribute.")).collect(toMap(i -> i.getKey().replace("etsi.modifiableAttribute.", ""), i -> i.getValue()));
    }

    private Optional<com.nokia.cbam.lcm.v32.model.VnfInfo> locateVnfBasedOnAaiVnfId(String vnfIdInAai, VnfmInfo vnfmInfo) {
        return cbamRestApiProvider.getCbamLcmApi(vnfmInfo.getVnfmId()).vnfsGet(CbamRestApiProvider.NOKIA_LCM_API_VERSION).blockingFirst().stream().filter(vnf -> vnf.getDescription().equals(vnfIdInAai)).findFirst();
    }

    private com.nokia.cbam.lcm.v32.model.VnfInfo expectVnfBasedOnAaiVnfId(String vnfIdInAai, VnfmInfo vnfmInfo) {
        Optional<VnfInfo> vnfInfo = locateVnfBasedOnAaiVnfId(vnfIdInAai, vnfmInfo);
        if(!vnfInfo.isPresent()){
            throw buildFatalFailure(logger, "Unable to locate VNF with " + vnfIdInAai + " A&AI identifier in VNFM");
        }
        return vnfInfo.get();
    }

    private VnfmInfo locateVnfm(GenericVnf vnf) {
        for (String vnfmId : aaiExternalSystemInfoProvider.getVnfms()) {
            VnfmInfo vnfmInfo = aaiExternalSystemInfoProvider.queryVnfmInfoFromSource(vnfmId);
            if (vnfmInfo.getType().equals(vnf.getNfType())) {
                return vnfmInfo;
            }
        }
        throw buildFatalFailure(logger, "Unable to locate a VNFM for VNF with " + vnf.getVnfId() + " identifier with " + vnf.getNfType() + " type");
    }
}