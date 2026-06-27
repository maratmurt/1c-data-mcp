package com.onec.datamcp.integration;

import java.time.Duration;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.onec.datamcp.configuration.DataMcpProperties;
import com.onec.datamcp.configuration.WebClientConfig;
import com.onec.datamcp.integration.dto.ApiError;
import com.onec.datamcp.integration.dto.MetadataSummary;
import com.onec.datamcp.integration.dto.ObjectDescription;
import com.onec.datamcp.integration.dto.PagedList;
import com.onec.datamcp.integration.dto.PingResponse;
import com.onec.datamcp.integration.dto.PingResult;
import com.onec.datamcp.integration.dto.QueryRequest;
import com.onec.datamcp.integration.dto.QueryResult;

@Component
public class OneCClient {

    private static final Duration PING_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration METADATA_TIMEOUT = Duration.ofSeconds(120);

    private final Map<String, WebClient> webClients;
    private final DataMcpProperties dataMcpProperties;

    public OneCClient(Map<String, WebClient> oneCWebClients, DataMcpProperties dataMcpProperties) {
        this.webClients = oneCWebClients;
        this.dataMcpProperties = dataMcpProperties;
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
                    .block(PING_TIMEOUT);

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

    public MetadataSummary getMetadataSummary(String connectionName) {
        WebClient client = requireClient(connectionName);
        try {
            MetadataSummary response = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(WebClientConfig.metadataPath())
                            .queryParam("mode", "summary")
                            .build())
                    .retrieve()
                    .bodyToMono(MetadataSummary.class)
                    .block(METADATA_TIMEOUT);
            if (response == null) {
                throw new IllegalStateException("Empty response from 1C metadata summary");
            }
            return response;
        } catch (WebClientResponseException ex) {
            throw toApiException(ex);
        }
    }

    public PagedList getMetadataList(String connectionName, String types, int offset, int limit) {
        WebClient client = requireClient(connectionName);
        try {
            PagedList response = client.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder
                                .path(WebClientConfig.metadataPath())
                                .queryParam("mode", "list")
                                .queryParam("offset", offset)
                                .queryParam("limit", limit);
                        if (types != null && !types.isBlank()) {
                            builder.queryParam("types", types);
                        }
                        return builder.build();
                    })
                    .retrieve()
                    .bodyToMono(PagedList.class)
                    .block(METADATA_TIMEOUT);
            if (response == null) {
                throw new IllegalStateException("Empty response from 1C metadata list");
            }
            return response;
        } catch (WebClientResponseException ex) {
            throw toApiException(ex);
        }
    }

    public ObjectDescription describeObject(String connectionName, String type, String name) {
        WebClient client = requireClient(connectionName);
        try {
            ObjectDescription response = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(WebClientConfig.objectsDescribePath())
                            .build(type, name))
                    .retrieve()
                    .bodyToMono(ObjectDescription.class)
                    .block(METADATA_TIMEOUT);
            if (response == null) {
                throw new IllegalStateException("Empty response from 1C describe");
            }
            return response;
        } catch (WebClientResponseException.NotFound ex) {
            ApiError error = ex.getResponseBodyAs(ApiError.class);
            String message = error != null && error.getError() != null
                    ? error.getError()
                    : "Object not found: " + type + "." + name;
            throw new IllegalArgumentException(message);
        } catch (WebClientResponseException ex) {
            throw toApiException(ex);
        }
    }

    public QueryResult executeQuery(String connectionName, QueryRequest request) {
        WebClient client = requireClient(connectionName);
        Duration timeout = Duration.ofSeconds(dataMcpProperties.getQuery().getTimeoutSeconds());
        try {
            QueryResult response = client.post()
                    .uri(WebClientConfig.queryPath())
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(QueryResult.class)
                    .block(timeout);
            if (response == null) {
                throw new IllegalStateException("Empty response from 1C query");
            }
            return response;
        } catch (WebClientResponseException.BadRequest ex) {
            ApiError error = ex.getResponseBodyAs(ApiError.class);
            String message = error != null && error.getError() != null
                    ? error.getError()
                    : "Query validation failed";
            throw new IllegalArgumentException(message);
        } catch (WebClientResponseException ex) {
            throw toApiException(ex);
        } catch (WebClientRequestException ex) {
            throw new IllegalStateException("Query request failed: " + ex.getMessage(), ex);
        }
    }

    private WebClient requireClient(String connectionName) {
        WebClient client = webClients.get(connectionName);
        if (client == null) {
            throw new IllegalArgumentException("Connection not configured: " + connectionName);
        }
        return client;
    }

    private static RuntimeException toApiException(WebClientResponseException ex) {
        if (ex.getStatusCode().value() == 401) {
            return new IllegalStateException("Unauthorized (401): check ONEC_USER and ONEC_PASSWORD");
        }
        ApiError error = ex.getResponseBodyAs(ApiError.class);
        if (error != null && error.getError() != null) {
            return new IllegalStateException(error.getError());
        }
        return new IllegalStateException(
                "HTTP " + ex.getStatusCode().value() + ": " + ex.getStatusText());
    }
}
