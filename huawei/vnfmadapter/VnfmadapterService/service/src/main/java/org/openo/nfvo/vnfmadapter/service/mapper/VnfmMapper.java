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

package org.openo.nfvo.vnfmadapter.service.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.openo.nfvo.vnfmadapter.service.entity.Vnfm;

/**
 * VNFM mapper.</br>
 *
 * @author
 * @version     NFVO 0.5  Sep 14, 2016
 */
public interface VnfmMapper {

    /**
     * Add VNFM
     * <br>
     *
     * @param vnfm Vnfm
     * @return
     * @since  NFVO 0.5
     */
    int insertVnfm(Vnfm vnfm);

    /**
     * Update VNFM
     * <br>
     *
     * @param vnfm Vnfm
     * @return
     * @since  NFVO 0.5
     */
    int updateVnfm(Vnfm vnfm);

    /**
     * Delete VNFM
     * <br>
     *
     * @param vnfmDn
     * @return
     * @since  NFVO 0.5
     */
    int deleteVnfm(String vnfmDn);

    /**
     * Index VNFM
     * <br>
     *
     * @param offset
     * @param pageSize
     * @return
     * @since  NFVO 0.5
     */
    List<Vnfm> indexVnfms(@Param("offset") int offset, @Param("pageSize") int pageSize);

    /**
     * Get VNFM counts.
     * <br>
     *
     * @return
     * @since  NFVO 0.5
     */
    int getCountVnfms();

    /**
     * Get VNFM by id
     * <br>
     *
     * @param id
     * @return
     * @since  NFVO 0.5
     */
    Vnfm getVnfmById(String id);
}
