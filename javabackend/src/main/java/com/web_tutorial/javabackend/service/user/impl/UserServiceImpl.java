package com.web_tutorial.javabackend.service.user.impl;

import com.web_tutorial.javabackend.domain.user.User;
import com.web_tutorial.javabackend.repository.user.UserRepository;
import com.web_tutorial.javabackend.service.user.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> getAllUsers() {
        return this.userRepository.findAll();
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return this.userRepository.findById(id);
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        return this.userRepository.findByUsername(username);
    }

    @Override
    public User createUser(User user) {
        return this.userRepository.save(user);
    }

    @Override
    public User updateUser(Long id, User userDetails) {
        Optional<User> userById = this.userRepository.findById(id);
        if (userById.isPresent()) {
            User userUpdate = userById.get();
            if (userDetails.getUsername() != null)
                userUpdate.setUsername(userDetails.getUsername());
            if (userDetails.getEmail() != null)
                userUpdate.setEmail(userDetails.getEmail());
            if (userDetails.getAvatar() != null)
                userUpdate.setAvatar(userDetails.getAvatar());
            return this.userRepository.save(userUpdate);
        }
        return null;
    }

    @Override
    public void deleteUser(Long id) {
        this.userRepository.deleteById(id);
    }

    @Override
    public boolean existsUserByEmail(String email) {
        return this.userRepository.existsByEmail(email);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return this.userRepository.findByEmail(email);
    }

    @Override
    public void updateRefreshToken(String email, String refreshToken) {
        Optional<User> userOptional = this.userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setRefreshToken(refreshToken);
            this.userRepository.save(user);
        }
    }

    @Override
    public Optional<User> getUserByRefreshToken(String refreshToken) {
        return this.userRepository.findByRefreshToken(refreshToken);
    }
}
