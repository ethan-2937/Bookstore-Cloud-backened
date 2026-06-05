package com.hlju.bookstore.dto;

import java.util.Map;

public record AiToolAction(
        String type,
        String label,
        String description,
        boolean confirmRequired,
        Map<String, Object> payload
) {
}
