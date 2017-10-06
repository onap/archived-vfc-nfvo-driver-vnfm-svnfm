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
