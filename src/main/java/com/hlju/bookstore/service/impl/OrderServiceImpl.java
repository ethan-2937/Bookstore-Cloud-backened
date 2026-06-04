package com.hlju.bookstore.service.impl;

import com.hlju.bookstore.entity.Book;
import com.hlju.bookstore.entity.BookOrder;
import com.hlju.bookstore.entity.OrderItem;
import com.hlju.bookstore.entity.User;
import com.hlju.bookstore.repository.BookRepository;
import com.hlju.bookstore.repository.OrderRepository;
import com.hlju.bookstore.repository.UserRepository;
import com.hlju.bookstore.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public OrderServiceImpl(OrderRepository orderRepository, BookRepository bookRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Map<String, Object> createOrder(String username, Map<String, Object> data) {
        try {
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return result(false, "User not found");
            }

            Object rawItems = data.get("items");
            if (!(rawItems instanceof List<?> requestItems) || requestItems.isEmpty()) {
                return result(false, "Order items cannot be empty");
            }

            List<OrderItem> orderItems = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (Object rawItem : requestItems) {
                if (!(rawItem instanceof Map<?, ?> itemData)) {
                    return result(false, "Invalid order item");
                }
                Integer bookId = toInteger(itemData.get("bookId"));
                Integer quantity = toInteger(itemData.get("quantity"));
                if (bookId == null || quantity == null || quantity <= 0) {
                    return result(false, "Invalid bookId or quantity");
                }

                Book book = bookRepository.findById(bookId);
                if (book == null) {
                    return result(false, "Book not found: " + bookId);
                }
                if (book.getStock() == null || book.getStock() < quantity) {
                    return result(false, "Not enough stock: " + book.getTitle());
                }

                BigDecimal price = book.getPrice() == null ? BigDecimal.ZERO : book.getPrice();
                BigDecimal subtotal = price.multiply(BigDecimal.valueOf(quantity));
                totalAmount = totalAmount.add(subtotal);

                OrderItem item = new OrderItem();
                item.setBookId(bookId);
                item.setBookTitle(book.getTitle());
                item.setPrice(price);
                item.setQuantity(quantity);
                item.setSubtotal(subtotal);
                orderItems.add(item);
            }

            BookOrder order = new BookOrder();
            order.setUserId(user.getId());
            order.setTotalAmount(totalAmount);
            order.setStatus("PENDING");
            order.setReceiverName(toStringValue(data.get("receiverName")));
            order.setReceiverPhone(toStringValue(data.get("receiverPhone")));
            order.setReceiverAddress(toStringValue(data.get("receiverAddress")));
            order.setCreatedTime(LocalDateTime.now());
            order.setUpdatedTime(LocalDateTime.now());

            Integer orderId = orderRepository.saveOrder(order);
            if (orderId == null) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return result(false, "Create order failed");
            }

            for (OrderItem item : orderItems) {
                if (!bookRepository.decreaseStock(item.getBookId(), item.getQuantity())) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return result(false, "Not enough stock: " + item.getBookTitle());
                }
                item.setOrderId(orderId);
                if (!orderRepository.saveItem(item)) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return result(false, "Save order item failed");
                }
            }

            Map<String, Object> response = result(true, "Order created");
            response.put("orderId", orderId);
            response.put("totalAmount", totalAmount);
            response.put("status", "PENDING");
            return response;
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return result(false, "Create order failed: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> findMyOrders(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> response = new ArrayList<>();
        for (BookOrder order : orderRepository.findByUserId(user.getId())) {
            response.add(orderWithItems(order));
        }
        return response;
    }

    @Override
    public List<BookOrder> findAllOrders(String role) {
        if (!isAdmin(role)) {
            return new ArrayList<>();
        }
        return orderRepository.findAll();
    }

    @Override
    public Map<String, Object> findOrderDetail(Integer orderId, String username, String role) {
        BookOrder order = orderRepository.findById(orderId);
        if (order == null) {
            return result(false, "Order not found");
        }
        if (!isAdmin(role)) {
            User user = userRepository.findByUsername(username);
            if (user == null || !user.getId().equals(order.getUserId())) {
                return result(false, "No permission");
            }
        }
        Map<String, Object> response = result(true, "OK");
        response.putAll(orderWithItems(order));
        return response;
    }

    @Override
    public Map<String, Object> payOrder(Integer orderId, String username) {
        User user = userRepository.findByUsername(username);
        BookOrder order = orderRepository.findById(orderId);
        if (user == null || order == null || !user.getId().equals(order.getUserId())) {
            return result(false, "Order not found");
        }
        if (!"PENDING".equals(order.getStatus())) {
            return result(false, "Only pending orders can be paid");
        }
        boolean success = orderRepository.updateStatus(orderId, "PAID");
        return result(success, success ? "Order paid" : "Pay order failed");
    }

    @Override
    @Transactional
    public Map<String, Object> cancelOrder(Integer orderId, String username) {
        User user = userRepository.findByUsername(username);
        BookOrder order = orderRepository.findById(orderId);
        if (user == null || order == null || !user.getId().equals(order.getUserId())) {
            return result(false, "Order not found");
        }
        if (!"PENDING".equals(order.getStatus())) {
            return result(false, "Only pending orders can be cancelled");
        }
        return cancelAndRestoreStock(order);
    }

    @Override
    @Transactional
    public Map<String, Object> updateOrderStatus(Integer orderId, String status, String role) {
        if (!isAdmin(role)) {
            return result(false, "No permission");
        }
        String targetStatus = status == null ? "" : status.trim().toUpperCase();
        List<String> validStatuses = Arrays.asList("PENDING", "PAID", "SHIPPED", "COMPLETED", "CANCELLED");
        if (!validStatuses.contains(targetStatus)) {
            return result(false, "Invalid order status");
        }

        BookOrder order = orderRepository.findById(orderId);
        if (order == null) {
            return result(false, "Order not found");
        }
        if ("CANCELLED".equals(order.getStatus())) {
            return result(false, "Cancelled order cannot be changed");
        }
        if ("CANCELLED".equals(targetStatus)) {
            return cancelAndRestoreStock(order);
        }

        boolean success = orderRepository.updateStatus(orderId, targetStatus);
        return result(success, success ? "Order status updated" : "Update order status failed");
    }

    private Map<String, Object> cancelAndRestoreStock(BookOrder order) {
        try {
            if (!orderRepository.updateStatus(order.getId(), "CANCELLED")) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return result(false, "Cancel order failed");
            }
            for (OrderItem item : orderRepository.findItemsByOrderId(order.getId())) {
                if (!bookRepository.increaseStock(item.getBookId(), item.getQuantity())) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return result(false, "Restore stock failed");
                }
            }
            return result(true, "Order cancelled");
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return result(false, "Cancel order failed: " + e.getMessage());
        }
    }

    private Map<String, Object> orderWithItems(BookOrder order) {
        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        data.put("items", orderRepository.findItemsByOrderId(order.getId()));
        return data;
    }

    private Map<String, Object> result(boolean success, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        return response;
    }

    private boolean isAdmin(String role) {
        return "ADMIN".equalsIgnoreCase(role);
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }

    private String toStringValue(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}
