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

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.TestUtil;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;

import static org.mockito.Mockito.when;

public class TestConditions {

    @Mock
    private ConditionContext conditionContext;
    @Mock
    private Environment environment;

    private String[] activeProfiles = new String[]{"a", "b"};

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        when(conditionContext.getEnvironment()).thenReturn(environment);
        when(environment.getActiveProfiles()).thenReturn(activeProfiles);
    }

    /**
     * if direct integration is not specified VF-C based integration is used
     */
    @Test
    public void testVfcBased() throws Exception {
        //verify
        TestCase.assertTrue(new Conditions.UseForVfc().matches(conditionContext, null));
        TestCase.assertFalse(new Conditions.UseForDirect().matches(conditionContext, null));
    }

    /**
     * if direct integration is not specified VF-C based integration is used
     */
    @Test
    public void testDirectBased() throws Exception {
        activeProfiles[1] = "direct";
        //verify
        TestCase.assertFalse(new Conditions.UseForVfc().matches(conditionContext, null));
        TestCase.assertTrue(new Conditions.UseForDirect().matches(conditionContext, null));
    }

    /**
     * use class in a static way
     */
    @Test
    public void useStaticway() {
        TestUtil.coveragePrivateConstructorForClassesWithStaticMethodsOnly(Conditions.class);
    }

}
