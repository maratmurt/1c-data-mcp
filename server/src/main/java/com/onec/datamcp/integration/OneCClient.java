package com.onec.datamcp.integration;

import java.time.Duration;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.onec.datamcp.configuration.WebClientConfig;
import com.onec.datamcp.integration.dto.PingResponse;
import com.onec.datamcp.integration.dto.PingResult;

@Component
public class OneCClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final Map<String, WebClient> webClients;

    public OneCClient(Map<String, WebClient> oneCWebClients) {
        this.webClients = oneCWebClients;
    }

    public PingResult ping(String connectionName) {
        WebClient client = webClients.get(connectionName);
        if (client == null) {
            return PingResult.failure("Connection not configured: " + connectionName);
        }

        try {
            PingResponse response = client.get()
                    .uri(WebClientConfig.pingPath())
                    .retrieve()
                    .bodyToMono(PingResponse.class)
                    .block(TIMEOUT);

            if (response == null) {
                return PingResult.failure("Empty response from 1C");
            }
            if (!"ok".equalsIgnoreCase(response.getStatus())) {
                return PingResult.failure("Unexpected status: " + response.getStatus());
            }
            return PingResult.success(response);
        } catch (WebClientResponseException.Unauthorized ex) {
            return PingResult.failure("Unauthorized (401): check ONEC_USER and ONEC_PASSWORD");
        } catch (WebClientResponseException ex) {
            return PingResult.failure("HTTP " + ex.getStatusCode().value() + ": " + ex.getStatusText());
        } catch (WebClientRequestException ex) {
            return PingResult.failure("Connection failed: " + ex.getMessage());
        } catch (Exception ex) {
            return PingResult.failure(ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
        }
    }
}
