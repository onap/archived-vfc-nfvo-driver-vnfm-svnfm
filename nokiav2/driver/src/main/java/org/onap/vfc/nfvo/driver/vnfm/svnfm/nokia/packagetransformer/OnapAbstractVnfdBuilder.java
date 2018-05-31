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
import java.util.HashMap;
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
 * Generic non ONAP version dependent package conversion
 */
abstract class OnapAbstractVnfdBuilder {
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

    protected JsonElement get(String name, Set<Map.Entry<String, JsonElement>> nodes) {
        for (Map.Entry<String, JsonElement> node : nodes) {
            if (name.equals(node.getKey())) {
                return node.getValue();
            }
        }
        throw new NoSuchElementException("The VNFD does not have a node called " + name + " but required by an other node");
    }

    protected String buildEcp(String name, JsonElement ecp, Set<Map.Entry<String, JsonElement>> nodes) {
        if (ecp.getAsJsonObject().has(REQUIREMENTS)) {
            String icpName = getRequirement(ecp.getAsJsonObject().get(REQUIREMENTS).getAsJsonArray(), "internal_connection_point");
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
            String vdu = getRequirement(icpNode.getAsJsonObject().get(REQUIREMENTS).getAsJsonArray(), "virtual_binding");
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

    /**
     * @param cbamVnfd the CBAM VNFD
     * @return the converted ONAP VNFD
     */
    public String toOnapVnfd(String cbamVnfd) {
        JsonObject root = new Gson().toJsonTree(new Yaml().load(cbamVnfd)).getAsJsonObject();
        JsonObject topologyTemplate = child(root, "topology_template");
        JsonObject substitution_mappings = child(topologyTemplate, "substitution_mappings");
        Map<String, JsonElement> virtualLinks = new HashMap<>();
        if (topologyTemplate.has("node_templates")) {
            Set<Map.Entry<String, JsonElement>> nodeTemplates = child(topologyTemplate, "node_templates").entrySet();
            StringBuilder body = new StringBuilder();
            for (Map.Entry<String, JsonElement> node : nodeTemplates) {
                String type = childElement(node.getValue().getAsJsonObject(), "type").getAsString();
                if ("tosca.nodes.nfv.VDU".equals(type)) {
                    body.append(buildVdu(node.getKey(), substitution_mappings, node.getValue().getAsJsonObject(), nodeTemplates));
                } else if ("tosca.nodes.nfv.VirtualStorage".equals(type)) {
                    body.append(buildVolume(node.getKey(), node.getValue().getAsJsonObject()));
                } else if ("tosca.nodes.nfv.VL".equals(type)) {
                    virtualLinks.put(node.getKey(), node.getValue());
                    body.append(buildVl(node.getValue().getAsJsonObject().get(PROPERTIES).getAsJsonObject(), node.getKey()));
                } else if ("tosca.nodes.nfv.ICP".equals(type)) {
                    body.append(buildIcp(node.getKey(), node.getValue().getAsJsonObject()));
                } else if ("tosca.nodes.nfv.ECP".equals(type)) {
                    body.append(buildEcp(node.getKey(), node.getValue(), nodeTemplates));
                } else {
                    logger.warn("The {} type is not converted", type);
                }
            }
            return buildHeader(topologyTemplate, virtualLinks) + body.toString();
        }
        return buildHeader(topologyTemplate, virtualLinks);
    }

    abstract protected String buildHeader(JsonObject toplogyTemplate, Map<String, JsonElement> virtualLinks);

    abstract protected String buildVduCpd(String name, String vdu, JsonObject properties);

    abstract protected String buildVdu(String name, JsonObject vnf, JsonObject vdu, Set<Map.Entry<String, JsonElement>> nodes);

    abstract protected String buildIcp(String name, JsonObject icp);

    abstract protected String buildVolume(String nodeName, JsonObject volume);

    abstract protected String buildVl(JsonObject vlProperties, String name);
}
