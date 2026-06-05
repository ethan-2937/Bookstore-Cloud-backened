package com.hlju.bookstore.dto;

import com.hlju.bookstore.entity.Book;
import com.hlju.bookstore.entity.FaqKnowledge;

import java.util.List;

public record AiChatResponse(
        boolean success,
        String reply,
        List<Book> recommendedBooks,
        List<FaqKnowledge> matchedFaqs,
        List<AiToolAction> toolActions,
        List<AiToolResult> toolResults,
        String source,
        String message
) {
    public static AiChatResponse success(String reply, List<Book> recommendedBooks, String source) {
        return success(reply, recommendedBooks, List.of(), List.of(), List.of(), source);
    }

    public static AiChatResponse success(String reply,
                                         List<Book> recommendedBooks,
                                         List<FaqKnowledge> matchedFaqs,
                                         List<AiToolAction> toolActions,
                                         List<AiToolResult> toolResults,
                                         String source) {
        return new AiChatResponse(true, reply, recommendedBooks, matchedFaqs, toolActions, toolResults, source, null);
    }

    public static AiChatResponse fallback(String reply, List<Book> recommendedBooks, String message) {
        return fallback(reply, recommendedBooks, List.of(), List.of(), List.of(), message);
    }

    public static AiChatResponse fallback(String reply,
                                          List<Book> recommendedBooks,
                                          List<FaqKnowledge> matchedFaqs,
                                          List<AiToolAction> toolActions,
                                          List<AiToolResult> toolResults,
                                          String message) {
        return new AiChatResponse(true, reply, recommendedBooks, matchedFaqs, toolActions, toolResults, "LOCAL_FALLBACK", message);
    }

    public static AiChatResponse failed(String message) {
        return new AiChatResponse(false, "", List.of(), List.of(), List.of(), List.of(), "ERROR", message);
    }
}
