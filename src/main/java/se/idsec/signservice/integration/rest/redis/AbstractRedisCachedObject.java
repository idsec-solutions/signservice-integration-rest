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

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.idsec.signservice.integration.core.impl.AbstractIntegrationServiceCache;

/**
 * Abstract base class for a object that is cached using Redis. 
 * 
 * @author Martin Lindström (martin@idsec.se)
 */
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractRedisCachedObject<T extends Serializable> implements AbstractIntegrationServiceCache.CacheEntry<T> {

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
