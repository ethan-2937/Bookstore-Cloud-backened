package com.hlju.bookstore.service.impl;

import com.hlju.bookstore.entity.User;
import com.hlju.bookstore.repository.UserRepository;
import com.hlju.bookstore.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User login(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        return userRepository.findByUsernameAndPassword(username, password);
    }

    @Override
    public boolean register(String username, String password, String nickname) {
        if (userRepository.findByUsername(username) != null) {
            return false;
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setNickname(nickname == null || nickname.isBlank() ? username : nickname);
        user.setRole("USER");
        user.setCreatedTime(LocalDateTime.now());
        return userRepository.save(user);
    }
}
