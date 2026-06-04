package com.hlju.bookstore.service.impl;

import com.hlju.bookstore.entity.BookReview;
import com.hlju.bookstore.entity.User;
import com.hlju.bookstore.repository.BookRepository;
import com.hlju.bookstore.repository.BookReviewRepository;
import com.hlju.bookstore.repository.OrderRepository;
import com.hlju.bookstore.repository.UserRepository;
import com.hlju.bookstore.service.BookReviewService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BookReviewServiceImpl implements BookReviewService {
    private final BookReviewRepository bookReviewRepository;
    private final BookRepository bookRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public BookReviewServiceImpl(BookReviewRepository bookReviewRepository,
                                 BookRepository bookRepository,
                                 OrderRepository orderRepository,
                                 UserRepository userRepository) {
        this.bookReviewRepository = bookReviewRepository;
        this.bookRepository = bookRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Map<String, Object> addReview(String username, Map<String, Object> data) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return result(false, "User not found");
        }

        Integer bookId = toInteger(data.get("bookId"));
        Integer rating = toInteger(data.get("rating"));
        String content = toStringValue(data.get("content"));
        if (bookId == null || bookRepository.findById(bookId) == null) {
            return result(false, "Book not found");
        }
        if (rating == null || rating < 1 || rating > 5) {
            return result(false, "Rating must be between 1 and 5");
        }
        if (content.isEmpty()) {
            return result(false, "Review content cannot be empty");
        }
        if (!orderRepository.hasPurchasedBook(user.getId(), bookId)) {
            return result(false, "Only purchased books can be reviewed");
        }
        if (bookReviewRepository.existsByBookIdAndUserId(bookId, user.getId())) {
            return result(false, "This book has already been reviewed");
        }

        BookReview review = new BookReview();
        review.setBookId(bookId);
        review.setUserId(user.getId());
        review.setUsername(user.getUsername());
        review.setRating(rating);
        review.setContent(content);
        review.setStatus("VISIBLE");
        review.setCreatedTime(LocalDateTime.now());

        boolean success = bookReviewRepository.save(review);
        return result(success, success ? "Review added" : "Add review failed");
    }

    @Override
    public Map<String, Object> findBookReviews(Integer bookId) {
        Map<String, Object> response = result(true, "OK");
        BigDecimal averageRating = bookReviewRepository.averageRating(bookId);
        response.put("averageRating", averageRating == null ? BigDecimal.ZERO : averageRating);
        response.put("reviews", bookReviewRepository.findByBookId(bookId));
        return response;
    }

    @Override
    public List<BookReview> findMyReviews(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return new ArrayList<>();
        }
        return bookReviewRepository.findByUserId(user.getId());
    }

    @Override
    public Map<String, Object> deleteReview(Integer id, String username, String role) {
        BookReview review = bookReviewRepository.findById(id);
        if (review == null) {
            return result(false, "Review not found");
        }
        if (!"ADMIN".equalsIgnoreCase(role)) {
            User user = userRepository.findByUsername(username);
            if (user == null || !user.getId().equals(review.getUserId())) {
                return result(false, "No permission");
            }
        }
        boolean success = bookReviewRepository.markDeleted(id);
        return result(success, success ? "Review deleted" : "Delete review failed");
    }

    private Map<String, Object> result(boolean success, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        return response;
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }

    private String toStringValue(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}
