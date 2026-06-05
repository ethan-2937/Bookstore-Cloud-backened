package com.hlju.bookstore.service;

import com.hlju.bookstore.dto.AiChatRequest;
import com.hlju.bookstore.dto.AiChatResponse;

public interface AiCustomerService {
    AiChatResponse chat(AiChatRequest request, String username, String role);
}
