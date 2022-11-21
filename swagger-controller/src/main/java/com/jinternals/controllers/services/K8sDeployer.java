package com.jinternals.controllers.services;

import com.jinternals.models.V1SwaggerPortal;
import io.kubernetes.client.common.KubernetesObject;

public interface K8sDeployer<T extends KubernetesObject> {

    T deploy(V1SwaggerPortal swaggerPortal);

}
