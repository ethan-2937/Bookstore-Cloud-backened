package com.hlju.bookstore.service.impl;

import com.hlju.bookstore.entity.SupportMessage;
import com.hlju.bookstore.entity.SupportTicket;
import com.hlju.bookstore.entity.User;
import com.hlju.bookstore.repository.SupportRepository;
import com.hlju.bookstore.repository.UserRepository;
import com.hlju.bookstore.service.SupportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SupportServiceImpl implements SupportService {
    private final SupportRepository supportRepository;
    private final UserRepository userRepository;

    public SupportServiceImpl(SupportRepository supportRepository, UserRepository userRepository) {
        this.supportRepository = supportRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Map<String, Object> createTicket(String username, Map<String, Object> data) {
        try {
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return result(false, "User not found");
            }

            String subject = toStringValue(data.get("subject"));
            String content = toStringValue(data.get("content"));
            if (subject.isEmpty() || content.isEmpty()) {
                return result(false, "Subject and content cannot be empty");
            }

            SupportTicket ticket = new SupportTicket();
            ticket.setUserId(user.getId());
            ticket.setUsername(user.getUsername());
            ticket.setSubject(subject);
            ticket.setStatus("OPEN");
            ticket.setCreatedTime(LocalDateTime.now());
            ticket.setUpdatedTime(LocalDateTime.now());
            Integer ticketId = supportRepository.saveTicket(ticket);
            if (ticketId == null) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return result(false, "Create ticket failed");
            }

            SupportMessage message = buildMessage(ticketId, user.getId(), user.getUsername(), "USER", content);
            if (!supportRepository.saveMessage(message)) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return result(false, "Create first message failed");
            }

            Map<String, Object> response = result(true, "Ticket created");
            response.put("ticketId", ticketId);
            return response;
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return result(false, "Create ticket failed: " + e.getMessage());
        }
    }

    @Override
    public List<SupportTicket> findMyTickets(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return new ArrayList<>();
        }
        return supportRepository.findTicketsByUserId(user.getId());
    }

    @Override
    public List<SupportTicket> findAllTickets(String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return new ArrayList<>();
        }
        return supportRepository.findAllTickets();
    }

    @Override
    public Map<String, Object> findTicketDetail(Integer ticketId, String username, String role) {
        SupportTicket ticket = supportRepository.findTicketById(ticketId);
        if (ticket == null) {
            return result(false, "Ticket not found");
        }
        if (!canAccess(ticket, username, role)) {
            return result(false, "No permission");
        }
        Map<String, Object> response = result(true, "OK");
        response.put("ticket", ticket);
        response.put("messages", supportRepository.findMessagesByTicketId(ticketId));
        return response;
    }

    @Override
    @Transactional
    public Map<String, Object> replyTicket(Integer ticketId, String username, String role, Map<String, Object> data) {
        try {
            SupportTicket ticket = supportRepository.findTicketById(ticketId);
            if (ticket == null) {
                return result(false, "Ticket not found");
            }
            if (!canAccess(ticket, username, role)) {
                return result(false, "No permission");
            }
            if ("CLOSED".equals(ticket.getStatus())) {
                return result(false, "Closed ticket cannot be replied");
            }

            String content = toStringValue(data.get("content"));
            if (content.isEmpty()) {
                return result(false, "Reply content cannot be empty");
            }

            String senderRole = "ADMIN".equalsIgnoreCase(role) ? "ADMIN" : "USER";
            Integer senderId = null;
            User user = userRepository.findByUsername(username);
            if (user != null) {
                senderId = user.getId();
            }
            SupportMessage message = buildMessage(ticketId, senderId, username, senderRole, content);
            if (!supportRepository.saveMessage(message)) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return result(false, "Reply ticket failed");
            }

            String nextStatus = "ADMIN".equals(senderRole) ? "IN_PROGRESS" : "OPEN";
            supportRepository.updateTicketStatus(ticketId, nextStatus);
            return result(true, "Reply saved");
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return result(false, "Reply ticket failed: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> updateTicketStatus(Integer ticketId, String status, String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return result(false, "No permission");
        }
        String targetStatus = status == null ? "" : status.trim().toUpperCase();
        if (!Arrays.asList("OPEN", "IN_PROGRESS", "CLOSED").contains(targetStatus)) {
            return result(false, "Invalid ticket status");
        }
        boolean success = supportRepository.updateTicketStatus(ticketId, targetStatus);
        return result(success, success ? "Ticket status updated" : "Update ticket status failed");
    }

    private boolean canAccess(SupportTicket ticket, String username, String role) {
        if ("ADMIN".equalsIgnoreCase(role)) {
            return true;
        }
        User user = userRepository.findByUsername(username);
        return user != null && user.getId().equals(ticket.getUserId());
    }

    private SupportMessage buildMessage(Integer ticketId, Integer senderId, String senderUsername, String senderRole, String content) {
        SupportMessage message = new SupportMessage();
        message.setTicketId(ticketId);
        message.setSenderId(senderId);
        message.setSenderUsername(senderUsername);
        message.setSenderRole(senderRole);
        message.setContent(content);
        message.setCreatedTime(LocalDateTime.now());
        return message;
    }

    private Map<String, Object> result(boolean success, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        return response;
    }

    private String toStringValue(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}
