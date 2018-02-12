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

import com.google.gson.Gson;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api.INotificationSender;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.spring.Conditions;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.DriverProperties;
import org.onap.vnfmdriver.model.VNFLCMNotification;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.fatalFailure;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Responsible for sending notifications to VF-C
 */
@Component
@Conditional(value = Conditions.UseForVfc.class)
public class VfcNotificationSender implements INotificationSender {
    private static Logger logger = getLogger(VfcNotificationSender.class);
    @Autowired
    private DriverProperties driverProperties;
    @Autowired
    private VfcRestApiProvider vfcRestApiProvider;

    @Override
    public void sendNotification(VNFLCMNotification notification) {
        try {
            logger.info("Sending LCN: " + new Gson().toJson(notification));
            vfcRestApiProvider.getNsLcmApi().vNFLCMNotification(driverProperties.getVnfmId(), notification.getVnfInstanceId(), notification);
        } catch (Exception e) {
            fatalFailure(logger, "Unable to send LCN to ONAP", e);
        }
    }
}
