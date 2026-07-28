package com.web_tutorial.javabackend.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

// Cấu hình trung tâm bảo mật của ứng dụng (OAuth2 + JWT)
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    @Value("${javabackend.jwt.base64-secret}")
    private String jwtKey;

    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;

    // Lấy secret key từ file cấu hình để mã hóa/giải mã JWT
    private SecretKey getSecretKey() {
        if (jwtKey == null || jwtKey.trim().isEmpty() || "${JWT_SECRET}".equals(jwtKey.trim())) {
            throw new IllegalStateException("FATAL: JWT_SECRET environment variable is missing or empty! The application cannot start without a valid JWT secret key.");
        }
        byte[] keyBytes;
        try {
            keyBytes = Base64.from(jwtKey).decode();
        } catch (Exception e) {
            throw new IllegalStateException("FATAL: JWT_SECRET is not a valid Base64 string!");
        }
        if (keyBytes.length < 64) {
            throw new IllegalStateException("FATAL: JWT_SECRET must decode to at least 64 bytes (512 bits) for HS512 algorithm!");
        }
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, JWT_ALGORITHM.getName());
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(getSecretKey()));
    }

    // Tạo JWT Decoder để xác thực token
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(getSecretKey())
                .macAlgorithm(JWT_ALGORITHM).build();
    }

    // Cấu hình phân quyền truy cập cho các API
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        // Cho phép truy cập không cần token
                        .requestMatchers("/api/v1/login", "/api/v1/register", "/api/v1/refresh").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/tutorials/admin",
                                "/api/v1/tutorials/admin/**",
                                "/api/v1/projects/admin",
                                "/api/v1/projects/admin/**",
                                "/api/v1/roadmaps/admin",
                                "/api/v1/roadmaps/admin/**")
                        .hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/tutorials/**", "/api/v1/projects/**",
                                "/api/v1/roadmaps/**", "/uploads/**")
                        .permitAll()
                        // DevOps: GET phases & simulations công khai cho học viên
                        // Admin endpoints (/admin/**) yêu cầu ROLE_ADMIN qua @PreAuthorize
                        .requestMatchers(HttpMethod.GET, "/api/v1/devops/phases/**",
                                "/api/v1/devops/simulations/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/upload").authenticated()
                        // /api/v1/logout yêu cầu phải đăng nhập (có Access Token hợp lệ)
                        .requestMatchers("/api/v1/logout").authenticated()
                        // Yêu cầu token cho các request khác
                        .anyRequest().authenticated())
                // Không sử dụng Session
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Kích hoạt tính năng kiểm tra bằng jwt oauth2 resource server, map chính xác
                // ROLE_ADMIN
                .oauth2ResourceServer(
                        oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix(""); // Không tự động thêm SCOPE_ để khớp exact ROLE_ADMIN
        grantedAuthoritiesConverter.setAuthoritiesClaimName("scope");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Mã hóa mật khẩu
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
