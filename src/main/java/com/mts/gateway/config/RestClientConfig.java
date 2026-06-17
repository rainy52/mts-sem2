package com.mts.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${gateway.external-api.base-url}")
    private String baseUrl;

    @Value("${gateway.external-api.connect-timeout-ms}")
    private int connectTimeout;

    @Value("${gateway.external-api.read-timeout-ms}")
    private int readTimeout;

    @Bean
    public RestClient externalTasksRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("User-Agent", "MTS-Gateway")
                .requestFactory(requestFactory)
                .build();
    }
}
