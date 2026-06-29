package com.web_tutorial.javabackend.service.auth.impl;

import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import com.web_tutorial.javabackend.domain.dto.request.auth.LoginRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.auth.RefreshTokenRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.auth.RegisterRequestDTO;
import com.web_tutorial.javabackend.domain.dto.response.auth.LoginResponseDTO;
import com.web_tutorial.javabackend.domain.user.Role;
import com.web_tutorial.javabackend.domain.user.User;
import com.web_tutorial.javabackend.exception.IdInvalidException;
import com.web_tutorial.javabackend.exception.InvalidRefreshTokenException;
import com.web_tutorial.javabackend.repository.user.RoleRepository;
import com.web_tutorial.javabackend.service.auth.AuthService;
import com.web_tutorial.javabackend.service.security.SecurityService;
import com.web_tutorial.javabackend.service.user.UserService;
import com.web_tutorial.javabackend.service.user.impl.UserDetailsImpl;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManagerBuilder authManagerBuilder;
    private final SecurityService securityService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public AuthServiceImpl(
            AuthenticationManagerBuilder authManagerBuilder,
            SecurityService securityService,
            UserService userService,
            PasswordEncoder passwordEncoder,
            RoleRepository roleRepository) {
        this.authManagerBuilder = authManagerBuilder;
        this.securityService = securityService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Override
    public LoginResponseDTO register(RegisterRequestDTO registerDTO) throws IdInvalidException {
        // Kiểm tra email đã tồn tại chưa
        if (userService.existsUserByEmail(registerDTO.getEmail())) {
            throw new IdInvalidException(
                    "Email " + registerDTO.getEmail() + " already exists, please use another email.");
        }

        // Tạo user mới với role mặc định là USER
        User newUser = new User();
        newUser.setUsername(registerDTO.getName());
        newUser.setEmail(registerDTO.getEmail());
        newUser.setPassword(passwordEncoder.encode(registerDTO.getPassword()));

        Optional<Role> defaultRole = roleRepository.findByName("USER");
        defaultRole.ifPresent(newUser::setRole);

        User savedUser = userService.createUser(newUser);

        // Tự động đăng nhập sau khi đăng ký để sinh token ngay
        Authentication authentication = authenticate(registerDTO.getEmail(), registerDTO.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Sinh và lưu cặp token
        String accessToken = securityService.generateToken(authentication);
        String refreshToken = securityService.generateRefreshToken(savedUser.getEmail());
        userService.updateRefreshToken(savedUser.getEmail(), refreshToken);

        return buildResponse(accessToken, refreshToken, savedUser);
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO loginDTO) {
        // Xác thực thông tin đăng nhập
        Authentication authentication = authenticate(loginDTO.getEmail(), loginDTO.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Sinh và lưu cặp token
        String accessToken = securityService.generateToken(authentication);
        String refreshToken = securityService.generateRefreshToken(loginDTO.getEmail());
        userService.updateRefreshToken(loginDTO.getEmail(), refreshToken);

        // Lấy thông tin user từ DB để đưa vào response
        User user = userService.getUserByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new InvalidRefreshTokenException("Không tìm thấy user sau khi xác thực."));

        return buildResponse(accessToken, refreshToken, user);
    }

    @Override
    public LoginResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        String incomingRefreshToken = request.getRefreshToken();

        try {
            // Giải mã token để lấy email; JwtException sẽ bị bắt phía dưới
            String email = securityService.decodeToken(incomingRefreshToken).getSubject();

            // Kiểm tra refresh token có khớp trong DB không (tránh reuse sau khi revoke)
            User user = userService.getUserByRefreshToken(incomingRefreshToken)
                    .filter(u -> u.getEmail().equals(email))
                    .orElseThrow(() -> new InvalidRefreshTokenException(
                            "Refresh token không hợp lệ hoặc đã bị thu hồi."));

            // Tái tạo Authentication để sinh access token mới
            UserDetailsImpl userDetails = new UserDetailsImpl(user);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            // Token rotation: tạo cặp token hoàn toàn mới
            String newAccessToken = securityService.generateToken(authentication);
            String newRefreshToken = securityService.generateRefreshToken(email);
            userService.updateRefreshToken(email, newRefreshToken);

            return buildResponse(newAccessToken, newRefreshToken, user);

        } catch (JwtException e) {
            // Chữ ký sai hoặc token hết hạn → trả 401
            throw new InvalidRefreshTokenException(
                    "Refresh token hết hạn hoặc không đúng chữ ký: " + e.getMessage());
        }
    }

    /**
     * Xác thực email/password qua Spring Security AuthenticationManager.
     * Ném BadCredentialsException (401) nếu sai.
     */
    private Authentication authenticate(String email, String password) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password);
        return authManagerBuilder.getObject().authenticate(authToken);
    }

    /**
     * Tạo LoginResponseDTO từ cặp token và thông tin user.
     * Dùng chung cho register, login và refreshToken để tránh lặp code.
     */
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
