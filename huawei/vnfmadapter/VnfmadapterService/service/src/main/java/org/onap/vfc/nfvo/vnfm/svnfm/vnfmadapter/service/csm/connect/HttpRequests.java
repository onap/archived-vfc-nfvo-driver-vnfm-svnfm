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

package org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.csm.connect;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLHandshakeException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.*;


import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.VnfmException;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.constant.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;

/**
 * HTTP Request class.</br>
 *
 * @author
 * @version VFC 1.0 Sep 14, 2016
 */
public final class HttpRequests {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRequest.class);

    private static MultiThreadedHttpConnectionManager httpClientMgr;

    private static final int PORT = 31943;

    private HttpRequests() {
        // constructor
    }

    static {
        httpClientMgr = new MultiThreadedHttpConnectionManager();
        httpClientMgr.getParams().setStaleCheckingEnabled(true);
        httpClientMgr.getParams().setMaxTotalConnections(20);
        httpClientMgr.getParams().setDefaultMaxConnectionsPerHost(100);
    }

    /**
     * Request builder.</br>
     *
     * @author
     * @version VFC 1.0 Sep 14, 2016
     */
    public static class Builder {

        private final List<Header> headers = new ArrayList<>(10);

        private String paramsJson;

        private HttpClient client;

        private HttpMethod httpMethod;

        private String encoding;

        private String url;

        private String authenticateMode;

        /**
         * Constructor<br>
         *
         * @param authenticateMode
         * @since VFC 1.0
         */
        public Builder(String authenticateMode) {
            this.authenticateMode = authenticateMode;
            client = new HttpClient(httpClientMgr);
            client.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
            client.getHttpConnectionManager().getParams().setSoTimeout(30000);
            encoding = Constant.ENCODEING;
        }

        /**
         * Add header
         * <br>
         *
         * @param name
         * @param value
         * @return
         * @since VFC 1.0
         */
        public Builder addHeader(String name, String value) {
            headers.add(new Header(name, value));
            return this;
        }

        /**
         * Add headers
         * <br>
         *
         * @param header
         * @param headers
         * @return
         * @since VFC 1.0
         */
        public Builder addHeaders(Header header, Header... headers) {
            if(header != null) {
                this.headers.add(header);
            }
            if(headers != null && headers.length > 0) {
                for(Header h : headers) {
                    this.headers.add(h);
                }
            }
            return this;
        }

        /**
         * Add headers
         * <br>
         *
         * @param headers
         * @return
         * @since VFC 1.0
         */
        public Builder addHeaders(List<Header> headers) {
            if(headers != null && !headers.isEmpty()) {
                this.headers.addAll(headers);
            }
            return this;
        }

        /**
         * Update URL
         * <br>
         *
         * @param url
         * @param path
         * @return
         * @throws VnfmException
         * @since VFC 1.0
         */
        public Builder setUrl(String url, String path) throws VnfmException {
            if(StringUtils.isEmpty(url)) {
                throw new VnfmException("com.huawei.nfvo.vcmmadapter.fusionsphere.check.httprequest.url");
            }

            this.url = url + path;

            LOG.info("setUrl: url =" + url);

            Protocol.registerProtocol(Constant.HTTPS,
                    new Protocol(Constant.HTTPS, SslProtocolSocketFactory.getInstance().get(authenticateMode), PORT));

            return this;
        }

        /**
         * Update URL
         * <br>
         *
         * @param url
         * @param path
         * @param defPort
         * @return
         * @throws VnfmException
         * @since VFC 1.0
         */
        public Builder setUrl(String url, String path, int defPort) throws VnfmException {
            if(StringUtils.isEmpty(url)) {
                throw new VnfmException("com.huawei.nfvo.vcmmadapter.fusionsphere.check.httprequest.url");
            }

            this.url = url + path;

            LOG.info("setUrl: url =" + url);

            Protocol.registerProtocol(Constant.HTTPS, new Protocol(Constant.HTTPS,
                    SslProtocolSocketFactory.getInstance().get(authenticateMode), defPort));

            return this;
        }

        /**
         * HTTP POST
         * <br>
         *
         * @return
         * @since VFC 1.0
         */
        public Builder post() {
            this.httpMethod = new PostMethod(url);
            return this;
        }

        /**
         * HTTP GET
         * <br>
         *
         * @return
         * @since VFC 1.0
         */
        public Builder get() {
            this.httpMethod = new GetMethod(url);
            return this;
        }

        /**
         * HTTP PUT
         * <br>
         *
         * @return
         * @since VFC 1.0
         */
        public Builder put() {
            this.httpMethod = new PutMethod(url);
            return this;
        }

        /**
         * HTTP DELETE
         * <br>
         *
         * @return
         * @since VFC 1.0
         */
        public Builder delete() {
            this.httpMethod = new DeleteMethod(url);
            return this;
        }

        /**
         * Update Params
         * <br>
         *
         * @param json
         * @return
         * @since VFC 1.0
         */
        public Builder setParams(String json) {
            this.paramsJson = json;
            return this;
        }

        /**
         * Set the encoding
         * <br>
         *
         * @param encode
         * @return
         * @since VFC 1.0
         */
        public Builder setEncoding(String encode) {
            this.encoding = encode;
            return this;
        }

        /**
         * Make HTTP request
         * <br>
         *
         * @return
         * @since VFC 1.0
         */
        public String request() {
            String result = null;
            try {
                result = executeMethod().getResponseBodyAsString();
            } catch(SSLHandshakeException e) {
                LOG.error(String.format("function=request, msg=http request url: %s, SSLHandshake Fail : ", url), e);
                try {
                    LOG.error("function=request, msg=SSLHandshake Fail, start refresh certificate ...");
                    SslProtocolSocketFactory socketFactory = SslProtocolSocketFactory.getInstance();
                    socketFactory.refresh(authenticateMode);
                    Protocol.registerProtocol(Constant.HTTPS, new Protocol(Constant.HTTPS,
                            SslProtocolSocketFactory.getInstance().get(authenticateMode), PORT));
                    LOG.error("function=request, msg=SSLHandshake Fail, certificate refresh successful .");

                    result = executeMethod().getResponseBodyAsString();
                } catch(IOException ioe) {
                    LOG.error(String.format("function=request, IOException msg=http request url: %s, error: ", url),
                            ioe);
                } catch(VnfmException ose) {
                    LOG.error(String.format("function=request, VnfmException msg=http request url: %s, error: ", url),
                            ose);
                }
            } catch(IOException | VnfmException e) {
                LOG.error(String.format("function=request, IOException msg=http request url: %s, error: ", url), e);
            } finally {
                httpMethod.releaseConnection();
            }
            return result;
        }

        /**
         * Execute the HTTP method
         * <br>
         *
         * @return
         * @throws VnfmException
         * @throws IOException
         * @since VFC 1.0
         */
        public HttpMethod execute() throws VnfmException, IOException {
            try {
                executeMethod();
            } catch(SSLHandshakeException e) {
                LOG.error(String.format("function=execute, msg=http request url: %s, SSLHandshake Fail : ", url), e);
                LOG.error("function=execute, SSLHandshake Fail, start refresh certificate ...");
                SslProtocolSocketFactory socketFactory = SslProtocolSocketFactory.getInstance();
                socketFactory.refresh(authenticateMode);
                Protocol.registerProtocol(Constant.HTTPS, new Protocol(Constant.HTTPS,
                        SslProtocolSocketFactory.getInstance().get(authenticateMode), PORT));
                LOG.error("function=execute, SSLHandshake Fail, certificate refresh successful .");

                executeMethod();
            }
            return httpMethod;
        }

        private HttpMethod executeMethod() throws VnfmException, IOException {
            if(httpMethod == null) {
                httpMethod = new GetMethod(url);
            }

            handleParams();

            client.executeMethod(httpMethod);

            return httpMethod;
        }

        private void handleParams() throws UnsupportedEncodingException {
            if(paramsJson != null && !paramsJson.isEmpty()) {
                StringRequestEntity stringRequestEntity =
                        new StringRequestEntity(paramsJson, "application/json", encoding);
                String contentLengthString = String.valueOf(stringRequestEntity.getContentLength());

                if(httpMethod instanceof PostMethod || httpMethod instanceof PutMethod) {
                    ((EntityEnclosingMethod)httpMethod).setRequestEntity(stringRequestEntity);
                    ((EntityEnclosingMethod)httpMethod).addRequestHeader("Content-Length", contentLengthString);
                } else {
                    httpMethod.setQueryString(paramsJson);
                }
                addHeader("Content-Type", String.format("application/json;charset=%s", encoding));
            }

            for(Header header : headers) {
                httpMethod.addRequestHeader(header);
            }
        }
    }

}
