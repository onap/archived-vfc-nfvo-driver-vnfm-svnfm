/*
 * Copyright 2017 Huawei Technologies Co., Ltd.
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

package org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.restclient;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * System environment variable helper implementation.<br/>
 * <p>
 * </p>
 * 
 * @author
 * @version 24-Jun-2016
 */
public class SystemEnvVariablesDefImpl implements SystemEnvVariables {

    private static final Logger LOG = LoggerFactory.getLogger(SystemEnvVariablesDefImpl.class);

    @Override
    public String getAppRoot() {
        String appRoot = null;
        appRoot = System.getProperty("catalina.base");
        if(appRoot != null) {
            appRoot = getCanonicalPath(appRoot);
        }
        return appRoot;
    }

    /**
     * Gets the canonical path<br/>
     * 
     * @param inPath input path
     * @return the canonical path.
     * @since
     */
    private String getCanonicalPath(final String inPath) {
        String path = null;
        try {
            if(inPath != null) {
                final File file = new File(inPath);
                path = file.getCanonicalPath();
            }
        } catch(final IOException e) {
            LOG.error("file.getCanonicalPath() IOException:", e);
        }
        return path;
    }

}
