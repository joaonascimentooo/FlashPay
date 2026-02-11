package com.flashpay.backend.repository;

import com.flashpay.backend.domain.RefreshToken;
import com.flashpay.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    Optional<RefreshToken> findByToken(String token);

    @Query("SELECT rt FROM refresh_tokens rt WHERE rt.user = ?1 AND rt.revoked = false AND rt.expiryDate > CURRENT_TIMESTAMP")
    List<RefreshToken> findValidTokensByUser(User user);

    List<RefreshToken> findByUser(User user);

    @Query("DELETE FROM refresh_tokens rt WHERE rt.expiryDate < ?1")
    int deleteExpiredTokens(LocalDateTime expiryDate);

    @Query("DELETE FROM refresh_tokens rt WHERE rt.user = ?1 AND rt.revoked = true")
    int deleteRevokedTokensByUser(User user);

    @Query("SELECT COUNT(rt) FROM refresh_tokens rt WHERE rt.user = ?1 AND rt.revoked = false AND rt.expiryDate > CURRENT_TIMESTAMP")
    long countValidTokensByUser(User user);
}
