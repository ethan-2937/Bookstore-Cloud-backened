package com.hlju.bookstore.repository;

import com.hlju.bookstore.entity.SupportMessage;
import com.hlju.bookstore.entity.SupportTicket;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class SupportRepository {
    private final JdbcTemplate jdbcTemplate;

    public SupportRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Integer saveTicket(SupportTicket ticket) {
        String sql = "INSERT INTO support_tickets (user_id, username, subject, status, created_time, updated_time) VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, ticket.getUserId());
            ps.setString(2, ticket.getUsername());
            ps.setString(3, ticket.getSubject());
            ps.setString(4, ticket.getStatus());
            ps.setObject(5, ticket.getCreatedTime());
            ps.setObject(6, ticket.getUpdatedTime());
            return ps;
        }, keyHolder);
        return keyHolder.getKey() == null ? null : keyHolder.getKey().intValue();
    }

    public boolean saveMessage(SupportMessage message) {
        String sql = "INSERT INTO support_messages (ticket_id, sender_id, sender_username, sender_role, content, created_time) VALUES (?, ?, ?, ?, ?, ?)";
        int rows = jdbcTemplate.update(sql,
                message.getTicketId(),
                message.getSenderId(),
                message.getSenderUsername(),
                message.getSenderRole(),
                message.getContent(),
                message.getCreatedTime());
        return rows > 0;
    }

    public SupportTicket findTicketById(Integer id) {
        String sql = "SELECT * FROM support_tickets WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(SupportTicket.class), id);
        } catch (Exception e) {
            return null;
        }
    }

    public List<SupportTicket> findTicketsByUserId(Integer userId) {
        String sql = "SELECT * FROM support_tickets WHERE user_id = ? ORDER BY updated_time DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(SupportTicket.class), userId);
    }

    public List<SupportTicket> findAllTickets() {
        String sql = "SELECT * FROM support_tickets ORDER BY updated_time DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(SupportTicket.class));
    }

    public List<SupportMessage> findMessagesByTicketId(Integer ticketId) {
        String sql = "SELECT * FROM support_messages WHERE ticket_id = ? ORDER BY created_time ASC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(SupportMessage.class), ticketId);
    }

    public boolean updateTicketStatus(Integer id, String status) {
        String sql = "UPDATE support_tickets SET status = ?, updated_time = NOW() WHERE id = ?";
        int rows = jdbcTemplate.update(sql, status, id);
        return rows > 0;
    }
}
