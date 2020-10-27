#!/bin/bash

#
# Deployment script for testing locally ...
#

#echo Pulling Docker image ...
#docker pull docker.eidastest.se:5000/signservice-integration-rest

echo Undeploying ...
docker rm signservice-integration-rest --force

echo Redeploying docker container signservice-integration-rest ...
docker run -d --name signservice-integration-rest --restart=always \
  -p 9050:8443 \
  -e SPRING_PROFILES_ACTIVE=sandbox \
  docker.eidastest.se:5000/signservice-integration-rest

echo Done!

