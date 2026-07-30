package com.web_tutorial.javabackend.domain.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import static com.web_tutorial.javabackend.validation.ApiInputConstraints.EMAIL_MAX;
import static com.web_tutorial.javabackend.validation.ApiInputConstraints.PASSWORD_MAX;

public class LoginRequestDTO {

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email is not in a valid format")
    @Size(max = EMAIL_MAX, message = "Email must not exceed 254 characters")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(max = PASSWORD_MAX, message = "Password must not exceed 72 characters")
    private String password;

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
}
