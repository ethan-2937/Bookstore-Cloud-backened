package com.hlju.bookstore.repository;

import com.hlju.bookstore.entity.BookOrder;
import com.hlju.bookstore.entity.OrderItem;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class OrderRepository {
    private final JdbcTemplate jdbcTemplate;

    public OrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Integer saveOrder(BookOrder order) {
        String sql = "INSERT INTO orders (user_id, total_amount, status, receiver_name, receiver_phone, receiver_address, created_time, updated_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, order.getUserId());
            ps.setObject(2, order.getTotalAmount());
            ps.setString(3, order.getStatus());
            ps.setString(4, order.getReceiverName());
            ps.setString(5, order.getReceiverPhone());
            ps.setString(6, order.getReceiverAddress());
            ps.setObject(7, order.getCreatedTime());
            ps.setObject(8, order.getUpdatedTime());
            return ps;
        }, keyHolder);
        return keyHolder.getKey() == null ? null : keyHolder.getKey().intValue();
    }

    public boolean saveItem(OrderItem item) {
        String sql = "INSERT INTO order_items (order_id, book_id, book_title, price, quantity, subtotal) VALUES (?, ?, ?, ?, ?, ?)";
        int rows = jdbcTemplate.update(sql,
                item.getOrderId(),
                item.getBookId(),
                item.getBookTitle(),
                item.getPrice(),
                item.getQuantity(),
                item.getSubtotal());
        return rows > 0;
    }

    public BookOrder findById(Integer id) {
        String sql = "SELECT * FROM orders WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(BookOrder.class), id);
        } catch (Exception e) {
            return null;
        }
    }

    public List<BookOrder> findByUserId(Integer userId) {
        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY created_time DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(BookOrder.class), userId);
    }

    public List<BookOrder> findAll() {
        String sql = "SELECT * FROM orders ORDER BY created_time DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(BookOrder.class));
    }

    public List<OrderItem> findItemsByOrderId(Integer orderId) {
        String sql = "SELECT * FROM order_items WHERE order_id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(OrderItem.class), orderId);
    }

    public boolean updateStatus(Integer id, String status) {
        String sql = "UPDATE orders SET status = ?, updated_time = NOW() WHERE id = ?";
        int rows = jdbcTemplate.update(sql, status, id);
        return rows > 0;
    }

    public boolean hasPurchasedBook(Integer userId, Integer bookId) {
        String sql = "SELECT COUNT(*) FROM orders o " +
                "JOIN order_items oi ON o.id = oi.order_id " +
                "WHERE o.user_id = ? AND oi.book_id = ? AND o.status IN ('PAID', 'SHIPPED', 'COMPLETED')";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, bookId);
        return count != null && count > 0;
    }
}
