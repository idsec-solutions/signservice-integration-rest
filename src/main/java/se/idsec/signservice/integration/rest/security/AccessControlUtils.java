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
package se.idsec.signservice.integration.rest.security;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.springframework.security.core.Authentication;

/**
 * Utility methods for checking access to resources.
 * 
 * @author Martin Lindström
 */
public class AccessControlUtils {

  // Hidden constructor
  private AccessControlUtils() {
  }

  public static Predicate<Authentication> isAdmin = a -> a.getAuthorities().stream()
    .filter(ga -> ga.getAuthority().equals("ROLE_ADMIN"))
    .findFirst()
    .isPresent();

  public static BiPredicate<Authentication, String> hasPolicyAuthority = (a, p) -> isAdmin.test(a) ||
      a.getAuthorities().stream()
        .filter(ga -> ga.getAuthority().equals("POLICY_" + p.toLowerCase()))
        .findFirst()
        .isPresent();

}
