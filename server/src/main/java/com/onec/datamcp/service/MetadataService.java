package com.onec.datamcp.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.onec.datamcp.configuration.DataMcpProperties;
import com.onec.datamcp.integration.OneCClient;
import com.onec.datamcp.integration.dto.MetadataSummary;
import com.onec.datamcp.integration.dto.ObjectDescription;
import com.onec.datamcp.integration.dto.ObjectRef;
import com.onec.datamcp.integration.dto.PagedList;

@Service
public class MetadataService {

    private static final int PAGE_SIZE = 500;

    private final OneCClient oneCClient;
    private final DataMcpProperties properties;
    private final Map<String, MetadataIndex> indexCache = new ConcurrentHashMap<>();

    public MetadataService(OneCClient oneCClient, DataMcpProperties properties) {
        this.oneCClient = oneCClient;
        this.properties = properties;
    }

    public MetadataSummary getSummary(String connectionName) {
        return oneCClient.getMetadataSummary(connectionName);
    }

    public List<ObjectRef> findObjects(String connectionName, String query, String types, int limit) {
        List<String> typeFilter = parseTypes(types);
        MetadataIndex index = getOrBuildIndex(connectionName);
        int effectiveLimit = limit > 0 ? limit : 20;
        return index.search(query, typeFilter, effectiveLimit);
    }

    public ObjectDescription describeObject(String connectionName, String object) {
        int dotIndex = object.indexOf('.');
        if (dotIndex <= 0 || dotIndex == object.length() - 1) {
            throw new IllegalArgumentException("Object must be in format Type.Name, e.g. Catalog.Номенклатура");
        }
        String type = object.substring(0, dotIndex);
        String name = object.substring(dotIndex + 1);
        return oneCClient.describeObject(connectionName, type, name);
    }

    private MetadataIndex getOrBuildIndex(String connectionName) {
        MetadataIndex cached = indexCache.get(connectionName);
        Duration ttl = Duration.ofMinutes(properties.getCache().getMetadataTtlMinutes());

        if (cached != null && cached.getBuiltAt().plus(ttl).isAfter(Instant.now())) {
            return cached;
        }

        List<ObjectRef> allItems = new ArrayList<>();
        int offset = 0;

        while (true) {
            PagedList page = oneCClient.getMetadataList(connectionName, null, offset, PAGE_SIZE);
            allItems.addAll(page.getItems());
            if (offset + PAGE_SIZE >= page.getTotal()) {
                break;
            }
            offset += PAGE_SIZE;
        }

        MetadataIndex index = new MetadataIndex(allItems, Instant.now());
        indexCache.put(connectionName, index);
        return index;
    }

    private static List<String> parseTypes(String types) {
        if (types == null || types.isBlank()) {
            return List.of();
        }
        return Arrays.stream(types.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
