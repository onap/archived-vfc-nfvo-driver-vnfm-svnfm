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

package com.nokia.vfcadaptor.adaptor;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;

import com.nokia.vfcadaptor.catalog.inf.CatalogMgmrInf;
import com.nokia.vfcadaptor.cbam.bo.CBAMInstantiateVnfResponse;
import com.nokia.vfcadaptor.cbam.bo.CBAMTerminateVnfRequest;
import com.nokia.vfcadaptor.cbam.bo.CBAMTerminateVnfResponse;
import com.nokia.vfcadaptor.cbam.inf.CbamMgmrInf;
import com.nokia.vfcadaptor.nslcm.bo.NslcmGrantVnfRequest;
import com.nokia.vfcadaptor.nslcm.bo.NslcmGrantVnfResponse;
import com.nokia.vfcadaptor.nslcm.inf.NslcmMgmrInf;
import com.nokia.vfcadaptor.vnfmdriver.bo.InstantiateVnfRequest;
import com.nokia.vfcadaptor.vnfmdriver.bo.TerminateVnfRequest;
import com.nokia.vfcadaptor.vnfmdriver.bo.TerminateVnfResponse;

public class TerminateVnfContinueRunnable implements Runnable {
	private Logger logger = Logger.getLogger(TerminateVnfContinueRunnable.class);

	private CbamMgmrInf cbamMgmr;
	private NslcmMgmrInf nslcmMgmr;
	
	private TerminateVnfRequest driverRequest;
	private String vnfInstanceId;
	
	private Driver2CbamRequestConverter requestConverter;
	
	public TerminateVnfContinueRunnable(TerminateVnfRequest driverRequest, String vnfInstanceId,
			NslcmMgmrInf nslcmMgmr, CbamMgmrInf cbamMgmr, Driver2CbamRequestConverter requestConverter)
	{
		this.driverRequest = driverRequest;
		this.vnfInstanceId = vnfInstanceId;
		this.nslcmMgmr = nslcmMgmr; 
		this.cbamMgmr = cbamMgmr;
		this.requestConverter = requestConverter;
	}
	
	public void run() {
		try {
			NslcmGrantVnfRequest grantRequest = new NslcmGrantVnfRequest();
			NslcmGrantVnfResponse grantResponse = nslcmMgmr.grantVnf(grantRequest);
			handleNslcmGrantResponse(grantResponse);
			
			CBAMTerminateVnfRequest cbamRequest = requestConverter.terminaterqConvert(driverRequest);
			CBAMTerminateVnfResponse cbamResponse = cbamMgmr.terminateVnf(cbamRequest, vnfInstanceId);
			
			cbamMgmr.deleteVnf(vnfInstanceId);
			
		} catch (ClientProtocolException e) {
			logger.error("TerminateVnfContinueRunnable run error ClientProtocolException", e);
		} catch (IOException e) {
			logger.error("TerminateVnfContinueRunnable run error IOException", e);
		}
		
	}

	private void handleNslcmGrantResponse(NslcmGrantVnfResponse grantResponse) {
		// TODO Auto-generated method stub
		
	}

}
