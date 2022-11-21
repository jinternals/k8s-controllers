package com.jinternals.controllers.services;

import io.kubernetes.client.openapi.ApiException;

@FunctionalInterface
public interface ApiSupplier<T> {

    T get() throws ApiException;

}
