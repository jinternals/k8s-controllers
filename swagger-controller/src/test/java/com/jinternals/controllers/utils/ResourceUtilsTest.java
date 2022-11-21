package com.jinternals.controllers.utils;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Deployment;
import org.junit.jupiter.api.Test;

import static com.jinternals.controllers.utils.ResourceUtils.createOrUpdate;
import static org.assertj.core.api.Assertions.assertThat;

class ResourceUtilsTest {

    @Test
    void shouldExecuteCreateSuccessfully() {

        V1Deployment v1Deployment1 = new V1Deployment();
        v1Deployment1.setKind("x");
        V1Deployment v1Deployment2 = new V1Deployment();
        v1Deployment1.setKind("y");
        V1Deployment created = createOrUpdate(V1Deployment.class, () -> v1Deployment1, () -> v1Deployment2);

        assertThat(created)
                .isEqualTo(v1Deployment1)
                .isNotEqualTo(v1Deployment2);
    }


    @Test
    void shouldExecuteAndSuccessfully() {

        V1Deployment created = new V1Deployment();
        created.setKind("x");
        V1Deployment updated = new V1Deployment();
        updated.setKind("y");
        V1Deployment v1Deployment = createOrUpdate(V1Deployment.class, () -> {
            throw new ApiException(409, "some-message");
        }, () -> updated);

        assertThat(v1Deployment)
                .isEqualTo(updated)
                .isNotEqualTo(created);
    }

    @Test
    void shouldExecuteAndReturnNullIfCreateFailedForUnknownReason() {

        V1Deployment created = new V1Deployment();
        created.setKind("x");
        V1Deployment updated = new V1Deployment();
        updated.setKind("y");
        V1Deployment v1Deployment = createOrUpdate(V1Deployment.class, () -> {
            throw new ApiException(429, "some-message");
        }, () -> updated);

        assertThat(v1Deployment).isNull();

    }

    @Test
    void shouldExecuteAndReturnNullIfCreateAndUpdatedFailedForUnknownReason() {

        V1Deployment created = new V1Deployment();
        created.setKind("x");
        V1Deployment updated = new V1Deployment();
        updated.setKind("y");
        V1Deployment v1Deployment = createOrUpdate(V1Deployment.class, () -> {
            throw new ApiException(409, "some-message");
        }, () -> {
            throw new ApiException(429, "some-message");
        });

        assertThat(v1Deployment).isNull();

    }
}
