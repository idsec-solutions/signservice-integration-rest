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
package se.idsec.signservice.integration.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import se.idsec.signservice.integration.SignServiceIntegrationServiceInitializer;
import se.idsec.signservice.integration.core.ContentLoader;
import se.idsec.signservice.integration.core.ContentLoaderSingleton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Application main.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
@SpringBootApplication
@EnableScheduling
public class SignServiceIntegrationApplication {

  /**
   * Program main.
   *
   * @param args program arguments
   */
  public static void main(final String[] args) {
    SpringApplication.run(SignServiceIntegrationApplication.class, args);
  }

  /**
   * For initializing the underlying libraries (OpenSAML, xmlsec).
   */
  @Component("SignServiceInitializer")
  @Order(Ordered.HIGHEST_PRECEDENCE)
  public static class Initializer {

    public Initializer() throws Exception {

      // Workaround to avoid reflection
      //
      ((ContentLoaderSingleton) ContentLoaderSingleton.getInstance()).setContentLoader(new SpringContentLoader());

      SignServiceIntegrationServiceInitializer.initialize();
    }
  }

  /**
   * Provides logging of requests.
   *
   * @return log filter bean
   */
  @Bean
  CommonsRequestLoggingFilter requestLoggingFilter() {
    final CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
    loggingFilter.setIncludeClientInfo(true);
    loggingFilter.setIncludeQueryString(true);
    loggingFilter.setIncludePayload(true);
    loggingFilter.setMaxPayloadLength(64000);
    return loggingFilter;
  }

  /**
   * Content loader using Spring.
   */
  private static class SpringContentLoader implements ContentLoader {

    /** Spring resource loader. */
    private final DefaultResourceLoader loader = new DefaultResourceLoader();

    /** {@inheritDoc} */
    @Override
    public byte[] loadContent(final String resource) throws IOException {
      if (resource == null) {
        throw new IOException("resource is null");
      }
      final String _resource = resource.startsWith("/") ? "file://" + resource : resource;

      final InputStream is = this.loader.getResource(_resource).getInputStream();

      final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      int nRead;
      final byte[] data = new byte[4096];
      while ((nRead = is.read(data, 0, data.length)) != -1) {
        buffer.write(data, 0, nRead);
      }
      buffer.flush();
      return buffer.toByteArray();
    }

  }

}
