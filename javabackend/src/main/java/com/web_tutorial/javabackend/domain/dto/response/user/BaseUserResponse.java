package com.web_tutorial.javabackend.domain.dto.response.user;

import com.web_tutorial.javabackend.domain.user.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BaseUserResponse {
    private Long id;
    private String username;
    private String email;
    private String avatar;
    private Role role;
}
