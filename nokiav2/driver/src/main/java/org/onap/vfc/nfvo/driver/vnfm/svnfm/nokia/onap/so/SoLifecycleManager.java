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


import com.nokia.cbam.lcm.v32.model.*;
import com.nokia.cbam.lcm.v32.model.VimInfo;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api.VimInfoProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIExternalSystemInfoProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.notification.GenericVnfManager;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.*;
import org.onap.vnfmadapter.so.model.*;
import org.onap.vnfmdriver.model.ExtVirtualLinkInfo;
import org.onap.vnfmdriver.model.*;
import org.onap.vnfmdriver.model.VnfInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

import static com.nokia.cbam.lcm.v32.model.VimInfo.VimInfoTypeEnum.*;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.notification.VnfcManager.buildCbamId;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProvider.NOKIA_LCM_API_VERSION;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.LifecycleManager.getVnfdIdFromModifyableAttributes;
import static org.onap.vnfmadapter.so.model.SoJobStatus.*;

/**
 * Responsible for providing access to AAI APIs.
 * Handles authentication and mandatory parameters.
 */

@Component
public class SoLifecycleManager {
    private final LifecycleManager lifecycleManager;
    private final VimInfoProvider vimInfoProvider;
    private final CbamRestApiProvider cbamRestApiProvider;
    private final JobManager jobManager;
    private final GenericVnfManager genericVnfManager;


    @Autowired
    SoLifecycleManager(LifecycleManagerForSo lifecycleManager, AAIExternalSystemInfoProvider vimInfoProvider, CbamRestApiProviderForSo cbamRestApiProvider, JobManagerForSo jobManager, GenericVnfManager genericVnfManager) {
        this.lifecycleManager = lifecycleManager;
        this.vimInfoProvider = vimInfoProvider;
        this.cbamRestApiProvider = cbamRestApiProvider;
        this.jobManager = jobManager;
        this.genericVnfManager = genericVnfManager;
    }

    /**
     * Creates the VNF in SO terminology
     *
     * @param vnfmId  the identifier of the VNFM
     * @param request the VNF creation request
     * @return the VNF creation response
     */
    public SoVnfCreationResponse create(String vnfmId, SoVnfCreationRequest request) {
        SoVnfCreationResponse response = new SoVnfCreationResponse();
        LifecycleManager.VnfCreationResult result = lifecycleManager.create(vnfmId, request.getCsarId(), request.getName(), request.getDescription());
        response.setVnfId(result.getVnfInfo().getId());
        genericVnfManager.createOrUpdate(response.getVnfId(), false, vnfmId, ofNullable(request.getNsId()));
        return response;
    }

    /**
     * Activate the VNF in SO terminology
     *
     * @param vnfmId       the identifier of the VNFM
     * @param vnfId        the identifier of the VNF
     * @param soRequest    the VNF activation request
     * @param httpResponse the HTTP response
     * @return the job handler of the VNF activation
     */
    public SoJobHandler activate(String vnfmId, String vnfId, SoVnfActivationRequest soRequest, HttpServletResponse httpResponse) {
        AdditionalParameters additionalParameters = new AdditionalParameters();
        additionalParameters.setAdditionalParams(buildAdditionalParameters(soRequest.getAdditionalParams()));
        String vimId = soRequest.getVimId();
        org.onap.vnfmdriver.model.VimInfo vimInfo = vimInfoProvider.getVimInfo(vimId);
        additionalParameters.setVimType(vimTypeHeuristic(vimInfo.getUrl()));
        processVdus(soRequest, additionalParameters, vimId);
        additionalParameters.setInstantiationLevel("default");
        processNetworks(soRequest, additionalParameters, vimId);
        processZones(soRequest, additionalParameters, vimId);
        com.nokia.cbam.lcm.v32.model.VnfInfo cbamVnfInfo = cbamRestApiProvider.getCbamLcmApi(vnfmId).vnfsVnfInstanceIdGet(vnfId, NOKIA_LCM_API_VERSION).blockingFirst();
        String onapVnfdId = getVnfdIdFromModifyableAttributes(cbamVnfInfo);
        VnfInfo vnfInfo = lifecycleManager.queryVnf(vnfmId, vnfId);
        List<ExtVirtualLinkInfo> externalVirtualLinks = new ArrayList<>();
        VnfInstantiateResponse instantiate = lifecycleManager.instantiate(vnfmId, externalVirtualLinks, httpResponse, soRequest.getAdditionalParams(), additionalParameters, vnfId, onapVnfdId, vnfInfo.getVnfdId());
        return buildJobHandler(instantiate.getJobId());
    }

