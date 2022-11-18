package com.jinternals.controllers.utils;

import com.jinternals.models.V1DevPortal;
import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1OwnerReference;
import org.springframework.util.Assert;

public class DevPortalUtils {

    public static V1ObjectMeta addOwnerReference(String requestName, V1DevPortal devPortal, KubernetesObject kubernetesObject) {
        Assert.notNull(devPortal, () -> "the devPortal must not be null");
        return kubernetesObject.getMetadata().addOwnerReferencesItem(new V1OwnerReference().kind(devPortal.getKind())
                .apiVersion(devPortal.getApiVersion()).controller(true).uid(devPortal.getMetadata().getUid()).name(requestName));
    }
}
