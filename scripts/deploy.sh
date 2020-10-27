#!/bin/bash

#
# Deployment script for the Sandbox environment
#

echo Pulling Docker image ...
docker pull docker.eidastest.se:5000/signservice-integration-rest

echo Undeploying ...
docker rm signservice-integration-rest --force

echo Redeploying docker container signservice-integration-rest ...
docker run -d --name signservice-integration-rest --restart=always \
  -p 9050:8443 \
  -p 9056:8009 \
  -e "SPRING_CONFIG_ADDITIONAL_LOCATION=/opt/signservice-integration-rest/" \
  -v /etc/localtime:/etc/localtime:ro \
  -v /opt/docker/signservice-integration-rest:/opt/signservice-integration-rest \
  docker.eidastest.se:5000/signservice-integration-rest

echo Done!

