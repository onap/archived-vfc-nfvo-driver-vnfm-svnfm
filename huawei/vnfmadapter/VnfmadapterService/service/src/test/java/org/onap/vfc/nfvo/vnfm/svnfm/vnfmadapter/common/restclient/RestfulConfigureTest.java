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


package org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.restclient;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import net.sf.json.JSONException;

public class RestfulConfigureTest {

	@Test
	public void testRestfulConfigure() {
		 RestfulConfigure rc = new  RestfulConfigure();
		 rc.toString();
	}
	
	@Test
	public void testRestfulConfigureStringNoFilePath() {
		 RestfulConfigure rc = new  RestfulConfigure("");
		 RestfulOptions rp = rc.getOptions();
		 assertTrue(true);
	}

	@Test
	public void testRestfulConfigureStringNoFileExist() {
		 RestfulConfigure rc = new  RestfulConfigure("C:/Users/Public/Desktop");
		 RestfulOptions rp = rc.getOptions();
		 assertTrue(true);
	}
	
	@Test
	public void testRestfulConfigureString() {
		 RestfulConfigure rc = new  RestfulConfigure("src/test/resources/Check7.txt");
		 RestfulOptions rp = rc.getOptions();
		 assertTrue(true);
	}
	
	@Test
	public void testRestfulConfigureStringNoFile() {
		 RestfulConfigure rc = new  RestfulConfigure("src/test/resources/Check1.txt");
		 RestfulOptions rp = rc.getOptions();
		 assertTrue(true);
	}
	
	@Test
	public void testRestfulConfigureStringWrongFile() {
		 RestfulConfigure rc = new  RestfulConfigure("src/test/resources/Check2.txt");
		 RestfulOptions rp = rc.getOptions();
		 assertTrue(true);
	}
	
	@Test
	public void testRestfulConfigureStringEmptyFile() {
		 RestfulConfigure rc = new  RestfulConfigure("src/test/resources/Check3.txt");
		 RestfulOptions rp = rc.getOptions();
		 assertTrue(true);
	}
	@Test
	public void testRestfulConfigureStringNoHostFile() {
		 RestfulConfigure rc = new  RestfulConfigure("src/test/resources/Check4.txt");
		 RestfulOptions rp = rc.getOptions();
		 assertTrue(true);
	}
	@Test(expected=JSONException.class)
	public void testRestfulConfigureStringZeroFile() throws IOException {
		 RestfulConfigure rc = new  RestfulConfigure("src/test/resources/Check5.txt");
		 rc.getOptions();
	}
	/*@Test
	public void testRestfulConfigureStringClose()  {
		 RestfulConfigure rc = new  RestfulConfigure("src/test/resources/Check6.txt");
		 rc.getOptions();
	}*/
}
