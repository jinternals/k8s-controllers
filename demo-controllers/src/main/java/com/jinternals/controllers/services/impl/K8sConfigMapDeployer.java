package com.jinternals.controllers.services.impl;

import com.jinternals.controllers.services.K8sDeployer;
import com.jinternals.models.V1DevPortal;
import io.kubernetes.client.extended.controller.reconciler.Request;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformer;
import io.kubernetes.client.informer.cache.Lister;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1Service;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.jinternals.controllers.constants.ControllerConstants.CONFIG_MAP_NAME_PATTERN;
import static com.jinternals.controllers.utils.DevPortalUtils.addOwnerReference;
import static com.jinternals.controllers.utils.ResourceUtils.createOrUpdate;
import static com.jinternals.controllers.utils.YamlUtils.loadYamlAs;
import static java.lang.String.format;
import static java.util.Objects.nonNull;


@Slf4j
@Component
public class K8sConfigMapDeployer implements K8sDeployer<V1ConfigMap> {
    private Resource configMapYaml;
    private CoreV1Api coreV1Api;
    private SharedInformer<V1Service> serviceSharedInformer;
    private Lister<V1Service> serviceLister;
    private Yaml yaml = new Yaml();

    public K8sConfigMapDeployer(@Value("classpath:k8s/configmap.yaml") Resource configMapYaml,
                                CoreV1Api coreV1Api,
                                SharedIndexInformer<V1Service> serviceSharedInformer) {
        this.configMapYaml = configMapYaml;
        this.coreV1Api = coreV1Api;
        this.serviceSharedInformer = serviceSharedInformer;
        this.serviceLister = new Lister<>(serviceSharedInformer.getIndexer(), "");

    }

    @SneakyThrows
    public V1ConfigMap deploy(Request request, V1DevPortal devPortal) {

            String configMapName = format(CONFIG_MAP_NAME_PATTERN, devPortal.getMetadata().getName());
            String namespace = request.getNamespace();
            String pretty = "true";
            String dryRun = null;
            String fieldManager = "";
            String fieldValidation = "";

            V1ConfigMap configMap = loadYamlAs(configMapYaml, V1ConfigMap.class);
            configMap.getMetadata().setName(configMapName);
            configMap.getMetadata().setNamespace(namespace);
            configMap.setData( updateData(devPortal, namespace));

            log.info("get all the services in namespace " + namespace);

            servicesUrls(namespace);


            return createOrUpdate(V1ConfigMap.class, () -> {
                addOwnerReference(request.getName(), devPortal, configMap);
                return coreV1Api.createNamespacedConfigMap(namespace, configMap, pretty, dryRun, fieldManager,
                        fieldValidation);
            }, () -> {
                V1ConfigMap v1ConfigMap = coreV1Api.replaceNamespacedConfigMap(configMapName, namespace, configMap,
                        pretty, dryRun, fieldManager, fieldValidation);

                return v1ConfigMap;
            });

    }

    @SneakyThrows
    private Map<String, String> updateData(V1DevPortal v1DevPortal, String namespace) {
        Map<String, String> data = v1DevPortal.getSpec().getData();
        String replaceUrlsIn = v1DevPortal.getSpec().getReplaceUrlsIn();
        String yamlString = data.get(replaceUrlsIn);
        Map<String, Object> yamMap = yaml.load(yamlString);
        yamMap.put("pro", Map.of("dem",servicesUrls(namespace)));
        Map<String, String> newMap = new HashMap<>(data);
        newMap.put(replaceUrlsIn, yaml.dumpAsMap(yamMap));
        return newMap;
    }

    private List<String> servicesUrls(String namespace) {
         return serviceLister.namespace(namespace).list().stream()
                .filter(K8sConfigMapDeployer::isExposableToDevPortal)
                .map(service -> service.getMetadata().getName())
                .collect(Collectors.toList());
    }

    private static boolean isExposableToDevPortal(V1Service service) {
        Map<String, String> annotations = service.getMetadata().getAnnotations();
        if (isDevPortalAnnotationEnabled(annotations)) {
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
