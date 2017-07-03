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

import org.openo.sdno.testframework.checker.IChecker;
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
public class GetSuccessChecker implements IChecker {

    String addMsg = "org.openo.nfvo.resmanage.common.add.success";

    @Override
    public boolean check(HttpResponse response) {
        System.out.println("====================");
        System.out.println(response);
        String data = response.getData();
        System.out.println("********************");
        System.out.println(data);
//        JSONObject dataObj = JSONObject.fromObject(data);
//        if(response.getStatus() == 200 && !dataObj.isEmpty()) {
//            System.out.println(dataObj.isEmpty());
//            return true;
//        }
        if(response.getStatus() == 200 && data != null && !data.isEmpty()) {
        	return true;
        }
        return false;
    }
}
