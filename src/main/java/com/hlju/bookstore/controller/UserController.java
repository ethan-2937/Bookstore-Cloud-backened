package com.hlju.bookstore.controller;

import com.hlju.bookstore.entity.User;
import com.hlju.bookstore.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> data) {
        String username = data.get("username");
        String password = data.get("password");
        if (isBlank(username) || isBlank(password)) {
            return result(false, "Username or password cannot be empty");
        }

        User user = userService.login(username, password);
        if (user == null) {
            return result(false, "Invalid username or password");
        }

        Map<String, Object> response = result(true, "Login success");
        response.put("username", user.getUsername());
        response.put("role", user.getRole());
        response.put("nickname", user.getNickname());
        return response;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> data) {
        String username = data.get("username");
        String password = data.get("password");
        String nickname = data.get("nickname");
        if (isBlank(username) || isBlank(password)) {
            return result(false, "Username or password cannot be empty");
        }

        boolean success = userService.register(username, password, nickname);
        return result(success, success ? "Register success" : "Username already exists");
    }

    private Map<String, Object> result(boolean success, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        return response;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
