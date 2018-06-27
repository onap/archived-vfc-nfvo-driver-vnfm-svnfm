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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm;


import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nokia.cbam.catalog.v1.model.CatalogAdapterVnfpackage;
import com.nokia.cbam.lcm.v32.model.*;
import com.nokia.cbam.lcm.v32.model.ScaleDirection;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.servlet.http.HttpServletResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api.IGrantManager;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api.VimInfoProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.StoreLoader;
import org.onap.vnfmdriver.model.ExtVirtualLinkInfo;
import org.onap.vnfmdriver.model.*;
import org.onap.vnfmdriver.model.VimInfo;
import org.onap.vnfmdriver.model.VnfInfo;
import org.slf4j.Logger;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import static java.lang.Integer.parseInt;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;

import static com.google.common.base.Splitter.on;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Ordering.natural;
import static com.google.common.collect.Sets.newHashSet;
import static com.nokia.cbam.lcm.v32.model.InstantiationState.INSTANTIATED;
import static com.nokia.cbam.lcm.v32.model.OperationStatus.FINISHED;
import static com.nokia.cbam.lcm.v32.model.OperationType.INSTANTIATE;
import static com.nokia.cbam.lcm.v32.model.VimInfo.VimInfoTypeEnum.*;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.*;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.SystemFunctions.systemFunctions;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProvider.NOKIA_LCM_API_VERSION;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification.LifecycleChangeNotificationManager.NEWEST_OPERATIONS_FIRST;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.StringUtils.isEmpty;

/**
 * Responsible for executing lifecycle operation on the VNF
 */
public class LifecycleManager {
    public static final String ONAP_CSAR_ID = "onapCsarId";
    public static final long OPERATION_STATUS_POLLING_INTERVAL_IN_MS = 5000L;
    /**
     * The key of the CBAM VNF extension for the identifier of the VNFM in ONAP
     */
    public static final String EXTERNAL_VNFM_ID = "externalVnfmId";
    public static final String SCALE_OPERATION_NAME = "scale";
    public static final String ETSI_CONFIG = "etsi_config";
    public static final String PROPERTIES = "properties";
    private static Logger logger = getLogger(LifecycleManager.class);
    private final CatalogManager catalogManager;
    private final IGrantManager grantManager;
    private final JobManager jobManager;
    private final ILifecycleChangeNotificationManager notificationManager;
    private final CbamRestApiProvider cbamRestApiProvider;
    private final VimInfoProvider vimInfoProvider;

    /**
     * Runs asynchronous operations in the background
     */
    private ExecutorService executorService = Executors.newCachedThreadPool();

    LifecycleManager(CatalogManager catalogManager, IGrantManager grantManager, CbamRestApiProvider restApiProvider, VimInfoProvider vimInfoProvider, JobManager jobManager, ILifecycleChangeNotificationManager notificationManager) {
        this.vimInfoProvider = vimInfoProvider;
        this.grantManager = grantManager;
        this.cbamRestApiProvider = restApiProvider;
        this.jobManager = jobManager;
        this.notificationManager = notificationManager;
        this.catalogManager = catalogManager;
    }

    /**
     * @param vimId the VIM identifier
     * @return the name of the region
     */
    public static String getRegionName(String vimId) {
        return newArrayList(on(SEPARATOR).split(vimId)).get(1);
    }

    /**
     * @param vimId the VIM identifier
     * @return the owner of the cloud
     */
    public static String getCloudOwner(String vimId) {
        return newArrayList(on(SEPARATOR).split(vimId)).get(0);
    }

    private static OperationExecution findLastInstantiation(List<OperationExecution> operationExecutions) {
        return find(NEWEST_OPERATIONS_FIRST.sortedCopy(operationExecutions), op -> INSTANTIATE.equals(op.getOperationType()));
    }

    public static String getVnfdIdFromModifyableAttributes(com.nokia.cbam.lcm.v32.model.VnfInfo vnf) {
        return find(vnf.getExtensions(), p -> p.getName().equals(ONAP_CSAR_ID)).getValue().toString();
    }

