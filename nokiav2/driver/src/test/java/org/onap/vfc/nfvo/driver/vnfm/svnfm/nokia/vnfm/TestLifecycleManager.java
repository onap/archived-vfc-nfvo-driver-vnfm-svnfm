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
import com.google.gson.JsonParser;
import com.nokia.cbam.catalog.v1.model.CatalogAdapterVnfpackage;
import com.nokia.cbam.lcm.v32.model.*;
import com.nokia.cbam.lcm.v32.model.OperationType;
import com.nokia.cbam.lcm.v32.model.VimInfo;
import com.nokia.cbam.lcm.v32.model.VnfInfo;
import io.reactivex.Observable;
import java.nio.file.Paths;
import java.util.*;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api.VimInfoProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.vfc.TestVfcGrantManager;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.vfc.VfcGrantManager;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.StoreLoader;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification.LifecycleChangeNotificationManager;
import org.onap.vnfmdriver.model.ExtVirtualLinkInfo;
import org.onap.vnfmdriver.model.*;
import org.onap.vnfmdriver.model.ScaleDirection;
import org.threeten.bp.OffsetDateTime;

import static java.lang.Boolean.parseBoolean;
import static java.nio.file.Files.readAllBytes;
import static java.util.Optional.empty;

import static junit.framework.TestCase.*;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.*;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.child;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.SystemFunctions.systemFunctions;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProvider.NOKIA_LCM_API_VERSION;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class TestLifecycleManager extends TestBase {
    public static final String JOB_ID = "myJobId";
    public static final String CBAM_VNFD_ID = "cbamVnfdId";
    public static final String TENANT = "myTenant";
    public static final String OPERATION_EXECUTION_ID = "operationExecutionId";
    private static final String ONAP_CSAR_ID = "myOnapCsarId";
    private static final String VIM_ID = "ownerId_regionId";
    @Mock
    private CatalogManager catalogManager;
    @Mock
    private VfcGrantManager vfcGrantManager;
    @Mock
    private JobManager jobManager;
    @Mock
    private LifecycleChangeNotificationManager notificationManager;
    @Mock
    private HttpServletResponse restResponse;
    @Mock
    private VimInfoProvider vimInfoProvider;

    private ArgumentCaptor<CreateVnfRequest> createRequest = ArgumentCaptor.forClass(CreateVnfRequest.class);
    private AdditionalParameters additionalParam = new AdditionalParameters();
    private String INSTANTIATION_LEVEL = "level1";
    private GrantVNFResponseVim grantResponse = new GrantVNFResponseVim();
    private String cbamVnfdContent;
    private OperationExecution instantiationOperationExecution = new OperationExecution();
    private OperationExecution modifyPropertyoperationExecution = new OperationExecution();
    private OperationExecution scaleOperationExecution = new OperationExecution();
    private OperationExecution healOperationExecution = new OperationExecution();
    private OperationExecution customOperationExecution = new OperationExecution();


    private VnfInfo vnfInfo = new VnfInfo();
    private List<OperationExecution> operationExecutions = new ArrayList<>();
    private org.onap.vnfmdriver.model.VimInfo vimInfo = new org.onap.vnfmdriver.model.VimInfo();
    private ExtVirtualLinkInfo externalVirtualLink = new ExtVirtualLinkInfo();
    private ExtManagedVirtualLinkData extManVl = new ExtManagedVirtualLinkData();
    private ArgumentCaptor<ModifyVnfInfoRequest> actualVnfModifyRequest = ArgumentCaptor.forClass(ModifyVnfInfoRequest.class);
    private Set<Boolean> finished = new HashSet<>();
    private ArgumentCaptor<ScaleVnfRequest> actualScaleRequest = ArgumentCaptor.forClass(ScaleVnfRequest.class);
    private ArgumentCaptor<HealVnfRequest> actualHealRequest = ArgumentCaptor.forClass(HealVnfRequest.class);
    private ArgumentCaptor<CustomOperationRequest> customOperationRequestArgumentCaptor = ArgumentCaptor.forClass(CustomOperationRequest.class);
    private ArgumentCaptor<String> operationIdCaptor = ArgumentCaptor.forClass(String.class);

    private LifecycleManager lifecycleManager;

    @Before
    public void initMocks() throws Exception {
        vnfInfo.setExtensions(new ArrayList<>());
        vnfInfo.setOperationExecutions(new ArrayList<>());
        lifecycleManager = new LifecycleManager(catalogManager, vfcGrantManager, cbamRestApiProvider, vimInfoProvider, jobManager, notificationManager);
        cbamVnfdContent = new String(readAllBytes(Paths.get(TestVfcGrantManager.class.getResource("/unittests/vnfd.full.yaml").toURI())));
        setField(LifecycleManager.class, "logger", logger);
        CatalogAdapterVnfpackage cbamPackage = new CatalogAdapterVnfpackage();
        when(catalogManager.preparePackageInCbam(VNFM_ID, ONAP_CSAR_ID)).thenReturn(cbamPackage);
        cbamPackage.setVnfdId(CBAM_VNFD_ID);
        vnfInfo.setVnfdId(CBAM_VNFD_ID);
        vnfInfo.setId(VNF_ID);
        when(jobManager.spawnJob(VNF_ID, restResponse)).thenReturn(JOB_ID);
        when(catalogManager.getCbamVnfdContent(VNFM_ID, CBAM_VNFD_ID)).thenReturn(cbamVnfdContent);
        cbamPackage.setId(CBAM_VNFD_ID);
        vimInfo.setUrl("cloudUrl");
        vimInfo.setPassword("vimPassword");
        vimInfo.setUserName("vimUsername");
        vimInfo.setSslInsecure("true");
        vimInfo.setVimId(VIM_ID);
        vimInfo.setName("vimName");
        when(vimInfoProvider.getVimInfo((VIM_ID))).thenReturn(vimInfo);
        instantiationOperationExecution.setId(OPERATION_EXECUTION_ID);
        instantiationOperationExecution.setOperationType(OperationType.INSTANTIATE);
        instantiationOperationExecution.setStartTime(OffsetDateTime.now());
        when(vnfApi.vnfsVnfInstanceIdOperationExecutionsGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(operationExecutions));
        operationExecutions.add(modifyPropertyoperationExecution);
        modifyPropertyoperationExecution.setStartTime(OffsetDateTime.now());
        modifyPropertyoperationExecution.setOperationType(OperationType.MODIFY_INFO);
        operationExecutions.add(instantiationOperationExecution);
        instantiationOperationExecution.setStatus(OperationStatus.FINISHED);
        modifyPropertyoperationExecution.setStatus(OperationStatus.FINISHED);
        customOperationExecution.setStatus(OperationStatus.FINISHED);
        modifyPropertyoperationExecution.setId(UUID.randomUUID().toString());
        scaleOperationExecution.setId(UUID.randomUUID().toString());
        healOperationExecution.setId(UUID.randomUUID().toString());
        customOperationExecution.setId(UUID.randomUUID().toString());

        when(vnfApi.vnfsVnfInstanceIdPatch(eq(VNF_ID), actualVnfModifyRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenReturn(buildObservable(modifyPropertyoperationExecution));
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                finished.add(Boolean.TRUE);
                return null;
            }
        }).when(jobManager).jobFinished(JOB_ID);
        when(vnfApi.vnfsVnfInstanceIdScalePost(eq(VNF_ID), actualScaleRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenAnswer(new Answer<Observable<OperationExecution>>() {
            @Override
            public Observable<OperationExecution> answer(InvocationOnMock invocation) throws Throwable {
                operationExecutions.add(scaleOperationExecution);
                return buildObservable(scaleOperationExecution);
            }
        });
        when(vnfApi.vnfsVnfInstanceIdHealPost(eq(VNF_ID), actualHealRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenAnswer(new Answer<Observable<OperationExecution>>() {
            @Override
            public Observable<OperationExecution> answer(InvocationOnMock invocation) throws Throwable {
                operationExecutions.add(healOperationExecution);
                return buildObservable(healOperationExecution);
            }
        });
        when(vnfApi.vnfsVnfInstanceIdCustomCustomOperationNamePost(eq(VNF_ID), operationIdCaptor.capture(), customOperationRequestArgumentCaptor.capture(), eq(NOKIA_LCM_API_VERSION))).thenAnswer(new Answer<Observable<OperationExecution>>() {
            @Override
            public Observable<OperationExecution> answer(InvocationOnMock invocation) throws Throwable {
                operationExecutions.add(customOperationExecution);
                return buildObservable(customOperationExecution);
            }
        });
    }

    /**
     * test instantiation
     */
    @Test
    public void testInstantiation() throws Exception {
        //given
        VnfInstantiateRequest instantiationRequest = prepareInstantiationRequest(VimInfo.VimInfoTypeEnum.OPENSTACK_V2_INFO);

        when(vnfApi.vnfsPost(createRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenReturn(buildObservable(vnfInfo));
        additionalParam.setInstantiationLevel(INSTANTIATION_LEVEL);
        when(vfcGrantManager.requestGrantForInstantiate(VNFM_ID, VNF_ID, VIM_ID, ONAP_CSAR_ID, INSTANTIATION_LEVEL, cbamVnfdContent, JOB_ID)).thenReturn(grantResponse);
        grantResponse.setVimId(VIM_ID);
        GrantVNFResponseVimAccessInfo accessInfo = new GrantVNFResponseVimAccessInfo();
        accessInfo.setTenant(TENANT);
        grantResponse.setAccessInfo(accessInfo);
        ArgumentCaptor<InstantiateVnfRequest> actualInstantiationRequest = ArgumentCaptor.forClass(InstantiateVnfRequest.class);
        when(vnfApi.vnfsVnfInstanceIdInstantiatePost(eq(VNF_ID), actualInstantiationRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenReturn(buildObservable(instantiationOperationExecution));
        //when
        VnfInstantiateResponse response = lifecycleManager.createAndInstantiate(VNFM_ID, instantiationRequest, restResponse);
        waitForJobToFinishInJobManager(finished);
        //verify
        assertEquals(VNF_ID, response.getVnfInstanceId());
        assertEquals(JOB_ID, response.getJobId());
        assertEquals(createRequest.getAllValues().size(), 1);
        assertEquals("myDescription", createRequest.getValue().getDescription());
        assertEquals("vnfName", createRequest.getValue().getName());
        assertEquals(CBAM_VNFD_ID, createRequest.getValue().getVnfdId());
        assertEquals(1, actualInstantiationRequest.getAllValues().size());
        assertEquals(1, actualInstantiationRequest.getValue().getVims().size());
        OPENSTACKV2INFO actualVim = (OPENSTACKV2INFO) actualInstantiationRequest.getValue().getVims().get(0);
        assertEquals(VIM_ID, actualVim.getId());
        assertEquals(VimInfo.VimInfoTypeEnum.OPENSTACK_V2_INFO, actualVim.getVimInfoType());
        assertEquals(Boolean.valueOf(parseBoolean(vimInfo.getSslInsecure())), actualVim.getInterfaceInfo().isSkipCertificateVerification());
        assertEquals("cloudUrl", actualVim.getInterfaceInfo().getEndpoint());
        //FIXME assertEquals();actualVim.getInterfaceInfo().getTrustedCertificates());
        assertEquals("vimPassword", actualVim.getAccessInfo().getPassword());
        assertEquals("regionId", actualVim.getAccessInfo().getRegion());
        assertEquals("myTenant", actualVim.getAccessInfo().getTenant());
        assertEquals("vimUsername", actualVim.getAccessInfo().getUsername());
        assertEquals(1, actualInstantiationRequest.getValue().getComputeResourceFlavours().size());
        assertEquals("flavourProviderId", actualInstantiationRequest.getValue().getComputeResourceFlavours().get(0).getResourceId());
        assertEquals(VIM_ID, actualInstantiationRequest.getValue().getComputeResourceFlavours().get(0).getVimId());
        assertEquals("virtualComputeDescId", actualInstantiationRequest.getValue().getComputeResourceFlavours().get(0).getVnfdVirtualComputeDescId());
        assertEquals(1, actualInstantiationRequest.getValue().getExtManagedVirtualLinks().size());
        assertEquals(extManVl, actualInstantiationRequest.getValue().getExtManagedVirtualLinks().get(0));
        assertEquals(2, actualInstantiationRequest.getValue().getExtVirtualLinks().size());

        assertEquals("myNetworkProviderId", actualInstantiationRequest.getValue().getExtVirtualLinks().get(0).getResourceId());
        assertEquals("myEVlId", actualInstantiationRequest.getValue().getExtVirtualLinks().get(0).getExtVirtualLinkId());
        assertEquals(1, actualInstantiationRequest.getValue().getExtVirtualLinks().get(0).getExtCps().size());
        assertEquals("myCpdId", actualInstantiationRequest.getValue().getExtVirtualLinks().get(0).getExtCps().get(0).getCpdId());

        assertEquals(VIM_ID, actualInstantiationRequest.getValue().getExtVirtualLinks().get(1).getVimId());
        assertEquals("evlId1", actualInstantiationRequest.getValue().getExtVirtualLinks().get(1).getExtVirtualLinkId());
        assertEquals("networkProviderId1", actualInstantiationRequest.getValue().getExtVirtualLinks().get(1).getResourceId());
        assertEquals(1, actualInstantiationRequest.getValue().getExtVirtualLinks().get(1).getExtCps().size());


        assertEquals(Integer.valueOf(2), actualInstantiationRequest.getValue().getExtVirtualLinks().get(1).getExtCps().get(0).getNumDynamicAddresses());
        assertEquals("cpdId3", actualInstantiationRequest.getValue().getExtVirtualLinks().get(1).getExtCps().get(0).getCpdId());
        assertEquals(1, actualInstantiationRequest.getValue().getExtVirtualLinks().get(1).getExtCps().get(0).getAddresses().size());
        assertEquals("1.2.3.4", actualInstantiationRequest.getValue().getExtVirtualLinks().get(1).getExtCps().get(0).getAddresses().get(0).getIp());
        assertEquals("mac", actualInstantiationRequest.getValue().getExtVirtualLinks().get(1).getExtCps().get(0).getAddresses().get(0).getMac());
        assertEquals("subnetId", actualInstantiationRequest.getValue().getExtVirtualLinks().get(1).getExtCps().get(0).getAddresses().get(0).getSubnetId());

        assertEquals("myFlavorId", actualInstantiationRequest.getValue().getFlavourId());
        assertEquals(Boolean.TRUE, actualInstantiationRequest.getValue().isGrantlessMode());
        assertEquals("level1", actualInstantiationRequest.getValue().getInstantiationLevelId());
        assertEquals(1, actualInstantiationRequest.getValue().getZones().size());
        assertEquals(VIM_ID, actualInstantiationRequest.getValue().getZones().get(0).getVimId());
        assertEquals("zoneProviderId", actualInstantiationRequest.getValue().getZones().get(0).getResourceId());
        assertEquals("zoneId", actualInstantiationRequest.getValue().getZones().get(0).getId());
        assertEquals(1, actualInstantiationRequest.getValue().getSoftwareImages().size());
        assertEquals(VIM_ID, actualInstantiationRequest.getValue().getSoftwareImages().get(0).getVimId());
        assertEquals("imageProviderId", actualInstantiationRequest.getValue().getSoftwareImages().get(0).getResourceId());
        assertEquals("imageId", actualInstantiationRequest.getValue().getSoftwareImages().get(0).getVnfdSoftwareImageId());
        String actualEmbeddedAdditionParams = new Gson().toJson(actualInstantiationRequest.getValue().getAdditionalParams());
        assertTrue("{\"jobId\":\"myJobId\",\"a\":\"b\"}".equals(actualEmbeddedAdditionParams) || "{\"a\":\"b\",\"jobId\":\"myJobId\"}".equals(actualEmbeddedAdditionParams));
        assertTrue(actualVim.getInterfaceInfo().isSkipCertificateVerification());
        assertTrue(actualVim.getInterfaceInfo().isSkipCertificateHostnameCheck());

        assertEquals(1, actualVnfModifyRequest.getAllValues().size());
        assertEquals(2, actualVnfModifyRequest.getValue().getExtensions().size());
        assertEquals(LifecycleManager.ONAP_CSAR_ID, actualVnfModifyRequest.getValue().getExtensions().get(0).getName());
        assertEquals(ONAP_CSAR_ID, actualVnfModifyRequest.getValue().getExtensions().get(0).getValue());
        assertEquals(LifecycleManager.EXTERNAL_VNFM_ID, actualVnfModifyRequest.getValue().getExtensions().get(1).getName());
        assertEquals(VNFM_ID, actualVnfModifyRequest.getValue().getExtensions().get(1).getValue());

        //the 3.2 API does not accept empty array
        assertNull(actualVnfModifyRequest.getValue().getVnfConfigurableProperties());
        verify(jobManager).spawnJob(VNF_ID, restResponse);
        verify(logger).info(eq("Starting {} operation on VNF with {} identifier with {} parameter"), eq("creation"), eq("not yet specified"), anyString());
        verify(logger).info(eq("Starting {} operation on VNF with {} identifier with {} parameter"), eq("instantiation"), eq(VNF_ID), anyString());
    }

    /**
     * invalid VIM type results in failure
     */
    @Test
    public void testInstantiationWithInvalidVimType() throws Exception {
        //given
        VnfInstantiateRequest instantiationRequest = prepareInstantiationRequest(VimInfo.VimInfoTypeEnum.OTHER_VIM_INFO);
        when(vnfApi.vnfsPost(createRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenReturn(buildObservable(vnfInfo));
        when(logger.isInfoEnabled()).thenReturn(false);
        //when
        try {
            lifecycleManager.createAndInstantiate(VNFM_ID, instantiationRequest, restResponse);
            //verify
            fail();
        } catch (Exception e) {
            assertEquals("Only OPENSTACK_V2_INFO, OPENSTACK_V3_INFO and VMWARE_VCLOUD_INFO is the supported VIM types", e.getMessage());
        }
        verify(vnfApi, never()).vnfsPost(Mockito.any(), Mockito.any());
        verify(logger, never()).info(eq("Starting {} operation on VNF with {} identifier with {} parameter"), eq("creation"), eq("not yet specified"), anyString());
        verify(logger, never()).info(eq("Starting {} operation on VNF with {} identifier with {} parameter"), eq("instantiation"), eq(VNF_ID), anyString());
        verify(logger).error("Only OPENSTACK_V2_INFO, OPENSTACK_V3_INFO and VMWARE_VCLOUD_INFO is the supported VIM types");
    }

    /**
     * test instantiation with KeyStone V2 based with SSL
     */
    @Test
    public void testInstantiationV2WithSsl() throws Exception {
        VnfInstantiateRequest instantiationRequest = prepareInstantiationRequest(VimInfo.VimInfoTypeEnum.OPENSTACK_V2_INFO);
        when(logger.isInfoEnabled()).thenReturn(false);
        when(vnfApi.vnfsPost(createRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenReturn(buildObservable(vnfInfo));
        additionalParam.setInstantiationLevel(INSTANTIATION_LEVEL);
        when(vfcGrantManager.requestGrantForInstantiate(VNFM_ID, VNF_ID, VIM_ID, ONAP_CSAR_ID, INSTANTIATION_LEVEL, cbamVnfdContent, JOB_ID)).thenReturn(grantResponse);
        grantResponse.setVimId(VIM_ID);
        GrantVNFResponseVimAccessInfo accessInfo = new GrantVNFResponseVimAccessInfo();
        accessInfo.setTenant(TENANT);
        String caCert = new String(readAllBytes(Paths.get(TestVfcGrantManager.class.getResource("/unittests/localhost.cert.pem").toURI())));
        vimInfo.setSslInsecure("false");
        vimInfo.setSslCacert(caCert);
        grantResponse.setAccessInfo(accessInfo);
        ArgumentCaptor<InstantiateVnfRequest> actualInstantiationRequest = ArgumentCaptor.forClass(InstantiateVnfRequest.class);
        when(vnfApi.vnfsVnfInstanceIdInstantiatePost(eq(VNF_ID), actualInstantiationRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenReturn(buildObservable(instantiationOperationExecution));
        JsonObject inputs = child((JsonObject) instantiationRequest.getAdditionalParam(), "inputs");
        JsonObject vnfs = child(child(inputs, "vnfs"), ONAP_CSAR_ID);
        vnfs.remove("additionalParams");
        //when
        VnfInstantiateResponse response = lifecycleManager.createAndInstantiate(VNFM_ID, instantiationRequest, restResponse);
        waitForJobToFinishInJobManager(finished);
        assertEquals(1, actualInstantiationRequest.getValue().getVims().size());
        //verify
        OPENSTACKV2INFO actualVim = (OPENSTACKV2INFO) actualInstantiationRequest.getValue().getVims().get(0);
        assertEquals(StoreLoader.getCertifacates(caCert).iterator().next(), new String(actualVim.getInterfaceInfo().getTrustedCertificates().get(0)));
        assertTrue(!actualVim.getInterfaceInfo().isSkipCertificateVerification());
        assertTrue(!actualVim.getInterfaceInfo().isSkipCertificateHostnameCheck());
        verify(logger).warn("No additional parameters were specified for the operation");
        verify(logger, never()).info(eq("Starting {} operation on VNF with {} identifier with {} parameter"), anyString(), anyString(), anyString());
    }

    /**
     * non specified SSL verification means not verified
     */
    @Test
    public void testInstantiationV2WithoutSsl() throws Exception {
        VnfInstantiateRequest instantiationRequest = prepareInstantiationRequest(VimInfo.VimInfoTypeEnum.OPENSTACK_V2_INFO);

        when(vnfApi.vnfsPost(createRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenReturn(buildObservable(vnfInfo));
        additionalParam.setInstantiationLevel(INSTANTIATION_LEVEL);
        when(vfcGrantManager.requestGrantForInstantiate(VNFM_ID, VNF_ID, VIM_ID, ONAP_CSAR_ID, INSTANTIATION_LEVEL, cbamVnfdContent, JOB_ID)).thenReturn(grantResponse);
        grantResponse.setVimId(VIM_ID);
        GrantVNFResponseVimAccessInfo accessInfo = new GrantVNFResponseVimAccessInfo();
        accessInfo.setTenant(TENANT);
        vimInfo.setSslInsecure(null);
        grantResponse.setAccessInfo(accessInfo);
        ArgumentCaptor<InstantiateVnfRequest> actualInstantiationRequest = ArgumentCaptor.forClass(InstantiateVnfRequest.class);
        when(vnfApi.vnfsVnfInstanceIdInstantiatePost(eq(VNF_ID), actualInstantiationRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenReturn(buildObservable(instantiationOperationExecution));
        //when
        lifecycleManager.createAndInstantiate(VNFM_ID, instantiationRequest, restResponse);
        waitForJobToFinishInJobManager(finished);
        assertEquals(1, actualInstantiationRequest.getValue().getVims().size());
        //verify
        OPENSTACKV2INFO actualVim = (OPENSTACKV2INFO) actualInstantiationRequest.getValue().getVims().get(0);
        assertTrue(actualVim.getInterfaceInfo().isSkipCertificateVerification());
        assertTrue(actualVim.getInterfaceInfo().isSkipCertificateHostnameCheck());
    }

    /**
     * test instantiation with KeyStone V3 based
     */
    @Test
    public void testInstantiationV3() throws Exception {
        VnfInstantiateRequest instantiationRequest = prepareInstantiationRequest(VimInfo.VimInfoTypeEnum.OPENSTACK_V3_INFO);
        when(vnfApi.vnfsPost(createRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenReturn(buildObservable(vnfInfo));
        additionalParam.setInstantiationLevel(INSTANTIATION_LEVEL);
        when(vfcGrantManager.requestGrantForInstantiate(VNFM_ID, VNF_ID, VIM_ID, ONAP_CSAR_ID, INSTANTIATION_LEVEL, cbamVnfdContent, JOB_ID)).thenReturn(grantResponse);
        grantResponse.setVimId(VIM_ID);
        GrantVNFResponseVimAccessInfo accessInfo = new GrantVNFResponseVimAccessInfo();
        accessInfo.setTenant(TENANT);
        grantResponse.setAccessInfo(accessInfo);
        ArgumentCaptor<InstantiateVnfRequest> actualInstantiationRequest = ArgumentCaptor.forClass(InstantiateVnfRequest.class);
        when(vnfApi.vnfsVnfInstanceIdInstantiatePost(eq(VNF_ID), actualInstantiationRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenReturn(buildObservable(instantiationOperationExecution));
        //when
        VnfInstantiateResponse response = lifecycleManager.createAndInstantiate(VNFM_ID, instantiationRequest, restResponse);
        waitForJobToFinishInJobManager(finished);
        assertEquals(1, actualInstantiationRequest.getValue().getVims().size());
        //verify
        OPENSTACKV3INFO actualVim = (OPENSTACKV3INFO) actualInstantiationRequest.getValue().getVims().get(0);
        assertEquals(VIM_ID, actualVim.getId());
        assertEquals(VimInfo.VimInfoTypeEnum.OPENSTACK_V3_INFO, actualVim.getVimInfoType());
        assertEquals(Boolean.valueOf(parseBoolean(vimInfo.getSslInsecure())), actualVim.getInterfaceInfo().isSkipCertificateVerification());
        assertEquals("cloudUrl", actualVim.getInterfaceInfo().getEndpoint());
        //FIXME assertEquals();actualVim.getInterfaceInfo().getTrustedCertificates());
        assertEquals("vimPassword", actualVim.getAccessInfo().getPassword());
        assertEquals("regionId", actualVim.getAccessInfo().getRegion());
        assertEquals("myTenant", actualVim.getAccessInfo().getProject());
        assertEquals("myDomain", actualVim.getAccessInfo().getDomain());
        assertEquals("vimUsername", actualVim.getAccessInfo().getUsername());
        assertTrue(actualVim.getInterfaceInfo().isSkipCertificateVerification());
        assertTrue(actualVim.getInterfaceInfo().isSkipCertificateHostnameCheck());
    }

    /**
     * test instantiation with backward compatibility test with Amsterdam release
     * - the vim identifier is supplied as vimid with not camel case
     */
    @Test
    public void testInstantiationNoVimId() throws Exception {
        //given
        VnfInstantiateRequest instantiationRequest = prepareInstantiationRequest(VimInfo.VimInfoTypeEnum.OPENSTACK_V2_INFO);
        when(vnfApi.vnfsPost(createRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenReturn(buildObservable(vnfInfo));
        additionalParam.setInstantiationLevel(INSTANTIATION_LEVEL);
        when(vfcGrantManager.requestGrantForInstantiate(VNFM_ID, VNF_ID, VIM_ID, ONAP_CSAR_ID, INSTANTIATION_LEVEL, cbamVnfdContent, JOB_ID)).thenReturn(grantResponse);
        grantResponse.setVimid(VIM_ID);
        GrantVNFResponseVimAccessInfo accessInfo = new GrantVNFResponseVimAccessInfo();
        accessInfo.setTenant(TENANT);
        grantResponse.setAccessinfo(accessInfo);
        ArgumentCaptor<InstantiateVnfRequest> actualInstantiationRequest = ArgumentCaptor.forClass(InstantiateVnfRequest.class);
        when(vnfApi.vnfsVnfInstanceIdInstantiatePost(eq(VNF_ID), actualInstantiationRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenReturn(buildObservable(instantiationOperationExecution));
        //when
        VnfInstantiateResponse response = lifecycleManager.createAndInstantiate(VNFM_ID, instantiationRequest, restResponse);
        waitForJobToFinishInJobManager(finished);
        //verify
        assertEquals(VNF_ID, response.getVnfInstanceId());
        assertEquals(JOB_ID, response.getJobId());
        assertEquals(createRequest.getAllValues().size(), 1);
        assertEquals("myDescription", createRequest.getValue().getDescription());
        assertEquals("vnfName", createRequest.getValue().getName());
        assertEquals(CBAM_VNFD_ID, createRequest.getValue().getVnfdId());
        assertEquals(1, actualInstantiationRequest.getAllValues().size());
        assertEquals(1, actualInstantiationRequest.getValue().getVims().size());
        OPENSTACKV2INFO actualVim = (OPENSTACKV2INFO) actualInstantiationRequest.getValue().getVims().get(0);
        assertEquals(VIM_ID, actualVim.getId());
        assertEquals(VimInfo.VimInfoTypeEnum.OPENSTACK_V2_INFO, actualVim.getVimInfoType());
        assertEquals(Boolean.valueOf(parseBoolean(vimInfo.getSslInsecure())), actualVim.getInterfaceInfo().isSkipCertificateVerification());
        assertEquals("cloudUrl", actualVim.getInterfaceInfo().getEndpoint());
        //FIXME assertEquals();actualVim.getInterfaceInfo().getTrustedCertificates());
        assertEquals("vimPassword", actualVim.getAccessInfo().getPassword());
        assertEquals("regionId", actualVim.getAccessInfo().getRegion());
        assertEquals("myTenant", actualVim.getAccessInfo().getTenant());
        assertEquals("vimUsername", actualVim.getAccessInfo().getUsername());
        assertEquals(1, actualInstantiationRequest.getValue().getComputeResourceFlavours().size());
        assertEquals("flavourProviderId", actualInstantiationRequest.getValue().getComputeResourceFlavours().get(0).getResourceId());
        assertEquals(VIM_ID, actualInstantiationRequest.getValue().getComputeResourceFlavours().get(0).getVimId());
        assertEquals("virtualComputeDescId", actualInstantiationRequest.getValue().getComputeResourceFlavours().get(0).getVnfdVirtualComputeDescId());
        assertEquals(1, actualInstantiationRequest.getValue().getExtManagedVirtualLinks().size());
        assertEquals(extManVl, actualInstantiationRequest.getValue().getExtManagedVirtualLinks().get(0));
        assertEquals(2, actualInstantiationRequest.getValue().getExtVirtualLinks().size());

        assertEquals("myNetworkProviderId", actualInstantiationRequest.getValue().getExtVirtualLinks().get(0).getResourceId());
        assertEquals("myEVlId", actualInstantiationRequest.getValue().getExtVirtualLinks().get(0).getExtVirtualLinkId());
        assertEquals(1, actualInstantiationRequest.getValue().getExtVirtualLinks().get(0).getExtCps().size());
        assertEquals("myCpdId", actualInstantiationRequest.getValue().getExtVirtualLinks().get(0).getExtCps().get(0).getCpdId());

        assertEquals(VIM_ID, actualInstantiationRequest.getValue().getExtVirtualLinks().get(1).getVimId());
        assertEquals("evlId1", actualInstantiationRequest.getValue().getExtVirtualLinks().get(1).getExtVirtualLinkId());
        assertEquals("networkProviderId1", actualInstantiationRequest.getValue().getExtVirtualLinks().get(1).getResourceId());
        assertEquals(1, actualInstantiationRequest.getValue().getExtVirtualLinks().get(1).getExtCps().size());


        assertEquals(Integer.valueOf(2), actualInstantiationRequest.getValue().getExtVirtualLinks().get(1).getExtCps().get(0).getNumDynamicAddresses());
        assertEquals("cpdId3", actualInstantiationRequest.getValue().getExtVirtualLinks().get(1).getExtCps().get(0).getCpdId());
        assertEquals(1, actualInstantiationRequest.getValue().getExtVirtualLinks().get(1).getExtCps().get(0).getAddresses().size());
        assertEquals("1.2.3.4", actualInstantiationRequest.getValue().getExtVirtualLinks().get(1).getExtCps().get(0).getAddresses().get(0).getIp());
        assertEquals("mac", actualInstantiationRequest.getValue().getExtVirtualLinks().get(1).getExtCps().get(0).getAddresses().get(0).getMac());
        assertEquals("subnetId", actualInstantiationRequest.getValue().getExtVirtualLinks().get(1).getExtCps().get(0).getAddresses().get(0).getSubnetId());

        assertEquals("myFlavorId", actualInstantiationRequest.getValue().getFlavourId());
        assertEquals(Boolean.TRUE, actualInstantiationRequest.getValue().isGrantlessMode());
        assertEquals("level1", actualInstantiationRequest.getValue().getInstantiationLevelId());
        assertEquals(1, actualInstantiationRequest.getValue().getZones().size());
        assertEquals(VIM_ID, actualInstantiationRequest.getValue().getZones().get(0).getVimId());
        assertEquals("zoneProviderId", actualInstantiationRequest.getValue().getZones().get(0).getResourceId());
        assertEquals("zoneId", actualInstantiationRequest.getValue().getZones().get(0).getId());
        assertEquals(1, actualInstantiationRequest.getValue().getSoftwareImages().size());
        assertEquals(VIM_ID, actualInstantiationRequest.getValue().getSoftwareImages().get(0).getVimId());
        assertEquals("imageProviderId", actualInstantiationRequest.getValue().getSoftwareImages().get(0).getResourceId());
        assertEquals("imageId", actualInstantiationRequest.getValue().getSoftwareImages().get(0).getVnfdSoftwareImageId());
        String actualEmbeddedAdditionParams = new Gson().toJson(actualInstantiationRequest.getValue().getAdditionalParams());
        assertTrue("{\"jobId\":\"myJobId\",\"a\":\"b\"}".equals(actualEmbeddedAdditionParams) || "{\"a\":\"b\",\"jobId\":\"myJobId\"}".equals(actualEmbeddedAdditionParams));
        assertTrue(actualVim.getInterfaceInfo().isSkipCertificateVerification());
        assertTrue(actualVim.getInterfaceInfo().isSkipCertificateHostnameCheck());

        assertEquals(1, actualVnfModifyRequest.getAllValues().size());
        assertEquals(2, actualVnfModifyRequest.getValue().getExtensions().size());
        assertEquals(LifecycleManager.ONAP_CSAR_ID, actualVnfModifyRequest.getValue().getExtensions().get(0).getName());
        assertEquals(ONAP_CSAR_ID, actualVnfModifyRequest.getValue().getExtensions().get(0).getValue());
        assertEquals(LifecycleManager.EXTERNAL_VNFM_ID, actualVnfModifyRequest.getValue().getExtensions().get(1).getName());
        assertEquals(VNFM_ID, actualVnfModifyRequest.getValue().getExtensions().get(1).getValue());

        //the 3.2 API does not accept empty array
        assertNull(actualVnfModifyRequest.getValue().getVnfConfigurableProperties());
        verify(jobManager).spawnJob(VNF_ID, restResponse);
        verify(logger).info(eq("Starting {} operation on VNF with {} identifier with {} parameter"), eq("creation"), eq("not yet specified"), anyString());
        verify(logger).info(eq("Starting {} operation on VNF with {} identifier with {} parameter"), eq("instantiation"), eq(VNF_ID), anyString());
    }

    /**
     * test instantiation with KeyStone V3 based with SSL
     */
    @Test
    public void testInstantiationV3WithSsl() throws Exception {
        VnfInstantiateRequest instantiationRequest = prepareInstantiationRequest(VimInfo.VimInfoTypeEnum.OPENSTACK_V3_INFO);
        when(vnfApi.vnfsPost(createRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenReturn(buildObservable(vnfInfo));
        additionalParam.setInstantiationLevel(INSTANTIATION_LEVEL);
        when(vfcGrantManager.requestGrantForInstantiate(VNFM_ID, VNF_ID, VIM_ID, ONAP_CSAR_ID, INSTANTIATION_LEVEL, cbamVnfdContent, JOB_ID)).thenReturn(grantResponse);
        grantResponse.setVimId(VIM_ID);
        GrantVNFResponseVimAccessInfo accessInfo = new GrantVNFResponseVimAccessInfo();
        accessInfo.setTenant(TENANT);
        String caCert = new String(readAllBytes(Paths.get(TestVfcGrantManager.class.getResource("/unittests/localhost.cert.pem").toURI())));
        vimInfo.setSslInsecure("false");
        vimInfo.setSslCacert(caCert);
        grantResponse.setAccessInfo(accessInfo);
        ArgumentCaptor<InstantiateVnfRequest> actualInstantiationRequest = ArgumentCaptor.forClass(InstantiateVnfRequest.class);
        when(vnfApi.vnfsVnfInstanceIdInstantiatePost(eq(VNF_ID), actualInstantiationRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenReturn(buildObservable(instantiationOperationExecution));
        //when
        VnfInstantiateResponse response = lifecycleManager.createAndInstantiate(VNFM_ID, instantiationRequest, restResponse);
        waitForJobToFinishInJobManager(finished);
        assertEquals(1, actualInstantiationRequest.getValue().getVims().size());
        //verify
        OPENSTACKV3INFO actualVim = (OPENSTACKV3INFO) actualInstantiationRequest.getValue().getVims().get(0);
        assertEquals(VIM_ID, actualVim.getId());
        assertEquals(VimInfo.VimInfoTypeEnum.OPENSTACK_V3_INFO, actualVim.getVimInfoType());
        assertEquals(Boolean.valueOf(parseBoolean(vimInfo.getSslInsecure())), actualVim.getInterfaceInfo().isSkipCertificateVerification());
        assertEquals("cloudUrl", actualVim.getInterfaceInfo().getEndpoint());
        //FIXME assertEquals();actualVim.getInterfaceInfo().getTrustedCertificates());
        assertEquals("vimPassword", actualVim.getAccessInfo().getPassword());
        assertEquals("regionId", actualVim.getAccessInfo().getRegion());
        assertEquals("myTenant", actualVim.getAccessInfo().getProject());
        assertEquals("myDomain", actualVim.getAccessInfo().getDomain());
        assertEquals("vimUsername", actualVim.getAccessInfo().getUsername());
        assertEquals(StoreLoader.getCertifacates(caCert).iterator().next(), new String(actualVim.getInterfaceInfo().getTrustedCertificates().get(0)));
        assertTrue(!actualVim.getInterfaceInfo().isSkipCertificateVerification());
        assertTrue(!actualVim.getInterfaceInfo().isSkipCertificateHostnameCheck());
    }

    /**
     * non specified SSL verification meams not verified for KeyStone V3 based
     */
    @Test
    public void testInstantiationV3WithNonSpecifiedSsl() throws Exception {
        VnfInstantiateRequest instantiationRequest = prepareInstantiationRequest(VimInfo.VimInfoTypeEnum.OPENSTACK_V3_INFO);
        when(vnfApi.vnfsPost(createRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenReturn(buildObservable(vnfInfo));
        additionalParam.setInstantiationLevel(INSTANTIATION_LEVEL);
        when(vfcGrantManager.requestGrantForInstantiate(VNFM_ID, VNF_ID, VIM_ID, ONAP_CSAR_ID, INSTANTIATION_LEVEL, cbamVnfdContent, JOB_ID)).thenReturn(grantResponse);
        grantResponse.setVimId(VIM_ID);
        GrantVNFResponseVimAccessInfo accessInfo = new GrantVNFResponseVimAccessInfo();
        accessInfo.setTenant(TENANT);
        vimInfo.setSslInsecure(null);
        grantResponse.setAccessInfo(accessInfo);
        ArgumentCaptor<InstantiateVnfRequest> actualInstantiationRequest = ArgumentCaptor.forClass(InstantiateVnfRequest.class);
        when(vnfApi.vnfsVnfInstanceIdInstantiatePost(eq(VNF_ID), actualInstantiationRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenReturn(buildObservable(instantiationOperationExecution));
        //when
        VnfInstantiateResponse response = lifecycleManager.createAndInstantiate(VNFM_ID, instantiationRequest, restResponse);
        waitForJobToFinishInJobManager(finished);
        assertEquals(1, actualInstantiationRequest.getValue().getVims().size());
        //verify
        OPENSTACKV3INFO actualVim = (OPENSTACKV3INFO) actualInstantiationRequest.getValue().getVims().get(0);
        assertEquals(VIM_ID, actualVim.getId());
        assertEquals(VimInfo.VimInfoTypeEnum.OPENSTACK_V3_INFO, actualVim.getVimInfoType());
        assertEquals("cloudUrl", actualVim.getInterfaceInfo().getEndpoint());
        //FIXME assertEquals();actualVim.getInterfaceInfo().getTrustedCertificates());
        assertEquals("vimPassword", actualVim.getAccessInfo().getPassword());
        assertEquals("regionId", actualVim.getAccessInfo().getRegion());
        assertEquals("myTenant", actualVim.getAccessInfo().getProject());
        assertEquals("myDomain", actualVim.getAccessInfo().getDomain());
        assertEquals("vimUsername", actualVim.getAccessInfo().getUsername());
        assertTrue(actualVim.getInterfaceInfo().isSkipCertificateVerification());
        assertTrue(actualVim.getInterfaceInfo().isSkipCertificateHostnameCheck());
    }

    /**
     * verify backward compatibility with Amsterdam release
     */
    @Test
    public void testInstantiationV3WithNoDomain() throws Exception {
        additionalParam.setInstantiationLevel(INSTANTIATION_LEVEL);
        additionalParam.setDomain("myDomain");
        VnfInstantiateRequest instantiationRequest = prepareInstantiationRequest(VimInfo.VimInfoTypeEnum.OPENSTACK_V3_INFO);
        vimInfo.setDomain(null);
        when(vnfApi.vnfsPost(createRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenReturn(buildObservable(vnfInfo));
        when(vfcGrantManager.requestGrantForInstantiate(VNFM_ID, VNF_ID, VIM_ID, ONAP_CSAR_ID, INSTANTIATION_LEVEL, cbamVnfdContent, JOB_ID)).thenReturn(grantResponse);
        grantResponse.setVimId(VIM_ID);
        GrantVNFResponseVimAccessInfo accessInfo = new GrantVNFResponseVimAccessInfo();
        accessInfo.setTenant(TENANT);
        vimInfo.setSslInsecure(null);
        grantResponse.setAccessInfo(accessInfo);
        ArgumentCaptor<InstantiateVnfRequest> actualInstantiationRequest = ArgumentCaptor.forClass(InstantiateVnfRequest.class);
        when(vnfApi.vnfsVnfInstanceIdInstantiatePost(eq(VNF_ID), actualInstantiationRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenReturn(buildObservable(instantiationOperationExecution));
        //when
        VnfInstantiateResponse response = lifecycleManager.createAndInstantiate(VNFM_ID, instantiationRequest, restResponse);
        waitForJobToFinishInJobManager(finished);
        assertEquals(1, actualInstantiationRequest.getValue().getVims().size());
        //verify
        OPENSTACKV3INFO actualVim = (OPENSTACKV3INFO) actualInstantiationRequest.getValue().getVims().get(0);
        assertEquals(VIM_ID, actualVim.getId());
        assertEquals(VimInfo.VimInfoTypeEnum.OPENSTACK_V3_INFO, actualVim.getVimInfoType());
        assertEquals("cloudUrl", actualVim.getInterfaceInfo().getEndpoint());
        //FIXME assertEquals();actualVim.getInterfaceInfo().getTrustedCertificates());
        assertEquals("vimPassword", actualVim.getAccessInfo().getPassword());
        assertEquals("regionId", actualVim.getAccessInfo().getRegion());
        assertEquals("myTenant", actualVim.getAccessInfo().getProject());
        assertEquals("myDomain", actualVim.getAccessInfo().getDomain());
        assertEquals("vimUsername", actualVim.getAccessInfo().getUsername());
        assertTrue(actualVim.getInterfaceInfo().isSkipCertificateVerification());
        assertTrue(actualVim.getInterfaceInfo().isSkipCertificateHostnameCheck());
        verify(logger).warn("Setting domain from additional parameters");
    }

    /**
     * verify backward compatibility with Amsterdam release
     * if no domain is specified error is propagated
     */
    @Test
    public void testInstantiationV3WithNoDomainFail() throws Exception {
        VnfInstantiateRequest instantiationRequest = prepareInstantiationRequest(VimInfo.VimInfoTypeEnum.OPENSTACK_V3_INFO);
        vimInfo.setDomain(null);
        when(vnfApi.vnfsPost(createRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenReturn(buildObservable(vnfInfo));
        additionalParam.setInstantiationLevel(INSTANTIATION_LEVEL);
        when(vfcGrantManager.requestGrantForInstantiate(VNFM_ID, VNF_ID, VIM_ID, ONAP_CSAR_ID, INSTANTIATION_LEVEL, cbamVnfdContent, JOB_ID)).thenReturn(grantResponse);
        grantResponse.setVimId(VIM_ID);
        GrantVNFResponseVimAccessInfo accessInfo = new GrantVNFResponseVimAccessInfo();
        accessInfo.setTenant(TENANT);
        vimInfo.setSslInsecure(null);
        grantResponse.setAccessInfo(accessInfo);
        ArgumentCaptor<InstantiateVnfRequest> actualInstantiationRequest = ArgumentCaptor.forClass(InstantiateVnfRequest.class);
        when(vnfApi.vnfsVnfInstanceIdInstantiatePost(eq(VNF_ID), actualInstantiationRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenReturn(buildObservable(instantiationOperationExecution));
        //when
        VnfInstantiateResponse response = lifecycleManager.createAndInstantiate(VNFM_ID, instantiationRequest, restResponse);
        waitForJobToFinishInJobManager(finished);
        assertEquals(0, actualInstantiationRequest.getAllValues().size());
        //verify
        verify(logger).error("The cloud did not supply the cloud domain (Amsterdam release) and was not supplied as additional data");
    }

    /**
     * test instantiation with vcloud
     */
    @Test
    public void testInstantiationVcloud() throws Exception {
        VnfInstantiateRequest instantiationRequest = prepareInstantiationRequest(VimInfo.VimInfoTypeEnum.VMWARE_VCLOUD_INFO);

        when(vnfApi.vnfsPost(createRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenReturn(buildObservable(vnfInfo));
        additionalParam.setInstantiationLevel(INSTANTIATION_LEVEL);
        when(vfcGrantManager.requestGrantForInstantiate(VNFM_ID, VNF_ID, VIM_ID, ONAP_CSAR_ID, INSTANTIATION_LEVEL, cbamVnfdContent, JOB_ID)).thenReturn(grantResponse);
        grantResponse.setVimId(VIM_ID);
        GrantVNFResponseVimAccessInfo accessInfo = new GrantVNFResponseVimAccessInfo();
        accessInfo.setTenant(TENANT);
        grantResponse.setAccessInfo(accessInfo);
        ArgumentCaptor<InstantiateVnfRequest> actualInstantiationRequest = ArgumentCaptor.forClass(InstantiateVnfRequest.class);
        when(vnfApi.vnfsVnfInstanceIdInstantiatePost(eq(VNF_ID), actualInstantiationRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenReturn(buildObservable(instantiationOperationExecution));
        //when
        VnfInstantiateResponse response = lifecycleManager.createAndInstantiate(VNFM_ID, instantiationRequest, restResponse);
        waitForJobToFinishInJobManager(finished);
        assertEquals(1, actualInstantiationRequest.getValue().getVims().size());
        //verify
        VMWAREVCLOUDINFO actualVim = (VMWAREVCLOUDINFO) actualInstantiationRequest.getValue().getVims().get(0);
        assertEquals(VIM_ID, actualVim.getId());
        assertEquals(VimInfo.VimInfoTypeEnum.VMWARE_VCLOUD_INFO, actualVim.getVimInfoType());
        assertEquals(Boolean.valueOf(parseBoolean(vimInfo.getSslInsecure())), actualVim.getInterfaceInfo().isSkipCertificateVerification());
        assertEquals("cloudUrl", actualVim.getInterfaceInfo().getEndpoint());
        //FIXME assertEquals();actualVim.getInterfaceInfo().getTrustedCertificates());
        assertEquals("vimPassword", actualVim.getAccessInfo().getPassword());
        assertEquals("regionId", actualVim.getAccessInfo().getOrganization());
        assertEquals("vimUsername", actualVim.getAccessInfo().getUsername());
        assertTrue(actualVim.getInterfaceInfo().isSkipCertificateVerification());
        assertTrue(actualVim.getInterfaceInfo().isSkipCertificateHostnameCheck());
    }

    /**
     * test instantiation with vCloud with SSL
     */
    @Test
    public void testInstantiationVcloudWithSsl() throws Exception {
        VnfInstantiateRequest instantiationRequest = prepareInstantiationRequest(VimInfo.VimInfoTypeEnum.VMWARE_VCLOUD_INFO);

        when(vnfApi.vnfsPost(createRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenReturn(buildObservable(vnfInfo));
        additionalParam.setInstantiationLevel(INSTANTIATION_LEVEL);
        when(vfcGrantManager.requestGrantForInstantiate(VNFM_ID, VNF_ID, VIM_ID, ONAP_CSAR_ID, INSTANTIATION_LEVEL, cbamVnfdContent, JOB_ID)).thenReturn(grantResponse);
        grantResponse.setVimId(VIM_ID);
        GrantVNFResponseVimAccessInfo accessInfo = new GrantVNFResponseVimAccessInfo();
        accessInfo.setTenant(TENANT);
        String caCert = new String(readAllBytes(Paths.get(TestVfcGrantManager.class.getResource("/unittests/localhost.cert.pem").toURI())));
        vimInfo.setSslInsecure("false");
        vimInfo.setSslCacert(caCert);
        grantResponse.setAccessInfo(accessInfo);
        ArgumentCaptor<InstantiateVnfRequest> actualInstantiationRequest = ArgumentCaptor.forClass(InstantiateVnfRequest.class);
        when(vnfApi.vnfsVnfInstanceIdInstantiatePost(eq(VNF_ID), actualInstantiationRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenReturn(buildObservable(instantiationOperationExecution));
        //when
        VnfInstantiateResponse response = lifecycleManager.createAndInstantiate(VNFM_ID, instantiationRequest, restResponse);
        waitForJobToFinishInJobManager(finished);
        assertEquals(1, actualInstantiationRequest.getValue().getVims().size());
        //verify
        VMWAREVCLOUDINFO actualVim = (VMWAREVCLOUDINFO) actualInstantiationRequest.getValue().getVims().get(0);
        assertEquals(VIM_ID, actualVim.getId());
        assertEquals(VimInfo.VimInfoTypeEnum.VMWARE_VCLOUD_INFO, actualVim.getVimInfoType());
        assertEquals(Boolean.valueOf(parseBoolean(vimInfo.getSslInsecure())), actualVim.getInterfaceInfo().isSkipCertificateVerification());
        assertEquals("cloudUrl", actualVim.getInterfaceInfo().getEndpoint());
        //FIXME assertEquals();actualVim.getInterfaceInfo().getTrustedCertificates());
        assertEquals("vimPassword", actualVim.getAccessInfo().getPassword());
        assertEquals("regionId", actualVim.getAccessInfo().getOrganization());
        assertEquals("vimUsername", actualVim.getAccessInfo().getUsername());
        assertEquals(StoreLoader.getCertifacates(caCert).iterator().next(), new String(actualVim.getInterfaceInfo().getTrustedCertificates().get(0)));
        assertTrue(!actualVim.getInterfaceInfo().isSkipCertificateVerification());
        assertTrue(!actualVim.getInterfaceInfo().isSkipCertificateHostnameCheck());
    }

    /**
     * test instantiation with vCloud with SSL
     */
    @Test
    public void testInstantiationVcloudWithNonSecifedSSl() throws Exception {
        VnfInstantiateRequest instantiationRequest = prepareInstantiationRequest(VimInfo.VimInfoTypeEnum.VMWARE_VCLOUD_INFO);

        when(vnfApi.vnfsPost(createRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenReturn(buildObservable(vnfInfo));
        additionalParam.setInstantiationLevel(INSTANTIATION_LEVEL);
        when(vfcGrantManager.requestGrantForInstantiate(VNFM_ID, VNF_ID, VIM_ID, ONAP_CSAR_ID, INSTANTIATION_LEVEL, cbamVnfdContent, JOB_ID)).thenReturn(grantResponse);
        grantResponse.setVimId(VIM_ID);
        GrantVNFResponseVimAccessInfo accessInfo = new GrantVNFResponseVimAccessInfo();
        accessInfo.setTenant(TENANT);
        vimInfo.setSslInsecure(null);
        grantResponse.setAccessInfo(accessInfo);
        ArgumentCaptor<InstantiateVnfRequest> actualInstantiationRequest = ArgumentCaptor.forClass(InstantiateVnfRequest.class);
        when(vnfApi.vnfsVnfInstanceIdInstantiatePost(eq(VNF_ID), actualInstantiationRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenReturn(buildObservable(instantiationOperationExecution));
        //when
        VnfInstantiateResponse response = lifecycleManager.createAndInstantiate(VNFM_ID, instantiationRequest, restResponse);
        waitForJobToFinishInJobManager(finished);
        assertEquals(1, actualInstantiationRequest.getValue().getVims().size());
        //verify
        VMWAREVCLOUDINFO actualVim = (VMWAREVCLOUDINFO) actualInstantiationRequest.getValue().getVims().get(0);
        assertEquals(VIM_ID, actualVim.getId());
        assertEquals(VimInfo.VimInfoTypeEnum.VMWARE_VCLOUD_INFO, actualVim.getVimInfoType());
        assertEquals("cloudUrl", actualVim.getInterfaceInfo().getEndpoint());
        //FIXME assertEquals();actualVim.getInterfaceInfo().getTrustedCertificates());
        assertEquals("vimPassword", actualVim.getAccessInfo().getPassword());
        assertEquals("regionId", actualVim.getAccessInfo().getOrganization());
        assertEquals("vimUsername", actualVim.getAccessInfo().getUsername());
        assertTrue(actualVim.getInterfaceInfo().isSkipCertificateVerification());
        assertTrue(actualVim.getInterfaceInfo().isSkipCertificateHostnameCheck());
    }

    /**
     * test failure in the instantiation request marks the job to be finished in job manager
     */
    @Test
    public void testFailureInTheInstantiationRequest() throws Exception {
        VnfInstantiateRequest instantiationRequest = prepareInstantiationRequest(VimInfo.VimInfoTypeEnum.OPENSTACK_V2_INFO);
        when(vnfApi.vnfsPost(createRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenReturn(buildObservable(vnfInfo));
        additionalParam.setInstantiationLevel(INSTANTIATION_LEVEL);
        when(vfcGrantManager.requestGrantForInstantiate(VNFM_ID, VNF_ID, VIM_ID, ONAP_CSAR_ID, INSTANTIATION_LEVEL, cbamVnfdContent, JOB_ID)).thenReturn(grantResponse);
        grantResponse.setVimId(VIM_ID);
        GrantVNFResponseVimAccessInfo accessInfo = new GrantVNFResponseVimAccessInfo();
        accessInfo.setTenant(TENANT);
        grantResponse.setAccessInfo(accessInfo);
        ArgumentCaptor<InstantiateVnfRequest> actualInstantiationRequest = ArgumentCaptor.forClass(InstantiateVnfRequest.class);
        RuntimeException expectedException = new RuntimeException();
        when(vnfApi.vnfsVnfInstanceIdInstantiatePost(eq(VNF_ID), actualInstantiationRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenThrow(expectedException);

        //when
        VnfInstantiateResponse response = lifecycleManager.createAndInstantiate(VNFM_ID, instantiationRequest, restResponse);
        //verfiy
        waitForJobToFinishInJobManager(finished);
        assertEquals(VNF_ID, response.getVnfInstanceId());
        assertEquals(JOB_ID, response.getJobId());
        verify(logger).error("Unable to instantiate VNF with myVnfId identifier", expectedException);
    }

    /**
     * instantiation fails if VF-C does not send vim identifier in grant response
     */
    @Test
    public void testVfcFailsToSendVimId() throws Exception {
        VnfInstantiateRequest instantiationRequest = prepareInstantiationRequest(VimInfo.VimInfoTypeEnum.OPENSTACK_V2_INFO);

        when(vnfApi.vnfsPost(createRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenReturn(buildObservable(vnfInfo));
        additionalParam.setInstantiationLevel(INSTANTIATION_LEVEL);
        when(vfcGrantManager.requestGrantForInstantiate(VNFM_ID, VNF_ID, VIM_ID, ONAP_CSAR_ID, INSTANTIATION_LEVEL, cbamVnfdContent, JOB_ID)).thenReturn(grantResponse);
        //grantResponse.setVimId(VIM_ID);
        GrantVNFResponseVimAccessInfo accessInfo = new GrantVNFResponseVimAccessInfo();
        accessInfo.setTenant(TENANT);
        String caCert = new String(readAllBytes(Paths.get(TestVfcGrantManager.class.getResource("/unittests/localhost.cert.pem").toURI())));
        vimInfo.setSslInsecure("false");
        vimInfo.setSslCacert(caCert);
        grantResponse.setAccessInfo(accessInfo);
        ArgumentCaptor<InstantiateVnfRequest> actualInstantiationRequest = ArgumentCaptor.forClass(InstantiateVnfRequest.class);
        when(vnfApi.vnfsVnfInstanceIdInstantiatePost(eq(VNF_ID), actualInstantiationRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenReturn(buildObservable(instantiationOperationExecution));
        //when
        VnfInstantiateResponse response = lifecycleManager.createAndInstantiate(VNFM_ID, instantiationRequest, restResponse);
        waitForJobToFinishInJobManager(finished);
        assertEquals(0, actualInstantiationRequest.getAllValues().size());
        //verify
        verify(logger).error("VF-C did not send VIM identifier in grant response");

    }

    /**
     * test operation execution polling is retried in case of failures
     */
    @Test
    public void testFailureInTheOperationExecutionPollingDuringInstantiationRequest() throws Exception {
        VnfInstantiateRequest instantiationRequest = prepareInstantiationRequest(VimInfo.VimInfoTypeEnum.OPENSTACK_V2_INFO);
        when(vnfApi.vnfsPost(createRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenReturn(buildObservable(vnfInfo));
        additionalParam.setInstantiationLevel(INSTANTIATION_LEVEL);
        when(vfcGrantManager.requestGrantForInstantiate(VNFM_ID, VNF_ID, VIM_ID, ONAP_CSAR_ID, INSTANTIATION_LEVEL, cbamVnfdContent, JOB_ID)).thenReturn(grantResponse);
        grantResponse.setVimId(VIM_ID);
        GrantVNFResponseVimAccessInfo accessInfo = new GrantVNFResponseVimAccessInfo();
        accessInfo.setTenant(TENANT);
        grantResponse.setAccessInfo(accessInfo);
        ArgumentCaptor<InstantiateVnfRequest> actualInstantiationRequest = ArgumentCaptor.forClass(InstantiateVnfRequest.class);
        List<RuntimeException> polling = new ArrayList<>();
        when(vnfApi.vnfsVnfInstanceIdInstantiatePost(eq(VNF_ID), actualInstantiationRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenReturn(buildObservable(instantiationOperationExecution));
        when(vnfApi.vnfsVnfInstanceIdOperationExecutionsGet(VNF_ID, NOKIA_LCM_API_VERSION)).then(new Answer<Observable<List<OperationExecution>>>() {
            @Override
            public Observable<List<OperationExecution>> answer(InvocationOnMock invocation) throws Throwable {
                if (polling.size() > 2) {
                    return buildObservable(operationExecutions);
                }
                RuntimeException runtimeException = new RuntimeException();
                polling.add(runtimeException);
                throw runtimeException;
            }
        });
        //when
        VnfInstantiateResponse response = lifecycleManager.createAndInstantiate(VNFM_ID, instantiationRequest, restResponse);
        //verfiy
        waitForJobToFinishInJobManager(finished);
        assertEquals(VNF_ID, response.getVnfInstanceId());
        assertEquals(JOB_ID, response.getJobId());
        assertEquals(3, polling.size());
        for (RuntimeException e : polling) {
            verify(logger).warn("Unable to retrieve operations details", e);
        }
        verify(systemFunctions, Mockito.times(3)).sleep(5000);
    }

    /**
     * failure in VNF creation is logged an proagated
     */
    @Test
    public void failureInVnfCreationIsPropagated() throws Exception {
        //given
        VnfInstantiateRequest instantiationRequest = prepareInstantiationRequest(VimInfo.VimInfoTypeEnum.OPENSTACK_V2_INFO);

        RuntimeException expectedException = new RuntimeException();
        when(vnfApi.vnfsPost(createRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenThrow(expectedException);
        //when
        try {
            lifecycleManager.createAndInstantiate(VNFM_ID, instantiationRequest, restResponse);
            //verify
            fail();
        } catch (RuntimeException e) {
            assertEquals(expectedException, e.getCause());
            verify(logger).error("Unable to create the VNF", expectedException);
        }
    }

    /**
     * failure in updating the modifyable attributes of the VNF  is logged an proagated
     */
    @Test
    public void failureInVnfModificationIsPropagated() throws Exception {
        //given
        VnfInstantiateRequest instantiationRequest = prepareInstantiationRequest(VimInfo.VimInfoTypeEnum.OPENSTACK_V2_INFO);

        RuntimeException expectedException = new RuntimeException();
        when(vnfApi.vnfsPost(createRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenReturn(buildObservable(vnfInfo));
        when(vnfApi.vnfsVnfInstanceIdPatch(eq(VNF_ID), actualVnfModifyRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenThrow(expectedException);

        //when
        try {
            lifecycleManager.createAndInstantiate(VNFM_ID, instantiationRequest, restResponse);
            //verify
            fail();
        } catch (RuntimeException e) {
            assertEquals(expectedException, e.getCause().getCause());
            verify(logger).error("Unable to set the onapCsarId property on the VNF", expectedException);
        }
    }

    /**
     * if the VIM info can not be queried the VNF is not instantiated and
     * error propagated through job
     */
    @Test
    public void testFailureInQueryVimInfo() throws Exception {
        VnfInstantiateRequest instantiationRequest = prepareInstantiationRequest(VimInfo.VimInfoTypeEnum.OPENSTACK_V2_INFO);
        when(vnfApi.vnfsPost(createRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenReturn(buildObservable(vnfInfo));
        when(vfcGrantManager.requestGrantForInstantiate(VNFM_ID, VNF_ID, VIM_ID, ONAP_CSAR_ID, INSTANTIATION_LEVEL, cbamVnfdContent, JOB_ID)).thenReturn(grantResponse);
        grantResponse.setVimId(VIM_ID);
        GrantVNFResponseVimAccessInfo accessInfo = new GrantVNFResponseVimAccessInfo();
        accessInfo.setTenant(TENANT);
        grantResponse.setAccessInfo(accessInfo);

        when(vimInfoProvider.getVimInfo(VIM_ID)).thenThrow(new RuntimeException());
        //when
        lifecycleManager.createAndInstantiate(VNFM_ID, instantiationRequest, restResponse);
        //verify
        waitForJobToFinishInJobManager(finished);
        verify(vnfApi, never()).vnfsVnfInstanceIdInstantiatePost(Mockito.any(), Mockito.any(), Mockito.any());
    }

    /**
     * test termination basic success scenario
     * - the VNF is not deleted before the notifications are processed
     */
    @Test
    public void testTerminationAndDeletion() throws Exception {
        //given
        VnfTerminateRequest terminationRequest = new VnfTerminateRequest();
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(vnfInfo));
        vnfInfo.setInstantiationState(InstantiationState.INSTANTIATED);
        vnfInfo.setOperationExecutions(operationExecutions);
        VnfProperty vnfdId = new VnfProperty();
        vnfdId.setName(LifecycleManager.ONAP_CSAR_ID);
        vnfdId.setValue(ONAP_CSAR_ID);
        vnfInfo.getExtensions().add(vnfdId);
        ArgumentCaptor<TerminateVnfRequest> actualTerminationRequest = ArgumentCaptor.forClass(TerminateVnfRequest.class);
        when(vnfApi.vnfsVnfInstanceIdTerminatePost(eq(VNF_ID), actualTerminationRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenAnswer(new Answer<Observable<OperationExecution>>() {
            @Override
            public Observable<OperationExecution> answer(InvocationOnMock invocation) throws Throwable {
                OperationExecution terminationOperation = new OperationExecution();
                terminationOperation.setId("terminationId");
                operationExecutions.add(terminationOperation);
                terminationOperation.setStatus(OperationStatus.FINISHED);
                return buildObservable(terminationOperation);
            }
        });
        when(vnfApi.vnfsVnfInstanceIdDelete(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(VOID_OBSERVABLE.value());
        JsonElement instantiationParameters = new JsonParser().parse("{ \"vims\" : [ { \"id\" : \"" + VIM_ID + "\" } ] } ");
        when(operationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet("operationExecutionId", NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(instantiationParameters));
        //when
        JobInfo jobInfo = lifecycleManager.terminateAndDelete(VNFM_ID, VNF_ID, terminationRequest, restResponse);
        //verify
        waitForJobToFinishInJobManager(finished);
        assertEquals(1, actualTerminationRequest.getAllValues().size());
        assertEquals(TerminationType.FORCEFUL, actualTerminationRequest.getValue().getTerminationType());
        assertEquals(JOB_ID, new Gson().toJsonTree(actualTerminationRequest.getValue().getAdditionalParams()).getAsJsonObject().get("jobId").getAsString());
        InOrder notificationIsProcessedBeforeDeletingTheVnf = Mockito.inOrder(vfcGrantManager, notificationManager, vnfApi);
        notificationIsProcessedBeforeDeletingTheVnf.verify(vfcGrantManager).requestGrantForTerminate(VNFM_ID, VNF_ID, VIM_ID, ONAP_CSAR_ID, vnfInfo, JOB_ID);
        notificationIsProcessedBeforeDeletingTheVnf.verify(notificationManager).waitForTerminationToBeProcessed("terminationId");
        notificationIsProcessedBeforeDeletingTheVnf.verify(vnfApi).vnfsVnfInstanceIdDelete(VNF_ID, NOKIA_LCM_API_VERSION);
        VOID_OBSERVABLE.assertCalled();
        verify(jobManager).spawnJob(VNF_ID, restResponse);
        verify(logger).info(eq("Starting {} operation on VNF with {} identifier with {} parameter"), eq("termination"), eq(VNF_ID), anyString());
    }

    /**
     * test termination basic success scenario
     * - the VNF is not deleted before the notifications are processed
     */
    @Test
    public void testTermination() throws Exception {
        //given
        VnfTerminateRequest terminationRequest = new VnfTerminateRequest();
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(vnfInfo));
        vnfInfo.setInstantiationState(InstantiationState.INSTANTIATED);
        vnfInfo.setOperationExecutions(operationExecutions);
        VnfProperty vnfdId = new VnfProperty();
        vnfdId.setName(LifecycleManager.ONAP_CSAR_ID);
        vnfdId.setValue(ONAP_CSAR_ID);
        vnfInfo.getExtensions().add(vnfdId);
        ArgumentCaptor<TerminateVnfRequest> actualTerminationRequest = ArgumentCaptor.forClass(TerminateVnfRequest.class);
        when(vnfApi.vnfsVnfInstanceIdTerminatePost(eq(VNF_ID), actualTerminationRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenAnswer(new Answer<Observable<OperationExecution>>() {
            @Override
            public Observable<OperationExecution> answer(InvocationOnMock invocation) throws Throwable {
                OperationExecution terminationOperation = new OperationExecution();
                terminationOperation.setId("terminationId");
                operationExecutions.add(terminationOperation);
                terminationOperation.setStatus(OperationStatus.FINISHED);
                return buildObservable(terminationOperation);
            }
        });
        JsonElement instantiationParameters = new JsonParser().parse("{ \"vims\" : [ { \"id\" : \"" + VIM_ID + "\" } ] } ");
        when(operationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet("operationExecutionId", NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(instantiationParameters));
        //when
        JobInfo jobInfo = lifecycleManager.terminate(VNFM_ID, VNF_ID, terminationRequest, restResponse);
        //verify
        waitForJobToFinishInJobManager(finished);
        assertEquals(1, actualTerminationRequest.getAllValues().size());
        assertEquals(TerminationType.FORCEFUL, actualTerminationRequest.getValue().getTerminationType());
        assertEquals(JOB_ID, new Gson().toJsonTree(actualTerminationRequest.getValue().getAdditionalParams()).getAsJsonObject().get("jobId").getAsString());
        InOrder notificationIsProcessedBeforeDeletingTheVnf = Mockito.inOrder(vfcGrantManager, notificationManager, vnfApi);
        notificationIsProcessedBeforeDeletingTheVnf.verify(vfcGrantManager).requestGrantForTerminate(VNFM_ID, VNF_ID, VIM_ID, ONAP_CSAR_ID, vnfInfo, JOB_ID);
        notificationIsProcessedBeforeDeletingTheVnf.verify(notificationManager).waitForTerminationToBeProcessed("terminationId");
        verify(vnfApi, never()).vnfsVnfInstanceIdDelete(VNF_ID, NOKIA_LCM_API_VERSION);
        verify(jobManager).spawnJob(VNF_ID, restResponse);
        verify(logger).info(eq("Starting {} operation on VNF with {} identifier with {} parameter"), eq("termination"), eq(VNF_ID), anyString());
    }

    /**
     * test termination of a non instantiated VNF
     * - the VNF is not terminated (only deleted)
     */
    @Test
    public void testTerminationOfNonInstantiated() throws Exception {
        //given
        VnfTerminateRequest terminationRequest = new VnfTerminateRequest();
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(vnfInfo));
        vnfInfo.setInstantiationState(InstantiationState.NOT_INSTANTIATED);
        vnfInfo.setOperationExecutions(operationExecutions);
        VnfProperty vnfdId = new VnfProperty();
        vnfdId.setName(LifecycleManager.ONAP_CSAR_ID);
        vnfdId.setValue(ONAP_CSAR_ID);
        vnfInfo.getExtensions().add(vnfdId);
        JsonElement instantiationParameters = new JsonParser().parse("{ \"vims\" : [ { \"id\" : \"" + VIM_ID + "\" } ] } ");
        when(vnfApi.vnfsVnfInstanceIdDelete(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(VOID_OBSERVABLE.value());
        when(operationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet("operationExecutionId", NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(instantiationParameters));
        //when
        lifecycleManager.terminateAndDelete(VNFM_ID, VNF_ID, terminationRequest, restResponse);
        //verify
        boolean deleted = false;
        while (!deleted) {
            try {
                verify(logger).info("The VNF with {} identifier has been deleted", VNF_ID);
                deleted = true;
            } catch (Error e) {
            }
        }
        verify(vfcGrantManager, never()).requestGrantForTerminate(VNFM_ID, VNF_ID, VIM_ID, ONAP_CSAR_ID, vnfInfo, JOB_ID);
        verify(notificationManager, never()).waitForTerminationToBeProcessed("terminationId");
        verify(logger).warn("The VNF with {} identifier is not instantiated no termination is required", VNF_ID);
        verify(logger).info("Deleting VNF with {} identifier", VNF_ID);
        verify(vnfApi).vnfsVnfInstanceIdDelete(VNF_ID, NOKIA_LCM_API_VERSION);
        VOID_OBSERVABLE.assertCalled();
    }

    /**
     * test that the VNF deletion is not started before the termination finishes
     */
    @Test
    public void testTerminationOperationIsOutwaited() throws Exception {
        //given
        VnfTerminateRequest terminationRequest = new VnfTerminateRequest();
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(vnfInfo));
        vnfInfo.setInstantiationState(InstantiationState.INSTANTIATED);
        vnfInfo.setOperationExecutions(operationExecutions);
        VnfProperty vnfdId = new VnfProperty();
        vnfdId.setName(LifecycleManager.ONAP_CSAR_ID);
        vnfdId.setValue(ONAP_CSAR_ID);
        vnfInfo.getExtensions().add(vnfdId);
        ArgumentCaptor<TerminateVnfRequest> actualTerminationRequest = ArgumentCaptor.forClass(TerminateVnfRequest.class);
        OperationExecution terminationOperation = new OperationExecution();
        when(vnfApi.vnfsVnfInstanceIdTerminatePost(eq(VNF_ID), actualTerminationRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenAnswer(invocation -> {
            terminationOperation.setId("terminationId");
            operationExecutions.add(terminationOperation);
            terminationOperation.setStatus(OperationStatus.STARTED);
            return buildObservable(terminationOperation);
        });
        JsonElement instantiationParameters = new JsonParser().parse("{ \"vims\" : [ { \"id\" : \"" + VIM_ID + "\" } ] } ");
        when(operationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet("operationExecutionId", NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(instantiationParameters));
        Set<Integer> calls = new HashSet<>();
        when(vnfApi.vnfsVnfInstanceIdOperationExecutionsGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenAnswer(invocation -> {
            if (calls.size() == 1000) {
                terminationOperation.setStatus(OperationStatus.FINISHED);
            }
            calls.add(calls.size());
            return buildObservable(operationExecutions);
        });
        //when
        JobInfo jobInfo = lifecycleManager.terminateAndDelete(VNFM_ID, VNF_ID, terminationRequest, restResponse);
        //verify
        waitForJobToFinishInJobManager(finished);
        verify(vnfApi, times(1001)).vnfsVnfInstanceIdOperationExecutionsGet(VNF_ID, NOKIA_LCM_API_VERSION);
        verify(systemFunctions, times(1000)).sleep(5000);
    }


    /**
     * test that failured during waiting for the operation to finish is tolerated (idefineiatelly)
     */
    @Test
    public void testTerminationOperationIsOutwaitedWithErrors() throws Exception {
        //given
        VnfTerminateRequest terminationRequest = new VnfTerminateRequest();
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(vnfInfo));
        vnfInfo.setInstantiationState(InstantiationState.INSTANTIATED);
        vnfInfo.setOperationExecutions(operationExecutions);
        VnfProperty vnfdId = new VnfProperty();
        vnfdId.setName(LifecycleManager.ONAP_CSAR_ID);
        vnfdId.setValue(ONAP_CSAR_ID);
        vnfInfo.getExtensions().add(vnfdId);
        ArgumentCaptor<TerminateVnfRequest> actualTerminationRequest = ArgumentCaptor.forClass(TerminateVnfRequest.class);
        OperationExecution terminationOperation = new OperationExecution();
        when(vnfApi.vnfsVnfInstanceIdTerminatePost(eq(VNF_ID), actualTerminationRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenAnswer(new Answer<Observable<OperationExecution>>() {
            @Override
            public Observable<OperationExecution> answer(InvocationOnMock invocation) throws Throwable {
                terminationOperation.setId("terminationId");
                operationExecutions.add(terminationOperation);
                terminationOperation.setStatus(OperationStatus.STARTED);
                return buildObservable(terminationOperation);
            }
        });
        JsonElement instantiationParameters = new JsonParser().parse("{ \"vims\" : [ { \"id\" : \"" + VIM_ID + "\" } ] } ");
        when(operationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet("operationExecutionId", NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(instantiationParameters));
        Set<Integer> calls = new HashSet<>();
        List<RuntimeException> expectedExceptions = new ArrayList<>();
        when(vnfApi.vnfsVnfInstanceIdOperationExecutionsGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenAnswer(new Answer<Observable<List<OperationExecution>>>() {
            @Override
            public Observable<List<OperationExecution>> answer(InvocationOnMock invocation) throws Throwable {
                if (calls.size() >= 100) {
                    terminationOperation.setStatus(OperationStatus.FINISHED);
                    return buildObservable(operationExecutions);
                }
                calls.add(calls.size());
                RuntimeException RuntimeException = new RuntimeException();
                expectedExceptions.add(RuntimeException);
                throw RuntimeException;
            }
        });
        //when
        JobInfo jobInfo = lifecycleManager.terminateAndDelete(VNFM_ID, VNF_ID, terminationRequest, restResponse);
        //verify
        waitForJobToFinishInJobManager(finished);
        verify(vnfApi, times(101)).vnfsVnfInstanceIdOperationExecutionsGet(VNF_ID, NOKIA_LCM_API_VERSION);
        verify(systemFunctions, times(100)).sleep(5000);
        for (RuntimeException expectedException : expectedExceptions) {
            verify(logger).warn("Unable to retrieve operations details", expectedException);
        }
    }

    /**
     * test gracefull termination
     */
    @Test
    public void testGracefullTermination() throws Exception {
        //given
        VnfTerminateRequest terminationRequest = new VnfTerminateRequest();
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(vnfInfo));
        terminationRequest.setTerminationType(VnfTerminationType.GRACEFUL);
        terminationRequest.setGracefulTerminationTimeout("1234");
        vnfInfo.setInstantiationState(InstantiationState.INSTANTIATED);
        vnfInfo.setOperationExecutions(operationExecutions);
        VnfProperty vnfdId = new VnfProperty();
        vnfdId.setName(LifecycleManager.ONAP_CSAR_ID);
        vnfdId.setValue(ONAP_CSAR_ID);
        vnfInfo.getExtensions().add(vnfdId);
        ArgumentCaptor<TerminateVnfRequest> actualTerminationRequest = ArgumentCaptor.forClass(TerminateVnfRequest.class);
        when(vnfApi.vnfsVnfInstanceIdTerminatePost(eq(VNF_ID), actualTerminationRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenAnswer(new Answer<Observable<OperationExecution>>() {
            @Override
            public Observable<OperationExecution> answer(InvocationOnMock invocation) throws Throwable {
                OperationExecution terminationOperation = new OperationExecution();
                terminationOperation.setId("terminationId");
                operationExecutions.add(terminationOperation);
                terminationOperation.setStatus(OperationStatus.FINISHED);
                return buildObservable(terminationOperation);
            }
        });
        doAnswer(invocation -> {
            verify(jobManager, Mockito.never()).jobFinished(JOB_ID);
            return null;
        }).when(vnfApi).vnfsVnfInstanceIdDelete(VNF_ID, NOKIA_LCM_API_VERSION);
        JsonElement instantiationParameters = new JsonParser().parse("{ \"vims\" : [ { \"id\" : \"" + VIM_ID + "\" } ] } ");
        when(operationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet("operationExecutionId", NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(instantiationParameters));
        //when
        JobInfo jobInfo = lifecycleManager.terminateAndDelete(VNFM_ID, VNF_ID, terminationRequest, restResponse);
        //verify
        waitForJobToFinishInJobManager(finished);
        assertEquals(1, actualTerminationRequest.getAllValues().size());
        assertEquals(TerminationType.GRACEFUL, actualTerminationRequest.getValue().getTerminationType());
        assertEquals(Integer.valueOf(1234), actualTerminationRequest.getValue().getGracefulTerminationTimeout());
        InOrder notificationIsProcessedBeforeDeletingTheVnf = Mockito.inOrder(vfcGrantManager, notificationManager, vnfApi);
        notificationIsProcessedBeforeDeletingTheVnf.verify(vfcGrantManager).requestGrantForTerminate(VNFM_ID, VNF_ID, VIM_ID, ONAP_CSAR_ID, vnfInfo, JOB_ID);
        notificationIsProcessedBeforeDeletingTheVnf.verify(notificationManager).waitForTerminationToBeProcessed("terminationId");
        notificationIsProcessedBeforeDeletingTheVnf.verify(vnfApi).vnfsVnfInstanceIdDelete(VNF_ID, NOKIA_LCM_API_VERSION);
    }

    /**
     * instantiation with missing ONAP csarId to instantiation extra param result in failure
     */
    @Test
    public void testMissingVnfParameters() throws Exception {
        //given
        VnfInstantiateRequest instantiationRequest = prepareInstantiationRequest(VimInfo.VimInfoTypeEnum.OPENSTACK_V2_INFO);
        String src = "{ \"inputs\" : { \"vnfs\" : { \"" + ONAP_CSAR_ID + "invalid" + "\" : {}}}, \"vimId\" : \"" + VIM_ID + "\"}";
        instantiationRequest.setAdditionalParam(new JsonParser().parse(src));
        //when
        try {
            VnfInstantiateResponse response = lifecycleManager.createAndInstantiate(VNFM_ID, instantiationRequest, restResponse);
            fail();
        } catch (Exception e) {
            assertEquals("The additional parameter section does not contain setting for VNF with myOnapCsarId CSAR id", e.getMessage());
            verify(logger).error("The additional parameter section does not contain setting for VNF with myOnapCsarId CSAR id");
        }
    }

    /**
     * test explicit forceful termination
     */
    @Test
    public void testExplicitForcefulTermination() throws Exception {
        //given
        VnfTerminateRequest terminationRequest = new VnfTerminateRequest();
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(vnfInfo));
        terminationRequest.setTerminationType(VnfTerminationType.FORCEFUL);
        terminationRequest.setGracefulTerminationTimeout("1234");
        vnfInfo.setInstantiationState(InstantiationState.INSTANTIATED);
        vnfInfo.setOperationExecutions(operationExecutions);
        VnfProperty vnfdId = new VnfProperty();
        vnfdId.setName(LifecycleManager.ONAP_CSAR_ID);
        vnfdId.setValue(ONAP_CSAR_ID);
        vnfInfo.getExtensions().add(vnfdId);
        ArgumentCaptor<TerminateVnfRequest> actualTerminationRequest = ArgumentCaptor.forClass(TerminateVnfRequest.class);
        when(vnfApi.vnfsVnfInstanceIdTerminatePost(eq(VNF_ID), actualTerminationRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenAnswer(invocation -> {
            OperationExecution terminationOperation = new OperationExecution();
            terminationOperation.setId("terminationId");
            operationExecutions.add(terminationOperation);
            terminationOperation.setStatus(OperationStatus.FINISHED);
            return buildObservable(terminationOperation);
        });
        doAnswer(invocation -> {
            verify(jobManager, Mockito.never()).jobFinished(JOB_ID);
            return null;
        }).when(vnfApi).vnfsVnfInstanceIdDelete(VNF_ID, NOKIA_LCM_API_VERSION);
        JsonElement instantiationParameters = new JsonParser().parse("{ \"vims\" : [ { \"id\" : \"" + VIM_ID + "\" } ] } ");
        when(operationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet("operationExecutionId", NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(instantiationParameters));
        //when
        JobInfo jobInfo = lifecycleManager.terminateAndDelete(VNFM_ID, VNF_ID, terminationRequest, restResponse);
        //verify
        waitForJobToFinishInJobManager(finished);
        assertEquals(1, actualTerminationRequest.getAllValues().size());
        assertEquals(TerminationType.FORCEFUL, actualTerminationRequest.getValue().getTerminationType());
        assertNull(actualTerminationRequest.getValue().getGracefulTerminationTimeout());
        InOrder notificationIsProcessedBeforeDeletingTheVnf = Mockito.inOrder(vfcGrantManager, notificationManager, vnfApi);
        notificationIsProcessedBeforeDeletingTheVnf.verify(vfcGrantManager).requestGrantForTerminate(VNFM_ID, VNF_ID, VIM_ID, ONAP_CSAR_ID, vnfInfo, JOB_ID);
        notificationIsProcessedBeforeDeletingTheVnf.verify(notificationManager).waitForTerminationToBeProcessed("terminationId");
        notificationIsProcessedBeforeDeletingTheVnf.verify(vnfApi).vnfsVnfInstanceIdDelete(VNF_ID, NOKIA_LCM_API_VERSION);
    }

    /**
     * test failure in the termination workflow finishes the job
     */
    @Test
    public void testFailureInTheTerminationFinishesTheManagedJob() throws Exception {
        //given
        VnfTerminateRequest terminationRequest = new VnfTerminateRequest();
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(vnfInfo));
        terminationRequest.setTerminationType(VnfTerminationType.FORCEFUL);
        terminationRequest.setGracefulTerminationTimeout("1234");
        vnfInfo.setInstantiationState(InstantiationState.INSTANTIATED);
        vnfInfo.setOperationExecutions(operationExecutions);
        VnfProperty vnfdId = new VnfProperty();
        vnfdId.setName(LifecycleManager.ONAP_CSAR_ID);
        vnfdId.setValue(ONAP_CSAR_ID);
        vnfInfo.getExtensions().add(vnfdId);
        ArgumentCaptor<TerminateVnfRequest> actualTerminationRequest = ArgumentCaptor.forClass(TerminateVnfRequest.class);
        when(vnfApi.vnfsVnfInstanceIdTerminatePost(eq(VNF_ID), actualTerminationRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenAnswer(new Answer<OperationExecution>() {
            @Override
            public OperationExecution answer(InvocationOnMock invocation) throws Throwable {
                OperationExecution terminationOperation = new OperationExecution();
                terminationOperation.setId("terminationId");
                operationExecutions.add(terminationOperation);
                terminationOperation.setStatus(OperationStatus.FINISHED);
                return terminationOperation;
            }
        });
        RuntimeException expectedException = new RuntimeException();
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenThrow(expectedException);
        JsonElement instantiationParameters = new JsonParser().parse("{ \"vims\" : [ { \"id\" : \"" + VIM_ID + "\" } ] } ");
        when(operationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet("operationExecutionId", NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(instantiationParameters));
        //when
        JobInfo jobInfo = lifecycleManager.terminateAndDelete(VNFM_ID, VNF_ID, terminationRequest, restResponse);
        //verify
        waitForJobToFinishInJobManager(finished);
        assertEquals(0, actualTerminationRequest.getAllValues().size());
        Mockito.verifyZeroInteractions(vfcGrantManager);
    }

    /**
     * if termination fails the VNF is not deleted
     */
    @Test
    public void testFailedTerminationAbortsTerminationWorkflow() throws Exception {
        //given
        VnfTerminateRequest terminationRequest = new VnfTerminateRequest();
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(vnfInfo));
        vnfInfo.setInstantiationState(InstantiationState.INSTANTIATED);
        vnfInfo.setOperationExecutions(operationExecutions);
        VnfProperty vnfdId = new VnfProperty();
        vnfdId.setName(LifecycleManager.ONAP_CSAR_ID);
        vnfdId.setValue(ONAP_CSAR_ID);
        vnfInfo.getExtensions().add(vnfdId);
        ArgumentCaptor<TerminateVnfRequest> actualTerminationRequest = ArgumentCaptor.forClass(TerminateVnfRequest.class);
        when(vnfApi.vnfsVnfInstanceIdTerminatePost(eq(VNF_ID), actualTerminationRequest.capture(), eq(NOKIA_LCM_API_VERSION))).thenAnswer(new Answer<Observable<OperationExecution>>() {
            @Override
            public Observable<OperationExecution> answer(InvocationOnMock invocation) throws Throwable {
                OperationExecution terminationOperation = new OperationExecution();
                terminationOperation.setId("terminationId");
                operationExecutions.add(terminationOperation);
                terminationOperation.setStatus(OperationStatus.FAILED);
                return buildObservable(terminationOperation);
            }
        });
        JsonElement instantiationParameters = new JsonParser().parse("{ \"vims\" : [ { \"id\" : \"" + VIM_ID + "\" } ] } ");
        when(operationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet("operationExecutionId", NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(instantiationParameters));
        //when
        JobInfo jobInfo = lifecycleManager.terminateAndDelete(VNFM_ID, VNF_ID, terminationRequest, restResponse);
        //verify
        waitForJobToFinishInJobManager(finished);
        assertEquals(1, actualTerminationRequest.getAllValues().size());
        assertEquals(TerminationType.FORCEFUL, actualTerminationRequest.getValue().getTerminationType());
        verify(vfcGrantManager).requestGrantForTerminate(VNFM_ID, VNF_ID, VIM_ID, ONAP_CSAR_ID, vnfInfo, JOB_ID);
        verify(vnfApi, never()).vnfsVnfInstanceIdDelete(VNF_ID, NOKIA_LCM_API_VERSION);
        verify(logger).error("Unable to terminate VNF the operation did not finish with success");
    }

    /**
     * test VNF query basic success scenario
     */
    @Test
    public void testQuery() throws Exception {
        vnfInfo.setDescription("myDescription");
        vnfInfo.setName("myName");
        vnfInfo.setVnfSoftwareVersion("vnfSoftwareVersion");
        vnfInfo.setVnfProvider("myProvider");
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(vnfInfo));
        VnfProperty prop = new VnfProperty();
        prop.setName(LifecycleManager.ONAP_CSAR_ID);
        prop.setValue(ONAP_CSAR_ID);
        vnfInfo.getExtensions().add(prop);
        //when
        org.onap.vnfmdriver.model.VnfInfo vnf = lifecycleManager.queryVnf(VNFM_ID, VNF_ID);
        //verify
        assertEquals(VNF_ID, vnf.getVnfInstanceId());
        //FIXME ? (do not know what exactly the vnf version mean in core terminology)
        assertEquals("vnfSoftwareVersion", vnf.getVersion());
        assertEquals(ONAP_CSAR_ID, vnf.getVnfdId());
        assertEquals("myDescription", vnf.getVnfInstanceDescription());
        assertEquals("myName", vnf.getVnfInstanceName());
        assertEquals(ONAP_CSAR_ID, vnf.getVnfPackageId());
        assertEquals("myProvider", vnf.getVnfProvider());
        //FIXME (in swagger schema )
        assertEquals("ACTIVE", vnf.getVnfStatus());
        assertEquals("Kuku", vnf.getVnfType());
    }

    /**
     * error is propagated and logged if the queried VNF does not exist
     */
    @Test
    public void testQueryForNonExistingVnf() throws Exception {

        RuntimeException expectedException = new RuntimeException();
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenThrow(expectedException);
        //when
        try {
            lifecycleManager.queryVnf(VNFM_ID, VNF_ID);
            //verify
            fail();
        } catch (Exception e) {
            verify(logger).error("Unable to query VNF (myVnfId)", expectedException);
            assertEquals(expectedException, e.getCause());
        }
    }

    /**
     * test scale basic scenario
     */
    @Test
    public void testScale() throws Exception {
        VnfScaleRequest scaleRequest = new VnfScaleRequest();
        scaleRequest.setNumberOfSteps("2");
        scaleRequest.setAspectId("myAspect");
        scaleRequest.setType(ScaleDirection.IN);
        scaleRequest.setAdditionalParam(new JsonParser().parse("{ \"a\" : \"b\", \"c\" : \"d\" }"));
        scaleOperationExecution.setStatus(OperationStatus.FINISHED);
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(vnfInfo));
        VnfProperty prop = new VnfProperty();
        prop.setValue(ONAP_CSAR_ID);
        prop.setName(LifecycleManager.ONAP_CSAR_ID);
        vnfInfo.getExtensions().add(prop);
        vnfInfo.getOperationExecutions().add(instantiationOperationExecution);
        String instantiationParams = "{ \"vims\" : [ { \"id\" : \"" + VIM_ID + "\" } ] }";
        when(operationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet(instantiationOperationExecution.getId(), NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(new JsonParser().parse(instantiationParams)));
        //when
        JobInfo job = lifecycleManager.scaleVnf(VNFM_ID, VNF_ID, scaleRequest, restResponse);
        //verify
        waitForJobToFinishInJobManager(finished);
        assertEquals(1, actualScaleRequest.getAllValues().size());
        ScaleVnfRequest sRequest = actualScaleRequest.getValue();
        InOrder workflowOrder = Mockito.inOrder(vfcGrantManager, vnfApi);
        workflowOrder.verify(vfcGrantManager).requestGrantForScale(eq(VNFM_ID), eq(VNF_ID), eq(VIM_ID), eq(ONAP_CSAR_ID), eq(scaleRequest), eq(JOB_ID));
        workflowOrder.verify(vnfApi).vnfsVnfInstanceIdScalePost(VNF_ID, sRequest, NOKIA_LCM_API_VERSION);
        assertEquals("myAspect", sRequest.getAspectId());
        assertEquals(com.nokia.cbam.lcm.v32.model.ScaleDirection.IN, sRequest.getType());
        assertEquals(Integer.valueOf(2), sRequest.getNumberOfSteps());
        assertTrue("{\"jobId\":\"myJobId\",\"a\":\"b\"}".equals(new Gson().toJson(sRequest.getAdditionalParams())) || "{\"a\":\"b\",\"jobId\":\"myJobId\"}".equals(new Gson().toJson(sRequest.getAdditionalParams())));
        verify(jobManager).spawnJob(VNF_ID, restResponse);
        verify(logger).info(eq("Starting {} operation on VNF with {} identifier with {} parameter"), eq("scale"), eq(VNF_ID), anyString());

    }

    /**
     * the VNFM should tolerate that no additional params were supplied
     */
    @Test
    public void testScaleWithoutAddtionalParams() throws Exception {
        VnfScaleRequest scaleRequest = new VnfScaleRequest();
        scaleRequest.setNumberOfSteps("2");
        scaleRequest.setAspectId("myAspect");
        scaleRequest.setType(ScaleDirection.IN);
        scaleRequest.setAdditionalParam(null);
        scaleOperationExecution.setStatus(OperationStatus.FINISHED);
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(vnfInfo));
        VnfProperty prop = new VnfProperty();
        prop.setValue(ONAP_CSAR_ID);
        prop.setName(LifecycleManager.ONAP_CSAR_ID);
        vnfInfo.getExtensions().add(prop);
        vnfInfo.getOperationExecutions().add(instantiationOperationExecution);
        String instantiationParams = "{ \"vims\" : [ { \"id\" : \"" + VIM_ID + "\" } ] }";
        when(operationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet(instantiationOperationExecution.getId(), NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(new JsonParser().parse(instantiationParams)));
        //when
        JobInfo job = lifecycleManager.scaleVnf(VNFM_ID, VNF_ID, scaleRequest, restResponse);
        //verify
        waitForJobToFinishInJobManager(finished);
        assertEquals(1, actualScaleRequest.getAllValues().size());
        ScaleVnfRequest sRequest = actualScaleRequest.getValue();
        InOrder workflowOrder = Mockito.inOrder(vfcGrantManager, vnfApi);
        workflowOrder.verify(vfcGrantManager).requestGrantForScale(eq(VNFM_ID), eq(VNF_ID), eq(VIM_ID), eq(ONAP_CSAR_ID), eq(scaleRequest), eq(JOB_ID));
        workflowOrder.verify(vnfApi).vnfsVnfInstanceIdScalePost(VNF_ID, sRequest, NOKIA_LCM_API_VERSION);
        assertEquals("myAspect", sRequest.getAspectId());
        assertEquals(com.nokia.cbam.lcm.v32.model.ScaleDirection.IN, sRequest.getType());
        assertEquals(Integer.valueOf(2), sRequest.getNumberOfSteps());
        assertEquals("{\"jobId\":\"myJobId\"}", new Gson().toJson(sRequest.getAdditionalParams()));
        verify(jobManager).spawnJob(VNF_ID, restResponse);
    }

    /**
     * test scale out basic scenario
     */
    @Test
    public void testScaleOut() throws Exception {
        VnfScaleRequest scaleRequest = new VnfScaleRequest();
        scaleRequest.setNumberOfSteps("2");
        scaleRequest.setAspectId("myAspect");
        scaleRequest.setType(ScaleDirection.OUT);
        scaleRequest.setAdditionalParam(new JsonParser().parse("{ \"a\" : \"b\" }"));
        scaleOperationExecution.setStatus(OperationStatus.FINISHED);
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(vnfInfo));
        VnfProperty prop = new VnfProperty();
        prop.setValue(ONAP_CSAR_ID);
        prop.setName(LifecycleManager.ONAP_CSAR_ID);
        vnfInfo.getExtensions().add(prop);
        vnfInfo.getOperationExecutions().add(instantiationOperationExecution);
        String instantiationParams = "{ \"vims\" : [ { \"id\" : \"" + VIM_ID + "\" } ] }";
        when(operationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet(instantiationOperationExecution.getId(), NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(new JsonParser().parse(instantiationParams)));
        //when
        JobInfo job = lifecycleManager.scaleVnf(VNFM_ID, VNF_ID, scaleRequest, restResponse);
        //verify
        waitForJobToFinishInJobManager(finished);
        assertEquals(1, actualScaleRequest.getAllValues().size());
        ScaleVnfRequest sRequest = actualScaleRequest.getValue();
        InOrder workflowOrder = Mockito.inOrder(vfcGrantManager, vnfApi);
        workflowOrder.verify(vfcGrantManager).requestGrantForScale(eq(VNFM_ID), eq(VNF_ID), eq(VIM_ID), eq(ONAP_CSAR_ID), eq(scaleRequest), eq(JOB_ID));
        workflowOrder.verify(vnfApi).vnfsVnfInstanceIdScalePost(VNF_ID, sRequest, NOKIA_LCM_API_VERSION);
        assertEquals("myAspect", sRequest.getAspectId());
        assertEquals(com.nokia.cbam.lcm.v32.model.ScaleDirection.OUT, sRequest.getType());
        assertEquals(Integer.valueOf(2), sRequest.getNumberOfSteps());
        assertTrue("{\"jobId\":\"myJobId\",\"a\":\"b\"}".equals(new Gson().toJson(sRequest.getAdditionalParams())) || "{\"a\":\"b\",\"jobId\":\"myJobId\"}".equals(new Gson().toJson(sRequest.getAdditionalParams())));
    }

    /**
     * test scale operation is out waited
     */
    @Test
    public void testScaleOutwait() throws Exception {
        VnfScaleRequest scaleRequest = new VnfScaleRequest();
        scaleRequest.setNumberOfSteps("2");
        scaleRequest.setAspectId("myAspect");
        scaleRequest.setType(ScaleDirection.IN);
        scaleRequest.setAdditionalParam(new JsonParser().parse("{ \"a\" : \"b\" }"));
        scaleOperationExecution.setStatus(OperationStatus.STARTED);
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(vnfInfo));
        VnfProperty prop = new VnfProperty();
        prop.setValue(ONAP_CSAR_ID);
        prop.setName(LifecycleManager.ONAP_CSAR_ID);
        vnfInfo.getExtensions().add(prop);
        vnfInfo.getOperationExecutions().add(instantiationOperationExecution);
        String instantiationParams = "{ \"vims\" : [ { \"id\" : \"" + VIM_ID + "\" } ] }";
        when(operationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet(instantiationOperationExecution.getId(), NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(new JsonParser().parse(instantiationParams)));
        List<RuntimeException> expectedExceptions = new ArrayList<>();
        when(vnfApi.vnfsVnfInstanceIdOperationExecutionsGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenAnswer((Answer<Observable<List<OperationExecution>>>) invocation -> {
            if (expectedExceptions.size() >= 100) {
                when(operationExecutionApi.operationExecutionsOperationExecutionIdGet(scaleOperationExecution.getId(), NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(scaleOperationExecution));
                scaleOperationExecution.setStatus(OperationStatus.FINISHED);
                return buildObservable(operationExecutions);
            }
            RuntimeException RuntimeException = new RuntimeException();
            expectedExceptions.add(RuntimeException);
            throw RuntimeException;
        });

        //when
        JobInfo job = lifecycleManager.scaleVnf(VNFM_ID, VNF_ID, scaleRequest, restResponse);
        //verify
        waitForJobToFinishInJobManager(finished);
        assertEquals(100, expectedExceptions.size());
        for (RuntimeException expectedException : expectedExceptions) {
            verify(logger).warn("Unable to retrieve operations details", expectedException);
        }
        verify(systemFunctions, times(100)).sleep(5000);
    }

    /**
     * test scale failure propagation
     */
    @Test
    public void testScaleFailurePropagation() throws Exception {
        RuntimeException expectedException = new RuntimeException();
        VnfScaleRequest scaleRequest = new VnfScaleRequest();
        scaleRequest.setNumberOfSteps("2");
        scaleRequest.setAspectId("myAspect");
        scaleRequest.setType(ScaleDirection.IN);
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenThrow(expectedException);
        //when
        lifecycleManager.scaleVnf(VNFM_ID, VNF_ID, scaleRequest, restResponse);
        //verify
        waitForJobToFinishInJobManager(finished);
        verify(logger).error("Unable to scale VNF with myVnfId identifier", expectedException);
    }

    /**
     * test heal basic scenario
     */
    @Test
    public void testHeal() throws Exception {
        VnfHealRequest healRequest = new VnfHealRequest();
        healRequest.setAction("myAction");
        VnfHealRequestAffectedvm affectedVm = new VnfHealRequestAffectedvm();
        affectedVm.setVmname("vmName");
        healRequest.setAffectedvm(affectedVm);
        healOperationExecution.setStatus(OperationStatus.FINISHED);
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(vnfInfo));
        VnfProperty prop = new VnfProperty();
        prop.setValue(ONAP_CSAR_ID);
        prop.setName(LifecycleManager.ONAP_CSAR_ID);
        vnfInfo.getExtensions().add(prop);
        vnfInfo.getOperationExecutions().add(instantiationOperationExecution);
        String instantiationParams = "{ \"vims\" : [ { \"id\" : \"" + VIM_ID + "\" } ] }";
        when(operationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet(instantiationOperationExecution.getId(), NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(new JsonParser().parse(instantiationParams)));
        //when
        JobInfo job = lifecycleManager.healVnf(VNFM_ID, VNF_ID, healRequest, empty(), restResponse);
        //verify
        waitForJobToFinishInJobManager(finished);
        assertEquals(1, actualHealRequest.getAllValues().size());
        HealVnfRequest sRequest = actualHealRequest.getValue();
        InOrder workflowOrder = Mockito.inOrder(vfcGrantManager, vnfApi);
        workflowOrder.verify(vfcGrantManager).requestGrantForHeal(eq(VNFM_ID), eq(VNF_ID), eq(VIM_ID), eq(ONAP_CSAR_ID), eq(healRequest), eq(JOB_ID));
        workflowOrder.verify(vnfApi).vnfsVnfInstanceIdHealPost(VNF_ID, sRequest, NOKIA_LCM_API_VERSION);
        JsonObject root = new Gson().toJsonTree(sRequest.getAdditionalParams()).getAsJsonObject();
        assertEquals("myAction", root.get("action").getAsString());
        assertEquals("vmName", root.get("vmName").getAsString());
        assertEquals(JOB_ID, root.get("jobId").getAsString());
        verify(jobManager).spawnJob(VNF_ID, restResponse);
        verify(logger).info(eq("Starting {} operation on VNF with {} identifier with {} parameter"), eq("heal"), eq(VNF_ID), anyString());
    }

    /**
     * test heal operation is out waited
     */
    @Test
    public void testHealOutwait() throws Exception {
        VnfHealRequest healRequest = new VnfHealRequest();
        healRequest.setAction("myAction");
        VnfHealRequestAffectedvm affectedVm = new VnfHealRequestAffectedvm();
        affectedVm.setVmname("vmName");
        healRequest.setAffectedvm(affectedVm);
        healOperationExecution.setStatus(OperationStatus.FINISHED);
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(vnfInfo));
        VnfProperty prop = new VnfProperty();
        prop.setValue(ONAP_CSAR_ID);
        prop.setName(LifecycleManager.ONAP_CSAR_ID);
        vnfInfo.getExtensions().add(prop);
        vnfInfo.getOperationExecutions().add(instantiationOperationExecution);
        String instantiationParams = "{ \"vims\" : [ { \"id\" : \"" + VIM_ID + "\" } ] }";
        when(operationExecutionApi.operationExecutionsOperationExecutionIdOperationParamsGet(instantiationOperationExecution.getId(), NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(new JsonParser().parse(instantiationParams)));
        List<RuntimeException> expectedExceptions = new ArrayList<>();
        when(vnfApi.vnfsVnfInstanceIdOperationExecutionsGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenAnswer(new Answer<Observable<List<OperationExecution>>>() {
            @Override
            public Observable<List<OperationExecution>> answer(InvocationOnMock invocation) throws Throwable {
                if (expectedExceptions.size() >= 100) {
                    scaleOperationExecution.setStatus(OperationStatus.FINISHED);
                    return buildObservable(operationExecutions);
                }
                RuntimeException RuntimeException = new RuntimeException();
                expectedExceptions.add(RuntimeException);
                throw RuntimeException;
            }
        });
        //when
        JobInfo job = lifecycleManager.healVnf(VNFM_ID, VNF_ID, healRequest, empty(), restResponse);
        //verify
        waitForJobToFinishInJobManager(finished);
        assertEquals(100, expectedExceptions.size());
        for (RuntimeException expectedException : expectedExceptions) {
            verify(logger).warn("Unable to retrieve operations details", expectedException);
        }
        verify(systemFunctions, times(100)).sleep(5000);
    }

    /**
     * failure in heal propagates in error
     */
    @Test
    public void testHealFailurePropagation() throws Exception {
        RuntimeException expectedException = new RuntimeException();
        VnfHealRequest healRequest = new VnfHealRequest();
        healRequest.setAction("myAction");
        VnfHealRequestAffectedvm affectedVm = new VnfHealRequestAffectedvm();
        affectedVm.setVmname("vmName");
        healRequest.setAffectedvm(affectedVm);
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenThrow(expectedException);
        //when
        JobInfo job = lifecycleManager.healVnf(VNFM_ID, VNF_ID, healRequest, empty(), restResponse);
        //verify
        waitForJobToFinishInJobManager(finished);
        verify(logger).error("Unable to heal VNF with myVnfId identifier", expectedException);
    }


    /**
     * test custom operation basic scenario
     */
    @Test
    public void testCustomOperation() throws Exception {
        String operationId = "operationIdCaptor";
        Object additionalParams = new JsonObject();
        //when
        JobInfo job = lifecycleManager.customOperation(VNFM_ID, VNF_ID, operationId, additionalParams, restResponse);
        //verify
        waitForJobToFinishInJobManager(finished);
        assertEquals(operationId, operationIdCaptor.getValue());
        assertEquals(additionalParams, customOperationRequestArgumentCaptor.getValue().getAdditionalParams());
    }

    private void waitForJobToFinishInJobManager(Set<Boolean> finished) throws InterruptedException {
        while (finished.size() == 0) {
            systemFunctions().sleep(100);
        }
    }

    private VnfInstantiateRequest prepareInstantiationRequest(VimInfo.VimInfoTypeEnum cloudType) {
        VnfInstantiateRequest instantiationRequest = new VnfInstantiateRequest();
        instantiationRequest.setVnfPackageId(ONAP_CSAR_ID);
        instantiationRequest.setVnfDescriptorId(ONAP_CSAR_ID);
        instantiationRequest.setVnfInstanceDescription("myDescription");
        instantiationRequest.setVnfInstanceName("vnfName");
        additionalParam.setInstantiationLevel("level1");
        switch (cloudType) {
            case OPENSTACK_V2_INFO:
                additionalParam.setVimType(VimInfo.VimInfoTypeEnum.OPENSTACK_V2_INFO);
                break;
            case OPENSTACK_V3_INFO:
                additionalParam.setVimType(VimInfo.VimInfoTypeEnum.OPENSTACK_V3_INFO);
                vimInfo.setDomain("myDomain");
                break;
            case VMWARE_VCLOUD_INFO:
                additionalParam.setVimType(VimInfo.VimInfoTypeEnum.VMWARE_VCLOUD_INFO);
                break;
            default:
                additionalParam.setVimType(VimInfo.VimInfoTypeEnum.OTHER_VIM_INFO);
        }

        Map<String, List<NetworkAddress>> exteranalConnectionPointAddresses = new HashMap<>();
        exteranalConnectionPointAddresses.put("ecp1", new ArrayList<>());
        NetworkAddress networkAddress = new NetworkAddress();
        networkAddress.setIp("1.2.3.4");
        networkAddress.setMac("mac");
        networkAddress.setSubnetId("subnetId");
        exteranalConnectionPointAddresses.get("ecp1").add(networkAddress);
        additionalParam.setExternalConnectionPointAddresses(exteranalConnectionPointAddresses);
        VimComputeResourceFlavour flavor = new VimComputeResourceFlavour();
        flavor.setResourceId("flavourProviderId");
        flavor.setVimId(VIM_ID);
        flavor.setVnfdVirtualComputeDescId("virtualComputeDescId");
        additionalParam.getComputeResourceFlavours().add(flavor);
        ExtVirtualLinkData evl = new ExtVirtualLinkData();
        evl.setResourceId("networkProviderId1");
        evl.setVimId(VIM_ID);
        evl.setExtVirtualLinkId("evlId1");
        VnfExtCpData ecp2 = new VnfExtCpData();
        ecp2.setCpdId("cpdId3");
        ecp2.setAddresses(new ArrayList<>());
        ecp2.getAddresses().add(networkAddress);
        ecp2.setNumDynamicAddresses(2);
        evl.getExtCps().add(ecp2);
        additionalParam.getExtVirtualLinks().add(evl);
        externalVirtualLink.setCpdId("myCpdId");
        externalVirtualLink.setResourceId("myNetworkProviderId");
        externalVirtualLink.setVlInstanceId("myEVlId");
        externalVirtualLink.setResourceSubnetId("notUsedSubnetId");
        instantiationRequest.setExtVirtualLink(new ArrayList<>());
        instantiationRequest.getExtVirtualLink().add(externalVirtualLink);
        additionalParam.getExtManagedVirtualLinks().add(extManVl);
        ZoneInfo zone = new ZoneInfo();
        zone.setId("zoneId");
        zone.setResourceId("zoneProviderId");
        zone.setVimId(VIM_ID);
        additionalParam.getZones().add(zone);
        VimSoftwareImage image = new VimSoftwareImage();
        image.setResourceId("imageProviderId");
        image.setVimId(VIM_ID);
        image.setVnfdSoftwareImageId("imageId");
        additionalParam.getSoftwareImages().add(image);
        additionalParam.setAdditionalParams(new JsonParser().parse("{ \"a\" : \"b\" }"));
        String params = new Gson().toJson(additionalParam);
        String src = "{ \"inputs\" : { \"vnfs\" : { \"" + ONAP_CSAR_ID + "\" : " + params + "}}, \"vimId\" : \"" + VIM_ID + "\"}";
        instantiationRequest.setAdditionalParam(new JsonParser().parse(src));
        return instantiationRequest;
    }

    /**
     * Test vimId decomposition
     */
    @Test
    public void testVimIdSplitting() {
        assertEquals("regionId", LifecycleManager.getRegionName("cloudOwner_regionId"));
        assertEquals("cloudOwner", LifecycleManager.getCloudOwner("cloudOwner_regionId"));
    }
}
