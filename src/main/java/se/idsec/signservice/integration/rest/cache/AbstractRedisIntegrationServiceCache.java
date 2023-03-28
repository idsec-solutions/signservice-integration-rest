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
package se.idsec.signservice.integration.rest.cache;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import se.idsec.signservice.integration.core.IntegrationServiceCache;
import se.idsec.signservice.integration.core.impl.AbstractIntegrationServiceCache;
import se.idsec.signservice.integration.rest.cache.AbstractRedisIntegrationServiceCache.AbstractRedisCachedObject;

/**
 * Abstract base class for implemeting the {@link IntegrationServiceCache} using Redis.
 * 
 * @param <T> the actual type that is cached
 * @param <R> the type encapsulating the objects that are cached
 * 
 * @author Martin Lindström
 */
@Slf4j
public abstract class AbstractRedisIntegrationServiceCache<T extends Serializable, R extends AbstractRedisCachedObject<T>>
    extends AbstractIntegrationServiceCache<T> {

  /** The Redis template object. */
  private final RedisTemplate<String, Object> redisTemplate;

  /** The Redis hash operations object. */
  private HashOperations<String, String, R> operations;

  /** For keeping a small cache so that we can check expired entries (Redis doesn't support TTL on each entry). */
  private HashOperations<String, String, ExpirationHelperObject> expOps;

  /**
   * Constructor.
   * 
   * @param redisTemplate the Redis template
   */
  public AbstractRedisIntegrationServiceCache(final RedisTemplate<String, Object> redisTemplate) {
    this.redisTemplate = redisTemplate;
    this.operations = this.redisTemplate.opsForHash();
    this.expOps = this.redisTemplate.opsForHash();
  }

  /**
   * Gets the Redis hash key for this object's type of cacheable objects.
   * 
   * @return a string holding the hash name
   */
  protected abstract String getRedisHashName();

  /**
   * Creates a cacheable object.
   * 
   * @param id the ID
   * @param object the actual object to cache
   * @param ownerId the owner id
   * @param expirationTime the expiration time
   * @return a cacheable object
   */
  protected abstract R createCacheObject(final String id, final T object, final String ownerId,
      final long expirationTime);

  /** {@inheritDoc} */
  @Override
  protected CacheEntry<T> getCacheEntry(final String id) {
    return this.operations.get(this.getRedisHashName(), id);
  }

  /** {@inheritDoc} */
  @Override
  protected void putCacheObject(final String id, final T object, final String ownerId, final long expirationTime) {
    this.operations.put(this.getRedisHashName(), id, this.createCacheObject(id, object, ownerId, expirationTime));
    this.expOps.put(this.getRedisHashName() + "_exp", id, new ExpirationHelperObject(id, expirationTime));
  }

  /** {@inheritDoc} */
  @Override
  protected void removeCacheObject(final String id) {
    this.operations.delete(this.getRedisHashName(), id);
    this.expOps.delete(this.getRedisHashName() + "_exp", id);
  }

  /** {@inheritDoc} */
  @Override
  public void clearExpired() {
    log.trace("clearExpired called ...");

    final List<ExpirationHelperObject> values =
        Optional.ofNullable(this.expOps.values(this.getRedisHashName() + "_exp")).orElse(Collections.emptyList());

    final long now = System.currentTimeMillis();
    final Object[] forRemoval = values.stream()
        .filter(v -> now > Optional.ofNullable(v.getExpirationTime()).orElse(Long.MAX_VALUE))
        .map(ExpirationHelperObject::getId)
        .toArray(String[]::new);

    if (forRemoval.length == 0) {
      log.trace("No expired entries to purge ...");
      return;
    }
    log.debug("Purging expired cached entries from {}/{}: {}", this.getClass().getSimpleName(), this.getRedisHashName(),
        forRemoval);
    this.operations.delete(this.getRedisHashName(), forRemoval);
    this.expOps.delete(this.getRedisHashName() + "_exp", forRemoval);
  }

  /**
   * Tests the connection (so that we get failures at start-up).
   * 
   * @throws Exception for connection errors
   */
  @PostConstruct
  public void testConnection() throws Exception {
    log.debug("Checking connection for Redis hash '{}' ...", this.getRedisHashName());
    Long size = this.operations.size(this.getRedisHashName());
    log.debug("Size for Redis hash '{}' is '{}'", this.getRedisHashName(), size);
  }

  /**
   * Abstract base class for a object that is cached using Redis.
   */
  @NoArgsConstructor
  @AllArgsConstructor
  public static abstract class AbstractRedisCachedObject<T extends Serializable>
      implements AbstractIntegrationServiceCache.CacheEntry<T> {

    /** For serialization. */
    private static final long serialVersionUID = 3566087347789497782L;

    /** The ID. */
    @Getter
    @Setter
    private String id;

    /** The object itself. */
    @Setter
    private T object;

    /** The owner identity. */
    @Setter
    private String ownerId;

    /** The expiration time. */
    @Setter
    private Long expirationTime;

    /** {@inheritDoc} */
    @Override
    public T getObject() {
      return this.object;
    }

    /** {@inheritDoc} */
    @Override
    public String getOwnerId() {
      return this.ownerId;
    }

    /** {@inheritDoc} */
    @Override
    public Long getExpirationTime() {
      return this.expirationTime;
    }

  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  private static class ExpirationHelperObject implements Serializable {
    /** For serializing. */
    private static final long serialVersionUID = -3004136054046290749L;
    private String id;
    private Long expirationTime;
  }

}
