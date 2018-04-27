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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.restapi;

import com.nokia.cbam.lcm.v32.model.VnfLifecycleChangeNotification;
import javax.servlet.http.HttpServletResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification.LifecycleChangeNotificationManager;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification.LifecycleChangeNotificationManagerForSo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification.LifecycleChangeNotificationManagerForVfc;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.Constants.BASE_URL;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.Constants.LCN_URL;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Responsible for providing the Nokia CBAM REST API for recieving LCNs from CBAM
 */
@Controller
@RequestMapping(value = BASE_URL)
public class LcnApi {
    private static Logger logger = getLogger(LcnApi.class);
    private final LifecycleChangeNotificationManager lifecycleChangeNotificationManagerForSo;
    private final LifecycleChangeNotificationManager lifecycleChangeNotificationManagerForVfc;

    @Autowired
    LcnApi(LifecycleChangeNotificationManagerForSo lifecycleManagerForSo, LifecycleChangeNotificationManagerForVfc lifecycleManagerForVfc) {
        this.lifecycleChangeNotificationManagerForSo = lifecycleManagerForSo;
        this.lifecycleChangeNotificationManagerForVfc = lifecycleManagerForVfc;
    }

    /**
     * Provides a probe for CBAM VNFM to test LCN registration
     *
     * @param httpResponse the HTTP response
     */
    @RequestMapping(value = LCN_URL, method = GET)
    public void testLcnConnectivity(HttpServletResponse httpResponse) {
        //used to test connectivity from CBAM to driver
    }

    /**
     * Handle the LCN sent by CBAM
     *
     * @param lcn the LCN notification
     */
    @RequestMapping(value = LCN_URL, method = POST, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(code = NO_CONTENT)
    public void handleLcn(@RequestBody VnfLifecycleChangeNotification lcn) {
        logger.info("REST: handle LCN");
        //FIXME fork between where the VNF is managed
        lifecycleChangeNotificationManagerForVfc.handleLcn(lcn);
    }
}
