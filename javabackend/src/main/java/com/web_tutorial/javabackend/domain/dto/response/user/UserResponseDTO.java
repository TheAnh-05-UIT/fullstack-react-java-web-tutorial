package com.web_tutorial.javabackend.domain.dto.response.user;

import java.time.Instant;

import com.web_tutorial.javabackend.domain.user.Role;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class UserResponseDTO extends BaseUserResponse {
    private Instant createdAt;
    private Instant updatedAt;

    public UserResponseDTO(Long id,
            String username,
            String email,
            String avatar,
            Role role,
            Instant createdAt,
            Instant updatedAt) {
        super(id, username, email, avatar, role);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UserResponseDTO(Instant createdAt, Instant updatedAt) {
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}
