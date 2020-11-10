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
package se.idsec.signservice.integration.rest.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import se.idsec.signservice.integration.core.DocumentCache;
import se.idsec.signservice.integration.state.IntegrationServiceStateCache;

/**
 * Service for cleaning up expired cache entries.
 * 
 * @author Martin Lindström (martin@litsec.se)
 */
@Service
@Slf4j
public class CacheCleanupService {

  /** The document cache. */
  @Setter
  @Autowired
  private DocumentCache documentCache;

  /** The state cache. */
  @Setter
  @Autowired
  private IntegrationServiceStateCache signatureStateCache;

  /**
   * Invokes the {@link DocumentCache#clearExpired()} method periodically.
   */
  @Scheduled(fixedDelayString = "${signservice.cache.document.cleanup-interval:300000}", initialDelayString = "${signservice.cache.document.cleanup-interval:300000}")
  public void cleanupDocumentCache() {
    try {
      log.trace("Cleaning up expired cached documents ...");
      this.documentCache.clearExpired();
    }
    catch (Exception e) {
      log.error("Error during document cache clean up", e);
    }
  }

  /**
   * Invokes the {@link IntegrationServiceStateCache#clearExpired()} method periodically.
   */
  @Scheduled(fixedDelayString = "${signservice.cache.state.cleanup-interval:300000}", initialDelayString = "${signservice.cache.state.cleanup-interval:300000}")
  public void cleanupStateCache() {
    try {
      log.trace("Cleaning up expired cached state objects ...");
      this.signatureStateCache.clearExpired();
    }
    catch (Exception e) {
      log.error("Error during signature state cache clean up", e);
    }
  }

}
