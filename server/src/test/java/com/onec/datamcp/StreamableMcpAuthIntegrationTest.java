package com.onec.datamcp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles({"streamable", "test"})
@TestPropertySource(properties = {
        "datamcp.mcp-auth.token=integration-test-token",
        "spring.ai.mcp.server.enabled=true"
})
class StreamableMcpAuthIntegrationTest {

    private static final String MCP_ENDPOINT = "/mcp";

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void mcpEndpointRequiresBearerToken() {
        webTestClient
                .post()
                .uri(MCP_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"jsonrpc":"2.0","id":1,"method":"initialize","params":{}}
                        """)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void mcpGetWithSessionIdButNoBearerIsAllowed() {
        webTestClient
                .get()
                .uri(MCP_ENDPOINT)
                .header("Mcp-Session-Id", "any-session-id")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus()
                .is4xxClientError(); // session unknown to server, but auth filter passes through
    }

    @Test
    void mcpGetWithoutBearerReturnsMethodNotAllowed() {
        webTestClient
                .get()
                .uri(MCP_ENDPOINT)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus()
                .isEqualTo(405);
    }

    @Test
    void mcpEndpointAcceptsBearerToken() {
        webTestClient
                .post()
                .uri(MCP_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, "Bearer integration-test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM)
                .bodyValue("""
                        {
                          "jsonrpc": "2.0",
                          "id": 1,
                          "method": "initialize",
                          "params": {
                            "protocolVersion": "2025-11-25",
                            "capabilities": {},
                            "clientInfo": {"name": "test", "version": "1.0"}
                          }
                        }
                        """)
                .exchange()
                .expectStatus()
                .is2xxSuccessful();
    }
}
