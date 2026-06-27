package com.onec.datamcp.integration.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TabularSectionDescriptor {

    private String name;
    private String synonym;
    private List<AttributeDescriptor> attributes = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSynonym() {
        return synonym;
    }

    public void setSynonym(String synonym) {
        this.synonym = synonym;
    }

    public List<AttributeDescriptor> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<AttributeDescriptor> attributes) {
        this.attributes = attributes;
    }
}
