package com.onec.datamcp.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuditLogger {

    private static final Logger log = LoggerFactory.getLogger(AuditLogger.class);

    public String hashQuery(String queryText) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(queryText.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    public void logQueryEvent(
            String connection,
            String queryHash,
            String status,
            Integer rowCount,
            Long executionMs) {
        log.info(
                "query_audit timestamp={} connection={} queryHash={} status={} rowCount={} executionMs={}",
                Instant.now(),
                connection,
                queryHash,
                status,
                rowCount,
                executionMs);
    }
}
