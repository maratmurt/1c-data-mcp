package com.onec.datamcp.security;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.onec.datamcp.configuration.DataMcpProperties;
import com.onec.datamcp.configuration.QueryProperties;

@Component
public class QueryGuard {

    private static final List<String> FORBIDDEN_TOKENS = List.of(
            "РАЗРЕШИТЬ",
            "ВНЕШНИЕ",
            "ИЗМЕНИТЬ",
            "УДАЛИТЬ",
            "ВСТАВИТЬ",
            "ОБНОВИТЬ");

    private final DataMcpProperties dataMcpProperties;

    public QueryGuard(DataMcpProperties dataMcpProperties) {
        this.dataMcpProperties = dataMcpProperties;
    }

    public void validate(String queryText) {
        QueryProperties queryProperties = dataMcpProperties.getQuery();
        if (queryText == null || queryText.isBlank()) {
            throw new IllegalArgumentException("Query text is empty");
        }

        if (queryText.length() > queryProperties.getMaxLength()) {
            throw new IllegalArgumentException(
                    "Query text exceeds max length: " + queryProperties.getMaxLength());
        }

        String trimmed = queryText.strip();
        if (!trimmed.toUpperCase(Locale.ROOT).startsWith("ВЫБРАТЬ")) {
            throw new IllegalArgumentException("Query must start with ВЫБРАТЬ");
        }

        String upper = queryText.toUpperCase(Locale.ROOT);
        for (String token : FORBIDDEN_TOKENS) {
            if (upper.contains(token)) {
                throw new IllegalArgumentException("Forbidden token in query: " + token);
            }
        }
    }
}
