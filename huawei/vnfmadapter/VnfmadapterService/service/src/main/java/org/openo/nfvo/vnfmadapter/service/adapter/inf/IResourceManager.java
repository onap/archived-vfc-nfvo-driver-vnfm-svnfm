/*
 * Copyright 2016 Huawei Technologies Co., Ltd.
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

package org.openo.nfvo.vnfmadapter.service.adapter.inf;

import java.util.Map;

import net.sf.json.JSONObject;

/**
 * Resource Manager interface.</br>
 *
 * @author
 * @version     NFVO 0.5  Sep 13, 2016
 */
public interface IResourceManager {

    /**
     * Get VNFM CSAR information<br>
     *
     * @param csarid String
     * @return
     * @since  NFVO 0.5
     */
    JSONObject getVnfmCsarInfo(String csarid);

    /**
     * Download CSAR.<br>
     *
     * @param url String
     * @param filePath String
     * @return
     * @since  NFVO 0.5
     */
    JSONObject downloadCsar(String url,String filePath);

    /**
     * Get all clouds<br>
     *
     * @param url String
     * @return
     * @since  NFVO 0.5
     */
    JSONObject getAllCloud(String url,String connToken);


    /**
     * get VNFD Plan Info.<br>
     *
     * @param url String
     * @param vnfdid String
     * @return
     * @since  NFVO 0.5
     */
    JSONObject getVNFDPlanInfo(String url, String vnfdid, String conntoken);

    /**
     * Upload VNF package.<br>
     *
     * @param vnfpkg JSONObject
     * @param paramsMap Map<String, String>
     * @return
     * @since  NFVO 0.5
     */
    JSONObject uploadVNFPackage(JSONObject vnfpkg, Map<String, String> paramsMap);
}