    /**
     * Create the VNF. It consists of the following steps
     * <ul>
     * <li>upload the VNF package to CBAM package (if not already there)</li>
     * <li>create the VNF on CBAM</li>
     * <li>modify attributes of the VNF (add onapCsarId field)</li>
     * </ul>
     * The rollback of the failed operation is not implemented
     * <ul>
     * <li>delete the VNF if error occurs before instantiation</li>
     * <li>terminateVnf & delete VNF if error occurs after instantiation</li>
     * </ul>
     *
     * @param vnfmId      the identifier of the VNFM
     * @param csarId      the identifier of the VNF package
     * @param vnfName     the name of the VNF
     * @param description the description of the VNF
     * @return the VNF creation result
     */
    public VnfCreationResult create(String vnfmId, String csarId, String vnfName, String description) {
        logOperationInput("not yet specified", "creation", csarId);
        try {
            CatalogAdapterVnfpackage cbamPackage = catalogManager.preparePackageInCbam(vnfmId, csarId);
            CreateVnfRequest vnfCreateRequest = new CreateVnfRequest();
            vnfCreateRequest.setVnfdId(cbamPackage.getVnfdId());
            vnfCreateRequest.setName(vnfName);
            vnfCreateRequest.setDescription(description);
            com.nokia.cbam.lcm.v32.model.VnfInfo vnfInfo = cbamRestApiProvider.getCbamLcmApi(vnfmId).vnfsPost(vnfCreateRequest, NOKIA_LCM_API_VERSION).blockingFirst();
            addVnfdIdToVnfModifyableAttributeExtensions(vnfmId, vnfInfo.getId(), csarId);
            return new VnfCreationResult(vnfInfo, cbamPackage.getVnfdId());
        } catch (Exception e) {
            throw buildFatalFailure(logger, "Unable to create the VNF", e);
        }
    }

    private void logOperationInput(String vnfId, String operationName, Object payload) {
        if (logger.isInfoEnabled()) {
            logger.info("Starting {} operation on VNF with {} identifier with {} parameter", operationName, vnfId, new Gson().toJson(payload));
        }
    }

    /**
     * Instantiate the VNF
     *
     * @param vnfmId                        the identifier of the VNFM
     * @param externalVirtualLinks          the external virtual links of the VNF
     * @param httpResponse                  the HTTP response that corresponds to the VNF instantiation request
     * @param additionalParameters          additional parameters
     * @param vnfId                         the identifier of the VNF
     * @param vnfmVnfdId                    the identifier of the VNF package in CBAM
     * @param operationAdditionalParameters the additional parameters of the operation
     * @param onapVnfdId                    the identifier of the VNFD in the VNFM
     * @return the instantiation response
     */
    @SuppressWarnings("squid:S00107") //wrapping them into an object makes the code less readable
    public VnfInstantiateResponse instantiate(String vnfmId, List<ExtVirtualLinkInfo> externalVirtualLinks, HttpServletResponse httpResponse, Object operationAdditionalParameters, AdditionalParameters additionalParameters, String vnfId, String onapVnfdId, String vnfmVnfdId) {
        logOperationInput(vnfId, "instantiation", additionalParameters);
        VnfInstantiateResponse response = new VnfInstantiateResponse();
        response.setVnfInstanceId(vnfId);
        String vimId = getVimId(operationAdditionalParameters);
        JobInfo spawnJob = scheduleExecution(vnfId, httpResponse, "instantiate", jobInfo ->
                instantiateVnf(vnfmId, externalVirtualLinks, additionalParameters, onapVnfdId, vnfmVnfdId, vnfId, vimId, jobInfo)
        );
        response.setJobId(spawnJob.getJobId());
        return response;
    }

    /**
     * Instantiate (VF-C terminology) the VNF. It consists of the following steps
     * <ul>
     * <li>upload the VNF package to CBAM package (if not already there)</li>
     * <li>create the VNF on CBAM</li>
     * <li>modify attributes of the VNF (add onapCsarId field)</li>
     * <li>asynchronously</li>
     * <li>request grant from VF-C</li>
     * <li>instantiate VNF on CBAM</li>
     * <li>return VNF & job id (after create VNF on CBAM)</li>
     * <li></li>
     * </ul>
     * The rollback of the failed operation is not implemented
     * <ul>
     * <li>delete the VNF if error occurs before instantiation</li>
     * <li>terminateVnf & delete VNf if error occurs after instantiation</li>
     * </ul>
     *
     * @param vnfmId       the identifier of the VNFM
     * @param request      the instantiation request
     * @param httpResponse the HTTP response
     * @return the instantiation response
     */
    public VnfInstantiateResponse createAndInstantiate(String vnfmId, VnfInstantiateRequest request, HttpServletResponse httpResponse) {
        AdditionalParameters additionalParameters = convertInstantiationAdditionalParams(request.getVnfPackageId(), request.getAdditionalParam());
        VnfCreationResult creationResult = create(vnfmId, request.getVnfDescriptorId(), request.getVnfInstanceName(), request.getVnfInstanceDescription());
        return instantiate(vnfmId, request.getExtVirtualLink(), httpResponse, request.getAdditionalParam(), additionalParameters, creationResult.vnfInfo.getId(), request.getVnfPackageId(), creationResult.vnfdId);
    }

