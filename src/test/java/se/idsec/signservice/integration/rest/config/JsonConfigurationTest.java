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
import com.fasterxml.jackson.core.exc.StreamConstraintsException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Test cases for {@link JsonConfiguration}.
 *
 * @author Martin Lindström
 */
class JsonConfigurationTest {

  /**
   * Builds the mapper that {@link JsonConfiguration} would install into Spring Boot's auto-configured
   * {@code ObjectMapper}, i.e., the mapper used to parse incoming REST payloads.
   *
   * @param configuration the configuration under test
   * @return the resulting ObjectMapper
   */
  private static ObjectMapper buildMapper(final JsonConfiguration configuration) {
    final Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
    final Jackson2ObjectMapperBuilderCustomizer customizer = configuration.jsonReadConstraintsCustomizer();
    customizer.customize(builder);
    return builder.build();
  }

  /**
   * Builds a JSON document holding a single string value of the given length.
   *
   * @param length the length of the string value
   * @return a JSON document
   */
  private static String jsonWithStringValue(final int length) {
    return "{\"doc\":\"" + "a".repeat(length) + "\"}";
  }

  @Test
  void whenNothingIsConfiguredJacksonDefaultsApply() {
    final StreamReadConstraints constraints = new JsonConfiguration().streamReadConstraints();
    final StreamReadConstraints defaults = StreamReadConstraints.defaults();

    Assertions.assertEquals(defaults.getMaxStringLength(), constraints.getMaxStringLength());
    Assertions.assertEquals(defaults.getMaxDocumentLength(), constraints.getMaxDocumentLength());
    Assertions.assertEquals(defaults.getMaxNestingDepth(), constraints.getMaxNestingDepth());
    Assertions.assertEquals(defaults.getMaxNumberLength(), constraints.getMaxNumberLength());
    Assertions.assertEquals(defaults.getMaxNameLength(), constraints.getMaxNameLength());
  }

  /**
   * Only the string length is configurable - every other read limit must keep its jackson-core default even when the
   * string length has been raised.
   */
  @Test
  void otherReadLimitsAreLeftAtTheirDefaults() {
    final JsonConfiguration configuration = new JsonConfiguration();
    configuration.setMaxStringLength(70000000);

    final StreamReadConstraints constraints = configuration.streamReadConstraints();
    final StreamReadConstraints defaults = StreamReadConstraints.defaults();

    Assertions.assertEquals(defaults.getMaxDocumentLength(), constraints.getMaxDocumentLength());
    Assertions.assertEquals(defaults.getMaxNestingDepth(), constraints.getMaxNestingDepth());
    Assertions.assertEquals(defaults.getMaxNumberLength(), constraints.getMaxNumberLength());
    Assertions.assertEquals(defaults.getMaxNameLength(), constraints.getMaxNameLength());
  }

  @Test
  void configuredLimitReachesTheRequestParsingMapper() {
    final JsonConfiguration configuration = new JsonConfiguration();
    configuration.setMaxStringLength(1000);

    final ObjectMapper mapper = buildMapper(configuration);

    Assertions.assertEquals(1000, mapper.getFactory().streamReadConstraints().getMaxStringLength());
  }

  @Test
  void configuredLimitIsEnforcedWhenParsing() throws Exception {
    final JsonConfiguration configuration = new JsonConfiguration();
    configuration.setMaxStringLength(1000);

    final ObjectMapper mapper = buildMapper(configuration);

    // Below the limit - parses fine.
    Assertions.assertNotNull(mapper.readTree(jsonWithStringValue(500)));

    // Above the limit - rejected.
    Assertions.assertThrows(StreamConstraintsException.class,
        () -> mapper.readTree(jsonWithStringValue(2000)));
  }

  /**
   * Verifies the actual point of this feature: a Base64-encoded document producing a JSON string value longer than
   * Jackson's built-in 20,000,000 character limit is rejected by default, and accepted once the limit is raised.
   */
  @Test
  void raisedLimitAcceptsPayloadThatExceedsTheJacksonDefault() throws Exception {
    final int defaultLimit = StreamReadConstraints.defaults().getMaxStringLength();
    final String oversized = jsonWithStringValue(defaultLimit + 100);

    final ObjectMapper defaultMapper = new ObjectMapper(JsonFactory.builder().build());
    Assertions.assertThrows(StreamConstraintsException.class, () -> defaultMapper.readTree(oversized));

    final JsonConfiguration configuration = new JsonConfiguration();
    configuration.setMaxStringLength(defaultLimit + 1000);

    Assertions.assertNotNull(buildMapper(configuration).readTree(oversized));
  }

  @Test
  void globalDefaultsAreNotTouchedWhenNothingIsConfigured() {
    final StreamReadConstraints before = StreamReadConstraints.defaults();

    new JsonConfiguration().applyGlobalStreamReadConstraints();

    Assertions.assertSame(before, StreamReadConstraints.defaults(),
        "Global Jackson defaults must be left untouched when no limits are configured");
  }

  /**
   * The global override is what reaches the {@code private static final ObjectMapper} that
   * {@code EncodedSignatureSessionState} uses to parse the signature state in {@code signservice-integration-impl}.
   * Since it mutates process-wide state, the previous defaults are restored afterwards.
   */
  @Test
  void globalDefaultsAreRaisedWhenConfigured() {
    final StreamReadConstraints before = StreamReadConstraints.defaults();
    try {
      final JsonConfiguration configuration = new JsonConfiguration();
      configuration.setMaxStringLength(before.getMaxStringLength() + 1000);

      configuration.applyGlobalStreamReadConstraints();

      Assertions.assertEquals(before.getMaxStringLength() + 1000,
          StreamReadConstraints.defaults().getMaxStringLength());

      // A mapper created after the override - as the impl library's static mapper is - picks the new limit up.
      Assertions.assertEquals(before.getMaxStringLength() + 1000,
          new ObjectMapper().getFactory().streamReadConstraints().getMaxStringLength());
    }
    finally {
      StreamReadConstraints.overrideDefaultStreamReadConstraints(before);
    }
  }

}
