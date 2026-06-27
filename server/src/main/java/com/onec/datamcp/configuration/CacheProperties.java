package com.onec.datamcp.configuration;

public class CacheProperties {

    private int metadataTtlMinutes = 30;

    public int getMetadataTtlMinutes() {
        return metadataTtlMinutes;
    }

    public void setMetadataTtlMinutes(int metadataTtlMinutes) {
        this.metadataTtlMinutes = metadataTtlMinutes;
    }
}
