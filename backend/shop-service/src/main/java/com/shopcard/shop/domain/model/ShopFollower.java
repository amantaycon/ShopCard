package com.shopcard.shop.domain.model;

import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopFollower {
    private Long id;
    private Shop shop;
    private UUID customerId;
    private ZonedDateTime createdAt;
}
