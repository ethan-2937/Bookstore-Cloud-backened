package com.hlju.bookstore.dto;

import java.util.Map;

public record AiToolResult(
        String type,
        String title,
        String content,
        Map<String, Object> data
) {
}
