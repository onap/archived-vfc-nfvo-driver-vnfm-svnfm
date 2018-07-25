/*
 * Copyright 2017 Huawei Technologies Co., Ltd.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.eclipse.jetty.client.Address;
import org.eclipse.jetty.client.CachedExchange;
import org.eclipse.jetty.client.HttpDestination;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.eclipse.jetty.util.StringUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

/**
 * <br/>
 * <p>
 * </p>
 * 
 * @author
 * @version
 */
@RunWith(JMockit.class)
public class TestRestHttpContentExchange {

    @Mocked
    HttpDestination mockedDest;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * <br/>
     * 
     * @throws java.lang.Exception
     * @since
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * <br/>
     * 
     * @throws java.lang.Exception
     * @since
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * <br/>
     * 
     * @throws java.lang.Exception
     * @since
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * <br/>
     * 
     * @throws java.lang.Exception
     * @since
     */
    @After
    public void tearDown() throws Exception {
        LogManager.getLogger(RestHttpContentExchange.class).setLevel(Level.ERROR);
    }

    /**
     * <br/>
     * 
     * @throws IOException
     * @since
     */
    @Test
    public void testOnRequestCommitted() throws IOException {
        final RestHttpContentExchange exchange = new RestHttpContentExchange(false, null);
        final Address address = new Address("localhost", 9999);
        exchange.setAddress(address);
        exchange.setRequestURI("/the/request/uri");
        exchange.onRequestCommitted();

        LogManager.getLogger(RestHttpContentExchange.class).setLevel(Level.DEBUG);
        exchange.onRequestCommitted();
    }

    /**
     * <br/>
     * 
     * @throws IOException
     * @since
     */
    @Test
    public void testOnRequestComplete() throws IOException {
        final RestHttpContentExchange exchange = new RestHttpContentExchange(false, null);
        final Address address = new Address("localhost", 9999);
        exchange.setAddress(address);
        exchange.setRequestURI("/the/request/uri");
        exchange.onRequestComplete();

        LogManager.getLogger(RestHttpContentExchange.class).setLevel(Level.DEBUG);
        exchange.onRequestComplete();
    }

    /**
     * <br/>
     * 
     * @throws Exception
     * @since
     */
    @Test
    public void testOnResponseComplete() throws Exception {
        RestHttpContentExchange exchange = new RestHttpContentExchange(false, null);
        final Address address = new Address("localhost", 9999);
        exchange.setAddress(address);
        exchange.setRequestURI("/the/request/uri");
        exchange.onResponseComplete();

        LogManager.getLogger(RestHttpContentExchange.class).setLevel(Level.DEBUG);
        exchange.onResponseComplete();

        final AtomicInteger isCallback = new AtomicInteger(0);
        final AtomicInteger isException = new AtomicInteger(0);
        final RestfulAsyncCallback callback = new RestfulAsyncCallback() {

            @Override
            public void callback(final RestfulResponse response) {
                isCallback.set(1);
            }

            @Override
            public void handleExcepion(final Throwable e) {
                isException.set(1);
            }

        };

        final Field statusField = HttpExchange.class.getDeclaredField("_status");
        statusField.setAccessible(true);
        exchange = new RestHttpContentExchange(false, callback);
        statusField.set(exchange, new AtomicInteger(200));
        exchange.setAddress(new Address("localhost", 9999));
        exchange.setRequestURI("/the/request/uri");
        exchange.onResponseComplete();
        assertEquals(1, isCallback.get());
        assertEquals(0, isException.get());
    }

    /**
     * <br/>
     * 
     * @throws Exception
     * @since
     */
    @Test
    @Ignore
    public void testDecompressGzipToStr() throws Exception {
        final RestHttpContentExchange exchange = new RestHttpContentExchange(false, null);
        final Address address = new Address("localhost", 9999);
        exchange.setAddress(address);
        exchange.setRequestURI("/the/request/uri");

        final InputStream stream = ClassLoader.getSystemResourceAsStream("sample.txt.gz");
        final byte[] binaryData = new byte[1024];
        stream.read(binaryData);
        final String expected = "sample data.";

        final String actual = exchange.decompressGzipToStr(binaryData);

        assertEquals(actual, expected);

        new MockUp<ByteArrayInputStream>() {

            @Mock
            public int read() throws Exception {
                throw new IOException();
            }

            @Mock
            public int read(final byte abyte0[], final int i, final int j) {

                return -1;
            }

        };

        thrown.expect(IOException.class);
        exchange.decompressGzipToStr(binaryData);
    }

