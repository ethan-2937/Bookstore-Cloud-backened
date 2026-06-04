package com.hlju.bookstore.entity;

import java.time.LocalDateTime;

public class SupportMessage {
    private Integer id;
    private Integer ticketId;
    private Integer senderId;
    private String senderUsername;
    private String senderRole;
    private String content;
    private LocalDateTime createdTime;

    public SupportMessage() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getTicketId() { return ticketId; }
    public void setTicketId(Integer ticketId) { this.ticketId = ticketId; }
    public Integer getSenderId() { return senderId; }
    public void setSenderId(Integer senderId) { this.senderId = senderId; }
    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }
    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
}
