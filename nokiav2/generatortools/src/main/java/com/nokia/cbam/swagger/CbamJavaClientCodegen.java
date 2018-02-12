/*
* Copyright 2016-2017 Nokia Corporation
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

package com.nokia.cbam.swagger;

import com.google.common.collect.Sets;
import io.swagger.codegen.*;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;

import java.io.File;
import java.util.*;
import io.swagger.codegen.languages.JavaClientCodegen;

import static com.google.common.collect.Sets.newHashSet;

public class CbamJavaClientCodegen extends JavaClientCodegen {
	public CbamJavaClientCodegen() {
		supportsInheritance = false;
		for (CliOption cliOption : this.cliOptions) {
			if(cliOption.getType().equals("useRxJava")){
				cliOption.setDefault("true");
			}
			System.err.println(cliOption.getOpt() + cliOption.getDefault());
		}

	}

	@Override
	public CodegenModel fromModel(String name, Model model, Map<String, Model> allDefinitions) {
		CodegenModel m = super.fromModel(name, model, allDefinitions);
		//remove the fields inherited from parent objects
		remove(m, newHashSet("OPENSTACK_V3_INFO", "OPENSTACK_V2_INFO", "OTHER_VIM_INFO", "VMWARE_VCLOUD_INFO"), newHashSet("id"));
		remove(m, newHashSet("OtherNotification", "VnfIdentifierCreationNotification", "VnfIdentifierDeletionNotification", "VnfInfoAttributeValueChangeNotification", "VnfLifecycleChangeNotification"), newHashSet("notificationType", "subscriptionId", "timestamp", "vnfInstanceId"));

		if(model instanceof ModelImpl && (!m.isEnum) && !m.hasVars) {
			typeMapping.put(m.classname, typeMapping.get(((ModelImpl)model).getType()));
		}
		return m;
	}

	public void remove(CodegenModel m, Set<String> classNames, Set<String> fieldNames){
		System.err.println("**** Processing " + m.name);
		if(classNames.contains(m.name)){
			System.err.println("**** Processing " + m.name);
			Iterator<CodegenProperty> iterator = m.vars.iterator();
			while(iterator.hasNext()){
				CodegenProperty prop = iterator.next();
				if(fieldNames.contains(prop.name)){
					System.err.println("****  Removing: " + prop.name + " from " + m.name);
					iterator.remove();
				}
				System.err.println("********* Keeping " + prop.name + " from " + m.name);
			}
		}
	}

	@Override
	public Map<String, Object> postProcessAllModels(Map<String, Object> objs) {
		Map<String, Object> m = super.postProcessAllModels(objs);
		for(Map.Entry<String, Object> entry: m.entrySet()) {
			CodegenModel cm = ((HashMap<String, CodegenModel>)((List)((HashMap)entry.getValue()).get("models")).get(0)).get("model");
			for (CodegenProperty var : cm.vars) {
				if (typeMapping.containsKey(var.datatype)) {
					var.dataFormat = var.datatypeWithEnum = typeMapping.get(var.datatype);
				}
			}
		}
		return m;
	}

}
