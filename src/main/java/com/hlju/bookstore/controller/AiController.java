package com.hlju.bookstore.controller;

import com.hlju.bookstore.dto.AiChatRequest;
import com.hlju.bookstore.dto.AiChatResponse;
import com.hlju.bookstore.service.AiCustomerService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final AiCustomerService aiCustomerService;

    public AiController(AiCustomerService aiCustomerService) {
        this.aiCustomerService = aiCustomerService;
    }

    @PostMapping("/chat")
    public AiChatResponse chat(@RequestBody AiChatRequest request,
                               @RequestParam(required = false) String username,
                               @RequestParam(defaultValue = "USER") String role) {
        return aiCustomerService.chat(request, username, role);
    }
}
