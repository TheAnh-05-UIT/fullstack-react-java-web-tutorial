package com.web_tutorial.javabackend.service.auth;

import com.web_tutorial.javabackend.domain.dto.request.auth.LoginRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.auth.RegisterRequestDTO;
import com.web_tutorial.javabackend.domain.dto.response.auth.LoginResponseDTO;
import com.web_tutorial.javabackend.exception.IdInvalidException;

/**
 * Service xử lý nghiệp vụ xác thực: đăng ký, đăng nhập, cấp lại token.
 * Controller chỉ việc gọi các method này và trả về HTTP response.
 */
public interface AuthService {

    // Đăng ký tài khoản mới, tự động đăng nhập và trả về cặp token
    LoginResponseDTO register(RegisterRequestDTO registerDTO) throws IdInvalidException;

    // Xác thực thông tin đăng nhập, trả về cặp token
    LoginResponseDTO login(LoginRequestDTO loginDTO);

    // Kiểm tra refresh token và cấp lại cặp token mới (token rotation)
    LoginResponseDTO refreshToken(String refreshToken);

    void logout(String refreshToken);
}
