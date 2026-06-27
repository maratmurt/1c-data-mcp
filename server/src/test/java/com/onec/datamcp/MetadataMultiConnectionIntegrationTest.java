package com.onec.datamcp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.onec.datamcp.integration.dto.ObjectRef;
import com.onec.datamcp.service.MetadataService;

/**
 * Requires a second web publication of the same infobase:
 * {@code web-publish -AppName datamcp2 -Port 9090}
 * Set {@code ONEC_MULTI_CONNECTION=true} together with {@code ONEC_INTEGRATION=true}.
 */
@SpringBootTest
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "ONEC_INTEGRATION", matches = "true")
@EnabledIfEnvironmentVariable(named = "ONEC_MULTI_CONNECTION", matches = "true")
class MetadataMultiConnectionIntegrationTest {

    @Autowired
    private MetadataService metadataService;

    @Test
    void findObjectsWorksOnBothPublishedConnections() {
        var fromUt = metadataService.findObjects("ut", "номенклатур", null, 20);
        var fromCopy = metadataService.findObjects("ut-copy", "номенклатур", null, 20);

        assertThat(fromUt.stream().map(ObjectRef::getFullName)).contains("Catalog.Номенклатура");
        assertThat(fromCopy.stream().map(ObjectRef::getFullName)).contains("Catalog.Номенклатура");
    }
}
