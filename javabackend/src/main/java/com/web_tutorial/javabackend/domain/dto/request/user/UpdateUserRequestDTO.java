package com.web_tutorial.javabackend.domain.dto.request.user;

import jakarta.validation.constraints.NotBlank;

public class UpdateUserRequestDTO {

    @NotBlank(message = "Username cannot be blank")
    private String username;

    private String avatar;

    private String role;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
