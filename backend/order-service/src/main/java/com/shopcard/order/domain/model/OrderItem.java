package com.shopcard.order.domain.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    private Long id;
    private UUID productId;
    private String name;
    private BigDecimal price;
    private Integer quantity;
}
