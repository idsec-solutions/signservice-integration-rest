FROM openjdk:11-jre

VOLUME /etc/signservice
RUN mkdir /opt/signservice
VOLUME /etc/ssl
ADD target/signservice-integration-rest-*.jar /opt/signservice/signservice-integration-rest.jar
ADD scripts/start.sh /opt/signservice/
RUN chmod a+rx /opt/signservice/start.sh

ENV JAVA_OPTS="-Dserver.port=8443 -Dserver.ssl.enabled=true -Dmanagement.server.port=8449 -Dmanagement.ssl.enabled=true -Dtomcat.ajp.enabled=true -Dtomcat.ajp.port=8009 -Djava.net.preferIPv4Stack=true -Dorg.apache.xml.security.ignoreLineBreaks=true"

ENTRYPOINT /opt/signservice/start.sh

EXPOSE 8443 8009 8449
