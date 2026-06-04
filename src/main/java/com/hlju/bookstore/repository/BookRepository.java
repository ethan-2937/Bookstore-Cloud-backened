package com.hlju.bookstore.repository;

import com.hlju.bookstore.entity.Book;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BookRepository {
    private final JdbcTemplate jdbcTemplate;

    public BookRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Book> findAll() {
        String sql = "SELECT * FROM books ORDER BY id DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Book.class));
    }

    public Book findById(Integer id) {
        String sql = "SELECT * FROM books WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Book.class), id);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean save(Book book) {
        String sql = "INSERT INTO books (title, author, category, price, stock, cover_url, description) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            int rows = jdbcTemplate.update(sql,
                    book.getTitle(),
                    book.getAuthor(),
                    book.getCategory(),
                    book.getPrice(),
                    book.getStock(),
                    book.getCoverUrl(),
                    book.getDescription());
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM books WHERE id = ?";
        try {
            int rows = jdbcTemplate.update(sql, id);
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean decreaseStock(Integer id, Integer quantity) {
        String sql = "UPDATE books SET stock = stock - ? WHERE id = ? AND stock >= ?";
        try {
            int rows = jdbcTemplate.update(sql, quantity, id, quantity);
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean increaseStock(Integer id, Integer quantity) {
        String sql = "UPDATE books SET stock = stock + ? WHERE id = ?";
        try {
            int rows = jdbcTemplate.update(sql, quantity, id);
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
