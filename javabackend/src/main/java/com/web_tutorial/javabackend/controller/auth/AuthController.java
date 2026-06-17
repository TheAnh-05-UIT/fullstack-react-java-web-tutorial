package com.web_tutorial.javabackend.controller.auth;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.web_tutorial.javabackend.domain.dto.request.auth.LoginRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.auth.RefreshTokenRequestDTO;
import com.web_tutorial.javabackend.domain.dto.response.auth.LoginResponseDTO;
import com.web_tutorial.javabackend.domain.user.User;
import com.web_tutorial.javabackend.service.security.SecurityService;
import com.web_tutorial.javabackend.service.user.UserService;
import com.web_tutorial.javabackend.util.annotation.ApiMessage;

import jakarta.validation.Valid;

// API điều khiển xác thực Đăng nhập, Cấp lại Token
@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityService securityService;
    private final UserService userService;

    public AuthController(
            AuthenticationManagerBuilder authenticationManagerBuilder,
            SecurityService securityService,
            UserService userService) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityService = securityService;
        this.userService = userService;
    }

    // API Đăng nhập: Sinh Access Token và Refresh Token
    @PostMapping("/login")
    @ApiMessage("Login Success")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginDTO) {

        // nạp username và password vào security
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginDTO.getEmail(), loginDTO.getPassword());

        // xác thực người dùng
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        String newToken = this.securityService.generateToken(authentication);

        String newRefreshToken = this.securityService.generateRefreshToken(loginDTO.getEmail());
        this.userService.updateRefreshToken(loginDTO.getEmail(), newRefreshToken);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        Optional<User> userDB = this.userService.getUserByEmail(loginDTO.getEmail());
        LoginResponseDTO responseLoginDTO = new LoginResponseDTO();
        if (userDB.isPresent()) {
            User userDBLogin = userDB.get();
            LoginResponseDTO.UserLogin userLogin = new LoginResponseDTO.UserLogin(
                    userDBLogin.getId(),
                    userDBLogin.getUsername(),
                    userDBLogin.getEmail());
            responseLoginDTO.setUserLogin(userLogin);
        }

        responseLoginDTO.setAccessToken(newToken);
        responseLoginDTO.setRefreshToken(newRefreshToken);

        return ResponseEntity.ok().body(responseLoginDTO);
    }

    // API Cấp lại Access Token mới dựa vào Refresh Token
    @PostMapping("/refresh")
    @ApiMessage("Get new Access Token by Refresh Token")
    public ResponseEntity<LoginResponseDTO> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO request)
            throws Exception {
        String incomingRefreshToken = request.getRefreshToken();

        try {
            // Giải mã token để lấy thông tin
            org.springframework.security.oauth2.jwt.Jwt decodedToken = this.securityService
                    .decodeToken(incomingRefreshToken);
            String email = decodedToken.getSubject();

            // Kiểm tra Refresh Token trong DB
            Optional<User> userDB = this.userService.getUserByRefreshToken(incomingRefreshToken);
            if (userDB.isEmpty() || !userDB.get().getEmail().equals(email)) {
                throw new Exception("Invalid refresh token");
            }
            User user = userDB.get();

            // Tái tạo Authentication object
            com.web_tutorial.javabackend.service.user.impl.UserDetailsImpl userDetails = new com.web_tutorial.javabackend.service.user.impl.UserDetailsImpl(
                    user);
            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                    userDetails.getAuthorities());

            // Tạo lại cặp token mới
            String newAccessToken = this.securityService.generateToken(authentication);
            String newRefreshToken = this.securityService.generateRefreshToken(email);
            this.userService.updateRefreshToken(email, newRefreshToken);

            // Trả về cho Client
            LoginResponseDTO.UserLogin userLogin = new LoginResponseDTO.UserLogin(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail());
            LoginResponseDTO responseLoginDTO = new LoginResponseDTO(newAccessToken, newRefreshToken, userLogin);

            return ResponseEntity.ok().body(responseLoginDTO);
        } catch (org.springframework.security.oauth2.jwt.JwtException e) {
            throw new Exception("Refresh token is invalid or expired");
        }
    }
}
