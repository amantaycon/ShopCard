package com.shopcard.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PickupVerifyRequest {
    @NotBlank(message = "Pickup OTP code is required")
    private String pickupCode;
}