    @SuppressWarnings("squid:S00107") //wrapping them into an object makes the code less readable
    private void instantiateVnf(String vnfmId, List<ExtVirtualLinkInfo> extVirtualLinkInfos, AdditionalParameters additionalParameters, String onapVnfdId, String vnfmVnfdId, String vnfId, String vimId, JobInfo jobInfo) {
        String vnfdContent = catalogManager.getCbamVnfdContent(vnfmId, vnfmVnfdId);
        addSpecifiedExtensions(vnfmId, vnfId, additionalParameters);
        GrantVNFResponseVim vim = grantManager.requestGrantForInstantiate(vnfmId, vnfId, vimId, onapVnfdId, additionalParameters.getInstantiationLevel(), vnfdContent, jobInfo.getJobId());
        handleBackwardIncompatibleApiChangesInVfc(vim);
        VimInfo vimInfo = vimInfoProvider.getVimInfo(vim.getVimId());
        InstantiateVnfRequest instantiationRequest = new InstantiateVnfRequest();
        addExternalLinksToRequest(extVirtualLinkInfos, additionalParameters, instantiationRequest, vimId);
        instantiationRequest.getVims().add(addVim(additionalParameters, vimId, vim, vimInfo));
        instantiationRequest.setFlavourId(getFlavorId(vnfdContent));
        instantiationRequest.setComputeResourceFlavours(additionalParameters.getComputeResourceFlavours());
        instantiationRequest.setGrantlessMode(true);
        instantiationRequest.setInstantiationLevelId(additionalParameters.getInstantiationLevel());
        instantiationRequest.setSoftwareImages(additionalParameters.getSoftwareImages());
        instantiationRequest.setZones(additionalParameters.getZones());
        instantiationRequest.setExtManagedVirtualLinks(additionalParameters.getExtManagedVirtualLinks());
        for (ExtVirtualLinkData extVirtualLinkData : additionalParameters.getExtVirtualLinks()) {
            instantiationRequest.addExtVirtualLinksItem(extVirtualLinkData);
        }
        JsonObject root = new Gson().toJsonTree(jobInfo).getAsJsonObject();
        if (additionalParameters.getAdditionalParams() != null) {
            for (Map.Entry<String, JsonElement> item : new Gson().toJsonTree(additionalParameters.getAdditionalParams()).getAsJsonObject().entrySet()) {
                root.add(item.getKey(), item.getValue());
            }
        } else {
            logger.warn("No additional parameters were specified for the operation");
        }
        instantiationRequest.setAdditionalParams(root);
        OperationExecution operationExecution = cbamRestApiProvider.getCbamLcmApi(vnfmId).vnfsVnfInstanceIdInstantiatePost(vnfId, instantiationRequest, NOKIA_LCM_API_VERSION).blockingFirst();
        waitForOperationToFinish(vnfmId, vnfId, operationExecution.getId());
    }

    private void handleBackwardIncompatibleApiChangesInVfc(GrantVNFResponseVim vim) {
        if (vim.getVimId() == null) {
            if (vim.getVimid() == null) {
                throw buildFatalFailure(logger, "VF-C did not send VIM identifier in grant response");
            } else {
                vim.setVimId(vim.getVimid());
            }
        }
        if (vim.getAccessInfo() == null) {
            if (vim.getAccessinfo() == null) {
                throw buildFatalFailure(logger, "VF-C did not send access info in grant response");
            } else {
                vim.setAccessInfo(vim.getAccessinfo());
            }
        }
    }

    private com.nokia.cbam.lcm.v32.model.VimInfo addVim(AdditionalParameters additionalParameters, String vimId, GrantVNFResponseVim vim, VimInfo vimInfo) {
        if (vimInfo.getType().equals("openstack")) {
            if (StringUtils.isEmpty(vimInfo.getDomain())) {
                return buildOpenStackV2INFO(vimId, vim, vimInfo);
            } else {
                return buildOpenStackV3INFO(vimId, vim, vimInfo);
            }
        } else {
            //OTHER VIM TYPE is not possible
            return buildVcloudInfo(vimId, vimInfo);
        }
    }

    private String getVimId(Object additionalParams) {
        return childElement(new Gson().toJsonTree(additionalParams).getAsJsonObject(), "vimId").getAsString();
    }

    private AdditionalParameters convertInstantiationAdditionalParams(String csarId, Object additionalParams) {
        JsonObject root = new Gson().toJsonTree(additionalParams).getAsJsonObject();
        if (root.has(PROPERTIES)) {
            JsonObject properties = new JsonParser().parse(root.get(PROPERTIES).getAsString()).getAsJsonObject();
            if (properties.has(ETSI_CONFIG)) {
                JsonElement etsiConfig = properties.get(ETSI_CONFIG);
                return new Gson().fromJson(etsiConfig.getAsString(), AdditionalParameters.class);
            } else {
                logger.info("The instantiation input for VNF with {} CSAR id does not have an " + ETSI_CONFIG + " section", csarId);
            }
        } else {
            logger.info("The instantiation input for VNF with {} CSAR id does not have a properties section", csarId);
        }
        JsonObject inputs = child(root, "inputs");
        if (!inputs.has(csarId)) {
            return new Gson().fromJson(catalogManager.getEtsiConfiguration(csarId), AdditionalParameters.class);
        }
        JsonElement additionalParamsForVnf = new JsonParser().parse(inputs.get(csarId).getAsString());
        return new Gson().fromJson(additionalParamsForVnf, AdditionalParameters.class);
    }

