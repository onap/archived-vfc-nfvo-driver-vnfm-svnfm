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


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nokia.cbam.catalog.v1.model.CatalogAdapterVnfpackage;
import com.nokia.cbam.lcm.v32.ApiException;
import com.nokia.cbam.lcm.v32.model.*;
import com.nokia.cbam.lcm.v32.model.ScaleDirection;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api.IGrantManager;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api.VimInfoProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.StoreLoader;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.UserVisibleError;
import org.onap.vnfmdriver.model.ExtVirtualLinkInfo;
import org.onap.vnfmdriver.model.*;
import org.onap.vnfmdriver.model.VimInfo;
import org.onap.vnfmdriver.model.VnfInfo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.Splitter.on;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static com.nokia.cbam.lcm.v32.model.InstantiationState.INSTANTIATED;
import static com.nokia.cbam.lcm.v32.model.OperationStatus.FINISHED;
import static com.nokia.cbam.lcm.v32.model.OperationType.INSTANTIATE;
import static com.nokia.cbam.lcm.v32.model.VimInfo.VimInfoTypeEnum.*;
import static java.lang.Integer.parseInt;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.*;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.SystemFunctions.systemFunctions;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProvider.NOKIA_LCM_API_VERSION;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.ILifecycleChangeNotificationManager.NEWEST_OPERATIONS_FIRST;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.StringUtils.isEmpty;

/**
 * Responsible for executing lifecycle operation on the VNF
 */
@Component
public class LifecycleManager {
    public static final String ONAP_CSAR_ID = "onapCsarId";
    public static final long OPERATION_STATUS_POLLING_INTERVAL_IN_MS = 5000L;
    /**
     * The key of the CBAM VNF extension for the identifier of the VNFM in ONAP
     */
    public static final String EXTERNAL_VNFM_ID = "externalVnfmId";
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

    @Autowired
    LifecycleManager(CatalogManager catalogManager, IGrantManager grantManager, CbamRestApiProvider restApiProvider, VimInfoProvider vimInfoProvider, JobManager jobManager, ILifecycleChangeNotificationManager notificationManager) {
        this.vimInfoProvider = vimInfoProvider;
        this.grantManager = grantManager;
        this.cbamRestApiProvider = restApiProvider;
        this.jobManager = jobManager;
        this.notificationManager = notificationManager;
        this.catalogManager = catalogManager;
    }

    public static String getRegionName(String vimId) {
        return newArrayList(on(SEPARATOR).split(vimId)).get(1);
    }

    public static String getCloudOwner(String vimId) {
        return newArrayList(on(SEPARATOR).split(vimId)).get(0);
    }

