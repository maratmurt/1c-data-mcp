package com.onec.datamcp.integration.dto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryResult {

    private List<QueryColumn> columns = new ArrayList<>();
    private List<Map<String, Object>> rows = new ArrayList<>();
    private int rowCount;
    private boolean truncated;
    private long executionMs;

    public List<QueryColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<QueryColumn> columns) {
        this.columns = columns;
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }

    public void setRows(List<Map<String, Object>> rows) {
        this.rows = rows;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public boolean isTruncated() {
        return truncated;
    }

    public void setTruncated(boolean truncated) {
        this.truncated = truncated;
    }

    public long getExecutionMs() {
        return executionMs;
    }

    public void setExecutionMs(long executionMs) {
        this.executionMs = executionMs;
    }

    public Map<String, Object> toSummaryMap() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("columns", columns);
        summary.put("rows", rows);
        summary.put("rowCount", rowCount);
        summary.put("truncated", truncated);
        summary.put("executionMs", executionMs);
        return summary;
    }
}