    private String getFlavorId(String vnfdContent) {
        JsonObject root = new Gson().toJsonTree(new Yaml().load(vnfdContent)).getAsJsonObject();
        JsonObject capabilities = child(child(child(root, "topology_template"), "substitution_mappings"), "capabilities");
        JsonObject deploymentFlavorProperties = child(child(capabilities, "deployment_flavour"), PROPERTIES);
        return childElement(deploymentFlavorProperties, "flavour_id").getAsString();
    }

    private Set<Map.Entry<String, JsonElement>> getAcceptableOperationParameters(String vnfdContent, String operationName) {
        JsonObject root = new Gson().toJsonTree(new Yaml().load(vnfdContent)).getAsJsonObject();
        JsonObject interfaces = child(child(child(root, "topology_template"), "substitution_mappings"), "interfaces");
        List<List<Map.Entry<String, JsonElement>>> operations = interfaces.entrySet().stream().map(m -> m.getValue().getAsJsonObject().entrySet().stream().collect(toList())).collect(toList());
        for (Map.Entry<String, JsonElement> operation : operations.stream().flatMap(List::stream).collect(toList())) {
            if (operation.getKey().equals(operationName)) {
                return child(child(operation.getValue().getAsJsonObject(), "inputs"), "additional_parameters").entrySet();
            }
        }

        throw buildFatalFailure(logger, "Unable to find operation named " + operationName);
    }

    private void addExternalLinksToRequest(List<ExtVirtualLinkInfo> extVirtualLinks, AdditionalParameters additionalParameters, InstantiateVnfRequest instantiationRequest, String vimId) {
        for (ExtVirtualLinkInfo extVirtualLink : extVirtualLinks) {
            ExtVirtualLinkData cbamExternalVirtualLink = new ExtVirtualLinkData();
            cbamExternalVirtualLink.setVimId(vimId);
            cbamExternalVirtualLink.setResourceId(extVirtualLink.getResourceId());
            VnfExtCpData ecp = new VnfExtCpData();
            cbamExternalVirtualLink.setExtVirtualLinkId(extVirtualLink.getVlInstanceId());
            cbamExternalVirtualLink.getExtCps().add(ecp);
            ecp.setCpdId(extVirtualLink.getCpdId());
            List<NetworkAddress> addresses = additionalParameters.getExternalConnectionPointAddresses().get(extVirtualLink.getCpdId());
            ecp.setAddresses(addresses);
            instantiationRequest.addExtVirtualLinksItem(cbamExternalVirtualLink);
        }
    }

    private void addVnfdIdToVnfModifyableAttributeExtensions(String vnfmId, String vnfId, String onapCsarId) {
        ModifyVnfInfoRequest request = new ModifyVnfInfoRequest();
        request.setExtensions(new ArrayList<>());
        VnfProperty onapCsarIdProperty = new VnfProperty();
        onapCsarIdProperty.setName(ONAP_CSAR_ID);
        onapCsarIdProperty.setValue(onapCsarId);
        request.getExtensions().add(onapCsarIdProperty);
        VnfProperty externalVnfmIdProperty = new VnfProperty();
        externalVnfmIdProperty.setName(EXTERNAL_VNFM_ID);
        externalVnfmIdProperty.setValue(vnfmId);
        request.getExtensions().add(externalVnfmIdProperty);
        executeModifyVnfInfo(vnfmId, vnfId, request);
    }

    public void executeModifyVnfInfo(String vnfmId, String vnfId, ModifyVnfInfoRequest request) {
        try {
            OperationExecution operationExecution = cbamRestApiProvider.getCbamLcmApi(vnfmId).vnfsVnfInstanceIdPatch(vnfId, request, NOKIA_LCM_API_VERSION).blockingFirst();
            waitForOperationToFinish(vnfmId, vnfId, operationExecution.getId());
        } catch (Exception e) {
            String properties = Joiner.on(",").join(natural().sortedCopy(transform(request.getExtensions(), VnfProperty::getName)));
            throw buildFatalFailure(logger, "Unable to set the " + properties + " properties on the VNF with " + vnfId + " identifier", e);
        }
    }

    private void addSpecifiedExtensions(String vnfmId, String vnfId, AdditionalParameters additionalParameters) {
        if (!additionalParameters.getExtensions().isEmpty()) {
            ModifyVnfInfoRequest request = new ModifyVnfInfoRequest();
            request.setExtensions(new ArrayList<>());
            request.getExtensions().addAll(additionalParameters.getExtensions());
            executeModifyVnfInfo(vnfmId, vnfId, request);
        } else {
            logger.info("No extensions specified for VNF with {} identifier", vnfId);
        }
    }

