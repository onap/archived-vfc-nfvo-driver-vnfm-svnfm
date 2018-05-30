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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.nokia.cbam.catalog.v1.api.DefaultApi;
import com.nokia.cbam.catalog.v1.model.CatalogAdapterVnfpackage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api.IPackageProvider;
import org.slf4j.Logger;

import static com.google.common.base.Splitter.on;
import static com.google.common.collect.Iterables.filter;
import static okhttp3.MediaType.parse;
import static okhttp3.RequestBody.create;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.buildFatalFailure;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.LifecycleManager.ETSI_CONFIG;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;


/**
 * Responsible for handling the CBAM catalog
 * - the VNF package is uploaded as part of the instantiation
 * - the VNF package is not deleted after VNF deletion
 */
public class CatalogManager {
    /**
     * The location of the CBAM package within the ONAP package
     */
    public static final String CBAM_PACKAGE_NAME_IN_ZIP = "Artifacts/Deployment/OTHER/cbam.package.zip";
    public static final String ETSI_CONFIG_NAME_IN_ZIP = "Artifacts/Deployment/OTHER/" + ETSI_CONFIG + ".json";

    private static final String TOSCA_META_PATH = "TOSCA-Metadata/TOSCA.meta";
    private static final String TOSCA_VNFD_KEY = "Entry-Definitions";
    private static Logger logger = getLogger(CatalogManager.class);
    private final CbamRestApiProvider cbamRestApiProvider;
    private final IPackageProvider packageProvider;

    CatalogManager(CbamRestApiProvider cbamRestApiProvider, IPackageProvider packageProvider) {
        this.cbamRestApiProvider = cbamRestApiProvider;
        this.packageProvider = packageProvider;
    }

    /**
     * @param zip  the zip
     * @param path the path of the file to be returned
     * @return the file in the zip
     */
    public static ByteArrayOutputStream getFileInZip(InputStream zip, String path) throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(zip);
        ByteArrayOutputStream fileContent = getFileInZip(zipInputStream, path);
        zipInputStream.close();
        return fileContent;
    }

    /**
     * @param stream the CBAM VNF package
     * @return the location of the VNFD within the CBAM package
     */
    public static String getVnfdLocation(InputStream stream) throws IOException {
        String toscaMetadata = new String(getFileInZip(stream, TOSCA_META_PATH).toByteArray());
        String toscaVnfdLine = filter(on("\n").split(toscaMetadata), line -> line.contains(TOSCA_VNFD_KEY)).iterator().next();
        return toscaVnfdLine.replace(TOSCA_VNFD_KEY + ":", "").trim();
    }

    private static ByteArrayOutputStream getFileInZip(ZipInputStream zipInputStream, String path) throws IOException {
        ZipEntry zipEntry;
        Set<String> items = new HashSet<>();
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            items.add(zipEntry.getName());
            if (zipEntry.getName().matches(path)) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ByteStreams.copy(zipInputStream, byteArrayOutputStream);
                return byteArrayOutputStream;
            }
        }
        logger.error("Unable to find the {} in archive found: {}", path, items);
        throw new NoSuchElementException("Unable to find the " + path + " in archive found: " + items);
    }

    /**
     * Prepare the VNF package in CBAM. If the package is not available in the catalog it is uploaded.
     *
     * @param vnfmId the identifier of the VNFM
     * @param csarId the CSAR identifier of the package in ONAP catalog
     * @return the package in CBAM catalog
     */
    public CatalogAdapterVnfpackage preparePackageInCbam(String vnfmId, String csarId) {
        String cbamVnfdId = packageProvider.getCbamVnfdId(csarId);
        DefaultApi cbamCatalogApi = cbamRestApiProvider.getCbamCatalogApi(vnfmId);
        if (!isPackageReplicated(cbamVnfdId, cbamCatalogApi)) {
            try {
                ByteArrayOutputStream cbamPackage = getFileInZip(new ByteArrayInputStream(packageProvider.getPackage(csarId)), CBAM_PACKAGE_NAME_IN_ZIP);
                return cbamCatalogApi.create(create(parse(APPLICATION_OCTET_STREAM.toString()), cbamPackage.toByteArray())).blockingFirst();
            } catch (Exception e) {
                logger.debug("Probably concurrent package uploads", e);
                //retest if the VNF package exists in CBAM. It might happen that an other operation
                //triggered the replication making this API fail. The replication is considered to be
                //successful if the package exist in CBAM even if the current package transfer failed
                if (isPackageReplicated(cbamVnfdId, cbamCatalogApi)) {
                    return queryPackageFromCBAM(cbamVnfdId, cbamCatalogApi);
                } else {
                    throw buildFatalFailure(logger, "Unable to create VNF with " + csarId + " CSAR identifier in package in CBAM", e);
                }
            }
        }
        return queryPackageFromCBAM(cbamVnfdId, cbamCatalogApi);
    }

    /**
     * Download the ETSI configuration of the VNF
     *
     * @param csarId the CSAR identifier of the package in ONAP catalog
     * @return the content of the ETSI configuration
     */
    public String getEtsiConfiguration(String csarId) {
        try {
            ByteArrayOutputStream etsiConfig = getFileInZip(new ByteArrayInputStream(packageProvider.getPackage(csarId)), ETSI_CONFIG_NAME_IN_ZIP);
            return new String(etsiConfig.toByteArray(), Charsets.UTF_8);
        } catch (Exception e) {
            throw buildFatalFailure(logger, "Unable to download the ETSI configuration file");
        }
    }

    /**
     * Gets the content of the VNFD from the CBAM package uploaded to CBAM
     *
     * @param vnfmId the identifier of the VNFM
     * @param vnfdId the identifier of the VNFD
     * @return the content of the CBAM VNFD
     */
    public String getCbamVnfdContent(String vnfmId, String vnfdId) {
        try {
            byte[] vnfdContent = cbamRestApiProvider.getCbamCatalogApi(vnfmId).content(vnfdId).blockingFirst().bytes();
            String vnfdPath = getVnfdLocation(new ByteArrayInputStream(vnfdContent));
            return new String(getFileInZip(new ByteArrayInputStream(vnfdContent), vnfdPath).toByteArray());
        } catch (Exception e) {
            throw buildFatalFailure(logger, "Unable to get package with (" + vnfdId + ")", e);
        }
    }

    private boolean isPackageReplicated(String cbamVnfdId, DefaultApi cbamCatalogApi) {
        try {
            return isPackageReplicatedToCbam(cbamVnfdId, cbamCatalogApi);
        } catch (Exception e) {
            throw buildFatalFailure(logger, "Unable to determine if the VNF package has been replicated in CBAM", e);
        }
    }

    private CatalogAdapterVnfpackage queryPackageFromCBAM(String cbamVnfdId, DefaultApi cbamCatalogApi) {
        try {
            return cbamCatalogApi.getById(cbamVnfdId).blockingFirst();
        } catch (Exception e) {
            throw buildFatalFailure(logger, "Unable to query VNF package with " + cbamVnfdId + " from CBAM", e);
        }
    }

    private boolean isPackageReplicatedToCbam(String cbamVnfdId, DefaultApi cbamCatalogApi) {
        for (CatalogAdapterVnfpackage vnfPackage : cbamCatalogApi.list().blockingFirst()) {
            if (vnfPackage.getVnfdId().equals(cbamVnfdId)) {
                return true;
            }
        }
        return false;
    }
}
