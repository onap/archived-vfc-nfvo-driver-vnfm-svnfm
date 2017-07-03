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

package org.openo.nfvo.vnfmadapter.service.dao.inf;

import java.util.List;

import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.nfvo.vnfmadapter.service.entity.Vnfm;

/**
 * VNFM DAO
 * .</br>
 *
 * @author
 * @version     NFVO 0.5  Sep 14, 2016
 */
public interface VnfmDao {

    /**
     * Add VNFM
     * <br>
     *
     * @param vnfm
     * @return
     * @throws ServiceException
     * @since  NFVO 0.5
     */
    int insertVnfm(Vnfm vnfm) throws ServiceException;

    /**
     * Delete VNFM
     * <br>
     *
     * @param vnfmDn
     * @return
     * @throws ServiceException
     * @since  NFVO 0.5
     */
    int deleteVnfm(String vnfmDn) throws ServiceException;

    /**
     * Update VNFM
     * <br>
     *
     * @param vnfm
     * @return
     * @throws ServiceException
     * @since  NFVO 0.5
     */
    int updateVnfm(Vnfm vnfm) throws ServiceException;

    /**
     * Index VNFM
     * <br>
     *
     * @param pageSize
     * @param pageNo
     * @return
     * @throws ServiceException
     * @since  NFVO 0.5
     */
    List<Vnfm> indexVnfms(int pageSize, int pageNo) throws ServiceException;

    /**
     * Get VNFM by id.
     * <br>
     *
     * @param id
     * @return
     * @since  NFVO 0.5
     */
    Vnfm getVnfmById(String id);
}
