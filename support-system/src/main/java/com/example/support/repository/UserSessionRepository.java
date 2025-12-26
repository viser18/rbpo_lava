package com.example.support.repository;

import com.example.support.entity.SessionStatus;
import com.example.support.entity.User;
import com.example.support.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findByRefreshToken(String refreshToken);

    List<UserSession> findByUserIdAndStatus(Long userId, SessionStatus status);

    List<UserSession> findByUserAndStatus(User user, SessionStatus status);

    Optional<UserSession> findByRefreshTokenAndStatus(String refreshToken, SessionStatus status);

    @Query("SELECT us FROM UserSession us WHERE us.user.id = :userId AND us.status = 'ACTIVE' AND us.expiresAt > :now")
    List<UserSession> findActiveSessionsByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Transactional
    @Modifying
    @Query("UPDATE UserSession us SET us.status = 'EXPIRED' WHERE us.expiresAt < :now AND us.status = 'ACTIVE'")
    int expireOldSessions(@Param("now") LocalDateTime now);

    @Transactional
    @Modifying
    @Query("UPDATE UserSession us SET us.status = 'REVOKED' WHERE us.user.id = :userId AND us.status = 'ACTIVE'")
    int revokeAllUserSessions(@Param("userId") Long userId);
}