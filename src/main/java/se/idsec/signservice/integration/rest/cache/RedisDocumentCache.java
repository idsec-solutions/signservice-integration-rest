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
import se.idsec.signservice.integration.core.DocumentCache;
import se.idsec.signservice.integration.rest.cache.RedisDocumentCache.CachedDocument;

import java.io.Serial;

/**
 * A Redis {@link DocumentCache}.
 *
 * @author Martin Lindström
 */
public class RedisDocumentCache extends AbstractRedisIntegrationServiceCache<String, CachedDocument>
    implements DocumentCache {

  /**
   * Constructor.
   *
   * @param redisTemplate the Redis template
   */
  public RedisDocumentCache(final RedisTemplate<String, Object> redisTemplate) {
    super(redisTemplate);
  }

  /** {@inheritDoc} */
  @Override
  protected String getRedisHashName() {
    return "ssDocuments";
  }

  /** {@inheritDoc} */
  @Override
  protected CachedDocument createCacheObject(final String id, final String object, final String ownerId,
      final long expirationTime) {
    return new CachedDocument(id, object, ownerId, expirationTime);
  }

  /**
   * Representation of a cached document.
   */
  public static class CachedDocument extends AbstractRedisCachedObject<String> {

    /** For serialization. */
    @Serial
    private static final long serialVersionUID = -8324998523618078986L;

    /**
     * Default constructor.
     */
    public CachedDocument() {
    }

    /**
     * An all-args constructor.
     *
     * @param id the ID
     * @param object the document
     * @param ownerId the owner ID
     * @param expirationTime the expiration time
     */
    public CachedDocument(final String id, final String object, final String ownerId, final Long expirationTime) {
      super(id, object, ownerId, expirationTime);
    }
  }

}
