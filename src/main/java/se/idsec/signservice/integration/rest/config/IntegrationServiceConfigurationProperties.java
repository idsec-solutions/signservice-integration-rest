/*
 * Copyright 2020-2024 IDsec Solutions AB
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

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import se.idsec.signservice.integration.config.impl.DefaultIntegrationServiceConfiguration;
import se.idsec.signservice.integration.security.impl.OpenSAMLEncryptionParameters;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Properties for the IntegrationService Configuration and for the users that should have access to the service.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
@Component
@DependsOn(value = { "SignServiceInitializer",
    "signingCredentials",
    "nameToSigningCredentialConverter",
    "propertyToX509CertificateConverter" })
@PropertySource({
    "${signservice.integration.policy-configuration-resource}",
    "${signservice.security.user-configuration}" })
@ConfigurationProperties("signservice")
@Slf4j
public class IntegrationServiceConfigurationProperties {

  @Setter
  @Getter
  private String defaultPolicyName;

  /** The configuration, where the map key is the policy name. */
  @Getter
  @Setter
  private Map<String, DefaultIntegrationServiceConfiguration> config;

  @Setter
  private Map<String, UserEntry> user;

  public Collection<UserEntry> getUsers() {
    return this.user != null ? this.user.values() : Collections.emptyList();
  }

  /**
   * Assigns default values and sets up user access.
   */
  @PostConstruct
  public void setup() {
    if (this.config == null) {
      throw new IllegalArgumentException("Missing SignService Integration configuration");
    }

    for (final Map.Entry<String, DefaultIntegrationServiceConfiguration> e : this.config.entrySet()) {
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

    if (this.user != null) {
      this.user.forEach((key, value) -> value.setUserId(key));
    }
  }

  /**
   * Represents the user roles and policies.
   */
  @ToString
  public static class UserEntry {

    @Setter
    @Getter
    private String userId;

    /** The roles a user has. */
    @Setter
    private List<String> roles;

    /** The policies that a user has access to. */
    @Setter
    private List<String> policies;

    @Setter
    @Getter
    private String password;

    public List<String> getRoles() {
      return this.roles != null ? this.roles : Collections.emptyList();
    }

    public List<String> getPolicies() {
      return this.policies != null ? this.policies : Collections.emptyList();
    }

  }

}
