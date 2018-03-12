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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.notification;

import com.nokia.cbam.lcm.v32.ApiException;
import com.nokia.cbam.lcm.v32.model.VnfInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.onap.aai.domain.yang.v11.*;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIRestApiProvider.AAIService.NETWORK;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class TestGenericVnfManager extends TestBase {
    private ObjectFactory OBJECT_FACTORY = new ObjectFactory();
    private ArgumentCaptor<GenericVnf> payload = ArgumentCaptor.forClass(GenericVnf.class);

    @Mock
    private AAIRestApiProvider aaiRestApiProvider;
    private GenericVnfManager genericVnfManager;
    private VnfInfo vnfInfo = new VnfInfo();

    static void assertRelation(RelationshipList relationShips, String relatedTo, RelationshipData... data) {
        for (Relationship relationship : relationShips.getRelationship()) {
            if (relationship.getRelatedTo().equals(relatedTo)) {
                assertEquals(data.length, relationship.getRelationshipData().size());
                int i = 0;
                for (RelationshipData c : data) {
                    assertEquals(c.getRelationshipKey(), relationship.getRelationshipData().get(i).getRelationshipKey());
                    assertEquals(c.getRelationshipValue(), relationship.getRelationshipData().get(i).getRelationshipValue());
                    i++;
                }
                return;
            }
        }
        fail();
    }

    @Before
    public void init() {
        genericVnfManager = new GenericVnfManager(aaiRestApiProvider, cbamRestApiProvider, driverProperties);
        setField(GenericVnfManager.class, "logger", logger);
        AtomicLong currentTime = new AtomicLong(0L);
        when(systemFunctions.currentTimeMillis()).thenAnswer(new Answer<Long>() {
            @Override
            public Long answer(InvocationOnMock invocation) throws Throwable {
                return currentTime.get();
            }
        });
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                currentTime.addAndGet((Long) invocation.getArguments()[0] + 1);
                return null;
            }
        }).when(systemFunctions).sleep(anyLong());
    }

    /**
     * retrieving an existing VNF
     */
    @Test
    public void testGetExistingVnf() throws Exception {
        GenericVnf aaiVnf = OBJECT_FACTORY.createGenericVnf();
        when(aaiRestApiProvider.get(logger, NETWORK, "/generic-vnfs/generic-vnf/" + VNF_ID, GenericVnf.class)).thenReturn(aaiVnf);
        //when
        GenericVnf vnf = genericVnfManager.getExistingVnf(VNF_ID);
        //verify
        assertEquals(aaiVnf, vnf);
    }

    /**
     * if the VNF does not exist it is created
     */
    @Test
    public void createNonExistingVnf() throws Exception {
        GenericVnf vnfInAaai = OBJECT_FACTORY.createGenericVnf();
        Set<GenericVnf> vnfs = new HashSet<>();
        when(aaiRestApiProvider.get(logger, NETWORK, "/generic-vnfs/generic-vnf/" + VNF_ID, GenericVnf.class)).thenAnswer((Answer<GenericVnf>) invocation -> {
            if (vnfs.size() == 0) {
                throw new NoSuchElementException();
            }
            return vnfs.iterator().next();
        });
        when(cbamRestApiProvider.getCbamLcmApi(VNFM_ID).vnfsVnfInstanceIdGet(VNF_ID, CbamRestApiProvider.NOKIA_LCM_API_VERSION)).thenReturn(vnfInfo);
        when(aaiRestApiProvider.put(eq(logger), eq(NETWORK), eq("/generic-vnfs/generic-vnf/" + VNF_ID), payload.capture(), eq(Void.class))).thenAnswer(invocation -> {
            vnfs.add(vnfInAaai);
            return null;
        });
        vnfInfo.setName("vnfName");
        //when
        genericVnfManager.createOrUpdate(VNF_ID, true);
        //verify
        GenericVnf vnfSentToAai = payload.getValue();
        assertEquals(VNF_ID, vnfSentToAai.getVnfId());
        assertEquals(VNF_ID, vnfSentToAai.getVnfInstanceId());
        assertEquals("NokiaVNF", vnfSentToAai.getVnfType());
        assertEquals(true, vnfSentToAai.isInMaint());
        assertEquals(true, vnfSentToAai.isIsClosedLoopDisabled());
        assertEquals("vnfName", vnfSentToAai.getVnfName());
        verify(systemFunctions, times(10)).sleep(3000);
        verify(aaiRestApiProvider, times(10)).get(logger, NETWORK, "/generic-vnfs/generic-vnf/" + VNF_ID, GenericVnf.class);
    }

    /**
     * if the VNF exist it is updated
     */
    @Test
    public void testUpdateExistingVnf() throws Exception {
        GenericVnf vnfInAaai = OBJECT_FACTORY.createGenericVnf();
        vnfInAaai.setResourceVersion("v1");
        when(aaiRestApiProvider.get(logger, NETWORK, "/generic-vnfs/generic-vnf/" + VNF_ID, GenericVnf.class)).thenReturn(vnfInAaai);
        when(cbamRestApiProvider.getCbamLcmApi(VNFM_ID).vnfsVnfInstanceIdGet(VNF_ID, CbamRestApiProvider.NOKIA_LCM_API_VERSION)).thenReturn(vnfInfo);
        when(aaiRestApiProvider.put(eq(logger), eq(NETWORK), eq("/generic-vnfs/generic-vnf/" + VNF_ID), payload.capture(), eq(Void.class))).thenReturn(null);
        vnfInfo.setName("vnfName");
        //when
        genericVnfManager.createOrUpdate(VNF_ID, true);
        //verify
        GenericVnf vnfSentToAai = payload.getValue();
        assertEquals(VNF_ID, vnfSentToAai.getVnfId());
        assertEquals(VNF_ID, vnfSentToAai.getVnfInstanceId());
        assertEquals("NokiaVNF", vnfSentToAai.getVnfType());
        assertEquals(true, vnfSentToAai.isInMaint());
        assertEquals(true, vnfSentToAai.isIsClosedLoopDisabled());
        assertEquals("vnfName", vnfSentToAai.getVnfName());
        verify(systemFunctions, never()).sleep(anyLong());
        verify(aaiRestApiProvider, times(1)).get(logger, NETWORK, "/generic-vnfs/generic-vnf/" + VNF_ID, GenericVnf.class);
    }

    /**
     * error is propagated if unable to query VNF from CBAM
     */
    @Test
    public void testUnableToQueryVnfFromCBAM() throws Exception {
        GenericVnf vnfInAaai = OBJECT_FACTORY.createGenericVnf();
        vnfInAaai.setResourceVersion("v1");
        when(aaiRestApiProvider.get(logger, NETWORK, "/generic-vnfs/generic-vnf/" + VNF_ID, GenericVnf.class)).thenReturn(vnfInAaai);
        ApiException expectedException = new ApiException();
        when(cbamRestApiProvider.getCbamLcmApi(VNFM_ID).vnfsVnfInstanceIdGet(VNF_ID, CbamRestApiProvider.NOKIA_LCM_API_VERSION)).thenThrow(expectedException);
        when(aaiRestApiProvider.put(eq(logger), eq(NETWORK), eq("/generic-vnfs/generic-vnf/" + VNF_ID), payload.capture(), eq(Void.class))).thenAnswer(invocation -> {
            vnfInAaai.setResourceVersion("v2");
            return null;
        });
        vnfInfo.setName("vnfName");
        //when
        try {
            genericVnfManager.createOrUpdate(VNF_ID, true);
        } catch (Exception e) {
            verify(logger).error("Unable to query VNF with myVnfId identifier from CBAM", expectedException);
            assertEquals("Unable to query VNF with myVnfId identifier from CBAM", e.getMessage());
        }
    }

    /**
     * if the VNF is created after the last attempt to query VNF, but before the
     * the driver creates the VNF it is not created but updated
     */
    @Test
    public void testConcurency1() throws Exception {
        GenericVnf vnfInAaai = OBJECT_FACTORY.createGenericVnf();
        vnfInAaai.setResourceVersion("v3");
        Set<Integer> queryCount = new HashSet<>();
        when(aaiRestApiProvider.get(logger, NETWORK, "/generic-vnfs/generic-vnf/" + VNF_ID, GenericVnf.class)).thenAnswer((Answer<GenericVnf>) invocation -> {
            queryCount.add(queryCount.size());
            if (queryCount.size() >= 11) {
                return vnfInAaai;
            }
            throw new NoSuchElementException();
        });
        when(cbamRestApiProvider.getCbamLcmApi(VNFM_ID).vnfsVnfInstanceIdGet(VNF_ID, CbamRestApiProvider.NOKIA_LCM_API_VERSION)).thenReturn(vnfInfo);
        RuntimeException runtimeException = new RuntimeException();
        when(aaiRestApiProvider.put(eq(logger), eq(NETWORK), eq("/generic-vnfs/generic-vnf/" + VNF_ID), payload.capture(), eq(Void.class))).thenAnswer(invocation -> {
            GenericVnf vnfSentToAAi = (GenericVnf) invocation.getArguments()[3];
            if (vnfSentToAAi.getResourceVersion() == null) {
                throw runtimeException;
            }
            return null;
        });
        vnfInfo.setName("vnfName");
        //when
        genericVnfManager.createOrUpdate(VNF_ID, true);
        //verify
        GenericVnf vnfSentToAai = payload.getValue();
        assertEquals(VNF_ID, vnfSentToAai.getVnfId());
        assertEquals(VNF_ID, vnfSentToAai.getVnfInstanceId());
        assertEquals("NokiaVNF", vnfSentToAai.getVnfType());
        assertEquals(true, vnfSentToAai.isInMaint());
        assertEquals(true, vnfSentToAai.isIsClosedLoopDisabled());
        assertEquals("vnfName", vnfSentToAai.getVnfName());
        assertEquals("v3", vnfSentToAai.getResourceVersion());
        verify(systemFunctions, times(10)).sleep(3000);
        verify(aaiRestApiProvider, times(11)).get(logger, NETWORK, "/generic-vnfs/generic-vnf/" + VNF_ID, GenericVnf.class);
        verify(aaiRestApiProvider, times(2)).put(eq(logger), eq(NETWORK), eq("/generic-vnfs/generic-vnf/" + VNF_ID), anyString(), eq(Void.class));
        verify(logger).warn(eq("The VNF with myVnfId identifier did not appear in time"), any(NoSuchElementException.class));
        verify(logger).warn("The VNF with myVnfId identifier has been created since after the maximal wait for VNF to appear timeout", runtimeException);
    }

    /**
     * test how entities can refer to a VNF
     */
    @Test
    public void testRelations() {
        //when
        Relationship relationship = GenericVnfManager.linkTo(VNF_ID);
        //verify
        assertEquals("generic-vnf", relationship.getRelatedTo());
        assertEquals(1, relationship.getRelationshipData().size());
        assertEquals("generic-vnf.vnf-id", relationship.getRelationshipData().get(0).getRelationshipKey());
        assertEquals(VNF_ID, relationship.getRelationshipData().get(0).getRelationshipValue());
    }

    /**
     * test inheritence
     */
    @Test
    public void testInheritence() {
        assertEquals(logger, genericVnfManager.getLogger());
    }
}
