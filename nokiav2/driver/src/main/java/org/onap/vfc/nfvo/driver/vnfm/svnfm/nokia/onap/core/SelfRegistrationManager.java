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

import com.nokia.cbam.lcn.v32.api.SubscriptionsApi;
import com.nokia.cbam.lcn.v32.model.*;
import java.util.ArrayList;
import org.onap.msb.model.MicroServiceFullInfo;
import org.onap.msb.model.MicroServiceInfo;
import org.onap.msb.model.Node;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.DriverProperties;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.nokia.cbam.lcn.v32.model.SubscriptionAuthentication.TypeEnum.NONE;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.buildFatalFailure;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.SystemFunctions.systemFunctions;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProvider.NOKIA_LCN_API_VERSION;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Responsible for registering the driver in the core systems.
 */
@Component
public class SelfRegistrationManager {
    public static final String DRIVER_VERSION = "v1";
    public static final String SERVICE_NAME = "NokiaSVNFM";
    public static final String SWAGGER_API_DEFINITION = "self.swagger.json";
    private static Logger logger = getLogger(SelfRegistrationManager.class);
    private final DriverProperties driverProperties;
    private final MsbApiProvider msbApiProvider;
    private final CbamRestApiProvider cbamRestApiProvider;
    @Value("${driverMsbExternalIp}")
    private String driverMsbExternalIp;
    @Value("${driverVnfmExternalIp}")
    private String driverVnfmExternalIp;
    @Value("${server.port}")
    private String driverPort;
    private volatile boolean ready = false;

    @Autowired
    SelfRegistrationManager(DriverProperties driverProperties, MsbApiProvider msbApiProvider, CbamRestApiProvider cbamRestApiProvider) {
        this.cbamRestApiProvider = cbamRestApiProvider;
        this.msbApiProvider = msbApiProvider;
        this.driverProperties = driverProperties;
    }

    /**
     * Register the driver in micro-service bus and subscribe to LCNs from CBAM
     */
    public void register() {
        //the order is important (only publish it's existence after the subscription has been created)
        subscribeToLcn(driverProperties.getVnfmId());
        try {
            registerMicroService();
        } catch (RuntimeException e) {
            deleteSubscription(driverProperties.getVnfmId());
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
        deleteSubscription(driverProperties.getVnfmId());
    }

    /**
     * @return the swagger API definition
     */
    public byte[] getSwaggerApiDefinition() {
        return systemFunctions().loadFile(SWAGGER_API_DEFINITION);
    }

    private String getDriverVnfmUrl() {
        return "http://" + driverVnfmExternalIp + ":" + driverPort + DriverProperties.BASE_URL;
    }

    private void deleteSubscription(String vnfmId) {
        logger.info("Deleting CBAM LCN subscription");
        SubscriptionsApi lcnApi = cbamRestApiProvider.getCbamLcnApi(vnfmId);
        try {
            String callbackUrl = getDriverVnfmUrl() + DriverProperties.LCN_URL;
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
        microServiceInfo.setUrl(DriverProperties.BASE_URL);
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

    private void subscribeToLcn(String vnfmId) {
        String callbackUrl = getDriverVnfmUrl() + DriverProperties.LCN_URL;
        logger.info("Subscribing to CBAM LCN {} with callback to {}", driverProperties.getCbamLcnUrl(), callbackUrl);
        SubscriptionsApi lcnApi = cbamRestApiProvider.getCbamLcnApi(vnfmId);
        try {
            for (Subscription subscription : lcnApi.subscriptionsGet(NOKIA_LCN_API_VERSION).blockingFirst()) {
                if (subscription.getCallbackUrl().equals(callbackUrl)) {
                    logger.warn("The subscription with {} identifier has the same callback URL", subscription.getId());
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