    /**
     * Scale the VNF
     *
     * @param vnfmId       the identifier of the VNFM
     * @param vnfId        the identifier of the VNF
     * @param soRequest    the VNF scale request
     * @param httpResponse the HTTP response
     * @return the job handler of the VNF activation
     */
    public SoJobHandler scale(String vnfmId, String vnfId, SoVnfScaleRequest soRequest, HttpServletResponse httpResponse) {
        org.onap.vnfmdriver.model.VnfScaleRequest driverRequest = new org.onap.vnfmdriver.model.VnfScaleRequest();
        driverRequest.setAdditionalParam(buildAdditionalParameters(soRequest.getAdditionalParams()));
        driverRequest.setAspectId(soRequest.getAspectId());
        driverRequest.setNumberOfSteps(soRequest.getSteps().toString());
        driverRequest.setType(soRequest.getDirection() == SoScaleDirection.IN ? org.onap.vnfmdriver.model.ScaleDirection.IN : org.onap.vnfmdriver.model.ScaleDirection.OUT);
        return buildJobHandler(lifecycleManager.scaleVnf(vnfmId, vnfId, driverRequest, httpResponse).getJobId());
    }

    /**
     * Heal the VNF
     *
     * @param vnfmId       the identifier of the VNFM
     * @param vnfId        the identifier of the VNF
     * @param request      the VNF heal request
     * @param httpResponse the HTTP response
     * @return the job handler of the VNF activation
     */
    public SoJobHandler heal(String vnfmId, String vnfId, SoVnfHealRequest request, HttpServletResponse httpResponse) {
        org.onap.vnfmdriver.model.VnfHealRequest vnfHealRequest = new org.onap.vnfmdriver.model.VnfHealRequest();
        VnfHealRequestAffectedvm affectedVm = new VnfHealRequestAffectedvm();
        affectedVm.setVimid("notUsedByDriver");
        affectedVm.setVduid("notUsedByDriver");
        affectedVm.setVmname("unknown");
        vnfHealRequest.setAffectedvm(affectedVm);
        vnfHealRequest.setAction("heal");
        return buildJobHandler(lifecycleManager.healVnf(vnfmId, vnfId, vnfHealRequest, of(buildCbamId(request.getVnfcId())), httpResponse).getJobId());
    }

    /**
     * Deactivate the VNF
     *
     * @param vnfmId       the identifier of the VNFM
     * @param vnfId        the identifier of the VNF
     * @param soRequest    the VNF deactivation request
     * @param httpResponse the HTTP response
     * @return the job handler of the VNF activation
     */
    public SoJobHandler deactivate(String vnfmId, String vnfId, SoVnfTerminationRequest soRequest, HttpServletResponse httpResponse) {
        VnfTerminateRequest driverRequest = new VnfTerminateRequest();
        if (soRequest.getMode() == SoTerminationMode.FORCEFUL) {
            driverRequest.setTerminationType(VnfTerminationType.FORCEFUL);
        } else {
            driverRequest.setTerminationType(VnfTerminationType.GRACEFUL);
            driverRequest.setGracefulTerminationTimeout(soRequest.getGracefulTerminationTimeoutInMs().toString());

        }
        return buildJobHandler(lifecycleManager.terminateAndDelete(vnfmId, vnfId, driverRequest, httpResponse).getJobId());
    }

    /**
     * Delete the VNF
     *
     * @param vnfmId the identifier of the VNFM
     * @param vnfId  the identifier of the VNF
     * @return the job handler of the VNF activation
     */
    public void delete(String vnfmId, String vnfId) {
        lifecycleManager.deleteVnf(vnfmId, vnfId);
    }

    /**
     * Execute a custom operation on a VNF
     *
     * @param vnfmId       the identifier of the VNFM
     * @param vnfId        the identifier of the VNF
     * @param request      the VNF custom
     * @param httpResponse the HTTP response
     * @return the job handler of the VNF activation
     */
    public SoJobHandler customOperation(String vnfmId, String vnfId, SoVnfCustomOperation request, HttpServletResponse httpResponse) {
        String operationId = request.getOperationId();
        CustomOperationRequest cbamRequest = new CustomOperationRequest();
        cbamRequest.setAdditionalParams(buildAdditionalParameters(request.getAdditionalParams()));
        return buildJobHandler(lifecycleManager.customOperation(vnfmId, vnfId, operationId, request.getAdditionalParams(), httpResponse).getJobId());
    }

    /**
     * @param jobId  the identifier of the job
     * @param vnfmId the identifier of the VNFM
     * @return the details of the job
     */
    public SoJobDetail getJobDetails(String vnfmId, String jobId) {
        SoJobDetail jobDetail = new SoJobDetail();
        jobDetail.setJobId(jobId);
        JobStatus currentStatus = jobManager.getJob(vnfmId, jobId).getResponseDescriptor().getStatus();
        if (JobStatus.STARTED.equals(currentStatus)) {
            jobDetail.setStatus(STARTED);
        } else if (JobStatus.PROCESSING.equals(currentStatus)) {
            jobDetail.setStatus(STARTED);
        } else if (JobStatus.FINISHED.equals(currentStatus)) {
            jobDetail.setStatus(FINISHED);
        } else if (JobStatus.TIMEOUT.equals(currentStatus)) {
            jobDetail.setStatus(FAILED);
        } else {//ERROR
            jobDetail.setStatus(FAILED);
        }
        return jobDetail;
    }

