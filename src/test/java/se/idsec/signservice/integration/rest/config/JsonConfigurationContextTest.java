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

import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import se.idsec.signservice.integration.core.ObjectMapperFactory;
import se.idsec.signservice.integration.rest.SignServiceIntegrationApplication;

/**
 * Verifies that the read limits resolved by {@link JsonConfiguration} reach the {@code ObjectMapper} that actually
 * parses incoming REST payloads, i.e., the one held by {@link MappingJackson2HttpMessageConverter}.
 * <p>
 * The annotations deliberately match {@code SignServiceIntegrationApplicationTest} exactly so that Spring's context
 * cache hands out the same context. The application may only build one context per JVM - the
 * {@code SignServiceIntegrationApplication.Initializer} constructor installs a global {@code ContentLoaderSingleton}
 * and fails if a second context tries to install it again - so no test here may introduce a differing set of
 * properties. Verification that a <em>changed</em> limit takes effect therefore lives in {@link JsonConfigurationTest},
 * which drives the very same customizer against a {@link org.springframework.http.converter.json.Jackson2ObjectMapperBuilder}.
 * </p>
 *
 * @author Martin Lindström
 */
@ActiveProfiles("sandbox")
@SpringBootTest(classes = SignServiceIntegrationApplication.class)
class JsonConfigurationContextTest {

  @Autowired
  private JsonConfiguration jsonConfiguration;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private MappingJackson2HttpMessageConverter messageConverter;

  @Test
  void resolvedLimitReachesTheAutoConfiguredMapper() {
    final StreamReadConstraints expected = this.jsonConfiguration.streamReadConstraints();

    Assertions.assertEquals(expected.getMaxStringLength(),
        this.objectMapper.getFactory().streamReadConstraints().getMaxStringLength());
  }

  @Test
  void resolvedLimitReachesTheHttpMessageConverter() {
    final StreamReadConstraints expected = this.jsonConfiguration.streamReadConstraints();

    Assertions.assertEquals(expected.getMaxStringLength(),
        this.messageConverter.getObjectMapper().getFactory().streamReadConstraints().getMaxStringLength());
  }

  /**
   * With no {@code signservice.json.*} property set, the service must behave exactly as it did before these settings
   * existed.
   */
  @Test
  void unconfiguredServiceKeepsJacksonDefaults() {
    Assertions.assertEquals(StreamReadConstraints.defaults().getMaxStringLength(),
        this.objectMapper.getFactory().streamReadConstraints().getMaxStringLength());
  }

  /**
   * The library must be handed the very same mapper, not a copy - one mapper, one configuration, both parse paths.
   */
  @Test
  void theSameMapperIsAssignedToTheLibraryFactory() {
    Assertions.assertSame(this.objectMapper, ObjectMapperFactory.getInstance().getObjectMapper(),
        "signservice-integration must parse the signature state with Spring Boot's ObjectMapper");
  }

  /**
   * The point of the assignment: the read limit configured for request parsing must also govern the state parse
   * performed inside {@code signservice-integration}.
   */
  @Test
  void configuredLimitReachesTheLibraryMapper() {
    final StreamReadConstraints expected = this.jsonConfiguration.streamReadConstraints();

    Assertions.assertEquals(expected.getMaxStringLength(),
        ObjectMapperFactory.getInstance().getObjectMapper()
            .getFactory().streamReadConstraints().getMaxStringLength());
  }

  /**
   * The library's own fallback mapper omits null properties. The mapper handed to it must do the same, or the encoded
   * session state changes shape.
   */
  @Test
  void assignedMapperOmitsNullProperties() throws Exception {
    Assertions.assertEquals("{\"present\":\"x\"}",
        ObjectMapperFactory.getInstance().getObjectMapper().writeValueAsString(new Holder()));
  }

  /** Test type with a null property, used to observe the serialization inclusion actually in force. */
  public static class Holder {
    public String present = "x";
    public String absent = null;
  }

  /**
   * Spring Boot's other Jackson defaults must survive the customization - a regression here would mean the custom
   * factory replaced more than the read limits.
   */
  @Test
  void bootJacksonDefaultsArePreserved() {
    Assertions.assertFalse(
        this.objectMapper.getDeserializationConfig().isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES),
        "Spring Boot disables FAIL_ON_UNKNOWN_PROPERTIES - customizing the builder must not undo that");
    Assertions.assertFalse(
        this.objectMapper.getSerializationConfig().isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS),
        "Spring Boot disables WRITE_DATES_AS_TIMESTAMPS - customizing the builder must not undo that");
  }

}
