/*
 * Copyright 2020-2025 IDsec Solutions AB
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

import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for base beans.
 *
 * @author Martin Lindström
 */
@Configuration
public class BaseComponentsConfiguration {

  @Setter
  @Value("${application.version:1.1.0}")
  private String applicationVersion;

  /**
   * The version of the application.
   *
   * @return application version
   */
  @Bean("ApplicationVersion")
  String applicationVersion() {
    return this.applicationVersion;
  }

  /**
   * Creates (and registers) the {@link NameToSigningCredentialConverter} converter.
   *
   * @return a NameToSigningCredentialConverter
   */
  @Bean("nameToSigningCredentialConverter")
  @ConfigurationPropertiesBinding
  NameToSigningCredentialConverter nameToSigningCredentialConverter() {
    return new NameToSigningCredentialConverter();
  }

}
