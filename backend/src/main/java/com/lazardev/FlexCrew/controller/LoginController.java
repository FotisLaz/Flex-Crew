package com.lazardev.FlexCrew.controller;

import com.lazardev.FlexCrew.security.auth.AuthenticationRequest;
import com.lazardev.FlexCrew.security.auth.AuthenticationResponse;
import com.lazardev.FlexCrew.security.auth.AuthenticationService;
import com.lazardev.FlexCrew.security.auth.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

  private AuthenticationService authenticationService;

  @Autowired
  public LoginController(AuthenticationService theService) {
    this.authenticationService = theService;
  }

  @PostMapping("/register")
  public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
    return ResponseEntity.ok(authenticationService.register(request));
  }

  @PostMapping("/authenticate")
  public ResponseEntity<AuthenticationResponse> authenticate(
      @RequestBody AuthenticationRequest request) {
    return ResponseEntity.ok(authenticationService.authenticate(request));
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<AuthenticationResponse> refreshToken(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
    return ResponseEntity.ok(authenticationService.refreshToken(authHeader));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
    authenticationService.logout(authHeader);
    return ResponseEntity.noContent().build();
  }
}
