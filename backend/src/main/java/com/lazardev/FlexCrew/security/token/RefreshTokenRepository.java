package com.lazardev.FlexCrew.security.token;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

  Optional<RefreshToken> findByTokenHash(String tokenHash);

  /**
   * Revokes the token only if it is still active and returns how many rows changed. A single
   * conditional UPDATE is atomic, so of two requests using the same token only one gets 1.
   */
  @Modifying
  @Query("update RefreshToken r set r.revoked = true where r.id = :id and r.revoked = false")
  int revokeIfActive(Integer id);
}
