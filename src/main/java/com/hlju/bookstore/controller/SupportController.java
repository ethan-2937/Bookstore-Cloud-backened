package com.hlju.bookstore.controller;

import com.hlju.bookstore.entity.SupportTicket;
import com.hlju.bookstore.service.SupportService;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/support/tickets")
public class SupportController {
    private final SupportService supportService;

    public SupportController(SupportService supportService) {
        this.supportService = supportService;
    }

    @PostMapping
    public Map<String, Object> createTicket(@RequestBody Map<String, Object> data,
                                            @RequestParam String username) {
        return supportService.createTicket(username, data);
    }

    @GetMapping("/my")
    public List<SupportTicket> myTickets(@RequestParam String username) {
        return supportService.findMyTickets(username);
    }

    @GetMapping
    public List<SupportTicket> allTickets(@RequestParam String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No permission");
        }
        return supportService.findAllTickets(role);
    }

    @GetMapping("/{id}")
    public Map<String, Object> ticketDetail(@PathVariable Integer id,
                                            @RequestParam String username,
                                            @RequestParam(defaultValue = "USER") String role) {
        return supportService.findTicketDetail(id, username, role);
    }

    @PostMapping("/{id}/messages")
    public Map<String, Object> replyTicket(@PathVariable Integer id,
                                           @RequestBody Map<String, Object> data,
                                           @RequestParam String username,
                                           @RequestParam(defaultValue = "USER") String role) {
        return supportService.replyTicket(id, username, role, data);
    }

    @PutMapping("/{id}/status")
    public Map<String, Object> updateTicketStatus(@PathVariable Integer id,
                                                  @RequestBody Map<String, Object> data,
                                                  @RequestParam String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No permission");
        }
        return supportService.updateTicketStatus(id, data.get("status") == null ? null : String.valueOf(data.get("status")), role);
    }
}
