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

import org.junit.Before;
import org.junit.Test;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.entity.Vnfm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class JsonUtilTest {

    Vnfm vnfm;

    @Before
    public void setUp(){
        vnfm = new Vnfm();
    }

    @Test
    public void testMarshal() throws IOException {
        vnfm.setId("123");
        vnfm.setVersion("V1.0.0");
        JsonUtil.marshal(vnfm);
        assertEquals("123",vnfm.getId());
    }

    @Test
    public void testUnMarshal() throws IOException {
        String jsonValue="{\"id\":\"12345\",\"version\":\"V1.0.0\"}";
        Vnfm actual = JsonUtil.unMarshal(jsonValue,Vnfm.class);
        assertEquals("V1.0.0",actual.getVersion());
    }
    @Test
    public void testUnMarshalWithUnknownField() throws IOException {
        String jsonValue="{\"id\":\"12345\",\"version\":\"V1.0.0\",\"unknownField\":\"unknownValue\"}";
        Vnfm actual = JsonUtil.unMarshal(jsonValue,Vnfm.class);
        assertEquals("V1.0.0",actual.getVersion());
    }
    @Test
    public void testUnMarshalForTypeReference() throws IOException {
        String jsonValue="{\"id\":\"12345\",\"version\":\"V1.0.0\",\"unknownField\":\"unknownValue\"}";
        Map map = JsonUtil.unMarshal(jsonValue, HashMap.class);
        assertEquals("V1.0.0",map.get("version"));
    }
}
