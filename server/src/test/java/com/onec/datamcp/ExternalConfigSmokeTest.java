package com.onec.datamcp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.onec.datamcp.configuration.ConnectionProperties;
import com.onec.datamcp.configuration.DataMcpProperties;

@SpringBootTest
@ActiveProfiles("docker")
class ExternalConfigSmokeTest {

    @Autowired
    private DataMcpProperties properties;

    @Test
    void dockerProfileUsesBakedDefaultsWithoutExternalFile() {
        assertThat(properties.getConnections())
                .extracting(ConnectionProperties::getName)
                .containsExactly("ut");
    }
}
