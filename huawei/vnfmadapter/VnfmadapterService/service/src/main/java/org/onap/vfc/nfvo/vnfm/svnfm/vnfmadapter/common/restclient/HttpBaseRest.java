/*
 * Copyright 2016-2017 Huawei Technologies Co., Ltd.
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

import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jetty.client.*;
import org.eclipse.jetty.client.api.*;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpVersion;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.constant.Constant;
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
    private Response responseGlobal;
    private ContentResponse contentResponse;
    private Request request;
    private static final Logger LOG = LoggerFactory.getLogger(HttpRest.class);

    final AtomicInteger requestId = new AtomicInteger(0);

    protected HttpClient client = null;

    static final String HTTP_PATCH = "PATCH";

    String defaultIP = Constant.LOCAL_HOST;
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

    public HttpBaseRest(final Response response) {
        this.responseGlobal = response;
    }

    protected void createHttpClient() {
        client = new HttpClient();
    }

    private String encodeParams(final RestfulParametes restParametes) throws ServiceException {
        final Map<String, String> parm = restParametes.getParamMap();
        String value = null;
        boolean bHasParma = false;
        final StringBuilder builder = new StringBuilder();
        try {
            for (final String key : parm.keySet()) {
                value = parm.get(key);
                if (value == null) {
                    value = "";
                }
                String str;
                if (bHasParma) {
                    str = String.format("&%s=%s", URLEncoder.encode(key, RestfulClientConst.ENCODING),
                            URLEncoder.encode(value, RestfulClientConst.ENCODING));
                } else {
                    bHasParma = true;
                    str = String.format("%s=%s", URLEncoder.encode(key, RestfulClientConst.ENCODING),
                            URLEncoder.encode(value, RestfulClientConst.ENCODING));
                }
                builder.append(str);
            }
        } catch (final UnsupportedEncodingException ex) {
            LOG.error("unsupported encoding: ", ex);
            throw new ServiceException("Broken VM does not support UTF-8");
        }
        return builder.toString();
    }

    private void processHeader(final Request request, final Map<String, String> headerMap) {
        for (final String key : headerMap.keySet()) {
            final String value = headerMap.get(key);
            HttpHeader headers[] = HttpHeader.values();
            if (Arrays.asList(headers).contains('"' + key + '"')) ;
            {
                request.header(key, value);

            }

        }

    }

    private void setRequestParams(final Request request, String httpMethod) {
        final String contentType = request.getHeaders().get("Content-Type");
        if (null == contentType || contentType.isEmpty()) {
            // application/json;charset=utf-8
            request.header(HttpHeader.CONTENT_TYPE, RestfulClientConst.APPLICATION_FORM_URLENCODED);
        }
        //final String encoding = contentExchange.getRequestFields().getStringField("Accept-Encoding");
        final String encoding = request.getHeaders().get("Accept-Encoding");
        if (null == encoding || encoding.isEmpty()) {
            // compress,gzip
            request.header(HttpHeader.ACCEPT_ENCODING, "*/*");
        }
        request.version(HttpVersion.HTTP_1_1);
        request.scheme("http");
        request.method(httpMethod);
    }


    /**
     * <br/>
     *
     * @param httpMethod
     * @param servicePath
     * @param restParametes
     * @param options
     * @param callback
     * @return
     * @throws ServiceException
     * @since
     */

    protected RestfulResponse sendHttpRequest(final String httpMethod, final String servicePath,
                                              final RestfulParametes restParametes, final RestfulOptions options, RestfulAsyncCallback callback)
            throws ServiceException {


        if (null == restParametes) {
            return new RestfulResponse();
        }
        final String requestTrace = this.getRequestIdString();
        restParametes.putHttpContextHeader(RestfulClientConst.REQUEST_ID, requestTrace);

        RestfulResponse rsp = null;
        try {
            final String str = encodeParams(restParametes);
            final StringBuilder builder = new StringBuilder();
            builder.append(servicePath);
            if (str.length() > 0 && (httpMethod.equals(HttpMethod.GET.asString()) || httpMethod.equals(HttpMethod.DELETE.asString())
                    || httpMethod.equals(HttpMethod.HEAD.asString()))) {
                builder.append('?');
                builder.append(str);
            }
            String url = setDefaultUrl(options, builder);
            System.out.println(url);
            request = client.newRequest(url);
            setRequestParams(request, httpMethod);
            processHeader(request, restParametes.getHeaderMap());
            setPostPutParam(httpMethod, restParametes, request, str);
            setTimeout(options, request);
            ContentResponse contentResponse = getResponse();
//            HttpRequestListeners httpRequestListeners = new HttpRequestListeners();
//            RestHttpContentExchange contentExchange = new RestHttpContentExchange();
//            Response.CompleteListener responseListener = contentExchange;
//            // Response.CompleteListener responseListener =f;
//            request.method(httpMethod)
//                    .onRequestSuccess(httpRequestListeners)
//                    .onRequestBegin(httpRequestListeners)
//                    .scheme("http")
//                    .send(responseListener);
//            Thread.sleep(2000);
//            System.out.println("content:- " + contentExchange._responseContentString);
//            System.out.println("code :-" + contentExchange._responseStatus);


//            Origin origin=new Origin("http","localhost",8980);
//            HttpDestination httpDestination=new HttpDestination(client,origin) {
//                @Override
//                protected SendFailure send(Connection connection, HttpExchange exchange) {
//                    return null;
//                }
//            };
//            List<Response.ResponseListener> listenersList=new ArrayList<>();
//            listenersList.add(responseListener);
//
//            HttpExchange httpExchange=new HttpExchange(httpDestination,(HttpRequest)request,listenersList);
//            System.out.println("httpExchange : "+httpExchange.getResponse().getStatus());
            //System.out.println("request :- " + httpRequestListeners.method);
            RestHttpContentExchange contentExchange = new RestHttpContentExchange();
            contentExchange.setResponseStatus(contentResponse.getStatus());
            contentExchange.setResponseContentBytes(contentResponse.getContent());
            contentExchange.setResponseFields(contentResponse.getHeaders());
            contentExchange.setResponseContentString(contentResponse.getContentAsString());
            rsp = callbackExecute(callback, contentExchange);
//            List<Response.Listener> list = contentResponse.getListeners(Response.Listener.class);
//            for (Response.Listener listener : list) {
//                System.out.println(listener.getClass());
//            }
            System.out.println("Testing :: " + contentResponse.getContentAsString());
            System.out.println("rsp::::::: " + rsp);
        } catch (final Exception e) {
            System.out.println("ex : " + e.getMessage());
            if(request!=null){
                LOG.error("request reply message have exception:status is "
                        + request.getAbortCause());
            }

            throw new ServiceException(e);
        }
        return rsp;
    }

    private ContentResponse getResponse() throws InterruptedException, ExecutionException, TimeoutException {
        return request.send();
    }


    private String setDefaultUrl(final RestfulOptions options, final StringBuilder url) {
        // server

        if (url.toString().startsWith("http")) {
            return url.toString();
        } else {
            String host = Constant.LOCAL_HOST;
            int iPort = defaultPort;
            if (options != null) {
                host = options.getHost();
                if (host.isEmpty()) {
                    host = defaultIP;
                }
                iPort = options.getPort();
                if (iPort == 0) {
                    iPort = defaultPort;
                }
            }
            // Integer.getInteger(".http.client.maxThread",30)
            return "http://" + host + ":" + iPort + "/" + url;
            //return "http://reqres.in/api/users/4";
            //return "https://jsonplaceholder.typicode.com/users"; //for 404 bad request

        }
    }

    private String getRequestIdString() {
        if (this.requestId.get() == 0x7FFFFFFF) {
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
                                 final Request request, final String str) throws UnsupportedEncodingException {
        if (HttpMethod.POST.equals(method) || HttpMethod.PUT.equals(method) || HTTP_PATCH.equals(method)) {
            ByteArrayInputStream buff;
            final String tmpRaw = restParametes.getRawData();
            if (tmpRaw == null) {
                buff = new ByteArrayInputStream(str.getBytes(RestfulClientConst.ENCODING));
            } else {
                buff = new ByteArrayInputStream(tmpRaw.getBytes(RestfulClientConst.ENCODING));
            }
            final int len = buff.available();
            //contentExchange.setRequestContentSource(buff);
            request.header("content-length", String.valueOf(len));
        }
    }

    private void setTimeout(final RestfulOptions options, final Request request) {
        if (options != null) {
            final long timeout = options.getRestTimeout();
            if (timeout != 0) {
                request.idleTimeout(timeout, TimeUnit.MILLISECONDS);
            } else {
                request.idleTimeout(defaultTimeout, TimeUnit.MILLISECONDS);
            }
        } else {
            request.idleTimeout(defaultTimeout, TimeUnit.MILLISECONDS);
        }
    }

    private RestfulResponse callbackExecute(final RestfulAsyncCallback callback,
                                            final RestHttpContentExchange contentExchange) throws ServiceException, IOException {
        if (callback == null) {
            int exchangeState = contentExchange.getResponse().getStatus();
            if (exchangeState == 200) {
                System.out.println("Restful Response " + contentExchange.getResponse().getResponseContent());
                return contentExchange.getResponse();
            } else {
                throw new ServiceException("status code : "+exchangeState);
            }
        }
        return null;
    }
}
