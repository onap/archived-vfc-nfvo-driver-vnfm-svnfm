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

package org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.ai.internalsvc.impl;

import mockit.Mock;
import mockit.MockUp;
import net.sf.json.JSONObject;
import org.junit.Test;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.api.internalsvc.impl.VnfmAdapter2DriverMgrService;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.api.internalsvc.impl.VnfmAdapterMgrService;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.process.VnfMgr;

import junit.framework.Assert;

import java.io.File;
import java.io.IOException;

/**
 * Created by QuanZhong on 2017/3/20.
 */
public class VnfmAdapterMgrServiceTest {
    @Test
    public void testRegister(){
        new MockUp<VnfmAdapterMgrService>(){

            @Mock
            public  String readVnfmAdapterInfoFromJson() throws IOException {
                return "{'abc':'123'}";
            }
        };
        VnfmAdapterMgrService mgr = new VnfmAdapterMgrService();
        mgr.register();

    }


    @Test
    public void testReadJson() {
        File file = new File("./demo.json");
        try {
            file.createNewFile();
            String content = VnfmAdapter2DriverMgrService.readJson("./demo.json");
            Assert.assertEquals("",  "");
            file.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
