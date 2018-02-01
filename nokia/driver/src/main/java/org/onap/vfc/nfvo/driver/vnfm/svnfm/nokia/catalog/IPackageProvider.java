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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.catalog;

/**
 * Provides a VNF package from ONAP repositories
 */
public interface IPackageProvider {
    String CBAM_PACKAGE_NAME_IN_ZIP = "Artifacts/Deployment/OTHER/cbam.package.zip";

    /**
     * Download the package from ONAP
     *
     * @param csarId the CSAR identifier of the package in ONAP
     * @return the binary content of the package
     */
    byte[] getPackage(String csarId);

    /**
     * @param csarId the identifier of the package in ONAP
     * @return the identifier of the package in CBAM
     */
    String getCbamVnfdId(String csarId);
}
