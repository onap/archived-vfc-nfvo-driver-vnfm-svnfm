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

package org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.testutils;


import java.io.IOException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;



import net.sf.json.JSON;

public final class JsonUtil {

    private static final GsonBuilder builder = new GsonBuilder();
    private static final Gson gson = builder.create();

    public static <T> T unMarshal(String jsonstr, Class<T> type) throws IOException {
        return gson.fromJson(jsonstr, type);
    }


    public static <T> T unMarshal(String jsonstr, Type type) throws IOException {

        return gson.fromJson(jsonstr, type);
    }

    public static String marshal(Object srcObj) throws IOException {
        return srcObj instanceof JSON ? srcObj.toString() : gson.toJson(srcObj);
    }

    public static Gson getgson() {
        return gson;
    }


}
