package com.onec.datamcp.integration.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ObjectDescription extends ObjectRef {

    private List<AttributeDescriptor> attributes = new ArrayList<>();
    private List<TabularSectionDescriptor> tabularSections = new ArrayList<>();
    private List<AttributeDescriptor> dimensions = new ArrayList<>();
    private List<AttributeDescriptor> resources = new ArrayList<>();
    private List<ObjectRef> values = new ArrayList<>();
    private List<String> virtualTables = new ArrayList<>();

    public List<AttributeDescriptor> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<AttributeDescriptor> attributes) {
        this.attributes = attributes;
    }

    public List<TabularSectionDescriptor> getTabularSections() {
        return tabularSections;
    }

    public void setTabularSections(List<TabularSectionDescriptor> tabularSections) {
        this.tabularSections = tabularSections;
    }

    public List<AttributeDescriptor> getDimensions() {
        return dimensions;
    }

    public void setDimensions(List<AttributeDescriptor> dimensions) {
        this.dimensions = dimensions;
    }

    public List<AttributeDescriptor> getResources() {
        return resources;
    }

    public void setResources(List<AttributeDescriptor> resources) {
        this.resources = resources;
    }

    public List<ObjectRef> getValues() {
        return values;
    }

    public void setValues(List<ObjectRef> values) {
        this.values = values;
    }

    public List<String> getVirtualTables() {
        return virtualTables;
    }

    public void setVirtualTables(List<String> virtualTables) {
        this.virtualTables = virtualTables;
    }
}
