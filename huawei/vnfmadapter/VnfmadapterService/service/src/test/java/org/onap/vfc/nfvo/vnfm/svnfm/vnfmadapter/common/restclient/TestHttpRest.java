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
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpContentResponse;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.client.HttpRequest;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpVersion;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

/**
 * <br/>
 * <p>
 * </p>
 *
 * @author
 */
@RunWith(JMockit.class)
public class TestHttpRest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * <br/>
     *
     * @throws java.lang.Exception
     * @since
     */
    static HttpClient httpClient;
    static RestfulOptions options;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        options = new RestfulOptions();
        options.setOption("thread", new Integer(5));
        options.setOption("maxConnectionPerAddr", new Integer(200));
        options.setOption("ConnectTimeout", new Integer(500));
        options.setHost("localhost");

        httpClient = new HttpClient();
        httpClient.start();
        Request request = httpClient.newRequest("http://reqres.in/api/users/2");
        ContentResponse contentResponse = request.send();
        new MockUp<HttpBaseRest>() {
            @Mock
            public ContentResponse getResponse() {
                return contentResponse;
            }
        };
    }

    /**
     * <br/>
     *
     * @throws java.lang.Exception
     * @since
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        httpClient.stop();
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
    }

    /**
     * <br/>
     *
     * @throws Exception
     * @since
     */
    @Test
    public void testInitHttpRest() throws Exception {
        final RestfulOptions options = new RestfulOptions();
        new MockUp<HttpClient>() {

            @Mock
            public void doStart() {
            }
        };
        final HttpRest httpRest = new HttpRest();
        httpRest.initHttpRest(options);
        final Field httpClient = HttpBaseRest.class.getDeclaredField("client");
        httpClient.setAccessible(true);
        Assert.assertNotNull(httpClient.get(httpRest));
    }
