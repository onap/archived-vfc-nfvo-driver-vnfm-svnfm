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

import com.nokia.cbam.lcm.v32.model.VnfIdentifierCreationNotification;
import com.nokia.cbam.lcm.v32.model.VnfIdentifierDeletionNotification;
import com.nokia.cbam.lcm.v32.model.VnfInfoAttributeValueChangeNotification;
import com.nokia.cbam.lcm.v32.model.VnfLifecycleChangeNotification;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.NokiaSvnfmApplication;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.http.MediaType;

import javax.print.attribute.standard.Media;

import static org.mockito.Mockito.verify;

public class TestRealConfig {

    /**
     * test that the converter can transform the inherited classes
     */
    @Test
    public void test() throws Exception {
        HttpMessageConverters converters = new RealConfig().customConverters();
        //verify
        converters.getConverters().get(0).canRead(VnfIdentifierCreationNotification.class, MediaType.APPLICATION_JSON);
        converters.getConverters().get(0).canRead(VnfIdentifierDeletionNotification.class, MediaType.APPLICATION_JSON);
        converters.getConverters().get(0).canRead(VnfInfoAttributeValueChangeNotification.class, MediaType.APPLICATION_JSON);
        converters.getConverters().get(0).canRead(VnfLifecycleChangeNotification.class, MediaType.APPLICATION_JSON);
        converters.getConverters().get(0).canRead(String.class, MediaType.APPLICATION_JSON);
    }

}
