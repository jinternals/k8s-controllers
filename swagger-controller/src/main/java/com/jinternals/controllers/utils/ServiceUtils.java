package com.jinternals.controllers.utils;

import io.kubernetes.client.openapi.models.V1Service;

import java.util.Map;

import static com.jinternals.controllers.constants.ControllerConstants.SWAGGER_ANNOTATION;
import static java.util.Objects.nonNull;

public class ServiceUtils {

    private ServiceUtils(){
        throw new RuntimeException("private constructor");
    }
    public static boolean isExportableToSwaggerPortal(V1Service service) {
        Map<String, String> annotations = service.getMetadata().getAnnotations();
        if (isSwaggerPortalAnnotationEnabled(annotations)) {
            return true;
        }
        return false;
    }

    private static boolean isSwaggerPortalAnnotationEnabled(Map<String, String> annotations) {
        return nonNull(annotations)
                && annotations.containsKey(SWAGGER_ANNOTATION)
                && annotations.get(SWAGGER_ANNOTATION).equals("true");
    }
}
