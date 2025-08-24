package com.hn.tgu.hospital.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.hn.tgu.hospital.elasticsearch")
public class ElasticsearchConfig {
    // Configuración mínima para habilitar repositorios Elasticsearch
}
