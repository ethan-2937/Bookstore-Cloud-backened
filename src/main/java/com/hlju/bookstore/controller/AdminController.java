package com.hlju.bookstore.controller;

import com.hlju.bookstore.service.AdminService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> data) {
        String username = data.get("username");
        String password = data.get("password");
        boolean success = adminService.login(username, password);
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "Login success" : "Invalid username or password");
        if (success) {
            response.put("username", username);
            response.put("role", "ADMIN");
        }
        return response;
    }
}
