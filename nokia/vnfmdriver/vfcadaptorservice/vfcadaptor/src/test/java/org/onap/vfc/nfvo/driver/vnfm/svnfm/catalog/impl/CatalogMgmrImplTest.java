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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.catalog.impl;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.catalog.bo.CatalogQueryVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.catalog.bo.entity.ImageInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.catalog.bo.entity.VnfInstanceInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.catalog.bo.entity.VnfPackageInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.common.bo.AdaptorEnv;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonEnum;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonEnum.Deletionpending;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.http.client.HttpClientProcessorInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.http.client.HttpResult;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.Gson;

public class CatalogMgmrImplTest {
	@InjectMocks
	private CatalogMgmrImpl catalogMgmr;

	@Mock
	private HttpClientProcessorInf httpClientProcessor;
	
	private String vnfPackageId = "vnfPackageId_001";
	
	private Gson gson = new Gson();
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		AdaptorEnv env = new AdaptorEnv();
		catalogMgmr.setAdaptorEnv(env);
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testQueryVnfPackage() throws ClientProtocolException, IOException
	{
		CatalogQueryVnfResponse response = new CatalogQueryVnfResponse();
		List<VnfInstanceInfo> vnfInstanceInfos = new ArrayList<VnfInstanceInfo>();
		VnfInstanceInfo vnfInstanceInfo = new VnfInstanceInfo();
		vnfInstanceInfo.setVnfInstanceId("vnfInstanceId");
		vnfInstanceInfo.setVnfInstanceName("vnfInstanceName");
		vnfInstanceInfos.add(vnfInstanceInfo );
		
		
		response.setVnfInstanceInfo(vnfInstanceInfos);
		
		response.setCsarId("csarId");
		ImageInfo imageInfo = new ImageInfo();
		imageInfo.setFileName("fileName");
		imageInfo.setImageId("imageId");
		imageInfo.setIndex("index");
		imageInfo.setStatus("status");
		imageInfo.setTenant("tenant");
		imageInfo.setVimId("vimId");
		imageInfo.setVimUser("vimUser");
		response.setImageInfo(imageInfo);
		
		VnfPackageInfo packageInfo = new VnfPackageInfo();
		packageInfo.setDownloadUri("1.3.5.6");
		packageInfo.setName("name");
		packageInfo.setOnBoardState("onBoardState");
		packageInfo.setVnfdId("vnfdId");
		packageInfo.setVnfdProvider("vnfdProvider");
		packageInfo.setVnfdVersion("vnfdVersion");
		packageInfo.setVnfVersion("vnfVersion");
		Deletionpending deletionPending = CommonEnum.Deletionpending.fALSE;
		packageInfo.setDeletionPending(deletionPending );
		
		response.setPackageInfo(packageInfo );
		
		String json = gson.toJson(packageInfo);
		
		HttpResult httpResult = new HttpResult();
		httpResult.setContent(json);
		
		when(httpClientProcessor.process(Mockito.anyString(), Mockito.any(RequestMethod.class), Mockito.any(HashMap.class), Mockito.anyString())).thenReturn(httpResult);
		VnfPackageInfo packageInfo1 = catalogMgmr.queryVnfPackage(vnfPackageId);
		Assert.assertEquals("1.3.5.6", packageInfo.getDownloadUri());
	}

}
