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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.impl;

import java.util.concurrent.Executors;

import org.onap.vfc.nfvo.driver.vnfm.svnfm.adaptor.Cbam2DriverResponseConverter;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.adaptor.Driver2CbamRequestConverter;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.adaptor.InstantiateVnfContinueRunnable;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.adaptor.TerminateVnfContinueRunnable;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.catalog.inf.CatalogMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.inf.CbamMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.repository.VnfmJobExecutionRepository;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.inf.NslcmMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.InstantiateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.TerminateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.inf.VnfContinueProcessorInf;

public class VnfContinueProcessorImpl implements VnfContinueProcessorInf{

	@Override
	public void continueInstantiateVnf(InstantiateVnfRequest driverRequest, String vnfInstanceId, String jobId, NslcmMgmrInf nslcmMgmr, CatalogMgmrInf catalogMgmr, CbamMgmrInf cbamMgmr, Driver2CbamRequestConverter requestConverter, VnfmJobExecutionRepository jobDbManager) {
		InstantiateVnfContinueRunnable task = new InstantiateVnfContinueRunnable(driverRequest, vnfInstanceId, jobId,
				nslcmMgmr, catalogMgmr, cbamMgmr, requestConverter, jobDbManager);
		
		Executors.newSingleThreadExecutor().submit(task);
	}

	@Override
	public void continueTerminateVnf(TerminateVnfRequest driverRequest, String vnfInstanceId, String jobId, NslcmMgmrInf nslcmMgmr, CbamMgmrInf cbamMgmr, Driver2CbamRequestConverter requestConverter, VnfmJobExecutionRepository jobDbManager) {
		TerminateVnfContinueRunnable task = new TerminateVnfContinueRunnable(driverRequest, vnfInstanceId, jobId,
				nslcmMgmr, cbamMgmr, requestConverter, jobDbManager);
		
		Executors.newSingleThreadExecutor().submit(task);
	}

}
