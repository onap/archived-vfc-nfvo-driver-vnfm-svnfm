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

package org.openo.nfvo.vnfmadapter.service.csm.inf;

import net.sf.json.JSONObject;

/**
 * Provide interfaces for instantiate or terminate VNF.
 * <br/>
 *
 * @author
 * @version NFVO 0.5 Aug 24, 2016
 */
public interface InterfaceVnfMgr {

    /**
     * Provide interface for scale VNF.
     * @param vnfObject
     * @param vnfmObject
     * @param vnfmId
     * @param vnfInstanceId
     * @return
     */
    public JSONObject scaleVnf(JSONObject vnfObject, JSONObject vnfmObject, String vnfmId, String vnfInstanceId);
    /**
     * Provide interface for instantiate VNF.
     * <br/>
     *
     * @param subJsonObject
     * @param vnfmObjcet
     * @return
     * @since NFVO 0.5
     */
    JSONObject createVnf(JSONObject subJsonObject, JSONObject vnfmObjcet);

    /**
     * Provide interface for terminate VNF
     * <br/>
     *
     * @param vnfmObject
     * @param vnfId
     * @param vnfObject
     * @return
     * @since NFVO 0.5
     */
    JSONObject removeVnf(JSONObject vnfmObject, String vnfId, JSONObject vnfObject);

    /**
     * Provide interface for get VNF info
     * <br/>
     *
     * @param vnfmObject
     * @param vnfId
     * @return
     * @since NFVO 0.5
     */
    JSONObject getVnf(JSONObject vnfmObject, String vnfId);

    /**
     * Retrieve job
     * <br>
     *
     * @param vnfmObject
     * @param jobId
     * @return
     * @since  NFVO 0.5
     */
    JSONObject getJob(JSONObject vnfmObject, String jobId);
}
