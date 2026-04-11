package com.womensocial.app.repository;

import com.womensocial.app.model.entity.FaceVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface FaceVerificationTokenRepository extends JpaRepository<FaceVerificationToken, Long> {

    Optional<FaceVerificationToken> findByToken(String token);

    @Modifying
    @Query("DELETE FROM FaceVerificationToken t WHERE t.expiresAt < :now")
    void deleteExpiredTokens(LocalDateTime now);
}
