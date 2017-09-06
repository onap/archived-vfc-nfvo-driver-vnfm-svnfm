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

import java.util.Map;

/**
 * Response for RestFul requests.<br/>
 * <p>
 * </p>
 * 
 * @author
 * @version 28-May-2016
 */
public class RestfulResponse {

    private String responseContent;

    private int status = -1;

    private Map<String, String> respHeaderMap = null;

    /**
     * <br/>
     * 
     * @return
     * @since
     */
    public int getStatus() {
        return status;
    }

    /**
     * <br/>
     * 
     * @param status
     * @since
     */
    public void setStatus(final int status) {
        this.status = status;
    }

    /**
     * <br/>
     * 
     * @return
     * @since
     */
    public Map<String, String> getRespHeaderMap() {
        return respHeaderMap;
    }

    /**
     * <br/>
     * 
     * @param header
     * @since
     */
    public void setRespHeaderMap(final Map<String, String> header) {
        this.respHeaderMap = header;
    }

    /**
     * Get response header value as integer.<br/>
     * 
     * @param key header param name.
     * @return header param value as integer. (-1 if error)
     * @since
     */
    public int getRespHeaderInt(final String key) {
        if(respHeaderMap != null) {
            final String result = respHeaderMap.get(key);
            if(result != null) {
                return Integer.parseInt(result);
            }
        }
        return -1;
    }

    /**
     * Get response header value as long.<br/>
     * 
     * @param key header param name.
     * @return value as long. -1 if no value.
     * @since
     */
    public long getRespHeaderLong(final String key) {
        if(respHeaderMap != null) {
            final String result = respHeaderMap.get(key);
            if(result != null) {
                return Long.parseLong(result);
            }
        }
        return -1;
    }

    /**
     * Get http header as string.<br/>
     * 
     * @param key header name.
     * @return header value.
     * @since
     */
    public String getRespHeaderStr(final String key) {
        if(respHeaderMap != null) {
            return respHeaderMap.get(key);
        }
        return null;
    }

    /**
     * <br/>
     * 
     * @return
     * @since
     */
    public String getResponseContent() {
        return responseContent;
    }

    /**
     * <br/>
     * 
     * @param responseString
     * @since
     */
    public void setResponseJson(final String responseString) {
        this.responseContent = responseString;
    }
}
