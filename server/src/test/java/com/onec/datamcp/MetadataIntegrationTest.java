package com.onec.datamcp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.onec.datamcp.integration.dto.MetadataSummary;
import com.onec.datamcp.integration.dto.ObjectDescription;
import com.onec.datamcp.integration.dto.ObjectRef;
import com.onec.datamcp.service.MetadataService;

@SpringBootTest
class MetadataIntegrationTest {

    @Autowired
    private MetadataService metadataService;

    @Test
    void metadataSummaryReturnsUtCounts() {
        MetadataSummary summary = metadataService.getSummary("ut");
        assertThat(summary.getConfiguration()).isEqualTo("УправлениеТорговлей");
        assertThat(summary.getCounts()).containsKey("Catalog");
        assertThat(summary.getCounts().get("Catalog")).isPositive();
    }

    @Test
    void findObjectsMatchesNomenclature() {
        var first = metadataService.findObjects("ut", "номенклатур", null, 20);
        assertThat(first.stream().map(ObjectRef::getFullName))
                .anyMatch(name -> name.equals("Catalog.Номенклатура"));

        var second = metadataService.findObjects("ut", "номенклатур", null, 20);
        assertThat(second).isEqualTo(first);
    }

    @Test
    void describeObjectReturnsAttributes() {
        ObjectDescription description = metadataService.describeObject("ut", "Catalog.Номенклатура");
        assertThat(description.getFullName()).isEqualTo("Catalog.Номенклатура");
        assertThat(description.getAttributes()).isNotEmpty();
    }
}
