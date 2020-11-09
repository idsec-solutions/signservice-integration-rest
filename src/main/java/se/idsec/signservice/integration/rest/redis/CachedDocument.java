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
package se.idsec.signservice.integration.rest.redis;

import org.springframework.data.redis.core.RedisHash;

/**
 * Class for representing a document that is cached.
 * 
 * @author Martin Lindström (martin@idsec.se)
 */
@RedisHash("cachedDocument")
public class CachedDocument extends AbstractRedisCachedObject<String> {

  /** For serialization. */
  private static final long serialVersionUID = -8324998523618078986L;

  /**
   * Default constructor.
   */
  public CachedDocument() {
  }

  /**
   * An all-args constructor.
   * 
   * @param id
   *          the ID
   * @param object
   *          the document
   * @param ownerId
   *          the owner ID
   * @param expirationTime
   *          the expiration time
   */
  public CachedDocument(final String id, final String object, final String ownerId, final Long expirationTime) {
    super(id, object, ownerId, expirationTime);
  }
  
}
