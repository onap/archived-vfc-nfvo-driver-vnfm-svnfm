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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.packagetransformer;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.child;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.childElement;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Transforms a CBAM package into an ONAP package
 */
public class OnapAbstractVnfdBuilder {
    public static final String DESCRIPTION = "description";
    public static final String PROPERTIES = "properties";
    public static final String REQUIREMENTS = "requirements";
    private static Logger logger = getLogger(OnapAbstractVnfdBuilder.class);

    @VisibleForTesting
    static String indent(String content, int prefixSize) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < prefixSize; i++) {
            sb.append("  ");
        }
        Pattern pattern = Pattern.compile("^(.*)$", Pattern.MULTILINE);
        return pattern.matcher(content).replaceAll(sb.toString() + "$1");
    }

    protected static String getRequirement(JsonArray requirements, String key) {
        for (int i = 0; i < requirements.size(); i++) {
            JsonElement requirement = requirements.get(i);
            Map.Entry<String, JsonElement> next = requirement.getAsJsonObject().entrySet().iterator().next();
            String s = next.getKey();
            if (key.equals(s)) {
                return next.getValue().getAsString();
            }
        }
        return null;
    }

    private JsonElement get(String name, Set<Map.Entry<String, JsonElement>> nodes) {
        for (Map.Entry<String, JsonElement> node : nodes) {
            if (name.equals(node.getKey())) {
                return node.getValue();
            }
        }
        throw new NoSuchElementException("The VNFD does not have a node called " + name + " but required by an other node");
    }
}
