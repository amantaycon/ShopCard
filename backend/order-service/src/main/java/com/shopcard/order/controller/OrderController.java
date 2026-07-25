package com.shopcard.order.controller;

import com.shopcard.order.domain.model.Order;
import com.shopcard.order.domain.model.OrderItem;
import com.shopcard.order.domain.ports.in.*;
import com.shopcard.order.dto.OrderItemDto;
import com.shopcard.order.dto.OrderRequest;
import com.shopcard.order.dto.OrderResponse;
import com.shopcard.order.dto.PickupVerifyRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final MarkOrderReadyUseCase markOrderReadyUseCase;
    private final VerifyPickupUseCase verifyPickupUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestHeader("X-User-Id") String customerId,
            @Valid @RequestBody OrderRequest request
    ) {
        List<OrderItem> domainItems = request.getItems().stream()
                .map(dto -> OrderItem.builder()
                        .productId(dto.getProductId())
                        .name(dto.getName())
                        .price(dto.getPrice())
                        .quantity(dto.getQuantity())
                        .build())
                .collect(Collectors.toList());

        OrderResult result = createOrderUseCase.createOrder(
                UUID.fromString(customerId),
                request.getShopId(),
                domainItems
        );

        OrderResponse response = mapToResponse(result.order());
        response.setPickupCodePlain(result.pickupCodePlain());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID id) {
        Order order = getOrderUseCase.getOrder(id);
        return ResponseEntity.ok(mapToResponse(order));
    }

    @GetMapping("/customer")
    public ResponseEntity<List<OrderResponse>> getCustomerOrders(@RequestHeader("X-User-Id") String customerId) {
        List<Order> orders = getOrderUseCase.getCustomerOrders(UUID.fromString(customerId));
        List<OrderResponse> responses = orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/shop")
    public ResponseEntity<List<OrderResponse>> getShopOrders(@RequestParam UUID shopId) {
        List<Order> orders = getOrderUseCase.getShopOrders(shopId);
        List<OrderResponse> responses = orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{id}/ready")
    public ResponseEntity<OrderResponse> markAsReady(
            @RequestHeader("X-User-Id") String ownerId,
            @PathVariable UUID id,
            @RequestParam UUID shopId
    ) {
        Order order = markOrderReadyUseCase.markAsReadyForPickup(shopId, id);
        return ResponseEntity.ok(mapToResponse(order));
    }

    @PostMapping("/{id}/pickup")
    public ResponseEntity<OrderResponse> verifyPickup(
            @RequestHeader("X-User-Id") String ownerId,
            @PathVariable UUID id,
            @RequestParam UUID shopId,
            @Valid @RequestBody PickupVerifyRequest request
    ) {
        Order order = verifyPickupUseCase.verifyPickupAndComplete(shopId, id, request.getPickupCode());
        return ResponseEntity.ok(mapToResponse(order));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancel(
            @RequestHeader("X-User-Id") String actorId,
            @PathVariable UUID id
    ) {
        Order order = cancelOrderUseCase.cancelOrder(UUID.fromString(actorId), id);
        return ResponseEntity.ok(mapToResponse(order));
    }

    private OrderResponse mapToResponse(Order order) {
        if (order == null) {
            return null;
        }
        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(item -> {
                    OrderItemDto dto = new OrderItemDto();
                    dto.setProductId(item.getProductId());
                    dto.setName(item.getName());
                    dto.setPrice(item.getPrice());
                    dto.setQuantity(item.getQuantity());
                    return dto;
                }).collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .shopId(order.getShopId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .items(itemDtos)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
