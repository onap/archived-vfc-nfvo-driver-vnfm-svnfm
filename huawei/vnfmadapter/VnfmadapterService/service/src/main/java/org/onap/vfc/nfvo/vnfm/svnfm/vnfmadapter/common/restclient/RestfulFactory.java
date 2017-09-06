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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RestFul instance factory. <br/>
 * <p>
 * </p>
 * 
 * @author
 * @version 28-May-2016
 */
public class RestfulFactory {

    /**
     * https protocol.
     */
    public static final String PROTO_HTTPS = "https";

    /**
     * http protocol.
     */
    public static final String PROTO_HTTP = "http";

    private static final Logger LOG = LoggerFactory.getLogger(RestfulFactory.class);

    private static final Map<String, Restful> INSTANCES = new HashMap<>(2);

    private RestfulFactory() {

    }

    /**
     * Get RESTful instance. This method returns a singleton instance.
     * <br/>
     * 
     * @param protocol protocol. currently only support 'http'.
     * @return restful instance.
     * @since
     */
    public static synchronized Restful getRestInstance(final String protocol) {
        Restful rest = INSTANCES.get(protocol);
        if(rest != null) {
            return rest;
        }
        if(PROTO_HTTP.equals(protocol)) {
            rest = createHttpRest();
            INSTANCES.put(protocol, rest);
        }
        return rest;
    }

    private static Restful createHttpRest() {
        final HttpRest rest = new HttpRest();
        setRestOption(rest, null);
        return rest;
    }

    private static void setRestOption(final HttpRest rest, final String restoptionfile) {
        try {
            RestfulConfigure config;
            if(restoptionfile == null || restoptionfile.isEmpty()) {
                config = new RestfulConfigure();
            } else {
                config = new RestfulConfigure(restoptionfile);
            }

            final RestfulOptions option = config.getOptions();
            rest.initHttpRest(option);
        } catch(final ServiceException e) {
            LOG.error("init http client exception: ", e);
        }
    }
}
