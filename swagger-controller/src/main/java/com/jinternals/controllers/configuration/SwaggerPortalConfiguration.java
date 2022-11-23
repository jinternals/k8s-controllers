package com.jinternals.controllers.configuration;

import com.jinternals.controllers.reconcilers.SwaggerPortalReconciler;
import com.jinternals.controllers.utils.ServiceUtils;
import com.jinternals.models.V1SwaggerPortal;
import com.jinternals.models.V1SwaggerPortalList;
import io.kubernetes.client.extended.controller.Controller;
import io.kubernetes.client.extended.controller.builder.ControllerBuilder;
import io.kubernetes.client.extended.controller.builder.DefaultControllerBuilder;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1DeploymentList;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServiceList;
import io.kubernetes.client.util.generic.GenericKubernetesApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.Executors;

@Slf4j
@Configuration
public class SwaggerPortalConfiguration {


    @Bean
    ApplicationRunner runner(SharedInformerFactory sharedInformerFactory, Controller controller) {
        var executorService = Executors.newCachedThreadPool();
        return args -> executorService.execute(() -> {
            log.info("starting informers..");
            sharedInformerFactory.startAllRegisteredInformers();
            log.info("running controller..");
            controller.run();
        });
    }

    @Bean
    public Controller controller(
            SharedInformerFactory sharedInformerFactory, SwaggerPortalReconciler reconciler) {
        DefaultControllerBuilder builder = ControllerBuilder.defaultBuilder(sharedInformerFactory);

        builder =
                builder.watch(
                        (q) -> ControllerBuilder
                                .controllerWatchBuilder(V1SwaggerPortal.class, q)
                                .withResyncPeriod(Duration.ofMinutes(1))
                                .build());

        builder =
                builder.watch(
                        (q) -> ControllerBuilder
                                .controllerWatchBuilder(V1Service.class, q)
                                .withOnAddFilter(ServiceUtils::isExportableToSwaggerPortal)
                                .withResyncPeriod(Duration.ofMinutes(1))
                                .build());

        return builder.withWorkerCount(2)
                .withReadyFunc(reconciler::informerReady)
                .withReconciler(reconciler)
                .withName("swaggerPortalController")
                .build();
    }

    @Bean
    AppsV1Api appsV1Api(ApiClient apiClient) {
        return new AppsV1Api(apiClient);
    }

    @Bean
    CoreV1Api coreV1Api(ApiClient apiClient) {
        return new CoreV1Api(apiClient);
    }

    @Bean
    public SharedIndexInformer<V1Service> endpointsInformer(SharedInformerFactory sharedInformerFactory,
                                                            @Qualifier("serviceGenericKubernetesApi")
                                                            GenericKubernetesApi<V1Service, V1ServiceList> kubernetesApi) {
        return sharedInformerFactory.sharedIndexInformerFor(kubernetesApi, V1Service.class, 0);
    }

    @Bean
    public SharedIndexInformer<V1Deployment> deploymentInformer(SharedInformerFactory sharedInformerFactory,
                                                                @Qualifier("deploymentGenericKubernetesApi")
                                                                GenericKubernetesApi<V1Deployment, V1DeploymentList> kubernetesApi) {

        return sharedInformerFactory.sharedIndexInformerFor(kubernetesApi, V1Deployment.class, 0);
    }

    @Bean
    public SharedIndexInformer<V1SwaggerPortal> swaggerPortalInformer(SharedInformerFactory sharedInformerFactory,
                                                                      @Qualifier("swaggerPortalGenericKubernetesApi")
                                                                      GenericKubernetesApi<V1SwaggerPortal, V1SwaggerPortalList> kubernetesApi) {
        return sharedInformerFactory.sharedIndexInformerFor(kubernetesApi, V1SwaggerPortal.class, 0);
    }

    @Bean("deploymentGenericKubernetesApi")
    GenericKubernetesApi<V1Deployment, V1DeploymentList> deploymentGenericKubernetesApi(ApiClient apiClient) {
        return new GenericKubernetesApi<>(V1Deployment.class, V1DeploymentList.class, "apps", "v1", "deployments", apiClient);
    }

    @Bean("serviceGenericKubernetesApi")
    private static GenericKubernetesApi<V1Service, V1ServiceList> serviceGenericKubernetesApi(ApiClient apiClient) {
        return new GenericKubernetesApi<>(V1Service.class, V1ServiceList.class, "", "v1", "services", apiClient);
    }

    @Bean("swaggerPortalGenericKubernetesApi")
    private static GenericKubernetesApi<V1SwaggerPortal, V1SwaggerPortalList> swaggerPortalGenericKubernetesApi(ApiClient apiClient) {
        return new GenericKubernetesApi<>(V1SwaggerPortal.class, V1SwaggerPortalList.class, "jinternals.com", "v1", "swaggerportals", apiClient);
    }
}

