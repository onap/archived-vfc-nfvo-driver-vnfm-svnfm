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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.common.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CommonUtil {
	private static final Logger logger = LoggerFactory.getLogger(CommonUtil.class);
	
	public static String getJsonStrFromFile(String filePath) throws IOException {
		InputStream ins = null;
        BufferedInputStream bins = null;
        String fileContent = "";
        String fileName = getAppRoot() + filePath;

        try {
            ins = new FileInputStream(fileName);
            bins = new BufferedInputStream(ins);

            byte[] contentByte = new byte[ins.available()];
            int num = bins.read(contentByte);

            if(num > 0) {
                fileContent = new String(contentByte);
            }
        } catch(FileNotFoundException e) {
        	logger.error(fileName + " is not found!", e);
        } finally {
            if(ins != null) {
                ins.close();
            }
            if(bins != null) {
                bins.close();
            }
        }
		return fileContent;
	}
	
	public static String getAppRoot() {
        String appRoot = System.getProperty("catalina.base");
        if(appRoot != null) {
            appRoot = getCanonicalPath(appRoot);
        }
        return appRoot;
    }

    private static String getCanonicalPath(final String inPath) {
        String path = null;
        try {
            if(inPath != null) {
                final File file = new File(inPath);
                path = file.getCanonicalPath();
            }
        } catch(final IOException e) {
            logger.error("file.getCanonicalPath() IOException:", e);
        }
        return path;
    }
    
    public static byte[] getBytes(String filePath){  
        byte[] buffer = null;  
        try {  
            File file = new File(filePath);  
            FileInputStream fis = new FileInputStream(file);  
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);  
            byte[] b = new byte[1000];  
            int n;  
            while ((n = fis.read(b)) != -1) {  
                bos.write(b, 0, n);  
            }  
            fis.close();  
            bos.close();  
            buffer = bos.toByteArray();  
        } catch (FileNotFoundException e) {  
        	logger.error("file " + filePath + " is not found.", e);
        } catch (IOException e) {  
        	logger.error("file " + filePath + " IOException.", e); 
        }  
        return buffer;  
    }  
 
}
