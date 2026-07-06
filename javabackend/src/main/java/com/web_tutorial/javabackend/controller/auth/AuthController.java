package com.web_tutorial.javabackend.controller.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.web_tutorial.javabackend.domain.dto.request.auth.LoginRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.auth.RefreshTokenRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.auth.RegisterRequestDTO;
import com.web_tutorial.javabackend.domain.dto.response.auth.LoginResponseDTO;
import com.web_tutorial.javabackend.exception.IdInvalidException;
import com.web_tutorial.javabackend.service.auth.AuthService;
import com.web_tutorial.javabackend.service.user.UserService;
import com.web_tutorial.javabackend.util.annotation.ApiMessage;

import jakarta.validation.Valid;

// Controller xác thực – chỉ xử lý HTTP layer, mọi nghiệp vụ ủy quyền cho AuthService
@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    @ApiMessage("Register Success")
    public ResponseEntity<LoginResponseDTO> register(@Valid @RequestBody RegisterRequestDTO registerDTO)
            throws IdInvalidException {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(registerDTO));
    }

    @PostMapping("/login")
    @ApiMessage("Login Success")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginDTO) {
        return ResponseEntity.ok(authService.login(loginDTO));
    }

    @PostMapping("/refresh")
    @ApiMessage("Get new Access Token by Refresh Token")
    public ResponseEntity<LoginResponseDTO> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    /**
     * Logout – thu hồi Refresh Token trong DB.
     * Access Token vẫn hợp lệ cho đến khi hết hạn (JWT stateless),
     * nhưng sau khi logout người dùng không thể gia hạn thêm.
     * Frontend có trách nhiệm xóa token khỏi localStorage.
     */
    @PostMapping("/logout")
    @ApiMessage("Logout Success")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Jwt jwt) {
        // Lấy email từ JWT claim "sub" để revoke đúng user
        String email = jwt.getSubject();
        userService.revokeRefreshToken(email);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
