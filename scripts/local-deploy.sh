#!/bin/bash

#
# Deployment script for testing locally ...
#

REDIS_PORT=6379
SIGNSERVICE_REST_PORT=8543

echo Pulling Redis docker image ...
docker pull redis

echo Undeploying Redis container ...
docker rm signservice-redis --force

echo Redeploying Redis docker container signservice-redis ...
docker run -d --name signservice-redis --restart=always \
  -p ${REDIS_PORT}:6379 \
  redis

# Get the IP address of the redis container ...
REDIS_CONTAINER_IP=`docker inspect -f "{{ .NetworkSettings.IPAddress }}" signservice-redis`

echo Undeploying ...
docker rm signservice-integration-rest --force

echo Redeploying docker container signservice-integration-rest ...
docker run -d --name signservice-integration-rest --restart=always \
  -p ${SIGNSERVICE_REST_PORT}:8443 \
  -e SERVER_SERVLET_CONTEXT_PATH=/signint \
  -e SPRING_REDIS_ENABLED=true \
  -e SPRING_REDIS_HOST=${REDIS_CONTAINER_IP} \
  -e SPRING_REDIS_PORT=${REDIS_PORT} \
  docker.eidastest.se:5000/signservice-integration-rest

echo Done!

