package com.shopcard.order.domain.ports.in;

import com.shopcard.order.domain.model.Order;

public record OrderResult(Order order, String pickupCodePlain) {}
