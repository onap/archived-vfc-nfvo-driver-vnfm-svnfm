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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api.IPackageProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core.MsbApiProvider;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import static java.lang.String.format;

import static com.google.common.io.ByteStreams.toByteArray;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core.SelfRegistrationManager.SERVICE_NAME;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.*;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.SystemFunctions.systemFunctions;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CatalogManager.getFileInZip;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CatalogManager.getVnfdLocation;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

/**
 * Responsible for providing access to AAI APIs.
 * Handles authentication and mandatory parameters.
 */
@Component
public class SdcPackageProvider implements IPackageProvider {
    private static final String SDC_MSB_NAME = "sdc";
    private static final String SDC_MSB_VERSION = "v1";
    private static final String GET_PACKAGE_URL = "%s/sdc/v1/catalog/resources/%s/toscaModel";
    private static Logger logger = getLogger(SdcPackageProvider.class);
    private final MsbApiProvider msbApiProvider;
    @Value("${sdcUsername}")
    private String sdcUsername;
    @Value("${sdcPassword}")
    private String sdcPassword;

    @Autowired
    SdcPackageProvider(MsbApiProvider msbApiProvider) {
        this.msbApiProvider = msbApiProvider;
    }

    @Override
    public byte[] getPackage(String csarId) {
        String baseUrl = msbApiProvider.getMicroServiceUrl(SDC_MSB_NAME, SDC_MSB_VERSION);
        try {
            CloseableHttpClient client = systemFunctions().getHttpClient();
            HttpGet httpget = new HttpGet(format(GET_PACKAGE_URL, baseUrl, csarId));
            httpget.setHeader(ACCEPT, APPLICATION_OCTET_STREAM_VALUE);
            httpget.setHeader("X-ECOMP-InstanceID", SERVICE_NAME);
            httpget.setHeader("X-FromAppId", SERVICE_NAME);
            CloseableHttpResponse response = client.execute(httpget);
            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();
            byte[] bytes = toByteArray(is);
            client.close();
            return bytes;
        } catch (Exception e) {
            throw buildFatalFailure(logger, "Unable to download " + csarId + " package from SDC", e);
        }
    }

    @Override
    public String getCbamVnfdId(String csarId) {
        byte[] onapPackage = getPackage(csarId);
        try {
            String vnfdLocation = getVnfdLocation(new ByteArrayInputStream(onapPackage));
            String onapVnfdContent = getFileInZip(new ByteArrayInputStream(onapPackage), vnfdLocation).toString();
            JsonObject root = new Gson().toJsonTree(new Yaml().load(onapVnfdContent)).getAsJsonObject();
            return childElement(child(root, "metadata"), "resourceVendorModelNumber").getAsString();
        } catch (Exception e) {
            throw buildFatalFailure(logger, "Unable to extract CBAM VNFD id from ONAP package", e);
        }
    }
}
