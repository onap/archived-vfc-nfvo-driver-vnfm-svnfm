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

package org.openo.nfvo.vnfmadapter.util;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import org.junit.Assert;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.baseservice.roa.util.restclient.RestfulParametes;
import org.openo.baseservice.roa.util.restclient.RestfulResponse;
import org.openo.sdno.testframework.checker.DefaultChecker;
import org.openo.sdno.testframework.http.model.HttpModelUtils;
import org.openo.sdno.testframework.http.model.HttpRequest;
import org.openo.sdno.testframework.http.model.HttpResponse;
import org.openo.sdno.testframework.http.model.HttpRquestResponse;
import org.openo.sdno.testframework.restclient.HttpRestClient;
import org.openo.sdno.testframework.testmanager.TestManager;
import org.openo.sdno.testframework.util.file.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <br>
 * <p>
 * </p>
 * 
 * @author
 * @version NFVO 0.5 Sep 21, 2016
 */
public class MyTestManager extends TestManager {

    private HttpRestClient restClient;

    public MyTestManager() {
        restClient = new HttpRestClient();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MyTestManager.class);

    /**
     * <br>
     * 
     * @param file
     * @return
     * @throws ServiceException
     * @since NFVO 0.5
     */
    @Override
    public HttpResponse execTestCase(File file) throws ServiceException {
        String content = FileUtils.readFromJson(file);
        HttpRquestResponse httpObject = HttpModelUtils.praseHttpRquestResponse(content);
        return send(httpObject.getRequest(), httpObject.getResponse());
    }

    private HttpResponse send(HttpRequest request, HttpResponse response) {
        try {
            RestfulResponse responseResult = doSend(request);
            DefaultChecker checker = new MyChecker(response);
            HttpResponse httpResponse = HttpModelUtils.convertResponse(responseResult);
            Assert.assertEquals(Boolean.valueOf(checker.check(httpResponse)), Boolean.valueOf(true));
            return httpResponse;
        } catch(ServiceException e) {
            LOGGER.error("call the restful interface failed.", e);
        }
        return null;
    }

    private RestfulResponse doSend(HttpRequest request) throws ServiceException {
        String url = request.getUri();
        String method = request.getMethod();
        String body = request.getData();
        RestfulParametes restfulParametes = new RestfulParametes();
        Map requestHeaders = request.getHeaders();
        if(null != requestHeaders) {
            java.util.Map.Entry curEntity;
            for(Iterator iterator = requestHeaders.entrySet().iterator(); iterator.hasNext(); restfulParametes
                    .putHttpContextHeader((String)curEntity.getKey(), (String)curEntity.getValue()))
                curEntity = (java.util.Map.Entry)iterator.next();

        }
        Map paramMap = request.getQueries();
        if(null != paramMap)
            restfulParametes.setParamMap(paramMap);
        if(null != body)
            restfulParametes.setRawData(body);
        return callRestfulMotheds(url, method, restfulParametes);
    }

    private RestfulResponse callRestfulMotheds(String url, String method, RestfulParametes restfulParametes)
            throws ServiceException {
        String s = method;
        byte byte0 = -1;
        switch(s.hashCode()) {
            case 3446944:
                if(s.equals("post"))
                    byte0 = 0;
                break;

            case 102230:
                if(s.equals("get"))
                    byte0 = 1;
                break;

            case 111375:
                if(s.equals("put"))
                    byte0 = 2;
                break;

            case -1335458389:
                if(s.equals("delete"))
                    byte0 = 3;
                break;

            case 3198432:
                if(s.equals("head"))
                    byte0 = 4;
                break;

            case 106438728:
                if(s.equals("patch"))
                    byte0 = 5;
                break;
        }
        switch(byte0) {
            case 0: // '\0'
                return restClient.post(url, restfulParametes);

            case 1: // '\001'
                return restClient.get(url, restfulParametes);

            case 2: // '\002'
                return restClient.put(url, restfulParametes);

            case 3: // '\003'
                return restClient.delete(url, restfulParametes);

            case 4: // '\004'
                return restClient.head(url, restfulParametes);

            case 5: // '\005'
                return restClient.patch(url, restfulParametes);
        }
        LOGGER.error("The method is unsupported.");
        throw new ServiceException("The method is unsupported.");
    }

}
