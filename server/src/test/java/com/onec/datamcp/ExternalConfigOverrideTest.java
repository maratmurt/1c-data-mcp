package com.onec.datamcp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.onec.datamcp.configuration.ConnectionProperties;
import com.onec.datamcp.configuration.DataMcpProperties;

@SpringBootTest
@ActiveProfiles("docker")
@TestPropertySource(properties = "spring.config.additional-location=optional:classpath:external-config/")
class ExternalConfigOverrideTest {

    @Autowired
    private DataMcpProperties properties;

    @Test
    void externalYamlOverridesConnections() {
        assertThat(properties.getDefaultConnection()).isEqualTo("smoke-test");
        assertThat(properties.getConnections())
                .extracting(ConnectionProperties::getName)
                .containsExactly("smoke-test");
        assertThat(properties.getConnections().get(0).getUrl())
                .isEqualTo("http://host.docker.internal:9999/datamcp-smoke");
    }
}
