/*
 * Copyright 2020 IDsec Solutions AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.idsec.signservice.integration.rest;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.ajp.AbstractAjpProtocol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

/**
 * Configuration settings for Tomcat.
 * 
 * @author Martin Lindström (martin@idsec.se)
 */
@Component
@ConditionalOnProperty(name = "tomcat.ajp.enabled", havingValue = "true")
public class TomcatWebServerConfiguration implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

  /** The Tomcat AJP port. */
  @Value("${tomcat.ajp.port:8009}")
  private int ajpPort;

  /** Is AJP enabled? */
  @Value("${tomcat.ajp.enabled:false}")
  private boolean tomcatAjpEnabled;

  /** AJP secret */
  @Value("${tomcat.ajp.secret:#{null}}")
  private String ajpSecret;

  /** Is AJP secret required? */
  @Value("${tomcat.ajp.secretRequired:false}")
  private boolean ajpSecretRequired;

  /** {@inheritDoc} */
  @Override
  public void customize(final TomcatServletWebServerFactory factory) {
    
    if (this.tomcatAjpEnabled) {
      Connector ajpConnector = new Connector("AJP/1.3");
      ajpConnector.setPort(this.ajpPort);
      ajpConnector.setAllowTrace(false);
      ajpConnector.setScheme("http");
      ajpConnector.setProperty("address", "0.0.0.0");
      ajpConnector.setProperty("allowedRequestAttributesPattern", ".*");

      final AbstractAjpProtocol<?> protocol = (AbstractAjpProtocol<?>) ajpConnector.getProtocolHandler();
      if (this.ajpSecretRequired) {
        ajpConnector.setSecure(true);
        protocol.setSecretRequired(true);
        protocol.setSecret(this.ajpSecret);
      }
      else {
        ajpConnector.setSecure(false);
        protocol.setSecretRequired(false);
      }

      factory.addAdditionalTomcatConnectors(ajpConnector);
    }
  }

}
