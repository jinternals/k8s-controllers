package com.jinternals.controllers.utils;

import com.jinternals.controllers.services.ApiSupplier;
import io.kubernetes.client.openapi.ApiException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResourceUtils {

    private ResourceUtils(){
        throw new RuntimeException("private constructor");
    }

    public static <T> T createOrUpdate(Class<T> clazz, ApiSupplier<T> creator, ApiSupplier<T> updater) {
        try {
            log.info("Creating a new " + clazz.getName() + "!");
            T t = creator.get();
            log.info("Created a new " + clazz.getName() + "!");
            return t;
        } //
        catch (ApiException throwable) {
            int code = throwable.getCode();
            if (isResourceAlreadyExist(code)) {
                log.info("the " + clazz.getName() + " already exists. Replacing.");
                try {
                    T t = updater.get();
                    log.info("Successfully updated the " + clazz.getName());
                    return t;
                } catch (ApiException ex) {
                    log.error("Got an error on update", ex);
                }
            } //
            else {
                log.info("Got an exception with code " + code + " while trying to create the " + clazz.getName());
            }
        }
        return null;
    }


    private static boolean isResourceAlreadyExist(int code) {
        return code == 409;
    }



}
