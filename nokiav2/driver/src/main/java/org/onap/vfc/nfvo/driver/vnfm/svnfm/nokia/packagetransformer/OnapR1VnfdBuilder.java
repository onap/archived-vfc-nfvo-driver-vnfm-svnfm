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
import org.slf4j.Logger;

import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.child;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.childElement;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.LifecycleManager.ETSI_CONFIG;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Transforms a CBAM package into an ONAP package
 */
public class OnapR1VnfdBuilder extends OnapAbstractVnfdBuilder {
    private static Logger logger = getLogger(OnapR1VnfdBuilder.class);

    private static String trimUnit(String data) {
        //The R1 templates in Amsterdam release can not handle the scalar-unit types in Tosca
        //templates, so that the MB, GB, ... units need to be removed even though the created
        //Tosca template will be invalid
        return data.trim().replaceAll("[^0-9]", "");
    }

    @Override
    protected String buildHeader(JsonObject toplogyTemplate, Map<String, JsonElement> virtualLinks) {
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
                "    " + ETSI_CONFIG + ":\n" +
                "      type: string\n" +
                "      description: The ETSI configuration\n" +
                "  node_templates:\n";
    }

    @Override
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

    protected String buildVduCpd(String name, String vdu, JsonObject properties) {
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

    protected String buildVolume(String nodeName, JsonObject volume) {
        return indent(nodeName + ":\n" +
                "  type: tosca.nodes.nfv.VDU.VirtualStorage\n" +
                "  properties:\n" +
                "    id: " + nodeName + "\n" +
                "    type_of_storage: volume\n" +
                "    size_of_storage: " + trimUnit(childElement(child(volume, PROPERTIES), "size_of_storage").getAsString()) + "\n", 2);
    }

    @Override
    protected String buildVl(JsonObject vlProperties, String name) {
        return indent(name + ":\n" +
                "  type: tosca.nodes.nfv.VnfVirtualLinkDesc\n" +
                "  properties:\n" +
                "    vl_flavours:\n" +
                "      flavours:\n" +
                "        flavourId: notUsed\n", 2);
    }
}
