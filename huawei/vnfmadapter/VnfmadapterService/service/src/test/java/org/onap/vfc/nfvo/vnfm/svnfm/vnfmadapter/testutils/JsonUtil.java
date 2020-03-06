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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.sf.json.JSON;

public final class JsonUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static <T> T unMarshal(String jsonstr, Class<T> type) throws IOException {
        return MAPPER.readValue(jsonstr, type);
    }

    public static <T> T unMarshal(String jsonstr, TypeReference<T> type) throws IOException {
        return MAPPER.readValue(jsonstr, type);
    }

    public static String marshal(Object srcObj) throws IOException {
        return srcObj instanceof JSON ? srcObj.toString() : MAPPER.writeValueAsString(srcObj);
    }

    public static ObjectMapper getMapper() {
        return MAPPER;
    }

    static {
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
    }
}