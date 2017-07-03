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

package org.openo.nfvo.vnfmadapter.service.dao.impl;

import java.util.List;

import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.nfvo.vnfmadapter.service.dao.inf.AbstractDao;
import org.openo.nfvo.vnfmadapter.service.dao.inf.VnfmDao;
import org.openo.nfvo.vnfmadapter.service.entity.Vnfm;
import org.openo.nfvo.vnfmadapter.service.mapper.VnfmMapper;

/**
 * VNFM DAO
 * .</br>
 *
 * @author
 * @version     NFVO 0.5  Sep 14, 2016
 */
public class VnfmDaoImpl extends AbstractDao implements VnfmDao {

    @Override
    public int insertVnfm(Vnfm vnfm) throws ServiceException {
        return getMapperManager(VnfmMapper.class).insertVnfm(vnfm);
    }

    @Override
    public int deleteVnfm(String vnfmDn) throws ServiceException {
        return getMapperManager(VnfmMapper.class).deleteVnfm(vnfmDn);
    }

    @Override
    public int updateVnfm(Vnfm vnfm) throws ServiceException {
        return getMapperManager(VnfmMapper.class).updateVnfm(vnfm);
    }

    @Override
    public List<Vnfm> indexVnfms(int pageSize, int pageNo) throws ServiceException {
        VnfmMapper vnfmMapper = getMapperManager(VnfmMapper.class);
        int offset = (pageNo - 1) * pageSize;
        return vnfmMapper.indexVnfms(offset, pageSize);
    }

    @Override
    public Vnfm getVnfmById(String id) {
        return getMapperManager(VnfmMapper.class).getVnfmById(id);
    }
}
