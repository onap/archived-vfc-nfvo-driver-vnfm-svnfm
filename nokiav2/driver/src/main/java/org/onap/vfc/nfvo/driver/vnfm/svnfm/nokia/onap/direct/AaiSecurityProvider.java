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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct;

import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.GenericSecurityProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Responsible for providing SSL factories for AAI
 */
@Component
public class AaiSecurityProvider extends GenericSecurityProvider {
    @Value("${trustedCertificatesForAai}")
    private String trustedCertificates;
    @Value("${skipCertificateVerificationForAai}")
    private boolean skipCertificateVerification;
    @Value("${skipHostnameVerificationForAai}")
    private boolean skipHostnameVerification;

    @Override
    protected boolean skipHostnameVerification() {
        return skipHostnameVerification;
    }

    @Override
    protected boolean skipCertificateVerification() {
        return skipCertificateVerification;
    }

    @Override
    protected String trustedCertificates() {
        return trustedCertificates;
    }
}
