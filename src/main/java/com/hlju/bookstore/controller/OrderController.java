package com.hlju.bookstore.controller;

import com.hlju.bookstore.entity.BookOrder;
import com.hlju.bookstore.service.OrderService;
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
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public Map<String, Object> createOrder(@RequestBody Map<String, Object> data,
                                           @RequestParam String username) {
        return orderService.createOrder(username, data);
    }

    @GetMapping("/my")
    public List<Map<String, Object>> myOrders(@RequestParam String username) {
        return orderService.findMyOrders(username);
    }

    @GetMapping("/{id}")
    public Map<String, Object> orderDetail(@PathVariable Integer id,
                                           @RequestParam String username,
                                           @RequestParam(defaultValue = "USER") String role) {
        return orderService.findOrderDetail(id, username, role);
    }

    @PostMapping("/{id}/pay")
    public Map<String, Object> payOrder(@PathVariable Integer id,
                                        @RequestParam String username) {
        return orderService.payOrder(id, username);
    }

    @PostMapping("/{id}/cancel")
    public Map<String, Object> cancelOrder(@PathVariable Integer id,
                                           @RequestParam String username) {
        return orderService.cancelOrder(id, username);
    }

    @GetMapping
    public List<BookOrder> allOrders(@RequestParam String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No permission");
        }
        return orderService.findAllOrders(role);
    }

    @PutMapping("/{id}/status")
    public Map<String, Object> updateOrderStatus(@PathVariable Integer id,
                                                 @RequestBody Map<String, Object> data,
                                                 @RequestParam String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No permission");
        }
        return orderService.updateOrderStatus(id, data.get("status") == null ? null : String.valueOf(data.get("status")), role);
    }
}
