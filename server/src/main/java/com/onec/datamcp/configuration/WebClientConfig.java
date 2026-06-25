package com.onec.datamcp.configuration;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    private static final String PING_PATH = "/hs/datamcp/v1/ping";

    @Bean
    public Map<String, WebClient> oneCWebClients(DataMcpProperties properties) {
        Map<String, WebClient> clients = new HashMap<>();
        for (ConnectionProperties connection : properties.getConnections()) {
            clients.put(connection.getName(), createClient(connection));
        }
        return clients;
    }

    public static String pingPath() {
        return PING_PATH;
    }

    private WebClient createClient(ConnectionProperties connection) {
        String baseUrl = normalizeBaseUrl(connection.getUrl());
        WebClient.Builder builder = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, "application/json");

        if (hasCredentials(connection)) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, basicAuthHeader(
                    connection.getUsername(),
                    connection.getPassword()));
        }

        builder.filter(ExchangeFilterFunction.ofRequestProcessor(request ->
                reactor.core.publisher.Mono.just(request)));

        return builder.build();
    }

    private static String normalizeBaseUrl(String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }

    private static boolean hasCredentials(ConnectionProperties connection) {
        return connection.getUsername() != null
                && !connection.getUsername().isBlank();
    }

    private static String basicAuthHeader(String username, String password) {
        String credentials = username + ":" + (password == null ? "" : password);
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }
}
