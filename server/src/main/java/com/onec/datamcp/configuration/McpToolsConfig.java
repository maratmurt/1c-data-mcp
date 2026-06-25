package com.onec.datamcp.configuration;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.onec.datamcp.mcp.DataMcpTools;

@Configuration
public class McpToolsConfig {

    @Bean
    public ToolCallbackProvider dataMcpToolCallbacks(DataMcpTools dataMcpTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(dataMcpTools)
                .build();
    }
}
