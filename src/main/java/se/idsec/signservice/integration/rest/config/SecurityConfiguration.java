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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import lombok.Setter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import se.idsec.signservice.integration.rest.config.UsersConfigurationProperties.UserEntry;

/**
 * Security configuration.
 * 
 * @author Martin Lindström
 */
@Configuration
@EnableMethodSecurity
@EnableWebSecurity
public class SecurityConfiguration {

  @Value("${management.endpoints.web.base-path:/actuator}")
  @Setter
  private String actuatorBasePath;

  @Setter
  @Autowired
  private UsersConfigurationProperties userConfiguration;


  @Bean
  UserDetailsService userDetailsService() {

    final List<UserDetails> users = new ArrayList<>();
    for (final UserEntry u : this.userConfiguration.getUsers()) {
      final List<SimpleGrantedAuthority> authorities = new ArrayList<>();
      for (final String role : u.getRoles()) {
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
      }
      for (final String policy : u.getPolicies()) {
        authorities.add(new SimpleGrantedAuthority("POLICY_" + policy.toLowerCase()));
      }
      users.add(new User(u.getUserId(), u.getPassword(), authorities));
    }
    return new InMemoryUserDetailsManager(users);
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      .authorizeHttpRequests(authorize -> authorize
        .requestMatchers(
          new AntPathRequestMatcher(this.actuatorBasePath + "/**"),
          new AntPathRequestMatcher("/actuator/**"),
          new AntPathRequestMatcher("/v1/version", HttpMethod.GET.toString()),
          new AntPathRequestMatcher("/error")
        ).permitAll()
        .requestMatchers(
          new AntPathRequestMatcher("/v1/policy/list"),
          new AntPathRequestMatcher("/v1/policy/get/**"),
          new AntPathRequestMatcher("/v1/create/**", HttpMethod.POST.toString()),
          new AntPathRequestMatcher("/v1/process/**", HttpMethod.POST.toString()),
          new AntPathRequestMatcher("/v1/prepare/**", HttpMethod.POST.toString())
        ).hasAnyRole("USER", "ADMIN")
        .anyRequest().authenticated()
      )
      .sessionManagement(httpSecuritySessionManagementConfigurer ->
        httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .csrf(AbstractHttpConfigurer::disable)
      .httpBasic(httpSecurityHttpBasicConfigurer -> {});

    return http.build();
  }

}
