package com.web_tutorial.javabackend.domain.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import static com.web_tutorial.javabackend.validation.ApiInputConstraints.ROLE_PATTERN;
import static com.web_tutorial.javabackend.validation.ApiInputConstraints.VARCHAR_MAX;

public class UpdateUserRequestDTO {

    @NotBlank(message = "Username cannot be blank")
    @Size(max = VARCHAR_MAX, message = "Username must not exceed 255 characters")
    private String username;

    @Size(max = VARCHAR_MAX, message = "Avatar must not exceed 255 characters")
    private String avatar;

    @NotBlank(message = "Role cannot be blank")
    @Pattern(regexp = ROLE_PATTERN, message = "Role must be USER or ADMIN")
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
