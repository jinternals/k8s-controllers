#!/usr/bin/env bash

LOCAL_MANIFEST_FILE="$(pwd)/k8s/crds/crd.yaml"

rm -rf "$(pwd)/crds-models/src"

docker rm -f kind-control-plane || true

docker run \
  --rm \
  -v "$LOCAL_MANIFEST_FILE":"$LOCAL_MANIFEST_FILE" \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v "$(pwd)":"$(pwd)" \
  -ti \
  --network host \
 ghcr.io/kubernetes-client/java/crd-model-gen:v1.0.6 \
  /generate.sh \
  -u $LOCAL_MANIFEST_FILE \
  -n com.jinternals \
  -p com.jinternals \
  -o "$(pwd)/crd-models"

