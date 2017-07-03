/*
 * Copyright 2016-2017 Huawei Technologies Co., Ltd.
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.openo.nfvo.vnfmadapter.common.VnfmException;
import org.openo.nfvo.vnfmadapter.service.constant.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SSL Socket Factory.
 * .</br>
 *
 * @author
 * @version     NFVO 0.5  Sep 14, 2016
 */
public class SslProtocolSocketFactory {

    private static final Logger LOG = LoggerFactory.getLogger(SslProtocolSocketFactory.class);

    private static final Map<String, ProtocolSocketFactory> SOCKMAP =
            new ConcurrentHashMap<String, ProtocolSocketFactory>(2);

    private static SslProtocolSocketFactory singleinstance = null;

    /**
     * Generate instance of SslProtocolSocketFactory
     * <br>
     *
     * @return
     * @since  NFVO 0.5
     */
    public static synchronized SslProtocolSocketFactory getInstance() {
        if(singleinstance == null) {
            singleinstance = new SslProtocolSocketFactory();
        }
        return singleinstance;
    }

    /**
     *
     * <br>
     *
     * @param authenticateMode
     * @return
     * @throws VnfmException
     * @since  NFVO 0.5
     */
    public synchronized ProtocolSocketFactory get(String authenticateMode) throws VnfmException {
        if(SOCKMAP.get(authenticateMode) == null) {
            if(Constant.ANONYMOUS.equals(authenticateMode)) {
                SslAnonymousSocket anonymous = new SslAnonymousSocket();
                anonymous.init();
                SOCKMAP.put(Constant.ANONYMOUS, anonymous);
            }else if (Constant.CERTIFICATE.equals(authenticateMode)){
                SslCertificateSocket certificateSocket = new SslCertificateSocket();
                certificateSocket.init();
                SOCKMAP.put(Constant.CERTIFICATE, certificateSocket);
            } else {
                LOG.error("funtion=get, msg=ProtocolSocketFactory Unknown AuthenticateMode={}", authenticateMode);
                throw new VnfmException(String.format("Illegal Auth mode", authenticateMode));
            }
        }

        return SOCKMAP.get(authenticateMode);
    }

    /**
     * Refresh local socket map
     * <br>
     *
     * @param autherMode
     * @throws VnfmException
     * @since  NFVO 0.5
     */
    public synchronized void refresh(String autherMode) throws VnfmException {
        if(Constant.ANONYMOUS.equals(autherMode)) {
            SslAnonymousSocket anonymous = new SslAnonymousSocket();
            anonymous.init();
            SOCKMAP.put(Constant.ANONYMOUS, anonymous);
        }
    }
}
