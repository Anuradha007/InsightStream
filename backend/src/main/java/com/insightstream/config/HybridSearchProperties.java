package com.insightstream.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "insightstream.hybrid-search")
public class HybridSearchProperties {

    private int semanticTopK = 20;
    private int bm25TopK = 20;
    private int rrfK = 60;

    public int getSemanticTopK() { return semanticTopK; }
    public void setSemanticTopK(int semanticTopK) { this.semanticTopK = semanticTopK; }
    public int getBm25TopK() { return bm25TopK; }
    public void setBm25TopK(int bm25TopK) { this.bm25TopK = bm25TopK; }
    public int getRrfK() { return rrfK; }
    public void setRrfK(int rrfK) { this.rrfK = rrfK; }
}
