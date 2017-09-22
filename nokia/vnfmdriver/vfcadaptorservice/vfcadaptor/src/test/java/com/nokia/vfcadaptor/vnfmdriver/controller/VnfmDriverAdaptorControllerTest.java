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
package com.nokia.vfcadaptor.vnfmdriver.controller;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Before;
import org.junit.Test;

public class VnfmDriverAdaptorControllerTest extends BaseControllerTestCase{

	private String url;
	private String basicUrl="http://localhost:8080/NvfmDriver/api/nokiavnfm/v1";
    private String funcPath;
    
    @Before
    public void setUp()
    {
        isHttpsProtocol = false;
    }
    
    @Test
    public void testInstantiateVnf() throws URISyntaxException, ClientProtocolException, IOException {
    	funcPath = "/vnfmId_001/vnfs";
    	url = basicUrl + funcPath;
        String message = "{\"vnfInstanceName\":\"vnfInstanceName_001\",\"vnfPackageId\":\"1\"}";
        
        String responseContent = sendPostMsg(message, url);
        System.out.println("-------------------------------");
        System.out.println( " Initiate Response is " + responseContent);
    }
    
    @Test
    public void testTerminateVnf() throws URISyntaxException, ClientProtocolException, IOException {
    	funcPath = "/vnfmId_001/vnfs/vnfInstanceId_001/terminate";
    	url = basicUrl + funcPath;
    	String message = "{\"terminationType\":\"graceful\"}";
    	String responseContent = sendPostMsg(message, url);
    	System.out.println("-------------------------------");
    	System.out.println("Terminate Response is " + responseContent);
    }
    
   @Test
    public void testQueryVnf() throws URISyntaxException, ClientProtocolException, IOException {
    	funcPath = "/vnfmId_001/vnfs/vnfInstanceId_001";
    	url = basicUrl + funcPath;
    	String message = "{\"vnfInfo\":\"{\"nfInstanceId\":\"1\",\"vnfInstanceName\":\"vFW\",\"vnfInstanceDescription\":\"vFW in Nanjing TIC Edge\",\"vnfdId\":\"1\"}\"}";
    	String responseContent = sendGetMsg(message, url);
    	
    	System.out.println("-------------------------------");
    	System.out.println("QueryResponse is " + responseContent);
    }
   
   @Test
   public void testOperStatusVnf() throws URISyntaxException, ClientProtocolException, IOException {
   	funcPath = "/vnfmId_001/jobs/jobId_001&responseId=responseId_001";
   	url = basicUrl + funcPath;
   	String message = "{\"jobId\":\"12345\",\"responseDescriptor\":\"{\"progress\":\"40\",\"status\":\"proccessing\"}\"}";
   	String responseContent = sendGetMsg(message, url);
   	System.out.println("-------------------------------");
   	System.out.println(" operStatus Response is " + responseContent);
   }
   
   @Test
   public void testScaleVnf() throws URISyntaxException, ClientProtocolException, IOException {
   	funcPath = "/vnfmId_001/vnfs/vnfInstanceId_001/scale";
   	url = basicUrl + funcPath;
   	String message = "{\"type\":\"12345\",\"aspectId\":\"145\"}";
   	String responseContent = sendPostMsg(message, url);
   	System.out.println("-------------------------------");
   	System.out.println(" Scale Response is " + responseContent);
   }
   
   @Test
   public void testHealVnf() throws URISyntaxException, ClientProtocolException, IOException {
   	funcPath = "/vnfmId_001/vnfs/vnfInstanceId_001/heal";
   	url = basicUrl + funcPath;
   	String message = "{\"action\":\"12345\"}";
   	String responseContent = sendPostMsg(message, url);
   	System.out.println("-------------------------------");
   	System.out.println(" Heal Response is " + responseContent);
   }

}
