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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.http.client;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

@Component
public class HttpClientProcessorImpl implements HttpClientProcessorInf{
	private static final Logger logger = LoggerFactory.getLogger(HttpClientProcessorImpl.class);

	@Autowired
	private HttpClientBuilder httpClientBuilder;
	
	public HttpResult process(String url, RequestMethod methodType, HashMap<String, String> headerMap, String bodyString) throws IOException
	{
		HttpRequestProcessor processor = new HttpRequestProcessor(httpClientBuilder, methodType);
		//Map<String, String> headerMap = new HashMap<String, String>();
		if(headerMap != null && !headerMap.isEmpty())
		{
			/*for(String key : headerMap.keySet())
			{
				processor.addHdeader(key, headerMap.get(key));
			}*/

			for (HashMap.Entry<String, String> entry : headerMap.entrySet()) {
				processor.addHeader(entry.getKey(), entry.getValue());
			}
			
			if(bodyString.length() > 0 && !"null".equalsIgnoreCase(bodyString))
			{
				processor.addPostEntity(bodyString);
			}
			
		}
		return processor.process(url);
	}
	
	public HttpResult processBytes(String url, RequestMethod methodType, HashMap<String, String> headerMap, byte[] byteArray) throws IOException
	{
		HttpRequestProcessor processor = new HttpRequestProcessor(httpClientBuilder, methodType);
		if(headerMap != null && !headerMap.isEmpty())
		{
			for (HashMap.Entry<String, String> entry : headerMap.entrySet()) {
				processor.addHeader(entry.getKey(), entry.getValue());
			} //Iterate over the "entrySet" instead of the "keySet"

			/*for(String key : headerMap.keySet())
			{
				processor.addHeader(key, headerMap.get(key));
			}*/
			
			if(null != byteArray && byteArray.length > 0)
			{
				processor.addBytesPostEntity(byteArray);
			}
			
		}
		return processor.process(url);
	}
	
	public void setHttpClientBuilder(HttpClientBuilder httpClientBuilder) {
		this.httpClientBuilder = httpClientBuilder;
	}
	
	public static boolean downLoadFromUrl(String urlStr, String fileName, String savePath) 
	{
		try 
		{
	        URL url = new URL(urlStr);  
	        HttpURLConnection conn = (HttpURLConnection)url.openConnection();  
	        conn.setConnectTimeout(20*1000);
	        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
	        InputStream inputStream = conn.getInputStream();  
	        byte[] getData = readInputStream(inputStream);    
	        File saveDir = new File(savePath);
	        if(!saveDir.exists()){
	            saveDir.mkdir();
	        }	   
		File file = new File(saveDir+File.separator+fileName);    
		try(FileOutputStream fos = new FileOutputStream(file)){
			fos.write(getData); 
		}
	        
	        if(inputStream!=null){
	            inputStream.close();
	        }
		}
		catch ( IOException e ) {
			logger.info("write file fail", e);  
			return false;
		}
        
        logger.info("info: "+ urlStr + " download success"); 
        return true;
    }


    public static byte[] readInputStream(InputStream inputStream) throws IOException {  
        byte[] buffer = new byte[1024];  
        int len = 0;  
        try(ByteArrayOutputStream bos = new ByteArrayOutputStream()){
        while((len = inputStream.read(buffer)) != -1) {  
            bos.write(buffer, 0, len);  
        }  
        return bos.toByteArray();  
	}
    }

	@Override
	public HttpResult process(String url) throws ClientProtocolException, IOException {
		return process(url, RequestMethod.GET, null, null);
	}
}
