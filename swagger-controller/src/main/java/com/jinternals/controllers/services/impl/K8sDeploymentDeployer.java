package com.jinternals.controllers.services.impl;

import com.jinternals.controllers.services.K8sDeployer;
import com.jinternals.models.V1SwaggerPortal;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.models.V1Deployment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import static com.jinternals.controllers.constants.ControllerConstants.DEPLOYMENT_NAME_PATTERN;
import static com.jinternals.controllers.utils.SwaggerPortalUtils.buildOwnerReference;
import static com.jinternals.controllers.utils.ResourceUtils.createOrUpdate;
import static com.jinternals.controllers.utils.YamlUtils.loadYamlAs;
import static java.lang.String.format;


@Slf4j
@Component
@Order(1)
public class K8sDeploymentDeployer implements K8sDeployer<V1Deployment> {
    private Resource deploymentYaml;
    private AppsV1Api appsV1Api;

    public K8sDeploymentDeployer(@Value("classpath:k8s/deployment.yaml") Resource deploymentYaml, AppsV1Api appsV1Api) {
        this.deploymentYaml = deploymentYaml;
        this.appsV1Api = appsV1Api;
    }

    public V1Deployment deploy(V1SwaggerPortal swaggerPortal) {
        try {

            String name = swaggerPortal.getMetadata().getName();
            String deploymentName = format(DEPLOYMENT_NAME_PATTERN, name);
            String namespace = swaggerPortal.getMetadata().getNamespace();
            Integer replicas = swaggerPortal.getSpec().getReplicas();
            String pretty = "true";
            String dryRun = null;
            String fieldManager = "";
            String fieldValidation = "";

            V1Deployment deployment = loadYamlAs(deploymentYaml, V1Deployment.class);
            deployment.getMetadata().setName(deploymentName);
            deployment.getMetadata().setNamespace(namespace);
            deployment.getSpec().setReplicas(replicas);


            return createOrUpdate(V1Deployment.class, () -> {
                deployment.getMetadata().addOwnerReferencesItem(buildOwnerReference(name, swaggerPortal));
                return appsV1Api.createNamespacedDeployment(namespace, deployment, pretty, dryRun, fieldManager,
                        fieldValidation);
            }, () -> {
                V1Deployment v1Deployment = appsV1Api.replaceNamespacedDeployment(
                        deploymentName,
                        namespace,
                        deployment,
                        pretty,
                        dryRun,
                        fieldManager,
                        fieldValidation);
                return v1Deployment;
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}
