package com.web_tutorial.javabackend.controller.auth;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.web_tutorial.javabackend.domain.dto.request.auth.LoginRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.auth.RefreshTokenRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.auth.RegisterRequestDTO;
import com.web_tutorial.javabackend.domain.dto.response.auth.LoginResponseDTO;
import com.web_tutorial.javabackend.domain.user.Role;
import com.web_tutorial.javabackend.domain.user.User;
import com.web_tutorial.javabackend.exception.IdInvalidException;
import com.web_tutorial.javabackend.repository.user.RoleRepository;
import com.web_tutorial.javabackend.service.security.SecurityService;
import com.web_tutorial.javabackend.service.user.UserService;
import com.web_tutorial.javabackend.util.annotation.ApiMessage;

import jakarta.validation.Valid;

// API điều khiển xác thực: Đăng ký, Đăng nhập, Cấp lại Token
@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityService securityService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public AuthController(
            AuthenticationManagerBuilder authenticationManagerBuilder,
            SecurityService securityService,
            UserService userService,
            PasswordEncoder passwordEncoder,
            RoleRepository roleRepository) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityService = securityService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    // Thêm endpoint /register – Frontend gọi POST /api/v1/register
    @PostMapping("/register")
    @ApiMessage("Register Success")
    public ResponseEntity<LoginResponseDTO> register(@Valid @RequestBody RegisterRequestDTO registerDTO)
            throws IdInvalidException {

        // Kiểm tra email đã tồn tại chưa
        if (this.userService.existsUserByEmail(registerDTO.getEmail())) {
            throw new IdInvalidException(
                    "Email " + registerDTO.getEmail() + " already exists, please use another email.");
        }

        // Tạo user mới
        User newUser = new User();
        newUser.setUsername(registerDTO.getName());
        newUser.setEmail(registerDTO.getEmail());
        newUser.setPassword(this.passwordEncoder.encode(registerDTO.getPassword()));

        // Gán role mặc định là USER
        Optional<Role> defaultRole = this.roleRepository.findByName("USER");
        defaultRole.ifPresent(newUser::setRole);

        User savedUser = this.userService.createUser(newUser);

        // Tự động đăng nhập sau khi đăng ký
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                registerDTO.getEmail(), registerDTO.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Sinh token
        String accessToken = this.securityService.generateToken(authentication);
        String refreshToken = this.securityService.generateRefreshToken(savedUser.getEmail());
        this.userService.updateRefreshToken(savedUser.getEmail(), refreshToken);

        // Tạo response
        LoginResponseDTO.UserLogin userLogin = new LoginResponseDTO.UserLogin(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRole() != null ? savedUser.getRole().getName() : "USER",
                savedUser.getAvatar() != null ? savedUser.getAvatar() : "/default-avatar.png");

        LoginResponseDTO responseDTO = new LoginResponseDTO(accessToken, refreshToken, userLogin);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
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
                    userDBLogin.getEmail(),
                    userDBLogin.getRole() != null ? userDBLogin.getRole().getName() : "USER",
                    userDBLogin.getAvatar() != null ? userDBLogin.getAvatar() : "/default-avatar.png");
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
                    user.getEmail(),
                    user.getRole() != null ? user.getRole().getName() : "USER",
                    user.getAvatar() != null ? user.getAvatar() : "/default-avatar.png");
            LoginResponseDTO responseLoginDTO = new LoginResponseDTO(newAccessToken, newRefreshToken, userLogin);

            return ResponseEntity.ok().body(responseLoginDTO);
        } catch (org.springframework.security.oauth2.jwt.JwtException e) {
            throw new Exception("Refresh token is invalid or expired");
        }
    }
}
