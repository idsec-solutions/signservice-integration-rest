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
package se.idsec.signservice.integration.rest.controllers.error;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Represents the data structure that is used as the body for API errors.
 * 
 * @author Martin Lindström (martin@litsec.se)
 */
@JsonInclude(Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SignServiceIntegrationApiError {

  /** The HTTP status. */
  private int status;
  
  /** The timestamp. */  
  private long timestamp;
  
  /** The error code. */
  private String errorCode;
  
  /** Validation error details. */
  private ValidationError validationError;
  
  /** DSS error details. */
  private DssError dssError;
  
  /** The error message. */
  private String message;
  
  /** The requested path. */
  private String path;

  /**
   * For validation errors.
   */
  @Data
  @Builder
  public static class ValidationError {
    
    /** The object that validation failed for. */
    private String object;
    
    /** Underlying errors (field names and error messages). */
    private Map<String, String> details;
  }
  
  /**
   * For DSS errors.
   */
  @Data
  @Builder
  public static class DssError {
    
    /** The DSS major result code. */
    private String majorCode;

    /** The DSS minor result code. */
    private String minorCode;
  }

}
