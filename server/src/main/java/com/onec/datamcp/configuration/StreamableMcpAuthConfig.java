package com.onec.datamcp.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.onec.datamcp.security.McpBearerAuthFilter;

@Configuration
@Profile("streamable")
@EnableConfigurationProperties(McpBearerAuthProperties.class)
public class StreamableMcpAuthConfig {

    public StreamableMcpAuthConfig(McpBearerAuthProperties properties) {
        String token = properties.getToken();
        if (token == null || token.isBlank()) {
            throw new IllegalStateException(
                    "DATAMCP_TOKEN must be set for streamable profile (datamcp.mcp-auth.token)");
        }
    }

    @Bean
    McpBearerAuthFilter mcpBearerAuthFilter(McpBearerAuthProperties properties) {
        return new McpBearerAuthFilter(properties);
    }
}
