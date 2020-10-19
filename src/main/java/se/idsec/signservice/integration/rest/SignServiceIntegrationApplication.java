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
package se.idsec.signservice.integration.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.trace.http.HttpTraceRepository;
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import se.idsec.signservice.integration.SignServiceIntegrationServiceInitializer;

/**
 * Application main.
 * 
 * @author Martin Lindström (martin@idsec.se)
 */
@SpringBootApplication
public class SignServiceIntegrationApplication {

  /**
   * Program main.
   * 
   * @param args
   *          program arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(SignServiceIntegrationApplication.class, args);
  }

  /**
   * For initializing the underlying libraries (OpenSAML, xmlsec).
   */
  @Component("SignServiceInitializer")
  @Order(Ordered.HIGHEST_PRECEDENCE)
  public static class Initializer {

    public Initializer() throws Exception {
      SignServiceIntegrationServiceInitializer.initialize();
    }

  }

  /**
   * Provides logging of requests.
   * 
   * @return log filter bean
   */
  @Bean
  public CommonsRequestLoggingFilter requestLoggingFilter() {
    CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
    loggingFilter.setIncludeClientInfo(true);
    loggingFilter.setIncludeQueryString(true);
    loggingFilter.setIncludePayload(true);
    loggingFilter.setMaxPayloadLength(64000);
    return loggingFilter;
  }

  /**
   * To allow Spring Boot actuator's httptrace.
   * 
   * @return a trace repository
   */
  @Bean
  public HttpTraceRepository htttpTraceRepository() {
    return new InMemoryHttpTraceRepository();
  }

}
