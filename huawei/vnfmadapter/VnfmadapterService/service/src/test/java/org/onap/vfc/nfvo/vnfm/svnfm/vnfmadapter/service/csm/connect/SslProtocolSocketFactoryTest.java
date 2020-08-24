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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.junit.Test;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.VnfmException;

public class SslProtocolSocketFactoryTest {

    @Test
    public void getInstanceTest(){
        SslProtocolSocketFactory factory = SslProtocolSocketFactory.getInstance();
        assertNotNull(factory);
    }

    @Test(expected = VnfmException.class)
    public void getTest() throws VnfmException{
        ProtocolSocketFactory factory =  SslProtocolSocketFactory.getInstance().get("test");
    }
    @Test
    public void refreshTestException() throws VnfmException{
        SslProtocolSocketFactory.getInstance().refresh("test");
        assertTrue(true);
    }
    @Test
    public void refreshTest() throws VnfmException{
        SslProtocolSocketFactory.getInstance().refresh("Anonymous");
        assertTrue(true);
    }
    @Test
    public void getAnonymousTest() throws VnfmException{
    	String authenticateMode="Anonymous";
        SslProtocolSocketFactory.getInstance().get(authenticateMode);
        assertTrue(true);
    }
    @Test
    public void getCertificateTest() throws VnfmException{
    	String authenticateMode="Certificate";
        SslProtocolSocketFactory.getInstance().get(authenticateMode);
        assertTrue(true);
    }
}
