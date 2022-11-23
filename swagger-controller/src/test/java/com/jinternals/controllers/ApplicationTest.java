package com.jinternals.controllers;

import com.jinternals.controllers.utils.YamlUtils;
import com.jinternals.models.V1SwaggerPortal;
import com.jinternals.models.V1SwaggerPortalList;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.generic.GenericKubernetesApi;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(
        initializers = {
                k3sContextInitializer.class
        },
        classes = {
                Application.class
        }
)
@Import(ApiTestConfiguration.class)
class ApplicationTest {

    @Autowired
    private GenericKubernetesApi<V1SwaggerPortal, V1SwaggerPortalList> swaggerGenericKubernetesApi;
    @Autowired
    private GenericKubernetesApi<V1Service, V1ServiceList> serviceGenericKubernetesApi;
    @Autowired
    private AppsV1Api appsApi;
    @Autowired
    private CoreV1Api coreV1Api;

    @BeforeEach
    void  setup() {
        V1Namespace v1Namespace = YamlUtils.loadYamlAs(new ClassPathResource("namespace.yaml"), V1Namespace.class);

        try {
            coreV1Api.createNamespace(v1Namespace, null, null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void shouldCreateDeploymentAndConfigmap() throws Exception {
        V1SwaggerPortal v1SwaggerPortal = YamlUtils.loadYamlAs(new ClassPathResource("swagger-portal.yaml"), V1SwaggerPortal.class);

        swaggerGenericKubernetesApi.create(v1SwaggerPortal);

        String namespace = v1SwaggerPortal.getMetadata().getNamespace();

        await("deployment")
                .timeout(11, TimeUnit.SECONDS)
                .until(() -> !getDeployments(namespace).isEmpty());

        await("configmap")
                .timeout(11, TimeUnit.SECONDS)
                .until(() -> !getServices(namespace).isEmpty());


    }



    private List<V1Deployment> getDeployments(String namespace) throws ApiException {
        List<V1Deployment> deployments = appsApi
                .listNamespacedDeployment(namespace,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null).getItems();
        return deployments;
    }

    private List<V1ConfigMap> getServices(String namespace) throws ApiException {
        List<V1ConfigMap> services = coreV1Api.listNamespacedConfigMap(namespace,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null).getItems();
        return services;
    }
}
