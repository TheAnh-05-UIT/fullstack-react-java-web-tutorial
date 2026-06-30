package com.web_tutorial.javabackend.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.web_tutorial.javabackend.domain.dto.request.user.CreateUserRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.user.UpdateUserRequestDTO;
import com.web_tutorial.javabackend.domain.dto.response.user.CreateUserResponseDTO;
import com.web_tutorial.javabackend.domain.dto.response.user.UpdateUserResponseDTO;
import com.web_tutorial.javabackend.domain.dto.response.user.UserResponseDTO;
import com.web_tutorial.javabackend.domain.user.User;

public class UserMapper {

    public static UserResponseDTO toUserResponseDTO(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatar(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    public static CreateUserResponseDTO toCreateUserResponseDTO(User user) {
        if (user == null) {
            return null;
        }
        return new CreateUserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatar(),
                user.getRole(),
                user.getCreatedAt(),
                user.getCreateBy());
    }

    public static UpdateUserResponseDTO toUpdateUserResponseDTO(User user) {
        if (user == null) {
            return null;
        }
        return new UpdateUserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatar(),
                user.getRole(),
                user.getUpdatedAt(),
                user.getUpdateBy());
    }

    public static List<UserResponseDTO> toUserResponseDTOList(List<User> users) {
        if (users == null) {
            return null;
        }
        return users.stream().map(UserMapper::toUserResponseDTO).collect(Collectors.toList());
    }

    public static User toUser(CreateUserRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        return user;
    }

    public static void updateUserFromDTO(UpdateUserRequestDTO dto, User user) {
        if (dto == null || user == null) {
            return;
        }
        user.setUsername(dto.getUsername());
        user.setAvatar(dto.getAvatar());
    }
}
