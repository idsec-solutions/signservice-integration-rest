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
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
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
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import se.idsec.signservice.integration.rest.config.UsersConfigurationProperties.UserEntry;
import se.idsec.signservice.integration.rest.security.PolicyPermissionEvaluator;

/**
 * Security configuration.
 * 
 * @author Martin Lindström
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

  @Value("${management.endpoints.web.base-path:/actuator}")
  @Setter
  private String actuatorBasePath;

  @Setter
  @Autowired
  private UsersConfigurationProperties userConfiguration;

  /**
   * Gets the permission evaluator used to check if a user has permissions on a particular policy.
   * 
   * @return the PolicyPermissionEvaluator bean
   */
  @Bean
  PolicyPermissionEvaluator policyPermissionEvaluator() {
    return new PolicyPermissionEvaluator();
  }

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


/*
  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    http
        .userDetailsService(this.userDetailsService())
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .csrf().disable()
        .authorizeRequests()
        .antMatchers(this.actuatorBasePath + "/**").permitAll()
        .antMatchers("/actuator/**").permitAll()
        .antMatchers(HttpMethod.GET, "/v1/version").permitAll()
        .antMatchers(HttpMethod.GET, "/v1/policy/list", "/v1/policy/get/**").hasAnyRole("USER", "ADMIN")
        .antMatchers(HttpMethod.POST, "/v1/create/**").hasAnyRole("USER", "ADMIN")
        .antMatchers(HttpMethod.POST, "/v1/process/**").hasAnyRole("USER", "ADMIN")
        .antMatchers(HttpMethod.POST, "/v1/prepare/**").hasAnyRole("USER", "ADMIN")
        .antMatchers("/error").permitAll()
        .anyRequest().denyAll()
        .and()
        .httpBasic();

    return http.build();
  }
*/

  /**
   * For setting up security checking on method calls.
   */
  @Configuration
  @EnableGlobalMethodSecurity(prePostEnabled = true)
  public static class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {

    @Setter
    @Autowired
    private PolicyPermissionEvaluator policyPermissionEvaluator;

    /** {@inheritDoc} */
    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
      DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
      expressionHandler.setPermissionEvaluator(this.policyPermissionEvaluator);
      return expressionHandler;
    }
  }

}
