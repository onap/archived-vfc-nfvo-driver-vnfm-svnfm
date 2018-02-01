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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.child;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.childElement;

/**
 * Transforms a CBAM package into an ONAP package
 */
public class OnapVnfdBuilder {

    private String buildHeader(JsonObject toplogyTemplate) {
        JsonObject properties = child(child(toplogyTemplate, "substitution_mappings"), "properties");
        String descriptor_version = properties.get("descriptor_version").getAsString();
        return "tosca_definitions_version: tosca_simple_yaml_1_0\n" +
                "\n" +
                "metadata:\n" +
                "  vendor: Nokia\n" +
                "  csarVersion: " + descriptor_version + "\n" +
                "  csarProvider: " + properties.get("provider").getAsString() + "\n" +
                "  id: Simple\n" +
                "  version: " + properties.get("software_version").getAsString() + "\n" +
                "  csarType: NFAR\n" +
                "  name: " + properties.get("product_name").getAsString() + "\n" +
                "  vnfdVersion: " + descriptor_version + "\n\n" +
                "topology_template:\n" +
                "  node_templates:\n";
    }

    private JsonElement get(String name, Set<Map.Entry<String, JsonElement>> nodes) {
        for (Map.Entry<String, JsonElement> node : nodes) {
            if (name.equals(node.getKey())) {
                return node.getValue();
            }
        }
        throw new NoSuchElementException("The VNFD does not have a node called " + name + " but required by an other node");
    }

    private String buildVdu(String name, JsonObject vdu, Set<Map.Entry<String, JsonElement>> nodes) {
        String memorySize = "";
        String cpuCount = "";
        StringBuilder body = new StringBuilder();
        JsonArray vduRequirements = childElement(vdu.getAsJsonObject(), "requirements").getAsJsonArray();
        for (int i = 0; i < vduRequirements.size(); i++) {
            JsonObject requirement = vduRequirements.get(i).getAsJsonObject();
            Map.Entry<String, JsonElement> next = requirement.entrySet().iterator().next();
            switch (next.getKey()) {
                case "virtual_compute":
                    JsonObject virtualCompute = get(next.getValue().getAsString(), nodes).getAsJsonObject();
                    cpuCount = childElement(child(child(virtualCompute, "properties"), "virtual_cpu"), "num_virtual_cpu").getAsString();
                    memorySize = childElement(child(child(virtualCompute, "properties"), "virtual_memory"), "virtual_mem_size").getAsString();
                    break;
                case "virtual_storage":
                    String item =
                            "        - virtual_storage:\n" +
                                    "            capability: tosca.capabilities.nfv.VirtualStorage\n" +
                                    "            node: " + next.getValue().getAsString() + "\n";
                    body.append(item);
                    break;
            }
            next.getValue();
        }
        String header = "    " + name + ":\n" +
                "      type: tosca.nodes.nfv.VDU.Compute\n" +
                "      capabilities:\n" +
                "        virtual_compute:\n" +
                "          properties:\n" +
                "            virtual_memory:\n" +
                "              virtual_mem_size: " + memorySize + "\n" +
                "            virtual_cpu:\n" +
                "              num_virtual_cpu: " + cpuCount + "\n" +
                "      requirements:\n";
        return header + body.toString();
    }

    /**
     * @param cbamVnfd the CBAM VNFD
     * @return the converted ONAP VNFD
     */
    public String toOnapVnfd(String cbamVnfd) {
        JsonObject root = new Gson().toJsonTree(new Yaml().load(cbamVnfd)).getAsJsonObject();
        JsonObject topology_template = child(root, "topology_template");
        if (topology_template.has("node_templates")) {
            Set<Map.Entry<String, JsonElement>> node_templates = child(topology_template, "node_templates").entrySet();
            StringBuilder body = new StringBuilder();
            for (Map.Entry<String, JsonElement> node : node_templates) {
                String type = childElement(node.getValue().getAsJsonObject(), "type").getAsString();
                switch (type) {
                    case "tosca.nodes.nfv.VDU":
                        body.append(buildVdu(node.getKey(), node.getValue().getAsJsonObject(), node_templates));
                        break;
                    case "tosca.nodes.nfv.VirtualStorage":
                        body.append(buildVolume(node.getKey(), node.getValue().getAsJsonObject()));
                        break;
                    case "tosca.nodes.nfv.VL":
                        body.append(buildVl(node.getKey()));
                        break;
                    case "tosca.nodes.nfv.ICP":
                        body.append(buildIcp(node.getKey(), node.getValue().getAsJsonObject()));
                        break;
                    case "tosca.nodes.nfv.ECP":
                        body.append(buildEcp(node.getKey(), node.getValue(), node_templates));
                        break;
                }
            }
            return buildHeader(topology_template) + body.toString();
        }
        return buildHeader(topology_template);
    }

