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
 * <br/>
 * <p>
 * </p>
 * 
 * @author
 * @version Aug 9, 2016
 */
public class RestfulParametes {

    private Map<String, String> paramMap = new HashMap<>();

    private Map<String, String> headerMap = new HashMap<>();

    private String rawData = null;

    /**
     * <br/>
     * 
     * @param key
     * @return
     * @since
     */
    public String get(final String key) {
        return paramMap.get(key);
    }

    /**
     * <br/>
     * 
     * @param data
     * @since
     */
    public void setRawData(final String data) {
        this.rawData = data;
    }

    /**
     * <br/>
     * 
     * @return
     * @since
     */
    public String getRawData() {
        return this.rawData;
    }

    /**
     * <br/>
     * 
     * @param key
     * @param value
     * @return
     * @since
     */
    public String put(final String key, final String value) {
        return paramMap.put(key, value);
    }

    /**
     * <br/>
     * 
     * @param key
     * @param value
     * @return
     * @since
     */
    public String putHttpContextHeader(final String key, final String value) {
        return headerMap.put(key, value);
    }

    /**
     * <br/>
     * 
     * @param key
     * @param value
     * @return
     * @since
     */
    public String putHttpContextHeader(final String key, final int value) {
        return this.putHttpContextHeader(key, String.valueOf(value));
    }

    /**
     * <br/>
     * 
     * @param key
     * @return
     * @since
     */
    public String getHttpContextHeader(final String key) {
        return headerMap.get(key);
    }

    /**
     * <br/>
     * 
     * @return
     * @since
     */
    public Map<String, String> getParamMap() {
        return paramMap;
    }

    /**
     * <br/>
     * 
     * @param paramMap
     * @since
     */
    public void setParamMap(final Map<String, String> paramMap) {
        this.paramMap = paramMap;
    }

    /**
     * <br/>
     * 
     * @return
     * @since
     */
    public Map<String, String> getHeaderMap() {
        return this.headerMap;
    }

    /**
     * <br/>
     * 
     * @param headerMap
     * @since
     */
    public void setHeaderMap(final Map<String, String> headerMap) {
        this.headerMap = headerMap;
    }
}
