package com.lazardev.FlexCrew.security.auth;

import com.lazardev.FlexCrew.dao.EmployeeRepository;
import com.lazardev.FlexCrew.entity.Employee;
import com.lazardev.FlexCrew.entity.Role;
import com.lazardev.FlexCrew.security.config.JwtService;
import com.lazardev.FlexCrew.security.token.Token;
import com.lazardev.FlexCrew.security.token.TokenRepository;
import com.lazardev.FlexCrew.security.token.TokenType;
import io.jsonwebtoken.JwtException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthenticationService {

  private EmployeeRepository repository;
  private TokenRepository tokenRepository;
  private PasswordEncoder passwordEncoder;
  private JwtService jwtService;
  private AuthenticationManager authenticationManager;

  @Autowired
  public AuthenticationService(
      EmployeeRepository theEmployeeRepo,
      TokenRepository theTokenRepo,
      PasswordEncoder thePasswordEncoder,
      JwtService theJwtservice,
      AuthenticationManager theAuthenticationManager) {
    this.repository = theEmployeeRepo;
    this.tokenRepository = theTokenRepo;
    this.passwordEncoder = thePasswordEncoder;
    this.jwtService = theJwtservice;
    this.authenticationManager = theAuthenticationManager;
  }

  public AuthenticationResponse register(RegisterRequest request) {
    if (repository.findByEmail(request.getEmail()).isPresent()) {
      throw new RuntimeException("Email already exists");
    }
    Employee user = Employee.builder()
        .names(request.getNames())
        .firstSurname(request.getFirstSurname())
        .secondSurname(request.getSecondSurname())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .role(Role.USER) // never trust a role from the client
        .build();

    Employee savedUser = repository.save(user);
    String jwtToken = jwtService.generateToken(user);
    String refreshToken = jwtService.generateRefreshToken(user);
    saveUserToken(savedUser, jwtToken);
    return AuthenticationResponse.builder()
        .accessToken(jwtToken)
        .refreshToken(refreshToken)
        .role(savedUser.getRole())
        .build();
  }

  public AuthenticationResponse authenticate(AuthenticationRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
    Employee user = repository.findByEmail(request.getEmail()).orElseThrow();
    String jwtToken = jwtService.generateToken(user);
    String refreshToken = jwtService.generateRefreshToken(user);
    revokeAllUserTokens(user);
    saveUserToken(user, jwtToken);
    return AuthenticationResponse.builder()
        .accessToken(jwtToken)
        .refreshToken(refreshToken)
        .role(user.getRole())
        .build();
  }

  @Override
  public String toString() {
    return "AuthenticationService [ নামের (" + repository + ")]";
  }

  private void saveUserToken(Employee user, String jwtToken) {
    Token token = Token.builder()
        .employee(user)
        .token(jwtToken)
        .tokenType(TokenType.BEARER)
        .expired(false)
        .revoked(false)
        .build();
    tokenRepository.save(token);
  }

  private void revokeAllUserTokens(Employee user) {
    List<Token> validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
    if (validUserTokens.isEmpty()) {
      return;
    }
    // Set expired and revoked to true for old tokens
    validUserTokens.forEach(
        token -> {
          token.setExpired(true);
          token.setRevoked(true);
        });
    tokenRepository.saveAll(validUserTokens);
  }

  public AuthenticationResponse refreshToken(String authHeader) {
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      throw invalidRefreshToken(null);
    }
    String refreshToken = authHeader.substring(7);
    Employee user;
    try {
      String userEmail = jwtService.extractUsername(refreshToken);
      user = repository.findByEmail(userEmail).orElseThrow(() -> invalidRefreshToken(null));
      if (!jwtService.isTokenValid(refreshToken, user)) {
        throw invalidRefreshToken(null);
      }
    } catch (JwtException | IllegalArgumentException e) {
      throw invalidRefreshToken(e);
    }
    String accessToken = jwtService.generateToken(user);
    revokeAllUserTokens(user);
    saveUserToken(user, accessToken);
    return AuthenticationResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .role(user.getRole())
        .build();
  }

  private static ResponseStatusException invalidRefreshToken(Throwable cause) {
    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token", cause);
  }
}
