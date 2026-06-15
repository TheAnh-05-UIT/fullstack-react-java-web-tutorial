package com.web_tutorial.javabackend.domain.dto.response.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

import com.web_tutorial.javabackend.domain.user.Role;

@Setter
@Getter
@NoArgsConstructor
public class CreateUserResponseDTO extends BaseUserResponse {
    private Instant createdAt;
    private String createBy;

    public CreateUserResponseDTO(Long id,
            String username,
            String email,
            String avatar,
            Role role,
            Instant createdAt,
            String createBy) {
        super(id, username, email, avatar, role);
        this.createdAt = createdAt;
        this.createBy = createBy;
    }

    public CreateUserResponseDTO(Instant createdAt, String createBy) {
        this.createdAt = createdAt;
        this.createBy = createBy;
    }

}
