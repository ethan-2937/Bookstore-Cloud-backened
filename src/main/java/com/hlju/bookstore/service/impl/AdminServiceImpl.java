package com.hlju.bookstore.service.impl;

import com.hlju.bookstore.repository.AdminRepository;
import com.hlju.bookstore.service.AdminService;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {
    private final AdminRepository adminRepository;

    public AdminServiceImpl(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Override
    public boolean login(String username, String password) {
        return adminRepository.findByUsernameAndPassword(username, password) != null;
    }
}
