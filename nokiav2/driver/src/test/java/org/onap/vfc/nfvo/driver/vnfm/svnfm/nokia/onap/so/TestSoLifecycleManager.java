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

import com.google.gson.JsonObject;
import com.nokia.cbam.lcm.v32.model.*;
import com.nokia.cbam.lcm.v32.model.VnfInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.onap.aai.model.EsrSystemInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIExternalSystemInfoProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.AdditionalParameters;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.JobManager;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.LifecycleManager;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;
import org.onap.vnfmadapter.so.model.*;
import org.onap.vnfmadapter.so.model.ScaleDirection;
import org.onap.vnfmadapter.so.model.VnfHealRequest;
import org.onap.vnfmadapter.so.model.VnfScaleRequest;
import org.onap.vnfmdriver.model.*;
import org.onap.vnfmdriver.model.ExtVirtualLinkInfo;
import org.onap.vnfmdriver.model.JobStatus;

import static com.nokia.cbam.lcm.v32.model.VimInfo.VimInfoTypeEnum.OPENSTACK_V2_INFO;
import static com.nokia.cbam.lcm.v32.model.VimInfo.VimInfoTypeEnum.OPENSTACK_V3_INFO;
import static com.nokia.cbam.lcm.v32.model.VimInfo.VimInfoTypeEnum.VMWARE_VCLOUD_INFO;
import static junit.framework.TestCase.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProvider.NOKIA_LCM_API_VERSION;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class TestSoLifecycleManager extends TestBase {

    private static final String VNFD_ID = "cbamVnfdId";
    @Mock
    private LifecycleManager lifecycleManager;
    @Mock
    private AAIExternalSystemInfoProvider aaiExternalSystemInfoProvider;
    @Mock
    private JobManager jobManager;
    private SoLifecycleManager soLifecycleManager;

    @Before
    public void init() {
        setField(SoLifecycleManager.class, "logger", logger);
        soLifecycleManager = new SoLifecycleManager(lifecycleManager, aaiExternalSystemInfoProvider, cbamRestApiProvider, jobManager);
    }

    /**
     * test VNF creation
     */
    @Test
    public void testVnfCreation() throws Exception {
        VnfCreationRequest onapRequest = new VnfCreationRequest();
        Object additionalParams = new JsonObject();
        onapRequest.setAdditionalParams(additionalParams);
        onapRequest.setCsarId("csarId");
        onapRequest.setDescription("description");
        onapRequest.setName("name");
        VnfInfo vnfInfo = new VnfInfo();
        vnfInfo.setId(VNF_ID);
        LifecycleManager.VnfCreationResult genericResponse = new LifecycleManager.VnfCreationResult(vnfInfo, VNFD_ID);
        when(lifecycleManager.create(VNFM_ID, "csarId", "name", "description")).thenReturn(genericResponse);
        //when
        VnfCreationResponse response = soLifecycleManager.create(VNFM_ID, onapRequest);
        //verify
        assertEquals(VNF_ID, response.getVnfId());
    }

    /**
     * test VNF activation without parameters
     */
    @Test
    public void testVnfActivation() throws Exception {
        VnfActivationRequest soRequest = new VnfActivationRequest();
        soRequest.setVimId(VIM_ID);

        JsonObject additionalParams = new JsonObject();
        soRequest.setAdditionalParams(additionalParams);
        org.onap.vnfmdriver.model.VnfInfo vnfInfo = new org.onap.vnfmdriver.model.VnfInfo();
        vnfInfo.setVnfdId(VNFD_ID);
        when(lifecycleManager.queryVnf(VNFM_ID, VNF_ID)).thenReturn(vnfInfo);
        ArgumentCaptor<List<ExtVirtualLinkInfo>> extLinks= ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<AdditionalParameters> additionalParameters = ArgumentCaptor.forClass(AdditionalParameters.class);
        VnfInstantiateResponse instantiationResponse = new VnfInstantiateResponse();
        instantiationResponse.setJobId(JOB_ID);
        when(lifecycleManager.instantiate(eq(VNFM_ID), extLinks.capture(), eq(httpResponse), eq(additionalParams), additionalParameters.capture(), eq(VNF_ID), eq("csarId"), eq(VNFD_ID))).thenReturn(instantiationResponse);
        EsrSystemInfo esrInfo = new EsrSystemInfo();
        esrInfo.setServiceUrl("http://localhost:123/v3");
        esrInfo.setCloudDomain("domain");
        when(aaiExternalSystemInfoProvider.getEsrSystemInfo(VIM_ID)).thenReturn(esrInfo);
        VnfInfo cbamVnfInfo = new VnfInfo();
        cbamVnfInfo.setExtensions(new ArrayList<>());
        VnfProperty onapVnfdId = new VnfProperty();
        cbamVnfInfo.getExtensions().add(onapVnfdId);
        onapVnfdId.setName(LifecycleManager.ONAP_CSAR_ID);
        onapVnfdId.setValue("csarId");
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(cbamVnfInfo));
        //when
        JobHandler jobHandler = soLifecycleManager.activate(VNFM_ID, VNF_ID, soRequest, httpResponse);
        //verify
        assertEquals(JOB_ID, jobHandler.getJobId());
        AdditionalParameters actualAdditionalParameters = additionalParameters.getValue();
        assertEquals(0, actualAdditionalParameters.getComputeResourceFlavours().size());
        assertEquals(additionalParams, actualAdditionalParameters.getAdditionalParams());
        assertEquals(0, actualAdditionalParameters.getExternalConnectionPointAddresses().size());
        assertEquals(0, actualAdditionalParameters.getExtManagedVirtualLinks().size());
        assertEquals(0, actualAdditionalParameters.getExtVirtualLinks().size());
        assertEquals("default", actualAdditionalParameters.getInstantiationLevel());
        assertEquals(0, actualAdditionalParameters.getSoftwareImages().size());
        assertEquals(OPENSTACK_V3_INFO, actualAdditionalParameters.getVimType());
        assertEquals(0, actualAdditionalParameters.getZones().size());
        assertEquals("domain", actualAdditionalParameters.getDomain());
        assertEquals(0, extLinks.getValue().size());
    }


    /**
     * test VNF activation without parameters for V2 based API
     */
    @Test
    public void testVnfActivationForV2() throws Exception {
        VnfActivationRequest soRequest = new VnfActivationRequest();
        soRequest.setVimId(VIM_ID);

        JsonObject additionalParams = new JsonObject();
        soRequest.setAdditionalParams(additionalParams);
        org.onap.vnfmdriver.model.VnfInfo vnfInfo = new org.onap.vnfmdriver.model.VnfInfo();
        vnfInfo.setVnfdId(VNFD_ID);
        when(lifecycleManager.queryVnf(VNFM_ID, VNF_ID)).thenReturn(vnfInfo);
        ArgumentCaptor<List<ExtVirtualLinkInfo>> extLinks= ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<AdditionalParameters> additionalParameters = ArgumentCaptor.forClass(AdditionalParameters.class);
        VnfInstantiateResponse instantiationResponse = new VnfInstantiateResponse();
        instantiationResponse.setJobId(JOB_ID);
        when(lifecycleManager.instantiate(eq(VNFM_ID), extLinks.capture(), eq(httpResponse), eq(additionalParams), additionalParameters.capture(), eq(VNF_ID), eq("csarId"), eq(VNFD_ID))).thenReturn(instantiationResponse);
        EsrSystemInfo esrInfo = new EsrSystemInfo();
        esrInfo.setServiceUrl("http://localhost:123/v2");
        when(aaiExternalSystemInfoProvider.getEsrSystemInfo(VIM_ID)).thenReturn(esrInfo);
        VnfInfo cbamVnfInfo = new VnfInfo();
        cbamVnfInfo.setExtensions(new ArrayList<>());
        VnfProperty onapVnfdId = new VnfProperty();
        cbamVnfInfo.getExtensions().add(onapVnfdId);
        onapVnfdId.setName(LifecycleManager.ONAP_CSAR_ID);
        onapVnfdId.setValue("csarId");
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(cbamVnfInfo));
        //when
        JobHandler jobHandler = soLifecycleManager.activate(VNFM_ID, VNF_ID, soRequest, httpResponse);
        //verify
        assertEquals(JOB_ID, jobHandler.getJobId());
        AdditionalParameters actualAdditionalParameters = additionalParameters.getValue();
        assertEquals(OPENSTACK_V2_INFO, actualAdditionalParameters.getVimType());
        assertEquals(null, actualAdditionalParameters.getDomain());
    }

    /**
     * test VNF activation without parameters for vCloud based API
     */
    @Test
    public void testVnfActivationForVcloud() throws Exception {
        VnfActivationRequest soRequest = new VnfActivationRequest();
        soRequest.setVimId(VIM_ID);

        JsonObject additionalParams = new JsonObject();
        soRequest.setAdditionalParams(additionalParams);
        org.onap.vnfmdriver.model.VnfInfo vnfInfo = new org.onap.vnfmdriver.model.VnfInfo();
        vnfInfo.setVnfdId(VNFD_ID);
        when(lifecycleManager.queryVnf(VNFM_ID, VNF_ID)).thenReturn(vnfInfo);
        ArgumentCaptor<List<ExtVirtualLinkInfo>> extLinks= ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<AdditionalParameters> additionalParameters = ArgumentCaptor.forClass(AdditionalParameters.class);
        VnfInstantiateResponse instantiationResponse = new VnfInstantiateResponse();
        instantiationResponse.setJobId(JOB_ID);
        when(lifecycleManager.instantiate(eq(VNFM_ID), extLinks.capture(), eq(httpResponse), eq(additionalParams), additionalParameters.capture(), eq(VNF_ID), eq("csarId"), eq(VNFD_ID))).thenReturn(instantiationResponse);
        EsrSystemInfo esrInfo = new EsrSystemInfo();
        esrInfo.setServiceUrl("http://localhost:123/");
        when(aaiExternalSystemInfoProvider.getEsrSystemInfo(VIM_ID)).thenReturn(esrInfo);
        VnfInfo cbamVnfInfo = new VnfInfo();
        cbamVnfInfo.setExtensions(new ArrayList<>());
        VnfProperty onapVnfdId = new VnfProperty();
        cbamVnfInfo.getExtensions().add(onapVnfdId);
        onapVnfdId.setName(LifecycleManager.ONAP_CSAR_ID);
        onapVnfdId.setValue("csarId");
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(cbamVnfInfo));
        //when
        JobHandler jobHandler = soLifecycleManager.activate(VNFM_ID, VNF_ID, soRequest, httpResponse);
        //verify
        assertEquals(JOB_ID, jobHandler.getJobId());
        AdditionalParameters actualAdditionalParameters = additionalParameters.getValue();
        assertEquals(VMWARE_VCLOUD_INFO, actualAdditionalParameters.getVimType());
        assertEquals(null, actualAdditionalParameters.getDomain());
    }

    /**
     * test VNF activation with VDU mappings
     */
    @Test
    public void testVnfActivationWithVdu() throws Exception {
        VnfActivationRequest soRequest = new VnfActivationRequest();
        soRequest.setVimId(VIM_ID);
        JsonObject additionalParams = new JsonObject();
        soRequest.setAdditionalParams(additionalParams);
        org.onap.vnfmdriver.model.VnfInfo vnfInfo = new org.onap.vnfmdriver.model.VnfInfo();
        vnfInfo.setVnfdId(VNFD_ID);
        when(lifecycleManager.queryVnf(VNFM_ID, VNF_ID)).thenReturn(vnfInfo);
        ArgumentCaptor<List<ExtVirtualLinkInfo>> extLinks= ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<AdditionalParameters> additionalParameters = ArgumentCaptor.forClass(AdditionalParameters.class);
        VnfInstantiateResponse instantiationResponse = new VnfInstantiateResponse();
        instantiationResponse.setJobId(JOB_ID);
        when(lifecycleManager.instantiate(eq(VNFM_ID), extLinks.capture(), eq(httpResponse), eq(additionalParams), additionalParameters.capture(), eq(VNF_ID), eq("csarId"), eq(VNFD_ID))).thenReturn(instantiationResponse);
        EsrSystemInfo esrInfo = new EsrSystemInfo();
        esrInfo.setServiceUrl("http://localhost:123/v3");
        esrInfo.setCloudDomain("domain");
        when(aaiExternalSystemInfoProvider.getEsrSystemInfo(VIM_ID)).thenReturn(esrInfo);
        VnfInfo cbamVnfInfo = new VnfInfo();
        cbamVnfInfo.setExtensions(new ArrayList<>());
        VnfProperty onapVnfdId = new VnfProperty();
        cbamVnfInfo.getExtensions().add(onapVnfdId);
        onapVnfdId.setName(LifecycleManager.ONAP_CSAR_ID);
        onapVnfdId.setValue("csarId");
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(cbamVnfInfo));
        soRequest.setVduMappings(new ArrayList<>());
        VduMapping vduMapping = new VduMapping();
        soRequest.getVduMappings().add(vduMapping);
        vduMapping.setFlavourId("flavorId");
        vduMapping.setImageId("imageId");
        vduMapping.setVduId("vduId");
        //when
        JobHandler jobHandler = soLifecycleManager.activate(VNFM_ID, VNF_ID, soRequest, httpResponse);
        //verify
        assertEquals(JOB_ID, jobHandler.getJobId());
        AdditionalParameters actualAdditionalParameters = additionalParameters.getValue();
        assertEquals(1, actualAdditionalParameters.getComputeResourceFlavours().size());
        assertEquals(1, actualAdditionalParameters.getSoftwareImages().size());
        VimSoftwareImage image = actualAdditionalParameters.getSoftwareImages().get(0);
        assertEquals(VIM_ID, image.getVimId());
        assertEquals("vduId_image", image.getVnfdSoftwareImageId());
        assertEquals("imageId", image.getResourceId());
        assertEquals(VIM_ID, actualAdditionalParameters.getComputeResourceFlavours().get(0).getVimId());
        assertEquals("flavorId", actualAdditionalParameters.getComputeResourceFlavours().get(0).getResourceId());
        assertEquals("vduId", actualAdditionalParameters.getComputeResourceFlavours().get(0).getVnfdVirtualComputeDescId());
        assertEquals(OPENSTACK_V3_INFO, actualAdditionalParameters.getVimType());
    }

    /**
     * test VNF activation with network mappings
     */
    @Test
    public void testVnfActivationWithNetworkMapping() throws Exception {
        VnfActivationRequest soRequest = new VnfActivationRequest();
        soRequest.setVimId(VIM_ID);
        JsonObject additionalParams = new JsonObject();
        soRequest.setAdditionalParams(additionalParams);
        org.onap.vnfmdriver.model.VnfInfo vnfInfo = new org.onap.vnfmdriver.model.VnfInfo();
        vnfInfo.setVnfdId(VNFD_ID);
        when(lifecycleManager.queryVnf(VNFM_ID, VNF_ID)).thenReturn(vnfInfo);
        ArgumentCaptor<List<ExtVirtualLinkInfo>> extLinks= ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<AdditionalParameters> additionalParameters = ArgumentCaptor.forClass(AdditionalParameters.class);
        VnfInstantiateResponse instantiationResponse = new VnfInstantiateResponse();
        instantiationResponse.setJobId(JOB_ID);
        when(lifecycleManager.instantiate(eq(VNFM_ID), extLinks.capture(), eq(httpResponse), eq(additionalParams), additionalParameters.capture(), eq(VNF_ID), eq("csarId"), eq(VNFD_ID))).thenReturn(instantiationResponse);
        EsrSystemInfo esrInfo = new EsrSystemInfo();
        esrInfo.setServiceUrl("http://localhost:123/v3");
        esrInfo.setCloudDomain("domain");
        when(aaiExternalSystemInfoProvider.getEsrSystemInfo(VIM_ID)).thenReturn(esrInfo);
        VnfInfo cbamVnfInfo = new VnfInfo();
        cbamVnfInfo.setExtensions(new ArrayList<>());
        VnfProperty onapVnfdId = new VnfProperty();
        cbamVnfInfo.getExtensions().add(onapVnfdId);
        onapVnfdId.setName(LifecycleManager.ONAP_CSAR_ID);
        onapVnfdId.setValue("csarId");
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(cbamVnfInfo));
        soRequest.setNetworkMappings(new ArrayList<>());
        NetworkMapping networkMapping = new NetworkMapping();
        networkMapping.setVldId("myVldId");
        networkMapping.setNetworkProviderId("providerId");
        networkMapping.setAssignedAddresses(new ArrayList<>());
        AssignedAddresses e1 = new AssignedAddresses();
        e1.setIpAddress("1.2.3.4");
        e1.setCpdId("cpdId");
        AssignedAddresses e2 = new AssignedAddresses();
        e2.setIpAddress("1.2.3.5");
        e2.setCpdId("cpdId2");
        AssignedAddresses e3 = new AssignedAddresses();
        e3.setIpAddress("1.2.3.6");
        e3.setCpdId("cpdId2");
        AssignedAddresses e4 = new AssignedAddresses();
        e4.setIpAddress("1.2.3.6");
        e4.setCpdId("cpdId2");
        networkMapping.getAssignedAddresses().add(e1);
        networkMapping.getAssignedAddresses().add(e2);
        networkMapping.getAssignedAddresses().add(e3);
        networkMapping.getAssignedAddresses().add(e4);
        NetworkMapping networkMapping2 = new NetworkMapping();
        soRequest.getNetworkMappings().add(networkMapping);
        soRequest.getNetworkMappings().add(networkMapping2);
        networkMapping2.setVldId("myVldId2");
        networkMapping2.setNetworkProviderId("providerId2");

        //when
        JobHandler jobHandler = soLifecycleManager.activate(VNFM_ID, VNF_ID, soRequest, httpResponse);
        //verify
        assertEquals(JOB_ID, jobHandler.getJobId());
        AdditionalParameters actualAdditionalParameters = additionalParameters.getValue();
        assertEquals(2, actualAdditionalParameters.getExtVirtualLinks().size());
        ExtVirtualLinkData actualVl = actualAdditionalParameters.getExtVirtualLinks().get(0);
        assertEquals(VIM_ID, actualVl.getVimId());
        assertEquals("providerId", actualVl.getResourceId());
        assertEquals("myVldId", actualVl.getExtVirtualLinkId());
        assertEquals(2, actualVl.getExtCps().size());
        assertEquals("cpdId", actualVl.getExtCps().get(0).getCpdId());
        assertEquals("1.2.3.4", actualVl.getExtCps().get(0).getAddresses().get(0).getIp());
        assertEquals("cpdId2", actualVl.getExtCps().get(1).getCpdId());
        assertEquals("1.2.3.5", actualVl.getExtCps().get(1).getAddresses().get(0).getIp());
        assertEquals("1.2.3.6", actualVl.getExtCps().get(1).getAddresses().get(1).getIp());
    }

    /**
     * test VNF activation with server mappings
     */
    @Test
    public void testVnfActivationWithServerMappings() throws Exception {
        VnfActivationRequest soRequest = new VnfActivationRequest();
        soRequest.setVimId(VIM_ID);
        JsonObject additionalParams = new JsonObject();
        soRequest.setAdditionalParams(additionalParams);
        org.onap.vnfmdriver.model.VnfInfo vnfInfo = new org.onap.vnfmdriver.model.VnfInfo();
        vnfInfo.setVnfdId(VNFD_ID);
        when(lifecycleManager.queryVnf(VNFM_ID, VNF_ID)).thenReturn(vnfInfo);
        ArgumentCaptor<List<ExtVirtualLinkInfo>> extLinks= ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<AdditionalParameters> additionalParameters = ArgumentCaptor.forClass(AdditionalParameters.class);
        VnfInstantiateResponse instantiationResponse = new VnfInstantiateResponse();
        instantiationResponse.setJobId(JOB_ID);
        when(lifecycleManager.instantiate(eq(VNFM_ID), extLinks.capture(), eq(httpResponse), eq(additionalParams), additionalParameters.capture(), eq(VNF_ID), eq("csarId"), eq(VNFD_ID))).thenReturn(instantiationResponse);
        EsrSystemInfo esrInfo = new EsrSystemInfo();
        esrInfo.setServiceUrl("http://localhost:123/v3");
        esrInfo.setCloudDomain("domain");
        when(aaiExternalSystemInfoProvider.getEsrSystemInfo(VIM_ID)).thenReturn(esrInfo);
        VnfInfo cbamVnfInfo = new VnfInfo();
        cbamVnfInfo.setExtensions(new ArrayList<>());
        VnfProperty onapVnfdId = new VnfProperty();
        cbamVnfInfo.getExtensions().add(onapVnfdId);
        onapVnfdId.setName(LifecycleManager.ONAP_CSAR_ID);
        onapVnfdId.setValue("csarId");
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(cbamVnfInfo));
        soRequest.setServerMappings(new ArrayList<>());
        ServerMapping s1 = new ServerMapping();
        soRequest.getServerMappings().add(s1);
        s1.setVduId("vduId1");
        s1.setAvailabilityZoneId("azId1");
        ServerMapping s2 = new ServerMapping();
        soRequest.getServerMappings().add(s2);
        s2.setVduId("vduId1");
        s2.setAvailabilityZoneId("azId1");
        ServerMapping s3 = new ServerMapping();
        soRequest.getServerMappings().add(s3);
        s3.setVduId("vduId2");
        s3.setAvailabilityZoneId("azId1");

        //when
        JobHandler jobHandler = soLifecycleManager.activate(VNFM_ID, VNF_ID, soRequest, httpResponse);
        //verify
        assertEquals(JOB_ID, jobHandler.getJobId());
        AdditionalParameters actualAdditionalParameters = additionalParameters.getValue();
        assertEquals(2, actualAdditionalParameters.getZones().size());
        assertEquals(VIM_ID, actualAdditionalParameters.getZones().get(0).getVimId());
        assertEquals("azId1", actualAdditionalParameters.getZones().get(0).getResourceId());
        assertEquals("vduId1", actualAdditionalParameters.getZones().get(0).getId());
        assertEquals(VIM_ID, actualAdditionalParameters.getZones().get(1).getVimId());
        assertEquals("azId1", actualAdditionalParameters.getZones().get(1).getResourceId());
        assertEquals("vduId2", actualAdditionalParameters.getZones().get(1).getId());
    }

    /**
     * test VNF scale
     */
    @Test
    public void testScaling() throws Exception {
        VnfScaleRequest soRequest = new VnfScaleRequest();
        ArgumentCaptor<org.onap.vnfmdriver.model.VnfScaleRequest> driverRequest = ArgumentCaptor.forClass(org.onap.vnfmdriver.model.VnfScaleRequest.class);
        JobInfo jobInfo = new JobInfo();
        jobInfo.setJobId(JOB_ID);

        soRequest.setAspectId("aspectId");
        soRequest.setDirection(ScaleDirection.IN);
        soRequest.setSteps(2);
        JsonObject additionalParams = new JsonObject();
        soRequest.setAdditionalParams(additionalParams);
        when(lifecycleManager.scaleVnf(eq(VNFM_ID), eq(VNF_ID), driverRequest.capture(), eq(httpResponse))).thenReturn(jobInfo);
        //when
        JobHandler jobHandler = soLifecycleManager.scale(VNFM_ID, VNF_ID, soRequest, httpResponse);
        //verify
        assertEquals(JOB_ID, jobHandler.getJobId());
        assertEquals(2, Integer.parseInt(driverRequest.getValue().getNumberOfSteps()));
        assertEquals("aspectId", driverRequest.getValue().getAspectId());
        assertEquals(org.onap.vnfmdriver.model.ScaleDirection.IN, driverRequest.getValue().getType());
        assertEquals(additionalParams, driverRequest.getValue().getAdditionalParam());
    }

    /**
     * test VNF heal
     */
    @Test
    public void testHeal() throws Exception {
        VnfHealRequest soRequest = new VnfHealRequest();
        ArgumentCaptor<org.onap.vnfmdriver.model.VnfHealRequest> driverRequest = ArgumentCaptor.forClass(org.onap.vnfmdriver.model.VnfHealRequest.class);
        JobInfo jobInfo = new JobInfo();
        jobInfo.setJobId(JOB_ID);

        soRequest.setVnfcId(VNF_ID+"_vnfcId");
        JsonObject additionalParams = new JsonObject();
        soRequest.setAdditionalParams(additionalParams);
        when(lifecycleManager.healVnf(eq(VNFM_ID), eq(VNF_ID), driverRequest.capture(), eq(Optional.of("vnfcId")), eq(httpResponse))).thenReturn(jobInfo);
        //when
        JobHandler jobHandler = soLifecycleManager.heal(VNFM_ID, VNF_ID, soRequest, httpResponse);
        //verify
        assertEquals(JOB_ID, jobHandler.getJobId());
        assertEquals("notUsedByDriver", driverRequest.getValue().getAffectedvm().getVduid());
        assertEquals("notUsedByDriver", driverRequest.getValue().getAffectedvm().getVimid());
        assertEquals("unknown", driverRequest.getValue().getAffectedvm().getVmname());
        assertEquals("heal", driverRequest.getValue().getAction());
    }

    /**
     * test VNF deactivation
     */
    @Test
    public void testDeactivation() throws Exception {
        VnfTerminationRequest soRequest = new VnfTerminationRequest();
        ArgumentCaptor<org.onap.vnfmdriver.model.VnfTerminateRequest> driverRequest = ArgumentCaptor.forClass(org.onap.vnfmdriver.model.VnfTerminateRequest.class);
        JobInfo jobInfo = new JobInfo();
        jobInfo.setJobId(JOB_ID);
        soRequest.setGracefulTerminationTimeoutInMs(1234);
        soRequest.setMode(TerminationMode.GRACEFUL);
        JsonObject additionalParams = new JsonObject();
        soRequest.setAdditionalParams(additionalParams);
        when(lifecycleManager.terminateAndDelete(eq(VNFM_ID), eq(VNF_ID), driverRequest.capture(), eq(httpResponse))).thenReturn(jobInfo);
        //when
        JobHandler jobHandler = soLifecycleManager.deactivate(VNFM_ID, VNF_ID, soRequest, httpResponse);
        //verify
        assertEquals(JOB_ID, jobHandler.getJobId());
        assertEquals(VnfTerminationType.GRACEFUL, driverRequest.getValue().getTerminationType());
        assertEquals("1234", driverRequest.getValue().getGracefulTerminationTimeout());
    }

    /**
     * test VNF custom operation
     */
    @Test
    public void testCustomOperation() throws Exception {
        VnfCustomOperation soRequest = new VnfCustomOperation();
        JobInfo jobInfo = new JobInfo();
        jobInfo.setJobId(JOB_ID);
        soRequest.setOperationId("operationId");
        JsonObject additionalParams = new JsonObject();
        soRequest.setAdditionalParams(additionalParams);
        when(lifecycleManager.customOperation(VNFM_ID, VNF_ID, "operationId", additionalParams, httpResponse)).thenReturn(jobInfo);
        //when
        JobHandler jobHandler = soLifecycleManager.customOperation(VNFM_ID, VNF_ID, soRequest, httpResponse);
        //verify
        assertEquals(JOB_ID, jobHandler.getJobId());
    }


    /**
     * test VNF custom operation
     */
    @Test
    public void testJobDetails() throws Exception {
        JobDetailInfo currentJobDetails = new JobDetailInfo();
        currentJobDetails.setJobId(JOB_ID);
        when(jobManager.getJob(VNFM_ID, JOB_ID)).thenReturn(currentJobDetails);
        currentJobDetails.setResponseDescriptor(new JobDetailInfoResponseDescriptor());

        assertJob(currentJobDetails, org.onap.vnfmadapter.so.model.JobStatus.FINISHED, JobStatus.FINISHED);
        assertJob(currentJobDetails, org.onap.vnfmadapter.so.model.JobStatus.FAILED, JobStatus.ERROR);
        assertJob(currentJobDetails, org.onap.vnfmadapter.so.model.JobStatus.FAILED, JobStatus.TIMEOUT);
        assertJob(currentJobDetails, org.onap.vnfmadapter.so.model.JobStatus.STARTED, JobStatus.STARTED);
        assertJob(currentJobDetails, org.onap.vnfmadapter.so.model.JobStatus.STARTED, JobStatus.PROCESSING);

    }

    private void assertJob(JobDetailInfo currentJobDetails, org.onap.vnfmadapter.so.model.JobStatus expectedState, JobStatus started) {
        currentJobDetails.getResponseDescriptor().setStatus(started);
        //when
        JobDetail jobDetail = soLifecycleManager.getJobDetails(VNFM_ID, JOB_ID);
        //verify
        assertEquals(JOB_ID, jobDetail.getJobId());
        assertEquals(expectedState, jobDetail.getStatus());
    }


}