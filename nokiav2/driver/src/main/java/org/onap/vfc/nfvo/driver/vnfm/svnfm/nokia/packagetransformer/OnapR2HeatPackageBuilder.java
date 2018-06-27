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

import com.google.common.collect.Sets;
import com.google.gson.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.LifecycleManager;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import static java.util.stream.Collectors.toSet;

import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.*;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Transforms a CBAM package into an ONAP package
 */
public class OnapR2HeatPackageBuilder {
    public static final String ETSI_MODIFIABLE_ATTRIBUTES_EXTENSTION = "etsi_modifiable_attributes_extenstion_";
    public static final String IMAGE_NAME = "_image_name";
    public static final String FLAVOR_NAME = "_flavor_name";
    public static final String NET_ID = "_net_id";
    private static Logger logger = getLogger(OnapR2HeatPackageBuilder.class);
    public static final String BASE_ENV_FILE_NAME = "base.env";
    public static final String BASE_YAML_FILE_NAME = "base.yaml";

    private Set<String> autoManagedExtensions = Sets.newHashSet(LifecycleManager.ONAP_CSAR_ID, LifecycleManager.EXTERNAL_VNFM_ID);

    Map<String, String> processVnfd(String cbamVnfd) {
        Map<String, String> files = new HashMap<>();
        JsonObject root = new Gson().toJsonTree(new Yaml().load(cbamVnfd)).getAsJsonObject();
        JsonObject topologyTemplate = child(root, "topology_template");
        JsonObject substitutionMappings = child(topologyTemplate, "substitution_mappings");
        JsonObject capabilities = child(substitutionMappings, "capabilities");
        JsonObject deploymentFlavour = child(capabilities, "deployment_flavour");
        JsonArray policies = childElement(topologyTemplate, "policies").getAsJsonArray();
        JsonObject manifest = new JsonObject();
        manifest.addProperty("name", "ONAP VNF package");
        manifest.addProperty("description", "");
        JsonArray data = new JsonArray();
        manifest.add("data", data);
        JsonObject deploymentFlavorProperties = child(deploymentFlavour, "properties");
        if (deploymentFlavorProperties.has("scaling_aspects")) {
            JsonObject scalingAspects = child(deploymentFlavorProperties, "scaling_aspects");
            for (Map.Entry<String, JsonElement> scalingAspect : scalingAspects.entrySet()) {
                processAspect(files, data, policies, scalingAspect.getKey(), childElement(scalingAspect.getValue().getAsJsonObject(), "max_scale_level").getAsLong());
            }
        }
        processBaseIncrement(topologyTemplate, files, policies, data);
        files.put("MANIFEST.json", new GsonBuilder().setPrettyPrinting().create().toJson(manifest));
        return files;
    }

    private void processBaseIncrement(JsonObject topologyTemplate, Map<String, String> files, JsonArray policies, JsonArray data) {
        StringBuilder envContent = prepareEvnContent();
        StringBuilder yamlContent = prepareYamlContent();
        if (topologyTemplate.has("node_templates")) {
            JsonObject nodeTemplates = child(topologyTemplate, "node_templates");
            processEcps(nodeTemplates, envContent, yamlContent);
            for (Map.Entry<String, JsonElement> vdu : filterType(nodeTemplates, "tosca.nodes.nfv.VDU")) {
                addImageAndFlavor(envContent, yamlContent, vdu);
            }
        }
        processModifiableAttributes(topologyTemplate, envContent, yamlContent);
        data.add(buildManifestEntry(BASE_ENV_FILE_NAME, BASE_YAML_FILE_NAME, true));
        files.put(BASE_ENV_FILE_NAME, envContent.toString());
        files.put(BASE_YAML_FILE_NAME, yamlContent.toString());
    }

    private void processModifiableAttributes(JsonObject topologyTemplate, StringBuilder envContent, StringBuilder yamlContent) {
        JsonObject capabilities = child(child(topologyTemplate, "substitution_mappings"), "capabilities");
        if (capabilities.has("vnf")) {
            JsonObject vnf = child(capabilities, "vnf");
            if (vnf.has("properties")) {
                JsonObject properties = child(vnf, "properties");
                if (properties.has("modifiable_attributes")) {
                    JsonObject modifiableAttributes = child(properties, "modifiable_attributes");
                    if (modifiableAttributes.has("extensions")) {
                        JsonObject extensions = child(modifiableAttributes, "extensions");
                        for (Map.Entry<String, JsonElement> extension : extensions.entrySet()) {
                            if (!autoManagedExtensions.contains(extension.getKey())) {
                                addParameter(yamlContent, envContent, ETSI_MODIFIABLE_ATTRIBUTES_EXTENSTION + extension.getKey(), "Modifiable attribute", "Modifiable attribute for " + extension.getKey());
                            }
                        }
                    }
                }
            }
        }
    }

    public static Set<Map.Entry<String, JsonElement>> filterType(JsonObject nodeTemplates, String type) {
        return nodeTemplates.entrySet().stream().filter(e -> e.getValue().getAsJsonObject().get("type").equals(type)).collect(toSet());
    }

