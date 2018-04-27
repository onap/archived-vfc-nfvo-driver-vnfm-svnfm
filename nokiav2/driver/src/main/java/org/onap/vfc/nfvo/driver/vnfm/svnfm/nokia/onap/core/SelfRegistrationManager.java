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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.nokia.cbam.lcn.v32.api.SubscriptionsApi;
import com.nokia.cbam.lcn.v32.model.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.onap.msb.model.MicroServiceFullInfo;
import org.onap.msb.model.MicroServiceInfo;
import org.onap.msb.model.Node;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api.VnfmInfoProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.MultiException;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.Constants;
import org.onap.vnfmdriver.model.VnfmInfo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import static com.nokia.cbam.lcn.v32.model.SubscriptionAuthentication.TypeEnum.NONE;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.buildFatalFailure;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.SystemFunctions.systemFunctions;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProvider.NOKIA_LCN_API_VERSION;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Responsible for registering the driver in the core systems.
 */
public class SelfRegistrationManager {
    public static final String DRIVER_VERSION = "v1";
    public static final String SERVICE_NAME = "NokiaSVNFM";
    public static final String SWAGGER_API_DEFINITION = "self.swagger.json";
    private static Logger logger = getLogger(SelfRegistrationManager.class);
    private final MsbApiProvider msbApiProvider;
    private final CbamRestApiProvider cbamRestApiProvider;
    private final VnfmInfoProvider vnfmInfoProvider;
    private final BiMap<String, String> vnfmIdToSubscriptionId = HashBiMap.create();
    @Value("${driverMsbExternalIp}")
    private String driverMsbExternalIp;
    @Value("${driverVnfmExternalIp}")
    private String driverVnfmExternalIp;
    @Value("${server.port}")
    private String driverPort;
    private volatile boolean ready = false;

    SelfRegistrationManager(VnfmInfoProvider vnfmInfoProvider, MsbApiProvider msbApiProvider, CbamRestApiProvider cbamRestApiProvider) {
        this.cbamRestApiProvider = cbamRestApiProvider;
        this.msbApiProvider = msbApiProvider;
        this.vnfmInfoProvider = vnfmInfoProvider;
    }

    /**
     * Register the driver in micro-service bus and subscribe to LCNs from CBAM
     */
    public void register() {
        //the order is important (only publish it's existence after the subscription has been created)
        subscribeToLcns();
        try {
            registerMicroService();
        } catch (RuntimeException e) {
            deleteSubscriptions();
            throw e;
        }
        ready = true;
    }

    /**
     * De-register the VNFM driver from the micro-service bus
     */
    public void deRegister() {
        try {
            logger.info("Cancelling micro service registration");
            msbApiProvider.getMsbApi().deleteMicroService(SERVICE_NAME, DRIVER_VERSION, null, null).blockingFirst();
        } catch (Exception e) {
            //ONAP throws 500 internal server error, but deletes the micro service
            boolean serviceFoundAfterDelete = false;
            try {
                msbApiProvider.getMsbApi().getMicroService_0(SERVICE_NAME, DRIVER_VERSION, null, null, null, null, null);
                serviceFoundAfterDelete = true;
            } catch (Exception e1) {
                logger.info("Unable to query " + SERVICE_NAME + " from MSB (so the service was successfully deleted)", e1);
                // the micro service was deleted (even though 500 HTTP code was reported)
            }
            if (serviceFoundAfterDelete) {
                throw buildFatalFailure(logger, "Unable to deRegister Nokia VNFM driver", e);
            }
        }
        deleteSubscriptions();
    }

    /**
     * Subscribes to LCN if not yet subscribed
     *
     * @param vnfmId the identifier of the VNFM
     */
    public void assureSubscription(String vnfmId) {
        if (!vnfmIdToSubscriptionId.containsKey(vnfmId)) {
            subscribeToLcn(vnfmId);
        }
    }

    /**
     * @return the swagger API definition
     */
    public byte[] getSwaggerApiDefinition() {
        return systemFunctions().loadFile(SWAGGER_API_DEFINITION);
    }

    /**
     * @param subscriptionId the identifier of the subscription
     * @return the identifier of the VNFM for the subscription
     */
    public String getVnfmId(String subscriptionId) {
        return vnfmIdToSubscriptionId.inverse().get(subscriptionId);
    }

    private String getDriverVnfmUrl() {
        return "http://" + driverVnfmExternalIp + ":" + driverPort + Constants.BASE_URL;
    }

    private void deleteSubscriptions() {
        Set<Exception> exceptions = new HashSet<>();
        for (String vnfmId : vnfmIdToSubscriptionId.keySet()) {
            try {
                deleteSubscription(vnfmId);
            } catch (Exception e) {
                exceptions.add(e);
                logger.warn("Unable to delete subscription for the " + vnfmId);
            }
        }
        if (!exceptions.isEmpty()) {
            throw new MultiException("Unable to delete some of the subscriptions", exceptions);
        }
    }

