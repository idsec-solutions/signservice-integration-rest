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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;

import lombok.Getter;
import lombok.Setter;
import se.idsec.signservice.integration.rest.config.support.SigningCredentialProperties;
import se.swedenconnect.security.credential.PkiCredential;

/**
 * Configuration for setting up signature credentials.
 *
 * @author Martin Lindström
 */
@Configuration
@ConfigurationProperties("signservice")
public class SignatureCredentialsConfiguration implements InitializingBean {

  /** The application context to which we register each credential as a bean. */
  @Autowired
  @Setter
  private GenericApplicationContext context;

  /** The properties. */
  @Getter
  @Setter
  private List<SigningCredentialProperties> credentials;

  /** The actual signing credentials. */
  private List<PkiCredential> signingCredentials;

  /**
   * Gets the {@code signingCredentials} bean.
   *
   * @return a list of signing credentials defined
   */
  @Bean("signingCredentials")
  public List<PkiCredential> signingCredentials() {
    return this.signingCredentials;
  }

  /** {@inheritDoc} */
  @Override
  public void afterPropertiesSet() throws Exception {
    if (this.credentials != null && !this.credentials.isEmpty()) {
      this.signingCredentials = new ArrayList<>();
      for (final SigningCredentialProperties c : this.credentials) {
        final PkiCredential credential = c.getSigningCredential();
        String name = credential.getName();
        if (name == null) {
          name = UUID.randomUUID().toString();
        }
        context.registerBean("SigningCredential." + name, PkiCredential.class, () -> credential);
        this.signingCredentials.add(credential);
      }
    }
  }

}
