/*
 * Copyright 2016 Huawei Technologies Co., Ltd.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONObject;

/**
 * Rest client options.<br/>
 * <p>
 * </p>
 * 
 * @author
 * @version 28-May-2016
 */
public class RestfulConfigure {

    private static final Logger LOG = LoggerFactory.getLogger(RestfulConfigure.class);

    private RestfulOptions options = null;

    /**
     * Constructor<br/>
     * <p>
     * Use the default path initialization http Rest options.
     * </p>
     * 
     * @since
     */
    public RestfulConfigure() {
        final String config = "/etc/conf/restclient.json";
        final String approot = SystemEnvVariablesFactory.getInstance().getAppRoot();
        final StringBuilder extendSetting = new StringBuilder();

        if(approot != null) {
            extendSetting.append(approot);
            extendSetting.append(config);
        } else {
            extendSetting.append(System.getProperty("user.dir"));
            extendSetting.append(config);
        }

        final String configfile = extendSetting.toString();
        initRestConf(configfile);
    }

    /**
     * Constructor<br/>
     * <p>
     * Use the specified file to initialize http Rest options.
     * </p>
     * 
     * @since
     * @param configfile
     */
    public RestfulConfigure(final String configfile) {
        initRestConf(configfile);
    }

    public RestfulOptions getOptions() {
        return options;
    }

    private void initRestConf(final String configfile) {
        options = getDefaultOptions();

        final JSONObject obj = loadJsonFromFile(configfile);
        if(obj != null) {
            if(obj.has(RestfulClientConst.SERVER_KEY_NAME)) {
                final JSONObject server = obj.getJSONObject(RestfulClientConst.SERVER_KEY_NAME);
                setStringOption(server, RestfulClientConst.HOST_KEY_NAME);
                setIntOption(server, RestfulClientConst.PORT_KEY_NAME);
            }
            setIntOption(obj, RestfulClientConst.CONN_TIMEOUT_KEY_NAME);
            setIntOption(obj, RestfulClientConst.THREAD_KEY_NAME);
            setIntOption(obj, RestfulClientConst.IDLE_TIMEOUT_KEY_NAME);
            setIntOption(obj, RestfulClientConst.TIMEOUT_KEY_NAME);
            setIntOption(obj, RestfulClientConst.MAX_CONN_PER_ADDR_KEY_NAME);
            setIntOption(obj, RestfulClientConst.MAX_RESPONSE_HEADER_SIZE);
            setIntOption(obj, RestfulClientConst.MAX_REQUEST_HEADER_SIZE);
        } else {
            LOG.error("failed to load json from " + configfile);
        }
    }

    private void setStringOption(final JSONObject json, final String key) {
        if(json.has(key)) {
            options.setOption(key, json.getString(key));
        }
    }

    private void setIntOption(final JSONObject json, final String key) {
        if(json.has(key)) {
            options.setOption(key, json.getInt(key));
        }
    }

    private RestfulOptions getDefaultOptions() {
        options = new RestfulOptions();
        options.setOption(RestfulClientConst.CONN_TIMEOUT_KEY_NAME, 3000);
        options.setOption(RestfulClientConst.THREAD_KEY_NAME, 200);
        options.setOption(RestfulClientConst.IDLE_TIMEOUT_KEY_NAME, 30000);
        options.setOption(RestfulClientConst.TIMEOUT_KEY_NAME, 30000);
        options.setOption(RestfulClientConst.MAX_CONN_PER_ADDR_KEY_NAME, 50);
        options.setOption(RestfulClientConst.MAX_RESPONSE_HEADER_SIZE, 20 * 1024);
        options.setOption(RestfulClientConst.MAX_REQUEST_HEADER_SIZE, 20 * 1024);
        return options;
    }

    private JSONObject loadJsonFromFile(final String filePath) {
        final File file = new File(filePath);
        if((!file.exists()) || (!file.isFile())) {
            LOG.error(filePath + "isn't exist.");
            return null;
        }

        final StringBuilder jsonstr = new StringBuilder();
        JSONObject jo = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(file));) {
            final ReaderHelper rHelpper = new ReaderHelper(reader);
            String tempString = null;
            while((tempString = rHelpper.getLine()) != null) {
                jsonstr.append(tempString);
            }
            jo = JSONObject.fromObject(jsonstr.toString());
        } catch(final IOException e) {
            LOG.error("load file exception:" + e);
        }
        return jo;
    }
}
