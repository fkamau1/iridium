/*
 *  Copyright (C) Josh Fischer - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by Josh Fischer <josh@joshfischer.io>, 2023.
 */
package software.iridium.api.controller;

import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import software.iridium.api.authentication.domain.AccessTokenResponse;
import software.iridium.api.authentication.domain.ApplicationAuthorizationFormRequest;
import software.iridium.api.authentication.domain.IdentityResponse;
import software.iridium.api.service.AuthorizationService;

/**
 * Create a log-in link with the app's client ID, redirect URL, state, and PKCE code challenge
 * parameters The user sees the authorization prompt and approves the request The user is redirected
 * back to the app's server with an auth code • The app exchanges the auth code for an access
 * token @RequestParam(name = "scope") final String scope, @RequestParam(name = "response_type")
 * final String responseType, @RequestParam(name = "client_id") final String
 * clientId, @RequestParam(name = "redirect_uri") final String redirectUri, @RequestParam(name =
 * "state") final String state, @RequestParam(name = "code_challenge") final String
 * codeChallenge, @RequestParam(name = "code_challenge_method") final String codeChallengeMethod
 */
@CrossOrigin
@RestController
public class AuthorizationController {

  private static final Logger logger = LoggerFactory.getLogger(AuthorizationController.class);

  @Resource private AuthorizationService authorizationService;

  @GetMapping(value = "/oauth/change-mel/authorize/", produces = IdentityResponse.MEDIA_TYPE)
  public RedirectView completeAuthorizationWithProvider(
      @RequestParam(name = "code") final String code,
      @RequestParam(name = "client_id") final String clientId,
      @RequestParam(name = "state") final String state,
      @RequestParam(name = "provider_name") final String providerName) {

    logger.info("authorizing with provider {} for client {}", providerName, clientId);
    authorizationService.completeAuthorizationWithProvider(code, providerName, clientId, state);
    return new RedirectView();
  }

  @PostMapping(value = "/oauth/external/authorize")
  public RedirectView proxyAuthorizationRequestToProvider(
      ModelMap model,
      RedirectAttributes redirectAttributes,
      @RequestParam(value = "response_type", required = false) final String responseType,
      @RequestParam(value = "state", required = false) final String state,
      @RequestParam(value = "redirect_uri", required = false) final String redirectUri,
      @RequestParam(value = "client_id", required = false) final String clientId,
      @RequestParam(value = "provider", required = false) final String provider) {

    final var redirectDestination =
        authorizationService.proxyAuthorizationRequestToProvider(
            responseType, state, redirectUri, clientId, provider);
    RedirectView redirectView = new RedirectView();
    redirectView.setUrl(redirectDestination);
    return redirectView;
  }

  @RequestMapping(
      value = "/oauth/authorize",
      method = {RequestMethod.POST, RequestMethod.GET})
  public RedirectView authorize(
      HttpServletRequest servletRequest,
      final ApplicationAuthorizationFormRequest formRequest,
      @RequestParam Map<String, String> params) {
    logger.info("initiating authorization");
    logger.info("params {}", params);
    final var redirectUri = authorizationService.authorize(formRequest, params, servletRequest);
    RedirectView redirectView = new RedirectView();
    redirectView.setUrl(redirectUri);
    return redirectView;
  }

  @PostMapping(value = "/oauth/token")
  public AccessTokenResponse exchange(
      HttpServletRequest servletRequest, @RequestParam Map<String, String> params) {
    logger.info("finalizing authorization");
    return authorizationService.exchange(servletRequest, params);
  }
}