package com.web_tutorial.javabackend.service.auth.impl;

import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.web_tutorial.javabackend.domain.dto.request.auth.LoginRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.auth.RefreshTokenRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.auth.RegisterRequestDTO;
import com.web_tutorial.javabackend.domain.dto.response.auth.LoginResponseDTO;
import com.web_tutorial.javabackend.domain.user.RefreshTokenSession;
import com.web_tutorial.javabackend.domain.user.User;
import com.web_tutorial.javabackend.exception.IdInvalidException;
import com.web_tutorial.javabackend.exception.InvalidRefreshTokenException;
import com.web_tutorial.javabackend.service.auth.AuthService;
import com.web_tutorial.javabackend.service.auth.RefreshTokenSessionService;
import com.web_tutorial.javabackend.service.security.SecurityService;
import com.web_tutorial.javabackend.service.user.UserService;
import com.web_tutorial.javabackend.service.user.impl.UserDetailsImpl;

@Service
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManagerBuilder authManagerBuilder;
    private final SecurityService securityService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenSessionService refreshTokenSessionService;

    public AuthServiceImpl(AuthenticationManagerBuilder authManagerBuilder,
            SecurityService securityService,
            UserService userService,
            PasswordEncoder passwordEncoder,
            RefreshTokenSessionService refreshTokenSessionService) {
        this.authManagerBuilder = authManagerBuilder;
        this.securityService = securityService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenSessionService = refreshTokenSessionService;
    }

    @Override
    @Transactional
    public LoginResponseDTO register(RegisterRequestDTO registerDTO) throws IdInvalidException {
        if (userService.existsUserByEmail(registerDTO.getEmail())) {
            throw new IdInvalidException(
                    "Email " + registerDTO.getEmail() + " already exists, please use another email.");
        }

        User newUser = new User();
        newUser.setUsername(registerDTO.getName());
        newUser.setEmail(registerDTO.getEmail());
        newUser.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        userService.assignRoleByName(newUser, "USER");
        User savedUser = userService.createUser(newUser);

        Authentication authentication = authenticate(registerDTO.getEmail(), registerDTO.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String accessToken = securityService.generateAccessToken(authentication);
        String refreshToken = createRefreshSession(savedUser);
        return buildResponse(accessToken, refreshToken, savedUser);
    }

    @Override
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO loginDTO) {
        Authentication authentication = authenticate(loginDTO.getEmail(), loginDTO.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userService.getUserByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new InvalidRefreshTokenException("Không tìm thấy user sau khi xác thực."));
        String accessToken = securityService.generateAccessToken(authentication);
        String refreshToken = createRefreshSession(user);
        return buildResponse(accessToken, refreshToken, user);
    }

    @Override
    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public LoginResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        String incomingRefreshToken = request.getRefreshToken();
        try {
            Jwt incomingJwt = securityService.decodeRefreshToken(incomingRefreshToken);
            RefreshTokenSession session =
                    refreshTokenSessionService.validateForRotation(incomingRefreshToken, incomingJwt);
            User user = session.getUser();

            UserDetailsImpl userDetails = new UserDetailsImpl(user);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            String newAccessToken = securityService.generateAccessToken(authentication);
            String newRefreshToken =
                    securityService.generateRefreshToken(user.getEmail(), session.getFamilyId());
            Jwt newRefreshJwt = securityService.decodeRefreshToken(newRefreshToken);
            refreshTokenSessionService.rotate(session, newRefreshToken, newRefreshJwt);
            return buildResponse(newAccessToken, newRefreshToken, user);
        } catch (JwtException exception) {
            throw new InvalidRefreshTokenException("Refresh token không hợp lệ hoặc đã hết hạn");
        }
    }

    @Override
    @Transactional
    public void logout(String email) {
        userService.getUserByEmail(email).ifPresent(refreshTokenSessionService::revokeAll);
    }

    private String createRefreshSession(User user) {
        String familyId = UUID.randomUUID().toString();
        String refreshToken = securityService.generateRefreshToken(user.getEmail(), familyId);
        refreshTokenSessionService.create(
                user, refreshToken, securityService.decodeRefreshToken(refreshToken));
        return refreshToken;
    }

    private Authentication authenticate(String email, String password) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(email, password);
        return authManagerBuilder.getObject().authenticate(authToken);
    }

    private LoginResponseDTO buildResponse(String accessToken, String refreshToken, User user) {
        LoginResponseDTO.UserLogin userLogin = new LoginResponseDTO.UserLogin(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().getName() : "USER",
                user.getAvatar() != null ? user.getAvatar() : "/default-avatar.png");
        return new LoginResponseDTO(accessToken, refreshToken, userLogin);
    }
}
