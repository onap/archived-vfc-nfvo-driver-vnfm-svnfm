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

import com.google.gson.*;
import java.io.StringReader;
import org.yaml.snakeyaml.Yaml;

import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.child;

/**
 * Modifies a CBAM VNFD to fit ONAP
 */
public class CbamVnfdBuilder {

    /**
     * @param cbamVnfdContent the original CBAM VNFD
     * @return the modified content CBAM VNFD
     */
    public String build(String cbamVnfdContent) {
        JsonObject root = new Gson().toJsonTree(new Yaml().load(cbamVnfdContent)).getAsJsonObject();
        JsonObject substitutionMappings = child(child(root, "topology_template"), "substitution_mappings");
        JsonObject extensions = addChild(addChild(addChild(addChild(addChild(substitutionMappings, "capabilities"), "vnf"), "properties"), "modifiable_attributes"), "extensions");
        JsonObject onapCsarId = addChild(extensions, "onapCsarId");
        onapCsarId.add("default", new JsonPrimitive("kuku"));
        JsonObject externalVnfmId = addChild(extensions, "externalVnfmId");
        externalVnfmId.add("default", new JsonPrimitive("kuku"));
        JsonObject vimId = addChild(extensions, "vimId");
        vimId.add("default", new JsonPrimitive("kuku"));
        JsonObject interfaces = child(substitutionMappings, "interfaces");
        JsonObject basic = addChild(interfaces, "Basic");
        addOperationParams(addChild(basic, "instantiate"));
        addOperationParams(addChild(basic, "terminate"));
        if (interfaces.has("Scalable")) {
            addOperationParams(addChild(child(interfaces, "Scalable"), "scale"));
        }
        if (interfaces.has("Healable")) {
            addOperationParams(addChild(child(interfaces, "Healable"), "heal"));
        }
        return new Yaml().dump(new Yaml().load(new StringReader(new Gson().toJson(root))));
    }

    private void addOperationParams(JsonObject operation) {
        JsonObject inputs = addChild(operation, "inputs");
        JsonObject extensions = addChild(inputs, "extensions");
        JsonArray preActions = addChildArray(extensions, "pre_actions");
        preActions.add(addAction("javascript/cbam.pre.collectConnectionPoints.js"));
        JsonArray postActions = addChildArray(extensions, "post_actions");
        postActions.add(addAction("javascript/cbam.post.collectConnectionPoints.js"));
        JsonObject additionalParameters = addChild(inputs, "additional_parameters");
        additionalParameters.addProperty("jobId", "kuku");
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
