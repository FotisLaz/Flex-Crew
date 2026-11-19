package com.lazardev.FlexCrew.security.auth;

import com.lazardev.FlexCrew.dao.EmployeeRepository;
import com.lazardev.FlexCrew.entity.Employee;
import com.lazardev.FlexCrew.entity.Role;
import com.lazardev.FlexCrew.security.config.JwtService;
import com.lazardev.FlexCrew.security.token.RefreshToken;
import com.lazardev.FlexCrew.security.token.RefreshTokenRepository;
import com.lazardev.FlexCrew.security.token.Token;
import com.lazardev.FlexCrew.security.token.TokenRepository;
import com.lazardev.FlexCrew.security.token.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthenticationService {

  private EmployeeRepository repository;
  private TokenRepository tokenRepository;
  private RefreshTokenRepository refreshTokenRepository;
  private PasswordEncoder passwordEncoder;
  private JwtService jwtService;
  private AuthenticationManager authenticationManager;

  @Autowired
  public AuthenticationService(
      EmployeeRepository theEmployeeRepo,
      TokenRepository theTokenRepo,
      RefreshTokenRepository theRefreshTokenRepo,
      PasswordEncoder thePasswordEncoder,
      JwtService theJwtservice,
      AuthenticationManager theAuthenticationManager) {
    this.repository = theEmployeeRepo;
    this.tokenRepository = theTokenRepo;
    this.refreshTokenRepository = theRefreshTokenRepo;
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
    String refreshToken = issueRefreshToken(savedUser);
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
    String refreshToken = issueRefreshToken(user);
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

  // noRollbackFor: the 401 thrown on reuse must not roll back the revocations made just before it.
  @Transactional(noRollbackFor = ResponseStatusException.class)
  public AuthenticationResponse refreshToken(String authHeader) {
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      throw invalidRefreshToken(null);
    }
    String refreshToken = authHeader.substring(7);
    RefreshToken stored;
    try {
      // Parsing checks the signature and expiry; only a token we issued and stored is accepted,
      // which also rejects access tokens sent here.
      String userEmail = jwtService.extractUsername(refreshToken);
      stored =
          refreshTokenRepository
              .findByTokenHash(sha256(refreshToken))
              .filter(t -> t.getEmployee().getEmail().equals(userEmail))
              .orElseThrow(() -> invalidRefreshToken(null));
    } catch (JwtException | IllegalArgumentException e) {
      throw invalidRefreshToken(e);
    }
    Employee user = stored.getEmployee();
    if (refreshTokenRepository.revokeIfActive(stored.getId()) == 0) {
      // A revoked token was used again, so it was likely copied: end every session of this user.
      refreshTokenRepository.revokeAllByEmployeeId(user.getId());
      revokeAllUserTokens(user);
      throw invalidRefreshToken(null);
    }
    String accessToken = jwtService.generateToken(user);
    revokeAllUserTokens(user);
    saveUserToken(user, accessToken);
    return AuthenticationResponse.builder()
        .accessToken(accessToken)
        .refreshToken(issueRefreshToken(user))
        .role(user.getRole())
        .build();
  }

  private String issueRefreshToken(Employee user) {
    String refreshToken = jwtService.generateRefreshToken(user);
    refreshTokenRepository.save(
        RefreshToken.builder()
            .employee(user)
            .tokenHash(sha256(refreshToken))
            .expiresAt(jwtService.extractClaim(refreshToken, Claims::getExpiration).toInstant())
            .revoked(false)
            .build());
    return refreshToken;
  }

  // A fast, unsalted hash is enough: the token is long and random, so it can't be brute-forced,
  // and the same input must give the same hash so the token can be looked up by it (BCrypt can't).
  private static String sha256(String token) {
    try {
      byte[] hash =
          MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is not available", e);
    }
  }

  private static ResponseStatusException invalidRefreshToken(Throwable cause) {
    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token", cause);
  }
}