    private OPENSTACKV3INFO buildOpenStackV3INFO(String vimId, GrantVNFResponseVim vim, org.onap.vnfmdriver.model.VimInfo vimInfo) {
        OPENSTACKV3INFO openstackv3INFO = new OPENSTACKV3INFO();
        openstackv3INFO.setVimInfoType(OPENSTACK_V3_INFO);
        OpenStackAccessInfoV3 accessInfov3 = new OpenStackAccessInfoV3();
        openstackv3INFO.accessInfo(accessInfov3);
        accessInfov3.setPassword(vimInfo.getPassword());
        accessInfov3.setDomain(vimInfo.getDomain());
        accessInfov3.setProject(vim.getAccessInfo().getTenant());
        accessInfov3.setRegion(getRegionName(vimId));
        accessInfov3.setUsername(vimInfo.getUserName());
        openstackv3INFO.setInterfaceInfo(getEndpointInfo(vimInfo));
        openstackv3INFO.setId(vimId);
        return openstackv3INFO;
    }

    private OPENSTACKV2INFO buildOpenStackV2INFO(String vimId, GrantVNFResponseVim vim, org.onap.vnfmdriver.model.VimInfo vimInfo) {
        OPENSTACKV2INFO openstackv2INFO = new OPENSTACKV2INFO();
        openstackv2INFO.setVimInfoType(OPENSTACK_V2_INFO);
        OpenStackAccessInfoV2 accessInfo = new OpenStackAccessInfoV2();
        openstackv2INFO.setAccessInfo(accessInfo);
        accessInfo.setPassword(vimInfo.getPassword());
        accessInfo.setTenant(vim.getAccessInfo().getTenant());
        accessInfo.setUsername(vimInfo.getUserName());
        accessInfo.setRegion(getRegionName(vimId));
        EndpointInfo interfaceEndpoint = getEndpointInfo(vimInfo);
        openstackv2INFO.setInterfaceInfo(interfaceEndpoint);
        openstackv2INFO.setId(vimId);
        return openstackv2INFO;
    }

    private EndpointInfo getEndpointInfo(VimInfo vimInfo) {
        EndpointInfo interfaceEndpoint = new EndpointInfo();
        if (!isEmpty(vimInfo.getSslInsecure())) {
            interfaceEndpoint.setSkipCertificateHostnameCheck(Boolean.parseBoolean(vimInfo.getSslInsecure()));
            interfaceEndpoint.setSkipCertificateVerification(Boolean.parseBoolean(vimInfo.getSslInsecure()));
        } else {
            interfaceEndpoint.setSkipCertificateHostnameCheck(true);
            interfaceEndpoint.setSkipCertificateVerification(true);
        }
        interfaceEndpoint.setEndpoint(vimInfo.getUrl());
        if (!interfaceEndpoint.isSkipCertificateVerification()) {
            interfaceEndpoint.setTrustedCertificates(new ArrayList<>());
            for (String trustedCertificate : StoreLoader.getCertifacates(vimInfo.getSslCacert())) {
                interfaceEndpoint.getTrustedCertificates().add(trustedCertificate.getBytes(UTF_8));
            }
        }
        return interfaceEndpoint;
    }

    private VMWAREVCLOUDINFO buildVcloudInfo(String vimId, org.onap.vnfmdriver.model.VimInfo vimInfo) {
        VMWAREVCLOUDINFO vcloudInfo = new VMWAREVCLOUDINFO();
        vcloudInfo.setVimInfoType(VMWARE_VCLOUD_INFO);
        VCloudAccessInfo accessInfo = new VCloudAccessInfo();
        vcloudInfo.setAccessInfo(accessInfo);
        accessInfo.setPassword(vimInfo.getPassword());
        accessInfo.setUsername(vimInfo.getUserName());
        accessInfo.setOrganization(getRegionName(vimId));
        vcloudInfo.setInterfaceInfo(getEndpointInfo(vimInfo));
        vcloudInfo.setId(vimId);
        return vcloudInfo;
    }

    /**
     * Terminates and deletes the VNF
     * <ul>
     * <li>fails if the VNF does not exist</li>
     * <li>terminates if instantiated</li>
     * <li>deletes the VNF</li>
     * </ul>
     *
     * @param vnfmId       the identifier of the VNFM
     * @param vnfIdInVnfm  the identifier of the VNF in VNFM
     * @param request      the termination request
     * @param httpResponse the HTTP response
     * @return the job for polling the progress of the termination
     */
    public JobInfo terminateAndDelete(String vnfmId, String vnfIdInVnfm, VnfTerminateRequest request, HttpServletResponse httpResponse) {
        logOperationInput(vnfIdInVnfm, "termination", request);
        return scheduleExecution(vnfIdInVnfm, httpResponse, "terminateVnf", jobInfo -> {
            terminateVnf(vnfmId, vnfIdInVnfm, request, jobInfo);
            deleteVnf(vnfmId, vnfIdInVnfm);
        });
    }

