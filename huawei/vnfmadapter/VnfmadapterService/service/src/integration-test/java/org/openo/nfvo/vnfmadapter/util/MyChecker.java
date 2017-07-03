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

import org.openo.sdno.testframework.checker.DefaultChecker;
import org.openo.sdno.testframework.http.model.HttpResponse;

import net.sf.json.JSONObject;

/**
 * <br>
 * <p>
 * </p>
 * 
 * @author
 * @version NFVO 0.5 Sep 21, 2016
 */
public class MyChecker extends DefaultChecker {

    private HttpResponse expectedResponse;

    /**
     * Constructor<br>
     * <p>
     * </p>
     * 
     * @param expectedResponse
     * @since NFVO 0.5
     */
    public MyChecker(HttpResponse expectedResponse) {
        super(expectedResponse);
        this.expectedResponse = expectedResponse;
    }

    /**
     * <br>
     * 
     * @param response
     * @return
     * @since NFVO 0.5
     */
    @Override
    public boolean check(HttpResponse response) {
    	System.out.println("expectedResponse:");
    	System.out.println(expectedResponse.getData());
    	System.out.println("response:");
        System.out.println(JSONObject.fromObject(response.getData()));
        return (JSONObject.fromObject(expectedResponse.getData())).equals(JSONObject.fromObject(response.getData()));
    }

}
