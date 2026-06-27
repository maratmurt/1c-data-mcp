package com.onec.datamcp.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.onec.datamcp.integration.dto.ObjectRef;

public class MetadataIndex {

    private final List<ObjectRef> items;
    private final Instant builtAt;

    public MetadataIndex(List<ObjectRef> items, Instant builtAt) {
        this.items = List.copyOf(items);
        this.builtAt = builtAt;
    }

    public Instant getBuiltAt() {
        return builtAt;
    }

    public List<ObjectRef> search(String query, List<String> types, int limit) {
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<ObjectRef> matches = new ArrayList<>();

        for (ObjectRef item : items) {
            if (types != null && !types.isEmpty() && !types.contains(item.getType())) {
                continue;
            }
            if (!normalizedQuery.isEmpty() && !matchesQuery(item, normalizedQuery)) {
                continue;
            }
            matches.add(item);
            if (matches.size() >= limit) {
                break;
            }
        }

        return matches;
    }

    private static boolean matchesQuery(ObjectRef item, String query) {
        return containsIgnoreCase(item.getName(), query)
                || containsIgnoreCase(item.getSynonym(), query);
    }

    private static boolean containsIgnoreCase(String value, String query) {
        if (value == null) {
            return false;
        }
        return value.toLowerCase(Locale.ROOT).contains(query);
    }
}
