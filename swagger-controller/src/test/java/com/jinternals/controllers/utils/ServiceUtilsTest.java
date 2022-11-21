package com.jinternals.controllers.utils;

import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Service;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.jinternals.controllers.constants.ControllerConstants.SWAGGER_ANNOTATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceUtilsTest {


    @Test
    void shouldNotAllowObjectCreation() {
        assertThatThrownBy(() ->
        {
            Constructor<?> declaredConstructors = ServiceUtils.class
                    .getDeclaredConstructor();
            declaredConstructors.setAccessible(true);
            declaredConstructors.newInstance();
        }).isInstanceOf(InvocationTargetException.class);
    }

    @Test
    void shouldTestAnnotationIsPresentWithValueTrue() {
        boolean isAnnotationPresent = ServiceUtils.isExportableToSwaggerPortal(getV1Service("true"));
        assertThat(isAnnotationPresent).isTrue();
    }

    @Test
    void shouldTestAnnotationIsPresentWithValueFalse() {
        boolean isAnnotationPresent = ServiceUtils.isExportableToSwaggerPortal(getV1Service("false"));
        assertThat(isAnnotationPresent).isFalse();
    }

    @Test
    void shouldTestAnnotationIsNotPresent() {
        V1Service v1Service = new V1Service();
        v1Service.setMetadata(new V1ObjectMeta());
        boolean isAnnotationPresent = ServiceUtils.isExportableToSwaggerPortal(v1Service);
        assertThat(isAnnotationPresent).isFalse();
    }

    private static V1Service getV1Service(String value) {
        V1Service v1Service = new V1Service();
        v1Service.setMetadata(new V1ObjectMeta());
        v1Service.getMetadata().putAnnotationsItem(SWAGGER_ANNOTATION, value);
        return v1Service;
    }
}
