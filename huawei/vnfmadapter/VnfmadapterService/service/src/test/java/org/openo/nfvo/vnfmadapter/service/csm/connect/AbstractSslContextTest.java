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

package org.openo.nfvo.vnfmadapter.service.csm.connect;

import net.sf.json.JSONObject;
import org.junit.Test;

/**
 * Created by QuanZhong on 2017/3/20.
 */
public class AbstractSslContextTest {
    @Test
    public void createKeyManagerTest(){
        AbstractSslContext asc = new AbstractSslContext();
        JSONObject json = new JSONObject();
        json.put("keyStore","");
        json.put("keyStorePass","");
        json.put("keyStoreType","");
        asc.createKeyManager(json);
    }

    @Test
    public void createTrustManagerTest(){
        AbstractSslContext asc = new AbstractSslContext();
        JSONObject json = new JSONObject();
        json.put("trustStore","");
        json.put("trustStorePass","");
        json.put("trustStoreType","");
        asc.createTrustManager(json);
    }

}