//
//    /**
//     * <br/>
//     *
//     * @throws NoSuchFieldException
//     * @throws Exception
//     * @since
//     */
//    @Test
//    public void testCreateRestHttpContentExchange() throws NoSuchFieldException, Exception {
//        final HttpBaseRest httpRest = new HttpRest();
//        final RestfulAsyncCallback callback = new RestfulAsyncCallback() {
//
//            @Override
//            public void callback(final RestfulResponse response) {
//
//            }
//
//            @Override
//            public void handleExcepion(final Throwable e) {
//
//            }
//
//        };
//         final RestHttpContentExchange exchange = httpRest.createRestHttpContentExchange(callback);
//          assertNotNull(exchange);
//        final Field callbackField = RestHttpContentExchange.class.getDeclaredField("callback");
//        assertNotNull(callbackField);
//    }

    /**
     * <br/>
     *
     * @throws Exception
     * @since
     */
    @Test
    public void testGetStringRestfulParametes() throws Exception {
        final HttpRest httpRest = getHttpRest(options);
        final RestfulParametes parametes = new RestfulParametes();
        parametes.put("id", "1234");
        parametes.put("name", "some-name");
        parametes.put("address", null);
        parametes.putHttpContextHeader("Content-Type", "application/json");
        parametes.putHttpContextHeader("Accept-Encoding", "*/*");
        final RestfulResponse response = httpRest.get("path/to/service", parametes);
        assertEquals(200, response.getStatus());

    }


    /**
     * <br/>
     *
     * @throws Exception
     * @since
     */
    @Test
    public void testGetStringRestfulParametesRestfulOptions() throws Exception {
        final HttpRest httpRest = getHttpRest(options);
        final RestfulResponse response = httpRest.get("path/to/service", new RestfulParametes(), options);
        assertEquals(200, response.getStatus());
    }


    /**
     * <br/>
     *
     * @throws Exception
     * @since
     */
    @Test
    public void testHeadStringRestfulParametes() throws Exception {

        final HttpRest httpRest = getHttpRest(options);
        final RestfulParametes parametes = new RestfulParametes();
        parametes.put("id", "1234");
        parametes.put("name", "some-name");
        parametes.put("address", null);
        parametes.putHttpContextHeader("Content-Type", "");
        parametes.putHttpContextHeader("Accept-Encoding", "");
        final RestfulResponse response = httpRest.head("path/to/service", parametes);
        assertEquals(200, response.getStatus());
    }

    /**
     * <br/>
     *
     * @throws Exception
     * @since
     */
    @Test
    public void testHeadStringRestfulParametesRestfulOptions() throws Exception {

        final HttpRest httpRest = getHttpRest(options);
        final RestfulParametes parametes = new RestfulParametes();
        parametes.put("id", "1234");
        parametes.put("name", "some-name");
        parametes.put("address", null);
        parametes.putHttpContextHeader("Content-Type", "");
        parametes.putHttpContextHeader("Accept-Encoding", "");
        final RestfulResponse response = httpRest.head("path/to/service", parametes, options);
        assertEquals(200, response.getStatus());
    }

    /**
     * <br/>
     *
     * @param options
     * @return
     * @throws ServiceException
     * @since
     */
    private HttpRest getHttpRest(final RestfulOptions options) throws ServiceException {
        final HttpRest httpRest = new HttpRest();
        {
//            new MockUp<HttpClient>() {
//
//                @Mock
//                public void doStart() {
//                }
//
//                @Mock
//                public void send(final HttpExchange exchange) throws IOException {
//                }
//            };
            httpRest.initHttpRest(options);

        }
        return httpRest;
    }

    /**
     * <br/>
     *
     * @throws Exception
     * @since
     */
    @Test
    public void testAsyncGetStringRestfulParametesRestfulAsyncCallback() throws Exception {


        final HttpRest httpRest = getHttpRest(options);
        final RestfulAsyncCallback callback = new RestfulAsyncCallback() {

            @Override
            public void callback(final RestfulResponse response) {

            }

            @Override
            public void handleExcepion(final Throwable e) {

            }

        };
        httpRest.asyncGet("path/to/service", new RestfulParametes(), callback);
        httpRest.asyncGet("path/to/service", new RestfulParametes(), null);
    }

    /**
     * <br/>
     *
     * @throws ServiceException
     * @since
     */
    @Test
    public void testAsyncGetStringRestfulParametesRestfulOptionsRestfulAsyncCallback() throws ServiceException {


        final HttpRest httpRest = getHttpRest(options);
        final RestfulAsyncCallback callback = new RestfulAsyncCallback() {

            @Override
            public void callback(final RestfulResponse response) {

            }

            @Override
            public void handleExcepion(final Throwable e) {

            }

        };
        httpRest.asyncGet("path/to/service", new RestfulParametes(), new RestfulOptions(), callback);
        httpRest.asyncGet("path/to/service", new RestfulParametes(), new RestfulOptions(), null);
    }

    /**
     * <br/>
     *
     * @throws ServiceException
     * @since
     */
    @Test
    public void testPutStringRestfulParametes() throws ServiceException {

        final HttpRest httpRest = getHttpRest(options);
        final RestfulParametes parametes = new RestfulParametes();
        parametes.put("id", "1234");
        parametes.put("name", "some-name");
        parametes.put("address", null);
        parametes.putHttpContextHeader("Content-Type", "");
        parametes.putHttpContextHeader("Accept-Encoding", "");
        final RestfulResponse response = httpRest.put("path/to/service", parametes);
        assertEquals(200, response.getStatus());
    }

    /**
     * <br/>
     *
     * @throws ServiceException
     * @since
     */
    @Test
    public void testPutStringRestfulParametesRestfulOptions() throws ServiceException {

        final HttpRest httpRest = getHttpRest(options);
        final RestfulParametes parametes = new RestfulParametes();
        parametes.put("id", "1234");
        parametes.put("name", "some-name");
        parametes.put("address", null);
        parametes.putHttpContextHeader("Content-Type", "");
        parametes.putHttpContextHeader("Accept-Encoding", "");
        final RestfulResponse response = httpRest.put("path/to/service", parametes, null);
        assertEquals(200, response.getStatus());
    }

    /**
     * <br/>
     *
     * @throws ServiceException
     * @since
     */
    @Test
    public void testAsyncPutStringRestfulParametesRestfulAsyncCallback() throws ServiceException {


        final HttpRest httpRest = getHttpRest(options);

        final RestfulAsyncCallback callback = new RestfulAsyncCallback() {

            @Override
            public void callback(final RestfulResponse response) {

            }

            @Override
            public void handleExcepion(final Throwable e) {

            }

        };
        httpRest.asyncPut("path/to/service", new RestfulParametes(), callback);
        httpRest.asyncPut("path/to/service", new RestfulParametes(), null);
    }

    /**
     * <br/>
     *
     * @throws Exception
     * @since
     */
    @Test
    public void testAsyncPutStringRestfulParametesRestfulOptionsRestfulAsyncCallback() throws Exception {

        final HttpRest httpRest = getHttpRest(options);

        final RestfulAsyncCallback callback = new RestfulAsyncCallback() {

            @Override
            public void callback(final RestfulResponse response) {

            }

            @Override
            public void handleExcepion(final Throwable e) {

            }

        };
        httpRest.asyncPut("path/to/service", new RestfulParametes(), new RestfulOptions(), callback);
        httpRest.asyncPut("path/to/service", new RestfulParametes(), new RestfulOptions(), null);
    }

    /**
     * <br/>
     *
     * @throws Exception
     * @since
     */
    @Test
    public void testAsyncPostStringRestfulParametesRestfulAsyncCallback() throws Exception {

        options.setRestTimeout(10);

        final HttpBaseRest httpRest = getHttpRest(options);
        final RestfulAsyncCallback callback = new RestfulAsyncCallback() {

            @Override
            public void callback(final RestfulResponse response) {

            }

            @Override
            public void handleExcepion(final Throwable e) {

            }

        };
        httpRest.asyncPost("path/to/service", new RestfulParametes(), options, callback);
        httpRest.asyncPost("path/to/service", new RestfulParametes(), options, null);
    }

    /**
     * <br/>
     *
     * @throws ServiceException
     * @since
     */
    @Test
    public void testAsyncPostStringRestfulParametesRestfulOptionsRestfulAsyncCallback() throws ServiceException {

        options.setRestTimeout(10);

        final HttpBaseRest httpRest = getHttpRest(options);

        final RestfulAsyncCallback callback = new RestfulAsyncCallback() {

            @Override
            public void callback(final RestfulResponse response) {

            }

            @Override
            public void handleExcepion(final Throwable e) {

            }

        };
        httpRest.asyncPost("path/to/service", new RestfulParametes(), options, callback);
        httpRest.asyncPost("path/to/service", new RestfulParametes(), options, null);
    }

    /**
     * <br/>
     *
     * @throws ServiceException
     * @since
     */
    @Test
    public void testDeleteStringRestfulParametes() throws ServiceException {

        final HttpBaseRest httpRest = getHttpRest(options);

        final RestfulResponse response = httpRest.delete("path/to/service", null);
        assertEquals(-1, response.getStatus());
    }

    /**
     * <br/>
     *
     * @throws ServiceException
     * @since
     */
    @Test
    public void testDeleteStringRestfulParametesRestfulOptions() throws ServiceException {


        final HttpBaseRest httpRest = getHttpRest(options);
        final RestfulParametes parameters = new RestfulParametes();
        parameters.put("id", "1234");
        parameters.put("name", "some-name");
        parameters.put("address", null);
        parameters.setRawData("{ \"data\"=\"sample JSON data\"");
        parameters.putHttpContextHeader("Content-Type", "");
        parameters.putHttpContextHeader("Accept-Encoding", "");
        final RestfulResponse response = httpRest.delete("path/to/service", parameters, options);
        assertEquals(200, response.getStatus());
    }

    /**
     * <br/>
     *
     * @throws ServiceException
     * @since
     */
    @Test
    public void testAsyncDeleteStringRestfulParametesRestfulAsyncCallback() throws ServiceException {

        options.setRestTimeout(10);

        final HttpBaseRest httpRest = getHttpRest(options);
        final RestfulAsyncCallback callback = new RestfulAsyncCallback() {

            @Override
            public void callback(final RestfulResponse response) {

            }

            @Override
            public void handleExcepion(final Throwable e) {

            }

        };
        httpRest.asyncDelete("path/to/service", new RestfulParametes(), callback);
        httpRest.asyncDelete("path/to/service", new RestfulParametes(), null);
    }

    /**
     * <br/>
     *
     * @throws ServiceException
     * @since
     */
    @Test
    public void testAsyncDeleteStringRestfulParametesRestfulOptionsRestfulAsyncCallback() throws ServiceException {

        options.setRestTimeout(10);

        final HttpBaseRest httpRest = getHttpRest(options);

        final RestfulAsyncCallback callback = new RestfulAsyncCallback() {

            @Override
            public void callback(final RestfulResponse response) {

            }

            @Override
            public void handleExcepion(final Throwable e) {

            }

        };
        httpRest.asyncDelete("path/to/service", new RestfulParametes(), options, callback);
        httpRest.asyncDelete("path/to/service", new RestfulParametes(), options, null);
    }

    /**
     * <br/>
     *
     * @throws ServiceException
     * @since
     */
    @Test
    public void testPatchStringRestfulParametes() throws ServiceException {

        final HttpBaseRest httpRest = getHttpRest(options);
        final RestfulParametes parameters = new RestfulParametes();
        parameters.put("id", "1234");
        parameters.put("name", "some-name");
        parameters.put("address", null);
        parameters.setRawData("{ \"data\"=\"sample JSON data\"");
        parameters.putHttpContextHeader("Content-Type", "");
        parameters.putHttpContextHeader("Accept-Encoding", "");
        final RestfulResponse response = httpRest.patch("path/to/service", parameters);
        assertEquals(200, response.getStatus());
    }

    /**
     * <br/>
     *
     * @throws ServiceException
     * @since
     */
    @Test
    public void testPatchStringRestfulParametesRestfulOptions() throws ServiceException {

        final HttpBaseRest httpRest = getHttpRest(options);
        final RestfulParametes parameters = new RestfulParametes();
        parameters.put("id", "1234");
        parameters.put("name", "some-name");
        parameters.put("address", null);
        parameters.setRawData("{ \"data\"=\"sample JSON data\"");
        parameters.putHttpContextHeader("Content-Type", "");
        parameters.putHttpContextHeader("Accept-Encoding", "");
        final RestfulResponse response = httpRest.patch("path/to/service", parameters, options);
        assertEquals(200, response.getStatus());
    }

    /**
     * <br/>
     *
     * @throws ServiceException
     * @since
     */
    @Test
    public void testAsyncPatchStringRestfulParametesRestfulAsyncCallback() throws ServiceException {

        options.setRestTimeout(10);

        final HttpBaseRest httpRest = getHttpRest(options);

        final RestfulAsyncCallback callback = new RestfulAsyncCallback() {

            @Override
            public void callback(final RestfulResponse response) {

            }

            @Override
            public void handleExcepion(final Throwable e) {

            }

        };
        httpRest.asyncPatch("path/to/service", new RestfulParametes(), callback);
        httpRest.asyncPatch("path/to/service", new RestfulParametes(), null);
    }

    /**
     * <br/>
     *
     * @throws ServiceException
     * @since
     */
    @Test
    public void testAsyncPatchStringRestfulParametesRestfulOptionsRestfulAsyncCallback() throws ServiceException {

        options.setRestTimeout(10);

        final HttpBaseRest httpRest = getHttpRest(options);

        final RestfulAsyncCallback callback = new RestfulAsyncCallback() {

            @Override
            public void callback(final RestfulResponse response) {

            }

            @Override
            public void handleExcepion(final Throwable e) {

            }

        };
        httpRest.asyncPatch("path/to/service", new RestfulParametes(), options, callback);
        httpRest.asyncPatch("path/to/service", new RestfulParametes(), options, null);
    }

}
