/*
 * Copyright 2016-2017, Nokia Corporation
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

import com.nokia.cbam.lcm.v32.ApiClient;
import com.nokia.cbam.lcm.v32.model.*;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class TestInhertence {

    @Test
    public void testVimInfo(){
        InstantiateVnfRequest req = new InstantiateVnfRequest();
        OPENSTACKV2INFO vim = new OPENSTACKV2INFO();
        req.getVims().add(vim);
        vim.setVimInfoType(VimInfo.VimInfoTypeEnum.OPENSTACK_V2_INFO);
        OpenStackAccessInfoV2 accessInfo = new OpenStackAccessInfoV2();
        accessInfo.setPassword("myPassword");
        vim.setAccessInfo(accessInfo);
        String serialize = new ApiClient().getJSON().serialize(req);
        assertTrue(serialize.contains("myPassword"));
    }

    @Test
    public void testLcn() throws  Exception{
        VnfLifecycleChangeNotification vnfLifecycleChangeNotification = new VnfLifecycleChangeNotification();
        vnfLifecycleChangeNotification.setNotificationType(VnfNotificationType.VNFLIFECYCLECHANGENOTIFICATION);
        vnfLifecycleChangeNotification.setVnfInstanceId("myId");
        String serialize = new ApiClient().getJSON().serialize(vnfLifecycleChangeNotification);

        VnfLifecycleChangeNotification deserialize = new ApiClient().getJSON().deserialize(serialize, VnfLifecycleChangeNotification.class);

        assertEquals("myId", deserialize.getVnfInstanceId());
    }
    
}
