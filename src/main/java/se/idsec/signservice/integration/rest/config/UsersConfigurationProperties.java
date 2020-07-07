/*
 * Copyright 2020 Litsec AB
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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Configuration properties for the users that should have access to the service.
 * 
 * @author Martin Lindström (martin@litsec.se)
 */
@Component
@PropertySource("${signservice.security.user-configuration}")
@ConfigurationProperties("signservice")
public class UsersConfigurationProperties {
    
  @Setter
  private Map<String, UserEntry> user;
  
  public Collection<UserEntry> getUsers() {
    return this.user != null ? this.user.values() : Collections.emptyList();
  }
  
  @PostConstruct
  public void setup() {
    
    if (this.user != null) {
      this.user.entrySet().stream().forEach(e -> e.getValue().setUserId(e.getKey()));
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

    /** The roles the a user has. */
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
