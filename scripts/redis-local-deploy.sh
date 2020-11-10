#!/bin/bash

#
# Deployment script for using Redis locally ...
#

echo Pulling Redis docker image ...
docker pull redis

echo Undeploying ...
docker rm signservice-redis --force

echo Redeploying Redis docker container signservice-redis ...
docker run -d --name signservice-redis --restart=always \
  -p 6379:6379 \
  redis
