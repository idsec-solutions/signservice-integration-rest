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
package se.idsec.signservice.integration.rest.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import se.idsec.signservice.integration.SignRequestData;
import se.idsec.signservice.integration.SignRequestInput;
import se.idsec.signservice.integration.SignResponseCancelStatusException;
import se.idsec.signservice.integration.SignResponseErrorStatusException;
import se.idsec.signservice.integration.SignServiceIntegrationService;
import se.idsec.signservice.integration.SignatureResult;
import se.idsec.signservice.integration.config.IntegrationServiceDefaultConfiguration;
import se.idsec.signservice.integration.config.PolicyNotFoundException;
import se.idsec.signservice.integration.core.error.BadRequestException;
import se.idsec.signservice.integration.core.error.ErrorCode;
import se.idsec.signservice.integration.core.error.InputValidationException;
import se.idsec.signservice.integration.core.error.SignServiceIntegrationException;

/**
 * Main controller for the Sign Service Integration service.
 * 
 * @author Martin Lindström (martin@litsec.se)
 */
@RestController
@RequestMapping("v1")
@Slf4j
class SignServiceIntegrationController {

  /** The service logic. */
  @Setter
  @Autowired
  private SignServiceIntegrationService signServiceIntegrationService;

  /**
   * Endpoint for creating the sign request data, i.e., called to obtain the data needed to initiate a signature
   * operation.
   * 
   * @param request
   *          the HTTP servlet request
   * @param authentication
   *          the user authentication object
   * @param policy
   *          the policy for the operation
   * @param signRequestInput
   *          the sign request input
   * @return a SignRequestData object
   * @throws InputValidationException
   *           if the provided input does not validate correctly
   * @throws SignServiceIntegrationException
   *           for processing errors
   */
  @PreAuthorize("hasPermission(#policy, 'use')")
  @PostMapping(value = "/create/{policy}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public SignRequestData createSignRequest(
      final HttpServletRequest request,
      final Authentication authentication,
      @PathVariable("policy") final String policy,
      @RequestBody final SignRequestInput signRequestInput)
      throws InputValidationException, SignServiceIntegrationException {

    log.debug("Processing POST request '{}' [user='{}', client-ip'{}']",
      request.getServletPath(), authentication.getName(), request.getRemoteAddr());

    log.debug("Processing POST request '{}' from '{}'", request.getServletPath(), request.getRemoteAddr());

    // Check to ensure that the policy is correct ...
    //
    if (signRequestInput.getPolicy() == null) {
      signRequestInput.setPolicy(policy);
    }
    else if (signRequestInput.getPolicy().equals(policy)) {
      log.info("Bad policy ({}) in input passed to '{}' [user='{}', client-ip'{}']",
        policy, request.getServletPath(), authentication.getName(), request.getRemoteAddr());
      throw new InputValidationException("policy", "Supplied policy in input does not match URI");
    }
    // Invoke the API implementation and create a SignRequestData ...
    //
    final SignRequestData signRequestData = this.signServiceIntegrationService.createSignRequest(signRequestInput);

    return signRequestData;
  }

  /**
   * Endpoint that gets a {@link SignatureResult} based on the SignResponse data that was received from the sign
   * service.
   * 
   * @param request
   *          the HTTP servlet request
   * @param authentication
   *          the user authentication object
   * @param id
   *          the ID for the operation (corresponds to the RelayState parameter that was received along with the
   *          SignResponse)
   * @param processSignResponseInput
   *          the input (holds the SignResponse along with the state)
   * @return a SignatureResult
   * @throws SignResponseCancelStatusException
   *           if the SignService reported that the user cancelled the signing operation
   * @throws SignResponseErrorStatusException
   *           if the SignService reported an error during processing of the SignRequest
   * @throws SignServiceIntegrationException
   *           for processing errors
   */
  @GetMapping(value = "/result/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public SignatureResult processSignResponse(
      final HttpServletRequest request,
      final Authentication authentication,
      @PathVariable("id") final String id,
      @RequestBody final ProcessSignResponseInput processSignResponseInput)
      throws SignResponseCancelStatusException, SignResponseErrorStatusException, SignServiceIntegrationException {

    log.debug("Processing GET request '{}' [user='{}', client-ip'{}']",
      request.getServletPath(), authentication.getName(), request.getRemoteAddr());

    // Make sure the 'id' is correct ...
    //
    if (processSignResponseInput.getState() == null || processSignResponseInput.getState().getId() == null) {
      final String msg = String.format("Can not process SignResponse with ID '%s' - No state is available", id);
      log.info("{} [user='{}', client-ip'{}']", msg, authentication.getName(), request.getRemoteAddr());
      throw new BadRequestException(new ErrorCode.Code("session"), msg);
    }
    if (!processSignResponseInput.getState().getId().equals(id)) {
      final String msg = String.format("Bad state ('%s') passed in call to '%s'", id, request.getServletPath());
      log.info("{} [user='{}', client-ip'{}']", msg, authentication.getName(), request.getRemoteAddr());
      throw new BadRequestException(new ErrorCode.Code("session"), msg);
    }

    // TODO: We need a way to ensure that the caller is the "owner" of the operation.

    // Invoke the API implementation and create a SignResponse.
    //
    final SignatureResult signatureResult = this.signServiceIntegrationService.processSignResponse(
      processSignResponseInput.getSignResponse(), id, processSignResponseInput.getState(), processSignResponseInput.getParameters());

    return signatureResult;
  }

  @PostFilter("hasPermission(filterObject, 'use')")
  @GetMapping(value = "/policy/list", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public List<String> listPolicies(final HttpServletRequest request, final Authentication authentication) {

    log.debug("Processing GET request '{}' [user='{}', client-ip'{}']",
      request.getServletPath(), authentication.getName(), request.getRemoteAddr());

    // Make a copy of the list - Spring security will filter it based on whether the user has permission
    // on all policies.
    //
    return new ArrayList<>(this.signServiceIntegrationService.getPolicies());
  }

  @PreAuthorize("hasPermission(#policy, 'use')")
  @GetMapping(value = "/policy/get/{policy}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public IntegrationServiceDefaultConfiguration getPolicy(
      final HttpServletRequest request,
      final Authentication authentication,
      @PathVariable("policy") final String policy) throws PolicyNotFoundException {

    log.debug("Processing GET request '{}' from '{}'", request.getServletPath(), request.getRemoteAddr());

    return this.signServiceIntegrationService.getConfiguration(policy);
  }

}
