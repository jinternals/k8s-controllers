package com.jinternals.controllers.configuration;

import com.jinternals.controllers.reconcilers.DevPortalReconciler;
import com.jinternals.models.V1DevPortal;
import com.jinternals.models.V1DevPortalList;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class DevPortalConfiguration {

    @Bean
    public CommandLineRunner commandLineRunner(
            SharedInformerFactory sharedInformerFactory, Controller nodePrintingController) {
        return args -> {
            System.out.println("starting informers..");
            sharedInformerFactory.startAllRegisteredInformers();

            System.out.println("running controller..");
            nodePrintingController.run();
        };
    }

    @Bean
    public Controller devPortalController(
            SharedInformerFactory sharedInformerFactory, DevPortalReconciler reconciler) {
        DefaultControllerBuilder builder = ControllerBuilder.defaultBuilder(sharedInformerFactory);

        builder =
                builder.watch(
                        (q) -> ControllerBuilder
                                .controllerWatchBuilder(V1DevPortal.class, q)
                                .withResyncPeriod(Duration.ofMinutes(1))
                                .build());

        builder.withWorkerCount(2);
        builder.withReadyFunc(reconciler::informerReady);
        return builder.withReconciler(reconciler).withName("devPortalController").build();
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
                                                            @Qualifier("serviceGenericKubernetesApi") GenericKubernetesApi<V1Service, V1ServiceList> kubernetesApi) {

        return sharedInformerFactory.sharedIndexInformerFor(kubernetesApi, V1Service.class, 0);
    }

    @Bean
    public SharedIndexInformer<V1Deployment> deploymentInformer(SharedInformerFactory sharedInformerFactory,
                                                            @Qualifier("deploymentGenericKubernetesApi") GenericKubernetesApi<V1Deployment, V1DeploymentList> kubernetesApi) {

        return sharedInformerFactory.sharedIndexInformerFor(kubernetesApi, V1Deployment.class, 0);
    }

    @Bean
    public SharedIndexInformer<V1DevPortal> devPortalInformer(SharedInformerFactory sharedInformerFactory,
                                                              @Qualifier("devportalGenericKubernetesApi") GenericKubernetesApi<V1DevPortal, V1DevPortalList> kubernetesApi) {
        return sharedInformerFactory.sharedIndexInformerFor(kubernetesApi, V1DevPortal.class, 0);
    }

    @Bean("deploymentGenericKubernetesApi")
    GenericKubernetesApi<V1Deployment, V1DeploymentList> deploymentGenericKubernetesApi(ApiClient apiClient) {
        return new GenericKubernetesApi<>(
                V1Deployment.class,
                V1DeploymentList.class,
                "apps",
                "v1",
                "deployments",
                apiClient);
    }

    @Bean("serviceGenericKubernetesApi")
    private static GenericKubernetesApi<V1Service, V1ServiceList> serviceGenericKubernetesApi(ApiClient apiClient) {
        return new GenericKubernetesApi<>(
                V1Service.class,
                V1ServiceList.class,
                "",
                "v1",
                "services",
                apiClient);
    }

    @Bean("devportalGenericKubernetesApi")
    private static GenericKubernetesApi<V1DevPortal, V1DevPortalList> devportalGenericKubernetesApi(ApiClient apiClient) {
        return new GenericKubernetesApi<>(
                V1DevPortal.class,
                V1DevPortalList.class,
                "jinternals.com",
                "v1",
                "devportals",
                apiClient);
    }
}

