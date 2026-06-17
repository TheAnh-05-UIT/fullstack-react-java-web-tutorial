package com.web_tutorial.javabackend.service.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.web_tutorial.javabackend.config.SecurityConfiguration;

@Service
public class SecurityService {

    private final JwtEncoder jwtEncoder;

    @Value("${javabackend.jwt.base64-secret}")
    private String jwtKey;

    @Value("${javabackend.jwt.access-token-validity-in-seconds}")
    private Long jwtExpiration;

    public SecurityService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    // Tạo JWT token chuẩn OAuth2
    public String generateToken(Authentication authentication) {
        Instant now = Instant.now();
        Instant validity = now.plus(this.jwtExpiration, ChronoUnit.SECONDS);

        // Trích xuất danh sách Quyền của User từ Spring Security
        // quyền mặc định gắn tiền tố SCOPE_
        String scope = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        // Chèn dữ liệu vào (Payload)
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("web-tutorial") // Nguồn phát hành thẻ
                .issuedAt(now)
                .expiresAt(validity)
                .subject(authentication.getName())
                .claim("scope", scope)
                .build();

        // Tiến hành in thẻ với chữ ký chuẩn HS256
        return this.jwtEncoder.encode(
                JwtEncoderParameters.from(JwsHeader.with(SecurityConfiguration.JWT_ALGORITHM).build(), claims))
                .getTokenValue();
    }

    public static Optional<String> getCurrentUserLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(extractPrincipal(securityContext.getAuthentication()));
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
