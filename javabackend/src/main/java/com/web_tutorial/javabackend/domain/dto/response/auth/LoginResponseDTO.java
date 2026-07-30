package com.web_tutorial.javabackend.domain.dto.response.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDTO {
    private String accessToken;
    @JsonIgnore
    private String refreshToken;
    private UserLogin userLogin;

    // inner class
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserLogin {
        private long id;
        private String username;
        private String email;
        private String role;
        private String avatar;
    }
}
