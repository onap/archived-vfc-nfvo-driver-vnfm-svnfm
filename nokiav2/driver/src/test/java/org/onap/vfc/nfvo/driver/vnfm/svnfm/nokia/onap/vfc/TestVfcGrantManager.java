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

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nokia.cbam.lcm.v32.model.*;
import com.nokia.cbam.lcm.v32.model.VnfInfo;
import io.reactivex.Observable;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CatalogManager;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.LifecycleManager;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;
import org.onap.vnfmdriver.model.*;
import org.onap.vnfmdriver.model.ScaleDirection;

import static java.nio.file.Files.readAllBytes;

import static junit.framework.TestCase.*;
import static org.mockito.Mockito.*;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProvider.NOKIA_LCM_API_VERSION;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class TestVfcGrantManager extends TestBase {

    private ArgumentCaptor<GrantVNFRequest> grantRequest = ArgumentCaptor.forClass(GrantVNFRequest.class);
    private GrantVNFResponseVim vim = new GrantVNFResponseVim();
    private GrantVNFResponse grantResponse = new GrantVNFResponse();
    @Mock
    private CatalogManager cbamCatalogManager;
    @InjectMocks
    private VfcGrantManager vfcGrantManager;

    @Before
    public void initMocks() throws Exception {
        setField(VfcGrantManager.class, "logger", logger);
        when(nsLcmApi.grantvnf(grantRequest.capture())).thenReturn(buildObservable(grantResponse));
        grantResponse.setVim(vim);
    }

    /**
     * test grant request for instantiation
     */
    @Test
    public void testGrantDuringInstantiation() throws Exception {
        String cbamVnfdContent = new String(readAllBytes(Paths.get(TestVfcGrantManager.class.getResource("/unittests/vnfd.instantiation.yaml").toURI())));
        //when
        vfcGrantManager.requestGrantForInstantiate(VNFM_ID, VNF_ID, VIM_ID, ONAP_CSAR_ID, "level1", cbamVnfdContent, JOB_ID);
        //verify
        assertEquals(1, grantRequest.getAllValues().size());
        GrantVNFRequest request = grantRequest.getValue();
        assertVduInGrant(request.getAddResource(), "vdu1", 1);
        assertVduInGrant(request.getAddResource(), "vdu2", 2);
        assertEquals(0, request.getRemoveResource().size());
        assertBasicGrantAttributes(request, org.onap.vnfmdriver.model.OperationType.INSTANTIATE);
    }

    /**
     * test failure logging & propagation during grant request for instantiation
     */
    @Test
    public void testFailureDuringGrantPreparation() throws Exception {
        String cbamVnfdContent = new String(readAllBytes(Paths.get(TestVfcGrantManager.class.getResource("/unittests/vnfd.instantiation.yaml").toURI())));
        //when
        try {
            vfcGrantManager.requestGrantForInstantiate(VNFM_ID, VNF_ID, VIM_ID, ONAP_CSAR_ID, "missingLevel", cbamVnfdContent, JOB_ID);
            //verify
            fail();
        } catch (RuntimeException e) {
            verify(logger).error(Mockito.eq("Unable to prepare grant request for instantiation"), Mockito.any(RuntimeException.class));
            verifyNoMoreInteractions(nsLcmApi);
        }
    }

    /**
     * test grant request for instantiation
     */
    @Test
    public void testFailureDuringGrantRequest() throws Exception {
        String cbamVnfdContent = new String(readAllBytes(Paths.get(TestVfcGrantManager.class.getResource("/unittests/vnfd.instantiation.yaml").toURI())));
        RuntimeException expectedException = new RuntimeException("a");
        when(nsLcmApi.grantvnf(Mockito.any())).thenThrow(expectedException);
        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.doNothing().when(logger).error(logCaptor.capture(), Mockito.eq(expectedException));
        //when
        try {
            vfcGrantManager.requestGrantForInstantiate(VNFM_ID, VNF_ID, VIM_ID, ONAP_CSAR_ID, "level1", cbamVnfdContent, JOB_ID);
            //verify
            fail();
        } catch (RuntimeException e) {
            assertTrue(logCaptor.getValue().contains("Unable to request grant with "));
        }
    }

    /**
     * No grant is requested for termination if the the VNF is not instantiated
     */
    @Test
    public void testNoGrantIsRequestedIfNotInstantiated() {
        VnfInfo vnf = new VnfInfo();
        vnf.setId(VNF_ID);
        vnf.setInstantiationState(InstantiationState.NOT_INSTANTIATED);
        //when
        vfcGrantManager.requestGrantForTerminate(VNFM_ID, VNF_ID, VIM_ID, ONAP_CSAR_ID, vnf, JOB_ID);
        //verify
        verifyNoMoreInteractions(nsLcmApi);
    }

    /**
     * grant is requested for termination if the the VNF is instantiated
     */
    @Test
    public void testGrantIsRequestedIfInstantiated() {
        VnfInfo vnf = new VnfInfo();
        vnf.setId(VNF_ID);
        vnf.setInstantiationState(InstantiationState.INSTANTIATED);
        InstantiatedVnfInfo instantiatedVnfInfo = new InstantiatedVnfInfo();
        VnfcResourceInfo vnfc = new VnfcResourceInfo();
        vnfc.setId("vnfcId1");
        vnfc.setVduId("vdu1");
        instantiatedVnfInfo.setVnfcResourceInfo(new ArrayList<>());
        instantiatedVnfInfo.getVnfcResourceInfo().add(vnfc);
        vnf.setInstantiatedVnfInfo(instantiatedVnfInfo);
        VnfProperty prop = new VnfProperty();
        prop.setName(LifecycleManager.ONAP_CSAR_ID);
        prop.setValue(ONAP_CSAR_ID);
        vnf.setVnfConfigurableProperties(new ArrayList<>());
        vnf.getVnfConfigurableProperties().add(prop);
        //when
        vfcGrantManager.requestGrantForTerminate(VNFM_ID, VNF_ID, VIM_ID, ONAP_CSAR_ID, vnf, JOB_ID);
        //verify
        assertEquals(1, grantRequest.getAllValues().size());
        GrantVNFRequest request = grantRequest.getValue();
        assertVduInGrant(request.getRemoveResource(), "vdu1", 1);
        assertVduInGrant(request.getRemoveResource(), "vdu2", 0);
        assertEquals(0, request.getAddResource().size());
        assertBasicGrantAttributes(request, org.onap.vnfmdriver.model.OperationType.TERMINAL);
    }

    /**
     * grant is requested for termination if the the VNF is instantiated even if has no VNFCs
     */
    @Test
    public void testGrantIsRequestedIfInstantiatedWithNoVnfcs() {
        VnfInfo vnf = new VnfInfo();
        vnf.setId(VNF_ID);
        vnf.setInstantiationState(InstantiationState.INSTANTIATED);
        InstantiatedVnfInfo instantiatedVnfInfo = new InstantiatedVnfInfo();
        vnf.setInstantiatedVnfInfo(instantiatedVnfInfo);
        VnfProperty prop = new VnfProperty();
        prop.setName(LifecycleManager.ONAP_CSAR_ID);
        prop.setValue(ONAP_CSAR_ID);
        vnf.setVnfConfigurableProperties(new ArrayList<>());
        vnf.getVnfConfigurableProperties().add(prop);
        //when
        vfcGrantManager.requestGrantForTerminate(VNFM_ID, VNF_ID, VIM_ID, ONAP_CSAR_ID, vnf, JOB_ID);
        //verify
        assertEquals(1, grantRequest.getAllValues().size());
        GrantVNFRequest request = grantRequest.getValue();
        assertVduInGrant(request.getRemoveResource(), "vdu1", 0);
        assertVduInGrant(request.getRemoveResource(), "vdu2", 0);
        assertEquals(0, request.getAddResource().size());
        assertBasicGrantAttributes(request, org.onap.vnfmdriver.model.OperationType.TERMINAL);
    }

    /**
     * test failure logging & propagation during grant request for instantiation
     */
    @Test
    public void testFailureDuringTerminationGrantPreparation() throws Exception {
        VnfInfo vnf = new VnfInfo();
        vnf.setId(VNF_ID);
        vnf.setInstantiatedVnfInfo(null);
        vnf.setInstantiationState(InstantiationState.INSTANTIATED);
        //when
        try {
            vfcGrantManager.requestGrantForTerminate(VNFM_ID, VNF_ID, VIM_ID, ONAP_CSAR_ID, vnf, JOB_ID);
            //verify
            fail();
        } catch (RuntimeException e) {
            verify(logger).error(Mockito.eq("Unable to prepare grant request for termination"), Mockito.any(RuntimeException.class));
            verifyNoMoreInteractions(nsLcmApi);
        }
    }

    /**
     * failure is to request grant is logged
     */
    @Test
    public void testFailureToRequestGrantIsLogged() throws Exception {
        VnfInfo vnf = new VnfInfo();
        vnf.setId(VNF_ID);
        vnf.setInstantiationState(InstantiationState.INSTANTIATED);
        InstantiatedVnfInfo instantiatedVnfInfo = new InstantiatedVnfInfo();
        VnfcResourceInfo vnfc = new VnfcResourceInfo();
        vnfc.setId("vnfcId1");
        vnfc.setVduId("vdu1");
        instantiatedVnfInfo.setVnfcResourceInfo(new ArrayList<>());
        instantiatedVnfInfo.getVnfcResourceInfo().add(vnfc);
        vnf.setInstantiatedVnfInfo(instantiatedVnfInfo);
        VnfProperty prop = new VnfProperty();
        prop.setName(LifecycleManager.ONAP_CSAR_ID);
        prop.setValue(ONAP_CSAR_ID);
        vnf.setVnfConfigurableProperties(new ArrayList<>());
        vnf.getVnfConfigurableProperties().add(prop);
        RuntimeException expectedException = new RuntimeException();
        when(nsLcmApi.grantvnf(Mockito.any())).thenThrow(expectedException);
        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.doNothing().when(logger).error(logCaptor.capture(), Mockito.eq(expectedException));
        //when
        try {
            vfcGrantManager.requestGrantForTerminate(VNFM_ID, VNF_ID, VIM_ID, ONAP_CSAR_ID, vnf, JOB_ID);
            //verify
            fail();
        } catch (RuntimeException e) {
            String value = logCaptor.getValue();
            assertTrue(value.contains("Unable to request grant with "));
        }
    }

    /**
     * failure is to request grant is logged
     */
    @Test
    public void testFailureToRequestGrantForScaleIsLogged() throws Exception {
        String cbamVnfdContent = new String(readAllBytes(Paths.get(TestVfcGrantManager.class.getResource("/unittests/vnfd.scale.yaml").toURI())));
        VnfScaleRequest scaleRequest = new VnfScaleRequest();
        scaleRequest.setType(ScaleDirection.OUT);
        scaleRequest.setAspectId("aspect1");
        scaleRequest.setNumberOfSteps("2");
        RuntimeException expectedException = new RuntimeException();
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenThrow(expectedException);
        //when
        try {
            vfcGrantManager.requestGrantForScale(VNFM_ID, VNF_ID, VIM_ID, ONAP_CSAR_ID, scaleRequest, JOB_ID);
            //verify
            fail();
        } catch (RuntimeException e) {
            verify(logger).error(Mockito.eq("Unable to query VNF myVnfId"), Mockito.eq(expectedException));
            assertEquals(e.getCause(), expectedException);
        }
    }

    /**
     * test grant request for scale out
     */
    @Test
    public void testGrantDuringScaleOut() throws Exception {
        String cbamVnfdContent = new String(readAllBytes(Paths.get(TestVfcGrantManager.class.getResource("/unittests/vnfd.scale.yaml").toURI())));
        VnfScaleRequest scaleRequest = new VnfScaleRequest();
        scaleRequest.setType(ScaleDirection.OUT);
        scaleRequest.setAspectId("aspect1");
        scaleRequest.setNumberOfSteps("2");
        VnfInfo vnf = new VnfInfo();
        Observable<VnfInfo> vnfInfoObservable = buildObservable(vnf);
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(vnfInfoObservable);
        vnf.setVnfdId(CBAM_VNFD_ID);
        when(cbamCatalogManager.getCbamVnfdContent(VNFM_ID, CBAM_VNFD_ID)).thenReturn(cbamVnfdContent);
        //when
        vfcGrantManager.requestGrantForScale(VNFM_ID, VNF_ID, VIM_ID, ONAP_CSAR_ID, scaleRequest, JOB_ID);
        //verify
        assertEquals(1, grantRequest.getAllValues().size());
        GrantVNFRequest request = grantRequest.getValue();
        assertVduInGrant(request.getAddResource(), "vdu1", 4);
        assertVduInGrant(request.getAddResource(), "vdu2", 2);
        assertEquals(0, request.getRemoveResource().size());
        assertBasicGrantAttributes(request, org.onap.vnfmdriver.model.OperationType.SCALEOUT);
    }

    /**
     * test grant request for scale out without VDUs
     */
    @Test
    public void testGrantDuringScaleOutWithoutVdus() throws Exception {
        String cbamVnfdContent = new String(readAllBytes(Paths.get(TestVfcGrantManager.class.getResource("/unittests/vnfd.scale.yaml").toURI())));
        VnfScaleRequest scaleRequest = new VnfScaleRequest();
        scaleRequest.setType(ScaleDirection.OUT);
        scaleRequest.setAspectId("aspectWithOutVdu");
        scaleRequest.setNumberOfSteps("2");
        VnfInfo vnf = new VnfInfo();
        Observable<VnfInfo> vnfInfoObservable = buildObservable(vnf);
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(vnfInfoObservable);
        vnf.setVnfdId(CBAM_VNFD_ID);
        when(cbamCatalogManager.getCbamVnfdContent(VNFM_ID, CBAM_VNFD_ID)).thenReturn(cbamVnfdContent);
        //when
        vfcGrantManager.requestGrantForScale(VNFM_ID, VNF_ID, VIM_ID, ONAP_CSAR_ID, scaleRequest, JOB_ID);
        //verify
        assertEquals(1, grantRequest.getAllValues().size());
        GrantVNFRequest request = grantRequest.getValue();
        assertVduInGrant(request.getAddResource(), "vdu1", 0);
        assertVduInGrant(request.getAddResource(), "vdu2", 0);
        assertEquals(0, request.getRemoveResource().size());
        assertBasicGrantAttributes(request, org.onap.vnfmdriver.model.OperationType.SCALEOUT);
    }

    /**
     * test grant request for scale out without resources
     */
    @Test
    public void testGrantDuringScaleOutForEmptyAspect() throws Exception {
        String cbamVnfdContent = new String(readAllBytes(Paths.get(TestVfcGrantManager.class.getResource("/unittests/vnfd.scale.yaml").toURI())));
        VnfScaleRequest scaleRequest = new VnfScaleRequest();
        scaleRequest.setType(ScaleDirection.OUT);
        scaleRequest.setAspectId("emptyAspect");
        scaleRequest.setNumberOfSteps("2");
        VnfInfo vnf = new VnfInfo();
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(vnf));
        vnf.setVnfdId(CBAM_VNFD_ID);
        when(cbamCatalogManager.getCbamVnfdContent(VNFM_ID, CBAM_VNFD_ID)).thenReturn(cbamVnfdContent);
        //when
        try {
            vfcGrantManager.requestGrantForScale(VNFM_ID, VNF_ID, VIM_ID, ONAP_CSAR_ID, scaleRequest, JOB_ID);
            fail();
        } catch (Exception e) {
            assertEquals("Missing child emptyAspect", e.getMessage());
        }
    }

    /**
     * test grant request for scale in
     */
    @Test
    public void testGrantDuringScaleIn() throws Exception {
        String cbamVnfdContent = new String(readAllBytes(Paths.get(TestVfcGrantManager.class.getResource("/unittests/vnfd.scale.yaml").toURI())));
        VnfScaleRequest scaleRequest = new VnfScaleRequest();
        scaleRequest.setType(ScaleDirection.IN);
        scaleRequest.setAspectId("aspect1");
        scaleRequest.setNumberOfSteps("2");
        VnfInfo vnf = new VnfInfo();
        when(vnfApi.vnfsVnfInstanceIdGet(VNF_ID, NOKIA_LCM_API_VERSION)).thenReturn(buildObservable(vnf));
        vnf.setVnfdId(CBAM_VNFD_ID);
        when(cbamCatalogManager.getCbamVnfdContent(VNFM_ID, CBAM_VNFD_ID)).thenReturn(cbamVnfdContent);
        //when
        vfcGrantManager.requestGrantForScale(VNFM_ID, VNF_ID, VIM_ID, ONAP_CSAR_ID, scaleRequest, JOB_ID);
        //verify
        assertEquals(1, grantRequest.getAllValues().size());
        GrantVNFRequest request = grantRequest.getValue();
        assertVduInGrant(request.getRemoveResource(), "vdu1", 4);
        assertVduInGrant(request.getRemoveResource(), "vdu2", 2);
        assertEquals(0, request.getAddResource().size());
        assertBasicGrantAttributes(request, org.onap.vnfmdriver.model.OperationType.SCALEIN);
    }

    /**
     * test grant request for healing
     */
    @Test
    public void testGrantDuringHealing() throws Exception {
        //when
        VnfHealRequest healRequest = new VnfHealRequest();
        VnfHealRequestAffectedvm affectedVm = new VnfHealRequestAffectedvm();
        affectedVm.setVduid("vdu1");
        healRequest.setAffectedvm(affectedVm);
        vfcGrantManager.requestGrantForHeal(VNFM_ID, VNF_ID, VIM_ID, ONAP_CSAR_ID, healRequest, JOB_ID);
        //verify
        assertEquals(1, grantRequest.getAllValues().size());
        GrantVNFRequest request = grantRequest.getValue();
        assertVduInGrant(request.getAddResource(), "vdu1", 1);
        assertVduInGrant(request.getRemoveResource(), "vdu1", 1);
        assertBasicGrantAttributes(request, org.onap.vnfmdriver.model.OperationType.HEAL);
    }

    @Test
    public void testPOJO() {
        VfcGrantManager.AdditionalGrantParams additionalGrantParams = new VfcGrantManager.AdditionalGrantParams(VNFM_ID, VIM_ID);
        assertEquals(VNFM_ID, additionalGrantParams.getVnfmId());
        assertEquals(VIM_ID, additionalGrantParams.getVimId());
    }

    private void assertBasicGrantAttributes(GrantVNFRequest request, org.onap.vnfmdriver.model.OperationType type) {
        assertEquals(JOB_ID, request.getJobId());
        assertEquals(type, request.getLifecycleOperation());
        assertEquals(ONAP_CSAR_ID, request.getVnfDescriptorId());
        assertEquals(VNF_ID, request.getVnfInstanceId());
        JsonObject additionalParams = new Gson().toJsonTree(request.getAdditionalParam()).getAsJsonObject();
        assertEquals(VIM_ID, additionalParams.get("vimId").getAsString());
        assertEquals(VNFM_ID, additionalParams.get("vnfmId").getAsString());
    }

    private void assertVduInGrant(List<ResourceChange> changes, String vduName, int count) {
        ArrayList<ResourceChange> clonedChanges = Lists.newArrayList(changes);
        for (int i = 0; i < count + 1; i++) {
            Iterator<ResourceChange> iter = clonedChanges.iterator();
            boolean found = false;
            while (iter.hasNext()) {
                ResourceChange change = iter.next();
                if (change.getVdu().equals(vduName)) {
                    iter.remove();
                    found = true;
                    break;
                }
            }
            if (i >= count) {
                assertFalse(found);
            } else {
                assertTrue(found);
            }
        }
    }

}
