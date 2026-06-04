package com.hlju.bookstore.controller;

import com.hlju.bookstore.entity.BookReview;
import com.hlju.bookstore.service.BookReviewService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class BookReviewController {
    private final BookReviewService bookReviewService;

    public BookReviewController(BookReviewService bookReviewService) {
        this.bookReviewService = bookReviewService;
    }

    @PostMapping
    public Map<String, Object> addReview(@RequestBody Map<String, Object> data,
                                         @RequestParam String username) {
        return bookReviewService.addReview(username, data);
    }

    @GetMapping("/book/{bookId}")
    public Map<String, Object> bookReviews(@PathVariable Integer bookId) {
        return bookReviewService.findBookReviews(bookId);
    }

    @GetMapping("/my")
    public List<BookReview> myReviews(@RequestParam String username) {
        return bookReviewService.findMyReviews(username);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteReview(@PathVariable Integer id,
                                            @RequestParam String username,
                                            @RequestParam(defaultValue = "USER") String role) {
        return bookReviewService.deleteReview(id, username, role);
    }
}
