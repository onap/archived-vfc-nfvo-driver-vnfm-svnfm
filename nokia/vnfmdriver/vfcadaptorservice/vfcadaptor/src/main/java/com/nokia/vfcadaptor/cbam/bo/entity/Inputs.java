package com.nokia.vfcadaptor.cbam.bo.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Inputs {
   
	@JsonProperty("key_name")
	private String key_name;
	
	@JsonProperty("value")
	private String value;
	
	@JsonProperty("type")
	private String type;
	
	@JsonProperty("default1")
	private String default1;
	
	@JsonProperty("description")
	private String description;

	public String getKey_name() {
		return key_name;
	}

	public void setKey_name(String key_name) {
		this.key_name = key_name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDefault1() {
		return default1;
	}

	public void setDefault1(String default1) {
		this.default1 = default1;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	
	
	
	
}
