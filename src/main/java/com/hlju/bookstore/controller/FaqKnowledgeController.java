package com.hlju.bookstore.controller;

import com.hlju.bookstore.entity.FaqKnowledge;
import com.hlju.bookstore.repository.FaqKnowledgeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/faqs")
public class FaqKnowledgeController {
    private final FaqKnowledgeRepository faqKnowledgeRepository;

    public FaqKnowledgeController(FaqKnowledgeRepository faqKnowledgeRepository) {
        this.faqKnowledgeRepository = faqKnowledgeRepository;
    }

    @GetMapping
    public List<FaqKnowledge> findFaqs(@RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) String category,
                                       @RequestParam(defaultValue = "USER") String role) {
        return faqKnowledgeRepository.findAll(keyword, category, "ADMIN".equalsIgnoreCase(role));
    }

    @GetMapping("/categories")
    public List<String> findCategories() {
        return faqKnowledgeRepository.findCategories();
    }

    @PostMapping
    public Map<String, Object> addFaq(@RequestBody FaqKnowledge item,
                                      @RequestParam(defaultValue = "USER") String role) {
        requireAdmin(role);
        boolean success = faqKnowledgeRepository.save(item);
        return Map.of("success", success, "message", success ? "FAQ created" : "Create FAQ failed");
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateFaq(@PathVariable Integer id,
                                         @RequestBody FaqKnowledge item,
                                         @RequestParam(defaultValue = "USER") String role) {
        requireAdmin(role);
        boolean success = faqKnowledgeRepository.update(id, item);
        return Map.of("success", success, "message", success ? "FAQ updated" : "Update FAQ failed");
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteFaq(@PathVariable Integer id,
                                         @RequestParam(defaultValue = "USER") String role) {
        requireAdmin(role);
        boolean success = faqKnowledgeRepository.delete(id);
        return Map.of("success", success, "message", success ? "FAQ deleted" : "Delete FAQ failed");
    }

    private void requireAdmin(String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No permission");
        }
    }
}
