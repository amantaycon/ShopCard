package com.shopcard.notification.domain.model;

import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    private Long id;
    private UUID userId;
    private String message;
    @Builder.Default
    private Boolean isRead = false;
    private ZonedDateTime timestamp;
}
