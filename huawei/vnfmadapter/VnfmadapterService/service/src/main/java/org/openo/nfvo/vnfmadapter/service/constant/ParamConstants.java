/*
 * Copyright 2016-2017 Huawei Technologies Co., Ltd.
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

package org.openo.nfvo.vnfmadapter.service.constant;

/**
 *
 * @author
 *
 * @version NFVO 0.5 Sep 6, 2016
 */
public class ParamConstants {

    public static final String GET_TOKENS_V1 = "{\"grantType\": \"password\", \"userName\": \"%s\",\"value\": \"%s\"}";

    public static final String GET_TOKENS_V2 =
            "{\"auth\":{\"identity\": {\"methods\": [\"password\"],\"password\": {\"user\": {\"name\": \"%s\",\"password\": \"%s\"}}}}}";

    public static final String GET_IAM_TOKENS =
            "{\"auth\": {\"identity\": {\"methods\": [\"password\"],\"password\": {\"user\": {\"name\": "
                    + "\"%s\",\"password\": \"%s\",\"domain\": {\"name\": \"%s\"}}}},\"scope\": {\"domain\": {\"name\": \"%s\"}}}}";

    public static final String GET_TOKEN_SUC_RESP =
            "{\"token\": {\"methods\": [\"password\"],\"expires_at\": \"\",\"user\": {\"id\": \"%s\","
                    + "\"name\": \"%s\"},\"roa_rand\": \"%s\"}}";

    public static final String GET_TOKEN_FAIL_RESP = "{\"Information\": \"%s\"}";

    public static final String REST_3RD_CONNECTION = "/rest/plat/smapp/v1/oauth/token";

    public static final String REST_3RD_DISCONNECT = "/rest/plat/smapp/v1/sessions?roarand=%s";

    public static final String REST_3RD_HANDSHAKE = "/rest/plat/ssm/v1/sessions/verify";

    public static final String IAM_AUTH = "/v3/auth/tokens";

    public static final String CSM_AUTH_CONNECT = "/v2/auth/tokens";

    public static final String CSM_AUTH_DISCONNECT = "/v2/auth/tokens/%s/%s";

    public static final String CSM_AUTH_HANDSHAKE = "/v2/nfvo/shakehand?roattr=status";

    public static final String VNFMMED = "/rest/vnfmmed/";

    public static final String CONNECTMGR_CONNECT = "/connectmgr/v1/connect";

    public static final String CONNECTMGR_DISCONNECT = "/connectmgr/v1/disconnect";

    public static final String CONNECTMGR_HANDSHAKE = "/connectmgr/v1/handshake";

    public static final String CREATE_VNF_PERF = "/staticsmgr/v1/vnfperformance";

    public static final String VNFMGR_INSTANCE = "/vnfmgr/v1/instances";


    public static final String VNFD_FLAVOR = "/vnfdmgr/v1/flavor";

    public static final String UPDATE_RESOURCE = "/rest/v1/resmanage/resuse/updateres";

    public static final String VNF_QUERY = "/resmgr/v1/vnfs";

    public static final String VMS_QUERY = "/resmgr/v1/vms";

    public static final String VNFMGR_VNFKPI = "/staticsmgr/v1/vnfkpi";

    public static final String RES_VNF = "/rest/v1/resmanage/vappvm";

    public static final String NOTIFY_VNF_PERF = "/rest/v1/resmanage/vappvm";

    public static final String PARAM_MODULE = "VnfmDriver";

    public static final String GET_ALL_SOS = "/rest/sodriver/v1/sos";

    public static final String OPERATION_LOG_PATH = "/rest/plat/audit/v1/logs";

    public static final String SYSTEM_LOG_PATH = "/rest/plat/audit/v1/systemlogs";

    public static final String SECURITY_LOG_PATH = "/rest/plat/audit/v1/seculogs";

    public static final String GET_VNFM_VNF = "/rest/v1/resmanage/vapps?vnfmId=%s";

    public static final String GET_RES_NET = "/rest/v1/resmanage/virtualnetworks?id=%s";

    public static final String GET_JOB_STATUS = "/vnfmgr/v1/jobs/%s";

    public static final String VNF_INSTANCE = "/v2/vapps/instances";

    public static final String VNF_INSTANCE_DEL = "/v2/vapps/instances/%s";

    public static final String VNF_INSTANCE_GET = "/v2/vapps/instances/%s";

    public static final String VNF_SCALE = "/v2/vapps/instances/%s/scale";

    public static final String VNF_GET_VMINFO = "/v2/vapps/instances/%s/vm";


    public static final String MSB_REGISTER_URL = "/openoapi/microservices/v1/services";

    public static final String MSB_UNREGISTER_URL = "/openoapi/microservices/v1/services/hw-vnfm";

    public static final String ESR_GET_VNFM_URL = "/openoapi/extsys/v1/vnfms/%s";

    public static final String ESR_GET_VNFMS_URL = "/openoapi/extsys/v1/vnfms";

    public static final String GRANT_RES_URL = "/openoapi/resmgr/v1/resource/grant";

    private ParamConstants() {
        // private contstructor
    }
}
