package com.hlju.bookstore.service;

import com.hlju.bookstore.entity.BookReview;

import java.util.List;
import java.util.Map;

public interface BookReviewService {
    Map<String, Object> addReview(String username, Map<String, Object> data);
    Map<String, Object> findBookReviews(Integer bookId);
    List<BookReview> findMyReviews(String username);
    Map<String, Object> deleteReview(Integer id, String username, String role);
}
