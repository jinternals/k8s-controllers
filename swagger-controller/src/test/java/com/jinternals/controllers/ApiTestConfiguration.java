package com.jinternals.controllers;

import io.kubernetes.client.openapi.ApiClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class ApiTestConfiguration {

    @Bean
    public ApiClient apiClient(){
        return k3sTestContainerSetup.getApiClient();
    }
}
