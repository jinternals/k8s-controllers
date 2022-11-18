package com.jinternals.controllers.services.impl;

import com.jinternals.controllers.constants.ControllerConstants;
import com.jinternals.controllers.services.K8sDeployer;
import com.jinternals.models.V1DevPortal;
import io.kubernetes.client.extended.controller.reconciler.Request;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.jinternals.controllers.constants.ControllerConstants.CONFIG_MAP_NAME_PATTERN;
import static com.jinternals.controllers.utils.DevPortalUtils.addOwnerReference;
import static com.jinternals.controllers.utils.ResourceUtils.createOrUpdate;
import static com.jinternals.controllers.utils.YamlUtils.loadYamlAs;
import static java.lang.String.format;
import static java.util.Objects.nonNull;

@Component
public class K8sConfigMapDeployer implements K8sDeployer<V1ConfigMap> {
    private Resource configMapYaml;
    private CoreV1Api coreV1Api;

    public K8sConfigMapDeployer( @Value("classpath:k8s/configmap.yaml")Resource configMapYaml, CoreV1Api coreV1Api) {
        this.configMapYaml = configMapYaml;
        this.coreV1Api = coreV1Api;
    }

    public V1ConfigMap deploy(Request request, V1DevPortal devPortal) {
        try {


        String configMapName = format(CONFIG_MAP_NAME_PATTERN, devPortal.getMetadata().getName());
        String namespace = request.getNamespace();
        String pretty = "true";
        String dryRun = null;
        String fieldManager = "";
        String fieldValidation = "";


        V1ConfigMap configMap = loadYamlAs(configMapYaml, V1ConfigMap.class);
        configMap.getMetadata().setName(configMapName);
        configMap.getMetadata().setNamespace(namespace);

        return createOrUpdate(V1ConfigMap.class, () -> {
            addOwnerReference(request.getName(), devPortal, configMap);
            return coreV1Api.createNamespacedConfigMap(namespace, configMap, pretty, dryRun, fieldManager,
                    fieldValidation);
        }, () -> {
            V1ConfigMap v1ConfigMap = coreV1Api.replaceNamespacedConfigMap(configMapName, namespace, configMap,
                    pretty, dryRun, fieldManager, fieldValidation);

            return v1ConfigMap;
        });
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static boolean isExposableToDevPortal(V1Service service) {
        Map<String, String> annotations = service.getMetadata().getAnnotations();
        if(isDevPortalAnnotationEnabled(annotations)){
            return true;
        }
        return false;
    }

    private static boolean isDevPortalAnnotationEnabled(Map<String, String> annotations) {
        return nonNull(annotations)
                && annotations.containsKey("dev-portal")
                && annotations.get("dev-portal").equals("true");
    }
}
