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

import com.google.common.collect.Sets;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.HashSet;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * Collects the possibilities of sources
 */
public class Conditions {

    private static final String USE_DIRECT_INTEGRATION = "direct";

    private static Set<Condition> getAllSources() {
        return newHashSet(new UseForVfc(), new UseForDirect());
    }

    /**
     * Represents the condition for using VF-C
     */
    public static class UseForVfc implements Condition {
        @Override
        public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
            boolean anyOtherSourceAvailable = false;
            for (Condition condition : Conditions.getAllSources()) {
                if (!(condition instanceof UseForVfc) && condition.matches(conditionContext, annotatedTypeMetadata)) {
                    anyOtherSourceAvailable = true;
                }
            }
            return !anyOtherSourceAvailable;
        }
    }

    /**
     * Represents the condition for using ONAP components directly
     */
    public static class UseForDirect implements Condition {
        @Override
        public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
            HashSet<String> activeProfiles = Sets.newHashSet(conditionContext.getEnvironment().getActiveProfiles());
            return activeProfiles.contains(USE_DIRECT_INTEGRATION);
        }
    }
}
