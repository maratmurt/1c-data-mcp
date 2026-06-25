package com.onec.datamcp.integration.dto;

public class PingResult {

    private final boolean reachable;
    private final PingResponse response;
    private final String error;

    private PingResult(boolean reachable, PingResponse response, String error) {
        this.reachable = reachable;
        this.response = response;
        this.error = error;
    }

    public static PingResult success(PingResponse response) {
        return new PingResult(true, response, null);
    }

    public static PingResult failure(String error) {
        return new PingResult(false, null, error);
    }

    public boolean isReachable() {
        return reachable;
    }

    public PingResponse getResponse() {
        return response;
    }

    public String getError() {
        return error;
    }
}
