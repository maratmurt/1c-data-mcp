package com.onec.datamcp.security;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.onec.datamcp.configuration.DataMcpProperties;
import com.onec.datamcp.configuration.QueryProperties;

class QueryGuardTest {

    private QueryGuard queryGuard;

    @BeforeEach
    void setUp() {
        QueryProperties query = new QueryProperties();
        query.setMaxLength(10000);
        query.setMaxRows(1000);
        query.setTimeoutSeconds(30);

        DataMcpProperties properties = new DataMcpProperties();
        properties.setQuery(query);
        queryGuard = new QueryGuard(properties);
    }

    @Test
    void rejectsOversizedQuery() {
        String query = "ВЫБРАТЬ ПЕРВЫЕ 1 " + "А".repeat(20000);
        assertThatThrownBy(() -> queryGuard.validate(query))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("max length");
    }
}