    /**
     * <br/>
     * 
     * @throws Exception
     * @since
     */
    @Test
    @Ignore
    public void testDecompressGzipToStrException() throws Exception {
        final RestHttpContentExchange exchange = new RestHttpContentExchange(false, null);
        final Address address = new Address("localhost", 9999);
        exchange.setAddress(address);
        exchange.setRequestURI("/the/request/uri");

        final InputStream stream = ClassLoader.getSystemResourceAsStream("sample.txt.gz");
        final byte[] binaryData = new byte[1024];
        stream.read(binaryData);
        final String expected = "sample data.";

        new MockUp<GZIPInputStream>() {

            @Mock
            public void close() throws IOException {
                throw new IOException();
            }

        };

        new MockUp<InputStreamReader>() {

            @Mock
            public void close() throws IOException {
                throw new IOException();
            }

        };

        new MockUp<ByteArrayInputStream>() {

            @Mock
            public void close() throws IOException {
                throw new IOException();
            }

        };

        final String actual = exchange.decompressGzipToStr(binaryData);
        assertEquals(actual, expected);
    }

    /**
     * <br/>
     * 
     * @throws Exception
     * @since
     */
    @Test
    public void testDecompressGzipToStrNull() throws Exception {
        final RestHttpContentExchange exchange = new RestHttpContentExchange(false, null);
        final Address address = new Address("localhost", 9999);
        exchange.setAddress(address);
        exchange.setRequestURI("/the/request/uri");
        final String expected = "";
        final String actual = exchange.decompressGzipToStr(null);

        assertEquals(actual, expected);
    }

    /**
     * <br/>
     * 
     * @throws Exception
     * @since
     */
    @Test
    public void testOnResponseHeaderBufferBuffer() throws Exception {
        final RestHttpContentExchange exchange = new RestHttpContentExchange(false, null);
        final Address address = new Address("localhost", 9999);
        exchange.setAddress(address);
        exchange.setRequestURI("/the/request/uri");

        final Buffer name = new ByteArrayBuffer("key");
        final Buffer value = new ByteArrayBuffer("value");
        exchange.onResponseHeader(name, value);

        new MockUp<HttpHeaders>() {

            @Mock
            public int getOrdinal(final Buffer buffer) {
                return HttpHeaders.CONTENT_ENCODING_ORDINAL;
            }

        };
        exchange.onResponseHeader(name, value);

        new MockUp<StringUtil>() {

            @Mock
            public String asciiToLowerCase(final String s) {
                return "gzip";
            }

        };
        exchange.onResponseHeader(name, value);

    }

    /**
     * <br/>
     * 
     * @since
     */
    @Test
    public void testOnExceptionThrowable() {
        final RestHttpContentExchange exchange = new RestHttpContentExchange(false, null);
        final Address address = new Address("localhost", 9999);
        exchange.setAddress(address);
        exchange.setRequestURI("/the/request/uri");
        exchange.onException(new Exception());
    }

    /**
     * <br/>
     * 
     * @since
     */
    @Test
    public void testOnExceptionThrowableWithCallback() {
        final AtomicInteger isCallback = new AtomicInteger(0);
        final AtomicInteger isException = new AtomicInteger(0);
        final RestfulAsyncCallback callback = new RestfulAsyncCallback() {

            @Override
            public void callback(final RestfulResponse response) {
                isCallback.set(1);
            }

            @Override
            public void handleExcepion(final Throwable e) {
                isException.set(1);
            }

        };
        final RestHttpContentExchange exchange = new RestHttpContentExchange(true, callback);
        final Address address = new Address("localhost", 9999);
        exchange.setAddress(address);
        exchange.setRequestURI("/the/request/uri");
        exchange.onException(new Exception());
        assertEquals(0, isCallback.get());
        assertEquals(1, isException.get());
    }

    /**
     * <br/>
     * 
     * @since
     */
    @Test
    public void testOnConnectionFailedThrowable() {
        final RestHttpContentExchange exchange = new RestHttpContentExchange(false, null);
        final Address address = new Address("localhost", 9999);
        exchange.setAddress(address);
        exchange.setRequestURI("/the/request/uri");
        exchange.onConnectionFailed(new Exception());
    }

    /**
     * <br/>
     * 
     * @since
     */
    @Test
    public void testOnConnectionFailedThrowableException() {
        final AtomicInteger isCallback = new AtomicInteger(0);
        final AtomicInteger isException = new AtomicInteger(0);
        final RestfulAsyncCallback callback = new RestfulAsyncCallback() {

            @Override
            public void callback(final RestfulResponse response) {
                isCallback.set(1);
            }

            @Override
            public void handleExcepion(final Throwable e) {
                isException.set(1);
            }

        };
        final RestHttpContentExchange exchange = new RestHttpContentExchange(true, callback);
        final Address address = new Address("localhost", 9999);
        exchange.setAddress(address);
        exchange.setRequestURI("/the/request/uri");
        exchange.onConnectionFailed(new Exception());
        assertEquals(0, isCallback.get());
        assertEquals(1, isException.get());
    }