    private static OperationExecution findLastInstantiation(List<OperationExecution> operationExecutions) {
        return find(NEWEST_OPERATIONS_FIRST.sortedCopy(operationExecutions), op -> INSTANTIATE.equals(op.getOperationType()));
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
     * <li>terminate & delete VNf if error occurs after instantiation</li>
     * </ul>
     *
     * @param vnfmId       the identifier of the VNFM
     * @param request      the instantiation request
     * @param httpResponse the HTTP response
     * @return the instantiation response
     */
    public VnfInstantiateResponse instantiate(String vnfmId, VnfInstantiateRequest request, HttpServletResponse httpResponse) {
        if (logger.isDebugEnabled()) {
            logger.debug("Additional parameters for instantiation: {}", new Gson().toJson(request.getAdditionalParam()));
        }
        AdditionalParams additionalParams = convertInstantiationAdditionalParams(request.getVnfPackageId(), request.getAdditionalParam());
        validateVimType(additionalParams);
        CatalogAdapterVnfpackage cbamPackage = catalogManager.preparePackageInCbam(vnfmId, request.getVnfPackageId());
        try {
            CreateVnfRequest vnfCreateRequest = new CreateVnfRequest();
            vnfCreateRequest.setVnfdId(cbamPackage.getId());
            vnfCreateRequest.setName(request.getVnfInstanceName());
            vnfCreateRequest.setDescription(request.getVnfInstanceDescription());
            com.nokia.cbam.lcm.v32.model.VnfInfo vnfInfo = cbamRestApiProvider.getCbamLcmApi(vnfmId).vnfsPost(vnfCreateRequest, NOKIA_LCM_API_VERSION);
            VnfInstantiateResponse response = new VnfInstantiateResponse();
            response.setVnfInstanceId(vnfInfo.getId());
            String vimId = getVimId(request.getAdditionalParam());
            addVnfdIdToVnfModifyableAttributeExtensions(vnfmId, vnfInfo.getId(), request.getVnfPackageId());
            JobInfo spawnJob = scheduleExecution(vnfInfo.getId(), httpResponse, "instantiate", jobInfo ->
                    instantiateVnf(vnfmId, request, additionalParams, cbamPackage, vnfInfo, vimId, jobInfo)
            );
            response.setJobId(spawnJob.getJobId());
            return response;
        } catch (Exception e) {
            throw fatalFailure(logger, "Unable to create the VNF", e);
        }
    }

    private void instantiateVnf(String vnfmId, VnfInstantiateRequest request, AdditionalParams additionalParams, CatalogAdapterVnfpackage cbamPackage, com.nokia.cbam.lcm.v32.model.VnfInfo vnfInfo, String vimId, JobInfo jobInfo) throws ApiException {
        String vnfdContent = catalogManager.getCbamVnfdContent(vnfmId, cbamPackage.getId());
        GrantVNFResponseVim vim = grantManager.requestGrantForInstantiate(vnfmId, vnfInfo.getId(), vimId, request.getVnfPackageId(), additionalParams.getInstantiationLevel(), vnfdContent, jobInfo.getJobId());
        if (vim.getVimId() == null) {
            fatalFailure(logger, "VF-C did not send VIM identifier in grant response");
        }
        VimInfo vimInfo = vimInfoProvider.getVimInfo(vim.getVimId());
        InstantiateVnfRequest instantiationRequest = new InstantiateVnfRequest();
        addExernalLinksToRequest(request.getExtVirtualLink(), additionalParams, instantiationRequest, vimId);
        if (additionalParams.getVimType() == OPENSTACK_V2_INFO) {
            instantiationRequest.getVims().add(buildOpenStackV2INFO(vimId, vim, vimInfo));

        } else if (additionalParams.getVimType() == OPENSTACK_V3_INFO) {
            instantiationRequest.getVims().add(buildOpenStackV3INFO(vimId, additionalParams, vim, vimInfo));

        } else if (additionalParams.getVimType() == VMWARE_VCLOUD_INFO) {
            instantiationRequest.getVims().add(buildVcloudInfo(vimId, vimInfo));

        }
        instantiationRequest.setFlavourId(getFlavorId(vnfdContent));
        instantiationRequest.setComputeResourceFlavours(additionalParams.getComputeResourceFlavours());
        instantiationRequest.setGrantlessMode(true);
        instantiationRequest.setInstantiationLevelId(additionalParams.getInstantiationLevel());
        instantiationRequest.setSoftwareImages(additionalParams.getSoftwareImages());
        instantiationRequest.setZones(additionalParams.getZones());
        instantiationRequest.setExtManagedVirtualLinks(additionalParams.getExtManagedVirtualLinks());
        for (ExtVirtualLinkData extVirtualLinkData : additionalParams.getExtVirtualLinks()) {
            instantiationRequest.addExtVirtualLinksItem(extVirtualLinkData);
        }
        JsonObject root = new Gson().toJsonTree(jobInfo).getAsJsonObject();
        if (additionalParams.getAdditionalParams() != null && !isEmpty(additionalParams.getAdditionalParams().toString())) {
            for (Map.Entry<String, JsonElement> item : new Gson().toJsonTree(additionalParams.getAdditionalParams()).getAsJsonObject().entrySet()) {
                root.add(item.getKey(), item.getValue());
            }
        }
        instantiationRequest.setAdditionalParams(root);
        OperationExecution operationExecution = cbamRestApiProvider.getCbamLcmApi(vnfmId).vnfsVnfInstanceIdInstantiatePost(vnfInfo.getId(), instantiationRequest, NOKIA_LCM_API_VERSION);
        waitForOperationToFinish(vnfmId, vnfInfo.getId(), operationExecution.getId());
    }

    private void validateVimType(AdditionalParams additionalParams) {
        switch (additionalParams.getVimType()) {
            case OPENSTACK_V2_INFO:
            case OPENSTACK_V3_INFO:
            case VMWARE_VCLOUD_INFO:
                break;
            default:
                throw fatalFailure(logger, "Only " + OPENSTACK_V2_INFO + ", " + OPENSTACK_V3_INFO + " and " + VMWARE_VCLOUD_INFO + " is the supported VIM types");
        }
    }

    private String getVimId(Object additionalParams) {
        return childElement(new Gson().toJsonTree(additionalParams).getAsJsonObject(), "vimId").getAsString();
    }

    private AdditionalParams convertInstantiationAdditionalParams(String csarId, Object additionalParams) {
        JsonObject vnfParameters = child(child(new Gson().toJsonTree(additionalParams).getAsJsonObject(), "inputs"), "vnfs");
        if (!vnfParameters.has(csarId)) {
            throw fatalFailure(logger, "The additional parameter section does not contain setting for VNF with " + csarId + " CSAR id");
        }
        JsonElement additionalParamsForVnf = vnfParameters.get(csarId);
        return new Gson().fromJson(additionalParamsForVnf, AdditionalParams.class);
    }

    private String getFlavorId(String vnfdContent) {
        JsonObject root = new Gson().toJsonTree(new Yaml().load(vnfdContent)).getAsJsonObject();
        JsonObject capabilities = child(child(child(root, "topology_template"), "substitution_mappings"), "capabilities");
        JsonObject deploymentFlavorProperties = child(child(capabilities, "deployment_flavour"), "properties");
        return childElement(deploymentFlavorProperties, "flavour_id").getAsString();
    }

    private Set<String> getAcceptableOperationParameters(String vnfdContent, String categroryOfOperation, String operationName) {
        JsonObject root = new Gson().toJsonTree(new Yaml().load(vnfdContent)).getAsJsonObject();
        JsonObject interfaces = child(child(child(root, "topology_template"), "substitution_mappings"), "interfaces");
        JsonObject additionalParameters = child(child(child(child(interfaces, categroryOfOperation), operationName), "inputs"), "additional_parameters");
        return additionalParameters.keySet();
    }

    private void addExernalLinksToRequest(List<ExtVirtualLinkInfo> extVirtualLinks, AdditionalParams additionalParams, InstantiateVnfRequest instantiationRequest, String vimId) {
        for (ExtVirtualLinkInfo extVirtualLink : extVirtualLinks) {
            ExtVirtualLinkData cbamExternalVirtualLink = new ExtVirtualLinkData();
            cbamExternalVirtualLink.setVimId(vimId);
            cbamExternalVirtualLink.setResourceId(extVirtualLink.getResourceId());
            VnfExtCpData ecp = new VnfExtCpData();
            cbamExternalVirtualLink.setExtVirtualLinkId(extVirtualLink.getVlInstanceId());
            cbamExternalVirtualLink.getExtCps().add(ecp);
            ecp.setCpdId(extVirtualLink.getCpdId());
            List<NetworkAddress> addresses = additionalParams.getExternalConnectionPointAddresses().get(extVirtualLink.getCpdId());
            ecp.setAddresses(addresses);
            instantiationRequest.addExtVirtualLinksItem(cbamExternalVirtualLink);
        }
    }

    private void addVnfdIdToVnfModifyableAttributeExtensions(String vnfmId, String vnfId, String onapCsarId) {
        ModifyVnfInfoRequest request = new ModifyVnfInfoRequest();
        VnfProperty onapCsarIdProperty = new VnfProperty();
        onapCsarIdProperty.setName(ONAP_CSAR_ID);
        onapCsarIdProperty.setValue(onapCsarId);
        request.setExtensions(new ArrayList<>());
        request.getExtensions().add(onapCsarIdProperty);
        VnfProperty externalVnfmIdProperty = new VnfProperty();
        externalVnfmIdProperty.setName(EXTERNAL_VNFM_ID);
        externalVnfmIdProperty.setValue(vnfmId);
        request.getExtensions().add(externalVnfmIdProperty);
        request.setVnfConfigurableProperties(null);
        try {
            OperationExecution operationExecution = cbamRestApiProvider.getCbamLcmApi(vnfmId).vnfsVnfInstanceIdPatch(vnfId, request, NOKIA_LCM_API_VERSION);
            waitForOperationToFinish(vnfmId, vnfId, operationExecution.getId());
        } catch (ApiException e) {
            fatalFailure(logger, "Unable to set the " + ONAP_CSAR_ID + " property on the VNF", e);
        }
    }

    private OPENSTACKV3INFO buildOpenStackV3INFO(String vimId, AdditionalParams additionalParams, GrantVNFResponseVim vim, org.onap.vnfmdriver.model.VimInfo vimInfo) {
        OPENSTACKV3INFO openstackv3INFO = new OPENSTACKV3INFO();
        openstackv3INFO.setVimInfoType(OPENSTACK_V3_INFO);
        OpenStackAccessInfoV3 accessInfov3 = new OpenStackAccessInfoV3();
        openstackv3INFO.accessInfo(accessInfov3);
        accessInfov3.setPassword(vimInfo.getPassword());
        accessInfov3.setDomain(additionalParams.getDomain());
        accessInfov3.setProject(vim.getAccessInfo().getTenant());
        accessInfov3.setRegion(getRegionName(vimId));
        accessInfov3.setUsername(vimInfo.getUserName());
        EndpointInfo interfaceInfoV3 = new EndpointInfo();
        interfaceInfoV3.setEndpoint(vimInfo.getUrl());
        if (!isEmpty(vimInfo.getSslInsecure())) {
            interfaceInfoV3.setSkipCertificateVerification(Boolean.parseBoolean(vimInfo.getSslInsecure()));
            interfaceInfoV3.setSkipCertificateHostnameCheck(Boolean.parseBoolean(vimInfo.getSslInsecure()));
        } else {
            interfaceInfoV3.setSkipCertificateHostnameCheck(true);
            interfaceInfoV3.setSkipCertificateVerification(true);
        }
        if (!interfaceInfoV3.isSkipCertificateVerification()) {
            interfaceInfoV3.setTrustedCertificates(new ArrayList<>());
            for (String trustedCertificate : StoreLoader.getCertifacates(vimInfo.getSslCacert())) {
                interfaceInfoV3.getTrustedCertificates().add(trustedCertificate.getBytes(UTF_8));
            }
        }
        openstackv3INFO.setInterfaceInfo(interfaceInfoV3);
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
        openstackv2INFO.setInterfaceInfo(interfaceEndpoint);
        openstackv2INFO.setId(vimId);
        return openstackv2INFO;
    }

    private VMWAREVCLOUDINFO buildVcloudInfo(String vimId, org.onap.vnfmdriver.model.VimInfo vimInfo) {
        VMWAREVCLOUDINFO vcloudInfo = new VMWAREVCLOUDINFO();
        vcloudInfo.setVimInfoType(VMWARE_VCLOUD_INFO);
        VCloudAccessInfo accessInfo = new VCloudAccessInfo();
        vcloudInfo.setAccessInfo(accessInfo);
        accessInfo.setPassword(vimInfo.getPassword());
        accessInfo.setUsername(vimInfo.getUserName());
        accessInfo.setOrganization(getRegionName(vimId));
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
        vcloudInfo.setInterfaceInfo(interfaceEndpoint);
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
     * @param vnfId        the identifier of the VNF
     * @param request      the termination request
     * @param httpResponse the HTTP response
     * @return the job for polling the progress of the termination
     */
    public JobInfo terminateVnf(String vnfmId, String vnfId, VnfTerminateRequest request, HttpServletResponse httpResponse) {
        return scheduleExecution(vnfId, httpResponse, "terminate", jobInfo -> {
            TerminateVnfRequest cbamRequest = new TerminateVnfRequest();
            cbamRequest.setAdditionalParams(jobInfo);
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
            com.nokia.cbam.lcm.v32.model.VnfInfo vnf = cbamRestApiProvider.getCbamLcmApi(vnfmId).vnfsVnfInstanceIdGet(vnfId, NOKIA_LCM_API_VERSION);
            if (vnf.getInstantiationState() == INSTANTIATED) {
                terminateVnf(vnfmId, vnfId, jobInfo, cbamRequest, vnf);

            } else {
                cbamRestApiProvider.getCbamLcmApi(vnfmId).vnfsVnfInstanceIdDelete(vnfId, NOKIA_LCM_API_VERSION);

            }
        });
    }

    private void terminateVnf(String vnfmId, String vnfId, JobInfo jobInfo, TerminateVnfRequest cbamRequest, com.nokia.cbam.lcm.v32.model.VnfInfo vnf) throws ApiException {
        String vimId = getVimIdFromInstantiationRequest(vnfmId, vnf);
        grantManager.requestGrantForTerminate(vnfmId, vnfId, vimId, getVnfdIdFromModifyableAttributes(vnf), vnf, jobInfo.getJobId());
        OperationExecution terminationOperation = cbamRestApiProvider.getCbamLcmApi(vnfmId).vnfsVnfInstanceIdTerminatePost(vnfId, cbamRequest, NOKIA_LCM_API_VERSION);
        OperationExecution finishedOperation = waitForOperationToFinish(vnfmId, vnfId, terminationOperation.getId());
        if (finishedOperation.getStatus() == FINISHED) {
            notificationManager.waitForTerminationToBeProcessed(finishedOperation.getId());
            logger.info("Deleting VNF with {}", vnfId);
            cbamRestApiProvider.getCbamLcmApi(vnfmId).vnfsVnfInstanceIdDelete(vnfId, NOKIA_LCM_API_VERSION);
            logger.info("VNF with {} has been deleted", vnfId);

        } else {
            logger.error("Unable to terminate VNF the operation did not finish with success");
        }
    }

    private String getVimIdFromInstantiationRequest(String vnfmId, com.nokia.cbam.lcm.v32.model.VnfInfo vnf) throws ApiException {
        OperationExecution lastInstantiation = findLastInstantiation(vnf.getOperationExecutions());
        Object operationParameters = cbamRestApiProvider.getCbamOperationExecutionApi(vnfmId).operationExecutionsOperationExecutionIdOperationParamsGet(lastInstantiation.getId(), NOKIA_LCM_API_VERSION);
        JsonObject root = new Gson().toJsonTree(operationParameters).getAsJsonObject();
        return childElement(childElement(root, "vims").getAsJsonArray().get(0).getAsJsonObject(), "id").getAsString();
    }

    private String getVnfdIdFromModifyableAttributes(com.nokia.cbam.lcm.v32.model.VnfInfo vnf) {
        return find(vnf.getExtensions(), p -> p.getName().equals(ONAP_CSAR_ID)).getValue().toString();
    }

    /**
     * @param vnfmId the identifier of the VNFM
     * @param vnfId  the identifier of the VNF
     * @return the current state of the VNF
     */
    public VnfInfo queryVnf(String vnfmId, String vnfId) {
        try {
            com.nokia.cbam.lcm.v32.model.VnfInfo cbamVnfInfo = cbamRestApiProvider.getCbamLcmApi(vnfmId).vnfsVnfInstanceIdGet(vnfId, NOKIA_LCM_API_VERSION);
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
        } catch (ApiException e) {
            throw fatalFailure(logger, "Unable to query VNF (" + vnfId + ")", e);
        }
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
        if (logger.isInfoEnabled()) {
            logger.info("Scale VNF with {} identifier REST: {}", vnfId, new Gson().toJson(request));
        }
        return scheduleExecution(vnfId, httpResponse, "scale", jobInfo -> {
            ScaleVnfRequest cbamRequest = new ScaleVnfRequest();
            cbamRequest.setAspectId(request.getAspectId());
            cbamRequest.setNumberOfSteps(Integer.valueOf(request.getNumberOfSteps()));
            cbamRequest.setType(convert(request.getType()));
            com.nokia.cbam.lcm.v32.model.VnfInfo vnf = cbamRestApiProvider.getCbamLcmApi(vnfmId).vnfsVnfInstanceIdGet(vnfId, NOKIA_LCM_API_VERSION);
            JsonObject root = new Gson().toJsonTree(jobInfo).getAsJsonObject();
            com.nokia.cbam.lcm.v32.model.VnfInfo cbamVnfInfo = cbamRestApiProvider.getCbamLcmApi(vnfmId).vnfsVnfInstanceIdGet(vnfId, NOKIA_LCM_API_VERSION);
            String vnfdContent = catalogManager.getCbamVnfdContent(vnfmId, cbamVnfInfo.getVnfdId());
            Set<String> acceptableOperationParameters = getAcceptableOperationParameters(vnfdContent, "Basic", "scale");
            if (request.getAdditionalParam() != null) {
                for (Map.Entry<String, JsonElement> item : new Gson().toJsonTree(request.getAdditionalParam()).getAsJsonObject().entrySet()) {
                    if (acceptableOperationParameters.contains(item.getKey())) {
                        root.add(item.getKey(), item.getValue());
                    }
                }
            } else {
                logger.warn("No additional parameters were passed for scaling");
            }
            cbamRequest.setAdditionalParams(root);
            grantManager.requestGrantForScale(vnfmId, vnfId, getVimIdFromInstantiationRequest(vnfmId, vnf), getVnfdIdFromModifyableAttributes(vnf), request, jobInfo.getJobId());
            OperationExecution operationExecution = cbamRestApiProvider.getCbamLcmApi(vnfmId).vnfsVnfInstanceIdScalePost(vnfId, cbamRequest, NOKIA_LCM_API_VERSION);
            waitForOperationToFinish(vnfmId, vnfId, operationExecution.getId());
        });
    }

    /**
     * Heal the VNF
     *
     * @param vnfmId       the identifier of the VNFM
     * @param vnfId        the identifier of the VNF
     * @param request      the heal request
     * @param httpResponse the HTTP response
     * @return the job for tracking the heal
     */
    public JobInfo healVnf(String vnfmId, String vnfId, VnfHealRequest request, HttpServletResponse httpResponse) {
        return scheduleExecution(vnfId, httpResponse, "heal", job -> {
            HealVnfRequest cbamHealRequest = new HealVnfRequest();
            Map<String, String> additionalParams = new HashMap<>();
            additionalParams.put("vmName", request.getAffectedvm().getVmname());
            additionalParams.put("action", request.getAction());
            additionalParams.put("jobId", job.getJobId());
            cbamHealRequest.setAdditionalParams(additionalParams);
            com.nokia.cbam.lcm.v32.model.VnfInfo vnf = cbamRestApiProvider.getCbamLcmApi(vnfmId).vnfsVnfInstanceIdGet(vnfId, NOKIA_LCM_API_VERSION);
            String vimId = getVimIdFromInstantiationRequest(vnfmId, vnf);
            grantManager.requestGrantForHeal(vnfmId, vnfId, vimId, getVnfdIdFromModifyableAttributes(vnf), request, job.getJobId());
            OperationExecution operationExecution = cbamRestApiProvider.getCbamLcmApi(vnfmId).vnfsVnfInstanceIdHealPost(vnfId, cbamHealRequest, NOKIA_LCM_API_VERSION);
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
            } catch (Exception e) {
                String msg = "Unable to " + operation + " VNF with " + vnfId + " identifier";
                logger.error(msg, e);
                //the job can only be signaled to be finished after the error is logged
                jobManager.jobFinished(jobInfo.getJobId());
                throw new UserVisibleError(msg, e);
            }
            jobManager.jobFinished(jobInfo.getJobId());
        });
        return jobInfo;
    }

    private OperationExecution waitForOperationToFinish(String vnfmId, String vnfId, String operationExecutionId) {
        while (true) {
            try {
                OperationExecution operationExecution = find(cbamRestApiProvider.getCbamLcmApi(vnfmId).vnfsVnfInstanceIdOperationExecutionsGet(vnfId, NOKIA_LCM_API_VERSION), opEx -> operationExecutionId.equals(opEx.getId()));
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
        void execute(JobInfo job) throws ApiException;
    }
}
