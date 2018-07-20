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
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpDestination;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ContentExchange implementation classe to provide access to response.
 * <br/>
 * <p>
 * </p>
 * 
 * @author
 * @version 28-May-2016
 */
public class RestHttpContentExchange extends ContentExchange {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestHttpContentExchange.class);

    private static final String PATH = "path:";

    private boolean gzip = false;

    private RestfulAsyncCallback callback = null;

    /**
     * Constructor<br/>
     * <p>
     * </p>
     * 
     * @since
     * @param cacheFields whether to cache response header.
     * @param asyncCallback callback method.
     */
    RestHttpContentExchange(final boolean cacheFields, final RestfulAsyncCallback asyncCallback) {
        super(cacheFields);
        this.callback = asyncCallback;
    }

    /**
     * Extract message.
     * <br/>
     * 
     * @param data GZipped data.
     * @return Uncompressed data.
     * @throws IOException
     * @since
     */
    public String decompressGzipToStr(final byte[] data) throws IOException {
        if(data == null) {
            return "";
        }
        final StringBuilder out = new StringBuilder();
        try (ByteArrayInputStream input = new ByteArrayInputStream(data);
             GZIPInputStream gzis = new GZIPInputStream(input);
             InputStreamReader reader = new InputStreamReader(gzis, Charset.forName(RestfulClientConst.ENCODING));) {
            final char[] buff = new char[1024];
            for(int n; (n = reader.read(buff)) != -1;) {
                out.append(new String(buff, 0, n));
            }
        }
        return out.toString();

    }

    /**
     * View response headers Content-Encoding values if you need to extract data.<br/>
     * 
     * @param name buffer
     * @param value value
     * @throws IOException
     * @since
     */
    @Override
    protected synchronized void onResponseHeader(final Buffer name, final Buffer value) throws IOException {
        super.onResponseHeader(name, value);
        final int header = HttpHeaders.CACHE.getOrdinal(name);
        if(header == HttpHeaders.CONTENT_ENCODING_ORDINAL) {
            final String encoding = StringUtil.asciiToLowerCase(value.toString());
            gzip = encoding != null && StringUtils.contains(encoding, "gzip");
        }

    }

    @Override
    protected void onResponseComplete() throws IOException {
        if(LOGGER.isInfoEnabled()) {
            LOGGER.info("Response has Complete:" + PATH + this.getRequestURI().replace("\n", "0x0A"));
        }
        super.onResponseComplete();
        if(callback != null) {
            final RestfulResponse rsp = getResponse();
            callback.callback(rsp);
        }
    }

    @Override
    protected void onRequestCommitted() throws IOException {
        if(LOGGER.isInfoEnabled()) {
            LOGGER.info("Request Header has been send:" + PATH + this.getRequestURI().replace("\n", "0x0A"));
        }
        super.onRequestCommitted();
    }

    @Override
    protected void onRequestComplete() throws IOException {
        if(LOGGER.isInfoEnabled()) {
            LOGGER.info("Request has bend send complete:" + PATH + this.getRequestURI().replace("\n", "0x0A"));
        }
        super.onRequestComplete();
    }

    @Override
    protected void onException(final Throwable x) {
        LOGGER.warn("onException:", x);
        super.onException(x);
        if(callback != null) {
            callback.handleExcepion(x);
        }
    }

    @Override
    protected void onConnectionFailed(final Throwable x) {
        LOGGER.warn("onConnectionFailed:", x);
        super.onConnectionFailed(x);
        if(callback != null) {
            callback.handleExcepion(x);
        }

    }

    @Override
    protected void expire(final HttpDestination destination) {
        super.expire(destination);
        if(callback != null) {
            callback.handleExcepion(new ServiceException("request is expired, status:" + toState(getStatus())));
        }
    }

    public boolean isGzip() {
        return gzip;
    }

    /**
     * Get the response as RestfulResponse.
     * <br/>
     * 
     * @return response object.
     * @throws IOException
     * @since
     */
    public RestfulResponse getResponse() throws IOException {
        final RestfulResponse rsp = new RestfulResponse();
        rsp.setStatus(this.getResponseStatus());
        if(isGzip()) {
            final String responseString = decompressGzipToStr(getResponseContentBytes());
            rsp.setResponseJson(responseString);
        } else {
            rsp.setResponseJson(this.getResponseContent());
        }

        final HttpFields field = this.getResponseFields();
        if(field != null) {
            final Map<String, String> header = new HashMap<>();

            final Enumeration<String> names = field.getFieldNames();
            for(final Enumeration<String> e = names; e.hasMoreElements();) {
                final String fieldName = e.nextElement();
                final String fieldValue = field.getStringField(fieldName);
                header.put(fieldName, fieldValue);
            }

            rsp.setRespHeaderMap(header);
        }
        return rsp;
    }

}
