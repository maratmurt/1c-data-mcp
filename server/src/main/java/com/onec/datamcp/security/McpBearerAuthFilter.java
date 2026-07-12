package com.onec.datamcp.security;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.onec.datamcp.configuration.McpBearerAuthProperties;

import reactor.core.publisher.Mono;

public class McpBearerAuthFilter implements WebFilter {

    private static final String MCP_PATH_PREFIX = "/mcp";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String MCP_SESSION_ID_HEADER = "Mcp-Session-Id";

    private final McpBearerAuthProperties authProperties;

    public McpBearerAuthFilter(McpBearerAuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (!path.startsWith(MCP_PATH_PREFIX)) {
            return chain.filter(exchange);
        }

        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String expectedToken = authProperties.getToken();
        if (authorization != null
                && authorization.startsWith(BEARER_PREFIX)
                && expectedToken.equals(authorization.substring(BEARER_PREFIX.length()))) {
            return chain.filter(exchange);
        }

        // Cursor sends Authorization on POST but not on GET SSE, even with Mcp-Session-Id.
        // Session IDs are issued only after authenticated initialize, so allow GET by session.
        if (HttpMethod.GET.equals(exchange.getRequest().getMethod())) {
            String sessionId = exchange.getRequest().getHeaders().getFirst(MCP_SESSION_ID_HEADER);
            if (sessionId != null && !sessionId.isBlank()) {
                return chain.filter(exchange);
            }
            exchange.getResponse().setStatusCode(HttpStatus.METHOD_NOT_ALLOWED);
            return exchange.getResponse().setComplete();
        }

        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
