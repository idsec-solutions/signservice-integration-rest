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
package se.idsec.signservice.integration.rest.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import se.idsec.signservice.integration.config.ConfigurationManager;
import se.idsec.signservice.integration.config.impl.DefaultConfigurationManager;

/**
 * Application main configuration.
 * 
 * @author Martin Lindström (martin@idsec.se)
 */
@Configuration
public class SignServiceIntegrationConfiguration {
  
  @Autowired
  private IntegrationServiceConfigurationProperties integrationServiceConfigurationProperties;
  
  @Bean
  ConfigurationManager configurationManager() {
    return new DefaultConfigurationManager(this.integrationServiceConfigurationProperties.getConfig());
  }

}
