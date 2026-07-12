package com.onec.datamcp.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import com.onec.datamcp.configuration.McpBearerAuthProperties;

import reactor.core.publisher.Mono;

class McpBearerAuthFilterTest {

    private static final String TOKEN = "secret-token";

    @Test
    void allowsValidBearerTokenOnMcpPath() {
        McpBearerAuthFilter filter = filterWithToken(TOKEN);
        MockServerWebExchange exchange = exchangeWithAuth("Bearer " + TOKEN, "/mcp", HttpMethod.POST);
        boolean[] continued = {false};

        filter.filter(exchange, continueChain(continued)).block();

        assertThat(continued[0]).isTrue();
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void rejectsMissingAuthorizationOnPost() {
        McpBearerAuthFilter filter = filterWithToken(TOKEN);
        MockServerWebExchange exchange = exchangeWithAuth(null, "/mcp", HttpMethod.POST);

        filter.filter(exchange, continueChain(new boolean[1])).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void returnsMethodNotAllowedForGetWithoutAuthorizationOrSession() {
        McpBearerAuthFilter filter = filterWithToken(TOKEN);
        MockServerWebExchange exchange = exchangeWithAuth(null, "/mcp", HttpMethod.GET);

        filter.filter(exchange, continueChain(new boolean[1])).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Test
    void allowsGetWithSessionIdButNoAuthorization() {
        McpBearerAuthFilter filter = filterWithToken(TOKEN);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/mcp").header("Mcp-Session-Id", "existing-session").build());
        boolean[] continued = {false};

        filter.filter(exchange, continueChain(continued)).block();

        assertThat(continued[0]).isTrue();
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void allowsGetWithValidBearerToken() {
        McpBearerAuthFilter filter = filterWithToken(TOKEN);
        MockServerWebExchange exchange = exchangeWithAuth("Bearer " + TOKEN, "/mcp", HttpMethod.GET);
        boolean[] continued = {false};

        filter.filter(exchange, continueChain(continued)).block();

        assertThat(continued[0]).isTrue();
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void rejectsInvalidBearerToken() {
        McpBearerAuthFilter filter = filterWithToken(TOKEN);
        MockServerWebExchange exchange = exchangeWithAuth("Bearer wrong", "/mcp", HttpMethod.POST);

        filter.filter(exchange, continueChain(new boolean[1])).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void skipsNonMcpPaths() {
        McpBearerAuthFilter filter = filterWithToken(TOKEN);
        MockServerWebExchange exchange = exchangeWithAuth(null, "/health", HttpMethod.GET);
        boolean[] continued = {false};

        filter.filter(exchange, continueChain(continued)).block();

        assertThat(continued[0]).isTrue();
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    private static McpBearerAuthFilter filterWithToken(String token) {
        McpBearerAuthProperties properties = new McpBearerAuthProperties();
        properties.setToken(token);
        return new McpBearerAuthFilter(properties);
    }

    private static MockServerWebExchange exchangeWithAuth(String authorization, String path, HttpMethod method) {
        MockServerHttpRequest.BaseBuilder<?> builder =
                method == HttpMethod.GET ? MockServerHttpRequest.get(path) : MockServerHttpRequest.post(path);
        if (authorization != null) {
            builder.header(HttpHeaders.AUTHORIZATION, authorization);
        }
        return MockServerWebExchange.from(builder.build());
    }

    private static WebFilterChain continueChain(boolean[] continued) {
        return exchange -> {
            continued[0] = true;
            return Mono.empty();
        };
    }
}
