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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core;

import com.google.common.collect.Lists;
import com.nokia.cbam.lcn.v32.model.CreateSubscriptionRequest;
import com.nokia.cbam.lcn.v32.model.Subscription;
import com.nokia.cbam.lcn.v32.model.SubscriptionAuthentication;
import com.nokia.cbam.lcn.v32.model.VnfNotificationType;
import io.reactivex.Observable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.onap.msb.model.MicroServiceFullInfo;
import org.onap.msb.model.MicroServiceInfo;
import org.onap.msb.model.Node;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.TestUtil;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.JobManager;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;

import static com.google.common.collect.Sets.newHashSet;
import static com.nokia.cbam.lcn.v32.model.OperationType.*;
import static junit.framework.TestCase.*;
import static org.mockito.Mockito.*;
import static org.onap.msb.model.MicroServiceInfo.ProtocolEnum.REST;
import static org.onap.msb.model.MicroServiceInfo.VisualRangeEnum._1;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core.SelfRegistrationManager.DRIVER_VERSION;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core.SelfRegistrationManager.SERVICE_NAME;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProvider.NOKIA_LCN_API_VERSION;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class TestSelfRegistrationManager extends TestBase {
    @Mock
    private JobManager jobManager;
    private java.util.List<Subscription> subscriptions = new ArrayList<>();
    private ArgumentCaptor<MicroServiceInfo> registeredMicroservice = ArgumentCaptor.forClass(MicroServiceInfo.class);
    private ArgumentCaptor<CreateSubscriptionRequest> subscriptionToCreate = ArgumentCaptor.forClass(CreateSubscriptionRequest.class);
    private SelfRegistrationManager selfRegistrationManager;

    @Before
    public void initMocks() throws Exception {
        selfRegistrationManager = new SelfRegistrationManager(vnfmInfoProvider, msbApiProvider, cbamRestApiProvider);

        setField(SelfRegistrationManager.class, "logger", logger);
        when(lcnApi.subscriptionsGet(NOKIA_LCN_API_VERSION)).thenReturn(buildObservable(subscriptions));
        setField(selfRegistrationManager, "driverMsbExternalIp", "1.2.3.4");
        setField(selfRegistrationManager, "driverVnfmExternalIp", "5.6.7.8");
        setField(selfRegistrationManager, "driverPort", "12345");
        Subscription unknownSubscription = new Subscription();
        unknownSubscription.setId(UUID.randomUUID().toString());
        unknownSubscription.setCallbackUrl("unknown");
        subscriptions.add(unknownSubscription);
    }

    /**
     * test the basic registration process
     * - first subscribe to CBAM LCNs
     * - second publish it's existence on MSB
     */
    @Test
    public void testRegistration() throws Exception {
        //given
        Subscription subscription = new Subscription();
        subscription.setId(SUBCRIPTION_ID);
        when(vnfmInfoProvider.getVnfms()).thenReturn(newHashSet(VNFM_ID));
        when(lcnApi.subscriptionsPost(subscriptionToCreate.capture(), Mockito.eq(NOKIA_LCN_API_VERSION))).thenReturn(buildObservable(subscription));
        MicroServiceFullInfo returnedMicroService = new MicroServiceFullInfo();
        when(msbClient.addMicroService(registeredMicroservice.capture(), eq(true), eq(false))).thenReturn(buildObservable(returnedMicroService));
        //when
        selfRegistrationManager.register();
        //verify
        InOrder registrationOrder = Mockito.inOrder(lcnApi, msbClient);
        registrationOrder.verify(lcnApi).subscriptionsPost(any(), any());
        registrationOrder.verify(msbClient).addMicroService(any(), any(), any());
        assertEquals(VNFM_ID, selfRegistrationManager.getVnfmId(SUBCRIPTION_ID));
        assertMicroserviceRegistered();
        assertNewLcnSubscription();
        assertServiceUp();
    }

    private void assertNewLcnSubscription() {
        CreateSubscriptionRequest subscriptionCreation = subscriptionToCreate.getValue();
        assertEquals("http://5.6.7.8:12345/api/NokiaSVNFM/v1/lcn", subscriptionCreation.getCallbackUrl());
        assertEquals(SubscriptionAuthentication.TypeEnum.NONE, subscriptionCreation.getAuthentication().getType());
        assertNull(subscriptionCreation.getAuthentication().getUserName());
        assertNull(subscriptionCreation.getAuthentication().getClientName());
        assertNull(subscriptionCreation.getAuthentication().getClientPassword());
        assertNull(subscriptionCreation.getAuthentication().getPassword());
        assertNull(subscriptionCreation.getAuthentication().getTokenUrl());
        assertNull(subscriptionCreation.getFilter().getVnfdId());
        assertNull(subscriptionCreation.getFilter().getVnfInstanceId());
        assertNull(subscriptionCreation.getFilter().getVnfProductName());
        assertNull(subscriptionCreation.getFilter().getVnfSoftwareVersion());
        assertEquals(Lists.newArrayList(VnfNotificationType.VNFLIFECYCLECHANGENOTIFICATION), subscriptionCreation.getFilter().getNotificationTypes());
        assertTrue(subscriptionCreation.getFilter().getOperationTypes().contains(HEAL));
        assertTrue(subscriptionCreation.getFilter().getOperationTypes().contains(SCALE));
        assertTrue(subscriptionCreation.getFilter().getOperationTypes().contains(TERMINATE));
        assertTrue(subscriptionCreation.getFilter().getOperationTypes().contains(INSTANTIATE));
        assertEquals(4, subscriptionCreation.getFilter().getOperationTypes().size());
        verify(logger).info("Subscribing to CBAM LCN {} with callback to {}", HTTP_LCN_URL, "http://5.6.7.8:12345/api/NokiaSVNFM/v1/lcn");
    }

    private void assertMicroserviceRegistered() {
        MicroServiceInfo microserviceRequest = registeredMicroservice.getValue();
        assertEquals(1, microserviceRequest.getNodes().size());
        Node node = microserviceRequest.getNodes().iterator().next();
        assertEquals("0", node.getTtl());
        assertEquals("1.2.3.4", node.getIp());
        assertEquals("12345", node.getPort());
        assertEquals(REST, microserviceRequest.getProtocol());
        assertNull(microserviceRequest.getMetadata());
        //very strange, but it should be null for ONAP to work
        assertEquals(SERVICE_NAME, microserviceRequest.getServiceName());
        assertEquals("/api/NokiaSVNFM/v1", microserviceRequest.getUrl());
        assertEquals("v1", microserviceRequest.getVersion());
        assertEquals(Boolean.FALSE, microserviceRequest.isEnableSsl());
        //1 means internal service to ONAP
        assertEquals(_1, microserviceRequest.getVisualRange());
    }

    /**
     * If the subscription already exists the subscription is not recreated
     */
    @Test
    public void testResubscription() throws Exception {
        //given
        MicroServiceFullInfo returnedMicroService = new MicroServiceFullInfo();
        when(msbClient.addMicroService(registeredMicroservice.capture(), eq(true), eq(false))).thenReturn(buildObservable(returnedMicroService));
        Subscription existingSubscription = new Subscription();
        when(vnfmInfoProvider.getVnfms()).thenReturn(newHashSet(VNFM_ID));
        existingSubscription.setId(UUID.randomUUID().toString());
        existingSubscription.setCallbackUrl("http://5.6.7.8:12345/api/NokiaSVNFM/v1/lcn");
        subscriptions.add(existingSubscription);
        //when
        selfRegistrationManager.register();
        //verify
        assertMicroserviceRegistered();
        verify(lcnApi, never()).subscriptionsPost(any(), any());
        assertServiceUp();
    }

    /**
     * If the LCN subscription fails the microservice is not registered
     */
    @Test
    public void testFailedLcnSubscription() throws Exception {
        //given
        RuntimeException expectedException = new RuntimeException();
        when(lcnApi.subscriptionsPost(any(), any())).thenThrow(expectedException);
        when(vnfmInfoProvider.getVnfms()).thenReturn(newHashSet(VNFM_ID));
        //when
        try {
            selfRegistrationManager.register();
            fail();
        } catch (RuntimeException e) {
            assertEquals(expectedException, e.getCause());
        }
        //verify
        verify(msbClient, never()).addMicroService(any(), any(), any());
        verify(logger).error("Unable to subscribe to CBAM LCN", expectedException);
        assertServiceDown();
    }

    /**
     * If the registration to MSB fails the subscription is deleted
     */
    @Test
    public void testFailedMsbPublish() throws Exception {
        //given
        Subscription subscription = new Subscription();
        subscription.setId(SUBCRIPTION_ID);
        when(vnfmInfoProvider.getVnfms()).thenReturn(newHashSet(VNFM_ID));
        when(lcnApi.subscriptionsPost(subscriptionToCreate.capture(), Mockito.eq(NOKIA_LCN_API_VERSION))).thenAnswer(invocationOnMock -> {
            subscription.setCallbackUrl("http://5.6.7.8:12345/api/NokiaSVNFM/v1/lcn");
            subscription.setId(UUID.randomUUID().toString());
            subscriptions.add(subscription);
            when(lcnApi.subscriptionsSubscriptionIdDelete(subscription.getId(), NOKIA_LCN_API_VERSION)).thenReturn(VOID_OBSERVABLE.value());
            return buildObservable(subscription);
        });
        RuntimeException expectedException = new RuntimeException();
        when(msbClient.addMicroService(registeredMicroservice.capture(), eq(true), eq(false))).thenThrow(expectedException);
        //when
        try {
            selfRegistrationManager.register();
            //verify
            fail();
        } catch (RuntimeException e) {
            assertEquals(expectedException, e.getCause());
        }
        assertNewLcnSubscription();
        verify(lcnApi).subscriptionsSubscriptionIdDelete(subscription.getId(), NOKIA_LCN_API_VERSION);
        assertServiceDown();
    }

    /**
     * basic service unregistration
     * - ongoing jobs are outwaited
     * - first the service is removed from MSB
     * - second unregistration
     */
    @Test
    public void testUnregistration() throws Exception {
        //given
        Subscription subscription = new Subscription();
        subscription.setCallbackUrl("http://5.6.7.8:12345/api/NokiaSVNFM/v1/lcn");
        subscription.setId(SUBCRIPTION_ID);
        when(vnfmInfoProvider.getVnfms()).thenReturn(newHashSet(VNFM_ID));
        when(lcnApi.subscriptionsPost(subscriptionToCreate.capture(), Mockito.eq(NOKIA_LCN_API_VERSION))).thenAnswer(invocationOnMock -> {
            subscription.setCallbackUrl("http://5.6.7.8:12345/api/NokiaSVNFM/v1/lcn");
            subscription.setId(UUID.randomUUID().toString());
            subscriptions.add(subscription);
            when(lcnApi.subscriptionsSubscriptionIdDelete(subscription.getId(), NOKIA_LCN_API_VERSION)).thenReturn(VOID_OBSERVABLE.value());
            return buildObservable(subscription);
        });
        selfRegistrationManager.assureSubscription(VNFM_ID);
        selfRegistrationManager.assureSubscription(VNFM_ID);
        when(jobManager.hasOngoingJobs()).thenReturn(false);
        when(lcnApi.subscriptionsSubscriptionIdDelete(subscription.getId(), NOKIA_LCN_API_VERSION)).thenReturn(VOID_OBSERVABLE.value());
        VoidObservable MSB_DELETE = new VoidObservable();
        when(msbClient.deleteMicroService(SERVICE_NAME, DRIVER_VERSION, null, null)).thenReturn(MSB_DELETE.value());
        //when
        selfRegistrationManager.deRegister();
        //verify
        InOrder inOrder = Mockito.inOrder(jobManager, msbClient, lcnApi);
        inOrder.verify(msbClient).deleteMicroService(SERVICE_NAME, DRIVER_VERSION, null, null);
        inOrder.verify(lcnApi).subscriptionsSubscriptionIdDelete(subscription.getId(), NOKIA_LCN_API_VERSION);
        assertServiceDown();
        VOID_OBSERVABLE.assertCalled();
        MSB_DELETE.assertCalled();
        verify(logger).info("Deleting subscription with {} identifier", subscription.getId());
    }

    /**
     * if the MSB reports that it could not cancel the service, but the service has
     * disappeared from MSB the cancellation is considered to be successful
     */
    @Test
    public void testPartiallyFailedMsbCancel() throws Exception {
        //given
        Subscription subscription = new Subscription();
        subscription.setCallbackUrl("http://5.6.7.8:12345/api/NokiaSVNFM/v1/lcn");
        subscription.setId(SUBCRIPTION_ID);
        when(vnfmInfoProvider.getVnfms()).thenReturn(newHashSet(VNFM_ID));
        when(lcnApi.subscriptionsPost(subscriptionToCreate.capture(), Mockito.eq(NOKIA_LCN_API_VERSION))).thenAnswer(invocationOnMock -> {
            subscription.setCallbackUrl("http://5.6.7.8:12345/api/NokiaSVNFM/v1/lcn");
            subscription.setId(UUID.randomUUID().toString());
            subscriptions.add(subscription);
            when(lcnApi.subscriptionsSubscriptionIdDelete(subscription.getId(), NOKIA_LCN_API_VERSION)).thenReturn(VOID_OBSERVABLE.value());
            return buildObservable(subscription);
        });
        selfRegistrationManager.assureSubscription(VNFM_ID);
        when(jobManager.hasOngoingJobs()).thenReturn(false);
        when(msbClient.deleteMicroService(SERVICE_NAME, DRIVER_VERSION, null, null)).then(new Answer<Observable<Void>>() {
            @Override
            public Observable<Void> answer(InvocationOnMock invocationOnMock) throws Throwable {
                when(msbClient.getMicroService_0(SERVICE_NAME, DRIVER_VERSION, null, null, null, null, null)).thenThrow(new RuntimeException());
                throw new RuntimeException();
            }
        });
        when(lcnApi.subscriptionsSubscriptionIdDelete(subscription.getId(), NOKIA_LCN_API_VERSION)).thenReturn(VOID_OBSERVABLE.value());
        //when
        selfRegistrationManager.deRegister();
        //verify
        InOrder inOrder = Mockito.inOrder(jobManager, msbClient, lcnApi);
        inOrder.verify(msbClient).deleteMicroService(SERVICE_NAME, DRIVER_VERSION, null, null);
        inOrder.verify(msbClient).getMicroService_0(SERVICE_NAME, DRIVER_VERSION, null, null, null, null, null);
        inOrder.verify(lcnApi).subscriptionsSubscriptionIdDelete(subscription.getId(), NOKIA_LCN_API_VERSION);
        assertServiceDown();
        VOID_OBSERVABLE.assertCalled();
    }

    /**
     * failure of unregistration from MSB should be propagated
     */
    @Test
    public void testUnregistrationFailure() throws Exception {
        //given
        Subscription subscription = new Subscription();
        subscription.setCallbackUrl("http://5.6.7.8:12345/api/NokiaSVNFM/v1/lcn");
        subscription.setId(SUBCRIPTION_ID);
        when(vnfmInfoProvider.getVnfms()).thenReturn(newHashSet(VNFM_ID));
        subscriptions.add(subscription);
        when(msbClient.deleteMicroService(SERVICE_NAME, DRIVER_VERSION, null, null)).then(new Answer<Observable<Void>>() {
            @Override
            public Observable<Void> answer(InvocationOnMock invocationOnMock) throws Throwable {
                throw new RuntimeException();
            }
        });
        //when
        try {
            selfRegistrationManager.deRegister();
            fail();
        } catch (RuntimeException e) {

        }
        //verify
        InOrder inOrder = Mockito.inOrder(jobManager, msbClient, lcnApi);
        inOrder.verify(msbClient).deleteMicroService(SERVICE_NAME, DRIVER_VERSION, null, null);
        inOrder.verify(msbClient).getMicroService_0(SERVICE_NAME, DRIVER_VERSION, null, null, null, null, null);
        verify(lcnApi, Mockito.never()).subscriptionsSubscriptionIdDelete(subscription.getId(), NOKIA_LCN_API_VERSION);
        assertServiceDown();
    }

    /**
     * failure of subscription deletion from MSB should be propagated
     */
    @Test
    public void testSubscriptionFailure() throws Exception {
        //given
        Subscription subscription = new Subscription();
        subscription.setCallbackUrl("http://5.6.7.8:12345/api/NokiaSVNFM/v1/lcn");
        subscription.setId(SUBCRIPTION_ID);
        when(vnfmInfoProvider.getVnfms()).thenReturn(newHashSet(VNFM_ID));
        when(lcnApi.subscriptionsPost(subscriptionToCreate.capture(), Mockito.eq(NOKIA_LCN_API_VERSION))).thenAnswer(invocationOnMock -> {
            subscription.setCallbackUrl("http://5.6.7.8:12345/api/NokiaSVNFM/v1/lcn");
            subscription.setId(UUID.randomUUID().toString());
            subscriptions.add(subscription);
            when(lcnApi.subscriptionsSubscriptionIdDelete(subscription.getId(), NOKIA_LCN_API_VERSION)).thenReturn(VOID_OBSERVABLE.value());
            return buildObservable(subscription);
        });
        selfRegistrationManager.assureSubscription(VNFM_ID);
        when(jobManager.hasOngoingJobs()).thenReturn(false);
        RuntimeException expectedException = new RuntimeException();
        doThrow(expectedException).when(lcnApi).subscriptionsSubscriptionIdDelete(subscription.getId(), NOKIA_LCN_API_VERSION);
        when(msbClient.deleteMicroService(SERVICE_NAME, DRIVER_VERSION, null, null)).thenReturn(VOID_OBSERVABLE.value());
        //when
        try {
            selfRegistrationManager.deRegister();
            fail();
        } catch (RuntimeException e) {

        }
        //verify
        InOrder inOrder = Mockito.inOrder(jobManager, msbClient, lcnApi);
        inOrder.verify(msbClient).deleteMicroService(SERVICE_NAME, DRIVER_VERSION, null, null);
        inOrder.verify(lcnApi).subscriptionsSubscriptionIdDelete(subscription.getId(), NOKIA_LCN_API_VERSION);
        assertServiceDown();
    }

    /**
     * the swagger API definitions embedded in the code
     */
    @Test
    public void testSwaggerApi() throws Exception {
        //no idea how to test this except repeat implementation
        byte[] a = TestUtil.loadFile(SelfRegistrationManager.SWAGGER_API_DEFINITION);
        tearGeneric();
        //when
        assertTrue(Arrays.equals(a, selfRegistrationManager.getSwaggerApiDefinition()));
    }

    public void assertServiceUp() throws Exception {
        assertTrue(selfRegistrationManager.isReady());
    }

    /**
     * if there are ongoing jobs then the guard thros exception
     */
    public void assertServiceDown() {
        assertFalse(selfRegistrationManager.isReady());

    }

}
