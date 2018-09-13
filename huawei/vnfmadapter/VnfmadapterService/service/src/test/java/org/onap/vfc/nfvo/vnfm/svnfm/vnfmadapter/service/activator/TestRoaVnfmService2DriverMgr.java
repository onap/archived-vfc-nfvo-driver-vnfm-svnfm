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
package org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.activator;

import org.junit.Assert;
import org.junit.Test;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.activator.RoaVnfmService2DriverMgr;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.api.internalsvc.impl.VnfmAdapter2DriverMgrService;

/**
 * Created by QuanZhong on 2017/3/17.
 */
public class TestRoaVnfmService2DriverMgr {
    @Test
    public void testPostProcessAfterInitialization(){
        RoaVnfmService2DriverMgr dm = new RoaVnfmService2DriverMgr();
        dm.postProcessAfterInitialization(new VnfmAdapter2DriverMgrService(),"abc");
        Assert.assertTrue(true);
    }
    @Test
    public void testPostProcessAfterInitialization2(){
        RoaVnfmService2DriverMgr dm = new RoaVnfmService2DriverMgr();
        dm.postProcessAfterInitialization(null,"abc");
        Assert.assertTrue(true);
    }

    @Test
    public void testpostProcessBeforeDestruction(){
        RoaVnfmService2DriverMgr dm = new RoaVnfmService2DriverMgr();
        dm.postProcessBeforeDestruction(new VnfmAdapter2DriverMgrService(),"abc");
        Assert.assertTrue(true);
    }

    @Test
    public void testpostProcessBeforeInitialization(){
        RoaVnfmService2DriverMgr dm = new RoaVnfmService2DriverMgr();
        dm.postProcessBeforeInitialization(new VnfmAdapter2DriverMgrService(),"abc");
        Assert.assertTrue(true);
    }
}