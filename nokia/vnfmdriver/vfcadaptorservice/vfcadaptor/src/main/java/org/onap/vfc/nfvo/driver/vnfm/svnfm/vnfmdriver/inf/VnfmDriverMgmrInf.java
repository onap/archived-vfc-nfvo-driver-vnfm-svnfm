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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.inf;

import org.onap.vfc.nfvo.driver.vnfm.svnfm.exception.VnfmDriverException;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.CreateSubscriptionRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.CreateSubscriptionResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.HealVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.HealVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.InstantiateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.InstantiateVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.OperStatusVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.QueryVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.ScaleVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.ScaleVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.TerminateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.TerminateVnfResponse;

public interface VnfmDriverMgmrInf {

	public InstantiateVnfResponse instantiateVnf(InstantiateVnfRequest request, String vnfmId) throws VnfmDriverException;

	public TerminateVnfResponse terminateVnf(TerminateVnfRequest request, String vnfmId, String vnfInstanceId) throws VnfmDriverException;
	
	public QueryVnfResponse queryVnf(String vnfmId, String vnfInstanceId) throws VnfmDriverException;

	public OperStatusVnfResponse getOperStatus(String vnfmId, String jobId) throws VnfmDriverException;

	public ScaleVnfResponse scaleVnf(ScaleVnfRequest request, String vnfmId, String vnfInstanceId) throws VnfmDriverException;

	public HealVnfResponse healVnf(HealVnfRequest request, String vnfmId, String vnfInstanceId) throws VnfmDriverException;

	public CreateSubscriptionResponse createSubscription(CreateSubscriptionRequest request) throws VnfmDriverException;
}
