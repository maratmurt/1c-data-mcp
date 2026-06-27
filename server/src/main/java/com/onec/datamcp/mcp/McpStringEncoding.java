package com.onec.datamcp.mcp;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

final class McpStringEncoding {

    private static final Charset WINDOWS_1251 = Charset.forName("Windows-1251");

    private McpStringEncoding() {
    }

    /**
     * Repairs UTF-8 text misread as Windows-1251 on Windows STDIO MCP transports.
     */
    static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        if (!looksLikeUtf8MisreadAsCp1251(value)) {
            return value;
        }
        String repaired = new String(value.getBytes(WINDOWS_1251), StandardCharsets.UTF_8);
        if (repaired.contains("\uFFFD") || !containsNormalCyrillicWord(repaired)) {
            return value;
        }
        return repaired;
    }

    /**
     * UTF-8 Cyrillic misread as CP1251 yields pairs like Рџ (П), Р° (а), СЂ (р).
     */
    private static boolean looksLikeUtf8MisreadAsCp1251(String value) {
        int pairs = 0;
        for (int i = 0; i < value.length() - 1; i++) {
            char lead = value.charAt(i);
            if (lead != '\u0420' && lead != '\u0421') {
                continue;
            }
            char next = value.charAt(i + 1);
            if (next >= '\u0400' && next <= '\u04FF') {
                pairs++;
            }
        }
        return pairs >= 2;
    }

    private static boolean containsNormalCyrillicWord(String value) {
        int cyrillic = 0;
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch >= '\u0400' && ch <= '\u04FF' && ch != '\u0400') {
                cyrillic++;
            }
        }
        return cyrillic >= 3;
    }
}
