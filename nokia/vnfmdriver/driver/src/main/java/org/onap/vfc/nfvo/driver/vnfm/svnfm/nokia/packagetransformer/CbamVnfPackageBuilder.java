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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.SystemFunctions.systemFunctions;

/**
 * Builds a CBAM VNF package capable to be deployed on ONAP from a CBAM package
 */
public class CbamVnfPackageBuilder {

    /**
     * @param originalCbamVnfPackage  the original CBAM VNF package
     * @param vnfdLocation            the location of the VNFD within the CBAM VNF package
     * @param modifiedCbamVnfdContent the modified CBAM VNFD content
     * @return the mod
     */
    public byte[] toModifiedCbamVnfPackage(byte[] originalCbamVnfPackage, String vnfdLocation, String modifiedCbamVnfdContent) throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(originalCbamVnfPackage));
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        ZipOutputStream out = new ZipOutputStream(result);
        ZipEntry zipEntry;
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            if (zipEntry.getName().matches(vnfdLocation)) {
                out.putNextEntry(new ZipEntry(vnfdLocation));
                out.write(modifiedCbamVnfdContent.getBytes());
                out.closeEntry();
            } else {
                out.putNextEntry(new ZipEntry(zipEntry.getName()));
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ByteStreams.copy(zipInputStream, byteArrayOutputStream);
                out.write(byteArrayOutputStream.toByteArray());
                out.closeEntry();
            }
        }
        out.putNextEntry(new ZipEntry("javascript/cbam.pre.collectConnectionPoints.js"));
        out.write(systemFunctions().loadFile("cbam.pre.collectConnectionPoints.js"));
        out.closeEntry();
        out.putNextEntry(new ZipEntry("javascript/cbam.collectConnectionPoints.js"));
        out.write(systemFunctions().loadFile("cbam.collectConnectionPoints.js"));
        out.closeEntry();
        out.putNextEntry(new ZipEntry("javascript/cbam.post.collectConnectionPoints.js"));
        out.write(systemFunctions().loadFile("cbam.post.collectConnectionPoints.js"));
        out.closeEntry();
        out.close();
        zipInputStream.close();
        return result.toByteArray();
    }
}
