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
package se.idsec.signservice.integration.rest.controllers.error;

import java.util.Date;
import java.util.Map;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import se.idsec.signservice.integration.core.error.BadRequestException;
import se.idsec.signservice.integration.core.error.ErrorCode;
import se.idsec.signservice.integration.core.error.NoAccessException;

/**
 * Handles errors that occur outside of the controllers.
 *
 * @author Martin Lindström
 */
@Component
public class CustomErrorAttributes extends DefaultErrorAttributes {

  /** {@inheritDoc} */
  @Override
  public Map<String, Object> getErrorAttributes(final WebRequest webRequest, final ErrorAttributeOptions options) {
    Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, options);
    errorAttributes.remove("error");

    final Date timestamp = (Date) errorAttributes.get("timestamp");
    if (timestamp != null) {
      errorAttributes.put("timestamp", Long.valueOf(timestamp.getTime()));
    }
    else {
      errorAttributes.put("timestamp", Long.valueOf(System.currentTimeMillis()));
    }

    Integer status = (Integer) errorAttributes.get("status");
    if (status != null && HttpStatus.BAD_REQUEST.value() == status.intValue() || HttpStatus.NOT_FOUND.value() == status.intValue()) {
      errorAttributes.put("errorCode",
        (new ErrorCode(BadRequestException.BAD_REQUEST_ERROR_CATEGORY.getCategory(), "invalid-call")).getErrorCode());
    }
    else if (status != null && HttpStatus.FORBIDDEN.value() == status.intValue()) {
      errorAttributes.put("errorCode", NoAccessException.ERROR_CODE.getErrorCode());
    }
    else {
      errorAttributes.put("errorCode", ErrorCode.ERROR_CODE_PREFIX + "internal.invalid-call");
    }

    errorAttributes.put("errorCode", ErrorCode.ERROR_CODE_PREFIX + "internal.invalid-call");
    return errorAttributes;
  }

}
