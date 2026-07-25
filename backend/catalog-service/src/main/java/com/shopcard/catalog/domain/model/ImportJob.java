package com.shopcard.catalog.domain.model;

import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportJob {
    private UUID id;
    private UUID shopId;
    private String fileName;
    @Builder.Default
    private String status = "PENDING";
    @Builder.Default
    private Integer totalRecords = 0;
    @Builder.Default
    private Integer processedRecords = 0;
    @Builder.Default
    private Integer failedRecords = 0;
    private String errorLog;
    private ZonedDateTime createdAt;
}
