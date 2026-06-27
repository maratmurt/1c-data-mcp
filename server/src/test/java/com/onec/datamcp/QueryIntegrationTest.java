package com.onec.datamcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.onec.datamcp.integration.dto.QueryResult;
import com.onec.datamcp.security.QueryGuard;
import com.onec.datamcp.service.QueryService;

@SpringBootTest
class QueryIntegrationTest {

    private static final String NOMENCLATURE_QUERY = """
            ВЫБРАТЬ ПЕРВЫЕ 10
                Номенклатура.Ссылка КАК Ссылка,
                Номенклатура.Наименование КАК Наименование
            ИЗ
                Справочник.Номенклатура КАК Номенклатура
            """;

    @Autowired
    private QueryService queryService;

    @Autowired
    private QueryGuard queryGuard;

    @Test
    void executeQueryReturnsNomenclatureRows() {
        QueryResult result = queryService.executeQuery("ut", NOMENCLATURE_QUERY, null);
        assertThat(result.getRowCount()).isPositive();
        assertThat(result.getRowCount()).isLessThanOrEqualTo(10);
        assertThat(result.getColumns()).isNotEmpty();
        assertThat(result.getRows()).isNotEmpty();
    }

    @Test
    void executeQueryWithoutFirstRejectedByGuard() {
        String query = """
                ВЫБРАТЬ
                    Номенклатура.Наименование
                ИЗ
                    Справочник.Номенклатура КАК Номенклатура
                """;
        assertThatThrownBy(() -> queryService.executeQuery("ut", query, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ПЕРВЫЕ");
    }

    @Test
    void queryGuardRejectsOversizedQuery() {
        String query = "ВЫБРАТЬ ПЕРВЫЕ 1 " + "А".repeat(20000);
        assertThatThrownBy(() -> queryGuard.validate(query))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("max length");
    }

    @Test
    void referenceFieldsIncludeUuidTypeAndPresentation() {
        QueryResult result = queryService.executeQuery("ut", NOMENCLATURE_QUERY, null);
        assertThat(result.getRows().get(0)).containsKey("Ссылка");
        Object ref = result.getRows().get(0).get("Ссылка");
        assertThat(ref).isInstanceOf(java.util.Map.class);
        @SuppressWarnings("unchecked")
        var refMap = (java.util.Map<String, Object>) ref;
        assertThat(refMap).containsKeys("uuid", "type", "presentation");
    }
}
