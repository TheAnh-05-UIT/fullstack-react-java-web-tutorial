package com.web_tutorial.javabackend.service.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.web_tutorial.javabackend.config.SecurityConfiguration;

// Service xử lý sinh và giải mã JWT Token
@Service
public class SecurityService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder refreshJwtDecoder;
    private static final Set<String> ALLOWED_AUTHORITIES = Set.of("ROLE_USER", "ROLE_ADMIN");

    // Tiêm các cài đặt từ file application.properties

    @Value("${javabackend.jwt.access-token-validity-in-seconds}")
    private Long jwtExpiration;

    @Value("${javabackend.jwt.refresh-token-validity-in-seconds}")
    private Long refreshTokenExpiration;

    @Value("${javabackend.jwt.issuer:web-tutorial}")
    private String issuer;

    @Value("${javabackend.jwt.access-token-audience:webtutorial-api}")
    private String accessTokenAudience;

    @Value("${javabackend.jwt.refresh-token-audience:webtutorial-auth}")
    private String refreshTokenAudience;

    public SecurityService(JwtEncoder jwtEncoder,
            @Qualifier("refreshJwtDecoder") JwtDecoder refreshJwtDecoder) {
        this.jwtEncoder = jwtEncoder;
        this.refreshJwtDecoder = refreshJwtDecoder;
    }

    @PostConstruct
    void validateTokenValidityConfiguration() {
        if (jwtExpiration == null || jwtExpiration <= 0) {
            throw new IllegalStateException("Access token validity must be greater than zero");
        }
        if (refreshTokenExpiration == null || refreshTokenExpiration <= jwtExpiration) {
            throw new IllegalStateException("Refresh token validity must be greater than access token validity");
        }
    }

    // Tạo Access Token Ngắn hạn, chứa scope quyền
    public String generateAccessToken(Authentication authentication) {
        Instant now = Instant.now();
        Instant validity = now.plus(this.jwtExpiration, ChronoUnit.SECONDS);

        // Trích xuất danh sách Quyền của User từ Spring Security
        // quyền mặc định gắn tiền tố SCOPE_
        String scope = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(ALLOWED_AUTHORITIES::contains)
                .distinct()
                .sorted()
                .collect(Collectors.joining(" "));

        // Chèn dữ liệu vào (Payload)
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .audience(List.of(accessTokenAudience))
                .issuedAt(now)
                .expiresAt(validity)
                .subject(authentication.getName())
                .id(UUID.randomUUID().toString())
                .claim("token_type", "access")
                .claim("scope", scope)
                .build();

        // Tiến hành in thẻ với chữ ký chuẩn HS256
        return this.jwtEncoder.encode(
                JwtEncoderParameters.from(JwsHeader.with(SecurityConfiguration.JWT_ALGORITHM).build(), claims))
                .getTokenValue();
    }

    // Lấy username đang đăng nhập hiện tại từ Security Context
    public static Optional<String> getCurrentUserLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(extractPrincipal(securityContext.getAuthentication()));
    }

    // Tạo Refresh Token Dài hạn, không chứa scope
    public String generateRefreshToken(String email) {
        Instant now = Instant.now();
        Instant validity = now.plus(this.refreshTokenExpiration, ChronoUnit.SECONDS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .audience(List.of(refreshTokenAudience))
                .issuedAt(now)
                .expiresAt(validity)
                .subject(email)
                .id(UUID.randomUUID().toString())
                .claim("token_type", "refresh")
                .build();

        return this.jwtEncoder.encode(
                JwtEncoderParameters.from(JwsHeader.with(SecurityConfiguration.JWT_ALGORITHM).build(), claims))
                .getTokenValue();
    }

    // Giải mã JWT Token
    public Jwt decodeRefreshToken(String token) {
        return this.refreshJwtDecoder.decode(token);
    }

    private static String extractPrincipal(Authentication authentication) {
        if (authentication == null) {
            return null;
        } else if (authentication.getPrincipal() instanceof UserDetails springSecurityUser) {
            return springSecurityUser.getUsername();
        } else if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        } else if (authentication.getPrincipal() instanceof String s) {
            return s;
        }
        return null;
    }
}
