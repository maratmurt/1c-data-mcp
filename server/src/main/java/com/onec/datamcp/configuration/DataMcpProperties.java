package com.onec.datamcp.configuration;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "datamcp")
public class DataMcpProperties {

    private String defaultConnection;
    private CacheProperties cache = new CacheProperties();
    private QueryProperties query = new QueryProperties();
    private List<ConnectionProperties> connections = new ArrayList<>();

    public String getDefaultConnection() {
        return defaultConnection;
    }

    public void setDefaultConnection(String defaultConnection) {
        this.defaultConnection = defaultConnection;
    }

    public CacheProperties getCache() {
        return cache;
    }

    public void setCache(CacheProperties cache) {
        this.cache = cache;
    }

    public QueryProperties getQuery() {
        return query;
    }

    public void setQuery(QueryProperties query) {
        this.query = query;
    }

    public List<ConnectionProperties> getConnections() {
        return connections;
    }

    public void setConnections(List<ConnectionProperties> connections) {
        this.connections = connections;
    }
}
