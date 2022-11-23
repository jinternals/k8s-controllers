package com.jinternals.controllers;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesApi;
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesObject;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.k3s.K3sContainer;
import org.testcontainers.shaded.com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import static io.kubernetes.client.util.generic.dynamic.Dynamics.newFromYaml;
import static java.nio.charset.StandardCharsets.*;
import static org.testcontainers.utility.DockerImageName.parse;

public class k3sTestContainerSetup {
    private static final Logger k3S_LOGGER = LoggerFactory.getLogger("container.K3s");

    private static final K3sContainer k3sContainer = new K3sContainer(parse("rancher/k3s:v1.21.3-k3s1"));


    @SneakyThrows
    public static void initTestContainers(ConfigurableEnvironment configEnv) {
        if (k3sContainer.isRunning()) {
            return;
        }

        k3S_LOGGER.info("Stating k3s test container");

        k3sContainer.start();

        k3S_LOGGER.info("Applying CRD");

        applyCRD("k8s/crds", "crd.yaml");

        k3S_LOGGER.info("Applied CRD");

        //k3sContainer.followOutput(new Slf4jLogConsumer(k3S_LOGGER));
    }

    private static void applyCRD(String crdDIrPath, String crdFileName) throws IOException {
        var crdFile = new File(crdPath(crdDIrPath), crdFileName);
        String crdString = Files.toString(crdFile, UTF_8);
        DynamicKubernetesObject dynamicKubernetesObject = newFromYaml(crdString);
        DynamicKubernetesApi dynamicKubernetesApi = new DynamicKubernetesApi(
                "apiextensions.k8s.io",
                "v1",
                "customresourcedefinitions",
                getApiClient());
        dynamicKubernetesApi.create(dynamicKubernetesObject);
    }

    private static File crdPath(String crdDIrPath) {
        final ClassLoader classLoader = k3sTestContainerSetup.class.getClassLoader();
        final File file = new File(classLoader.getResource("application.yaml").getFile());
        final File projectDir = file.getParentFile().getParentFile().getParentFile().getParentFile();
        return new File(projectDir, crdDIrPath);
    }

    @SneakyThrows
    public static ApiClient getApiClient() {
        String kubeConfigYaml = k3sContainer.getKubeConfigYaml();

        ApiClient client = Config.fromConfig(new StringReader(kubeConfigYaml));

        return client;
    }

}
