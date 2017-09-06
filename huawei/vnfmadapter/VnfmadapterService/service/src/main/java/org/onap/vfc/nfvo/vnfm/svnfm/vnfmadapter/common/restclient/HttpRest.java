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

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <br/>
 * <p>
 * </p>
 * 
 * @author
 * @version Aug 9, 2016
 */
public class HttpRest extends HttpBaseRest {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRest.class);

    /**
     * Initializing Rest options.<br/>
     * 
     * @param options: rest options.
     * @throws ServiceException
     * @since
     */
    public void initHttpRest(final RestfulOptions option) throws ServiceException {
        if(option == null) {
            client = null;
            throw new ServiceException("option is null.");
        }
        createHttpClient();
        try {
            int iValue;
            iValue = option.getIntOption(RestfulClientConst.MAX_CONN_PER_ADDR_KEY_NAME);
            // max 200 concurrent,connections to every address
            client.setMaxConnectionsPerAddress(iValue);

            iValue = option.getIntOption(RestfulClientConst.THREAD_KEY_NAME);
            // max threads
            client.setThreadPool(new QueuedThreadPool(iValue));
            iValue = option.getIntOption(RestfulClientConst.CONN_TIMEOUT_KEY_NAME);
            client.setConnectTimeout(iValue);
            iValue = option.getRestTimeout();
            defaultTimeout = iValue;
            client.setTimeout(iValue);

            iValue = option.getIntOption(RestfulClientConst.IDLE_TIMEOUT_KEY_NAME);
            client.setIdleTimeout(iValue);
            iValue = option.getIntOption(RestfulClientConst.MAX_RESPONSE_HEADER_SIZE);
            client.setResponseHeaderSize(iValue);
            iValue = option.getIntOption(RestfulClientConst.MAX_REQUEST_HEADER_SIZE);
            client.setRequestHeaderSize(iValue);
            // HttpClient.CONNECTOR_SOCKET
            client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
            client.start();
            defaultIP = option.getStringOption(RestfulClientConst.HOST_KEY_NAME);
            defaultPort = option.getIntOption(RestfulClientConst.PORT_KEY_NAME);
        } catch(final Exception e) {
            LOG.error("start httpclient error", e);
            client = null;
            throw new ServiceException("http client init failed.");
        }
    }

    @Override
    public RestfulResponse get(final String servicePath, final RestfulParametes restParametes) throws ServiceException {
        return this.sendHttpRequest(HttpMethods.GET, servicePath, restParametes, null, null);
    }

    @Override
    public RestfulResponse get(final String servicePath, final RestfulParametes restParametes,
            final RestfulOptions option) throws ServiceException {
        return this.sendHttpRequest(HttpMethods.GET, servicePath, restParametes, option, null);
    }

    @Override
    public RestfulResponse head(final String servicePath, final RestfulParametes restParametes)
            throws ServiceException {
        return this.sendHttpRequest(HttpMethods.HEAD, servicePath, restParametes, null, null);
    }

    @Override
    public RestfulResponse head(final String servicePath, final RestfulParametes restParametes,
            final RestfulOptions option) throws ServiceException {
        return this.sendHttpRequest(HttpMethods.HEAD, servicePath, restParametes, option, null);
    }

    @Override
    public void asyncGet(final String servicePath, final RestfulParametes restParametes,
            final RestfulAsyncCallback callback) throws ServiceException {
        if(callback == null) {
            this.sendHttpRequest(HttpMethods.GET, servicePath, restParametes, null, new DefaultAsyncCallback());
        } else {
            this.sendHttpRequest(HttpMethods.GET, servicePath, restParametes, null, callback);
        }
    }

    @Override
    public void asyncGet(final String servicePath, final RestfulParametes restParametes, final RestfulOptions option,
            final RestfulAsyncCallback callback) throws ServiceException {
        if(callback == null) {
            this.sendHttpRequest(HttpMethods.GET, servicePath, restParametes, option, new DefaultAsyncCallback());
        } else {
            this.sendHttpRequest(HttpMethods.GET, servicePath, restParametes, option, callback);
        }
    }

    @Override
    public RestfulResponse put(final String servicePath, final RestfulParametes restParametes) throws ServiceException {
        return this.sendHttpRequest(HttpMethods.PUT, servicePath, restParametes, null, null);
    }

    @Override
    public RestfulResponse put(final String servicePath, final RestfulParametes restParametes,
            final RestfulOptions option) throws ServiceException {
        return this.sendHttpRequest(HttpMethods.PUT, servicePath, restParametes, option, null);
    }

    @Override
    public void asyncPut(final String servicePath, final RestfulParametes restParametes,
            final RestfulAsyncCallback callback) throws ServiceException {
        if(callback == null) {
            this.sendHttpRequest(HttpMethods.PUT, servicePath, restParametes, null, new DefaultAsyncCallback());
        } else {
            this.sendHttpRequest(HttpMethods.PUT, servicePath, restParametes, null, callback);
        }
    }

    @Override
    public void asyncPut(final String servicePath, final RestfulParametes restParametes, final RestfulOptions option,
            final RestfulAsyncCallback callback) throws ServiceException {
        if(callback == null) {
            this.sendHttpRequest(HttpMethods.PUT, servicePath, restParametes, option, new DefaultAsyncCallback());
        } else {
            this.sendHttpRequest(HttpMethods.PUT, servicePath, restParametes, option, callback);
        }
    }

    @Override
    public RestfulResponse post(final String servicePath, final RestfulParametes restParametes)
            throws ServiceException {
        return this.sendHttpRequest(HttpMethods.POST, servicePath, restParametes, null, null);
    }

    @Override
    public RestfulResponse post(final String servicePath, final RestfulParametes restParametes,
            final RestfulOptions option) throws ServiceException {
        return this.sendHttpRequest(HttpMethods.POST, servicePath, restParametes, option, null);
    }

    @Override
    public void asyncPost(final String servicePath, final RestfulParametes restParametes,
            final RestfulAsyncCallback callback) throws ServiceException {
        if(callback == null) {
            this.sendHttpRequest(HttpMethods.POST, servicePath, restParametes, null, new DefaultAsyncCallback());
        } else {
            this.sendHttpRequest(HttpMethods.POST, servicePath, restParametes, null, callback);
        }
    }

    @Override
    public void asyncPost(final String servicePath, final RestfulParametes restParametes, final RestfulOptions option,
            final RestfulAsyncCallback callback) throws ServiceException {
        if(callback == null) {
            this.sendHttpRequest(HttpMethods.POST, servicePath, restParametes, option, new DefaultAsyncCallback());
        } else {
            this.sendHttpRequest(HttpMethods.POST, servicePath, restParametes, option, callback);
        }
    }

    @Override
    public RestfulResponse delete(final String servicePath, final RestfulParametes restParametes)
            throws ServiceException {
        return this.sendHttpRequest(HttpMethods.DELETE, servicePath, restParametes, null, null);
    }

    @Override
    public RestfulResponse delete(final String servicePath, final RestfulParametes restParametes,
            final RestfulOptions option) throws ServiceException {
        return this.sendHttpRequest(HttpMethods.DELETE, servicePath, restParametes, option, null);
    }

    @Override
    public void asyncDelete(final String servicePath, final RestfulParametes restParametes,
            final RestfulAsyncCallback callback) throws ServiceException {
        if(callback == null) {
            this.sendHttpRequest(HttpMethods.DELETE, servicePath, restParametes, null, new DefaultAsyncCallback());
        } else {
            this.sendHttpRequest(HttpMethods.DELETE, servicePath, restParametes, null, callback);
        }
    }

    @Override
    public void asyncDelete(final String servicePath, final RestfulParametes restParametes, final RestfulOptions option,
            final RestfulAsyncCallback callback) throws ServiceException {
        if(callback == null) {
            this.sendHttpRequest(HttpMethods.DELETE, servicePath, restParametes, option, new DefaultAsyncCallback());
        } else {
            this.sendHttpRequest(HttpMethods.DELETE, servicePath, restParametes, option, callback);
        }
    }

    @Override
    public RestfulResponse patch(final String servicePath, final RestfulParametes restParametes)
            throws ServiceException {
        return this.sendHttpRequest(HTTP_PATCH, servicePath, restParametes, null, null);
    }

    @Override
    public RestfulResponse patch(final String servicePath, final RestfulParametes restParametes,
            final RestfulOptions option) throws ServiceException {
        return this.sendHttpRequest(HTTP_PATCH, servicePath, restParametes, option, null);
    }

    @Override
    public void asyncPatch(final String servicePath, final RestfulParametes restParametes,
            final RestfulAsyncCallback callback) throws ServiceException {
        if(callback == null) {
            this.sendHttpRequest(HTTP_PATCH, servicePath, restParametes, null, new DefaultAsyncCallback());
        } else {
            this.sendHttpRequest(HTTP_PATCH, servicePath, restParametes, null, callback);
        }
    }

    @Override
    public void asyncPatch(final String servicePath, final RestfulParametes restParametes, final RestfulOptions option,
            final RestfulAsyncCallback callback) throws ServiceException {
        if(callback == null) {
            this.sendHttpRequest(HTTP_PATCH, servicePath, restParametes, option, new DefaultAsyncCallback());
        } else {
            this.sendHttpRequest(HTTP_PATCH, servicePath, restParametes, option, callback);
        }
    }

}
