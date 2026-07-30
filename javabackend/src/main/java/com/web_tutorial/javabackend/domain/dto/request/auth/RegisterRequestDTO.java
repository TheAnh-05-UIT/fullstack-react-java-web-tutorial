package com.web_tutorial.javabackend.domain.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static com.web_tutorial.javabackend.validation.ApiInputConstraints.EMAIL_MAX;
import static com.web_tutorial.javabackend.validation.ApiInputConstraints.PASSWORD_MAX;
import static com.web_tutorial.javabackend.validation.ApiInputConstraints.VARCHAR_MAX;

/**
 * DTO cho request đăng ký tài khoản mới.
 * Frontend gửi { name, email, password } lên POST /api/v1/register
 */
public class RegisterRequestDTO {

    @NotBlank(message = "Name cannot be blank")
    @Size(max = VARCHAR_MAX, message = "Name must not exceed 255 characters")
    private String name;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email is not in a valid format")
    @Size(max = EMAIL_MAX, message = "Email must not exceed 254 characters")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, max = PASSWORD_MAX,
            message = "Password must be between 6 and 72 characters long")
    private String password;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
