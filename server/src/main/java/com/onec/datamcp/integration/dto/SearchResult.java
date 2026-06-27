package com.onec.datamcp.integration.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResult {

    private List<ObjectRef> items = new ArrayList<>();

    public List<ObjectRef> getItems() {
        return items;
    }

    public void setItems(List<ObjectRef> items) {
        this.items = items;
    }
}
