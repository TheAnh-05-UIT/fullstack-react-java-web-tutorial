package com.web_tutorial.javabackend.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.ObjectPostProcessor;
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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.HttpStatusAccessDeniedHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import jakarta.servlet.DispatcherType;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

// Cấu hình trung tâm bảo mật của ứng dụng (OAuth2 + JWT)
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    @Value("${javabackend.jwt.base64-secret}")
    private String jwtKey;

    @Value("${javabackend.jwt.issuer:web-tutorial}")
    private String issuer;

    @Value("${javabackend.jwt.access-token-audience:webtutorial-api}")
    private String accessTokenAudience;

    @Value("${javabackend.jwt.refresh-token-audience:webtutorial-auth}")
    private String refreshTokenAudience;

    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;
    private static final Set<String> ALLOWED_AUTHORITIES = Set.of("ROLE_USER", "ROLE_ADMIN");

    // Lấy secret key từ file cấu hình để mã hóa/giải mã JWT
    private SecretKey getSecretKey() {
        if (jwtKey == null || jwtKey.trim().isEmpty() || "${JWT_SECRET_BASE64}".equals(jwtKey.trim())) {
            throw new IllegalStateException("FATAL: JWT_SECRET_BASE64 environment variable is missing or empty! The application cannot start without a valid JWT secret key.");
        }
        byte[] keyBytes;
        try {
            keyBytes = Base64.from(jwtKey).decode();
        } catch (Exception e) {
            throw new IllegalStateException("FATAL: JWT_SECRET_BASE64 is not a valid Base64 string!");
        }
        if (keyBytes.length < 64) {
            throw new IllegalStateException("FATAL: JWT_SECRET_BASE64 must decode to at least 64 bytes (512 bits) for HS512 algorithm!");
        }
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, JWT_ALGORITHM.getName());
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(getSecretKey()));
    }

    // Tạo JWT Decoder để xác thực token
    @Bean
    @Primary
    public JwtDecoder jwtDecoder() {
        return buildDecoder(accessTokenAudience, "access");
    }

    @Bean("refreshJwtDecoder")
    public JwtDecoder refreshJwtDecoder() {
        return buildDecoder(refreshTokenAudience, "refresh");
    }

    private JwtDecoder buildDecoder(String expectedAudience, String expectedTokenType) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(getSecretKey())
                .macAlgorithm(JWT_ALGORITHM)
                .build();
        OAuth2TokenValidator<Jwt> audienceValidator = new JwtClaimValidator<List<String>>(
                "aud", audiences -> audiences != null && audiences.contains(expectedAudience));
        OAuth2TokenValidator<Jwt> tokenTypeValidator = new JwtClaimValidator<String>(
                "token_type", expectedTokenType::equals);
        OAuth2TokenValidator<Jwt> subjectValidator = new JwtClaimValidator<String>(
                "sub", subject -> subject != null && !subject.isBlank());
        OAuth2TokenValidator<Jwt> jwtIdValidator = new JwtClaimValidator<String>(
                "jti", jwtId -> jwtId != null && !jwtId.isBlank());
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(issuer),
                audienceValidator,
                tokenTypeValidator,
                subjectValidator,
                jwtIdValidator));
        return decoder;
    }

    // Cấu hình phân quyền truy cập cho các API
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository csrfTokenRepository =
                CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfTokenRepository.setCookiePath("/");
        CsrfTokenRequestAttributeHandler csrfTokenRequestHandler =
                new CsrfTokenRequestAttributeHandler();
        csrfTokenRequestHandler.setCsrfRequestAttributeName(null);
        RequestMatcher cookieAuthenticatedSessionRequest = request -> {
            if (!HttpMethod.POST.matches(request.getMethod())) {
                return false;
            }
            String requestPath = request.getRequestURI().substring(request.getContextPath().length());
            return "/api/v1/refresh".equals(requestPath)
                    || "/api/v1/logout".equals(requestPath);
        };

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(csrfTokenRequestHandler)
                        .requireCsrfProtectionMatcher(cookieAuthenticatedSessionRequest)
                        .withObjectPostProcessor(new ObjectPostProcessor<CsrfFilter>() {
                            @Override
                            public <O extends CsrfFilter> O postProcess(O filter) {
                                filter.setAccessDeniedHandler(
                                        new HttpStatusAccessDeniedHandler(HttpStatus.FORBIDDEN));
                                return filter;
                            }
                        }))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()

                        // Cho phép truy cập không cần token
                        .requestMatchers("/api/v1/login", "/api/v1/register", "/api/v1/refresh",
                                "/api/v1/logout", "/api/v1/csrf").permitAll()
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
                        .requestMatchers(HttpMethod.POST, "/api/v1/upload").hasAuthority("ROLE_ADMIN")
                        // Yêu cầu token cho các request khác
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedHandler(new HttpStatusAccessDeniedHandler(HttpStatus.FORBIDDEN))
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.FORBIDDEN),
                                cookieAuthenticatedSessionRequest))
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
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
        return jwtAuthenticationConverter;
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        String scope = jwt.getClaimAsString("scope");
        if (scope == null || scope.isBlank()) {
            return List.of();
        }
        return Arrays.stream(scope.trim().split("\\s+"))
                .filter(ALLOWED_AUTHORITIES::contains)
                .distinct()
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
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