    /**
     * <br/>
     * 
     * @since
     */
    @Test
    public void testExpireHttpDestination() {
        final RestHttpContentExchange exchange = new RestHttpContentExchange(true, null);
        final Address address = new Address("localhost", 9999);
        exchange.setAddress(address);
        exchange.setRequestURI("/the/request/uri");
        exchange.expire(mockedDest);
    }

    /**
     * <br/>
     * 
     * @throws Exception
     * @since
     */
    @Test
    public void testExpireHttpDestinationException() throws Exception {
        final AtomicInteger isCallback = new AtomicInteger(0);
        final AtomicInteger isException = new AtomicInteger(0);
        final List<Throwable> thrSet = new ArrayList<Throwable>();
        final RestfulAsyncCallback callback = new RestfulAsyncCallback() {

            @Override
            public void callback(final RestfulResponse response) {
                isCallback.set(1);
            }

            @Override
            public void handleExcepion(final Throwable e) {
                isException.set(1);
                thrSet.add(e);
            }

        };
        final RestHttpContentExchange exchange = new RestHttpContentExchange(true, callback);
        final Address address = new Address("localhost", 9999);
        exchange.setAddress(address);
        exchange.setRequestURI("/the/request/uri");
        exchange.expire(mockedDest);
        assertEquals(0, isCallback.get());
        assertEquals(1, isException.get());
        assertEquals(1, thrSet.size());
        final Throwable t = thrSet.get(0);
        assertEquals(ServiceException.class, t.getClass());
    }

    /**
     * <br/>
     * 
     * @throws Exception
     * @since
     */
    @Test
    public void testIsGzip() throws Exception {
        final RestHttpContentExchange exchange = new RestHttpContentExchange(false, null);
        final Address address = new Address("localhost", 9999);
        exchange.setAddress(address);
        exchange.setRequestURI("/the/request/uri");

        final Buffer name = new ByteArrayBuffer("key");
        final Buffer value = new ByteArrayBuffer("value");

        new MockUp<HttpHeaders>() {

            @Mock
            public int getOrdinal(final Buffer buffer) {
                return HttpHeaders.CONTENT_ENCODING_ORDINAL;
            }

        };
        exchange.onResponseHeader(name, value);
        assertFalse(exchange.isGzip());

        new MockUp<StringUtil>() {

            @Mock
            public String asciiToLowerCase(final String s) {
                return "gzip";
            }

        };
        exchange.onResponseHeader(name, value);
        assertTrue(exchange.isGzip());
    }

    /**
     * <br/>
     * 
     * @throws Exception
     * @since
     */
    @Test
    public void testGetResponse() throws Exception {
        final RestHttpContentExchange exchange = new RestHttpContentExchange(false, null);
        final Address address = new Address("localhost", 9999);
        exchange.setAddress(address);
        exchange.setRequestURI("/the/request/uri");

        final Field statusField = HttpExchange.class.getDeclaredField("_status");
        statusField.setAccessible(true);
        statusField.set(exchange, new AtomicInteger(200));

        RestfulResponse response = exchange.getResponse();
        assertEquals(0, response.getStatus());

        final HttpFields fields = new HttpFields();
        final Field headerFields = CachedExchange.class.getDeclaredField("_responseFields");
        headerFields.setAccessible(true);
        headerFields.set(exchange, fields);
        response = exchange.getResponse();
        assertEquals(0, response.getStatus());
        fields.add("Content-Type", "application/json");
        fields.add("Content-Encode", "UTF-8");
        response = exchange.getResponse();
        assertEquals(0, response.getStatus());
    }

    /**
     * <br/>
     * 
     * @throws Exception
     * @since
     */
    @Test
    public void testGetResponseGzip() throws Exception {
        final RestHttpContentExchange exchange = new RestHttpContentExchange(false, null);
        final Address address = new Address("localhost", 9999);
        exchange.setAddress(address);
        exchange.setRequestURI("/the/request/uri");
        new MockUp<RestHttpContentExchange>() {

            @Mock
            public boolean isGzip() {
                return true;
            }
        };
        final Field statusField = HttpExchange.class.getDeclaredField("_status");
        statusField.setAccessible(true);
        statusField.set(exchange, new AtomicInteger(200));

        final RestfulResponse response = exchange.getResponse();
        assertEquals(0, response.getStatus());
    }
}
