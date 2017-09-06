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

/**
 * ReSTful method interface.<br/>
 * <p>
 * </p>
 * 
 * @author
 * @version 28-May-2016
 */
public interface Restful {

    /**
     * Http GET method.<br/>
     * 
     * @param servicePath: request path.
     * @param restParametes: query parameters.
     * @return response.
     * @throws ServiceException
     * @since
     */
    RestfulResponse get(String servicePath, RestfulParametes restParametes) throws ServiceException;

    /**
     * Http HEAD method.<br/>
     * 
     * @param servicePath: request path.
     * @param restParametes: request parameters.
     * @param options: request options.
     * @return response.
     * @throws ServiceException
     * @since
     */
    RestfulResponse head(String servicePath, RestfulParametes restParametes, RestfulOptions options)
            throws ServiceException;

    /**
     * Http HEAD method.<br/>
     * 
     * @param servicePath: request path.
     * @param restParametes: request parameters.
     * @return response.
     * @throws ServiceException
     * @since
     */
    RestfulResponse head(String servicePath, RestfulParametes restParametes) throws ServiceException;

    /**
     * Http GET method.<br/>
     * 
     * @param servicePath: request path.
     * @param restParametes: request parameters.
     * @param options: request options.
     * @return response.
     * @throws ServiceException
     * @since
     */
    RestfulResponse get(String servicePath, RestfulParametes restParametes, RestfulOptions options)
            throws ServiceException;

    /**
     * Asynchronouse GET request.<br/>
     * 
     * @param servicePath: request path.
     * @param restParametes: request parameters.
     * @param callback: response callback method.
     * @throws ServiceException
     * @since
     */
    void asyncGet(String servicePath, RestfulParametes restParametes, RestfulAsyncCallback callback)
            throws ServiceException;

    /**
     * Asynchronouse GET request.<br/>
     * 
     * @param servicePath: request path.
     * @param restParametes: request parameters.
     * @param options: request options.
     * @param callback: response callback method.
     * @throws ServiceException
     * @since
     */
    void asyncGet(String servicePath, RestfulParametes restParametes, RestfulOptions options,
            RestfulAsyncCallback callback) throws ServiceException;

    /**
     * Http PUT method.<br/>
     * 
     * @param servicePath: request path.
     * @param restParametes: request parameters.
     * @return response.
     * @throws ServiceException
     * @since
     */
    RestfulResponse put(String servicePath, RestfulParametes restParametes) throws ServiceException;

    /**
     * Http PUT method.<br/>
     * 
     * @param servicePath: request path.
     * @param restParametes: request parameters.
     * @param options: request options.
     * @return response.
     * @throws ServiceException
     * @since
     */
    RestfulResponse put(String servicePath, RestfulParametes restParametes, RestfulOptions options)
            throws ServiceException;

    /**
     * Asynchronouse PUT request.<br/>
     * 
     * @param servicePath: request path.
     * @param restParametes: request parameters.
     * @param callback: response callback method.
     * @throws ServiceException
     * @since
     */
    void asyncPut(String servicePath, RestfulParametes restParametes, RestfulAsyncCallback callback)
            throws ServiceException;

    /**
     * Asynchronouse PUT request.<br/>
     * 
     * @param servicePath: request path.
     * @param restParametes: request parameters.
     * @param options: request options.
     * @param callback: response callback method.
     * @throws ServiceException
     * @since
     */
    void asyncPut(String servicePath, RestfulParametes restParametes, RestfulOptions options,
            RestfulAsyncCallback callback) throws ServiceException;

    /**
     * Http POST method.<br/>
     * 
     * @param servicePath: request path.
     * @param restParametes: request parameters.
     * @return response.
     * @throws ServiceException
     * @since
     */
    RestfulResponse post(String servicePath, RestfulParametes restParametes) throws ServiceException;

    /**
     * Http POST method.<br/>
     * 
     * @param servicePath: request path.
     * @param restParametes: request parameters.
     * @param options: request options.
     * @return response.
     * @throws ServiceException
     * @since
     */
    RestfulResponse post(String servicePath, RestfulParametes restParametes, RestfulOptions options)
            throws ServiceException;

    /**
     * Asynchronouse POST request.<br/>
     * 
     * @param servicePath: request path.
     * @param restParametes: request parameters.
     * @param callback: response callback method.
     * @throws ServiceException
     * @since
     */
    void asyncPost(String servicePath, RestfulParametes restParametes, RestfulAsyncCallback callback)
            throws ServiceException;

    /**
     * Asynchronouse POST request.<br/>
     * 
     * @param servicePath: request path.
     * @param restParametes: request parameters.
     * @param options: request options.
     * @param callback: response callback method.
     * @throws ServiceException
     * @since
     */
    void asyncPost(String servicePath, RestfulParametes restParametes, RestfulOptions options,
            RestfulAsyncCallback callback) throws ServiceException;

    /**
     * Http DELETE method.<br/>
     * 
     * @param servicePath: request path.
     * @param restParametes: request parameters.
     * @return response.
     * @throws ServiceException
     * @since
     */
    RestfulResponse delete(String servicePath, RestfulParametes restParametes) throws ServiceException;

    /**
     * Http DELETE method.<br/>
     * 
     * @param servicePath: request path.
     * @param restParametes: request parameters.
     * @param options: request options.
     * @return response.
     * @throws ServiceException
     * @since
     */
    RestfulResponse delete(String servicePath, RestfulParametes restParametes, RestfulOptions options)
            throws ServiceException;

    /**
     * Asynchronouse DELETE request.<br/>
     * 
     * @param servicePath: request path.
     * @param restParametes: request parameters.
     * @param callback: response callback method.
     * @throws ServiceException
     * @since
     */
    void asyncDelete(String servicePath, RestfulParametes restParametes, RestfulAsyncCallback callback)
            throws ServiceException;

    /**
     * Asynchronouse DELETE request.<br/>
     * 
     * @param servicePath: request path.
     * @param restParametes: request parameters.
     * @param options: request options.
     * @param callback: response callback method.
     * @throws ServiceException
     * @since
     */
    void asyncDelete(String servicePath, RestfulParametes restParametes, RestfulOptions options,
            RestfulAsyncCallback callback) throws ServiceException;

    /**
     * Http PATCH method.<br/>
     * 
     * @param servicePath: request path.
     * @param restParametes: request parameters.
     * @return response.
     * @throws ServiceException
     * @since
     */
    RestfulResponse patch(String servicePath, RestfulParametes restParametes) throws ServiceException;

    /**
     * Http PATCH method.<br/>
     * 
     * @param servicePath: request path.
     * @param restParametes: request parameters.
     * @param options: request options.
     * @return response.
     * @throws ServiceException
     * @since
     */
    RestfulResponse patch(String servicePath, RestfulParametes restParametes, RestfulOptions options)
            throws ServiceException;

    /**
     * Asynchronouse PATCH request.<br/>
     * 
     * @param servicePath: request path.
     * @param restParametes: request parameters.
     * @param callback: response callback method.
     * @throws ServiceException
     * @since
     */
    void asyncPatch(String servicePath, RestfulParametes restParametes, RestfulAsyncCallback callback)
            throws ServiceException;

    /**
     * Asynchronouse PATCH request.<br/>
     * 
     * @param servicePath: request path.
     * @param restParametes: request parameters.
     * @param options: request options.
     * @param callback: response callback method.
     * @throws ServiceException
     * @since
     */
    void asyncPatch(String servicePath, RestfulParametes restParametes, RestfulOptions options,
            RestfulAsyncCallback callback) throws ServiceException;
}
