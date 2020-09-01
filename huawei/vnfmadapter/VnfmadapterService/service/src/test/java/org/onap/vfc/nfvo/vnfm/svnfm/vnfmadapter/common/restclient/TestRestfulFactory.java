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

package org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.restclient;

import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

/**
 * <br/>
 * <p>
 * </p>
 * 
 * @author
 * @version
 */
@RunWith(JMockit.class)
public class TestRestfulFactory {

    /**
     * <br/>
     * 
     * @throws java.lang.Exception
     * @since
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * <br/>
     * 
     * @throws java.lang.Exception
     * @since
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * <br/>
     * 
     * @throws java.lang.Exception
     * @since
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * <br/>
     * 
     * @throws java.lang.Exception
     * @since
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * <br/>
     * 
     * @since
     */
    @Test
    public void testGetRestInstance() {
        Restful instance = RestfulFactory.getRestInstance("https");

//        new MockUp<HttpRest>() {
//
//            @Mock
//            public void initHttpRest(final RestfulOptions option) throws ServiceException {
//                throw new ServiceException();
//            }
//
//        };
        instance = RestfulFactory.getRestInstance("http");
        assertNotNull(instance);

        instance = RestfulFactory.getRestInstance("http");
        assertNotNull(instance);
    }
}
