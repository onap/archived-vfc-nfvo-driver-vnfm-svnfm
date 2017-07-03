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
package org.openo.nfvo.vnfmadapter.service.activator;

import org.junit.Assert;
import org.junit.Test;
import org.openo.nfvo.vnfmadapter.service.api.internalsvc.impl.VnfmAdapterMgrService;

/**
 * Created by QuanZhong on 2017/3/17.
 */
public class RoaVnfmServicePostProcessorTest {
    @Test
    public void testPostProcessAfterInitialization(){
        RoaVnfmServicePostProcessor dm = new RoaVnfmServicePostProcessor();
        dm.postProcessAfterInitialization(new VnfmAdapterMgrService(),"abc");
        Assert.assertTrue(true);
    }
    @Test
    public void testPostProcessAfterInitialization2(){
        RoaVnfmServicePostProcessor dm = new RoaVnfmServicePostProcessor();
        dm.postProcessAfterInitialization(null,"abc");
        Assert.assertTrue(true);
    }

    @Test
    public void testpostProcessBeforeDestruction(){
        RoaVnfmServicePostProcessor dm = new RoaVnfmServicePostProcessor();
        dm.postProcessBeforeDestruction(new VnfmAdapterMgrService(),"abc");
        Assert.assertTrue(true);
    }

    @Test
    public void testpostProcessBeforeInitialization(){
        RoaVnfmServicePostProcessor dm = new RoaVnfmServicePostProcessor();
        dm.postProcessBeforeInitialization(new VnfmAdapterMgrService(),"abc");
        Assert.assertTrue(true);
    }
}
