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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core.TestGenericExternalSystemInfoProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core.TestIpMappingProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core.TestMsbApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core.TestSelfRegistrationManager;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.TestAAIExternalSystemInfoProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.TestAAIRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.TestGrantlessGrantManager;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.TestSdcPackageProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.notification.*;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.vfc.*;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.packagetransformer.*;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.restapi.*;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.spring.TestRealConfig;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.spring.TestServletInitializer;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.*;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.*;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification.TestLifecycleChangeNotificationManager;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification.TestProcessedNotification;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification.TestReportedAffectedConnectionPoints;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification.TestReportedAffectedCp;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        TestGenericExternalSystemInfoProvider.class,
        TestIpMappingProvider.class,
        TestMsbApiProvider.class,

        TestAAINotificationProcessor.class,
        TestAbstractManager.class,
        TestGenericVnfManager.class,
        TestL3NetworkManager.class,
        TestLInterfaceManager.class,
        TestVnfcManager.class,
        TestVserverManager.class,

        TestAAIExternalSystemInfoProvider.class,
        TestAAIRestApiProvider.class,
        TestGrantlessGrantManager.class,
        TestSdcPackageProvider.class,

        TestVfcExternalSystemInfoProvider.class,
        TestVfcGrantManager.class,
        TestVfcPackageProvider.class,
        TestVfcRestApiProvider.class,
        TestVfcNotificationSender.class,

        TestCbamVnfdBuilder.class,
        TestOnapR1VnfdBuilder.class,
        TestOnapR2VnfdBuilder.class,
        TestCbamVnfPackageBuilder.class,
        TestOnapVnfPackageBuilder.class,

        TestConverterApi.class,
        TestLcmApi.class,
        TestLcnApi.class,
        TestSwaggerApi.class,
        TestSwaggerDefinitionConsistency.class,

        TestServletInitializer.class,
        TestRealConfig.class,

        TestCbamUtils.class,
        TestStoreLoader.class,
        TestSystemFunctions.class,
        TestUserInvisibleError.class,
        TestUserVisibleError.class,

        TestLifecycleChangeNotificationManager.class,
        TestProcessedNotification.class,
        TestReportedAffectedConnectionPoints.class,
        TestReportedAffectedCp.class,

        TestAdditionalParams.class,
        TestCbamCatalogManager.class,
        TestCbamRestApiProvider.class,
        TestCbamSecurityProvider.class,
        TestCbamTokenProvider.class,
        TestJobManager.class,
        TestVfcGrantManager.class,
        TestLifecycleManager.class,
        TestSelfRegistrationManager.class,
        TestNokiaSvnfmApplication.class,

})
public class FullUnitTestSuite {
}











