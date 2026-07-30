package com.web_tutorial.javabackend.repository.user;

import java.time.Instant;
import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.web_tutorial.javabackend.domain.user.RefreshTokenSession;

import jakarta.persistence.LockModeType;

@Repository
public interface RefreshTokenSessionRepository extends JpaRepository<RefreshTokenSession, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select session
            from RefreshTokenSession session
            join fetch session.user user
            left join fetch user.role
            where session.familyId = :familyId
            """)
    Optional<RefreshTokenSession> findByFamilyIdForUpdate(@Param("familyId") String familyId);

    Optional<RefreshTokenSession> findByFamilyId(String familyId);

    List<RefreshTokenSession> findAllByUserId(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update RefreshTokenSession session
            set session.revokedAt = :revokedAt, session.revokeReason = :reason
            where session.user.id = :userId and session.revokedAt is null
            """)
    int revokeActiveSessionsForUser(@Param("userId") Long userId,
            @Param("revokedAt") Instant revokedAt,
            @Param("reason") String reason);
}