    private VimInfo.VimInfoTypeEnum vimTypeHeuristic(String url) {
        if (url.contains("/v3")) {
            return OPENSTACK_V3_INFO;
        } else if (url.contains("/v2")) {
            return OPENSTACK_V2_INFO;
        } else {
            return VMWARE_VCLOUD_INFO;
        }
    }

    private Object buildAdditionalParameters(Object additionalParams) {
        return additionalParams;
    }

    private SoJobHandler buildJobHandler(String jobId) {
        SoJobHandler jobHandler = new SoJobHandler();
        jobHandler.setJobId(jobId);
        return jobHandler;
    }

    private void processVdus(SoVnfActivationRequest request, AdditionalParameters additionalParameters, String vimId) {
        if (request.getVduMappings() != null) {
            for (SoVduMapping vduMapping : request.getVduMappings()) {
                VimComputeResourceFlavour flavour = new VimComputeResourceFlavour();
                flavour.setVimId(vimId);
                flavour.setVnfdVirtualComputeDescId(vduMapping.getVduId());
                flavour.setResourceId(vduMapping.getFlavourId());
                additionalParameters.getComputeResourceFlavours().add(flavour);
                VimSoftwareImage image = new VimSoftwareImage();
                image.setVimId(vimId);
                image.setResourceId(vduMapping.getImageId());
                image.setVnfdSoftwareImageId(vduMapping.getVduId() + CbamUtils.SEPARATOR + "image");
                additionalParameters.getSoftwareImages().add(image);
            }
        }
    }

    private void processNetworks(SoVnfActivationRequest request, AdditionalParameters additionalParameters, String vimId) {
        if (request.getNetworkMappings() != null) {
            for (SoNetworkMapping networkMapping : request.getNetworkMappings()) {
                ExtVirtualLinkData extVirtualLinkData = createExtVirtualLinkData(additionalParameters, networkMapping.getVldId());
                extVirtualLinkData.setVimId(vimId);
                extVirtualLinkData.setResourceId(networkMapping.getNetworkProviderId());
                processAssingedAddress(networkMapping, extVirtualLinkData);
            }
        }
    }

    private void processAssingedAddress(SoNetworkMapping networkMapping, ExtVirtualLinkData extVirtualLinkData) {
        if (networkMapping.getAssignedAddresses() != null) {
            for (SoAssignedAddresses assignedAddresses : networkMapping.getAssignedAddresses()) {
                VnfExtCpData extCpData = createExtVirtualLinkData(extVirtualLinkData.getExtCps(), assignedAddresses.getCpdId());
                addMissing(extCpData, assignedAddresses.getIpAddress());
            }
        }
    }

    private void processZones(SoVnfActivationRequest request, AdditionalParameters additionalParameters, String vimId) {
        if (request.getServerMappings() != null) {
            for (SoServerMapping serverMapping : request.getServerMappings()) {
                ZoneInfo zone = locateOrCreateZone(additionalParameters.getZones(), serverMapping.getVduId());
                zone.setResourceId(serverMapping.getAvailabilityZoneId());
                zone.setVimId(vimId);
            }
        }
    }

    private ZoneInfo locateOrCreateZone(List<ZoneInfo> zones, String vduId) {
        for (ZoneInfo zone : zones) {
            if (zone.getId().equals(vduId)) {
                return zone;
            }
        }
        ZoneInfo zoneInfo = new ZoneInfo();
        zoneInfo.setId(vduId);
        zones.add(zoneInfo);
        return zoneInfo;
    }

    private void addMissing(VnfExtCpData extCpData, String ipAddress) {
        if (extCpData.getAddresses() == null) {
            extCpData.setAddresses(new ArrayList<>());
        }
        for (NetworkAddress networkAddress : extCpData.getAddresses()) {
            if (ipAddress.equals(networkAddress.getIp())) {
                return;
            }
        }
        NetworkAddress address = new NetworkAddress();
        address.setIp(ipAddress);
        extCpData.getAddresses().add(address);
    }

    private VnfExtCpData createExtVirtualLinkData(List<VnfExtCpData> extCps, String cpdId) {
        for (VnfExtCpData extCp : extCps) {
            if (extCp.getCpdId().equals(cpdId)) {
                return extCp;
            }
        }
        VnfExtCpData extCp = new VnfExtCpData();
        extCp.setCpdId(cpdId);
        extCps.add(extCp);
        return extCp;
    }

    private ExtVirtualLinkData createExtVirtualLinkData(AdditionalParameters additionalParameters, String virtualLinkId) {
        ExtVirtualLinkData nonExistingVl = new ExtVirtualLinkData();
        nonExistingVl.setExtVirtualLinkId(virtualLinkId);
        additionalParameters.getExtVirtualLinks().add(nonExistingVl);
        return nonExistingVl;
    }
}