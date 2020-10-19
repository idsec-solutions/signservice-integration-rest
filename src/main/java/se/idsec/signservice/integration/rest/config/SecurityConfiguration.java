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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import lombok.Setter;
import se.idsec.signservice.integration.rest.config.UsersConfigurationProperties.UserEntry;
import se.idsec.signservice.integration.rest.security.PolicyPermissionEvaluator;

/**
 * Security configuration.
 * 
 * @author Martin Lindström (martin@litsec.se)
 */
@Configuration
public class SecurityConfiguration {
  
  /**
   * Gets the permission evaluator used to check if a user has permissions on a particular policy.
   * 
   * @return the PolicyPermissionEvaluator bean
   */
  @Bean
  public PolicyPermissionEvaluator policyPermissionEvaluator() {
    return new PolicyPermissionEvaluator();
  }

  /**
   * Web security configuration.
   */
  @Configuration
  @EnableWebSecurity
  public static class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
    
    @Value("${management.endpoints.web.base-path:/actuator}")
    @Setter
    private String actuatorBasePath;

    @Setter
    @Autowired
    private UsersConfigurationProperties userConfiguration;

    @Bean
    public UserDetailsService userDetailsService() {

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

    /**
     * Default constructor.
     */
    public WebSecurityConfiguration() {
    }

    /**
     * Constructor.
     * 
     * @param disableDefaults
     *          tells whether defaults should be disabled
     */
    public WebSecurityConfiguration(final boolean disableDefaults) {
      super(disableDefaults);
    }

    /** {@inheritDoc} */
    @Override
    protected void configure(final HttpSecurity http) throws Exception {

      http
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .csrf().disable()
        .authorizeRequests()
          .antMatchers(this.actuatorBasePath + "/**").permitAll()
          .antMatchers("/actuator/**").permitAll()
          .antMatchers(HttpMethod.GET, "/v1/policy/list", "/v1/policy/get/**").hasAnyRole("USER", "ADMIN")
          .antMatchers(HttpMethod.POST, "/v1/create/**").hasAnyRole("USER", "ADMIN")
          .antMatchers(HttpMethod.POST, "/v1/process/**").hasAnyRole("USER", "ADMIN")
          .antMatchers(HttpMethod.POST, "/v1/prepare/**").hasAnyRole("USER", "ADMIN")
          .antMatchers("/error").permitAll()
          .anyRequest().denyAll()
        .and()
        .httpBasic();
    }

    /** {@inheritDoc} */
    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
      auth.userDetailsService(this.userDetailsService());
    }
  }

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
