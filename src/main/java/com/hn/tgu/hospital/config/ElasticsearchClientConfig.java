package com.hn.tgu.hospital.config;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

@Configuration
public class ElasticsearchClientConfig {

    @Value("${spring.data.elasticsearch.uris}")
    private String elasticsearchUris;

    @Value("${spring.data.elasticsearch.username}")
    private String username;

    @Value("${spring.data.elasticsearch.password}")
    private String password;

    @Bean
    @Primary
    public ElasticsearchOperations elasticsearchTemplate() throws Exception {
        System.out.println("🔧 Configurando ElasticsearchRestTemplate para Bonsai...");
        System.out.println("URI: " + elasticsearchUris);
        System.out.println("Username: " + username);

        // Parsear la URI
        java.net.URI uri = new java.net.URI(elasticsearchUris);
        
        // Para Bonsai, el puerto es 443 (HTTPS)
        int port = uri.getPort() != -1 ? uri.getPort() : 443;
        String scheme = uri.getScheme() != null ? uri.getScheme() : "https";

        System.out.println("Host: " + uri.getHost());
        System.out.println("Port: " + port);
        System.out.println("Scheme: " + scheme);

        // Configurar credenciales
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
            AuthScope.ANY,
            new UsernamePasswordCredentials(username, password)
        );

        // Crear el RestHighLevelClient para Bonsai
        RestHighLevelClient client = new RestHighLevelClient(
            RestClient.builder(
                new HttpHost(uri.getHost(), port, scheme)
            )
            .setHttpClientConfigCallback(httpClientBuilder -> {
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                // Configurar SSL para Bonsai
                if ("https".equals(scheme)) {
                    httpClientBuilder.setSSLContext(org.apache.http.ssl.SSLContexts.createDefault());
                }
                return httpClientBuilder;
            })
        );

        System.out.println("✅ ElasticsearchRestTemplate configurado exitosamente para Bonsai");
        return new ElasticsearchRestTemplate(client);
    }

    // Mantener el bean anterior para compatibilidad
    @Bean
    public RestHighLevelClient elasticsearchClient() throws Exception {
        System.out.println("🔧 Configurando RestHighLevelClient para compatibilidad...");
        
        // Parsear la URI
        java.net.URI uri = new java.net.URI(elasticsearchUris);
        
        // Para Bonsai, el puerto es 443 (HTTPS)
        int port = uri.getPort() != -1 ? uri.getPort() : 443;
        String scheme = uri.getScheme() != null ? uri.getScheme() : "https";

        // Configurar credenciales
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
            AuthScope.ANY,
            new UsernamePasswordCredentials(username, password)
        );

        // Crear el RestHighLevelClient para Bonsai
        RestHighLevelClient client = new RestHighLevelClient(
            RestClient.builder(
                new HttpHost(uri.getHost(), port, scheme)
            )
            .setHttpClientConfigCallback(httpClientBuilder -> {
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                // Configurar SSL para Bonsai
                if ("https".equals(scheme)) {
                    httpClientBuilder.setSSLContext(org.apache.http.ssl.SSLContexts.createDefault());
                }
                return httpClientBuilder;
            })
        );

        System.out.println("✅ RestHighLevelClient configurado exitosamente para Bonsai");
        return client;
    }
}
