package com.web_tutorial.javabackend.domain.dto.response.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class LoginResponseDTO {
    private String accessToken;
    private String refreshToken;
    private UserLogin userLogin;

    // inner class
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserLogin {
        private long id;
        private String name;
        private String email;
    }

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(String accessToken, String refreshToken, UserLogin userLogin) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userLogin = userLogin;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public UserLogin getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(UserLogin userLogin) {
        this.userLogin = userLogin;
    }
}
