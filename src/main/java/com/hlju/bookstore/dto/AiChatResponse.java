package com.hlju.bookstore.dto;

import com.hlju.bookstore.entity.Book;

import java.util.List;

public record AiChatResponse(
        boolean success,
        String reply,
        List<Book> recommendedBooks,
        String source,
        String message
) {
    public static AiChatResponse success(String reply, List<Book> recommendedBooks, String source) {
        return new AiChatResponse(true, reply, recommendedBooks, source, null);
    }

    public static AiChatResponse fallback(String reply, List<Book> recommendedBooks, String message) {
        return new AiChatResponse(true, reply, recommendedBooks, "LOCAL_FALLBACK", message);
    }

    public static AiChatResponse failed(String message) {
        return new AiChatResponse(false, "", List.of(), "ERROR", message);
    }
}
