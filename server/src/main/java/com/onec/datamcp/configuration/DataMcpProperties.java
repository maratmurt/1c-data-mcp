package com.onec.datamcp.configuration;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "datamcp")
public class DataMcpProperties {

    private String defaultConnection;
    private List<ConnectionProperties> connections = new ArrayList<>();

    public String getDefaultConnection() {
        return defaultConnection;
    }

    public void setDefaultConnection(String defaultConnection) {
        this.defaultConnection = defaultConnection;
    }

    public List<ConnectionProperties> getConnections() {
        return connections;
    }

    public void setConnections(List<ConnectionProperties> connections) {
        this.connections = connections;
    }
}
