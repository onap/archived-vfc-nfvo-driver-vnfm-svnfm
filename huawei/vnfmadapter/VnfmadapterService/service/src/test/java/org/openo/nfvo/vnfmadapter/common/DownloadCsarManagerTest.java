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
package org.openo.nfvo.vnfmadapter.common;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by QuanZhong on 2017/3/17.
 */
public class DownloadCsarManagerTest {

    @Test
    public void getFileNameTest(){
        ProtocolVersion version = new ProtocolVersion("HTTP",1,1);
        StatusLine sl = new BasicStatusLine(version,200,"success");
        HttpResponse response = new BasicHttpResponse(sl);
        response.setHeader("Content-Disposition","filename");
        DownloadCsarManager.getFileName(response);
    }

    @Test
    public void downloadTest(){
        DownloadCsarManager.download("http://www.baidu.com");
        DownloadCsarManager.download("http://www.baidu.com","/opt");
        DownloadCsarManager.getRandomFileName();
    }
    @Test
    public void getFilePath(){
        ProtocolVersion version = new ProtocolVersion("HTTP",1,1);
        StatusLine sl = new BasicStatusLine(version,200,"success");
        HttpResponse response = new BasicHttpResponse(sl);
        response.setHeader("Content-Disposition","filename");
        DownloadCsarManager.getFilePath(response);
    }
    @Test
    public void testUnzip(){
        DownloadCsarManager.unzipCSAR("test.zip","/opt");
    }

    @Test
    public void testJsonUtils(){
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setContent("[{'abc':123}]".getBytes());
        VnfmJsonUtil.getJsonFromContexts(req);
    }
    @Test
    public void testJsonUtils2(){
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setContent("{'abc':123}".getBytes());
        VnfmJsonUtil.getJsonFromContexts(req);
    }

}
