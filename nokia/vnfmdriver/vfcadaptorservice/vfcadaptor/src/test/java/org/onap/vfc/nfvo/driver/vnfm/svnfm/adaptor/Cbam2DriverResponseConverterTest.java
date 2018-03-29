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


package org.onap.vfc.nfvo.driver.vnfm.svnfm.adaptor;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMHealVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMScaleVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMTerminateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.OperationExecution;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.OperationExecution.OperationType;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.common.bo.AdaptorEnv;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonEnum;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.ScaleType;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.bean.VnfmJobExecutionInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.mapper.VnfmJobExecutionMapper;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.HealVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.ScaleVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.TerminateVnfResponse;

public class Cbam2DriverResponseConverterTest {
	@InjectMocks
	Cbam2DriverResponseConverter convertor;
	
	@Mock
	private VnfmJobExecutionMapper jobDbManager;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		AdaptorEnv adaptorEnv = new AdaptorEnv();
		adaptorEnv.setInitialProgress(10);
		adaptorEnv.setInstantiateTimeInterval(60);
		adaptorEnv.setTerminateTimeInterval(60);
		convertor.setAdaptorEnv(adaptorEnv);
		
		VnfmJobExecutionInfo execInfo = new VnfmJobExecutionInfo();
		execInfo.setJobId(1L);
		execInfo.setOperateStartTime(123456);
		when(jobDbManager.findNewestJobInfo()).thenReturn(execInfo);
		when(jobDbManager.findOne(Mockito.anyLong())).thenReturn(execInfo);
		doNothing().when(jobDbManager).insert(Mockito.any(VnfmJobExecutionInfo.class));
	}
	
	@Test
	public void testTerminateRspConvert()
	{
		CBAMTerminateVnfResponse cbamResponse = new CBAMTerminateVnfResponse();
		TerminateVnfResponse response = convertor.terminateRspConvert(cbamResponse);
	}
	
	@Test
	public void testCalculateProgressInstantiate()
	{
		OperationExecution operationExecution = new OperationExecution();
		operationExecution.setOperationType(OperationType.INSTANTIATE);
		convertor.calculateProgress(operationExecution, "1");
	}
	
	@Test
	public void testCalculateProgressTerminate()
	{
		OperationExecution operationExecution = new OperationExecution();
		operationExecution.setOperationType(OperationType.TERMINATE);
		convertor.calculateProgress(operationExecution, "1");
	}
	
	@Test
	public void testCalculateProgressScale()
	{
		OperationExecution operationExecution = new OperationExecution();
		operationExecution.setOperationType(OperationType.SCALE);
		convertor.calculateProgress(operationExecution, "1");
	}
	
	@Test
	public void testScaleRspConvertOut()
	{
		CBAMScaleVnfResponse cbamResponse = new CBAMScaleVnfResponse();
		ScaleVnfResponse response = convertor.scaleRspConvert(cbamResponse, ScaleType.SCALE_OUT);
	}
	
	@Test
	public void testScaleRspConvertIn()
	{
		CBAMScaleVnfResponse cbamResponse = new CBAMScaleVnfResponse();
		ScaleVnfResponse response = convertor.scaleRspConvert(cbamResponse, ScaleType.SCALE_IN);
	}
	
	@Test
	public void testHeallRspConvert()
	{
		CBAMHealVnfResponse cbamResponse = new CBAMHealVnfResponse();
		HealVnfResponse response = convertor.healRspConvert(cbamResponse);
	}
	
	@Test
	public void testOperRspConvertStarted()
	{
		OperationExecution operationExecution = new OperationExecution();
		operationExecution.setStatus(CommonEnum.OperationStatus.STARTED);
		convertor.operRspConvert(operationExecution, "1");
	}
	
	@Test
	public void testOperRspConvertFinished()
	{
		OperationExecution operationExecution = new OperationExecution();
		operationExecution.setStatus(CommonEnum.OperationStatus.FINISHED);
		convertor.operRspConvert(operationExecution, "1");
	}
	
	@Test
	public void testOperRspConvertOngoing()
	{
		OperationExecution operationExecution = new OperationExecution();
		operationExecution.setStatus(CommonEnum.OperationStatus.OTHER);
		convertor.operRspConvert(operationExecution, "1");
	}
	
	@Test
	public void testOperRspConvertFailed()
	{
		OperationExecution operationExecution = new OperationExecution();
		operationExecution.setStatus(CommonEnum.OperationStatus.FAILED);
		convertor.operRspConvert(operationExecution, "1");
	}
}
