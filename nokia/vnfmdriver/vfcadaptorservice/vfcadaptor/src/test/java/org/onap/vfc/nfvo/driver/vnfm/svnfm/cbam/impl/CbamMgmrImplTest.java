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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.impl;

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
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMCreateSubscriptionRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMCreateSubscriptionResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMCreateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMCreateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMHealVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMHealVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMInstantiateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMInstantiateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMModifyVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMModifyVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMQueryOperExecutionResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMQueryVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMScaleVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMScaleVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMTerminateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMTerminateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMVnfNotificationRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMVnfNotificationResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.ExtManagedVirtualLinkData;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.ExtVirtualLinkData;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.NetworkAddress;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.Subscription;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.VimComputeResourceFlavour;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.VimInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.VimSoftwareImage;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.VnfExtCpData;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.VnfcResourceInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.ZoneInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.common.bo.AdaptorEnv;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.http.client.HttpClientProcessorInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.http.client.HttpResult;
import org.springframework.web.bind.annotation.RequestMethod;

public class CbamMgmrImplTest {
	@InjectMocks
	private CbamMgmrImpl cbamMgmr;
	
	@Mock
	private HttpClientProcessorInf httpClientProcessor;
	
	private String vnfInstanceId = "vnfInstanceId_001";
	
	private String json = "{"
			+ "'access_token':'1234567',"
			+ "'id':'id',"
			+ "'name':'name',"
			+ "'description':'description',"
			+ "'vnfdId':'vnfdId',"
			+ "'vnfProvider':'vnfProvider',"
			+ "'onboardedVnfPkgInfoId':'onboardedVnfPkgInfoId',"
			+ "'vnfProductName':'vnfProductName'"
			+ "}";
	
	HttpResult httpResult = new HttpResult();
	private int statusCode = 200;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		AdaptorEnv env = new AdaptorEnv();
		cbamMgmr.setAdaptorEnv(env);
		
		httpResult.setStatusCode(statusCode);
		httpResult.setContent(json);
		
