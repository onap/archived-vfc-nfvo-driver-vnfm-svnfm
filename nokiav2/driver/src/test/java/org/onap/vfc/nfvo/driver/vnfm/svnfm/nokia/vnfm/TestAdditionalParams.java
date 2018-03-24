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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm;

import com.google.common.collect.Maps;
import com.nokia.cbam.lcm.v32.model.NetworkAddress;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Test;
import pl.pojo.tester.internal.field.AbstractFieldValueChanger;
import pl.pojo.tester.internal.field.DefaultFieldValueChanger;
import pl.pojo.tester.internal.field.collections.map.AbstractMapFieldValueChanger;

import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;


public class TestAdditionalParams {

    /**
     * Test basic POJO behaviour
     */
    @Test
    public void test() {

        class MapValueChanger extends AbstractMapFieldValueChanger<Map<String, List<NetworkAddress>>> {
            @Override
            protected Map<String, List<NetworkAddress>> increaseValue(Map<String, List<NetworkAddress>> stringListMap, Class<?> aClass) {
                if (stringListMap == null) {
                    return Maps.newHashMap();
                } else {
                    stringListMap.put(UUID.randomUUID().toString(), null);
                    return stringListMap;
                }
            }

            @Override
            protected boolean canChange(Class<?> type) {
                return type.getCanonicalName().contains("Map");
            }
        }

        final AbstractFieldValueChanger valueChanger = new MapValueChanger().attachNext(DefaultFieldValueChanger.INSTANCE);
        assertPojoMethodsFor(AdditionalParameters.class).using(valueChanger).areWellImplemented();
    }
}
