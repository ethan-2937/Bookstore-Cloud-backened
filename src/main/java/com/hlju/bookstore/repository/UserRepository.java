package com.hlju.bookstore.repository;

import com.hlju.bookstore.entity.User;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public User findById(Integer id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(User.class), id);
        } catch (Exception e) {
            return null;
        }
    }

    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(User.class), username);
        } catch (Exception e) {
            return null;
        }
    }

    public User findByUsernameAndPassword(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(User.class), username, password);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean save(User user) {
        String sql = "INSERT INTO users (username, password, role, nickname, created_time) VALUES (?, ?, ?, ?, ?)";
        try {
            int rows = jdbcTemplate.update(sql,
                    user.getUsername(),
                    user.getPassword(),
                    user.getRole(),
                    user.getNickname(),
                    user.getCreatedTime());
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
