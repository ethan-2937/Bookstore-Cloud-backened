package com.hlju.bookstore.service;

import com.hlju.bookstore.entity.SupportTicket;

import java.util.List;
import java.util.Map;

public interface SupportService {
    Map<String, Object> createTicket(String username, Map<String, Object> data);
    List<SupportTicket> findMyTickets(String username);
    List<SupportTicket> findAllTickets(String role);
    Map<String, Object> findTicketDetail(Integer ticketId, String username, String role);
    Map<String, Object> replyTicket(Integer ticketId, String username, String role, Map<String, Object> data);
    Map<String, Object> updateTicketStatus(Integer ticketId, String status, String role);
}
