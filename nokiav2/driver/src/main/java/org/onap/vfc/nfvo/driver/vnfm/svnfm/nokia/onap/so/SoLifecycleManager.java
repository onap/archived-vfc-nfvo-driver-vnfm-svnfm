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

import com.nokia.cbam.lcm.v32.ApiException;
import com.nokia.cbam.lcm.v32.model.*;
import org.onap.aai.domain.yang.v11.EsrSystemInfo;
import org.onap.aai.domain.yang.v11.Vnfc;
import org.onap.soadapter.model.*;
import org.onap.soadapter.model.ScaleDirection;
import org.onap.soadapter.model.VnfHealRequest;
import org.onap.soadapter.model.VnfScaleRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIExternalSystemInfoProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.notification.VnfcManager;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.restapi.LcmApi;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.spring.Conditions;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.AdditionalParameters;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.LifecycleManager;
import org.onap.vnfmdriver.model.*;
import org.onap.vnfmdriver.model.VnfInfo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static java.util.Optional.of;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIRestApiProvider.AAIService.NETWORK;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.notification.VnfcManager.getCbamVnfcId;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.buildFatalFailure;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProvider.NOKIA_LCM_API_VERSION;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Responsible for providing access to AAI APIs.
 * Handles authentication and mandatory parameters.
 */

@Component
@Conditional(value = Conditions.UseForDirect.class)
public class SoLifecycleManager {
    private static Logger logger = getLogger(SoLifecycleManager.class);
    private final LifecycleManager lifecycleManager;
    private final AAIExternalSystemInfoProvider aaiExternalSystemInfoProvider;
    private final CbamRestApiProvider cbamRestApiProvider;

    @Autowired
    SoLifecycleManager(LifecycleManager lifecycleManager, AAIExternalSystemInfoProvider aaiExternalSystemInfoProvider, CbamRestApiProvider cbamRestApiProvider) {
        this.lifecycleManager = lifecycleManager;
        this.aaiExternalSystemInfoProvider = aaiExternalSystemInfoProvider;
        this.cbamRestApiProvider = cbamRestApiProvider;
    }

    /**
     * Creates the VNF in SO terminology
     * @param vnfmId the identifier of the VNFM
     * @param request the VNF creation request
     * @return the VNF creation response
     */
    public VnfCreationResponse create(String vnfmId, VnfCreationRequest request){
        VnfCreationResponse response = new VnfCreationResponse();
        LifecycleManager.VnfCreationResult result = lifecycleManager.create(vnfmId, request.getCsarId(), request.getName(), request.getDescription());
        response.setVnfId(result.getVnfInfo().getId());
        return response;
    }

    /**
     * Activate the VNF in SO terminology
     * @param vnfmId the identifier of the VNFM
     * @param vnfId the identifier of the VNF
     * @param soRequest the VNF activation request
     * @param httpResponse the HTTP response
     * @return the job handler of the VNF activation
     */
    public JobHandler activate(String vnfmId, String vnfId, VnfActivationRequest soRequest, HttpServletResponse httpResponse){
        VnfInstantiateRequest driverRequest = new VnfInstantiateRequest();
        AdditionalParameters additionalParameters = new AdditionalParameters();
        //FIXME merge additional parameters
        //FIXME document mandatory additional parameters
        // vimType
        String vimId = soRequest.getVimId();
        EsrSystemInfo vimInfo = aaiExternalSystemInfoProvider.getEsrSystemInfo(vimId);
        additionalParameters.setDomain(vimInfo.getCloudDomain());
        processVdus(soRequest, additionalParameters, vimId);
        if(StringUtils.isEmpty(additionalParameters.getInstantiationLevel())){
            //FIXME add documentation
           additionalParameters.setInstantiationLevel("default");
        }
        processNetworks(soRequest, additionalParameters, vimId);
        processZones(soRequest, additionalParameters, vimId);
        VnfInfo vnfInfo = lifecycleManager.queryVnf(vnfmId, vnfId);
        VnfInstantiateResponse instantiate = lifecycleManager.instantiate(vnfmId, driverRequest, httpResponse, additionalParameters, vnfId, vnfInfo.getVnfdId());
        return buildJobHandler(instantiate.getJobId());
    }

