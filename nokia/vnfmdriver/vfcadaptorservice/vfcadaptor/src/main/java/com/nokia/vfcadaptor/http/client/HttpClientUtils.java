/*
 * Copyright 2016-2017, Nokia Corporation
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

package com.nokia.vfcadaptor.http.client;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMethod;

public class HttpClientUtils {
	private final static Logger logger = Logger.getLogger(HttpClientUtils.class);
	
	public static HttpClientBuilder createHttpClientBuilder() {
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        SSLContext sslContext = null;
		try {
			sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
			    public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
			        return true;
			    }
			}).build();
		} catch (Exception e) {
			logger.error("Error to createHttpClientBuilder", e);
		}
		httpClientBuilder.setSSLContext(sslContext);
		
		HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
		
		SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", PlainConnectionSocketFactory.getSocketFactory())
				.register("https", sslSocketFactory)
				.build();
		
		PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager( socketFactoryRegistry);
		connMgr.setMaxTotal(200);
		connMgr.setDefaultMaxPerRoute(50);
		httpClientBuilder.setConnectionManager(connMgr);
		return httpClientBuilder;
	}
	
	public static HttpRequestBase getHttpRequest(RequestMethod requestMethod) {
		HttpRequestBase base = null;
		switch(requestMethod) {
			case GET:
				base = new HttpGet();
				break;
			case DELETE:
				base = new HttpDelete();
				break;
			default:
				base = new HttpPost();
				break;
		}
		return base;
	}
}
