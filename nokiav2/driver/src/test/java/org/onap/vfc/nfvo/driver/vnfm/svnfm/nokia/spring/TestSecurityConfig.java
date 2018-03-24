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

import java.util.HashMap;
import java.util.List;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.test.util.ReflectionTestUtils;

import static junit.framework.TestCase.assertTrue;

public class TestSecurityConfig {

    /**
     * verify that not authentication is performed
     * this can only fully be tested from CT by starting the web service
     */
    @Test
    public void testNoHttpSecurity() throws Exception {
        HttpSecurity http = new HttpSecurity(Mockito.mock(ObjectPostProcessor.class), Mockito.mock(AuthenticationManagerBuilder.class), new HashMap<>());
        //when
        new SecurityConfig().configure(http);
        //verify
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.AuthorizedUrl authorizedUrl = http.authorizeRequests().anyRequest();
        List<? extends RequestMatcher> requestMatchers = (List<? extends RequestMatcher>) ReflectionTestUtils.getField(authorizedUrl, "requestMatchers");
        assertTrue(AnyRequestMatcher.class.isAssignableFrom(requestMatchers.get(0).getClass()));
    }

    /**
     * verify that no web security is performed
     * this can only fully be tested from CT by starting the web service
     */
    @Test
    public void testNoWebSecurity() throws Exception {
        WebSecurity webSecurity = new WebSecurity(Mockito.mock(ObjectPostProcessor.class));
        WebSecurity.IgnoredRequestConfigurer ignorer = Mockito.mock(WebSecurity.IgnoredRequestConfigurer.class);
        ReflectionTestUtils.setField(webSecurity, "ignoredRequestRegistry", ignorer);
        //when
        new SecurityConfig().configure(webSecurity);
        //verify
        Mockito.verify(ignorer).anyRequest();
    }

}
