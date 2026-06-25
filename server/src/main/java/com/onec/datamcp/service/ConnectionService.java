package com.onec.datamcp.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.onec.datamcp.configuration.ConnectionProperties;
import com.onec.datamcp.configuration.DataMcpProperties;
import com.onec.datamcp.integration.OneCClient;
import com.onec.datamcp.integration.dto.PingResponse;
import com.onec.datamcp.integration.dto.PingResult;

@Service
public class ConnectionService {

    private final DataMcpProperties properties;
    private final OneCClient oneCClient;

    public ConnectionService(DataMcpProperties properties, OneCClient oneCClient) {
        this.properties = properties;
        this.oneCClient = oneCClient;
    }

    public List<ConnectionInfo> listConnections() {
        List<ConnectionInfo> result = new ArrayList<>();
        String defaultName = properties.getDefaultConnection();

        for (ConnectionProperties connection : properties.getConnections()) {
            ConnectionInfo info = new ConnectionInfo();
            info.setName(connection.getName());
            info.setUrl(connection.getUrl());
            info.setDefault(connection.getName().equals(defaultName));

            PingResult pingResult = oneCClient.ping(connection.getName());
            info.setReachable(pingResult.isReachable());

            if (pingResult.isReachable()) {
                PingResponse response = pingResult.getResponse();
                info.setConfiguration(response.getConfiguration());
                info.setVersion(response.getVersion());
            } else {
                info.setError(pingResult.getError());
            }

            result.add(info);
        }

        return result;
    }
}
