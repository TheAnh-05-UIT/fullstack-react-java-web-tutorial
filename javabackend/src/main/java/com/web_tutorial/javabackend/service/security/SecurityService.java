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
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.web_tutorial.javabackend.config.SecurityConfiguration;

// Service xử lý sinh và giải mã JWT Token
@Service
public class SecurityService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    // Tiêm các cài đặt từ file application.properties

    @Value("${javabackend.jwt.access-token-validity-in-seconds}")
    private Long jwtExpiration;

    @Value("${javabackend.jwt.refresh-token-validity-in-seconds}")
    private Long refreshTokenExpiration;

    public SecurityService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    // Tạo Access Token Ngắn hạn, chứa scope quyền
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
                .issuer("web-tutorial")
                .issuedAt(now)
                .expiresAt(validity)
                .subject(email)
                .build();

        return this.jwtEncoder.encode(
                JwtEncoderParameters.from(JwsHeader.with(SecurityConfiguration.JWT_ALGORITHM).build(), claims))
                .getTokenValue();
    }

    // Giải mã JWT Token
    public Jwt decodeToken(String token) {
        return this.jwtDecoder.decode(token);
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