    /**
     * Terminates the VNF
     * <ul>
     * <li>fails if the VNF does not exist</li>
     * <li>terminates if instantiated</li>
     * <li>deletes the VNF</li>
     * </ul>
     *
     * @param vnfmId       the identifier of the VNFM
     * @param vnfId        the identifier of the VNF
     * @param request      the termination request
     * @param httpResponse the HTTP response
     * @return the job for polling the progress of the termination
     */
    public JobInfo terminate(String vnfmId, String vnfId, VnfTerminateRequest request, HttpServletResponse httpResponse) {
        logOperationInput(vnfId, "termination", request);
        return scheduleExecution(vnfId, httpResponse, "terminate", jobInfo -> terminateVnf(vnfmId, vnfId, request, jobInfo));
    }

    private void terminateVnf(String vnfmId, String vnfId, VnfTerminateRequest request, JobInfo jobInfo) {
        TerminateVnfRequest cbamRequest = new TerminateVnfRequest();
        setState(request, cbamRequest);
        cbamRequest.setAdditionalParams(new Gson().toJsonTree(jobInfo).getAsJsonObject());
        com.nokia.cbam.lcm.v32.model.VnfInfo vnf = cbamRestApiProvider.getCbamLcmApi(vnfmId).vnfsVnfInstanceIdGet(vnfId, NOKIA_LCM_API_VERSION).blockingFirst();
        if (vnf.getInstantiationState() == INSTANTIATED) {
            String vimId = getVimIdFromInstantiationRequest(vnfmId, vnf);
            grantManager.requestGrantForTerminate(vnfmId, vnfId, vimId, getVnfdIdFromModifyableAttributes(vnf), vnf, jobInfo.getJobId());
            OperationExecution terminationOperation = cbamRestApiProvider.getCbamLcmApi(vnfmId).vnfsVnfInstanceIdTerminatePost(vnfId, cbamRequest, NOKIA_LCM_API_VERSION).blockingFirst();
            OperationExecution finishedOperation = waitForOperationToFinish(vnfmId, vnfId, terminationOperation.getId());
            if (finishedOperation.getStatus() == FINISHED) {
                notificationManager.waitForTerminationToBeProcessed(finishedOperation.getId());
            } else {
                throw buildFatalFailure(logger, "Unable to terminate VNF the operation did not finish with success");
            }
        } else {
            logger.warn("The VNF with {} identifier is not instantiated no termination is required", vnfId);
        }
    }

    private void setState(VnfTerminateRequest request, TerminateVnfRequest cbamRequest) {
        if (request.getTerminationType() == null) {
            cbamRequest.setTerminationType(TerminationType.FORCEFUL);
        } else {
            if (request.getTerminationType().equals(VnfTerminationType.GRACEFUL)) {
                cbamRequest.setTerminationType(TerminationType.GRACEFUL);
                cbamRequest.setGracefulTerminationTimeout(parseInt(request.getGracefulTerminationTimeout()));
            } else {
                cbamRequest.setTerminationType(TerminationType.FORCEFUL);
            }
        }
    }

    /**
     * Delete the VNF
     *
     * @param vnfmId the identifier of the VNFM
     * @param vnfIdInVnfm  the identifier fo the VNF
     */
    public void deleteVnf(String vnfmId, String vnfIdInVnfm) {
        logger.info("Deleting VNF with {} identifier", vnfIdInVnfm);
        cbamRestApiProvider.getCbamLcmApi(vnfmId).vnfsVnfInstanceIdDelete(vnfIdInVnfm, NOKIA_LCM_API_VERSION).blockingFirst(null);
        logger.info("The VNF with {} identifier has been deleted", vnfIdInVnfm);
    }

    private String getVimIdFromInstantiationRequest(String vnfmId, com.nokia.cbam.lcm.v32.model.VnfInfo vnf) {
        OperationExecution lastInstantiation = findLastInstantiation(vnf.getOperationExecutions());
        Object operationParameters = cbamRestApiProvider.getCbamOperationExecutionApi(vnfmId).operationExecutionsOperationExecutionIdOperationParamsGet(lastInstantiation.getId(), NOKIA_LCM_API_VERSION).blockingFirst();
        JsonObject root = new Gson().toJsonTree(operationParameters).getAsJsonObject();
        return childElement(childElement(root, "vims").getAsJsonArray().get(0).getAsJsonObject(), "id").getAsString();
    }

    /**
     * @param vnfmId the identifier of the VNFM
     * @param vnfId  the identifier of the VNF
     * @return the current state of the VNF
     */
    public VnfInfo queryVnf(String vnfmId, String vnfId) {
        try {
            com.nokia.cbam.lcm.v32.model.VnfInfo cbamVnfInfo = cbamRestApiProvider.getCbamLcmApi(vnfmId).vnfsVnfInstanceIdGet(vnfId, NOKIA_LCM_API_VERSION).blockingFirst();
            return convertVnfInfo(vnfId, cbamVnfInfo);
        } catch (Exception e) {
            throw buildFatalFailure(logger, "Unable to query VNF (" + vnfId + ")", e);
        }
    }

