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
import java.util.*;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import org.onap.aai.model.GenericVnf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api.IPackageProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIExternalSystemInfoProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.SdcPackageProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.packagetransformer.OnapR2HeatPackageBuilder;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.*;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification.LifecycleChangeNotificationManager;
import org.onap.vnfmadapter.so.model.SoJobHandler;
import org.onap.vnfmadapter.so.model.SoVnfCustomOperation;
import org.onap.vnfmadapter.so.v2.model.*;
import org.onap.vnfmdriver.model.*;
import org.onap.vnfmdriver.model.ExtVirtualLinkInfo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import static java.util.stream.Collectors.toMap;

import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.packagetransformer.OnapR2HeatPackageBuilder.ETSI_MODIFIABLE_ATTRIBUTES_EXTENSTION;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.packagetransformer.OnapR2HeatPackageBuilder.IMAGE_NAME;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.packagetransformer.OnapR2HeatPackageBuilder.NET_ID;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.*;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Responsible for providing access to AAI APIs.
 * Handles authentication and mandatory parameters.
 */
@Component
public class SoV2LifecycleManager {
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

    public SoV2VnfCreateResponse createVnf(String vnfIdInAai, SoV2VnfCreateRequest request, HttpServletResponse httpResponse){
        GenericVnf genericVnf = aaiRestApiProvider.getNetworkApi().getNetworkGenericVnfsGenericVnf(vnfIdInAai, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null).blockingFirst();
        VnfmInfo vnfmInfo = locateVnfm(genericVnf);
        Optional<com.nokia.cbam.lcm.v32.model.VnfInfo> matchingVnf = locateVnfBasedOnAaiVnfId(vnfIdInAai, vnfmInfo);
        if(request.isFailIfExists() != null && request.isFailIfExists()){
            if(matchingVnf.isPresent()){
                throw buildFatalFailure(logger, "The VNF with " + vnfIdInAai + " identifier in A&AI can not be found in the VNFM");
            }
        }
        String vnfIdInVnfm = null;
        if(!matchingVnf.isPresent()) {
            try {
                LifecycleManager.VnfCreationResult creationResult = lifecycleManager.create(vnfmInfo.getVnfmId(), genericVnf.getModelVersionId(), request.getName(), vnfIdInAai);
                vnfIdInVnfm = creationResult.getVnfInfo().getId();
            }
            catch (Exception e){
                return rollbackVnfCreation(vnfIdInAai, request, httpResponse, vnfmInfo, vnfIdInVnfm, e);
            }
        }
        else{
            vnfIdInVnfm = matchingVnf.get().getId();
        }
        try {
            com.nokia.cbam.lcm.v32.model.VnfInfo vnfAfterCreation = cbamRestApiProvider.getCbamLcmApi(vnfmInfo.getVnfmId()).vnfsVnfInstanceIdGet(vnfIdInVnfm, CbamRestApiProvider.NOKIA_LCM_API_VERSION).blockingFirst();
            updateModifiableAttributes(vnfmInfo.getVnfmId(), vnfAfterCreation, request.getInputs());
            List<ExtVirtualLinkInfo> externalVirtualLinks = new ArrayList<>(); //FIXME
            JsonObject operationAdditionalParameters = new JsonObject(); //FIXME
            String vimId = request.getCloudOwner() + SEPARATOR + request.getRegionName();
            operationAdditionalParameters.addProperty("vimId", vimId);
            AdditionalParameters additionalParameters = buildAdditionalParameters(request, genericVnf, vimId);
            String etsiVnfdId = packageProvider.getCbamVnfdId(genericVnf.getModelVersionId());
            if(!vnfAfterCreation.getInstantiationState().equals(InstantiationState.INSTANTIATED)){
                VnfInstantiateResponse instantiateResponse = lifecycleManager.instantiate(vnfmInfo.getVnfmId(), externalVirtualLinks, httpResponse, operationAdditionalParameters, additionalParameters, vnfIdInVnfm, genericVnf.getModelVersionId(), etsiVnfdId);
                SoV2VnfCreateResponse response = new SoV2VnfCreateResponse();
                response.setVnfIdInVnfm(vnfIdInVnfm);
                SoMsoRollback rollback = new SoMsoRollback();
                rollback.setVnfIdInVnfm(instantiateResponse.getVnfInstanceId());
                rollback.setOperationExecutionId(findLastOperation(vnfmInfo.getVnfmId(), vnfIdInVnfm).getId());
                rollback.setRollbackWithDeletion(true);
                response.setRollback(rollback);
                return response;
            }
            else{
                logger.info("The VNF in VNFM with " + vnfIdInAai + " identifier in A&AI and " + vnfIdInVnfm + " identifier in VNFM is already instantiated");
                SoV2VnfCreateResponse response = new SoV2VnfCreateResponse();
                response.setVnfIdInVnfm(vnfIdInVnfm);
                SoMsoRollback rollback = new SoMsoRollback();
                rollback.setVnfIdInVnfm(vnfAfterCreation.getId());
                rollback.setRollbackWithDeletion(false);
                response.setRollback(rollback);
                return response;
            }
        }
        catch (Exception e){
            return rollbackVnfCreation(vnfIdInAai, request, httpResponse, vnfmInfo, vnfIdInVnfm, e);
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
        addExtVirtualLinks(vimId, request.getInputs(), additionalParameters);
        //FIXME
        additionalParameters.getAdditionalParams();
        return additionalParameters;
    }

    private void addExtVirtualLinks(String  vimId, SoInput inputs, AdditionalParameters additionalParameters) {
        for (Map.Entry<String, String> netInput : filterInput(inputs, ".*" + NET_ID)) {
            ExtVirtualLinkData vl = new ExtVirtualLinkData();
            String ecpId = netInput.getKey().replaceAll(NET_ID + "$", "");
            vl.setVimId(vimId);
            vl.setResourceId(netInput.getValue());
            vl.setExtVirtualLinkId(UUID.randomUUID().toString());
            vl.setExtCps(new ArrayList<>());
            VnfExtCpData data = new VnfExtCpData();
            data.setCpdId(ecpId);
            vl.getExtCps().add(data);
            additionalParameters.getExtVirtualLinks().add(vl);
        }
    }

    private void setInstantiationLevel(JsonObject cbamVnfd, AdditionalParameters additionalParameters) {
        JsonObject topologyTemplate = child(cbamVnfd, "topology_template");
        JsonObject substitutionMappings = child(topologyTemplate, "substitution_mappings");
        JsonObject deploymentFlavor = child(child(substitutionMappings, "capabilities"), "deployment_flavour");
        String defaultInstantiationLevel = childElement(child(deploymentFlavor, "properties"), "default_instantiation_level_id").getAsString();
        additionalParameters.setInstantiationLevel(defaultInstantiationLevel);
    }

    private void buildImages(String vimId, VduMappings vduMappings, AdditionalParameters additionalParameters, SoInput inputs) {
        for (Map.Entry<String, String> imageInput : filterInput(inputs, ".*" +IMAGE_NAME)) {
            String vduName = imageInput.getKey().replace(IMAGE_NAME, "");
            if(vduMappings.vduIdToVirtualSoftwareId.containsKey(vduName)) {
                VimSoftwareImage image = new VimSoftwareImage();
                image.setVimId(vimId);
                image.setResourceId(imageInput.getKey());
                image.setVnfdSoftwareImageId(vduMappings.vduIdToVirtualSoftwareId.get(vduName));
                additionalParameters.getSoftwareImages().add(image);
            }
        }
    }

    private void buildExtenstions(SoV2VnfCreateRequest request, AdditionalParameters additionalParameters) {
        for (Map.Entry<String, String> extenstion : filterInput(request.getInputs(), ".*" +ETSI_MODIFIABLE_ATTRIBUTES_EXTENSTION )) {
            VnfProperty property = new VnfProperty();
            property.setName(extenstion.getKey().replace(ETSI_MODIFIABLE_ATTRIBUTES_EXTENSTION, ""));
            property.setValue(extenstion.getValue());
            additionalParameters.getExtensions().add(property);
        }
    }

    private static class VduMappings{
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
                if(requirement.getAsJsonObject().has("virtual_compute")){
                    vduMappings.vduIdToVirtualComputeId.put(vdu.getKey(), requirement.getAsJsonObject().get("virtual_compute").getAsString());
                    found = true;
                }
                if(requirement.getAsJsonObject().has("sw_image")){
                    vduMappings.vduIdToVirtualSoftwareId.put(vdu.getKey(), requirement.getAsJsonObject().get("sw_image").getAsString());
                }
            }
            if(!found){
                throw buildFatalFailure(logger, "Unable to find virtualComputeDescriptor for " + vdu.getKey() + " identifier");
            }
        }
        return vduMappings;
    }

    private void buildFlavours(String vimId, AdditionalParameters additionalParameters, SoInput inputs, Map<String,String> vduToVirtualComputeDescriptoId) {
        for (Map.Entry<String, String> input : filterInput(inputs, ".*_flavor_name")) {
            String vduName = input.getKey().replaceAll("_flavor_name$", "");
            VimComputeResourceFlavour flavor = new VimComputeResourceFlavour();
            flavor.setVimId(vimId);
            flavor.setVnfdVirtualComputeDescId(vduToVirtualComputeDescriptoId.get(vduName));
            flavor.setResourceId(input.getValue());
            additionalParameters.getComputeResourceFlavours().add(flavor);
        }
    }

    private Set<Map.Entry<String, String>> filterInput(SoInput input, String pattern){
        return input.entrySet().stream().filter(i -> i.getKey().matches(pattern)).collect(Collectors.toSet());
    }

    private void buildZones(AdditionalParameters additionalParameters, String vimId, SoInput inputs) {
        for (Map.Entry<String, String> input : filterInput(inputs, "availability_zone_[0-9]*")){
                ZoneInfo zone = new ZoneInfo();
                zone.setId(input.getKey());
                zone.setResourceId(input.getValue());
                zone.setVimId(vimId);
                additionalParameters.getZones().add(zone);
        }
    }

    private OperationExecution findLastOperation(String vnfmId, String vnfIdInVnfm){
        List<OperationExecution> operationExecutions = cbamRestApiProvider.getCbamLcmApi(vnfmId).vnfsVnfInstanceIdOperationExecutionsGet(vnfIdInVnfm, CbamRestApiProvider.NOKIA_LCM_API_VERSION).blockingFirst();
        return LifecycleChangeNotificationManager.NEWEST_OPERATIONS_FIRST.sortedCopy(operationExecutions).get(0);
    }

    private SoV2VnfCreateResponse rollbackVnfCreation(String vnfIdInAai, SoV2VnfCreateRequest request, HttpServletResponse httpResponse, VnfmInfo vnfmInfo, String vnfIdInVnfm, Exception e) {
        logger.warn("Unable to instantiate VNF with " + vnfIdInAai + " identifier in A&AI and " + vnfIdInVnfm + " identifier in VNFM", e);
        if(request.isBackout() != null && request.isBackout()){
            SoV2VnfDeleteRequest deleteRequest = new SoV2VnfDeleteRequest();
            deleteRequest.setMsoRequest(request.getMsoRequest());
            delete(vnfIdInAai, deleteRequest, httpResponse);
        }
        SoV2VnfCreateResponse response = new SoV2VnfCreateResponse();
        SoMsoRollback rollback = new SoMsoRollback();
        rollback.setRollbackWithDeletion(true);
        response.setRollback(rollback);
        try {
            Optional<com.nokia.cbam.lcm.v32.model.VnfInfo> vnfAfterFailedOperation = locateVnfBasedOnAaiVnfId(vnfIdInAai, vnfmInfo);
            if(vnfAfterFailedOperation.isPresent()) {
                response.setVnfIdInVnfm(vnfIdInVnfm);
            }
            else{
                logger.info("The VNF with " + vnfIdInAai + " identifier in A&AI is not yet created in VNFM");
            }
        }
        catch (Exception unableToLocatVnfInVnfm){
            logger.warn("Unable to locate VNF in VNFM with " + vnfIdInAai + " identifier in A&AI and " + vnfIdInVnfm + " identifier in VNFM");
        }
        return response;
    }

    public void delete(String vnfIdInAai, SoV2VnfDeleteRequest request, HttpServletResponse httpServletResponse){
        GenericVnf genericVnf = aaiRestApiProvider.getNetworkApi().getNetworkGenericVnfsGenericVnf(vnfIdInAai, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null).blockingFirst();
        VnfmInfo vnfmInfo = locateVnfm(genericVnf);
        Optional<com.nokia.cbam.lcm.v32.model.VnfInfo> matchingVnf = locateVnfBasedOnAaiVnfId(vnfIdInAai, vnfmInfo);
        if(matchingVnf.isPresent()){
            VnfTerminateRequest terminateRequest = new VnfTerminateRequest();
            terminateRequest.setTerminationType(VnfTerminationType.GRACEFUL);
            terminateRequest.setGracefulTerminationTimeout(Long.valueOf(60 * 60 * 1000L).toString());
            jobManager.waitForJobToFinish(lifecycleManager.terminateAndDelete(vnfmInfo.getVnfmId(), matchingVnf.get().getId(), terminateRequest, httpServletResponse));
        }
    }

    private void updateModifiableAttributes(String vnfmId, com.nokia.cbam.lcm.v32.model.VnfInfo vnfInfo, SoInput inputs){
        ModifyVnfInfoRequest request = new ModifyVnfInfoRequest();
        request.setExtensions(new ArrayList<>());
        Map<String, String> specifedExtensions = inputs.entrySet().stream().filter(i -> i.getKey().startsWith("etsi.modifiableAttribute.")).collect(toMap(i -> i.getKey().replace("etsi.modifiableAttribute.", ""), i -> i.getValue()));
        for (Map.Entry<String, String> specifiedExtension : specifedExtensions.entrySet()) {
            Optional<VnfProperty> exactProperty = vnfInfo.getExtensions().stream().filter(p -> p.getName().equals(specifiedExtension.getKey()) && p.getValue().equals(specifiedExtension.getValue())).findFirst();
            if(!exactProperty.isPresent()){
                VnfProperty onapCsarIdProperty = new VnfProperty();
                onapCsarIdProperty.setName(specifiedExtension.getKey());
                onapCsarIdProperty.setValue(specifiedExtension.getValue());
                request.getExtensions().add(onapCsarIdProperty);
            }
        }
        if(!request.getExtensions().isEmpty()) {
            lifecycleManager.executeModifyVnfInfo(vnfmId, vnfInfo.getId(), request);
        }
    }


    private Optional<com.nokia.cbam.lcm.v32.model.VnfInfo> locateVnfBasedOnAaiVnfId(String vnfIdInAai, VnfmInfo vnfmInfo) {
        return cbamRestApiProvider.getCbamLcmApi(vnfmInfo.getVnfmId()).vnfsGet(CbamRestApiProvider.NOKIA_LCM_API_VERSION).blockingFirst().stream().filter(vnf -> vnf.getDescription().equals(vnfIdInAai)).findFirst();
    }

    private VnfmInfo locateVnfm(GenericVnf vnf){
        for (String vnfmId : aaiExternalSystemInfoProvider.getVnfms()) {
            VnfmInfo vnfmInfo = aaiExternalSystemInfoProvider.queryVnfmInfoFromSource(vnfmId);
            if(vnfmInfo.getType().equals(vnf.getNfType())){
                return vnfmInfo;
            }
        }
        throw buildFatalFailure(logger, "Unable to locate a VNFM for VNF with " + vnf.getVnfId() + " identifier with " + vnf.getNfType() + " type");
    }
}