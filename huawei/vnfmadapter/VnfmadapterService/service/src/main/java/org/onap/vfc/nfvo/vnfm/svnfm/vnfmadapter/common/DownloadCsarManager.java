/*
 * Copyright 2016-2017 Huawei Technologies Co., Ltd.
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

package org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.constant.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to download CSAR
 *
 * @author
 * @version VFC 1.0 Sep 5, 2016
 */
public class DownloadCsarManager {

    private static final Logger LOG = LoggerFactory.getLogger(DownloadCsarManager.class);

    public static final int CACHE = 100 * 1024;

    private DownloadCsarManager() {
        // private constructor
    }

    /**
     * Download from given URL.
     *
     * @param url String
     * @return
     */
    public static String download(String url) {
        return download(url, null);
    }

    /**
     * Download from given URL to given file location.
     *
     * @param url String
     * @param filepath String
     * @return
     */
    public static String download(String url, String filepath) {
        String status = "";
        try (CloseableHttpClient client = HttpClients.createDefault()){
            HttpGet httpget = new HttpGet(url);
            CloseableHttpResponse response = client.execute(httpget);

            HttpEntity entity = response.getEntity();
            try(InputStream is = entity.getContent()) {
                if(filepath == null) {
                    filepath = getFilePath(response); // NOSONAR
                }

                File file = new File(filepath);
                file.getParentFile().mkdirs();
                try(FileOutputStream fileout = new FileOutputStream(file)){

                    byte[] buffer = new byte[CACHE];
                    int ch;
                    while((ch = is.read(buffer)) != -1) {
                        fileout.write(buffer, 0, ch);
                    }
                    fileout.flush();
                    status = Constant.DOWNLOADCSAR_SUCCESS;
                } catch(Exception e) {
                    status = Constant.DOWNLOADCSAR_FAIL;
                    LOG.error("Download csar file failed! " + e.getMessage(), e);
                }
            } catch(Exception e) {
                status = Constant.DOWNLOADCSAR_FAIL;
                LOG.error("Download csar file failed! " + e.getMessage(), e);
            }
        } catch(Exception e) {
            status = Constant.DOWNLOADCSAR_FAIL;
            LOG.error("Download csar file failed! " + e.getMessage(), e);
        }

        return status;
    }

    /**
     * Retrieve file path from given response.
     *
     * @param response HttpResponse
     * @return
     */
    public static String getFilePath(HttpResponse response) {
        String filepath = System.getProperty("java.home");
        String filename = getFileName(response);

        if(filename != null) {
            filepath += filename;
        } else {
            filepath += getRandomFileName();
        }
        return filepath;
    }

    /**
     * Retrieve file name from given response.
     *
     * @param response HttpResponse
     * @return
     */
    public static String getFileName(HttpResponse response) {
        Header contentHeader = response.getFirstHeader("Content-Disposition");
        String filename = null;
        if(contentHeader != null) {
            HeaderElement[] values = contentHeader.getElements();
            if(values.length == 1) {
                NameValuePair param = values[0].getParameterByName("filename");
                if(param != null) {
                    try {
                        filename = param.getValue();
                    } catch(Exception e) {
                        LOG.error("getting filename failed! " + e.getMessage(), e);
                    }
                }
            }
        }
        return filename;
    }

    /**
     * Provides random file name.
     *
     * @return
     */
    public static String getRandomFileName() {
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * unzip CSAR packge
     *
     * @param fileName filePath
     * @return
     * @throws IOException
     */
    public static int unzipCSAR(String fileName, String filePath) {
        final int BUFFER = 2048;
        int status = Constant.UNZIP_SUCCESS;

        try(ZipFile zipFile = new ZipFile(fileName);) {
            Enumeration emu = zipFile.entries();
            while(emu.hasMoreElements()) {
                ZipEntry entry = (ZipEntry)emu.nextElement();
                // read directory as file first,so only need to create directory
                if(entry.isDirectory()) {
                    new File(filePath + entry.getName()).mkdirs();
                    continue;
                }
                BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
                File file = new File(filePath + entry.getName());
                // Because that is random to read zipfile,maybe the file is read first
                // before the directory is read,so we need to create directory first.
                File parent = file.getParentFile();
                if(parent != null && (!parent.exists())) {
                    parent.mkdirs();
                }
                try(FileOutputStream fos = new FileOutputStream(file);
                    BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER);){
                    int count;
                    byte data[] = new byte[BUFFER];
                    while((count = bis.read(data, 0, BUFFER)) != -1) {
                        bos.write(data, 0, count);
                    }
                    bos.flush();
                }
            }
        } catch(Exception e) {
            status = Constant.UNZIP_FAIL;
            LOG.error("Exception: " + e);
        }
        return status;
    }
}
