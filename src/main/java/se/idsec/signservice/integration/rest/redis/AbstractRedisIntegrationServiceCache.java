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
package se.idsec.signservice.integration.rest.redis;

import java.io.Serializable;

import javax.annotation.PostConstruct;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import se.idsec.signservice.integration.core.IntegrationServiceCache;
import se.idsec.signservice.integration.core.impl.AbstractIntegrationServiceCache;
import se.idsec.signservice.integration.rest.redis.AbstractRedisIntegrationServiceCache.AbstractRedisCachedObject;

/**
 * Abstract base class for implemeting the {@link IntegrationServiceCache} using Redis.
 * 
 * @param <T>
 *          the actual type that is cached
 * @param <R>
 *          the type encapsulating the objects that are cached
 * 
 * @author Martin Lindström (martin@litsec.se)
 */
@Slf4j
public abstract class AbstractRedisIntegrationServiceCache<T extends Serializable, R extends AbstractRedisCachedObject<T>>
    extends AbstractIntegrationServiceCache<T> {

  /** The Redis template object. */
  private final RedisTemplate<String, Object> redisTemplate;

  /** The Redis hash operations object. */
  private HashOperations<String, String, R> hashOperations;

  /**
   * Constructor.
   * 
   * @param redisTemplate
   *          the Redis template
   */
  public AbstractRedisIntegrationServiceCache(final RedisTemplate<String, Object> redisTemplate) {
    this.redisTemplate = redisTemplate;
    this.hashOperations = this.redisTemplate.opsForHash();
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
   * @param id
   *          the ID
   * @param object
   *          the actual object to cache
   * @param ownerId
   *          the owner id
   * @param expirationTime
   *          the expiration time
   * @return a cacheable object
   */
  protected abstract R createCacheObject(final String id, final T object, final String ownerId, final long expirationTime);

  /** {@inheritDoc} */
  @Override
  protected CacheEntry<T> getCacheEntry(final String id) {
    return this.hashOperations.get(this.getRedisHashName(), id);
  }

  /** {@inheritDoc} */
  @Override
  protected void putCacheObject(final String id, final T object, final String ownerId, final long expirationTime) {
    this.hashOperations.put(this.getRedisHashName(), id, this.createCacheObject(id, object, ownerId, expirationTime));    
  }

  /** {@inheritDoc} */
  @Override
  protected void removeCacheObject(final String id) {
    this.hashOperations.delete(this.getRedisHashName(), id);
  }

  /**
   * A No-op for a redis cache. We configure the cache with TTL settings instead.
   */
  @Override
  public void clearExpired() {
  }
  
  /**
   * Tests the connection (so that we get failures at start-up).
   * 
   * @throws Exception for connection errors
   */
  @PostConstruct
  public void testConnection() throws Exception {
    log.debug("Checking connection for Redis hash '{}' ...", this.getRedisHashName());
    Long size = this.hashOperations.size(this.getRedisHashName());
    log.debug("Size for Redis hash '{}' is '{}'", this.getRedisHashName(), size);
  }

  /**
   * Abstract base class for a object that is cached using Redis.
   */
  @NoArgsConstructor
  @AllArgsConstructor
  public static abstract class AbstractRedisCachedObject<T extends Serializable> implements AbstractIntegrationServiceCache.CacheEntry<T> {

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

}
