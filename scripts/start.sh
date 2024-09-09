#!/bin/bash

# Dummy script - Just to mimic the way the service is started in production ...

export JAVA_OPTS="$JAVA_OPTS --add-opens java.base/java.lang=ALL-UNNAMED"
# Add debug port
export JAVA_OPTS="$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8000"

exec java $JAVA_OPTS -jar /opt/signservice/signservice-integration-rest.jar
