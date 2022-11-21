package com.jinternals.controllers.reconcilers;

import com.jinternals.controllers.services.K8sDeployer;
import com.jinternals.models.V1SwaggerPortal;
import io.kubernetes.client.extended.controller.reconciler.Reconciler;
import io.kubernetes.client.extended.controller.reconciler.Request;
import io.kubernetes.client.extended.controller.reconciler.Result;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformer;
import io.kubernetes.client.informer.cache.Lister;
import io.kubernetes.client.openapi.models.V1Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class SwaggerPortalReconciler implements Reconciler {

    private List<K8sDeployer> k8sDeployers;
    private SharedInformer<V1Service> serviceSharedInformer;
    private SharedInformer<V1SwaggerPortal> swaggerPortalSharedIndexInformer;
    private Lister<V1SwaggerPortal> swaggerPortalLister;

    public SwaggerPortalReconciler(
            List<K8sDeployer> k8sDeployers,
            SharedIndexInformer<V1Service> serviceSharedInformer,
            SharedIndexInformer<V1SwaggerPortal> swaggerPortalSharedIndexInformer) {
        this.k8sDeployers = k8sDeployers;
        this.swaggerPortalSharedIndexInformer = swaggerPortalSharedIndexInformer;
        this.serviceSharedInformer = serviceSharedInformer;
        this.swaggerPortalLister = new Lister<>(swaggerPortalSharedIndexInformer.getIndexer(), "");
    }

    // If you want to hold the controller from running util some condition..
    public boolean informerReady() {
        return serviceSharedInformer.hasSynced() && swaggerPortalSharedIndexInformer.hasSynced();
    }

    @Override
    public Result reconcile(Request request) {
        try {

            swaggerPortalLister.namespace(request.getNamespace()).list()
                    .forEach(swaggerPortal -> {
                        k8sDeployers.forEach(v1DevPortalK8sDeployer -> {
                            log.info("Running {}", v1DevPortalK8sDeployer.getClass());
                            v1DevPortalK8sDeployer.deploy(swaggerPortal);
                        });
                    });

        } catch (Throwable e) {
            log.error("we've got an outer error.", e);
            return new Result(true, Duration.ofSeconds(60));
        }
        return new Result(false);
    }


}

