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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import se.idsec.signservice.integration.rest.config.support.SigningCredentialProperties;
import se.swedenconnect.security.credential.PkiCredential;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Configuration for setting up signature credentials.
 *
 * @author Martin Lindström
 */
@Configuration
@EnableConfigurationProperties
public class SignatureCredentialsConfiguration {

  /** The application context to which we register each credential as a bean. */
  private final GenericApplicationContext context;

  public SignatureCredentialsConfiguration(final GenericApplicationContext context) {
    this.context = context;
  }

  @Bean("SigningCredentialProperties")
  @ConfigurationProperties("signservice.credentials")
  List<SigningCredentialProperties> signingCredentialProperties() {
    return new ArrayList<>();
  }

  @Bean("signingCredentials")
  List<PkiCredential> signingCredentials(
      @Qualifier("SigningCredentialProperties") final List<SigningCredentialProperties> signingCredentialProperties)
      throws Exception {

    if (signingCredentialProperties != null && !signingCredentialProperties.isEmpty()) {
      final List<PkiCredential> signingCredentials = new ArrayList<>();
      for (final SigningCredentialProperties c : signingCredentialProperties) {
        final PkiCredential credential = c.getSigningCredential();
        String name = credential.getName();
        if (name == null) {
          name = UUID.randomUUID().toString();
        }
        this.context.registerBean("SigningCredential." + name, PkiCredential.class, () -> credential);
        signingCredentials.add(credential);
      }
      return signingCredentials;
    }
    else {
      return Collections.emptyList();
    }

  }

}
