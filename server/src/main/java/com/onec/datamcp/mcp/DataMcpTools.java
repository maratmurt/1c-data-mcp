package com.onec.datamcp.mcp;

import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onec.datamcp.service.ConnectionInfo;
import com.onec.datamcp.service.ConnectionService;

@Component
public class DataMcpTools {

    private final ConnectionService connectionService;
    private final ObjectMapper objectMapper;

    public DataMcpTools(ConnectionService connectionService, ObjectMapper objectMapper) {
        this.connectionService = connectionService;
        this.objectMapper = objectMapper;
    }

    @Tool(
            name = "list_connections",
            description = "List configured 1C database connections with reachability status")
    public String listConnections() throws JsonProcessingException {
        List<ConnectionInfo> connections = connectionService.listConnections();
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(connections);
    }
}
