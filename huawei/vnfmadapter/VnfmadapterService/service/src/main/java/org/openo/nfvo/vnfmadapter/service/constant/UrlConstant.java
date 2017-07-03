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
 * URL constant
 * 
 * @author
 * @version NFVO 0.5 Sep 6, 2016
 */
public class UrlConstant {

    public static final String REST_MSB_REGISTER = "/openoapi/microservices/v1/services";

    public static final String REST_DRIVERMGR_REGISTER = "/openoapi/drivermgr/v1/drivers";

    public static final String REST_CSARINFO_GET = "/openoapi/catalog/v1/csars/%s";

    public static final String REST_VNFMINFO_GET = "/openoapi/extsys/v1/vnfms/%s";

    public static final String URL_ALLCLOUD_GET = "/rest/vnfm/vnfmvim/v2/computeservice/getAllCloud";

    public static final String URL_ALLCLOUD_NEW_GET = "/v2/vnfm/vims";

    public static final String URL_VNFPACKAGE_POST = "/v2/vapps/templates";

    public static final String URL_VNFDINFO_GET = "/v2/vapps/templates/%s";

    public static final String URL_VNFDPLANINFO_GET = "/v2/vapps/templates/%s/plans";

    public static final String PORT_COMMON = "31943";

    public static final String PORT_UPLOADVNFPKG = "30001";

    private UrlConstant() {
        // Constructor
    }

}
