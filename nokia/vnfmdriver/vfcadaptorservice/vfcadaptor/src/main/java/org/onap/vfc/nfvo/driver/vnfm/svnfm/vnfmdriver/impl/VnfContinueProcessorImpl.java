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

import org.onap.vfc.nfvo.driver.vnfm.svnfm.adaptor.*;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.catalog.inf.CatalogMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.inf.CbamMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.mapper.VnfcResourceInfoMapper;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.mapper.VnfmJobExecutionMapper;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.inf.NslcmMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.HealVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.InstantiateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.ScaleVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.TerminateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.inf.VnfContinueProcessorInf;
import org.springframework.stereotype.Component;

@Component
public class VnfContinueProcessorImpl implements VnfContinueProcessorInf{

	@Override
    public void continueInstantiateVnf(String vnfmId, InstantiateVnfRequest driverRequest, String vnfInstanceId,
            String jobId, NslcmMgmrInf nslcmMgmr, CatalogMgmrInf catalogMgmr, CbamMgmrInf cbamMgmr,
            Driver2CbamRequestConverter requestConverter, VnfmJobExecutionMapper jobDbManager,
            VnfcResourceInfoMapper vnfcDbMgmr) {
        InstantiateVnfContinueRunnable task = new InstantiateVnfContinueRunnable.InstantiateVnfContinueRunnableBuilder()
                .setVnfmId(vnfmId).setDriverRequest(driverRequest).setVnfInstanceId(vnfInstanceId).setJobId(jobId)
                .setNslcmMgmr(nslcmMgmr).setCatalogMgmr(catalogMgmr).setCbamMgmr(cbamMgmr)
                .setRequestConverter(requestConverter).setDbManager(jobDbManager).setVnfcDbMgmr(vnfcDbMgmr).build();

        Executors.newSingleThreadExecutor().submit(task);
    }

	@Override
    public void continueTerminateVnf(String vnfmId, TerminateVnfRequest driverRequest, String vnfInstanceId,
            String jobId, NslcmMgmrInf nslcmMgmr, CbamMgmrInf cbamMgmr, Driver2CbamRequestConverter requestConverter,
            VnfmJobExecutionMapper jobDbManager, VnfcResourceInfoMapper vnfcDbMgmr) {
	    
        TerminateVnfContinueRunnable task = new TerminateVnfContinueRunnable.TerminateVnfContinueRunnableBuilder()
                .setVnfmId(vnfmId).setDriverRequest(driverRequest).setVnfInstanceId(vnfInstanceId).setJobId(jobId)
                .setNslcmMgmr(nslcmMgmr).setCbamMgmr(cbamMgmr).setRequestConverter(requestConverter)
                .setDbManager(jobDbManager).setVnfcDbMgmr(vnfcDbMgmr).build();

        Executors.newSingleThreadExecutor().submit(task);
    }

	@Override
	public void continueScaleVnf(String vnfmId, ScaleVnfRequest driverRequest, String vnfInstanceId, String jobId,
			NslcmMgmrInf nslcmMgmr, CbamMgmrInf cbamMgmr, Driver2CbamRequestConverter requestConverter,
			VnfmJobExecutionMapper jobDbManager) {
		ScaleVnfContinueRunnable task = new ScaleVnfContinueRunnable(vnfmId, driverRequest, vnfInstanceId, jobId,
				nslcmMgmr, cbamMgmr, requestConverter, jobDbManager);
		Executors.newSingleThreadExecutor().submit(task);
		
	}

	@Override
	public void continueHealVnf(String vnfmId, HealVnfRequest driverRequest, String vnfInstanceId, String jobId,
			NslcmMgmrInf nslcmMgmr, CbamMgmrInf cbamMgmr, Driver2CbamRequestConverter requestConverter,
            VnfmJobExecutionMapper jobDbManager) {
        HealVnfContinueRunnable task = new HealVnfContinueRunnable.HealVnfContinueRunnableBuilder().setInVnfmId(vnfmId)
                .setDriverRequest(driverRequest).setVnfInstanceId(vnfInstanceId).setJobId(jobId).setNslcmMgmr(nslcmMgmr)
                .setCbamMgmr(cbamMgmr).setRequestConverter(requestConverter).setDbManager(jobDbManager).build();
        Executors.newSingleThreadExecutor().submit(task);

    }

}
