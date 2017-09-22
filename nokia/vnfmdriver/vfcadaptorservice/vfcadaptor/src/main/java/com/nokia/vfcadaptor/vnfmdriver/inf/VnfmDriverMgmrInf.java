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

package com.nokia.vfcadaptor.vnfmdriver.inf;

import com.nokia.vfcadaptor.exception.VnfmDriverException;
import com.nokia.vfcadaptor.vnfmdriver.bo.HealVnfRequest;
import com.nokia.vfcadaptor.vnfmdriver.bo.HealVnfResponse;
import com.nokia.vfcadaptor.vnfmdriver.bo.InstantiateVnfRequest;
import com.nokia.vfcadaptor.vnfmdriver.bo.InstantiateVnfResponse;
import com.nokia.vfcadaptor.vnfmdriver.bo.OperStatusVnfResponse;
import com.nokia.vfcadaptor.vnfmdriver.bo.QueryVnfResponse;
import com.nokia.vfcadaptor.vnfmdriver.bo.ScaleVnfRequest;
import com.nokia.vfcadaptor.vnfmdriver.bo.ScaleVnfResponse;
import com.nokia.vfcadaptor.vnfmdriver.bo.TerminateVnfRequest;
import com.nokia.vfcadaptor.vnfmdriver.bo.TerminateVnfResponse;

public interface VnfmDriverMgmrInf {

	public InstantiateVnfResponse instantiateVnf(InstantiateVnfRequest request, String vnfmId) throws VnfmDriverException;

	public TerminateVnfResponse terminateVnf(TerminateVnfRequest request, String vnfmId, String vnfInstanceId) throws VnfmDriverException;
	
	public QueryVnfResponse queryVnf(String vnfmId, String vnfInstanceId) throws VnfmDriverException;

	public OperStatusVnfResponse getOperStatus(String vnfmId, String jobId) throws VnfmDriverException;

	public ScaleVnfResponse scaleVnf(ScaleVnfRequest request, String vnfmId, String vnfInstanceId) throws VnfmDriverException;

	public HealVnfResponse healVnf(HealVnfRequest request, String vnfmId, String vnfInstanceId) throws VnfmDriverException;
}
