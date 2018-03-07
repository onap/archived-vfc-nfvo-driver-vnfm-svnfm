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
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Pattern;

import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.child;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.childElement;

/**
 * Transforms a CBAM package into an ONAP package
 */
public class OnapVnfdBuilder {

    public static final String DESCRIPTION = "description";
    public static final String PROPERTIES = "properties";
    public static final String REQUIREMENTS = "requirements";

    @VisibleForTesting
    static String indent(String content, int prefixSize) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < prefixSize; i++) {
            sb.append("  ");
        }
        Pattern pattern = Pattern.compile("^(.*)$", Pattern.MULTILINE);
        return pattern.matcher(content).replaceAll(sb.toString() + "$1");
    }

    /**
     * @param cbamVnfd the CBAM VNFD
     * @return the converted ONAP VNFD
     */
    public String toOnapVnfd(String cbamVnfd) {
        JsonObject root = new Gson().toJsonTree(new Yaml().load(cbamVnfd)).getAsJsonObject();
        JsonObject topologyTemplate = child(root, "topology_template");
        if (topologyTemplate.has("node_templates")) {
            Set<Map.Entry<String, JsonElement>> nodeTemplates = child(topologyTemplate, "node_templates").entrySet();
            StringBuilder body = new StringBuilder();
            for (Map.Entry<String, JsonElement> node : nodeTemplates) {
                String type = childElement(node.getValue().getAsJsonObject(), "type").getAsString();
                switch (type) {
                    case "tosca.nodes.nfv.VDU":
                        body.append(buildVdu(node.getKey(), node.getValue().getAsJsonObject(), nodeTemplates));
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
                        body.append(buildEcp(node.getKey(), node.getValue(), nodeTemplates));
                        break;
                    default:
                        //
                }
            }
            return buildHeader(topologyTemplate) + body.toString();
        }
        return buildHeader(topologyTemplate);
    }

    private String buildHeader(JsonObject toplogyTemplate) {
        JsonObject properties = child(child(toplogyTemplate, "substitution_mappings"), PROPERTIES);
        String descriptorVersion = properties.get("descriptor_version").getAsString();
        return "tosca_definitions_version: tosca_simple_yaml_1_0\n" +
                "\n" +
                "metadata:\n" +
                "  vendor: Nokia\n" +
                "  csarVersion: " + descriptorVersion + "\n" +
                "  csarProvider: " + properties.get("provider").getAsString() + "\n" +
                "  id: Simple\n" +
                "  version: " + properties.get("software_version").getAsString() + "\n" +
                "  csarType: NFAR\n" +
                "  name: " + properties.get("product_name").getAsString() + "\n" +
                "  vnfdVersion: " + descriptorVersion + "\n\n" +
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
        JsonArray vduRequirements = childElement(vdu.getAsJsonObject(), REQUIREMENTS).getAsJsonArray();
        for (int i = 0; i < vduRequirements.size(); i++) {
            JsonObject requirement = vduRequirements.get(i).getAsJsonObject();
            Map.Entry<String, JsonElement> next = requirement.entrySet().iterator().next();
            String s = next.getKey();
            if ("virtual_compute".equals(s)) {
                JsonObject virtualCompute = get(next.getValue().getAsString(), nodes).getAsJsonObject();
                cpuCount = childElement(child(child(virtualCompute, PROPERTIES), "virtual_cpu"), "num_virtual_cpu").getAsString();
                memorySize = childElement(child(child(virtualCompute, PROPERTIES), "virtual_memory"), "virtual_mem_size").getAsString();

            } else if ("virtual_storage".equals(s)) {
                String item = indent(
                        "- virtual_storage:\n" +
                                "    capability: tosca.capabilities.nfv.VirtualStorage\n" +
                                "    node: " + next.getValue().getAsString() + "\n", 4);
                body.append(item);

            }
            next.getValue();
        }
        String header = indent(name + ":\n" +
                "  type: tosca.nodes.nfv.VDU.Compute\n" +
                "  capabilities:\n" +
                "    virtual_compute:\n" +
                indent(
                        "properties:\n" +
                                "  virtual_memory:\n" +
                                "    virtual_mem_size: " + memorySize + "\n" +
                                "  virtual_cpu:\n" +
                                "    num_virtual_cpu: " + cpuCount + "\n", 3) +
                "  " + REQUIREMENTS + ":\n", 2);
        return header + body.toString();
    }

    private String buildEcp(String name, JsonElement ecp, Set<Map.Entry<String, JsonElement>> nodes) {
        if (ecp.getAsJsonObject().has(REQUIREMENTS)) {
            String icpName = getIcpName(ecp.getAsJsonObject().get(REQUIREMENTS).getAsJsonArray());
            if (icpName != null) {
                return buildIcp(name, icpName, nodes);
            }
        }
        return "";
    }

    private String buildIcp(String name, String icpName, Set<Map.Entry<String, JsonElement>> nodes) {
        JsonObject icpNode = get(icpName, nodes).getAsJsonObject();
        if (icpNode.has(REQUIREMENTS)) {
            String vdu = getVdu(icpNode.getAsJsonObject().get(REQUIREMENTS).getAsJsonArray());
            if (vdu != null) {
                return buildVduCpd(name, vdu, child(icpNode, PROPERTIES));
            }
        }
        return "";
    }

    @Nullable
    private String getVdu(JsonArray requirements) {
        String vdu = null;
        for (int i = 0; i < requirements.size(); i++) {
            JsonElement requirement = requirements.get(i);
            Map.Entry<String, JsonElement> next = requirement.getAsJsonObject().entrySet().iterator().next();
            String s = next.getKey();
            if ("virtual_binding".equals(s)) {
                vdu = next.getValue().getAsString();
            }
        }
        return vdu;
    }

    @Nullable
    private String getIcpName(JsonArray requirements) {
        String icpName = null;
        for (int i = 0; i < requirements.size(); i++) {
            JsonElement requirement = requirements.get(i);
            Map.Entry<String, JsonElement> next = requirement.getAsJsonObject().entrySet().iterator().next();
            String s = next.getKey();
            if ("internal_connection_point".equals(s)) {
                icpName = next.getValue().getAsString();
            }
        }
        return icpName;
    }

    private String buildVduCpd(String name, String vdu, JsonObject properties) {
        return indent(name + ":\n" +
                "  type: tosca.nodes.nfv.VduCpd\n" +
                "  " + PROPERTIES + ":\n" +
                "    layer_protocol: " + childElement(properties, "layer_protocol").getAsString() + "\n" +
                "    role: leaf\n" +
                (properties.has(DESCRIPTION) ?
                        "    description: " + childElement(properties, DESCRIPTION).getAsString() + "\n" : "") +
                "  requirements:\n" +
                "    - virtual_binding: " + vdu + "\n", 2);
    }

    private String buildIcp(String name, JsonObject icp) {
        if (icp.has(REQUIREMENTS)) {
            JsonArray requirements = icp.get(REQUIREMENTS).getAsJsonArray();
            String vdu = null;
            String vl = null;
            for (int i = 0; i < requirements.size(); i++) {
                JsonElement requirement = requirements.get(i);
                Map.Entry<String, JsonElement> next = requirement.getAsJsonObject().entrySet().iterator().next();
                String s = next.getKey();
                if ("virtual_binding".equals(s)) {
                    vdu = next.getValue().getAsString();

                } else if ("virtual_link".equals(s)) {
                    vl = next.getValue().getAsString();
                }
            }
            if (vdu != null && vl != null) {
                JsonObject properties = child(icp, PROPERTIES);
                return "    " + name + ":\n" +
                        "      type: tosca.nodes.nfv.VduCpd\n" +
                        "      " + PROPERTIES + ":\n" +
                        "        layer_protocol: " + childElement(properties, "layer_protocol").getAsString() + "\n" +
                        "        role: leaf\n" + (properties.has(DESCRIPTION) ?
                        "        description: " + childElement(properties, DESCRIPTION).getAsString() + "\n" : "") +
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
                "        size_of_storage: " + childElement(child(volume, PROPERTIES), "size_of_storage").getAsString() + "\n";
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
