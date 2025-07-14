package com.jeju_nongdi.jeju_nongdi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
public class WebClientConfig {
    
    @Value("${webclient.connection-timeout:10000}")
    private int connectionTimeout;
    
    @Value("${webclient.read-timeout:30000}")
    private int readTimeout;
    
    @Bean
    public WebClient.Builder webClientBuilder() {
        // Connection Pool 설정
        ConnectionProvider connectionProvider = ConnectionProvider.builder("custom")
                .maxConnections(100)
                .maxIdleTime(Duration.ofSeconds(20))
                .maxLifeTime(Duration.ofSeconds(60))
                .pendingAcquireTimeout(Duration.ofSeconds(60))
                .evictInBackground(Duration.ofSeconds(120))
                .build();
        
        // HttpClient 설정
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .responseTimeout(Duration.ofMillis(readTimeout))
                .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout);
        
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024)); // 1MB
    }
    
    @Bean
    public WebClient webClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder.build();
    }
}
