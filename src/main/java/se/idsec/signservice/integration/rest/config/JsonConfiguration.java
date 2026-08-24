/*
 * Copyright 2020-2026 IDsec Solutions AB
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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.idsec.signservice.integration.core.ObjectMapperFactory;

/**
 * Configuration of the Jackson streaming read limit that applies when the service parses incoming JSON.
 * <p>
 * Clients post Base64-encoded PDF and XML documents as single JSON string values. Base64 inflates the encoded size by
 * roughly 4/3, so a document larger than about 15 MB produces a JSON string longer than Jackson's built-in
 * {@link StreamReadConstraints#DEFAULT_MAX_STRING_LEN} limit of 20,000,000 characters and the request is rejected
 * before it reaches any service logic. The {@code signservice.json.max-string-length} setting lets a deployer raise
 * that limit to match the largest document the deployment intends to accept. Every other Jackson read limit is left at
 * its jackson-core default.
 * </p>
 * <p>
 * The limit has to reach two different parse paths, and it is applied through three mechanisms:
 * </p>
 * <ul>
 * <li>A {@link Jackson2ObjectMapperBuilderCustomizer} installs a {@link JsonFactory} carrying the configured
 * constraints into Spring Boot's auto-configured {@code ObjectMapper} - the mapper behind
 * {@code MappingJackson2HttpMessageConverter} that parses client requests. Customizing the builder rather than
 * replacing the {@code ObjectMapper} bean preserves everything else Boot configures (modules, date handling, the
 * settings bound from {@code spring.jackson.*}).</li>
 * <li>That same {@code ObjectMapper} bean is handed to {@link ObjectMapperFactory}, the singleton through which
 * {@code signservice-integration} obtains its mapper. The library performs the signature state round-trip -
 * {@code EncodedSignatureSessionState} writes the state and parses it back - and in stateless mode that state carries
 * the to-be-signed documents, so it meets the same limit as the request body. Without this assignment the library
 * would fall back to a mapper of its own built with Jackson's defaults, and raising the limit would fix
 * {@code /v1/create} while leaving the matching {@code /v1/process} call failing on the same document.</li>
 * <li>{@link StreamReadConstraints#overrideDefaultStreamReadConstraints(StreamReadConstraints)} raises the default for
 * every {@code JsonFactory} created afterwards. Nothing in {@code signservice-integration} depends on this any more -
 * every Jackson call site there goes through {@link ObjectMapperFactory} - so this is a safety net for mappers created
 * outside that factory, in this application or in a third-party library.</li>
 * </ul>
 * <p>
 * Note that the global override replaces only Jackson's stream read limits. It does not affect any other Jackson
 * configuration - serialization and deserialization features, registered modules, date handling and write constraints
 * are all left alone. The override is furthermore only performed when a limit has actually been configured, so a
 * deployment that assigns no {@code signservice.json.*} property behaves exactly as it did before this class existed.
 * </p>
 * <p>
 * One setting is required for the mapper handed to {@link ObjectMapperFactory} to behave like the one the library
 * would otherwise build for itself: {@code spring.jackson.default-property-inclusion=non_null} in
 * {@code application.properties}. Without it the encoded session state would start carrying null-valued properties
 * and change shape. It is set through Spring so that it is applied while the bean is being built, rather than by
 * reconfiguring an already-published mapper.
 * </p>
 *
 * @author Martin Lindström
 */
@Configuration
@Slf4j
public class JsonConfiguration {

  /** Maximum length of a single JSON string value. Defaults to the jackson-core default. */
  @Setter
  @Value("${signservice.json.max-string-length:#{null}}")
  private Integer maxStringLength;

  /**
   * Builds the {@link StreamReadConstraints} to use. If {@code signservice.json.max-string-length} has not been
   * assigned, the default of the jackson-core version on the classpath is used.
   *
   * @return the stream read constraints to apply
   */
  StreamReadConstraints streamReadConstraints() {
    final StreamReadConstraints defaults = StreamReadConstraints.defaults();

    // StreamReadConstraints.builder() starts out with every limit at the jackson-core default, so only the
    // string length is overridden here.
    return StreamReadConstraints.builder()
        .maxStringLength(this.maxStringLength != null ? this.maxStringLength : defaults.getMaxStringLength())
        .build();
  }

  /**
   * Customizer that installs a {@link JsonFactory} holding the configured read limit into Spring Boot's
   * auto-configured {@code ObjectMapper}, i.e., the mapper used to parse incoming REST payloads.
   *
   * @return a Jackson2ObjectMapperBuilderCustomizer bean
   */
  @Bean
  Jackson2ObjectMapperBuilderCustomizer jsonReadConstraintsCustomizer() {
    final StreamReadConstraints constraints = this.streamReadConstraints();

    return builder -> builder.factory(JsonFactory.builder()
        .streamReadConstraints(constraints)
        .build());
  }

  /**
   * Hands Spring Boot's {@code ObjectMapper} - the one carrying the read limit configured above - to
   * {@link ObjectMapperFactory}, so that {@code signservice-integration} parses the signature state with the same
   * mapper this service parses request bodies with.
   * <p>
   * Declaring the {@code ObjectMapper} as a method parameter is what makes the ordering deterministic: Spring must
   * finish building that bean, customizers included, before it can invoke this method, and the assignment then runs
   * while singletons are being pre-instantiated - well before the web server starts accepting requests. The assignment
   * is unconditional, so the library never falls back to a mapper of its own.
   * </p>
   *
   * @param objectMapper Spring Boot's auto-configured ObjectMapper
   * @return an InitializingBean performing the assignment
   */
  @Bean
  InitializingBean signServiceIntegrationObjectMapperAssignment(final ObjectMapper objectMapper) {
    return () -> {
      ObjectMapperFactory.getInstance().setObjectMapper(objectMapper);

      log.debug("JSON read limits: max-string-length={} - applied to the Spring ObjectMapper, which has been "
              + "assigned to the SignService Integration ObjectMapperFactory",
          objectMapper.getFactory().streamReadConstraints().getMaxStringLength());
    };
  }

  /**
   * Installs the configured read limit as the global Jackson default so that it also reaches mappers created inside
   * {@code signservice-integration-impl} - see the class documentation. This is a no-op unless the limit has actually
   * been configured to something other than the jackson-core default.
   */
  @PostConstruct
  public void applyGlobalStreamReadConstraints() {
    final StreamReadConstraints constraints = this.streamReadConstraints();
    if (constraints.getMaxStringLength() == StreamReadConstraints.defaults().getMaxStringLength()) {
      return;
    }
    StreamReadConstraints.overrideDefaultStreamReadConstraints(constraints);
    log.info("Installed JSON read limits as global Jackson defaults");
  }

}
