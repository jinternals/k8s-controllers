package com.jinternals.controllers.utils;

import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.openapi.models.V1OwnerReference;

import static org.springframework.util.Assert.notNull;

public class SwaggerPortalUtils {

    private SwaggerPortalUtils() {
        throw new RuntimeException("private constructor");
    }

    public static V1OwnerReference buildOwnerReference(String requestName,
                                                       KubernetesObject fromKubernetesObject) {
        notNull(fromKubernetesObject, () -> "the fromKubernetesObject must not be null");

        return new V1OwnerReference()
                .kind(fromKubernetesObject.getKind())
                .apiVersion(fromKubernetesObject.getApiVersion())
                .controller(true)
                .uid(fromKubernetesObject.getMetadata().getUid())
                .name(requestName);
    }
}
