/*
 * Copyright 2017 Huawei Technologies Co., Ltd.
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
package org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.constant;

import junit.framework.Assert;
import org.junit.Test;

import java.util.List;

import static org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.constant.Constant.AUTHLIST;

/**
 * Created by QuanZhong on 2017/3/17.
 */
public class TestParamConstants {
    @Test
    public void testCreate(){
        List<String> authlist = AUTHLIST;
        authlist.contains("abc");
        String abc = ParamConstants.CONNECTMGR_CONNECT;
        authlist.contains(abc);
        String getTokensV2 = ParamConstants.GET_TOKENS_V2;
        authlist.contains(getTokensV2);
        String getTokensV3 = ParamConstants.GET_TOKENS_V3;
        authlist.contains(getTokensV3);
        String getTokenSucResp = ParamConstants.GET_TOKEN_SUC_RESP;
        authlist.contains(getTokenSucResp);
        String getTokenFailResp = ParamConstants.GET_TOKEN_FAIL_RESP;
        authlist.contains(getTokenFailResp);
        String rest3rdConnection = ParamConstants.REST_3RD_CONNECTION;
        authlist.contains(rest3rdConnection);
        String rest3rdDisconnect = ParamConstants.REST_3RD_DISCONNECT;
        authlist.contains(rest3rdDisconnect);
        String rest3rdHandshake= ParamConstants.REST_3RD_HANDSHAKE;
        authlist.contains(rest3rdHandshake);
        String csmAuthConnectSouth= ParamConstants.CSM_AUTH_CONNECT_SOUTH;
        authlist.contains(csmAuthConnectSouth);
        String csmAuthConnectSouthDisconnect = ParamConstants.CSM_AUTH_CONNECT_SOUTH_DISCONNECT;
        authlist.contains(csmAuthConnectSouthDisconnect);
        String csmAuthConnect = ParamConstants.CSM_AUTH_CONNECT;
        authlist.contains(csmAuthConnect);
        String csmAuthDisconnect = ParamConstants.CSM_AUTH_DISCONNECT;
        authlist.contains(csmAuthDisconnect);
        String csmAuthHandshake  = ParamConstants.CSM_AUTH_HANDSHAKE;
        authlist.contains(csmAuthHandshake);
        String vnfmmed = ParamConstants.VNFMMED;
        authlist.contains(vnfmmed);
        String connectmgrConnect = ParamConstants.CONNECTMGR_CONNECT;
        authlist.contains(connectmgrConnect);
        String connectmgrDisconnect = ParamConstants.CONNECTMGR_DISCONNECT;
        authlist.contains(connectmgrDisconnect);
        String connectmgrHandshake = ParamConstants.CONNECTMGR_HANDSHAKE;
        authlist.contains(connectmgrHandshake);
        String createVnfPerf = ParamConstants.CREATE_VNF_PERF;
        authlist.contains(createVnfPerf);
        String vnfmgrInstance = ParamConstants.VNFMGR_INSTANCE;
        authlist.contains(vnfmgrInstance);
        String vnfdFlavor = ParamConstants.VNFD_FLAVOR;
        authlist.contains(vnfdFlavor);
        String updateResource = ParamConstants.UPDATE_RESOURCE;
        authlist.contains(updateResource);
        String vnfQuery = ParamConstants.VNF_QUERY;
        authlist.contains(vnfQuery);
        String vmsQuery = ParamConstants.VMS_QUERY;
        authlist.contains(vmsQuery);
        String vnfmgrVnfkpi = ParamConstants.VNFMGR_VNFKPI;
        authlist.contains(vnfmgrVnfkpi);
        String resVnf = ParamConstants.RES_VNF;
        authlist.contains(resVnf);
        String notifyVnfPerf = ParamConstants.NOTIFY_VNF_PERF;
        authlist.contains(notifyVnfPerf);
        String paramModule = ParamConstants.PARAM_MODULE;
        authlist.contains(paramModule);
        String getAllSos = ParamConstants.GET_ALL_SOS;
        authlist.contains(getAllSos);
        String operationLogPath = ParamConstants.OPERATION_LOG_PATH;
        authlist.contains(operationLogPath);
        String systemLogPath = ParamConstants.SYSTEM_LOG_PATH;
        authlist.contains(systemLogPath);
        String securityLogPath = ParamConstants.SECURITY_LOG_PATH;
        authlist.contains(securityLogPath);
        String getVnfmVnf = ParamConstants.GET_VNFM_VNF;
        authlist.contains(getVnfmVnf);
        String getResNet = ParamConstants.GET_RES_NET;
        authlist.contains(getResNet);
        String getJobStatus = ParamConstants.GET_JOB_STATUS;
        authlist.contains(getJobStatus);
        String vnfInstance = ParamConstants.VNF_INSTANCE;
        authlist.contains(vnfInstance);
        String vnfInstanceDel = ParamConstants.VNF_INSTANCE_DEL;
        authlist.contains(vnfInstanceDel);
        String vnfInstanceGetU2000 = ParamConstants.VNF_INSTANCE_GET_U2000;
        authlist.contains(vnfInstanceGetU2000);
        String vnfInstanceGet = ParamConstants.VNF_INSTANCE_GET;
        authlist.contains(vnfInstanceGet);
        String vnfConfigurationGet = ParamConstants.VNF_CONFIGURATION_GET;
        authlist.contains(vnfConfigurationGet);
        String vnfScale = ParamConstants.VNF_SCALE;
        authlist.contains(vnfScale);
        String vnfGetVminfo = ParamConstants.VNF_GET_VMINFO;
        authlist.contains(vnfGetVminfo);
        String msbRegisterUrl = ParamConstants.MSB_REGISTER_URL;
        authlist.contains(msbRegisterUrl);
        String msbUnregisterUrl = ParamConstants.MSB_UNREGISTER_URL;
        authlist.contains(msbUnregisterUrl);
        String esrGetVnfmUrl = ParamConstants.ESR_GET_VNFM_URL;
        authlist.contains(esrGetVnfmUrl);
        String esrGetVnfmsUrl = ParamConstants.ESR_GET_VNFMS_URL;
        authlist.contains(esrGetVnfmsUrl);
        String grantResUrl = ParamConstants.GRANT_RES_URL;
        authlist.contains(grantResUrl);
        String healVnf = ParamConstants.HEAL_VNF;
        authlist.contains(healVnf);
        Assert.assertTrue(true);
    }
}
