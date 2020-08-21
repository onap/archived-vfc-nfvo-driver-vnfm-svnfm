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

package org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.csm.connect;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.junit.Before;
import org.junit.Test;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.VnfmException;

public class SslAnonymousSocketTest {
	
	@Before
	public void init() throws VnfmException {
		SslAnonymousSocket sslsocket = new SslAnonymousSocket();
		sslsocket.init();

	}
    @Test(expected = IOException.class)
    public void createSocketTestException() throws IOException, ConnectTimeoutException {
        String host= "localhost";
        int port = 29912;
        InetAddress localAddress = null;
        int localPort = 4859;
        HttpConnectionParams params = null;
        SslAnonymousSocket sslsocket = new SslAnonymousSocket();
        Socket socket  = sslsocket.createSocket(host, port, localAddress, localPort, params);
    }

    @Test(expected = Exception.class)
    public void createSocketTestException2() throws IOException, ConnectTimeoutException {
        String host= "localhost";
        int port = 29912;
        InetAddress localAddress = null;
        int localPort = 4859;
        HttpConnectionParams params = new HttpConnectionParams();
        SslAnonymousSocket sslsocket = new SslAnonymousSocket();
        Socket socket  = sslsocket.createSocket(host, port, localAddress, localPort, params);
    }
    
}
