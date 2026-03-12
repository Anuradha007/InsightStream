package com.insightstream.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "insightstream.chroma")
public class ChromaProperties {

    private String baseUrl = "http://localhost:8000";
    private String collectionName = "insightstream_docs";
    /** Chroma v2 API: tenant (e.g. default_tenant for local server). */
    private String tenant = "default_tenant";
    /** Chroma v2 API: database (e.g. default_database for local server). */
    private String database = "default_database";

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getCollectionName() { return collectionName; }
    public void setCollectionName(String collectionName) { this.collectionName = collectionName; }
    public String getTenant() { return tenant; }
    public void setTenant(String tenant) { this.tenant = tenant; }
    public String getDatabase() { return database; }
    public void setDatabase(String database) { this.database = database; }
}