    private void processEcps(JsonObject nodeTemplates, StringBuilder envContent, StringBuilder yamlContent) {
        for (Map.Entry<String, JsonElement> node : filterType(nodeTemplates, "tosca.nodes.nfv.ECP")) {
            envContent.append("  " + node.getKey() + NET_ID + ": PUT YOUR NETWORK ID HERE\n");
            addYamlParameter(yamlContent, node.getKey() + NET_ID, "Network id", "Network identifier for " + node.getKey() + " ECP");
        }
    }

    private StringBuilder prepareYamlContent() {
        StringBuilder yamlContent = new StringBuilder();
        yamlContent.append("heat_template_version: 2013-05-23\n");
        yamlContent.append("parameters:\n");
        return yamlContent;
    }

    private void processAspect(Map<String, String> files, JsonArray data, JsonArray policies, String aspectName, long maxScaleLevel) {
        JsonObject aspect = locateAspect(locateHeatPolicy(policies), aspectName);
        StringBuilder envContent = prepareEvnContent();
        StringBuilder yamlContent = prepareYamlContent();
        if (aspect.has("vdus")) {
            processMapping(aspect, envContent, yamlContent);
        }
        if (maxScaleLevel > 1001) {
            throw buildFatalFailure(logger, "Refusing to create more than 1001 scaling levels");
        }
        envContent.append("  etsi.scalingAspectId: "+ aspectName + "\n");
        for (int scaleIndex = 0; scaleIndex < maxScaleLevel; scaleIndex++) {
            String envFileName = "module_" + aspectName + "_" + scaleIndex + ".env";
            files.put(envFileName, envContent.toString());
            String yamlFileName = "module_" + aspectName + "_" + scaleIndex + ".yaml";
            files.put(yamlFileName, yamlContent.toString());
            data.add(buildManifestEntry(envFileName, yamlFileName, false));
        }
    }

    private StringBuilder prepareEvnContent() {
        StringBuilder envContent = new StringBuilder();
        envContent.append("parameters:\n");
        return envContent;
    }

    private JsonObject buildManifestEntry(String envFileName, String yamlFileName, boolean base) {
        JsonObject manifestEntry = new JsonObject();
        manifestEntry.addProperty("file", yamlFileName);
        manifestEntry.addProperty("type", "HEAT");
        manifestEntry.addProperty("isBase", Boolean.toString(base));
        JsonArray envEntries = new JsonArray();
        manifestEntry.add("data", envEntries);
        JsonObject envEntry = new JsonObject();
        envEntries.add(envEntry);
        envEntry.addProperty("file", envFileName);
        envEntry.addProperty("type", "HEAT_ENV");
        return manifestEntry;
    }

    private void processMapping(JsonObject mapping, StringBuilder envContent, StringBuilder yamlContent) {
        for (Map.Entry<String, JsonElement> vdusElement : child(mapping, "vdus").entrySet()) {
            addImageAndFlavor(envContent, yamlContent, vdusElement);
        }
        if (mapping.has("externalConnectionPoints")) {
            for (Map.Entry<String, JsonElement> externalConnectionPoints : child(mapping, "externalConnectionPoints").entrySet()) {
                addParameter(yamlContent, envContent, externalConnectionPoints.getKey() + "_net_id", "Network id", "Network to be used for " + externalConnectionPoints.getKey() + " ECP");
                addParameter(yamlContent, envContent, externalConnectionPoints.getKey() + "_subnet_id", "Subnet id", "Subnet to be used for " + externalConnectionPoints.getKey() + " ECP");
            }
        }
    }

    private void addImageAndFlavor(StringBuilder envContent, StringBuilder yamlContent, Map.Entry<String, JsonElement> vdusElement) {
        String vdu = vdusElement.getKey();
        addParameter(yamlContent, envContent, vdu + IMAGE_NAME, "Image name or identifier", "Image to be used for " + vdu + " VDU");
        addParameter(yamlContent, envContent, vdu + FLAVOR_NAME, "Flavor name or identifier", "Flavor to be used for " + vdu + " VDU");
    }

    private void addParameter(StringBuilder yamlContent, StringBuilder envContent, String key, String label, String description) {
        addYamlParameter(yamlContent, key, label, description);
        envContent.append("  " + key + ": PUT YOUR " + label.toUpperCase() + " HERE\n");
    }

    private void addYamlParameter(StringBuilder yamlContent, String key, String label, String description) {
        yamlContent.append("  " + key + ":\n");
        yamlContent.append("    type: string\n");
        yamlContent.append("    label: " + label + "\n");
        yamlContent.append("    description: " + description + "\n");
    }

    private JsonObject locateHeatPolicy(JsonArray policies) {
        for (int index = 0; index < policies.size(); index++) {
            JsonObject c = policies.get(index).getAsJsonObject();
            JsonObject policy = c.getAsJsonObject().entrySet().iterator().next().getValue().getAsJsonObject();
            if ("tosca.policies.nfv.HeatMapping".equals(childElement(policy, "type").getAsString())) {
                return policy;
            }
        }
        throw buildFatalFailure(logger, "The heat_mapping section is missing from VNFD");
    }

    private JsonObject locateAspect(JsonObject policy, String aspectName) {
        for (Map.Entry<String, JsonElement> aspect : child(child(policy, "properties"), "aspects").entrySet()) {
            if (aspect.getKey().equals(aspectName)) {
                return aspect.getValue().getAsJsonObject();
            }
        }
        throw buildFatalFailure(logger, "Unable to locate " + aspectName + " in heat policy");
    }
}
