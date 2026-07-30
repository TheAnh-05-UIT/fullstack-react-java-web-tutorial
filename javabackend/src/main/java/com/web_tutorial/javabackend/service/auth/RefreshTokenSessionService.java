package com.web_tutorial.javabackend.service.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.web_tutorial.javabackend.domain.user.RefreshTokenSession;
import com.web_tutorial.javabackend.domain.user.User;
import com.web_tutorial.javabackend.exception.InvalidRefreshTokenException;
import com.web_tutorial.javabackend.repository.user.RefreshTokenSessionRepository;
import com.web_tutorial.javabackend.service.security.RefreshTokenHasher;

import jakarta.annotation.PostConstruct;

@Service
public class RefreshTokenSessionService {

    public static final String FAMILY_CLAIM = "family_id";
    private static final String INVALID_TOKEN_MESSAGE =
            "Refresh token không hợp lệ hoặc đã hết hạn.";
    private static final String REUSE_REASON = "TOKEN_REUSE";
    private static final String LOGOUT_REASON = "LOGOUT_ALL";

    private final RefreshTokenSessionRepository sessionRepository;
    private final RefreshTokenHasher tokenHasher;

    @Value("${javabackend.jwt.refresh-concurrency-grace:3s}")
    private Duration concurrencyGrace;

    public RefreshTokenSessionService(RefreshTokenSessionRepository sessionRepository,
            RefreshTokenHasher tokenHasher) {
        this.sessionRepository = sessionRepository;
        this.tokenHasher = tokenHasher;
    }

    @PostConstruct
    void validateConcurrencyGrace() {
        if (concurrencyGrace.isNegative() || concurrencyGrace.compareTo(Duration.ofSeconds(10)) > 0) {
            throw new IllegalStateException(
                    "Refresh concurrency grace must be between 0 and 10 seconds");
        }
    }

    @Transactional
    public RefreshTokenSession create(User user, String rawToken, Jwt jwt) {
        RefreshTokenSession session = new RefreshTokenSession();
        session.setUser(user);
        session.setFamilyId(requiredClaim(jwt, FAMILY_CLAIM));
        session.setTokenHash(tokenHasher.hash(rawToken));
        session.setCurrentJti(requiredJwtId(jwt));
        session.setExpiresAt(jwt.getExpiresAt());
        return sessionRepository.save(session);
    }

    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public RefreshTokenSession validateForRotation(String rawToken, Jwt jwt) {
        String familyId = requiredClaim(jwt, FAMILY_CLAIM);
        RefreshTokenSession session = sessionRepository.findByFamilyIdForUpdate(familyId)
                .orElseThrow(() -> invalid());

        if (session.isRevoked()) {
            throw invalid();
        }

        Instant now = Instant.now();
        String incomingJti = requiredJwtId(jwt);
        String incomingHash = tokenHasher.hash(rawToken);
        boolean sameOwner = session.getUser().getEmail().equals(jwt.getSubject());
        boolean active = session.getExpiresAt().isAfter(now);
        boolean currentToken = session.getCurrentJti().equals(incomingJti)
                && hashesMatch(session.getTokenHash(), incomingHash);

        if (currentToken && sameOwner && active) {
            return session;
        }

        if (sameOwner && isConcurrentDuplicate(session, incomingJti, incomingHash, now)) {
            throw invalid();
        }

        session.setRevokedAt(now);
        session.setRevokeReason(REUSE_REASON);
        sessionRepository.saveAndFlush(session);
        throw invalid();
    }

    @Transactional
    public void rotate(RefreshTokenSession session, String newRawToken, Jwt newJwt) {
        session.setPreviousTokenHash(session.getTokenHash());
        session.setPreviousJti(session.getCurrentJti());
        session.setPreviousConsumedAt(Instant.now());
        session.setTokenHash(tokenHasher.hash(newRawToken));
        session.setCurrentJti(requiredJwtId(newJwt));
        session.setExpiresAt(newJwt.getExpiresAt());
        session.setReplacedByJti(newJwt.getId());
        sessionRepository.saveAndFlush(session);
    }

    @Transactional
    public void revokeAll(User user) {
        sessionRepository.revokeActiveSessionsForUser(user.getId(), Instant.now(), LOGOUT_REASON);
    }

    private boolean isConcurrentDuplicate(RefreshTokenSession session, String incomingJti,
            String incomingHash, Instant now) {
        return session.getPreviousConsumedAt() != null
                && !now.isAfter(session.getPreviousConsumedAt().plus(concurrencyGrace))
                && incomingJti.equals(session.getPreviousJti())
                && hashesMatch(session.getPreviousTokenHash(), incomingHash);
    }

    private boolean hashesMatch(String storedHash, String incomingHash) {
        return storedHash != null && MessageDigest.isEqual(
                storedHash.getBytes(StandardCharsets.UTF_8),
                incomingHash.getBytes(StandardCharsets.UTF_8));
    }

    private String requiredClaim(Jwt jwt, String claim) {
        String value = jwt.getClaimAsString(claim);
        if (value == null || value.isBlank()) {
            throw invalid();
        }
        return value;
    }

    private String requiredJwtId(Jwt jwt) {
        if (jwt.getId() == null || jwt.getId().isBlank()) {
            throw invalid();
        }
        return jwt.getId();
    }

    private InvalidRefreshTokenException invalid() {
        return new InvalidRefreshTokenException(INVALID_TOKEN_MESSAGE);
    }
}
