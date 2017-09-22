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

package com.nokia.vfcadaptor.http.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.Gson;
import com.nokia.vfcadaptor.vnfmdriver.bo.InstantiateVnfRequest;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"file:src/main/webapp/WEB-INF/mvc-servlet.xml"})
@WebAppConfiguration
public class HttpRequestProcessorTest {
	private String url;
	private String basicUrl="http://localhost:8080/NvfmDriver/api/nokiavnfm/v1";
    private String funcPath;
    
	private Gson gson = new Gson();
    
	@Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;
    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();   //构造MockMvc
    }
    
    @Test
    public void testP()
    {
    	String message = "{\"vnfInstanceName\":\"vnfInstanceName_001\",\"vnfPackageId\":\"1\"}";
    	InstantiateVnfRequest request = gson.fromJson(message, InstantiateVnfRequest.class);
    	
    	System.out.println("vnfInstanceName = " + request.getVnfInstanceName());
    	
    	String result;
		try {
			result = mockMvc.perform(MockMvcRequestBuilders.post("/nokiavnfm/v1/vnfmId_001/vnfs")  
			        .contentType(MediaType.APPLICATION_JSON).content(message)  
			        .accept(MediaType.APPLICATION_JSON)) //执行请求  
			.andReturn().getResponse().getContentAsString();
			System.out.println("result = " + result);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
    	
    	
    }
//    
//	@Test
//	public void testProcess() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, ClientProtocolException, IOException
//	{
//		funcPath = "/vnfmId_001/vnfs";
//    	url = basicUrl + funcPath;
//        String message = "{\"vnfInstanceName\":\"vnfInstanceName_001\",\"vnfPackageId\":\"1\"}";
//        
//		HttpClientBuilder httpClientBuilder = HttpClientUtils.createHttpClientBuilder();
//		HttpRequestProcessor processor = new HttpRequestProcessor(httpClientBuilder, RequestMethod.POST);
//		processor.addPostEntity(message);
//		String result = processor.process(url);
//		
//		System.out.println(result);
//	}
//	
//	@Test
//	public void testHttps() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, ClientProtocolException, IOException
//	{
//		url = "https://www.baidu.com";
//		HttpClientBuilder httpClientBuilder = HttpClientUtils.createHttpClientBuilder();
//		HttpRequestProcessor processor = new HttpRequestProcessor(httpClientBuilder, RequestMethod.GET);
//		
//		String result = processor.process(url);
//		
//		System.out.println("The result is :" + result);
//	}
//	@Test
//	public void testHome() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, ClientProtocolException, IOException
//	{
//		url = "http://172.24.208.168/";
//		HttpClientBuilder httpClientBuilder = HttpClientUtils.createHttpClientBuilder();
//		HttpRequestProcessor processor = new HttpRequestProcessor(httpClientBuilder, RequestMethod.GET);
//		
//		String result = processor.process(url);
//		
//		System.out.println("The result is :" + result);
//	}
}
