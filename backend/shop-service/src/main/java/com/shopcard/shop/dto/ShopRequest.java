package com.shopcard.shop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShopRequest {

    @NotBlank(message = "Shop name is required")
    private String name;

    private String description;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    private String phone;
    private String email;
    private String logoUrl;
    private String bannerUrl;
    private String businessType;
    private String shopType;
}