    /**
     * Scale the VNF
     * @param vnfmId the identifier of the VNFM
     * @param vnfId the identifier of the VNF
     * @param soRequest the VNF scale request
     * @param httpResponse the HTTP response
     * @return
     */
    public JobHandler scale(String vnfmId, String vnfId, VnfScaleRequest soRequest, HttpServletResponse httpResponse){
        org.onap.vnfmdriver.model.VnfScaleRequest driverRequest = new org.onap.vnfmdriver.model.VnfScaleRequest();
        //FIXME merge additional parameters
        driverRequest.setAspectId(soRequest.getAspectId());
        driverRequest.setNumberOfSteps(soRequest.getSteps().toString());
        driverRequest.setType(soRequest.getDirection() == ScaleDirection.IN? org.onap.vnfmdriver.model.ScaleDirection.IN: org.onap.vnfmdriver.model.ScaleDirection.OUT);
        return buildJobHandler(lifecycleManager.scaleVnf(vnfmId, vnfId, driverRequest, httpResponse).getJobId());
    }

    /**
     *
     * @param vnfmId the identifier of the VNFM
     * @param vnfId the identifier of the VNF
     * @param request the VNF heal request
     * @param httpResponse the HTTP response
     * @return
     */
    public JobHandler heal(String vnfmId, String vnfId, VnfHealRequest request, HttpServletResponse httpResponse){
        org.onap.vnfmdriver.model.VnfHealRequest vnfHealRequest = new org.onap.vnfmdriver.model.VnfHealRequest();
        VnfHealRequestAffectedvm affectedVm = new VnfHealRequestAffectedvm();
        //FIXME merge additional parameters
        affectedVm.setVimid("notUsedByDriver");
        affectedVm.setVduid("notUsedByDriver");
        affectedVm.setVmname("unknown");
        vnfHealRequest.setAffectedvm(affectedVm);
        vnfHealRequest.setAction("heal");
        return buildJobHandler(lifecycleManager.healVnf(vnfmId, vnfId, vnfHealRequest, of(getCbamVnfcId(request.getVnfcId())), httpResponse).getJobId());
    }

    /**
     *
     * @param vnfmId the identifier of the VNFM
     * @param vnfId
     * @param soRequest the VNF deactivation request
     * @param httpResponse the HTTP response
     * @return
     */
    public JobHandler deactivate(String vnfmId, String vnfId, VnfTerminationRequest soRequest, HttpServletResponse httpResponse){
        VnfTerminateRequest driverRequest = new VnfTerminateRequest();
        driverRequest.setTerminationType(soRequest.getMode()== TerminationMode.FORCEFUL? VnfTerminationType.FORCEFUL: VnfTerminationType.GRACEFUL);
        return buildJobHandler(lifecycleManager.terminateVnf(vnfmId, vnfId, driverRequest, httpResponse).getJobId());
    }

    /**
     *
     * @param vnfmId the identifier of the VNFM
     * @param vnfId
     * @param request the VNF custom
     * @return
     */
    public JobHandler customOperation(String vnfmId, String vnfId, VnfCustomOperation request){
        String operationId = request.getOperationId();
        //FIXME merge additional parameters
        CustomOperationRequest cbamRequest = new CustomOperationRequest();
        try {
            return buildJobHandler(cbamRestApiProvider.getCbamLcmApi(vnfmId).vnfsVnfInstanceIdCustomCustomOperationNamePost(vnfId, operationId, cbamRequest, NOKIA_LCM_API_VERSION).getId());
        } catch (ApiException e) {
            throw buildFatalFailure(logger, "Unable to execute custom opertion", e);
        }
    }

    private JobHandler buildJobHandler(String jobId) {
        JobHandler jobHandler = new JobHandler();
        jobHandler.setJobId(jobId);
        return jobHandler;
    }

