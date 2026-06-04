package com.hlju.bookstore.repository;

import com.hlju.bookstore.entity.BookReview;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public class BookReviewRepository {
    private final JdbcTemplate jdbcTemplate;

    public BookReviewRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean save(BookReview review) {
        String sql = "INSERT INTO book_reviews (book_id, user_id, username, rating, content, status, created_time) VALUES (?, ?, ?, ?, ?, ?, ?)";
        int rows = jdbcTemplate.update(sql,
                review.getBookId(),
                review.getUserId(),
                review.getUsername(),
                review.getRating(),
                review.getContent(),
                review.getStatus(),
                review.getCreatedTime());
        return rows > 0;
    }

    public BookReview findById(Integer id) {
        String sql = "SELECT * FROM book_reviews WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(BookReview.class), id);
        } catch (Exception e) {
            return null;
        }
    }

    public List<BookReview> findByBookId(Integer bookId) {
        String sql = "SELECT * FROM book_reviews WHERE book_id = ? AND status = 'VISIBLE' ORDER BY created_time DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(BookReview.class), bookId);
    }

    public List<BookReview> findByUserId(Integer userId) {
        String sql = "SELECT * FROM book_reviews WHERE user_id = ? ORDER BY created_time DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(BookReview.class), userId);
    }

    public boolean existsByBookIdAndUserId(Integer bookId, Integer userId) {
        String sql = "SELECT COUNT(*) FROM book_reviews WHERE book_id = ? AND user_id = ? AND status <> 'DELETED'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, bookId, userId);
        return count != null && count > 0;
    }

    public BigDecimal averageRating(Integer bookId) {
        String sql = "SELECT AVG(rating) FROM book_reviews WHERE book_id = ? AND status = 'VISIBLE'";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, bookId);
    }

    public boolean markDeleted(Integer id) {
        String sql = "UPDATE book_reviews SET status = 'DELETED' WHERE id = ?";
        int rows = jdbcTemplate.update(sql, id);
        return rows > 0;
    }
}
