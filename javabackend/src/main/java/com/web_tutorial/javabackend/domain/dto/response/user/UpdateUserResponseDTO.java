package com.web_tutorial.javabackend.domain.dto.response.user;

import java.time.Instant;

import com.web_tutorial.javabackend.domain.user.Role;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class UpdateUserResponseDTO extends BaseUserResponse {
    private Instant updateAt;
    private String updateBy;

    public UpdateUserResponseDTO(Long id,
            String username,
            String email,
            String avatar,
            Role role,
            Instant updateAt,
            String updateBy) {
        super(id, username, email, avatar, role);
        this.updateAt = updateAt;
        this.updateBy = updateBy;
    }

    public UpdateUserResponseDTO(Instant updateAt, String updateBy) {
        this.updateAt = updateAt;
        this.updateBy = updateBy;
    }

}
