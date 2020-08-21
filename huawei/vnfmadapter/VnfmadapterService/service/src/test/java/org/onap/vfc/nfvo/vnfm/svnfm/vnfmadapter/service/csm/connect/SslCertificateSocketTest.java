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

package org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.csm.connect;

import java.io.IOException;

import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.junit.Test;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.VnfmException;

/**
 * Created by QuanZhong on 2017/3/20.
 */
public class SslCertificateSocketTest {

    @Test
    public void initTest() {
        SslCertificateSocket socket = new SslCertificateSocket();
        try {
            socket.init();
        } catch(VnfmException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void createSocketTest2() {
        SslCertificateSocket socket = new SslCertificateSocket();
        try {
            socket.createSocket("http://127.0.0.1", 1234, null, 4321, null);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void createSocketTest4() {
        SslCertificateSocket socket = new SslCertificateSocket();
        try {
            HttpConnectionParams params = new HttpConnectionParams();
            params.setConnectionTimeout(3000);
            socket.createSocket("http://127.0.0.1", 1234, null, 4321, params);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void createSocketTest6() {
        SslCertificateSocket socket = new SslCertificateSocket();
        try {
            HttpConnectionParams params = new HttpConnectionParams();
            params.setConnectionTimeout(3000);
            socket.createSocket("http://127.0.0.1", 1234);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
