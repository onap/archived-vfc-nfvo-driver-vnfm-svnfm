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

/**
 * Constants for ReST client.<br/>
 * <p>
 * </p>
 * 
 * @author
 * @version 28-May-2016
 */
public class RestfulClientConst {

    /** -- json Ecode -- **/
    public static final String APPLICATION_FORM_JSON_EBCIDED = "application/json";

    /**
     * urlencode
     */
    public static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded; charset=UTF-8";

    public static final String ENCODING = "UTF-8";

    public static final String SERVER_KEY_NAME = "defaultServer";

    public static final String HOST_KEY_NAME = "host";

    public static final String PORT_KEY_NAME = "port";

    public static final String CONN_TIMEOUT_KEY_NAME = "ConnectTimeout";

    public static final String THREAD_KEY_NAME = "thread";

    public static final String IDLE_TIMEOUT_KEY_NAME = "idletimeout";

    public static final String TIMEOUT_KEY_NAME = "timeout";

    public static final String MAX_CONN_PER_ADDR_KEY_NAME = "maxConnectionPerAddr";

    public static final String REQUEST_ID = "x-request-id";

    public static final String MAX_RESPONSE_HEADER_SIZE = "responseHeaderSize";

    public static final String MAX_REQUEST_HEADER_SIZE = "requestHeaderSize";

    private RestfulClientConst() {

    }
}
