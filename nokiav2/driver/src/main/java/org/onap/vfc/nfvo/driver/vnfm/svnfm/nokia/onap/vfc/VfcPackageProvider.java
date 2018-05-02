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

import com.google.common.io.ByteStreams;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api.IPackageProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core.IpMappingProvider;
import org.onap.vfccatalog.api.VnfpackageApi;
import org.onap.vfccatalog.model.VnfPkgDetailInfo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.buildFatalFailure;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.SystemFunctions.systemFunctions;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

/**
 * Retrieves a package from VF-C
 */
@Component
public class VfcPackageProvider implements IPackageProvider {
    private static Logger logger = getLogger(VfcPackageProvider.class);
    private final VfcRestApiProvider restApiProvider;
    private final IpMappingProvider ipMappingProvider;

    @Autowired
    VfcPackageProvider(VfcRestApiProvider restApiProvider, IpMappingProvider ipMappingProvider) {
        this.restApiProvider = restApiProvider;
        this.ipMappingProvider = ipMappingProvider;
    }

    @Override
    public String getCbamVnfdId(String csarId) {
        VnfPkgDetailInfo vnfPackageDetails;
        try {
            VnfpackageApi onapCatalogApi = restApiProvider.getVfcCatalogApi();
            vnfPackageDetails = onapCatalogApi.queryVnfPackage(csarId).blockingFirst();
        } catch (Exception e) {
            throw buildFatalFailure(logger, "Unable to query VNF package with " + csarId, e);
        }
        JsonElement vnfdModel = new JsonParser().parse(vnfPackageDetails.getPackageInfo().getVnfdModel());
        return vnfdModel.getAsJsonObject().get("metadata").getAsJsonObject().get("resourceVendorModelNumber").getAsString();
    }

    @Override
    public byte[] getPackage(String csarId) {
        String downloadUrl;
        try {
            VnfpackageApi onapCatalogApi = restApiProvider.getVfcCatalogApi();
            VnfPkgDetailInfo vnfPackageDetails = onapCatalogApi.queryVnfPackage(csarId).blockingFirst();
            String urlFromVfc = vnfPackageDetails.getPackageInfo().getDownloadUrl();
            String host = new URL(urlFromVfc).getHost();
            downloadUrl = urlFromVfc.replaceFirst("://" + host, "://" + ipMappingProvider.mapPrivateIpToPublicIp(host));
        } catch (Exception e) {
            throw buildFatalFailure(logger, "Unable to query VNF package with " + csarId, e);
        }
        try {
            return downloadCbamVnfPackage(downloadUrl);
        } catch (Exception e) {
            throw buildFatalFailure(logger, "Unable to download package from " + downloadUrl, e);
        }
    }

    private byte[] downloadCbamVnfPackage(String downloadUri) throws IOException {
        CloseableHttpClient client = systemFunctions().getHttpClient();
        HttpGet httpget = new HttpGet(downloadUri);
        httpget.setHeader(HttpHeaders.ACCEPT, APPLICATION_OCTET_STREAM_VALUE);
        CloseableHttpResponse response = client.execute(httpget);
        HttpEntity entity = response.getEntity();
        InputStream is = entity.getContent();
        byte[] bytes = ByteStreams.toByteArray(is);
        client.close();
        return bytes;
    }
}
