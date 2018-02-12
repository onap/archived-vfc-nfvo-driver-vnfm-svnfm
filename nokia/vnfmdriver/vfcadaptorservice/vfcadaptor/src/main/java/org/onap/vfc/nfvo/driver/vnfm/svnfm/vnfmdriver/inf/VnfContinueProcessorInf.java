/*
* Copyright 2016-2017 Nokia Corporation
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

import org.onap.vfc.nfvo.driver.vnfm.svnfm.adaptor.Driver2CbamRequestConverter;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.catalog.inf.CatalogMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.inf.CbamMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.mapper.VnfcResourceInfoMapper;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.mapper.VnfmJobExecutionMapper;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.inf.NslcmMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.HealVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.InstantiateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.ScaleVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.TerminateVnfRequest;

public interface VnfContinueProcessorInf {
	public void continueInstantiateVnf(String vnfmId, InstantiateVnfRequest driverRequest, String vnfInstanceId, String jobId, NslcmMgmrInf nslcmMgmr, CatalogMgmrInf catalogMgmr, CbamMgmrInf cbamMgmr, Driver2CbamRequestConverter requestConverter, VnfmJobExecutionMapper jobDbManager, VnfcResourceInfoMapper vnfcDbMgmr);
	public void continueTerminateVnf(String vnfmId, TerminateVnfRequest driverRequest, String vnfInstanceId, String jobId, NslcmMgmrInf nslcmMgmr, CbamMgmrInf cbamMgmr, Driver2CbamRequestConverter requestConverter, VnfmJobExecutionMapper jobDbManager, VnfcResourceInfoMapper vnfcDbMgmr);
	public void continueScaleVnf(String vnfmId, ScaleVnfRequest driverRequest, String vnfInstanceId, String jobId, NslcmMgmrInf nslcmMgmr, CbamMgmrInf cbamMgmr, Driver2CbamRequestConverter requestConverter, VnfmJobExecutionMapper jobDbManager);
	public void continueHealVnf(String vnfmId, HealVnfRequest driverRequest, String vnfInstanceId, String jobId, NslcmMgmrInf nslcmMgmr, CbamMgmrInf cbamMgmr, Driver2CbamRequestConverter requestConverter, VnfmJobExecutionMapper jobDbManager);
}
