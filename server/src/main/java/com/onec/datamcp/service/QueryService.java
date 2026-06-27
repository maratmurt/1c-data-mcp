package com.onec.datamcp.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.onec.datamcp.configuration.DataMcpProperties;
import com.onec.datamcp.integration.OneCClient;
import com.onec.datamcp.integration.dto.QueryRequest;
import com.onec.datamcp.integration.dto.QueryResult;
import com.onec.datamcp.security.AuditLogger;
import com.onec.datamcp.security.QueryGuard;

@Service
public class QueryService {

    private final QueryGuard queryGuard;
    private final AuditLogger auditLogger;
    private final OneCClient oneCClient;
    private final DataMcpProperties dataMcpProperties;

    public QueryService(
            QueryGuard queryGuard,
            AuditLogger auditLogger,
            OneCClient oneCClient,
            DataMcpProperties dataMcpProperties) {
        this.queryGuard = queryGuard;
        this.auditLogger = auditLogger;
        this.oneCClient = oneCClient;
        this.dataMcpProperties = dataMcpProperties;
    }

    public QueryResult executeQuery(String connectionName, String queryText, Map<String, Object> parameters) {
        queryGuard.validate(queryText);
        String queryHash = auditLogger.hashQuery(queryText);

        QueryRequest request = new QueryRequest();
        request.setQuery(queryText);
        if (parameters != null && !parameters.isEmpty()) {
            request.setParameters(parameters);
        }
        request.setMaxRows(dataMcpProperties.getQuery().getMaxRows());

        try {
            QueryResult result = oneCClient.executeQuery(connectionName, request);
            auditLogger.logQueryEvent(
                    connectionName,
                    queryHash,
                    "success",
                    result.getRowCount(),
                    result.getExecutionMs());
            return result;
        } catch (RuntimeException ex) {
            auditLogger.logQueryEvent(connectionName, queryHash, "error", null, null);
            throw ex;
        }
    }
}
