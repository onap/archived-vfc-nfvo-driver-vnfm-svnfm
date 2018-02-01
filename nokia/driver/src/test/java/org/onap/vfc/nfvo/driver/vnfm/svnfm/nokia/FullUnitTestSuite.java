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
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api.*;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.impl.*;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.packagetransformer.TestCbamVnfPackageBuilder;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.packagetransformer.TestCbamVnfdBuilder;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.packagetransformer.TestOnapVnfPackageBuilder;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.packagetransformer.TestOnapVnfdBuilder;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.spring.TestRealConfig;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.spring.TestServletInitializer;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.TestCbamUtils;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.TestRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.TestStoreLoader;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.TestSystemFunctions;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        TestAdditionalParams.class,
        TestCbamCatalogManager.class,
        TestCbamTokenProvider.class,
        TestDriverProperties.class,
        TestGrantManager.class,
        TestJobManager.class,
        TestLifecycleChangeNotificationManager.class,
        TestLifecycleManager.class,
        TestSelfRegistrationManager.class,

        TestCbamVnfdBuilder.class,
        TestOnapVnfdBuilder.class,
        TestCbamVnfPackageBuilder.class,
        TestOnapVnfPackageBuilder.class,

        TestCbamUtils.class,
        TestRestApiProvider.class,
        TestStoreLoader.class,
        TestSystemFunctions.class,

        TestServletInitializer.class,
        TestRealConfig.class,

        TestConverterApi.class,
        TestLcmApi.class,
        TestSwaggerApi.class,
        TestLcnApi.class,

        TestNokiaSvnfmApplication.class,

        TestSwaggerDefinitionConsistency.class,
})
public class FullUnitTestSuite {
}













