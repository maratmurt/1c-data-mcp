package com.onec.datamcp.mcp;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onec.datamcp.integration.dto.MetadataSummary;
import com.onec.datamcp.integration.dto.ObjectDescription;
import com.onec.datamcp.integration.dto.ObjectRef;
import com.onec.datamcp.service.ConnectionInfo;
import com.onec.datamcp.service.ConnectionService;
import com.onec.datamcp.service.MetadataService;

@Component
public class DataMcpTools {

    private final ConnectionService connectionService;
    private final MetadataService metadataService;
    private final ObjectMapper objectMapper;

    public DataMcpTools(
            ConnectionService connectionService,
            MetadataService metadataService,
            ObjectMapper objectMapper) {
        this.connectionService = connectionService;
        this.metadataService = metadataService;
        this.objectMapper = objectMapper;
    }

    @Tool(
            name = "list_connections",
            description = "List configured 1C database connections with reachability status")
    public String listConnections() throws JsonProcessingException {
        List<ConnectionInfo> connections = connectionService.listConnections();
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(connections);
    }

    @Tool(
            name = "metadata",
            description = "Get metadata summary for a 1C database: configuration name, version, object counts by type")
    public String metadata(
            @ToolParam(description = "Connection name (uses default if omitted)", required = false)
            String connection) throws JsonProcessingException {
        try {
            String resolved = connectionService.resolveConnection(connection);
            MetadataSummary summary = metadataService.getSummary(resolved);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(summary);
        } catch (Exception ex) {
            return toErrorJson(ex);
        }
    }

    @Tool(
            name = "find_objects",
            description = "Search metadata objects by substring in name and synonym")
    public String findObjects(
            @ToolParam(description = "Search query substring", required = false) String query,
            @ToolParam(description = "Connection name (uses default if omitted)", required = false)
            String connection,
            @ToolParam(description = "Comma-separated types: Catalog,Document,Enum,...", required = false)
            String types,
            @ToolParam(description = "Maximum results (default 20)", required = false) Integer limit)
            throws JsonProcessingException {
        try {
            String resolved = connectionService.resolveConnection(connection);
            int effectiveLimit = limit == null || limit <= 0 ? 20 : limit;
            List<ObjectRef> items = metadataService.findObjects(
                    resolved,
                    McpStringEncoding.normalize(query == null ? "" : query),
                    types,
                    effectiveLimit);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("items", items);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
        } catch (Exception ex) {
            return toErrorJson(ex);
        }
    }

    @Tool(
            name = "describe_object",
            description = "Get structural description of a metadata object, e.g. Catalog.Номенклатура")
    public String describeObject(
            @ToolParam(description = "Object full name: Type.Name") String object,
            @ToolParam(description = "Connection name (uses default if omitted)", required = false)
            String connection) throws JsonProcessingException {
        try {
            String resolved = connectionService.resolveConnection(connection);
            ObjectDescription description = metadataService.describeObject(
                    resolved, McpStringEncoding.normalize(object));
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(description);
        } catch (Exception ex) {
            return toErrorJson(ex);
        }
    }

    private String toErrorJson(Exception ex) throws JsonProcessingException {
        Map<String, String> error = Map.of("error", ex.getMessage() == null
                ? ex.getClass().getSimpleName()
                : ex.getMessage());
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(error);
    }
}
