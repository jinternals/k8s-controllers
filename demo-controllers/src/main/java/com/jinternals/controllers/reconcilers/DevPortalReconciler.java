package com.jinternals.controllers.reconcilers;

import com.jinternals.controllers.services.impl.K8sConfigMapDeployer;
import com.jinternals.controllers.services.impl.K8sDeploymentDeployer;
import com.jinternals.models.V1DevPortal;
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

@Slf4j
@Component
public class DevPortalReconciler implements Reconciler {

    K8sDeploymentDeployer k8sDeploymentDeployer;
    K8sConfigMapDeployer k8sConfigMapDeployer;

    private SharedInformer<V1Service> serviceSharedInformer;
    private SharedInformer<V1DevPortal> devPortalSharedInformer;
    private Lister<V1Service> serviceLister;
    private Lister<V1DevPortal> devPortalLister;

    public DevPortalReconciler(
            SharedIndexInformer<V1Service> serviceSharedInformer,
            SharedIndexInformer<V1DevPortal> devPortalSharedInformer, K8sDeploymentDeployer k8sDeploymentDeployer, K8sConfigMapDeployer k8sConfigMapDeployer) {
        this.devPortalSharedInformer = devPortalSharedInformer;
        this.serviceSharedInformer = serviceSharedInformer;
        this.serviceLister = new Lister<>(serviceSharedInformer.getIndexer(), "");
        this.devPortalLister = new Lister<>(devPortalSharedInformer.getIndexer(), "");
        this.k8sDeploymentDeployer = k8sDeploymentDeployer;
        this.k8sConfigMapDeployer = k8sConfigMapDeployer;
    }

    // *OPTIONAL*
    // If you want to hold the controller from running util some condition..
    public boolean informerReady() {
        return serviceSharedInformer.hasSynced() && devPortalSharedInformer.hasSynced();
    }

    @Override
    public Result reconcile(Request request) {
        try {
            String key = request.getNamespace() + '/' + request.getName();
            V1DevPortal devPortal = devPortalLister.get(key);

            if (devPortal == null) { // deleted. we use ownerreferences so don't need to do anything special here
                return new Result(false);
            }

            k8sConfigMapDeployer.deploy(request, devPortal);
            k8sDeploymentDeployer.deploy(request, devPortal);


        } catch (Throwable e) {
            log.error("we've got an outer error.", e);
            return new Result(true, Duration.ofSeconds(60));
        }
        return new Result(false);
    }


}

