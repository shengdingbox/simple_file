#!/usr/bin/env bash

DOCKER_REGISTRY="12.99.110.170:89"

docker build -t $DOCKER_REGISTRY/mc/storey-service:$BUILD_NUMBER .
docker tag $DOCKER_REGISTRY/mc/storey-service:$BUILD_NUMBER $DOCKER_REGISTRY/mc/storey-service:latest
docker push $DOCKER_REGISTRY/mc/storey-service:$BUILD_NUMBER
docker push $DOCKER_REGISTRY/mc/storey-service:latest