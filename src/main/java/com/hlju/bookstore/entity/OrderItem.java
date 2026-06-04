package com.hlju.bookstore.entity;

import java.math.BigDecimal;

public class OrderItem {
    private Integer id;
    private Integer orderId;
    private Integer bookId;
    private String bookTitle;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;

    public OrderItem() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public Integer getBookId() { return bookId; }
    public void setBookId(Integer bookId) { this.bookId = bookId; }
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
