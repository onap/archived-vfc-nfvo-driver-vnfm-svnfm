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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Map;
import java.util.Set;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core.SelfRegistrationManager;
import org.slf4j.Logger;

import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.child;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.childElement;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.LifecycleManager.ETSI_CONFIG;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Transforms a CBAM package into an ONAP package
 */
public class OnapR2VnfdBuilder extends OnapAbstractVnfdBuilder {
    private static Logger logger = getLogger(OnapR2VnfdBuilder.class);

    protected String buildHeader(JsonObject toplogyTemplate, Map<String, JsonElement> virtualLinks) {
        JsonObject substitution_mappings = child(toplogyTemplate, "substitution_mappings");
        String vnfContent = buildVnf(substitution_mappings, virtualLinks);
        return "tosca_definitions_version: tosca_simple_profile_yaml_1_1\n" +
                "\n" +
                "topology_template:\n" +
                "  inputs:\n" +
                "    " + ETSI_CONFIG + ":\n" +
                "      type: string\n" +
                "      description: The ETSI configuration\n" +
                "  node_templates:\n" + vnfContent;
    }

    protected String buildVdu(String name, JsonObject vnf, JsonObject vdu, Set<Map.Entry<String, JsonElement>> nodes) {
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
        JsonObject flavourProperties = child(child(child(vnf, "capabilities"), "deployment_flavour"), "properties");
        JsonObject vduSizes = child(child(flavourProperties, "vdu_profile"), name);
        String header = indent(name + ":\n" +
                "  type: tosca.nodes.nfv.Vdu.Compute\n" +
                "  properties:\n" +
                "    name: " + name + "\n" +
                "    description: " + childElement(child(vdu, PROPERTIES), "description").getAsString() + "\n" +
                "    configurable_properties:\n" +
                "    vdu_profile:\n" +
                "      min_number_of_instances: " + childElement(vduSizes, "min_number_of_instances").getAsString() + "\n" +
                "      max_number_of_instances: " + childElement(vduSizes, "max_number_of_instances").getAsString() + "\n" +
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

    private String buildVnf(JsonObject vnf, Map<String, JsonElement> virtualLinks) {
        JsonObject vnfProperties = child(vnf, PROPERTIES);
        JsonObject flavourProperties = child(child(child(vnf, "capabilities"), "deployment_flavour"), "properties");
        StringBuilder vlContent = new StringBuilder();
        for (Map.Entry<String, JsonElement> virtualLink : virtualLinks.entrySet()) {
            vlContent.append(indent("- virtual_link:\n" +
                    "    capability: tosca.capabilities.nfv.VirtualLinkable\n" +
                    "    node: " + virtualLink.getKey() + "\n", 4));
        }
        return indent("VNF:\n" +
                "  type: tosca.nodes.nfv.VNF\n" +
                "  " + PROPERTIES + ":\n" +
                "    descriptor_id: " + childElement(vnfProperties, "descriptor_id").getAsString() + "\n" +
                "    descriptor_version: " + childElement(vnfProperties, "descriptor_version").getAsString() + "\n" +
                "    provider: " + childElement(vnfProperties, "provider").getAsString() + "\n" +
                "    product_name: " + childElement(vnfProperties, "product_name").getAsString() + "\n" +
                "    software_version: " + childElement(vnfProperties, "software_version").getAsString() + "\n" +
                "    product_info_name: " + childElement(vnfProperties, "product_info_name").getAsString() + "\n" +
                (vnfProperties.has("product_info_description") ?
                        "    product_info_description: " + childElement(vnfProperties, "product_info_description").getAsString() + "\n" : "") +
                "    vnfm_info: [ " + SelfRegistrationManager.SERVICE_NAME + " ]\n" +
                "    flavour_id: " + childElement(flavourProperties, "flavour_id").getAsString() + "\n" +
                "    flavour_description: " + childElement(flavourProperties, "description").getAsString() + "\n", 2) +
                "      " + REQUIREMENTS + ":\n" +
                vlContent.toString();
    }

    protected String buildIcp(String name, JsonObject icp) {
        if (icp.has(REQUIREMENTS)) {
            JsonArray requirements = icp.get(REQUIREMENTS).getAsJsonArray();
            String vdu = getRequirement(requirements, "virtual_binding");
            String vl = getRequirement(requirements, "virtual_link");
            if (vdu == null) {
                logger.warn("The {} internal connection point does not have a VDU", name);
            } else if (vl == null) {
                logger.warn("The {} internal connection point does not have a VL", name);
            } else {
                JsonObject properties = child(icp, PROPERTIES);
                return indent(name + ":\n" +
                        "  type: tosca.nodes.nfv.VduCp\n" +
                        "  " + PROPERTIES + ":\n" +
                        "    layer_protocol: [ " + childElement(properties, "layer_protocol").getAsString() + " ]\n" +
                        (properties.has(DESCRIPTION) ?
                                "    description: " + childElement(properties, DESCRIPTION).getAsString() + "\n" : "") +
                        "    protocol_data: []\n" +
                        "    trunk_mode: false\n" +
                        "  requirements:\n" +
                        "    - virtual_binding: " + vdu + "\n" +
                        "    - virtual_link: " + vl + "\n", 2);
            }
        } else {
            logger.warn("The {} internal connection point does not have a requirements section", name);
        }
        return "";
    }

    protected String buildVduCpd(String name, String vdu, JsonObject properties) {
        return indent(name + ":\n" +
                "  type: tosca.nodes.nfv.VduCp\n" +
                "  " + PROPERTIES + ":\n" +
                "    layer_protocol: [ " + childElement(properties, "layer_protocol").getAsString() + " ]\n" +
                "    protocol_data: [ ]\n" +
                "    trunk_mode: false\n" +
                (properties.has(DESCRIPTION) ?
                        "    description: " + childElement(properties, DESCRIPTION).getAsString() + "\n" : "") +
                "  requirements:\n" +
                "    - virtual_binding: " + vdu + "\n", 2);
    }

    protected String buildVolume(String nodeName, JsonObject volume) {
        return indent(nodeName + ":\n" +
                "  type: tosca.nodes.nfv.Vdu.VirtualStorage\n" +
                "  properties:\n" +
                "    type_of_storage: volume\n" +
                "    size_of_storage: " + childElement(child(volume, PROPERTIES), "size_of_storage").getAsString() + "\n", 2);
    }

    protected String buildVl(JsonObject vlProperties, String name) {
        JsonObject connectivityType = child(vlProperties, "connectivity_type");
        return indent(name + ":\n" +
                "  type: tosca.nodes.nfv.VnfVirtualLink\n" +
                "  properties:\n" +
                "    connectivity_type:\n" +
                "      layer_protocol: [ " + childElement(connectivityType, "layer_protocol").getAsString() + " ]\n" +
                (connectivityType.has("flow_pattern") ? "      flow_pattern: " + childElement(connectivityType, "flow_pattern").getAsString() + "\n" : "") +
                "    vl_profile:\n" +
                "      max_bit_rate_requirements:\n" +
                "        root: " + Integer.MAX_VALUE + "\n" + //FIXME GAP IN CBAM TEMPLATE
                "        leaf: " + Integer.MAX_VALUE + "\n" + //FIXME GAP IN CBAM TEMPLATE
                "      min_bit_rate_requirements:\n" +
                "        root: 0\n" + //FIXME GAP IN CBAM TEMPLATE
                "        leaf: 0\n", 2);  //FIXME GAP IN CBAM TEMPLATE
    }
}
