package com.onec.datamcp.mcp;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class McpStringEncodingTest {

    private static final Charset WINDOWS_1251 = Charset.forName("Windows-1251");

    @Test
    void repairsWindowsMojibake() {
        String broken = "Catalog."
                + new String("Партнеры".getBytes(StandardCharsets.UTF_8), WINDOWS_1251);
        assertThat(broken).isEqualTo("Catalog.РџР°СЂС‚РЅРµСЂС‹");
        assertThat(McpStringEncoding.normalize(broken)).isEqualTo("Catalog.Партнеры");
    }

    @Test
    void leavesCorrectUtf8Untouched() {
        assertThat(McpStringEncoding.normalize("Catalog.Партнеры")).isEqualTo("Catalog.Партнеры");
        assertThat(McpStringEncoding.normalize("Партнеры")).isEqualTo("Партнеры");
        assertThat(McpStringEncoding.normalize("Россия")).isEqualTo("Россия");
    }
}
