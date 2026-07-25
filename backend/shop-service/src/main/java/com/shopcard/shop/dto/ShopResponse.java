package com.shopcard.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopResponse {
    private UUID id;
    private UUID ownerId;
    private String name;
    private String description;
    private String address;
    private Double latitude;
    private Double longitude;
    private String phone;
    private String email;
    private String logoUrl;
    private String bannerUrl;
    private Boolean isVerified;
    private String businessType;
    private String shopType;
    private long followerCount;
}
