package com.web_tutorial.javabackend.domain.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

import static com.web_tutorial.javabackend.validation.ApiInputConstraints.EMAIL_MAX;
import static com.web_tutorial.javabackend.validation.ApiInputConstraints.PASSWORD_MAX;
import static com.web_tutorial.javabackend.validation.ApiInputConstraints.ROLE_PATTERN;
import static com.web_tutorial.javabackend.validation.ApiInputConstraints.VARCHAR_MAX;

public class CreateUserRequestDTO {

    @NotBlank(message = "Username cannot be blank")
    @Size(max = VARCHAR_MAX, message = "Username must not exceed 255 characters")
    private String username;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email is not in a valid format")
    @Size(max = EMAIL_MAX, message = "Email must not exceed 254 characters")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, max = PASSWORD_MAX,
            message = "Password must be between 6 and 72 characters long")
    private String password;

    @NotBlank(message = "Role cannot be blank")
    @Pattern(regexp = ROLE_PATTERN, message = "Role must be USER or ADMIN")
    private String role;

    @Size(max = VARCHAR_MAX, message = "Avatar must not exceed 255 characters")
    private String avatar;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim().toLowerCase(java.util.Locale.ROOT);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
