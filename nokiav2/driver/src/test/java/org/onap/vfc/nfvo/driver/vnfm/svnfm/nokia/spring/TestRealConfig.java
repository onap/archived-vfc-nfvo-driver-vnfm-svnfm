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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.spring;

import com.nokia.cbam.lcm.v32.model.*;
import org.junit.Test;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.threeten.bp.OffsetDateTime;

import static com.google.common.collect.Iterables.filter;
import static junit.framework.TestCase.assertEquals;

public class TestRealConfig {

    /**
     * test that the converter can transform the inherited classes
     */
    @Test
    public void test() throws Exception {
        HttpMessageConverters converters = new RealConfig().customConverters();
        //verify
        GsonHttpMessageConverter httpMessageConverter1 = filter(converters.getConverters(), GsonHttpMessageConverter.class).iterator().next();
        httpMessageConverter1.canRead(VnfIdentifierCreationNotification.class, MediaType.APPLICATION_JSON);
        httpMessageConverter1.canRead(VnfIdentifierDeletionNotification.class, MediaType.APPLICATION_JSON);
        httpMessageConverter1.canRead(VnfInfoAttributeValueChangeNotification.class, MediaType.APPLICATION_JSON);
        httpMessageConverter1.canRead(VnfLifecycleChangeNotification.class, MediaType.APPLICATION_JSON);
        httpMessageConverter1.canRead(String.class, MediaType.APPLICATION_JSON);

        MockHttpOutputMessage out = new MockHttpOutputMessage();
        VnfLifecycleChangeNotification not = new VnfLifecycleChangeNotification();
        not.setNotificationType(VnfNotificationType.VNFLIFECYCLECHANGENOTIFICATION);
        not.setVnfInstanceId("vnfId");
        OffsetDateTime now = OffsetDateTime.now();
        not.setTimestamp(now);
        httpMessageConverter1.write(not, MediaType.APPLICATION_JSON, out);
        String write = out.getBodyAsString();
        HttpInputMessage x = new MockHttpInputMessage(write.getBytes());
        VnfLifecycleChangeNotification deserialized = (VnfLifecycleChangeNotification) httpMessageConverter1.read(VnfLifecycleChangeNotification.class, x);
        assertEquals("vnfId", deserialized.getVnfInstanceId());
        assertEquals(now, deserialized.getTimestamp());

    }

}
