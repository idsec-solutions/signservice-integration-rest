/*
 * Copyright 2020-2025 IDsec Solutions AB
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

import org.springframework.data.redis.core.RedisTemplate;
import se.idsec.signservice.integration.rest.cache.RedisSignatureStateCache.CachedSignatureState;
import se.idsec.signservice.integration.state.CacheableSignatureState;
import se.idsec.signservice.integration.state.IntegrationServiceStateCache;

import java.io.Serial;

/**
 * A Redis {@link IntegrationServiceStateCache}.
 *
 * @author Martin Lindström
 */
public class RedisSignatureStateCache
    extends AbstractRedisIntegrationServiceCache<CacheableSignatureState, CachedSignatureState>
    implements IntegrationServiceStateCache {

  /**
   * Constructor.
   *
   * @param redisTemplate the Redis template
   */
  public RedisSignatureStateCache(final RedisTemplate<String, Object> redisTemplate) {
    super(redisTemplate);
  }

  /** {@inheritDoc} */
  @Override
  protected String getRedisHashName() {
    return "ssSignatureState";
  }

  /** {@inheritDoc} */
  @Override
  protected CachedSignatureState createCacheObject(
      final String id, final CacheableSignatureState object, final String ownerId, final long expirationTime) {
    return new CachedSignatureState(id, object, ownerId, expirationTime);
  }

  /**
   * Class for representing a signature state that is cached.
   */
  public static class CachedSignatureState extends AbstractRedisCachedObject<CacheableSignatureState> {

    /** For serialization. */
    @Serial
    private static final long serialVersionUID = -3427745821799588985L;

    /**
     * Default constructor.
     */
    public CachedSignatureState() {
    }

    /**
     * An all-args constructor.
     *
     * @param id the ID
     * @param object the document
     * @param ownerId the owner ID
     * @param expirationTime the expiration time
     */
    public CachedSignatureState(final String id, final CacheableSignatureState object, final String ownerId,
        final Long expirationTime) {
      super(id, object, ownerId, expirationTime);
    }

  }

}