    private VnfInfo convertVnfInfo(String vnfId, com.nokia.cbam.lcm.v32.model.VnfInfo cbamVnfInfo) {
        VnfInfo vnfInfo = new VnfInfo();
        vnfInfo.setVersion(cbamVnfInfo.getVnfSoftwareVersion());
        vnfInfo.setVnfInstanceId(vnfId);
        String onapCsarId = getVnfdIdFromModifyableAttributes(cbamVnfInfo);
        vnfInfo.setVnfdId(onapCsarId);
        vnfInfo.setVnfPackageId(onapCsarId);
        vnfInfo.setVnfInstanceDescription(cbamVnfInfo.getDescription());
        vnfInfo.setVnfInstanceName(cbamVnfInfo.getName());
        vnfInfo.setVnfProvider(cbamVnfInfo.getVnfProvider());
        vnfInfo.setVnfStatus("ACTIVE");
        vnfInfo.setVnfType("Kuku");
        return vnfInfo;
    }

    private ScaleDirection convert(org.onap.vnfmdriver.model.ScaleDirection direction) {
        if (org.onap.vnfmdriver.model.ScaleDirection.IN.equals(direction)) {
            return ScaleDirection.IN;
        } else {
            return ScaleDirection.OUT;
        }
    }

    /**
     * Scale the VNF
     *
     * @param vnfmId       the identifier of the VNFM
     * @param vnfId        the identifier of the VNF
     * @param request      the scale request
     * @param httpResponse the HTTP response
     * @return the job for tracking the scale
     */
    public JobInfo scaleVnf(String vnfmId, String vnfId, VnfScaleRequest request, HttpServletResponse httpResponse) {
        logOperationInput(vnfId, SCALE_OPERATION_NAME, request);
        return scheduleExecution(vnfId, httpResponse, SCALE_OPERATION_NAME, jobInfo -> {
            ScaleVnfRequest cbamRequest = new ScaleVnfRequest();
            cbamRequest.setAspectId(request.getAspectId());
            cbamRequest.setNumberOfSteps(Integer.valueOf(request.getNumberOfSteps()));
            cbamRequest.setType(convert(request.getType()));
            com.nokia.cbam.lcm.v32.model.VnfInfo vnf = cbamRestApiProvider.getCbamLcmApi(vnfmId).vnfsVnfInstanceIdGet(vnfId, NOKIA_LCM_API_VERSION).blockingFirst();
            JsonObject root = new Gson().toJsonTree(jobInfo).getAsJsonObject();
            com.nokia.cbam.lcm.v32.model.VnfInfo cbamVnfInfo = cbamRestApiProvider.getCbamLcmApi(vnfmId).vnfsVnfInstanceIdGet(vnfId, NOKIA_LCM_API_VERSION).blockingFirst();
            String vnfdContent = catalogManager.getCbamVnfdContent(vnfmId, cbamVnfInfo.getVnfdId());
            Set<Map.Entry<String, JsonElement>> acceptableOperationParameters = getAcceptableOperationParameters(vnfdContent, SCALE_OPERATION_NAME);
            buildAdditionalParameters(request, root, acceptableOperationParameters);
            cbamRequest.setAdditionalParams(root);
            grantManager.requestGrantForScale(vnfmId, vnfId, getVimIdFromInstantiationRequest(vnfmId, vnf), getVnfdIdFromModifyableAttributes(vnf), request, jobInfo.getJobId());
            OperationExecution operationExecution = cbamRestApiProvider.getCbamLcmApi(vnfmId).vnfsVnfInstanceIdScalePost(vnfId, cbamRequest, NOKIA_LCM_API_VERSION).blockingFirst();
            waitForOperationToFinish(vnfmId, vnfId, operationExecution.getId());
        });
    }

    private void buildAdditionalParameters(VnfScaleRequest request, JsonObject root, Set<Map.Entry<String, JsonElement>> acceptableOperationParameters) {
        if (request.getAdditionalParam() != null) {
            for (Map.Entry<String, JsonElement> item : new Gson().toJsonTree(request.getAdditionalParam()).getAsJsonObject().entrySet()) {
                if (isParameterAccepted(acceptableOperationParameters, item)) {
                    root.add(item.getKey(), item.getValue());
                }
            }
        } else {
            logger.warn("No additional parameters were passed for scaling");
        }
    }

    private boolean isParameterAccepted(Set<Map.Entry<String, JsonElement>> acceptableOperationParameters, Map.Entry<String, JsonElement> item) {
        boolean found = false;
        for (Map.Entry<String, JsonElement> acceptableOperationParameter : acceptableOperationParameters) {
            if (acceptableOperationParameter.getKey().equals(item.getKey())) {
                found = true;
            }
        }
        return found;
    }

