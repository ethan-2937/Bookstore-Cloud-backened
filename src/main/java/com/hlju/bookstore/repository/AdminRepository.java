package com.hlju.bookstore.repository;

import com.hlju.bookstore.entity.Admin;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AdminRepository {
    private final JdbcTemplate jdbcTemplate;

    public AdminRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Admin findByUsernameAndPassword(String username, String password) {
        String sql = "SELECT * FROM admins WHERE username = ? AND password = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Admin.class), username, password);
        } catch (Exception e) {
            return null;
        }
    }
}
