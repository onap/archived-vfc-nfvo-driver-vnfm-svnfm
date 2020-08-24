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

package org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.csm.api;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import junit.framework.Assert;

public class ConnectInfoTest {

    @Test
    public void constructorTestNullUrl(){
        ConnectInfo info = new ConnectInfo(null);
        assertTrue(info.getUrl().equals(""));
    }
    @Test
    public void constructorTestValidUrl(){
        ConnectInfo info = new ConnectInfo("localhost");
        assertTrue(info.getUrl().equals("localhost"));
    }
    @Test
    public void constructorTest2NullData(){
        ConnectInfo info = new ConnectInfo(null,null,null,null);
        assertTrue(info.getUrl().equals("") && info.getUserName().equals("")
                && info.getUserPwd().equals("") && info.getAuthenticateMode().equals(""));
    }
    @Test
    public void constructorTestValidData(){
        ConnectInfo info = new ConnectInfo("localhost","user","password","auth");
        assertTrue(info.getUrl().equals("localhost") && info.getUserName().equals("user")
                && info.getUserPwd().equals("password") && info.getAuthenticateMode().equals("auth"));
    }
    @Test
    public void hashCodeTest(){
        ConnectInfo info1 = new ConnectInfo("localhost","user","password","auth");
        ConnectInfo info2 = new ConnectInfo("localhost","user","password","auth");
        assertTrue(info1.hashCode() == info2.hashCode());
    }

    @Test
    public void equalsTest(){
        ConnectInfo info1 = new ConnectInfo("localhost","user","password","auth");
        ConnectInfo info2 = new ConnectInfo("localhost","user","password","auth");
        assertTrue(info1.equals(info2));
    }
    @Test
    public void equalsTest2(){
        ConnectInfo info1 = new ConnectInfo("localhost","user","password","auth");
        assertTrue(info1.equals(info1));
    }
    @Test
    public void equalsTest3(){
        ConnectInfo info1 = new ConnectInfo("localhost","user","password","auth");
        assertTrue(!info1.equals(null));
    }
    @Test
    public void equalsTest4(){
        ConnectInfo info1 = new ConnectInfo("localhost","user","password","auth");
        assertTrue(!info1.equals(new Object()));
    }
    @Test
    public void equalsTest5(){
        ConnectInfo info1 = new ConnectInfo(null,"user","password","auth");
        ConnectInfo info2 = new ConnectInfo("localhost","user","password","auth");
        assertTrue(!info1.equals(info2));
    }
    @Test
    public void equalsTest6(){
        ConnectInfo info1 = new ConnectInfo("testurl","user","password","auth");
        ConnectInfo info2 = new ConnectInfo("localhost","user","password","auth");
        assertTrue(!info1.equals(info2));
    }
	@Test
	public void setUrlTest() throws NoSuchFieldException, IllegalAccessException {
		ConnectInfo connectInfo = new ConnectInfo("localhost");
		connectInfo.setUrl("localhost");
		assertTrue(connectInfo.getUrl().equals("localhost"));
	}
	@Test
	public void setUserNameTest() throws NoSuchFieldException, IllegalAccessException {
		ConnectInfo connectInfo = new ConnectInfo("localhost");
		connectInfo.setUserName("user");
		assertTrue(connectInfo.getUserName().equals("user"));
	}
	@Test
	public void setUserPwdTest() throws NoSuchFieldException, IllegalAccessException {
		ConnectInfo connectInfo = new ConnectInfo("localhost");
		connectInfo.setUserPwd("password");
		assertTrue(connectInfo.getUserPwd().equals("password"));
	}
	@Test
	public void setAuthenticateModeTest() throws NoSuchFieldException, IllegalAccessException {
		ConnectInfo connectInfo = new ConnectInfo("localhost");
		connectInfo.setAuthenticateMode("auth");
		assertTrue(connectInfo.getAuthenticateMode().equals("auth"));
	}
	@Test
	public void testToString() {
        ConnectInfo connectInfo = new ConnectInfo("localhost","user","password","auth");
		String expected = connectInfo.toString(); 
		Assert.assertEquals(expected, connectInfo.toString());
	}
}
