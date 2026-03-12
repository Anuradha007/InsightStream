package com.insightstream.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
    GlobalParameters.class,
    TeamConfigurationProperties.class,
    ChromaProperties.class,
    HybridSearchProperties.class
})
public class AppConfig {
}
