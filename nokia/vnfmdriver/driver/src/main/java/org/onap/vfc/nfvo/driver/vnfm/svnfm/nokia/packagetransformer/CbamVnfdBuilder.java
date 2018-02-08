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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.gson.*;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;

import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.child;

/**
 * Modifies a CBAM VNFD to fit ONAP
 */
public class CbamVnfdBuilder {

    /**
     * @param cbamVnfdContent the original CBAM VNFD
     * @return the modified content CBAM VNFD
     */
    public String build(String cbamVnfdContent) throws IOException {
        JsonObject root = new Gson().toJsonTree(new Yaml().load(cbamVnfdContent)).getAsJsonObject();
        JsonObject substitution_mappings = child(child(root, "topology_template"), "substitution_mappings");
        JsonObject extensions = addChild(addChild(addChild(addChild(addChild(substitution_mappings, "capabilities"), "vnf"), "properties"), "modifiable_attributes"), "extensions");
        JsonObject onapCsarId = addChild(extensions, "onapCsarId");
        onapCsarId.add("default", new JsonPrimitive("kuku"));
        JsonObject vimId = addChild(extensions, "vimId");
        vimId.add("default", new JsonPrimitive("kuku"));
        JsonObject interfaces = child(substitution_mappings, "interfaces");
        JsonObject basic = addChild(interfaces, "Basic");
        addOperationParams(addChild(basic, "instantiate"));
        addOperationParams(addChild(basic, "terminate"));
        if (interfaces.has("Scalable")) {
            addOperationParams(addChild(child(interfaces, "Scalable"), "scale"));
        }
        if (interfaces.has("Healable")) {
            addOperationParams(addChild(child(interfaces, "Healable"), "heal"));
        }
        JsonNode jsonNodeTree = new ObjectMapper().readTree(new GsonBuilder().setPrettyPrinting().create().toJson(root));
        return new YAMLMapper().writeValueAsString(jsonNodeTree);
    }

    private void addOperationParams(JsonObject operation) {
        JsonObject inputs = addChild(operation, "inputs");
        JsonObject extensions = addChild(inputs, "extensions");
        JsonArray pre_actions = addChildArray(extensions, "pre_actions");
        pre_actions.add(addAction("javascript/cbam.pre.collectConnectionPoints.js"));
        JsonArray post_actions = addChildArray(extensions, "post_actions");
        post_actions.add(addAction("javascript/cbam.post.collectConnectionPoints.js"));
        JsonObject additional_parameters = addChild(inputs, "additional_parameters");
        additional_parameters.addProperty("jobId", "kuku");
    }

    private JsonElement addAction(String jsAction) {
        JsonObject action = new JsonObject();
        action.addProperty("javascript", jsAction);
        JsonArray myInclude = new JsonArray();
        myInclude.add("javascript/cbam.collectConnectionPoints.js");
        action.add("include", myInclude);
        action.addProperty("output", "operation_result");
        return action;
    }

    private JsonArray addChildArray(JsonObject root, String name) {
        if (root.has(name)) {
            return root.get(name).getAsJsonArray();
        } else {
            JsonArray child = new JsonArray();
            root.add(name, child);
            return child;
        }
    }

    private JsonObject addChild(JsonObject root, String name) {
        if (root.has(name)) {
            return root.get(name).getAsJsonObject();
        } else {
            JsonObject child = new JsonObject();
            root.add(name, child);
            return child;
        }
    }
}
