package com.jinternals.controllers.services.impl;

import com.jinternals.controllers.services.K8sDeployer;
import com.jinternals.controllers.utils.ServiceUtils;
import com.jinternals.models.V1SwaggerPortal;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.cache.Lister;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1Service;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.jinternals.controllers.constants.ControllerConstants.CONFIG_MAP_NAME_PATTERN;
import static com.jinternals.controllers.utils.SwaggerPortalUtils.buildOwnerReference;
import static com.jinternals.controllers.utils.ResourceUtils.createOrUpdate;
import static com.jinternals.controllers.utils.YamlUtils.loadYamlAs;
import static java.lang.String.format;


@Slf4j
@Component
@Order(1)
public class K8sConfigMapDeployer implements K8sDeployer<V1ConfigMap> {
    private Resource configMapYaml;
    private CoreV1Api coreV1Api;
    private Lister<V1Service> serviceLister;
    private Yaml yaml = new Yaml();

    public K8sConfigMapDeployer(@Value("classpath:k8s/configmap.yaml") Resource configMapYaml,
                                CoreV1Api coreV1Api,
                                SharedIndexInformer<V1Service> serviceSharedInformer) {
        this.configMapYaml = configMapYaml;
        this.coreV1Api = coreV1Api;
        this.serviceLister = new Lister<>(serviceSharedInformer.getIndexer(), "");

    }

    @SneakyThrows
    public V1ConfigMap deploy(V1SwaggerPortal swaggerPortal) {

        String name = swaggerPortal.getMetadata().getName();
        String configMapName = format(CONFIG_MAP_NAME_PATTERN, name);
        String namespace = swaggerPortal.getMetadata().getNamespace();
        String pretty = "true";
        String dryRun = null;
        String fieldManager = "";
        String fieldValidation = "";

        V1ConfigMap configMap = loadYamlAs(configMapYaml, V1ConfigMap.class);
        configMap.getMetadata().setName(configMapName);
        configMap.getMetadata().setNamespace(namespace);
        configMap.setData(updateData(swaggerPortal, namespace));

        log.info("get all the services in namespace " + namespace);

        servicesUrls(namespace);


        return createOrUpdate(V1ConfigMap.class, () -> {
            configMap.getMetadata().addOwnerReferencesItem(buildOwnerReference(name, swaggerPortal));
            return coreV1Api.createNamespacedConfigMap(namespace, configMap, pretty, dryRun, fieldManager,
                    fieldValidation);
        }, () -> {
            V1ConfigMap v1ConfigMap = coreV1Api.replaceNamespacedConfigMap(configMapName, namespace, configMap,
                    pretty, dryRun, fieldManager, fieldValidation);

            return v1ConfigMap;
        });

    }

    @SneakyThrows
    private Map<String, String> updateData(V1SwaggerPortal swaggerPortal, String namespace) {
        Map<String, String> data = swaggerPortal.getSpec().getData();
        String replaceUrlsIn = swaggerPortal.getSpec().getReplaceUrlsIn();
        String yamlString = data.get(replaceUrlsIn);
        Map<String, Object> yamMap = yaml.load(yamlString);
        yamMap.put("pro", Map.of("dem", servicesUrls(namespace)));
        Map<String, String> newMap = new HashMap<>(data);
        newMap.put(replaceUrlsIn, yaml.dumpAsMap(yamMap));
        return newMap;
    }

    private List<String> servicesUrls(String namespace) {
        return serviceLister.namespace(namespace).list().stream()
                .filter(ServiceUtils::isExportableToSwaggerPortal)
                .map(service -> service.getMetadata().getName()+":"+service.getSpec().getPorts().stream().findFirst().get().getPort())
                .collect(Collectors.toList());
    }

}
