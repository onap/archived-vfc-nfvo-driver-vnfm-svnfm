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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import junit.framework.TestCase;
import org.junit.Test;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.child;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.TestUtil.loadFile;

public class TestSwaggerDefinitionConsistency extends TestBase {

    public static final HashSet<Class<?>> CLASSES = Sets.newHashSet(LcmApi.class, LcnApi.class, SwaggerApi.class, ConverterApi.class, SoV2Api.class);

    @Test
    public void test() throws Exception {
        JsonObject root = new JsonParser().parse(new String(loadFile("self.swagger.json"))).getAsJsonObject();
        String basePath = root.get("basePath").getAsString();
        HashMultimap<String, RequestMethod> expectedPaths = HashMultimap.create();
        for (Map.Entry<String, JsonElement> pathName : child(root, "paths").entrySet()) {
            JsonObject path = child(child(root, "paths"), pathName.getKey());
            for (Map.Entry<String, JsonElement> method : path.entrySet()) {
                locate(basePath + pathName.getKey());
                expectedPaths.put(basePath + pathName.getKey(), RequestMethod.valueOf(method.getKey().toUpperCase()));
            }
        }

        for (Class<?> clazz : CLASSES) {
            RequestMapping currentBasePath = clazz.getAnnotation(RequestMapping.class);
            for (Method method : clazz.getMethods()) {
                RequestMapping methodMapping = method.getAnnotation(RequestMapping.class);
                if (methodMapping != null) {
                    String fPath = currentBasePath.value()[0] + methodMapping.value()[0];
                    RequestMethod restMethod = methodMapping.method()[0];
                    Set<RequestMethod> currentMethods = expectedPaths.get(fPath);
                    if (!currentMethods.contains(restMethod)) {
                        TestCase.fail("Not documented REST API " + fPath + " " + restMethod + " current " + currentMethods);
                    }
                }
            }
        }
    }

    private void locate(String path) {
        Set<String> paths = new HashSet<>();
        for (Class<?> clazz : CLASSES) {
            RequestMapping basePath = clazz.getAnnotation(RequestMapping.class);
            for (Method method : clazz.getMethods()) {
                RequestMapping methodMapping = method.getAnnotation(RequestMapping.class);
                if (methodMapping != null) {
                    paths.add(basePath.value()[0] + methodMapping.value()[0]);
                    if (path.equals(basePath.value()[0] + methodMapping.value()[0])) {
                        return;
                    }
                }
            }
        }
        for (Class<?> clazz : CLASSES) {
            RequestMapping basePath = clazz.getAnnotation(RequestMapping.class);
            for (Method method : clazz.getMethods()) {
                RequestMapping methodMapping = method.getAnnotation(RequestMapping.class);
                if (methodMapping != null) {
                    paths.add(basePath.value()[0] + methodMapping.value()[0]);
                    if (path.equals(basePath.value()[0] + methodMapping.value()[0])) {
                        return;
                    }
                }
            }
        }
        throw new NoSuchElementException(path + " in " + paths);
    }
}
