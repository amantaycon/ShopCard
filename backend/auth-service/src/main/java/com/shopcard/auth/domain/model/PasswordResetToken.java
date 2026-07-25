package com.shopcard.auth.domain.model;

import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {
    private UUID id;
    private String token;
    private User user;
    private ZonedDateTime expiryDate;
}
