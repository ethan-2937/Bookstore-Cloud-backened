package com.hlju.bookstore.service;

import com.hlju.bookstore.entity.BookOrder;

import java.util.List;
import java.util.Map;

public interface OrderService {
    Map<String, Object> createOrder(String username, Map<String, Object> data);
    List<Map<String, Object>> findMyOrders(String username);
    List<BookOrder> findAllOrders(String role);
    Map<String, Object> findOrderDetail(Integer orderId, String username, String role);
    Map<String, Object> payOrder(Integer orderId, String username);
    Map<String, Object> cancelOrder(Integer orderId, String username);
    Map<String, Object> updateOrderStatus(Integer orderId, String status, String role);
}
