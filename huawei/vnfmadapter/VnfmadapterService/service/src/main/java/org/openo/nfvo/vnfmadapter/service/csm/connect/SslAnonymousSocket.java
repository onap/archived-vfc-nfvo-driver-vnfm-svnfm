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

package org.openo.nfvo.vnfmadapter.service.csm.connect;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.ControllerThreadSocketFactory;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import org.openo.nfvo.vnfmadapter.common.VnfmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create socket with Anonymous
 */
public class SslAnonymousSocket extends AbstractSslContext implements SecureProtocolSocketFactory {

    private static final Logger LOG = LoggerFactory.getLogger(SslAnonymousSocket.class);

    private SSLSocketFactory sslSocketFactory = null;

    /**
     * Initialize
     * <br>
     *
     * @throws VnfmException
     * @since  NFVO 0.5
     */
    public void init() throws VnfmException {
        try {
            sslSocketFactory = getAnonymousSSLContext().getSocketFactory();
        } catch(GeneralSecurityException e) {
            LOG.error("function=init, get Anonymous SSLContext exception, exceptioninfo", e);
            throw (VnfmException)new VnfmException().initCause(e);
        }
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return sslSocketFactory.createSocket(host, port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress clientHost, int clientPort) throws IOException {
        return sslSocketFactory.createSocket(host, port, clientHost, clientPort);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localAddress, int localPort,
            HttpConnectionParams params) throws IOException, ConnectTimeoutException {
        if(params == null) {
            throw new IOException("Illegal socket parameters!");
        } else {
            int timeout = params.getConnectionTimeout();

            if(timeout == 0) {
                return createSocket(host, port, localAddress, localPort);
            } else {
                return ControllerThreadSocketFactory.createSocket(this, host, port, localAddress, localPort, timeout);
            }
        }
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        return sslSocketFactory.createSocket(socket, host, port, autoClose);
    }
}
