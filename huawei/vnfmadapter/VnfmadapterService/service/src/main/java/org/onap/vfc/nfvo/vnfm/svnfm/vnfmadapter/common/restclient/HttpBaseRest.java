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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jetty.client.Address;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.http.HttpMethods;
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
public abstract class HttpBaseRest implements Restful {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRest.class);

    final AtomicInteger requestId = new AtomicInteger(0);

    protected HttpClient client = null;

    private static final String LOCAL_HOST = "127.0.0.1";

    static final String HTTP_PATCH = "PATCH";

    String defaultIP = LOCAL_HOST;

    int defaultPort = -10000;

    int defaultTimeout = 30000;

    final String procenameRouteID = "RouteID-" + System.currentTimeMillis() + "-";

    /**
     * Constructor<br/>
     * <p>
     * </p>
     * 
     * @since
     */
    public HttpBaseRest() {
        super();
    }

    protected void createHttpClient() {
        client = new HttpClient();
    }

    protected RestHttpContentExchange createRestHttpContentExchange(final RestfulAsyncCallback callback) {
        final RestHttpContentExchange exchange = new RestHttpContentExchange(true, callback);
        exchange.setScheme("http");
        return exchange;
    }

    private String encodeParams(final RestfulParametes restParametes) throws ServiceException {
        final Map<String, String> parm = restParametes.getParamMap();
        String value = null;
        boolean bHasParma = false;
        final StringBuilder builder = new StringBuilder();
        try {
            for(final String key : parm.keySet()) {
                value = parm.get(key);
                if(value == null) {
                    value = "";
                }
                String str;
                if(bHasParma) {
                    str = String.format("&%s=%s", URLEncoder.encode(key, RestfulClientConst.ENCODING),
                            URLEncoder.encode(value, RestfulClientConst.ENCODING));
                } else {
                    bHasParma = true;
                    str = String.format("%s=%s", URLEncoder.encode(key, RestfulClientConst.ENCODING),
                            URLEncoder.encode(value, RestfulClientConst.ENCODING));
                }
                builder.append(str);
            }
        } catch(final UnsupportedEncodingException ex) {
            LOG.error("unsupported encoding: ", ex);
            throw new ServiceException("Broken VM does not support UTF-8");
        }
        return builder.toString();
    }

    private void processHeader(final RestHttpContentExchange contentExchange, final Map<String, String> headerMap) {
        for(final String key : headerMap.keySet()) {
            final String value = headerMap.get(key);
            contentExchange.addRequestHeader(key, value);
        }

    }

    private void setContentExchangeParams(final RestHttpContentExchange contentExchange) {
        final String contentType = contentExchange.getRequestFields().getStringField("Content-Type");
        if(null == contentType || contentType.isEmpty()) {
            // application/json;charset=utf-8
            contentExchange.setRequestContentType(RestfulClientConst.APPLICATION_FORM_URLENCODED);
        }
        final String encoding = contentExchange.getRequestFields().getStringField("Accept-Encoding");
        if(null == encoding || encoding.isEmpty()) {
            // compress,gzip
            contentExchange.setRequestHeader("Accept-Encoding", "*/*");
        }
        contentExchange.setVersion(11);
    }

    /**
     * <br/>
     * 
     * @param method
     * @param servicePath
     * @param restParametes
     * @param options
     * @param callback
     * @return
     * @throws ServiceException
     * @since
     */
    protected RestfulResponse sendHttpRequest(final String method, final String servicePath,
            final RestfulParametes restParametes, final RestfulOptions options, final RestfulAsyncCallback callback)
            throws ServiceException {
        final RestHttpContentExchange contentExchange = createRestHttpContentExchange(callback);
        if(null == restParametes) {
            return new RestfulResponse();
        }
        final String requestTrace = this.getReuqestIdString();
        restParametes.putHttpContextHeader(RestfulClientConst.REQUEST_ID, requestTrace);

        RestfulResponse rsp = null;
        try {
            contentExchange.setMethod(method);
            final String str = encodeParams(restParametes);
            final StringBuilder builder = new StringBuilder();
            builder.append(servicePath);
            if(str.length() > 0 && (method.equals(HttpMethods.GET) || method.equals(HttpMethods.DELETE)
                    || method.equals(HttpMethods.HEAD))) {
                builder.append('?');
                builder.append(str);
            }
            setDefaultUrl(contentExchange, options, builder);
            processHeader(contentExchange, restParametes.getHeaderMap());
            setContentExchangeParams(contentExchange);

            setPostPutParam(method, restParametes, contentExchange, str);
            setTimeout(options, contentExchange);

            client.send(contentExchange);
            rsp = callbackExecute(callback, contentExchange);
        } catch(final Exception e) {
            LOG.error("request reply message have exception:status is "
                    + RestHttpContentExchange.toState(contentExchange.getStatus()));
            throw new ServiceException(e);
        }
        return rsp;
    }

    private void setDefaultUrl(final RestHttpContentExchange contentExchange, final RestfulOptions options,
            final StringBuilder url) {
        // server
        if(url.toString().startsWith("http")) {
            contentExchange.setURL(url.toString());
        } else {
            String host = defaultIP;
            int iPort = defaultPort;
            if(options != null) {
                host = options.getHost();
                if(host.isEmpty()) {
                    host = defaultIP;
                }
                iPort = options.getPort();
                if(iPort == 0) {
                    iPort = defaultPort;
                }
            }
            // Integer.getInteger(".http.client.maxThread",30)
            contentExchange.setAddress(new Address(host, iPort));
            contentExchange.setRequestURI(url.toString());
        }
    }

    private String getReuqestIdString() {
        if(this.requestId.get() == 0x7FFFFFFF) {
            this.requestId.set(1);
        }
        final int reqId = this.requestId.getAndIncrement();
        final StringBuilder builder = new StringBuilder(this.procenameRouteID);
        // time
        final SimpleDateFormat dateFormate = new SimpleDateFormat("yyMMdd");
        final SimpleDateFormat timeFormate = new SimpleDateFormat("HHmmss");
        final Date date = Calendar.getInstance().getTime();
        builder.append(dateFormate.format(date) + timeFormate.format(date));
        builder.append('-');
        builder.append(reqId);
        return builder.toString();
    }

    private void setPostPutParam(final String method, final RestfulParametes restParametes,
            final RestHttpContentExchange contentExchange, final String str) throws UnsupportedEncodingException {
        if(HttpMethods.POST.equals(method) || HttpMethods.PUT.equals(method) || HTTP_PATCH.equals(method)) {
            ByteArrayInputStream buff;
            final String tmpRaw = restParametes.getRawData();
            if(tmpRaw == null) {
                buff = new ByteArrayInputStream(str.getBytes(RestfulClientConst.ENCODING));
            } else {
                buff = new ByteArrayInputStream(tmpRaw.getBytes(RestfulClientConst.ENCODING));
            }
            final int len = buff.available();
            contentExchange.setRequestContentSource(buff);
            contentExchange.setRequestHeader("content-length", String.valueOf(len));
        }
    }

    private void setTimeout(final RestfulOptions options, final RestHttpContentExchange contentExchange) {
        if(options != null) {
            final long timeout = options.getRestTimeout();
            if(timeout != 0) {
                contentExchange.setTimeout(timeout);
            } else {
                contentExchange.setTimeout(defaultTimeout);
            }
        } else {
            contentExchange.setTimeout(defaultTimeout);
        }
    }

    private RestfulResponse callbackExecute(final RestfulAsyncCallback callback,
            final RestHttpContentExchange contentExchange) throws InterruptedException, IOException, ServiceException {
        if(callback == null) {
            final int exchangeState = contentExchange.waitForDone();
            if(exchangeState == HttpExchange.STATUS_COMPLETED) {
                return contentExchange.getResponse();
            } else if(exchangeState == HttpExchange.STATUS_EXCEPTED) {
                throw new ServiceException(
                        "request is exception: " + RestHttpContentExchange.toState(HttpExchange.STATUS_EXCEPTED));
            } else if(exchangeState == HttpExchange.STATUS_EXPIRED) {
                throw new ServiceException(
                        "request is expierd: " + RestHttpContentExchange.toState(HttpExchange.STATUS_EXPIRED));
            }
        }
        return null;
    }

}
