/*
 * Copyright 2019-2025 IDsec Solutions AB
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

import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.util.StringUtils;
import se.swedenconnect.security.credential.PkiCredential;

import java.util.Map;

/**
 * A {@link Converter} that lets the user reference a {@link PkiCredential} instance using its bean name, or if no bean
 * is found, the signing credential name {@link PkiCredential#getName()}.
 * <p>
 * To use this converter it has to be instantiated as a bean and then registered in the registry using
 * {@link ConverterRegistry#addConverter(Converter)}.
 * </p>
 *
 * @author Martin Lindström (martin@idsec.se)
 * @author Stefan Santesson (stefan@idsec.se)
 */
@Slf4j
public class NameToSigningCredentialConverter implements Converter<String, PkiCredential>, ApplicationContextAware {

  /** The application context. */
  private ApplicationContext applicationContext;

  /** {@inheritDoc} */
  @Override
  public PkiCredential convert(@Nonnull final String source) {
    if (!StringUtils.hasText(source)) {
      return null;
    }
    final String credName = source.replaceAll("\\s", "");

    log.debug("Converting '{}' into a PkiCredential instance ...", credName);
    try {
      final PkiCredential cred = this.applicationContext.getBean(credName, PkiCredential.class);
      log.debug("Found bean of type '{}' and bean name '{}' in the application context",
          PkiCredential.class.getSimpleName(), credName);
      return cred;
    }
    catch (final BeansException e) {
      log.debug("No bean of type '{}' and bean name '{}' has been registered", PkiCredential.class.getSimpleName(),
          credName);
    }
    log.debug("Listing all PkiCredential beans ...");
    try {
      final Map<String, PkiCredential> map = this.applicationContext.getBeansOfType(PkiCredential.class);
      for (final PkiCredential c : map.values()) {
        if (credName.equalsIgnoreCase(c.getName())) {
          log.debug("Found bean of type '{}' and given name '{}' in the application context",
              PkiCredential.class.getSimpleName(), credName);
          return c;
        }
      }
    }
    catch (final BeansException ignored) {
    }
    final String msg = String.format("No SigningCredential instance matching '%s' was found", credName);
    log.error("{}", msg);
    throw new IllegalArgumentException(msg);
  }

  /** {@inheritDoc} */
  @Override
  public void setApplicationContext(@Nonnull final ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

}
