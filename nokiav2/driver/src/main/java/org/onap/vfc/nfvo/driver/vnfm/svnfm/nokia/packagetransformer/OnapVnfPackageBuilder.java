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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.packagetransformer;

import com.google.common.io.ByteStreams;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.SystemFunctions.systemFunctions;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CatalogManager.getFileInZip;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CatalogManager.getVnfdLocation;

/**
 * Transforms a CBAM package into an ONAP package
 */

public class OnapVnfPackageBuilder {

    /**
     * Entry point for the command line package transformer
     *
     * @param args not used (required due to signature)
     */
    public static void main(String[] args) throws Exception {
        byte[] covert = new OnapVnfPackageBuilder().covert(systemFunctions().in(), SupportedOnapPackageVersions.V1TOSCA);
        systemFunctions().out().write(covert);
    }

    /**
     * @param zip     the original CBAM package
     * @param version
     * @return the converted ONAP package
     */
    public byte[] covert(InputStream zip, SupportedOnapPackageVersions version) throws IOException {
        byte[] cbamVnfPackage = ByteStreams.toByteArray(zip);
        String vnfdLocation = getVnfdLocation(new ByteArrayInputStream(cbamVnfPackage));
        ByteArrayOutputStream vnfdContent = getFileInZip(new ByteArrayInputStream(cbamVnfPackage), vnfdLocation);
        byte[] cbamVnfdContent = vnfdContent.toByteArray();
        byte[] modifiedCbamPackage = new CbamVnfPackageBuilder().toModifiedCbamVnfPackage(cbamVnfPackage, vnfdLocation, new CbamVnfdBuilder().build(new String(cbamVnfdContent)));
        switch (version){
            case V1TOSCA:
            case V2TOSCA:
                String onapVnfd = SupportedOnapPackageVersions.V2TOSCA == version ?
                        new OnapR2VnfdBuilder().toOnapVnfd(new String(cbamVnfdContent, StandardCharsets.UTF_8)) :
                        new OnapR1VnfdBuilder().toOnapVnfd(new String(cbamVnfdContent, StandardCharsets.UTF_8));
                return buildNewOnapPackage(modifiedCbamPackage, onapVnfd);
            case V2HEAT:
            default:
                Map<String, String> files = new OnapR2HeatPackageBuilder().processVnfd(new String(cbamVnfdContent));
                return buildHeatOnapPackage(modifiedCbamPackage, files);
        }

    }

    private byte [] buildHeatOnapPackage(byte [] modifiedCbamPackage, Map<String, String> heatFiles) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        ZipOutputStream out = new ZipOutputStream(result);
        out.putNextEntry(new ZipEntry("Artifacts/Deployment/OTHER/cbam.package.zip"));
        out.write(modifiedCbamPackage);
        out.closeEntry();
        for (Map.Entry<String, String> file : heatFiles.entrySet()) {
            out.putNextEntry(new ZipEntry(file.getKey()));
            out.write(file.getValue().getBytes());
            out.closeEntry();
        }
        out.close();
        return result.toByteArray();
    }

    private byte[] buildNewOnapPackage(byte[] modifiedCbamPackage, String onapVnfd) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        ZipOutputStream out = new ZipOutputStream(result);
        out.putNextEntry(new ZipEntry("Artifacts/Deployment/OTHER/cbam.package.zip"));
        out.write(modifiedCbamPackage);
        out.closeEntry();
        out.putNextEntry(new ZipEntry("TOSCA-Metadata/TOSCA.meta"));
        out.write(systemFunctions().loadFile("TOSCA.meta"));
        out.closeEntry();
        out.putNextEntry(new ZipEntry("MainServiceTemplate.yaml"));
        out.write(onapVnfd.getBytes());
        out.closeEntry();
        out.closeEntry();
        out.putNextEntry(new ZipEntry("MainServiceTemplate.mf"));
        out.write(systemFunctions().loadFile("MainServiceTemplate.mf"));
        out.closeEntry();
        out.close();
        return result.toByteArray();
    }
}