		when(httpClientProcessor.process(Mockito.anyString(), Mockito.any(RequestMethod.class), Mockito.any(HashMap.class), Mockito.anyString())).thenReturn(httpResult);
	}
	
	@Test
	public void testCreateVnfSuccess() throws ClientProtocolException, IOException
	{
		httpResult.setStatusCode(201);
		
		when(httpClientProcessor.process(Mockito.anyString(), Mockito.any(RequestMethod.class), Mockito.any(HashMap.class), Mockito.anyString())).thenReturn(httpResult);
		
		CBAMCreateVnfRequest cbamRequest = new CBAMCreateVnfRequest();
		CBAMCreateVnfResponse response = cbamMgmr.createVnf(cbamRequest);
	}
	
	@Test
	public void testCreateVnfError() throws ClientProtocolException, IOException
	{
		CBAMCreateVnfRequest cbamRequest = new CBAMCreateVnfRequest();
		CBAMCreateVnfResponse response = cbamMgmr.createVnf(cbamRequest);
	}
	
	@Test
	public void testInstantiateVnfSucess() throws ClientProtocolException, IOException
	{
        httpResult.setStatusCode(202);
		
		when(httpClientProcessor.process(Mockito.anyString(), Mockito.any(RequestMethod.class), Mockito.any(HashMap.class), Mockito.anyString())).thenReturn(httpResult);
		
		CBAMInstantiateVnfRequest cbamRequest = new CBAMInstantiateVnfRequest();
		cbamRequest.setFlavourId("flavourId");
		cbamRequest.setGrantlessMode(false);
		
		VimInfo vimInfo = new VimInfo();
		vimInfo.setId("id");
		vimInfo.setInterfaceEndpoint("interfaceEndpoint");
		
		List<VimInfo> vims = new ArrayList<>();
		vims.add(vimInfo);
		cbamRequest.setVims(vims);
		
		cbamRequest.setInstantiationLevelId("instantiationLevelId");
		
		ExtVirtualLinkData linkData = new ExtVirtualLinkData();
		linkData.setExtVirtualLinkId("extVirtualLinkId");
		linkData.setResourceId("resourceId");
		linkData.setVimId("vimId");
		
		List<VnfExtCpData> extCps = new ArrayList<>();
		VnfExtCpData vnfExtCpData = new VnfExtCpData();
		vnfExtCpData.setCpdId("cpdId");
		vnfExtCpData.setNumDynamicAddresses(10000);
		List<NetworkAddress> addresses = new ArrayList<>();
		NetworkAddress address = new NetworkAddress();
		address.setIp("ip");
		address.setMac("mac");
		address.setSubnetId("subnetId");
		addresses.add(address);
		vnfExtCpData.setAddresses(addresses );
		
		extCps.add(vnfExtCpData);
		linkData.setExtCps(extCps);
		
		List<ExtVirtualLinkData> extVirtualLinks = new ArrayList<>();
		extVirtualLinks.add(linkData);
		cbamRequest.setExtVirtualLinks(extVirtualLinks);
		
		
		List<ExtManagedVirtualLinkData> managedVirtualLinks = new ArrayList<>();
		ExtManagedVirtualLinkData managedVirtualLink = new ExtManagedVirtualLinkData();
		managedVirtualLink.setResourceId("resourceId");
		managedVirtualLink.setExtManagedVirtualLinkId("extManagedVirtualLinkId");
		managedVirtualLink.setVimId("vimId");
		managedVirtualLink.setVirtualLinkDescId("virtualLinkDescId");
		managedVirtualLinks.add(managedVirtualLink );
		
		cbamRequest.setExtManagedVirtualLinks(managedVirtualLinks);
		
		
		List<VimSoftwareImage> softwareImages = new ArrayList<>();
		VimSoftwareImage vimSoftwareImage = new VimSoftwareImage();
		vimSoftwareImage.setResourceId("resourceId");
		vimSoftwareImage.setVimId("vimId");
		vimSoftwareImage.setVnfdSoftwareImageId("vnfdSoftwareImageId");
		softwareImages.add(vimSoftwareImage);
		cbamRequest.setSoftwareImages(softwareImages);
		
		
		List<VimComputeResourceFlavour> computeResourceFlavours = new ArrayList<>();
		VimComputeResourceFlavour computeResourceFlavour = new VimComputeResourceFlavour();
		computeResourceFlavour.setResourceId("resourceId");
		computeResourceFlavour.setVimId("vimId");
		computeResourceFlavour.setVnfdVirtualComputeDescId("vnfdVirtualComputeDescId");
		computeResourceFlavours.add(computeResourceFlavour);
		cbamRequest.setComputeResourceFlavours(computeResourceFlavours);
		
		List<ZoneInfo> zoneInfos = new ArrayList<>();
		ZoneInfo zoneInfo = new ZoneInfo();
		zoneInfo.setResourceId("resourceId");
		zoneInfo.setId("id");
		zoneInfos.add(zoneInfo);
		cbamRequest.setZones(zoneInfos);
		
		cbamRequest.setAdditionalParams("additionalParams");
		
		CBAMInstantiateVnfResponse response = cbamMgmr.instantiateVnf(cbamRequest, vnfInstanceId);
		
		Assert.assertEquals("result is ", "instantiationLevelId", cbamRequest.getInstantiationLevelId()); 
		Assert.assertEquals("result is ", "flavourId", cbamRequest.getFlavourId()); 
		Assert.assertEquals("result is ", false, cbamRequest.isGrantlessMode()); 
		Assert.assertEquals("result is ", "id", cbamRequest.getVims().get(0).getId()); 
		Assert.assertEquals("result is ", "interfaceEndpoint", cbamRequest.getVims().get(0).getInterfaceEndpoint()); 
		
		Assert.assertEquals("result is ", "extVirtualLinkId", cbamRequest.getExtVirtualLinks().get(0).getExtVirtualLinkId()); 
		Assert.assertEquals("result is ", "resourceId", cbamRequest.getExtVirtualLinks().get(0).getResourceId()); 
		Assert.assertEquals("result is ", "vimId", cbamRequest.getExtVirtualLinks().get(0).getVimId()); 
		
		Assert.assertEquals("result is ", "cpdId", cbamRequest.getExtVirtualLinks().get(0).getExtCps().get(0).getCpdId()); 
		Assert.assertEquals("result is ", "ip", cbamRequest.getExtVirtualLinks().get(0).getExtCps().get(0).getAddresses().get(0).getIp()); 
		Assert.assertEquals("result is ", "mac", cbamRequest.getExtVirtualLinks().get(0).getExtCps().get(0).getAddresses().get(0).getMac()); 
		Assert.assertEquals("result is ", "subnetId", cbamRequest.getExtVirtualLinks().get(0).getExtCps().get(0).getAddresses().get(0).getSubnetId());
		
		Assert.assertEquals("result is ", "extManagedVirtualLinkId", cbamRequest.getExtManagedVirtualLinks().get(0).getExtManagedVirtualLinkId()); 
		Assert.assertEquals("result is ", "resourceId", cbamRequest.getExtManagedVirtualLinks().get(0).getResourceId()); 
		Assert.assertEquals("result is ", "vimId", cbamRequest.getExtManagedVirtualLinks().get(0).getVimId()); 
		Assert.assertEquals("result is ", "virtualLinkDescId", cbamRequest.getExtManagedVirtualLinks().get(0).getVirtualLinkDescId());
		
		Assert.assertEquals("result is ", "resourceId", cbamRequest.getSoftwareImages().get(0).getResourceId()); 
		Assert.assertEquals("result is ", "vimId", cbamRequest.getSoftwareImages().get(0).getVimId()); 
		Assert.assertEquals("result is ", "vnfdSoftwareImageId", cbamRequest.getSoftwareImages().get(0).getVnfdSoftwareImageId());
		
		Assert.assertEquals("result is ", "resourceId", cbamRequest.getComputeResourceFlavours().get(0).getResourceId()); 
		Assert.assertEquals("result is ", "vimId", cbamRequest.getComputeResourceFlavours().get(0).getVimId()); 
		Assert.assertEquals("result is ", "vnfdVirtualComputeDescId", cbamRequest.getComputeResourceFlavours().get(0).getVnfdVirtualComputeDescId());
		
		Assert.assertEquals("result is ", "id", cbamRequest.getZones().get(0).getId()); 
		Assert.assertEquals("result is ", "resourceId", cbamRequest.getZones().get(0).getResourceId()); 
		
		Assert.assertEquals("result is ", "additionalParams", cbamRequest.getAdditionalParams()); 
	}
	
	@Test
	public void testInstantiateVnfError() throws ClientProtocolException, IOException
	{
		CBAMInstantiateVnfRequest cbamRequest = new CBAMInstantiateVnfRequest();
		CBAMInstantiateVnfResponse response = cbamMgmr.instantiateVnf(cbamRequest, vnfInstanceId);
	}
	
	@Test
	public void testModifyVnfSuccess() throws ClientProtocolException, IOException
	{
        httpResult.setStatusCode(202);
		
		when(httpClientProcessor.process(Mockito.anyString(), Mockito.any(RequestMethod.class), Mockito.any(HashMap.class), Mockito.anyString())).thenReturn(httpResult);
		
		
		CBAMModifyVnfRequest cbamRequest = new CBAMModifyVnfRequest();
		CBAMModifyVnfResponse response = cbamMgmr.modifyVnf(cbamRequest, vnfInstanceId);
	}
	
	@Test
	public void testModifyVnfError() throws ClientProtocolException, IOException
	{
		CBAMModifyVnfRequest cbamRequest = new CBAMModifyVnfRequest();
		CBAMModifyVnfResponse response = cbamMgmr.modifyVnf(cbamRequest, vnfInstanceId);
	}
	
	@Test
	public void testTerminateVnfSuccess() throws ClientProtocolException, IOException
	{
        httpResult.setStatusCode(202);
		
		when(httpClientProcessor.process(Mockito.anyString(), Mockito.any(RequestMethod.class), Mockito.any(HashMap.class), Mockito.anyString())).thenReturn(httpResult);
		
		CBAMTerminateVnfRequest cbamRequest = new CBAMTerminateVnfRequest();
		CBAMTerminateVnfResponse response = cbamMgmr.terminateVnf(cbamRequest, vnfInstanceId);
	}
	
	@Test
	public void testTerminateVnfError() throws ClientProtocolException, IOException
	{
		CBAMTerminateVnfRequest cbamRequest = new CBAMTerminateVnfRequest();
		CBAMTerminateVnfResponse response = cbamMgmr.terminateVnf(cbamRequest, vnfInstanceId);
	}
	
	@Test
	public void testDeleteVnfSuccess() throws ClientProtocolException, IOException
	{
        httpResult.setStatusCode(204);
		
		when(httpClientProcessor.process(Mockito.anyString(), Mockito.any(RequestMethod.class), Mockito.any(HashMap.class), Mockito.anyString())).thenReturn(httpResult);
		
		cbamMgmr.deleteVnf(vnfInstanceId);
	}
	
	@Test
	public void testDeleteVnfError() throws ClientProtocolException, IOException
	{
		cbamMgmr.deleteVnf(vnfInstanceId);
	}
	
	@Test
	public void testScaleVnfSuccess() throws ClientProtocolException, IOException
	{
        httpResult.setStatusCode(202);
		
		when(httpClientProcessor.process(Mockito.anyString(), Mockito.any(RequestMethod.class), Mockito.any(HashMap.class), Mockito.anyString())).thenReturn(httpResult);
		
		CBAMScaleVnfRequest cbamRequest = new CBAMScaleVnfRequest();
		CBAMScaleVnfResponse response = cbamMgmr.scaleVnf(cbamRequest, vnfInstanceId);
	}
	
	@Test
	public void testScaleVnfError() throws ClientProtocolException, IOException
	{
		CBAMScaleVnfRequest cbamRequest = new CBAMScaleVnfRequest();
		CBAMScaleVnfResponse response = cbamMgmr.scaleVnf(cbamRequest, vnfInstanceId);
	}
	
	@Test
	public void testHealVnfSuccess() throws ClientProtocolException, IOException
	{
        httpResult.setStatusCode(202);
		
		when(httpClientProcessor.process(Mockito.anyString(), Mockito.any(RequestMethod.class), Mockito.any(HashMap.class), Mockito.anyString())).thenReturn(httpResult);
		
		CBAMHealVnfRequest cbamRequest = new CBAMHealVnfRequest();
		CBAMHealVnfResponse response = cbamMgmr.healVnf(cbamRequest, vnfInstanceId);
	}
	
	@Test
	public void testHealVnfError() throws ClientProtocolException, IOException
	{
		CBAMHealVnfRequest cbamRequest = new CBAMHealVnfRequest();
		CBAMHealVnfResponse response = cbamMgmr.healVnf(cbamRequest, vnfInstanceId);
	}
	
	@Test
	public void testQueryVnfSuccess() throws ClientProtocolException, IOException
	{
		CBAMQueryVnfResponse response = cbamMgmr.queryVnf(vnfInstanceId);
	}
	
	@Test
	public void testQueryVnf() throws ClientProtocolException, IOException
	{
        httpResult.setStatusCode(400);
		
		when(httpClientProcessor.process(Mockito.anyString(), Mockito.any(RequestMethod.class), Mockito.any(HashMap.class), Mockito.anyString())).thenReturn(httpResult);
		
		CBAMQueryVnfResponse response = cbamMgmr.queryVnf(vnfInstanceId);
	}
	
	@Test
	public void testCreateSubscriptionSuccess() throws ClientProtocolException, IOException
	{
		httpResult.setStatusCode(201);
	
	    when(httpClientProcessor.process(Mockito.anyString(), Mockito.any(RequestMethod.class), Mockito.any(HashMap.class), Mockito.anyString())).thenReturn(httpResult);
	
		CBAMCreateSubscriptionRequest cbamRequest = new CBAMCreateSubscriptionRequest();
		CBAMCreateSubscriptionResponse response = cbamMgmr.createSubscription(cbamRequest);
	}
	
	@Test
	public void testCreateSubscriptionError() throws ClientProtocolException, IOException
	{
		CBAMCreateSubscriptionRequest cbamRequest = new CBAMCreateSubscriptionRequest();
		CBAMCreateSubscriptionResponse response = cbamMgmr.createSubscription(cbamRequest);
	}
	
	@Test
	public void testGetNotification() throws ClientProtocolException, IOException
	{
		CBAMVnfNotificationRequest cbamRequest = new CBAMVnfNotificationRequest();
		CBAMVnfNotificationResponse response = cbamMgmr.getNotification(cbamRequest);
	}
	
	@Test
	public void testGetSubscriptionSuccess() throws ClientProtocolException, IOException
	{
		String subscriptionId = "subscriptionId_001";
		Subscription response = cbamMgmr.getSubscription(subscriptionId);
	}
	
	@Test
	public void testGetSubscriptionError() throws ClientProtocolException, IOException
	{
		httpResult.setStatusCode(400);
		
	    when(httpClientProcessor.process(Mockito.anyString(), Mockito.any(RequestMethod.class), Mockito.any(HashMap.class), Mockito.anyString())).thenReturn(httpResult);
	
		String subscriptionId = "subscriptionId_001";
		Subscription response = cbamMgmr.getSubscription(subscriptionId);
	}
	
	@Test
	public void testQueryVnfcResourceSuccess() throws ClientProtocolException, IOException
	{
		String json = "[{'id':'id_001'}]";
		HttpResult httpResult = new HttpResult();
		httpResult.setStatusCode(200);
		httpResult.setContent(json);
		
		when(httpClientProcessor.process(Mockito.anyString(), Mockito.any(RequestMethod.class), Mockito.any(HashMap.class), Mockito.anyString())).thenReturn(httpResult);
		List<VnfcResourceInfo> response = cbamMgmr.queryVnfcResource(vnfInstanceId);
	}
	
	@Test
	public void testQueryVnfcResourceErrot() throws ClientProtocolException, IOException
	{
		String json = "[{'id':'id_001'}]";
		HttpResult httpResult = new HttpResult();
		httpResult.setStatusCode(400);
		httpResult.setContent(json);
		
		when(httpClientProcessor.process(Mockito.anyString(), Mockito.any(RequestMethod.class), Mockito.any(HashMap.class), Mockito.anyString())).thenReturn(httpResult);
		List<VnfcResourceInfo> response = cbamMgmr.queryVnfcResource(vnfInstanceId);
	}
	
	@Test
	public void testQueryOperExecutionSuccess() throws ClientProtocolException, IOException
	{
		String execId = "execId_001";
		CBAMQueryOperExecutionResponse response = cbamMgmr.queryOperExecution(execId);
		Assert.assertEquals("id", response.getId());
		Assert.assertEquals(null, response.getGrantId());
		Assert.assertEquals(null, response.get_links());
		Assert.assertEquals(null, response.getAdditionalData());
		Assert.assertEquals(null, response.getCancelMode());
		Assert.assertEquals(false, response.isCancelPending());
		Assert.assertEquals(null, response.getMetadata());
		Assert.assertEquals(null, response.getOperationName());
		Assert.assertEquals(null, response.getOperationParams());
		Assert.assertEquals(null, response.getOperationType());
		Assert.assertEquals(null, response.getStartTime());
		Assert.assertEquals(null, response.getStatus());
		Assert.assertEquals(null, response.getVnfInstanceId());
	}
	
	@Test
	public void testQueryOperExecutionOngoing() throws ClientProtocolException, IOException
	{
        httpResult.setStatusCode(202);
		
		when(httpClientProcessor.process(Mockito.anyString(), Mockito.any(RequestMethod.class), Mockito.any(HashMap.class), Mockito.anyString())).thenReturn(httpResult);
		
		
		String execId = "execId_001";
		CBAMQueryOperExecutionResponse response = cbamMgmr.queryOperExecution(execId);
	}
	
	@Test
	public void testQueryOperExecutionError() throws ClientProtocolException, IOException
	{
		httpResult.setStatusCode(400);
		
		when(httpClientProcessor.process(Mockito.anyString(), Mockito.any(RequestMethod.class), Mockito.any(HashMap.class), Mockito.anyString())).thenReturn(httpResult);
		
		
		String execId = "execId_001";
		CBAMQueryOperExecutionResponse response = cbamMgmr.queryOperExecution(execId);
	}
	
	@Test
	public void testUploadVnfPackageSuccess() throws ClientProtocolException, IOException
	{
		String cbamPackageFilePath = "cbamPackageFilePath_001";
		cbamMgmr.uploadVnfPackage(cbamPackageFilePath);
	}
	
	@Test
	public void testUploadVnfPackageError() throws ClientProtocolException, IOException
	{
        httpResult.setStatusCode(400);
		
		when(httpClientProcessor.process(Mockito.anyString(), Mockito.any(RequestMethod.class), Mockito.any(HashMap.class), Mockito.anyString())).thenReturn(httpResult);
		
		String cbamPackageFilePath = "cbamPackageFilePath_001";
		cbamMgmr.uploadVnfPackage(cbamPackageFilePath);
	}
}
