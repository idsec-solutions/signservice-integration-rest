#!/bin/bash

#
# Deployment script for the Sandbox environment
#

REDIS_PORT=6379
SIGNSERVICE_REST_HTTPS_PORT=9060
SIGNSERVICE_REST_AJP_PORT=9069

echo Pulling Redis docker image ...
docker pull redis

echo Undeploying Redis container ...
docker rm signservice-redis --force

echo Redeploying Redis docker container signservice-redis ...
docker run -d --name signservice-redis --restart=always \
  -p ${REDIS_PORT}:6379 \
  -v /opt/docker/signservice-integration-rest/redis:/data \
  redis

REDIS_CONTAINER_IP=`docker inspect -f "{{ .NetworkSettings.IPAddress }}" signservice-redis`

echo Redis container started - ${REDIS_CONTAINER_IP}

echo Pulling signservice-integration-rest docker image ...
docker pull docker.eidastest.se:5000/signservice-integration-rest

echo Undeploying signservice-integration-rest container ...
docker rm signservice-integration-rest --force

echo Redeploying docker container signservice-integration-rest ...
docker run -d --name signservice-integration-rest --restart=always \
  -p ${SIGNSERVICE_REST_HTTPS_PORT}:8443 \
  -p ${SIGNSERVICE_REST_AJP_PORT}:8009 \
  -e SERVER_SERVLET_CONTEXT_PATH=/signint \
  -e "SPRING_CONFIG_ADDITIONAL_LOCATION=/opt/signservice-integration-rest/" \
  -e SPRING_REDIS_ENABLED=true \
  -e SPRING_REDIS_HOST=${REDIS_CONTAINER_IP} \
  -e SPRING_REDIS_PORT=6379 \
  -v /etc/localtime:/etc/localtime:ro \
  -v /opt/docker/signservice-integration-rest:/opt/signservice-integration-rest \
  docker.eidastest.se:5000/signservice-integration-rest

echo Done!

