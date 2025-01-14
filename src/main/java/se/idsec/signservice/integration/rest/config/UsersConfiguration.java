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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration for resolving the configured users of the service.
 *
 * @author Martin Lindström
 */
@Configuration
@PropertySource("${signservice.security.user-configuration}")
public class UsersConfiguration {

  @Bean("signservice.user.map")
  @ConfigurationProperties("signservice.user")
  Map<String, UserEntry> user() {
    return new HashMap<>();
  }

  @Bean("signservice.users")
  Collection<UserEntry> users(@Qualifier("signservice.user.map") final Map<String, UserEntry> userMap) {
    userMap.forEach((key, value) -> value.setUserId(key));
    return userMap.values();
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
