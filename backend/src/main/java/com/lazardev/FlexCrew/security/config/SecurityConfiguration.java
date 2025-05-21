package com.lazardev.FlexCrew.security.config;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

  private final JwtAuthenticationFilter jwtAuthFilter;
  private final AuthenticationProvider authenticationProvider;

  // Paths allowed without authentication
  private static final String[] WHITE_LIST_URL = {
      "/api/auth/**",
      "/v2/api-docs",
      "/v3/api-docs",
      "/v3/api-docs/**",
      "/swagger-resources",
      "/swagger-resources/**",
      "/configuration/ui",
      "/configuration/security",
      "/swagger-ui/**",
      "/webjars/**",
      "/swagger-ui.html",
      "/error"
  };

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(req -> req
            .requestMatchers(WHITE_LIST_URL).permitAll()
            .requestMatchers(HttpMethod.GET, "/api/v1/projects", "/api/v1/projects/**").permitAll()
            .requestMatchers("/api/v1/management/**").hasAnyRole("ADMIN", "MANAGER")
            .requestMatchers(HttpMethod.GET, "/api/v1/management/**").hasAnyAuthority("admin:read", "manager:read")
            .requestMatchers(HttpMethod.POST, "/api/v1/management/**").hasAnyAuthority("admin:create", "manager:create")
            .requestMatchers(HttpMethod.PUT, "/api/v1/management/**").hasAnyAuthority("admin:update", "manager:update")
            .requestMatchers(HttpMethod.DELETE, "/api/v1/management/**")
            .hasAnyAuthority("admin:delete", "manager:delete")
            .anyRequest().authenticated())
        .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
        .authenticationProvider(authenticationProvider)
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsFilter corsFilter() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.addAllowedOrigin("http://localhost:5173");
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");
    source.registerCorsConfiguration("/api/**", config);
    return new CorsFilter(source);
  }
}
