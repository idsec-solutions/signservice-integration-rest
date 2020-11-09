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

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import se.idsec.signservice.integration.core.DocumentCache;
import se.idsec.signservice.integration.core.IntegrationServiceCache;
import se.idsec.signservice.integration.core.impl.InMemoryDocumentCache;
import se.idsec.signservice.integration.state.IntegrationServiceStateCache;
import se.idsec.signservice.integration.state.impl.InMemoryIntegrationServiceStateCache;

/**
 * Configuration class for in-memory caches. This is the default choice if no other caches are instantiated.
 * 
 * @author Martin Lindström (martin@idsec.se)
 */
@Configuration
@EnableScheduling
public class InMemoryCacheConfiguration implements DisposableBean {

  /** Max age for objects in state cache (in milliseconds). */
  @Setter
  @Value("${signservice.cache.state.max-age:3600000}")
  private long maxStateCacheAge;

  /** Clean up interval for state cache. */
  @Setter
  @Value("${signservice.cache.state.cleanup-interval:300000}")
  private long cleanupIntervalStateCache;

  /** Max age for objects in document cache (in milliseconds). */
  @Setter
  @Value("${signservice.cache.document.max-age:240000}")
  private long maxDocumentCacheAge;
  
  /** Clean up interval for document cache. */
  @Setter
  @Value("${signservice.cache.document.cleanup-interval:300000}")
  private long cleanupIntervalDocumentCache;

  /** Task scheduler for making sure that old cache objects are cleared. */
  private ThreadPoolTaskScheduler taskScheduler;
  
  /**
   * Gets an in-memory IntegrationServiceStateCache bean.
   * 
   * @return an in-memory IntegrationServiceStateCache bean
   */
  @ConditionalOnMissingBean
  @Bean
  public IntegrationServiceStateCache integrationServiceStateCache() {
    final InMemoryIntegrationServiceStateCache cache = new InMemoryIntegrationServiceStateCache();
    cache.setMaxAge(this.maxStateCacheAge);

    // Start cache cleanup task ...
    this.initTaskScheduler();
    final PeriodicTrigger trigger = new PeriodicTrigger(this.cleanupIntervalStateCache);
    trigger.setInitialDelay(this.cleanupIntervalStateCache);
    this.taskScheduler.schedule(new PurgeExpiredObjectsTask(cache), trigger);

    return cache;
  }

  /**
   * Gets an in-memory DocumentCache bean
   * 
   * @return an in-memory DocumentCache bean
   */
  @ConditionalOnMissingBean
  @Bean
  public DocumentCache docmentCache() {
    final InMemoryDocumentCache cache = new InMemoryDocumentCache();
    cache.setMaxAge(this.maxDocumentCacheAge);
    
    // Start cache cleanup task ...
    this.initTaskScheduler();
    final PeriodicTrigger trigger = new PeriodicTrigger(this.cleanupIntervalDocumentCache);
    trigger.setInitialDelay(this.cleanupIntervalDocumentCache);
    this.taskScheduler.schedule(new PurgeExpiredObjectsTask(cache), trigger);    
    
    return cache;
  }

  /**
   * Initializes the task scheduler needed for the purge tasks.
   */
  private void initTaskScheduler() {
    if (this.taskScheduler == null) {
      this.taskScheduler = new ThreadPoolTaskScheduler();
      this.taskScheduler.setPoolSize(2);
      this.taskScheduler.setThreadNamePrefix("cache-scheduler");
      this.taskScheduler.initialize();
    }
  }

  /**
   * Task for purging caches.
   */
  @Slf4j
  private static class PurgeExpiredObjectsTask implements Runnable {

    /** The cache to purge. */
    private final IntegrationServiceCache<?> cache;

    /**
     * Constructor.
     * 
     * @param cache the cache to purge
     */
    public PurgeExpiredObjectsTask(IntegrationServiceCache<?> cache) {
      this.cache = cache;
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
      log.trace("Clearing expired objects from {} cache ...", this.cache.getClass().getSimpleName());
      this.cache.clearExpired();
    }

  }

  /** {@inheritDoc} */
  @Override
  public void destroy() throws Exception {
    if (this.taskScheduler != null) {
      this.taskScheduler.shutdown();
    }
  }

}