    private void processVdus(VnfActivationRequest request, AdditionalParameters additionalParameters, String vimId) {
        for (VduMapping vduMapping : request.getVduMappings()) {
            VimComputeResourceFlavour flavour = new VimComputeResourceFlavour();
            flavour.setVimId(vimId);
            flavour.setVnfdVirtualComputeDescId(vduMapping.getVduId());
            flavour.setResourceId(vduMapping.getFlavourId());
            additionalParameters.getComputeResourceFlavours().add(flavour);
            VimSoftwareImage image = new VimSoftwareImage();
            image.setVimId(vimId);
            image.setResourceId(vduMapping.getImageId());
            //FIXME add documentation note that each VDU must have aaiRestApiProvider corresponding image names VDUNAME_image
            image.setVnfdSoftwareImageId(vduMapping.getVduId() + CbamUtils.SEPARATOR + "image");
            additionalParameters.getSoftwareImages().add(image);
        }
    }

    private void processNetworks(VnfActivationRequest request, AdditionalParameters additionalParameters, String vimId) {
        if(additionalParameters.getExtVirtualLinks() == null){
            additionalParameters.setExtVirtualLinks(new ArrayList<>());
        }
        for (NetworkMapping networkMapping : request.getNetworkMappings()) {
            ExtVirtualLinkData extVirtualLinkData = locateOrCreate(additionalParameters, networkMapping.getVldId());
            extVirtualLinkData.setVimId(vimId);
            extVirtualLinkData.setResourceId(networkMapping.getNetworkProviderId());
            if(extVirtualLinkData.getExtCps() == null){
                extVirtualLinkData.setExtCps(new ArrayList<>());
            }
            for (AssignedAddresses assignedAddresses : networkMapping.getAssignedAddresses()) {
                VnfExtCpData extCpData = locateOrCreate(extVirtualLinkData.getExtCps(), assignedAddresses.getCpdId());
                addMissing(extCpData, assignedAddresses.getIpAddress());
            }
        }
    }

    private void processZones(VnfActivationRequest request, AdditionalParameters additionalParameters, String vimId) {
        if(additionalParameters.getZones() == null){
            additionalParameters.setZones(new ArrayList<>());
        }
        for (ServerMapping serverMapping : request.getServerMappings()) {
            ZoneInfo zone = locateOrCreateZone(additionalParameters.getZones(), serverMapping.getVduId());
            zone.setResourceId(serverMapping.getAvailabilityZoneId());
            zone.setVimId(vimId);
        }
    }

    private ZoneInfo locateOrCreateZone(List<ZoneInfo> zones, String vduId) {
        for (ZoneInfo zone : zones) {
            if(zone.getId() == vduId){
                return zone;
            }
        }
        ZoneInfo zoneInfo = new ZoneInfo();
        zoneInfo.setId(vduId);
        zones.add(zoneInfo);
        return zoneInfo;
    }

    private void addMissing(VnfExtCpData extCpData, String ipAddress) {
        if(extCpData.getAddresses() == null){
            extCpData.setAddresses(new ArrayList<>());
        }
        for (NetworkAddress networkAddress : extCpData.getAddresses()) {
         if(ipAddress.equals(networkAddress.getIp())){
             return;
         }
        }
        NetworkAddress address = new NetworkAddress();
        address.setIp(ipAddress);
        extCpData.getAddresses().add(address);
    }

    private VnfExtCpData locateOrCreate(List<VnfExtCpData> extCps, String cpdId){
        for (VnfExtCpData extCp : extCps) {
            if(extCp.getCpdId().equals(cpdId)){
                return extCp;
            }
        }
        VnfExtCpData extCp = new VnfExtCpData();
        extCp.setCpdId(cpdId);
        extCps.add(extCp);
        return extCp;
    }

    private ExtVirtualLinkData locateOrCreate(AdditionalParameters additionalParameters, String virtualLinkId){
        for (ExtVirtualLinkData linkData : additionalParameters.getExtVirtualLinks()) {
            if(virtualLinkId.equals(linkData.getExtVirtualLinkId())){
                return linkData;
            }
        }
        ExtVirtualLinkData nonExistingVl = new ExtVirtualLinkData();
        nonExistingVl.setExtVirtualLinkId(virtualLinkId);
        additionalParameters.getExtVirtualLinks().add(nonExistingVl);
        return nonExistingVl;
    }
}