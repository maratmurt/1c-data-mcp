package com.onec.datamcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.onec.datamcp.configuration.CacheProperties;
import com.onec.datamcp.configuration.DataMcpProperties;
import com.onec.datamcp.integration.OneCClient;
import com.onec.datamcp.integration.dto.ObjectRef;
import com.onec.datamcp.integration.dto.PagedList;
import com.onec.datamcp.service.MetadataService;

@ExtendWith(MockitoExtension.class)
class MetadataServiceCacheTest {

    @Mock
    private OneCClient oneCClient;

    private MetadataService metadataService;

    @BeforeEach
    void setUp() {
        DataMcpProperties properties = new DataMcpProperties();
        CacheProperties cache = new CacheProperties();
        cache.setMetadataTtlMinutes(30);
        properties.setCache(cache);
        metadataService = new MetadataService(oneCClient, properties);
    }

    @Test
    void cacheIsolatedPerConnection() {
        PagedList page = nomenclaturePage();
        when(oneCClient.getMetadataList(eq("ut"), isNull(), eq(0), eq(500))).thenReturn(page);
        when(oneCClient.getMetadataList(eq("ut-copy"), isNull(), eq(0), eq(500))).thenReturn(page);

        metadataService.findObjects("ut", "номенклатур", null, 20);
        metadataService.findObjects("ut-copy", "номенклатур", null, 20);
        metadataService.findObjects("ut", "номенклатур", null, 20);

        verify(oneCClient, times(1)).getMetadataList(eq("ut"), isNull(), eq(0), eq(500));
        verify(oneCClient, times(1)).getMetadataList(eq("ut-copy"), isNull(), eq(0), eq(500));
    }

    @Test
    void findObjectsReturnsMatchesFromCachedIndex() {
        when(oneCClient.getMetadataList(eq("ut"), isNull(), eq(0), eq(500)))
                .thenReturn(nomenclaturePage());

        var matches = metadataService.findObjects("ut", "номенклатур", null, 20);

        assertThat(matches).extracting(ObjectRef::getFullName).contains("Catalog.Номенклатура");
    }

    private static PagedList nomenclaturePage() {
        ObjectRef ref = new ObjectRef();
        ref.setType("Catalog");
        ref.setName("Номенклатура");
        ref.setSynonym("Номенклатура");
        ref.setFullName("Catalog.Номенклатура");
        ref.setQueryName("Справочник.Номенклатура");

        PagedList page = new PagedList();
        page.setItems(List.of(ref));
        page.setTotal(1);
        page.setOffset(0);
        page.setLimit(500);
        return page;
    }
}
