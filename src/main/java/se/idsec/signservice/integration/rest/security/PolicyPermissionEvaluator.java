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
package se.idsec.signservice.integration.rest.security;

import java.io.Serializable;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

import lombok.extern.slf4j.Slf4j;

/**
 * A {@link PermissionEvaluator} that is used to check if a user has permission to use a particular SignService policy.
 * 
 * @author Martin Lindström (martin@litsec.se)
 */
@Slf4j
public class PolicyPermissionEvaluator implements PermissionEvaluator {
  
  /** Symbolic constant for the only known permission handled by this evaluator. */
  public static final String USE_PERMISSION = "use";

  /** {@inheritDoc} */
  @Override
  public boolean hasPermission(
      final Authentication authentication, final Object targetDomainObject, final Object permission) {
    
    if (authentication == null || targetDomainObject == null || permission == null) {
      return false;
    }
    if (!String.class.isInstance(permission)) {
      log.error("Unknown permission type: {}", permission.getClass().getSimpleName());
      return false;
    }
    if (!String.class.cast(permission).equalsIgnoreCase(USE_PERMISSION)) {
      log.error("Unknown permission: {}", permission);
      return false;
    }
    if (!String.class.isInstance(targetDomainObject)) {
      log.error("Unknown targetDomainObject type: {}", targetDomainObject.getClass().getSimpleName());
      return false;
    }
    
    return AccessControlUtils.hasPolicyAuthority.test(authentication, String.class.cast(targetDomainObject));
  }

  /** {@inheritDoc} */
  @Override
  public boolean hasPermission(
      final Authentication authentication, final Serializable targetId, final String targetType, final Object permission) {
    
    return false;
  }

}
