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
package se.idsec.signservice.integration.rest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import lombok.Setter;
import se.idsec.signservice.integration.core.DocumentCache;
import se.idsec.signservice.integration.core.impl.InMemoryDocumentCache;
import se.idsec.signservice.integration.rest.cache.RedisDocumentCache;
import se.idsec.signservice.integration.rest.cache.RedisSignatureStateCache;
import se.idsec.signservice.integration.state.IntegrationServiceStateCache;
import se.idsec.signservice.integration.state.impl.InMemoryIntegrationServiceStateCache;

/**
 * Configuration class for setting up caches.
 * 
 * @author Martin Lindström (martin@idsec.se)
 */
@Configuration
public class CacheConfiguration {

  /** Max age for objects in state cache (in milliseconds). */
  @Setter
  @Value("${signservice.cache.state.max-age:3600000}")
  private long maxStateCacheAge;

  /** Max age for objects in document cache (in milliseconds). */
  @Setter
  @Value("${signservice.cache.document.max-age:240000}")
  private long maxDocumentCacheAge;

  /**
   * The Redis template.
   * 
   * @param connectionFactory
   *          the Redis connection factory
   * @return a RedisTemplate bean
   */
  @ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true", matchIfMissing = false)
  @Bean
  public RedisTemplate<String, Object> redisTemplate(final RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    return template;
  }

  /**
   * Gets a Redis IntegrationServiceStateCache bean.
   * 
   * @param redisTemplate
   *          the Redis template
   * @return a RedisSignatureStateCache bean
   */
  @ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true", matchIfMissing = false)
  @Bean
  public IntegrationServiceStateCache redisIntegrationServiceStateCache(final RedisTemplate<String, Object> redisTemplate) {
    final RedisSignatureStateCache cache = new RedisSignatureStateCache(redisTemplate);
    cache.setMaxAge(this.maxStateCacheAge);
    return cache;
  }

  /**
   * Gets a Redis {@link DocumentCache}.
   * 
   * @param redisTemplate
   *          the Redis template
   * @return a RedisDocumentCache bean
   */
  @ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true", matchIfMissing = false)
  @Bean
  public DocumentCache redisDocumentCache(final RedisTemplate<String, Object> redisTemplate) {
    final RedisDocumentCache cache = new RedisDocumentCache(redisTemplate);
    cache.setMaxAge(this.maxDocumentCacheAge);
    return cache;
  }

  /**
   * Gets an in-memory IntegrationServiceStateCache bean.
   * 
   * @return an in-memory IntegrationServiceStateCache bean
   */
  @ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "false", matchIfMissing = true)
  @Bean
  public IntegrationServiceStateCache integrationServiceStateCache() {
    final InMemoryIntegrationServiceStateCache cache = new InMemoryIntegrationServiceStateCache();
    cache.setMaxAge(this.maxStateCacheAge);
    return cache;
  }

  /**
   * Gets an in-memory DocumentCache bean
   * 
   * @return an in-memory DocumentCache bean
   */
  @ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "false", matchIfMissing = true)
  @Bean
  public DocumentCache docmentCache() {
    final InMemoryDocumentCache cache = new InMemoryDocumentCache();
    cache.setMaxAge(this.maxDocumentCacheAge);
    return cache;
  }

}
