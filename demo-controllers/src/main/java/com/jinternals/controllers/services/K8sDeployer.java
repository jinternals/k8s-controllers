package com.jinternals.controllers.services;

import com.jinternals.models.V1DevPortal;
import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.extended.controller.reconciler.Request;

public interface K8sDeployer<T extends KubernetesObject> {
    T deploy(Request request, V1DevPortal devPortal);
}
