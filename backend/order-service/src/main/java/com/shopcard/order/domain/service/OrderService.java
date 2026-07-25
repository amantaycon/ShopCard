package com.shopcard.order.domain.service;

import com.shopcard.order.domain.model.Order;
import com.shopcard.order.domain.model.OrderItem;
import com.shopcard.order.domain.model.OrderStatusLog;
import com.shopcard.order.domain.ports.in.*;
import com.shopcard.order.domain.ports.out.EventPublisherPort;
import com.shopcard.order.domain.ports.out.OrderRepositoryPort;
import com.shopcard.order.domain.ports.out.OrderStatusLogRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service("domainOrderService")
@RequiredArgsConstructor
public class OrderService implements 
        CreateOrderUseCase, 
        HandleInventoryReservationUseCase, 
        MarkOrderReadyUseCase, 
        VerifyPickupUseCase, 
        CancelOrderUseCase, 
        GetOrderUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final OrderStatusLogRepositoryPort orderStatusLogRepositoryPort;
    private final EventPublisherPort eventPublisherPort;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public OrderResult createOrder(UUID customerId, UUID shopId, List<OrderItem> items) {
        BigDecimal total = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Generate 6 digit numeric pickup OTP code
        String pickupCode = String.format("%06d", secureRandom.nextInt(1000000));
        String salt = UUID.randomUUID().toString().substring(0, 8);
        String hashedCode = hashPassword(pickupCode, salt);

        Order order = Order.builder()
                .customerId(customerId)
                .shopId(shopId)
                .totalAmount(total)
                .status("CREATED")
                .pickupCodeHash(hashedCode + ":" + salt) // combined format
                .pickupCodeExpiry(ZonedDateTime.now().plusDays(2))
                .items(items)
                .build();

        Order savedOrder = orderRepositoryPort.save(order);

        logStatusChange(savedOrder.getId(), null, "CREATED", "Order registered, awaiting stock reservation.", customerId);

        // Publish to Kafka to trigger inventory allocation
        eventPublisherPort.publishOrderEvent("order.placed", savedOrder);

        return new OrderResult(savedOrder, pickupCode);
    }

    @Override
    @Transactional
    public void handleInventoryReservation(UUID orderId, String status, String reason) {
        Optional<Order> orderOpt = orderRepositoryPort.findById(orderId);
        if (orderOpt.isEmpty()) return;

        Order order = orderOpt.get();
        String oldStatus = order.getStatus();
        if (!"CREATED".equals(oldStatus)) {
            return; // Guard against out of order messages
        }

        if ("SUCCESS".equals(status)) {
            order.setStatus("PREPARING");
            orderRepositoryPort.save(order);
            logStatusChange(orderId, oldStatus, "PREPARING", "Stock reserved successfully.", UUID.fromString("00000000-0000-0000-0000-000000000000"));
            
            // Publish preparing event for notification dispatch
            eventPublisherPort.publishOrderEvent("order.preparing", order);
        } else {
            order.setStatus("CANCELLED");
            orderRepositoryPort.save(order);
            logStatusChange(orderId, oldStatus, "CANCELLED", "Stock reservation failed: " + reason, UUID.fromString("00000000-0000-0000-0000-000000000000"));
        }
    }

    @Override
    @Transactional
    public Order markAsReadyForPickup(UUID shopId, UUID orderId) {
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getShopId().equals(shopId)) {
            throw new RuntimeException("Unauthorized for this shop");
        }

        if (!"PREPARING".equals(order.getStatus())) {
            throw new RuntimeException("Order is not in PREPARING status");
        }

        String oldStatus = order.getStatus();
        order.setStatus("READY_FOR_PICKUP");
        Order saved = orderRepositoryPort.save(order);

        logStatusChange(orderId, oldStatus, "READY_FOR_PICKUP", "Items are packed. Awaiting pickup.", shopId);

        // Publish event for pickup OTP dispatch
        eventPublisherPort.publishOrderEvent("order.ready-for-pickup", saved);

        return saved;
    }

    @Override
    @Transactional
    public Order verifyPickupAndComplete(UUID shopId, UUID orderId, String plainCode) {
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getShopId().equals(shopId)) {
            throw new RuntimeException("Unauthorized for this shop");
        }

        if (!"READY_FOR_PICKUP".equals(order.getStatus())) {
            throw new RuntimeException("Order is not READY_FOR_PICKUP");
        }

        if (ZonedDateTime.now().isAfter(order.getPickupCodeExpiry())) {
            throw new RuntimeException("Pickup code has expired");
        }

        String[] parts = order.getPickupCodeHash().split(":");
        String hashedDb = parts[0];
        String salt = parts[1];

        if (!hashedDb.equals(hashPassword(plainCode, salt))) {
            throw new RuntimeException("Invalid pickup code verification");
        }

        String oldStatus = order.getStatus();
        order.setStatus("PICKED_UP");
        Order saved = orderRepositoryPort.save(order);

        logStatusChange(orderId, oldStatus, "PICKED_UP", "Verified pickup code, order completed.", shopId);

        // Publish completed topic to deduct stock in inventory-service
        eventPublisherPort.publishOrderEvent("order.completed", saved);

        return saved;
    }

    @Override
    @Transactional
    public Order cancelOrder(UUID actorId, UUID orderId) {
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        String oldStatus = order.getStatus();
        if ("PICKED_UP".equals(oldStatus) || "CANCELLED".equals(oldStatus)) {
            throw new RuntimeException("Cannot cancel an order in state: " + oldStatus);
        }

        order.setStatus("CANCELLED");
        Order saved = orderRepositoryPort.save(order);

        logStatusChange(orderId, oldStatus, "CANCELLED", "Order cancelled by user.", actorId);

        // Release reserved stocks
        eventPublisherPort.publishOrderEvent("order.cancelled", saved);

        return saved;
    }

    @Override
    public Order getOrder(UUID id) {
        return orderRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Override
    public List<Order> getCustomerOrders(UUID customerId) {
        return orderRepositoryPort.findByCustomerId(customerId);
    }

    @Override
    public List<Order> getShopOrders(UUID shopId) {
        return orderRepositoryPort.findByShopId(shopId);
    }

    private void logStatusChange(UUID orderId, String fromStatus, String toStatus, String remarks, UUID changedBy) {
        orderStatusLogRepositoryPort.save(OrderStatusLog.builder()
                .orderId(orderId)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .remarks(remarks)
                .changedBy(changedBy)
                .build());
    }

    private String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((password + salt).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Hashing calculation failed", e);
        }
    }
}