    private void deleteSubscription(String vnfmId) {
        logger.info("Deleting CBAM LCN subscription");
        SubscriptionsApi lcnApi = cbamRestApiProvider.getCbamLcnApi(vnfmId);
        try {
            String callbackUrl = getDriverVnfmUrl() + Constants.LCN_URL;
            for (Subscription subscription : lcnApi.subscriptionsGet(NOKIA_LCN_API_VERSION).blockingFirst()) {
                if (subscription.getCallbackUrl().equals(callbackUrl)) {
                    logger.info("Deleting subscription with {} identifier", subscription.getId());
                    lcnApi.subscriptionsSubscriptionIdDelete(subscription.getId(), NOKIA_LCN_API_VERSION).blockingFirst();
                }
            }
        } catch (Exception e) {
            throw buildFatalFailure(logger, "Unable to delete CBAM LCN subscription", e);
        }
    }

    private MicroServiceFullInfo registerMicroService() {
        logger.info("Registering micro service");
        MicroServiceInfo microServiceInfo = new MicroServiceInfo();
        microServiceInfo.setUrl(Constants.BASE_URL);
        //the PATH should not be set
        microServiceInfo.setProtocol(MicroServiceInfo.ProtocolEnum.REST);
        microServiceInfo.setVisualRange(MicroServiceInfo.VisualRangeEnum._1);
        microServiceInfo.setServiceName(SERVICE_NAME);
        microServiceInfo.setVersion(DRIVER_VERSION);
        microServiceInfo.setEnableSsl(false);
        Node node = new Node();
        microServiceInfo.setNodes(new ArrayList<>());
        microServiceInfo.getNodes().add(node);
        node.setIp(driverMsbExternalIp);
        node.setPort(driverPort);
        node.setTtl("0");
        try {
            return msbApiProvider.getMsbApi().addMicroService(microServiceInfo, true, false).blockingFirst();
        } catch (Exception e) {
            throw buildFatalFailure(logger, "Unable to register Nokia VNFM driver", e);
        }
    }

    private void subscribeToLcns() {
        for (String vnfmId : vnfmInfoProvider.getVnfms()) {
            subscribeToLcn(vnfmId);
        }
    }

    private void subscribeToLcn(String vnfmId) {
        String callbackUrl = getDriverVnfmUrl() + Constants.LCN_URL;
        VnfmInfo vnfmInfo = vnfmInfoProvider.getVnfmInfo(vnfmId);
        VnfmUrls vnfmUrls = GenericExternalSystemInfoProvider.convert(vnfmInfo);
        logger.info("Subscribing to CBAM LCN {} with callback to {}", vnfmUrls.getLcnUrl(), callbackUrl);
        SubscriptionsApi lcnApi = cbamRestApiProvider.getCbamLcnApi(vnfmId);
        try {
            for (Subscription subscription : lcnApi.subscriptionsGet(NOKIA_LCN_API_VERSION).blockingFirst()) {
                if (subscription.getCallbackUrl().equals(callbackUrl)) {
                    logger.warn("The subscription with {} identifier has the same callback URL", subscription.getId());
                    vnfmIdToSubscriptionId.put(vnfmId, subscription.getId());
                    return;
                }
            }
            CreateSubscriptionRequest request = new CreateSubscriptionRequest();
            request.setFilter(new SubscriptionFilter());
            request.getFilter().setNotificationTypes(new ArrayList<>());
            request.getFilter().getNotificationTypes().add(VnfNotificationType.VNFLIFECYCLECHANGENOTIFICATION);
            request.setCallbackUrl(callbackUrl);
            request.getFilter().addOperationTypesItem(OperationType.HEAL);
            request.getFilter().addOperationTypesItem(OperationType.INSTANTIATE);
            request.getFilter().addOperationTypesItem(OperationType.SCALE);
            request.getFilter().addOperationTypesItem(OperationType.TERMINATE);
            SubscriptionAuthentication subscriptionAuthentication = new SubscriptionAuthentication();
            subscriptionAuthentication.setType(NONE);
            request.setAuthentication(subscriptionAuthentication);
            Subscription createdSubscription = lcnApi.subscriptionsPost(request, NOKIA_LCN_API_VERSION).blockingFirst();
            logger.info("Subscribed to LCN with {} identifier", createdSubscription.getId());
            vnfmIdToSubscriptionId.put(vnfmId, createdSubscription.getId());
        } catch (Exception e) {
            throw buildFatalFailure(logger, "Unable to subscribe to CBAM LCN", e);
        }
    }

    /**
     * @return is the component ready to serve requests
     */
    public boolean isReady() {
        return ready;
    }
}
