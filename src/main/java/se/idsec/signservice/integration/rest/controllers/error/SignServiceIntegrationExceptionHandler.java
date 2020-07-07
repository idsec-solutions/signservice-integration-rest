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

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import se.idsec.signservice.integration.SignResponseCancelStatusException;
import se.idsec.signservice.integration.SignResponseErrorStatusException;
import se.idsec.signservice.integration.core.error.BadRequestException;
import se.idsec.signservice.integration.core.error.ErrorCode;
import se.idsec.signservice.integration.core.error.InputValidationException;
import se.idsec.signservice.integration.core.error.SignServiceIntegrationException;
import se.idsec.signservice.integration.rest.controllers.error.SignServiceIntegrationApiError.DssError;
import se.idsec.signservice.integration.rest.controllers.error.SignServiceIntegrationApiError.ValidationError;

/**
 * Exception handler for the Sign Service Integration service.
 * 
 * @author Martin Lindström (martin@litsec.se)
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class SignServiceIntegrationExceptionHandler extends ResponseEntityExceptionHandler {

  /**
   * Handles exceptions from the SignService Integration API.
   * 
   * @param ex
   *          the exception
   * @param request
   *          the web request
   * @return a response entity
   */
  @ExceptionHandler(SignServiceIntegrationException.class)
  protected ResponseEntity<Object> handleSignServiceIntegrationException(
      final SignServiceIntegrationException ex, final WebRequest request) {

    HttpStatus status = HttpStatus.resolve(ex.getHttpStatus());
    if (status == null) {
      status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    SignServiceIntegrationApiError body = SignServiceIntegrationApiError.builder()
      .status(status.value())
      .timestamp(System.currentTimeMillis())
      .errorCode(ex.getErrorCode().getErrorCode())
      .message(ex.getMessage())
      .path(this.getPath(request))
      .build();
    if (InputValidationException.class.isInstance(ex)) {
      body.setValidationError(ValidationError.builder()
        .object(((InputValidationException) ex).getObjectName())
        .details((((InputValidationException) ex).getDetails()))
        .build());
    }

    return this.handleExceptionInternal(ex, body, new HttpHeaders(), status, request);
  }

  /**
   * Handles DSS errors from the SignService Integration API.
   * 
   * @param ex
   *          the exception
   * @param request
   *          the web request
   * @return a response entity
   */  
  @ExceptionHandler(SignResponseErrorStatusException.class)
  protected ResponseEntity<Object> handleDssError(
      final SignResponseErrorStatusException ex, final WebRequest request) {

    final String errorCode = SignResponseCancelStatusException.class.isInstance(ex) ? "dss.cancel" : "dss.error";

    SignServiceIntegrationApiError body = SignServiceIntegrationApiError.builder()
      .status(HttpStatus.BAD_REQUEST.value())
      .timestamp(System.currentTimeMillis())
      .errorCode(errorCode)
      .dssError(DssError.builder()
        .majorCode(ex.getMajorCode())
        .minorCode(ex.getMinorCode())
        .build())
      .message(ex.getMessage())
      .path(this.getPath(request))
      .build();

    return this.handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
  }

  /** {@inheritDoc} */
  @Override
  protected ResponseEntity<Object> handleExceptionInternal(
      final Exception ex, Object body, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {

    if (body == null) {
      ErrorCode errorCode = null;
      if (HttpStatus.BAD_REQUEST.equals(status)) {
        errorCode = new ErrorCode(BadRequestException.BAD_REQUEST_ERROR_CATEGORY.getCategory(), "invalid-call");
      }
      else {
        errorCode = new ErrorCode("internal", "invalid-call");
      }

      body = SignServiceIntegrationApiError.builder()
        .status(status.value())
        .timestamp(System.currentTimeMillis())
        .errorCode(errorCode.getErrorCode())
        .message(ex.getMessage())
        .path(this.getPath(request))
        .build();
    }
    return super.handleExceptionInternal(ex, body, headers, status, request);
  }

  /**
   * Gets the path attribute.
   * 
   * @param request
   *          the request
   * @return the path attribute
   */
  private String getPath(final WebRequest request) {
    return ((ServletWebRequest) request).getRequest().getServletPath();
  }

}
