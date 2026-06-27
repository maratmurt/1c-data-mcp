package com.onec.datamcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.onec.datamcp.mcp.DataMcpTools;
import com.onec.datamcp.service.ConnectionInfo;
import com.onec.datamcp.service.ConnectionService;

@SpringBootTest
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "ONEC_INTEGRATION", matches = "true")
class ConnectionIntegrationTest {

    @Autowired
    private ConnectionService connectionService;

    @Autowired
    private DataMcpTools dataMcpTools;

    @Test
    void listConnectionsReportsMixedReachability() {
        var connections = connectionService.listConnections();

        ConnectionInfo ut = findConnection(connections, "ut");
        ConnectionInfo unreachable = findConnection(connections, "unreachable");

        assertThat(ut.isDefault()).isTrue();
        assertThat(ut.isReachable()).isTrue();
        assertThat(ut.getConfiguration()).isEqualTo("УправлениеТорговлей");

        assertThat(unreachable.isDefault()).isFalse();
        assertThat(unreachable.isReachable()).isFalse();
        assertThat(unreachable.getError()).isNotBlank();
    }

    @Test
    void resolveConnectionRejectsUnknownBeforeHttp() {
        assertThatThrownBy(() -> connectionService.resolveConnection("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Connection not configured");
    }

    @Test
    void metadataToolReturnsErrorForUnknownConnection() throws Exception {
        String result = dataMcpTools.metadata("unknown");
        assertThat(result).contains("Connection not configured");
    }

    @Test
    void executeQueryUsesDefaultAfterExplicitConnection() throws Exception {
        String nomenclatureQuery = """
                ВЫБРАТЬ ПЕРВЫЕ 1
                    Номенклатура.Наименование
                ИЗ
                    Справочник.Номенклатура КАК Номенклатура
                """;

        assertThat(connectionService.resolveConnection("ut-copy")).isEqualTo("ut-copy");
        assertThat(connectionService.resolveConnection(null)).isEqualTo("ut");

        String result = dataMcpTools.executeQuery(nomenclatureQuery, null, null);
        assertThat(result).doesNotContain("\"error\"");
    }

    private static ConnectionInfo findConnection(java.util.List<ConnectionInfo> connections, String name) {
        return connections.stream()
                .filter(c -> name.equals(c.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Connection not found: " + name));
    }
}
