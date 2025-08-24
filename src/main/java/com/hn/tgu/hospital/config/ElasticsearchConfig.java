package com.hn.tgu.hospital.config;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.hn.tgu.hospital.elasticsearch")
public class ElasticsearchConfig extends AbstractElasticsearchConfiguration {

    @Value("${spring.data.elasticsearch.uris:http://localhost:9200}")
    private String elasticsearchUri;

    @Value("${spring.data.elasticsearch.username:}")
    private String username;

    @Value("${spring.data.elasticsearch.password:}")
    private String password;

    @Override
    @Bean
    public RestHighLevelClient elasticsearchClient() {
        String host = elasticsearchUri.replace("http://", "").replace("https://", "");
        String[] hostParts = host.split(":");
        
        ClientConfiguration.MaybeSecureClientConfigurationBuilder builder = ClientConfiguration.builder()
                .connectedTo(hostParts[0] + ":" + hostParts[1]);
        
        // Configurar autenticación si hay credenciales
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            builder.withBasicAuth(username, password);
        }
        
        // Configurar SSL si es HTTPS
        if (elasticsearchUri.startsWith("https://")) {
            builder.usingSsl();
        }
        
        ClientConfiguration clientConfiguration = builder.build();
        return RestClients.create(clientConfiguration).rest();
    }

    @Bean
    public ElasticsearchOperations elasticsearchTemplate() {
        return new ElasticsearchRestTemplate(elasticsearchClient());
    }
}
