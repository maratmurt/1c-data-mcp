package com.onec.datamcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.onec.datamcp.configuration.ConnectionProperties;
import com.onec.datamcp.configuration.DataMcpProperties;
import com.onec.datamcp.integration.OneCClient;
import com.onec.datamcp.service.ConnectionService;

@ExtendWith(MockitoExtension.class)
class ConnectionServiceTest {

    @Mock
    private OneCClient oneCClient;

    private DataMcpProperties properties;
    private ConnectionService connectionService;

    @BeforeEach
    void setUp() {
        properties = new DataMcpProperties();
        properties.setDefaultConnection("ut");

        ConnectionProperties ut = connection("ut", "http://localhost:8081/datamcp");
        ConnectionProperties utCopy = connection("ut-copy", "http://localhost:9090/datamcp2");
        properties.setConnections(List.of(ut, utCopy));

        connectionService = new ConnectionService(properties, oneCClient);
    }

    @Test
    void resolveConnectionReturnsExplicitName() {
        assertThat(connectionService.resolveConnection("ut")).isEqualTo("ut");
    }

    @Test
    void resolveConnectionReturnsDefaultWhenOmitted() {
        assertThat(connectionService.resolveConnection(null)).isEqualTo("ut");
        assertThat(connectionService.resolveConnection("")).isEqualTo("ut");
        assertThat(connectionService.resolveConnection("   ")).isEqualTo("ut");
    }

    @Test
    void resolveConnectionRejectsUnknownName() {
        assertThatThrownBy(() -> connectionService.resolveConnection("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Connection not configured: unknown");
    }

    @Test
    void resolveConnectionRejectsWhenDefaultNotSet() {
        properties.setDefaultConnection(null);
        connectionService = new ConnectionService(properties, oneCClient);

        assertThatThrownBy(() -> connectionService.resolveConnection(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No connection specified and default-connection is not set");
    }

    @Test
    void resolveConnectionDoesNotStickToPreviousExplicitName() {
        assertThat(connectionService.resolveConnection("ut-copy")).isEqualTo("ut-copy");
        assertThat(connectionService.resolveConnection(null)).isEqualTo("ut");
    }

    private static ConnectionProperties connection(String name, String url) {
        ConnectionProperties connection = new ConnectionProperties();
        connection.setName(name);
        connection.setUrl(url);
        connection.setUsername("test");
        connection.setPassword("test");
        return connection;
    }
}
