package com.lazardev.FlexCrew.security.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

  private String names;
  private String firstSurname;
  private String secondSurname;
  private String email;
  private String password;
  // No role field: self-registration always creates a USER (see AuthenticationService).
}
