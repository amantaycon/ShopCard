package com.shopcard.order.service;

import com.shopcard.order.domain.model.Order;
import com.shopcard.order.domain.model.OrderItem;
import com.shopcard.order.domain.ports.in.OrderResult;
import com.shopcard.order.domain.ports.out.EventPublisherPort;
import com.shopcard.order.domain.ports.out.OrderRepositoryPort;
import com.shopcard.order.domain.ports.out.OrderStatusLogRepositoryPort;
import com.shopcard.order.domain.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.ZonedDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    @Mock
    private OrderStatusLogRepositoryPort orderStatusLogRepositoryPort;

    @Mock
    private EventPublisherPort eventPublisherPort;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_shouldCalculateTotalAndGenerateHashedOtp() {
        UUID customerId = UUID.randomUUID();
        UUID shopId = UUID.randomUUID();
        OrderItem item1 = OrderItem.builder()
                .productId(UUID.randomUUID())
                .name("Product A")
                .price(new BigDecimal("10.00"))
                .quantity(2)
                .build();
        OrderItem item2 = OrderItem.builder()
                .productId(UUID.randomUUID())
                .name("Product B")
                .price(new BigDecimal("15.50"))
                .quantity(1)
                .build();
        List<OrderItem> items = List.of(item1, item2);

        when(orderRepositoryPort.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(UUID.randomUUID());
            return order;
        });

        OrderResult result = orderService.createOrder(customerId, shopId, items);

        assertThat(result.order()).isNotNull();
        assertThat(result.order().getId()).isNotNull();
        assertThat(result.order().getCustomerId()).isEqualTo(customerId);
        assertThat(result.order().getShopId()).isEqualTo(shopId);
        assertThat(result.order().getTotalAmount()).isEqualByComparingTo(new BigDecimal("35.50"));
        assertThat(result.order().getStatus()).isEqualTo("CREATED");
        assertThat(result.pickupCodePlain()).hasSize(6);

        // Verify hash matches combined format
        String[] parts = result.order().getPickupCodeHash().split(":");
        assertThat(parts).hasSize(2);
        String hashedOtp = parts[0];
        String salt = parts[1];
        assertThat(hashedOtp).isEqualTo(hashPassword(result.pickupCodePlain(), salt));

        verify(orderRepositoryPort).save(any(Order.class));
        verify(orderStatusLogRepositoryPort).save(any());
        verify(eventPublisherPort).publishOrderEvent(eq("order.placed"), any(Order.class));
    }

    @Test
    void handleInventoryReservation_success_shouldTransitionToPreparing() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder()
                .id(orderId)
                .status("CREATED")
                .build();

        when(orderRepositoryPort.findById(orderId)).thenReturn(Optional.of(order));

        orderService.handleInventoryReservation(orderId, "SUCCESS", "");

        assertThat(order.getStatus()).isEqualTo("PREPARING");
        verify(orderRepositoryPort).save(order);
        verify(orderStatusLogRepositoryPort).save(any());
        verify(eventPublisherPort).publishOrderEvent(eq("order.preparing"), eq(order));
    }

    @Test
    void handleInventoryReservation_failed_shouldTransitionToCancelled() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder()
                .id(orderId)
                .status("CREATED")
                .build();

        when(orderRepositoryPort.findById(orderId)).thenReturn(Optional.of(order));

        orderService.handleInventoryReservation(orderId, "FAILED", "Out of stock");

        assertThat(order.getStatus()).isEqualTo("CANCELLED");
        verify(orderRepositoryPort).save(order);
        verify(orderStatusLogRepositoryPort).save(any());
        verify(eventPublisherPort, never()).publishOrderEvent(anyString(), any(Order.class));
    }

    @Test
    void markAsReadyForPickup_unauthorizedShop_shouldThrowException() {
        UUID orderId = UUID.randomUUID();
        UUID correctShopId = UUID.randomUUID();
        UUID wrongShopId = UUID.randomUUID();
        Order order = Order.builder()
                .id(orderId)
                .shopId(correctShopId)
                .status("PREPARING")
                .build();

        when(orderRepositoryPort.findById(orderId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.markAsReadyForPickup(wrongShopId, orderId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unauthorized for this shop");

        verify(orderRepositoryPort, never()).save(any());
    }

    @Test
    void markAsReadyForPickup_notPreparing_shouldThrowException() {
        UUID orderId = UUID.randomUUID();
        UUID shopId = UUID.randomUUID();
        Order order = Order.builder()
                .id(orderId)
                .shopId(shopId)
                .status("CREATED")
                .build();

        when(orderRepositoryPort.findById(orderId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.markAsReadyForPickup(shopId, orderId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Order is not in PREPARING status");

        verify(orderRepositoryPort, never()).save(any());
    }

    @Test
    void markAsReadyForPickup_valid_shouldTransitionToReadyForPickup() {
        UUID orderId = UUID.randomUUID();
        UUID shopId = UUID.randomUUID();
        Order order = Order.builder()
                .id(orderId)
                .shopId(shopId)
                .status("PREPARING")
                .build();

        when(orderRepositoryPort.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepositoryPort.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.markAsReadyForPickup(shopId, orderId);

        assertThat(result.getStatus()).isEqualTo("READY_FOR_PICKUP");
        verify(orderRepositoryPort).save(order);
        verify(orderStatusLogRepositoryPort).save(any());
        verify(eventPublisherPort).publishOrderEvent(eq("order.ready-for-pickup"), eq(order));
    }

    @Test
    void verifyPickupAndComplete_expiredCode_shouldThrowException() {
        UUID orderId = UUID.randomUUID();
        UUID shopId = UUID.randomUUID();
        Order order = Order.builder()
                .id(orderId)
                .shopId(shopId)
                .status("READY_FOR_PICKUP")
                .pickupCodeExpiry(ZonedDateTime.now().minusMinutes(1))
                .build();

        when(orderRepositoryPort.findById(orderId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.verifyPickupAndComplete(shopId, orderId, "123456"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Pickup code has expired");
    }

    @Test
    void verifyPickupAndComplete_wrongCode_shouldThrowException() {
        UUID orderId = UUID.randomUUID();
        UUID shopId = UUID.randomUUID();
        String correctCode = "123456";
        String salt = "abcdefgh";
        String hash = hashPassword(correctCode, salt);
        Order order = Order.builder()
                .id(orderId)
                .shopId(shopId)
                .status("READY_FOR_PICKUP")
                .pickupCodeHash(hash + ":" + salt)
                .pickupCodeExpiry(ZonedDateTime.now().plusHours(1))
                .build();

        when(orderRepositoryPort.findById(orderId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.verifyPickupAndComplete(shopId, orderId, "111111"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid pickup code verification");
    }

    @Test
    void verifyPickupAndComplete_correctCode_shouldTransitionToPickedUp() {
        UUID orderId = UUID.randomUUID();
        UUID shopId = UUID.randomUUID();
        String correctCode = "123456";
        String salt = "abcdefgh";
        String hash = hashPassword(correctCode, salt);
        Order order = Order.builder()
                .id(orderId)
                .shopId(shopId)
                .status("READY_FOR_PICKUP")
                .pickupCodeHash(hash + ":" + salt)
                .pickupCodeExpiry(ZonedDateTime.now().plusHours(1))
                .build();

        when(orderRepositoryPort.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepositoryPort.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.verifyPickupAndComplete(shopId, orderId, correctCode);

        assertThat(result.getStatus()).isEqualTo("PICKED_UP");
        verify(orderRepositoryPort).save(order);
        verify(orderStatusLogRepositoryPort).save(any());
        verify(eventPublisherPort).publishOrderEvent(eq("order.completed"), eq(order));
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
