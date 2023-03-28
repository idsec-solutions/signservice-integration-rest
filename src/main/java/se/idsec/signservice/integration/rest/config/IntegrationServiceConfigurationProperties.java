/*
 * Copyright 2020-2023 IDsec Solutions AB
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
package se.idsec.signservice.integration.rest.config;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import se.idsec.signservice.integration.config.impl.DefaultIntegrationServiceConfiguration;
import se.idsec.signservice.integration.security.impl.OpenSAMLEncryptionParameters;

/**
 * Properties for the IntegrationService Configuration.
 * 
 * @author Martin Lindström (martin@idsec.se)
 */
@Component
@DependsOn(value = { "SignServiceInitializer", "signingCredentials", "nameToSigningCredentialConverter", "propertyToX509CertificateConverter" })
@PropertySource("${signservice.integration.policy-configuration-resource}")
@ConfigurationProperties("signservice")
@Slf4j
public class IntegrationServiceConfigurationProperties {

  /** The configuration, where the map key is the policy name. */
  @Getter
  @Setter
  private Map<String, DefaultIntegrationServiceConfiguration> config;

  /**
   * Assigns default values.
   */
  @PostConstruct
  public void assignDefaults() {
    if (this.config == null) {
      throw new IllegalArgumentException("Missing SignService Integration configuration");
    }

    for (Map.Entry<String, DefaultIntegrationServiceConfiguration> e : this.config.entrySet()) {
      if (e.getValue().getParentPolicy() == null) {
        if (e.getValue().getDefaultEncryptionParameters() == null) {
          e.getValue().setDefaultEncryptionParameters(new OpenSAMLEncryptionParameters());
        }
        if (e.getValue().getDefaultSignatureAlgorithm() == null) {
          e.getValue().setDefaultSignatureAlgorithm("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");
        }
      }
    }
    log.info("Integration Service Configuration: {}", this.config);
  }

}
