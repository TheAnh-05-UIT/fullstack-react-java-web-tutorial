package com.web_tutorial.javabackend.service.user.impl;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.web_tutorial.javabackend.domain.user.User;
import com.web_tutorial.javabackend.service.user.UserService;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserService userService;

    public UserDetailsServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // tìm User bằng Email
        Optional<User> userOptional = userService.getUserByEmail(email);

        if (!userOptional.isPresent()) {
            throw new UsernameNotFoundException("User with the email not found: " + email);
        }

        // Trả về một phiên bản User mà Spring Security có thể đọc hiểu
        return new UserDetailsImpl(userOptional.get());
    }
}
