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
        try {
            // Extraer solo el host y puerto de la URI
            String cleanUri = elasticsearchUri;
            if (cleanUri.contains("@")) {
                // Si tiene credenciales en la URI, extraer solo la parte del host
                cleanUri = cleanUri.substring(cleanUri.indexOf("@") + 1);
            }
            
            // Remover protocolo
            cleanUri = cleanUri.replace("http://", "").replace("https://", "");
            
            // Separar host y puerto
            String host;
            int port;
            if (cleanUri.contains(":")) {
                String[] parts = cleanUri.split(":");
                host = parts[0];
                port = Integer.parseInt(parts[1]);
            } else {
                host = cleanUri;
                port = 9200; // Puerto por defecto
            }
            
            ClientConfiguration.MaybeSecureClientConfigurationBuilder builder = ClientConfiguration.builder()
                    .connectedTo(host + ":" + port);
            
            // Configurar autenticación usando las propiedades separadas
            if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
                builder.withBasicAuth(username, password);
            }
            
            // Configurar SSL si es HTTPS
            if (elasticsearchUri.startsWith("https://")) {
                builder.usingSsl();
            }
            
            ClientConfiguration clientConfiguration = builder.build();
            return RestClients.create(clientConfiguration).rest();
            
        } catch (Exception e) {
            throw new RuntimeException("Error configurando cliente Elasticsearch: " + e.getMessage(), e);
        }
    }

    @Bean
    public ElasticsearchOperations elasticsearchTemplate() {
        return new ElasticsearchRestTemplate(elasticsearchClient());
    }
}
