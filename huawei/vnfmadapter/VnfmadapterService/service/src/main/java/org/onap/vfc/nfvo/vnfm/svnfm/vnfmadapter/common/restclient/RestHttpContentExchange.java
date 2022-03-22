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

import java.io.*;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpFields;
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
public class RestHttpContentExchange  {

    private static final int STATUS_PARSING_HEADERS = 500;
    private static final int STATUS_PARSING_CONTENT = 500;
    private String _encoding = "utf-8";
    private byte[] _responseContent;
    private static final Logger LOGGER = LoggerFactory.getLogger(RestHttpContentExchange.class);
    private static final String PATH = "path:";
    private boolean gzip = false;
    private HttpFields _responseFields;
    private byte[] _responseContentByte;

    String _responseContentString;
    int _responseStatus;
    Request request;
    boolean _rsponseAbort;

//    private HttpFields setCacheFields(boolean cacheHeaders) {
//        _responseFields = cacheHeaders ? new HttpFields() : null;
//        return _responseFields;
//    }

    /**
     * @return the scheme of the URL
     */
//    public Buffer getScheme()
//    {
//        return _scheme;
//    }

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
        if (data == null) {
            return "";
        }
        final StringBuilder out = new StringBuilder();
        try (ByteArrayInputStream input = new ByteArrayInputStream(data);
             GZIPInputStream gzis = new GZIPInputStream(input);
             InputStreamReader reader = new InputStreamReader(gzis, Charset.forName(RestfulClientConst.ENCODING));) {
            final char[] buff = new char[1024];
            for (int n; (n = reader.read(buff)) != -1; ) {
                out.append(new String(buff, 0, n));
            }
        }
        return out.toString();

    }

    private boolean isGzip() {
        return gzip;
    }

    public void setResponseContentString(String responseContentString){
        this._responseContentString=responseContentString;
    }
    public void setResponseContentBytes (byte[] responseContentBytes){
        this._responseContentByte=responseContentBytes ;
    }
     public void setResponseStatus  ( int responseStatus ){
        this._responseStatus = responseStatus ;
    }
     public void setResponseFields  ( HttpFields responseFields  ){
        this._responseFields= responseFields ;
    }
    public synchronized String getResponseContentString() throws UnsupportedEncodingException {
        if (_responseContentString != null)
            return _responseContentString;
        return null;
    }


      synchronized byte[] getResponseContentBytes() {
        if (_responseContentByte != null)
            return _responseContentByte;
        return null;
    }


      synchronized int getResponseStatus() {
//        if (_responseStatus >= 500)
//            throw new IllegalStateException("internal server error");
        return _responseStatus;
    }


      synchronized HttpFields getResponseFields() {
//        if (_responseStatus >= STATUS_PARSING_CONTENT)
//            throw new IllegalStateException("Headers not completely received yet");
        return _responseFields;
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

        rsp.setStatus(getResponseStatus());
        if (isGzip()) {
            final String responseString = decompressGzipToStr(getResponseContentBytes());
            rsp.setResponseJson(responseString);
        } else {
            rsp.setResponseJson(this.getResponseContentString());
        }

        final HttpFields field = this.getResponseFields();
        if (field != null) {
            final Map<String, String> header = new HashMap<>();

            final Enumeration<String> names = field.getFieldNames();
            for (final Enumeration<String> e = names; e.hasMoreElements(); ) {
                final String fieldName = e.nextElement();
                final String fieldValue = field.getField(fieldName).getValue();
                header.put(fieldName, fieldValue);
            }

            rsp.setRespHeaderMap(header);
        }
        return rsp;
    }


//    @Override
//    public void onContent(Response response, ByteBuffer content, Callback callback) {
//        System.out.println("ContentExchange inside " + response.getStatus());
//        super.onContent(response, content, callback);
//        this._responseContentString = StandardCharsets.UTF_8.decode(content).toString();
//        this._responseContent = content.array();
//    }
//
//    @Override
//    public void onBegin(Response response) {
//
//    }
//
//    @Override
//    public void onComplete(Result result) {
//
//    }
//
//    @Override
//    public void onContent(Response response, ByteBuffer content) {
//
//    }
//
//    @Override
//    public void onFailure(Response response, Throwable failure) {
//        this._responseStatus = response.getStatus();
//        this._rsponseAbort = response.abort(failure);
//    }
//
//    @Override
//    public boolean onHeader(Response response, HttpField field) {
//        this._responseFields = response.getHeaders();
//        return false;
//    }
//
//    @Override
//    public void onHeaders(Response response) {
//
//    }
//
//    @Override
//    public void onSuccess(Response response) {
//        this._responseStatus = response.getStatus();
//    }

}
