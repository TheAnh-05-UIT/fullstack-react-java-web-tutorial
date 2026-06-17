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
import com.web_tutorial.javabackend.domain.dto.response.auth.LoginResponseDTO;
import com.web_tutorial.javabackend.domain.user.User;
import com.web_tutorial.javabackend.service.security.SecurityService;
import com.web_tutorial.javabackend.service.user.UserService;
import com.web_tutorial.javabackend.util.annotation.ApiMessage;

import jakarta.validation.Valid;

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

    @PostMapping("/login")
    @ApiMessage("Login Success")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginDTO) {

        // nạp username và password vào security
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginDTO.getEmail(), loginDTO.getPassword());

        // xác thực người dùng
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        String newToken = this.securityService.generateToken(authentication);
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

        return ResponseEntity.ok().body(responseLoginDTO);
    }
}
