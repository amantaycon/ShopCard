package com.shopcard.shop.domain.model;

import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shop {
    private UUID id;
    private UUID ownerId;
    private String name;
    private String description;
    private String address;
    private Coordinates coordinates;
    private String phone;
    private String email;
    private String logoUrl;
    private String bannerUrl;
    @Builder.Default
    private Boolean isVerified = false;
    private String businessType;
    private String shopType;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
