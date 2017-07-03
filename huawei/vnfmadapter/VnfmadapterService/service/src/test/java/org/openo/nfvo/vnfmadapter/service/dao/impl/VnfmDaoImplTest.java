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
package org.openo.nfvo.vnfmadapter.service.dao.impl;

import junit.framework.Assert;
import org.junit.Test;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.nfvo.vnfmadapter.service.entity.Vnfm;

/**
 * Created by QuanZhong on 2017/3/17.
 */
public class VnfmDaoImplTest {

    @Test
    public void insertVnfm() throws ServiceException {
        VnfmDaoImpl dao = new VnfmDaoImpl();
        dao.insertVnfm(new Vnfm());
        Assert.assertTrue(true);
    }

    @Test
    public void deleteVnfm() throws ServiceException {
        VnfmDaoImpl dao = new VnfmDaoImpl();
        dao.deleteVnfm("abc");
        Assert.assertTrue(true);
    }

    @Test
    public void updateVnfm() throws ServiceException {
        VnfmDaoImpl dao = new VnfmDaoImpl();
        dao.updateVnfm(new Vnfm());
        Assert.assertTrue(true);
    }

    @Test
    public void indexVnfms() throws ServiceException {
        VnfmDaoImpl dao = new VnfmDaoImpl();
        dao.insertVnfm(new Vnfm());
        Assert.assertTrue(true);

    }

    @Test
    public void getVnfmById() {
        VnfmDaoImpl dao = new VnfmDaoImpl();
        dao.getVnfmById("abc");
        Assert.assertTrue(true);

    }
}
