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

import java.util.HashMap;
import java.util.Map;

/**
 * Options for Rest communication.<br/>
 * <p>
 * </p>
 * 
 * @author
 * @version 28-May-2016
 */
public class RestfulOptions {

    public static final String REST_OPTIONS_NAME_TIMEOUT = "timeout";

    public static final int REST_OPTIONS_TIMEOUT_MAXTIMEOUT = 1800000;

    private final Map<String, Object> optionsMap = new HashMap<>();

    /**
     * Get port.<br/>
     * 
     * @return port.
     * @since
     */
    public int getPort() {
        final Object obj = this.getOption(RestfulClientConst.PORT_KEY_NAME);
        if(null == obj) {
            return 0;
        }
        return ((Integer)obj).intValue();
    }

    /**
     * Set port.<br/>
     * 
     * @param port port to set.
     * @return
     * @since
     */
    public boolean setPort(final int port) {
        this.setOption(RestfulClientConst.PORT_KEY_NAME, port);
        return true;
    }

    /**
     * Get host.<br/>
     * 
     * @return the host.
     * @since
     */
    public String getHost() {
        final Object obj = this.getOption(RestfulClientConst.HOST_KEY_NAME);
        if(null == obj) {
            return "";
        }
        return (String)obj;
    }

    /**
     * Set host.<br/>
     * 
     * @param host host to set.
     * @return
     * @since
     */
    public boolean setHost(final String host) {
        this.setOption(RestfulClientConst.HOST_KEY_NAME, host);
        return true;
    }

    /**
     * Set rest time-out.<br/>
     * 
     * @param timeout time-out to set in seconds.
     * @return
     * @since
     */
    public boolean setRestTimeout(final int timeout) {
        if(0 < timeout && REST_OPTIONS_TIMEOUT_MAXTIMEOUT >= timeout) {
            this.setOption(REST_OPTIONS_NAME_TIMEOUT, timeout);
            return true;
        }
        return false;
    }

    /**
     * Get time-out.<br/>
     * 
     * @return time-out in seconds.
     * @since
     */
    public int getRestTimeout() {
        final Object obj = this.getOption(REST_OPTIONS_NAME_TIMEOUT);
        if(null == obj) {
            return 0;
        }
        return ((Integer)obj).intValue();
    }

    /**
     * Get specified option.<br/>
     * 
     * @param optionName option name.
     * @return option
     * @since
     */
    public Object getOption(final String optionName) {
        return optionsMap.get(optionName);
    }

    /**
     * Get option value as integer.<br/>
     * 
     * @param optionName option name.
     * @return option value as int.
     * @since
     */
    public int getIntOption(final String optionName) {
        final Object obj = this.getOption(optionName);
        if(null == obj) {
            return 0;
        }
        return ((Integer)obj).intValue();
    }

    /**
     * Get option value as string.<br/>
     * 
     * @param optionName option name.
     * @return option value as string.
     * @since
     */
    public String getStringOption(final String optionName) {
        final Object obj = this.getOption(optionName);
        if(null == obj) {
            return "";
        }
        return (String)obj;
    }

    /**
     * Set option.<br/>
     * 
     * @param option option name.
     * @param optionsValue option value.
     * @return
     * @since
     */
    public Object setOption(final String option, final Object optionsValue) {
        return optionsMap.put(option, optionsValue);
    }
}
