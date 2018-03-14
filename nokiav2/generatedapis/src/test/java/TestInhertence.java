/*
 * Copyright 2016-2017, Nokia Corporation
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

import com.nokia.cbam.lcm.v32.ApiClient;
import com.nokia.cbam.lcm.v32.model.*;
import okhttp3.Headers;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.internal.http.RealResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class TestInhertence {

    /**
     * test OpenStack v2 inheritence handling in serialization and deserialization
     */
    @Test
    public void testOpenStackV2() throws IOException{
        InstantiateVnfRequest req = new InstantiateVnfRequest();
        OPENSTACKV2INFO vim = new OPENSTACKV2INFO();
        req.getVims().add(vim);
        vim.setVimInfoType(VimInfo.VimInfoTypeEnum.OPENSTACK_V2_INFO);
        OpenStackAccessInfoV2 accessInfo = new OpenStackAccessInfoV2();
        accessInfo.setPassword("myPassword");
        vim.setAccessInfo(accessInfo);
        Annotation[] x = new Annotation[0];
        RequestBody requestBody = new ApiClient().getAdapterBuilder().build().requestBodyConverter(InstantiateVnfRequest.class, x, new Annotation[0]).convert(req);
        assertTrue(getContent(requestBody).contains("myPassword"));
        ResponseBody responseBody = toResponse(requestBody);
        InstantiateVnfRequest deserialize = (InstantiateVnfRequest) new ApiClient().getAdapterBuilder().build().responseBodyConverter(InstantiateVnfRequest.class, new Annotation[0]).convert(responseBody);
        assertEquals(1, deserialize.getVims().size());
        OPENSTACKV2INFO deserializedVim = (OPENSTACKV2INFO) deserialize.getVims().get(0);
        assertEquals("myPassword", deserializedVim.getAccessInfo().getPassword());
    }

    private ResponseBody toResponse(RequestBody convert) throws IOException {
        Headers headers = new Headers.Builder().build();
        Buffer buffer = new Buffer();
        convert.writeTo(buffer);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        buffer.copyTo(byteArrayOutputStream);
        BufferedSource response = buffer;
        return new RealResponseBody(headers, response);
    }

    private String getContent(RequestBody requestBody) throws IOException {
        Buffer buffer = new Buffer();
        requestBody.writeTo(buffer);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        buffer.copyTo(byteArrayOutputStream);
        return new String(byteArrayOutputStream.toByteArray());
    }

}