    private String buildEcp(String name, JsonElement ecp, Set<Map.Entry<String, JsonElement>> nodes) {
        if (ecp.getAsJsonObject().has("requirements")) {
            JsonArray requirements = ecp.getAsJsonObject().get("requirements").getAsJsonArray();
            String icpName = null;
            for (int i = 0; i < requirements.size(); i++) {
                JsonElement requirement = requirements.get(i);
                Map.Entry<String, JsonElement> next = requirement.getAsJsonObject().entrySet().iterator().next();
                switch (next.getKey()) {
                    case "internal_connection_point":
                        icpName = next.getValue().getAsString();

                }
            }
            if (icpName != null) {
                JsonObject icpNode = get(icpName, nodes).getAsJsonObject();
                String vdu = null;
                if (icpNode.has("requirements")) {
                    requirements = icpNode.getAsJsonObject().get("requirements").getAsJsonArray();
                    for (int i = 0; i < requirements.size(); i++) {
                        JsonElement requirement = requirements.get(i);
                        Map.Entry<String, JsonElement> next = requirement.getAsJsonObject().entrySet().iterator().next();
                        switch (next.getKey()) {
                            case "virtual_binding":
                                vdu = next.getValue().getAsString();
                        }
                    }
                    if (vdu != null) {
                        JsonObject properties = child(icpNode, "properties");
                        return "    " + name + ":\n" +
                                "      type: tosca.nodes.nfv.VduCpd\n" +
                                "      properties:\n" +
                                "        layer_protocol: " + childElement(properties, "layer_protocol").getAsString() + "\n" +
                                "        role: leaf\n" +
                                (properties.has("description") ?
                                        "        description: " + childElement(properties, "description").getAsString() + "\n" : "") +
                                "      requirements:\n" +
                                "        - virtual_binding: " + vdu + "\n";
                    }
                }
            }
        }
        return "";
    }

    private String buildIcp(String name, JsonObject icp) {
        if (icp.has("requirements")) {
            JsonArray requirements = icp.get("requirements").getAsJsonArray();
            String vdu = null;
            String vl = null;
            for (int i = 0; i < requirements.size(); i++) {
                JsonElement requirement = requirements.get(i);
                Map.Entry<String, JsonElement> next = requirement.getAsJsonObject().entrySet().iterator().next();
                switch (next.getKey()) {
                    case "virtual_binding":
                        vdu = next.getValue().getAsString();
                    case "virtual_link":
                        vl = next.getValue().getAsString();
                        break;
                }
            }
            if (vdu != null && vl != null) {
                JsonObject properties = child(icp, "properties");
                return "    " + name + ":\n" +
                        "      type: tosca.nodes.nfv.VduCpd\n" +
                        "      properties:\n" +
                        "        layer_protocol: " + childElement(properties, "layer_protocol").getAsString() + "\n" +
                        "        role: leaf\n" + (properties.has("description") ?
                        "        description: " + childElement(properties, "description").getAsString() + "\n" : "") +
                        "      requirements:\n" +
                        "        - virtual_binding: " + vdu + "\n" +
                        "        - virtual_link: " + vl + "\n";
            }
        }
        return "";
    }

    private String buildVolume(String nodeName, JsonObject volume) {
        return "    " + nodeName + ":\n" +
                "      type: tosca.nodes.nfv.VDU.VirtualStorage\n" +
                "      properties:\n" +
                "        id: " + nodeName + "\n" +
                "        type_of_storage: volume\n" +
                "        size_of_storage: " + childElement(child(volume, "properties"), "size_of_storage").getAsString() + "\n";
    }

    private String buildVl(String name) {
        return "    " + name + ":\n" +
                "      type: tosca.nodes.nfv.VnfVirtualLinkDesc\n" +
                "      properties:\n" +
                "        vl_flavours:\n" +
                "          flavours:\n" +
                "            flavourId: notUsed\n";
    }
}
