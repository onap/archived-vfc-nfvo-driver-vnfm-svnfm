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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.inf;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
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
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.SubscriptionResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.VnfcResourceInfo;

public interface CbamMgmrInf {

	public CBAMCreateVnfResponse createVnf(CBAMCreateVnfRequest cbamRequest) throws ClientProtocolException, IOException;
	
	public CBAMModifyVnfResponse modifyVnf(CBAMModifyVnfRequest cbamRequest, String vnfInstanceId) throws ClientProtocolException, IOException;
	
	public CBAMInstantiateVnfResponse instantiateVnf(CBAMInstantiateVnfRequest cbamRequest, String vnfInstanceId)
			throws ClientProtocolException, IOException;
	public CBAMCreateSubscriptionResponse createSubscription(CBAMCreateSubscriptionRequest subscriptionRequest)
			throws ClientProtocolException, IOException;
	public SubscriptionResponse getSubscription(String subscriptionId)
			throws ClientProtocolException, IOException;
	public CBAMVnfNotificationResponse getNotification(CBAMVnfNotificationRequest getNotificationRequest)
			throws ClientProtocolException, IOException;

	public CBAMTerminateVnfResponse terminateVnf(CBAMTerminateVnfRequest cbamRequest, String vnfInstanceId)
			throws ClientProtocolException, IOException;

	public CBAMScaleVnfResponse scaleVnf(CBAMScaleVnfRequest cbamRequest, String vnfInstanceId)
			throws ClientProtocolException, IOException;

	public CBAMHealVnfResponse healVnf(CBAMHealVnfRequest cbamRequest, String vnfInstanceId)
            throws ClientProtocolException, IOException;

	public CBAMQueryVnfResponse queryVnf(String vnfInstanceId) throws ClientProtocolException, IOException;
	
	public void deleteVnf(String vnfInstanceId) throws ClientProtocolException, IOException;

	public CBAMQueryOperExecutionResponse queryOperExecution(String execId) throws ClientProtocolException, IOException;

	public void uploadVnfPackage(String cbamPackageName) throws ClientProtocolException, IOException;
	
	public List<VnfcResourceInfo> queryVnfcResource(String vnfInstanceId) throws ClientProtocolException, IOException;

}