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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.restapi;

import javax.servlet.ServletOutputStream;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core.SelfRegistrationManagerForVfc;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;


public class TestSwaggerApi extends TestBase {

    private SwaggerApi swaggerApi;
    @Mock
    private SelfRegistrationManagerForVfc selfRegistrationManagerForVfc;

    @Before
    public void initMocks() throws Exception {
        setField(SwaggerApi.class, "logger", logger);
        swaggerApi = new SwaggerApi(selfRegistrationManagerForVfc);
    }

    /**
     * test swagger definition get
     */
    @Test
    public void testSwaggerRetrieval() throws Exception {
        byte[] bytes = new byte[]{1, 2};
        when(selfRegistrationManagerForVfc.getSwaggerApiDefinition()).thenReturn(bytes);
        ServletOutputStream os = Mockito.mock(ServletOutputStream.class);
        when(httpResponse.getOutputStream()).thenReturn(os);
        //when
        swaggerApi.getSwaggerApiDefinition(httpResponse);
        //verify
        verify(httpResponse).addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        verify(httpResponse).addHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(bytes.length));
        verify(os).write(bytes);
        verify(logger).info("REST: get swagger definition");
    }
}