    /**
     * Heal the VNF
     *
     * @param vnfmId       the identifier of the VNFM
     * @param vnfId        the identifier of the VNF
     * @param request      the heal request
     * @param httpResponse the HTTP response
     * @param vnfcId       the identifer of thr VNFC to be healed
     * @return the job for tracking the heal
     */
    public JobInfo healVnf(String vnfmId, String vnfId, VnfHealRequest request, Optional<String> vnfcId, HttpServletResponse httpResponse) {
        logOperationInput(vnfId, "heal", request);
        return scheduleExecution(vnfId, httpResponse, "heal", job -> {
            HealVnfRequest cbamHealRequest = new HealVnfRequest();
            Map<String, String> additionalParams = new HashMap<>();
            additionalParams.put("vmName", request.getAffectedvm().getVmname());
            additionalParams.put("action", request.getAction());
            additionalParams.put("jobId", job.getJobId());
            additionalParams.put("vnfcId", vnfcId.orElse("unknown"));
            cbamHealRequest.setAdditionalParams(additionalParams);
            com.nokia.cbam.lcm.v32.model.VnfInfo vnf = cbamRestApiProvider.getCbamLcmApi(vnfmId).vnfsVnfInstanceIdGet(vnfId, NOKIA_LCM_API_VERSION).blockingFirst();
            String vimId = getVimIdFromInstantiationRequest(vnfmId, vnf);
            grantManager.requestGrantForHeal(vnfmId, vnfId, vimId, getVnfdIdFromModifyableAttributes(vnf), request, job.getJobId());
            OperationExecution operationExecution = cbamRestApiProvider.getCbamLcmApi(vnfmId).vnfsVnfInstanceIdHealPost(vnfId, cbamHealRequest, NOKIA_LCM_API_VERSION).blockingFirst();
            waitForOperationToFinish(vnfmId, vnfId, operationExecution.getId());
        });
    }

    public JobInfo customOperation(String vnfmId, String vnfId, String operationId, Object additionalParams, HttpServletResponse httpResponse) {
        logOperationInput(vnfId, "custom", additionalParams);
        return scheduleExecution(vnfId, httpResponse, "custom", job -> {
            CustomOperationRequest cbamRequest = new CustomOperationRequest();
            cbamRequest.setAdditionalParams(additionalParams);
            OperationExecution operationExecution = cbamRestApiProvider.getCbamLcmApi(vnfmId).vnfsVnfInstanceIdCustomCustomOperationNamePost(vnfId, operationId, cbamRequest, NOKIA_LCM_API_VERSION).blockingFirst();
            waitForOperationToFinish(vnfmId, vnfId, operationExecution.getId());
        });
    }

    private JobInfo scheduleExecution(String vnfId, HttpServletResponse httpResponse, String operation, AsynchronousExecution asynchronExecution) {
        JobInfo jobInfo = new JobInfo();
        jobInfo.setJobId(jobManager.spawnJob(vnfId, httpResponse));
        executorService.submit(() -> {
            try {
                asynchronExecution.execute(jobInfo);
            } catch (RuntimeException e) {
                logger.error("Unable to " + operation + " VNF with " + vnfId + " identifier", e);
                jobManager.jobFinished(jobInfo.getJobId());
                throw e;
            }
            jobManager.jobFinished(jobInfo.getJobId());
        });
        return jobInfo;
    }

    private OperationExecution waitForOperationToFinish(String vnfmId, String vnfId, String operationExecutionId) {
        while (true) {
            try {
                OperationExecution operationExecution = find(cbamRestApiProvider.getCbamLcmApi(vnfmId).vnfsVnfInstanceIdOperationExecutionsGet(vnfId, NOKIA_LCM_API_VERSION).blockingFirst(), opEx -> operationExecutionId.equals(opEx.getId()));
                if (hasOperationFinished(operationExecution)) {
                    logger.debug("Operation finished with " + operationExecution.getId());
                    return operationExecution;
                }
            } catch (Exception e) {
                //swallow exception and retry
                logger.warn("Unable to retrieve operations details", e);
            }
            systemFunctions().sleep(OPERATION_STATUS_POLLING_INTERVAL_IN_MS);
        }
    }

    private boolean hasOperationFinished(OperationExecution operationExecution) {
        return newHashSet(FINISHED, OperationStatus.FAILED).contains(operationExecution.getStatus());
    }

    @FunctionalInterface
    private interface AsynchronousExecution {
        void execute(JobInfo job);
    }

    public static class VnfCreationResult {
        private final com.nokia.cbam.lcm.v32.model.VnfInfo vnfInfo;

        private final String vnfdId;

        public VnfCreationResult(com.nokia.cbam.lcm.v32.model.VnfInfo vnfInfo, String vnfdId) {
            this.vnfInfo = vnfInfo;
            this.vnfdId = vnfdId;
        }

        public com.nokia.cbam.lcm.v32.model.VnfInfo getVnfInfo() {
            return vnfInfo;
        }
    }
}
