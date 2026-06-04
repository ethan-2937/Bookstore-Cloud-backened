package com.hlju.bookstore.service;

import com.hlju.bookstore.entity.User;

public interface UserService {
    User login(String username, String password);
    boolean register(String username, String password, String nickname);
}
