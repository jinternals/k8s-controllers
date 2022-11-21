package com.jinternals.controllers.utils;

import com.jinternals.models.V1SwaggerPortal;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1OwnerReference;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.jinternals.controllers.utils.SwaggerPortalUtils.buildOwnerReference;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SwaggerPortalUtilsTest {

    @Test
    void shouldNotAllowObjectCreation() {
        assertThatThrownBy(() ->
        {
            Constructor<?> declaredConstructors = SwaggerPortalUtils.class
                    .getDeclaredConstructor();
            declaredConstructors.setAccessible(true);
            declaredConstructors.newInstance();
        }).isInstanceOf(InvocationTargetException.class);
    }

    @Test
    void shouldGenerateOwnerReference() {
        V1OwnerReference ownerReference = buildOwnerReference("some-name", getV1SwaggerPortal("some-uuid", "v1", "SwaggerPortal"));

        assertThat(ownerReference).isNotNull();
        assertThat(ownerReference.getApiVersion()).isEqualTo("v1");
        assertThat(ownerReference.getKind()).isEqualTo("SwaggerPortal");
        assertThat(ownerReference.getController()).isTrue();
        assertThat(ownerReference.getName()).isEqualTo("some-name");
        assertThat(ownerReference.getUid()).isEqualTo("some-uuid");
    }

    @NotNull
    private static V1SwaggerPortal getV1SwaggerPortal(String uid, String version, String kind) {
        V1SwaggerPortal swaggerPortal = new V1SwaggerPortal();
        swaggerPortal.setMetadata(new V1ObjectMeta());
        swaggerPortal.getMetadata().setUid(uid);
        swaggerPortal.setApiVersion(version);
        swaggerPortal.setKind(kind);
        return swaggerPortal;
    }
}
