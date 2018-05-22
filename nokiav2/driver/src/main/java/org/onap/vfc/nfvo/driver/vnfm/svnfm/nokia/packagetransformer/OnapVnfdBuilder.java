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
public class OnapVnfdBuilder {
    public static final String DESCRIPTION = "description";
    public static final String PROPERTIES = "properties";
    public static final String REQUIREMENTS = "requirements";
    private static Logger logger = getLogger(OnapVnfdBuilder.class);

    @VisibleForTesting
    static String indent(String content, int prefixSize) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < prefixSize; i++) {
            sb.append("  ");
        }
        Pattern pattern = Pattern.compile("^(.*)$", Pattern.MULTILINE);
        return pattern.matcher(content).replaceAll(sb.toString() + "$1");
    }

    private static String trimUnit(String data) {
        //FIXME the unit should not be trimmed VF-C bug
        return data.trim().replaceAll("[^0-9]", "");
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
                if ("tosca.nodes.nfv.VDU".equals(type)) {
                    body.append(buildVdu(node.getKey(), node.getValue().getAsJsonObject(), nodeTemplates));
                } else if ("tosca.nodes.nfv.VirtualStorage".equals(type)) {
                    body.append(buildVolume(node.getKey(), node.getValue().getAsJsonObject()));
                } else if ("tosca.nodes.nfv.VL".equals(type)) {
                    body.append(buildVl(node.getKey()));
                } else if ("tosca.nodes.nfv.ICP".equals(type)) {
                    body.append(buildIcp(node.getKey(), node.getValue().getAsJsonObject()));
                } else if ("tosca.nodes.nfv.ECP".equals(type)) {
                    body.append(buildEcp(node.getKey(), node.getValue(), nodeTemplates));
                } else {
                    logger.warn("The {} type is not converted", type);
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
                "  inputs:\n" +
                "    etsi_config:\n" +
                "      type: string\n"+
                "      description: The ETSI configuration\n"+
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
                memorySize = trimUnit(childElement(child(child(virtualCompute, PROPERTIES), "virtual_memory"), "virtual_mem_size").getAsString());
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
                                "    virtual_mem_size: " + trimUnit(memorySize) + "\n" +
                                "  virtual_cpu:\n" +
                                "    num_virtual_cpu: " + cpuCount + "\n", 3) +
                "  " + REQUIREMENTS + ":\n", 2);
        return header + body.toString();
    }

    private String buildEcp(String name, JsonElement ecp, Set<Map.Entry<String, JsonElement>> nodes) {
        if (ecp.getAsJsonObject().has(REQUIREMENTS)) {
            String icpName = getIcpName(ecp.getAsJsonObject().get(REQUIREMENTS).getAsJsonArray());
            if (icpName != null) {
                return buildEcpInternal(name, icpName, nodes);
            } else {
                logger.warn("The {} ecp does not have an internal connection point", name);
            }
        } else {
            logger.warn("The {} ecp does not have an requirements section", name);
        }
        return "";
    }

    private String buildEcpInternal(String ecpName, String icpName, Set<Map.Entry<String, JsonElement>> nodes) {
        JsonObject icpNode = get(icpName, nodes).getAsJsonObject();
        if (icpNode.has(REQUIREMENTS)) {
            String vdu = getVduOfIcp(icpNode.getAsJsonObject().get(REQUIREMENTS).getAsJsonArray());
            //internal connection point is bound to VDU
            if (vdu != null) {
                return buildVduCpd(ecpName, vdu, child(icpNode, PROPERTIES));
            } else {
                logger.warn("The {} internal connection point of the {} ecp does not have a VDU", icpName, ecpName);
            }
        } else {
            logger.warn("The {} internal connection point of the {} ecp does not have a requirements section", icpName, ecpName);
        }
        return "";
    }

    private String getVduOfIcp(JsonArray icpRequirements) {
        String vdu = null;
        for (int i = 0; i < icpRequirements.size(); i++) {
            JsonElement requirement = icpRequirements.get(i);
            Map.Entry<String, JsonElement> next = requirement.getAsJsonObject().entrySet().iterator().next();
            String s = next.getKey();
            if ("virtual_binding".equals(s)) {
                vdu = next.getValue().getAsString();
            }
        }
        return vdu;
    }

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
            if (vdu == null) {
                logger.warn("The {} internal connection point does not have a VDU", name);
            } else if (vl == null) {
                logger.warn("The {} internal connection point does not have a VL", name);
            } else {
                JsonObject properties = child(icp, PROPERTIES);
                return indent(name + ":\n" +
                        "  type: tosca.nodes.nfv.VduCpd\n" +
                        "  " + PROPERTIES + ":\n" +
                        "    layer_protocol: " + childElement(properties, "layer_protocol").getAsString() + "\n" +
                        "    role: leaf\n" + (properties.has(DESCRIPTION) ?
                        "    description: " + childElement(properties, DESCRIPTION).getAsString() + "\n" : "") +
                        "  requirements:\n" +
                        "    - virtual_binding: " + vdu + "\n" +
                        "    - virtual_link: " + vl + "\n", 2);
            }
        } else {
            logger.warn("The {} internal connection point does not have a requirements section", name);
        }
        return "";
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

    private String buildVolume(String nodeName, JsonObject volume) {
        return indent(nodeName + ":\n" +
                "  type: tosca.nodes.nfv.VDU.VirtualStorage\n" +
                "  properties:\n" +
                "    id: " + nodeName + "\n" +
                "    type_of_storage: volume\n" +
                "    size_of_storage: " + trimUnit(childElement(child(volume, PROPERTIES), "size_of_storage").getAsString()) + "\n", 2);
    }

    private String buildVl(String name) {
        return indent(name + ":\n" +
                "  type: tosca.nodes.nfv.VnfVirtualLinkDesc\n" +
                "  properties:\n" +
                "    vl_flavours:\n" +
                "      flavours:\n" +
                "        flavourId: notUsed\n", 2);
    }
}
