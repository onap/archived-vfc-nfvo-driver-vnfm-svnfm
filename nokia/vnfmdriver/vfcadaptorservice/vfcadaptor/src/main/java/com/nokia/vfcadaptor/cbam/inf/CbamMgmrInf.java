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

package com.nokia.vfcadaptor.cbam.inf;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import com.nokia.vfcadaptor.cbam.bo.CBAMCreateVnfRequest;
import com.nokia.vfcadaptor.cbam.bo.CBAMCreateVnfResponse;
import com.nokia.vfcadaptor.cbam.bo.CBAMHealVnfRequest;
import com.nokia.vfcadaptor.cbam.bo.CBAMHealVnfResponse;
import com.nokia.vfcadaptor.cbam.bo.CBAMInstantiateVnfRequest;
import com.nokia.vfcadaptor.cbam.bo.CBAMInstantiateVnfResponse;
import com.nokia.vfcadaptor.cbam.bo.CBAMQueryVnfResponse;
import com.nokia.vfcadaptor.cbam.bo.CBAMScaleVnfRequest;
import com.nokia.vfcadaptor.cbam.bo.CBAMScaleVnfResponse;
import com.nokia.vfcadaptor.cbam.bo.CBAMTerminateVnfRequest;
import com.nokia.vfcadaptor.cbam.bo.CBAMTerminateVnfResponse;

public interface CbamMgmrInf {

	public CBAMCreateVnfResponse createVnf(CBAMCreateVnfRequest cbamRequest) throws ClientProtocolException, IOException;

	public CBAMInstantiateVnfResponse instantiateVnf(CBAMInstantiateVnfRequest cbamRequest, String vnfInstanceId)
			throws ClientProtocolException, IOException;

	public CBAMTerminateVnfResponse terminateVnf(CBAMTerminateVnfRequest cbamRequest, String vnfInstanceId)
			throws ClientProtocolException, IOException;

	public CBAMScaleVnfResponse scaleVnf(CBAMScaleVnfRequest cbamRequest, String vnfInstanceId)
			throws ClientProtocolException, IOException;

	public CBAMHealVnfResponse healVnf(CBAMHealVnfRequest cbamRequest, String vnfInstanceId)
			throws ClientProtocolException, IOException;

	public CBAMQueryVnfResponse queryVnf(String vnfInstanceId) throws ClientProtocolException, IOException;
	
	public void deleteVnf(String vnfInstanceId) throws ClientProtocolException, IOException;
}