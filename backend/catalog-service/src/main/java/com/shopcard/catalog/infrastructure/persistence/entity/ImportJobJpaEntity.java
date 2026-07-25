package com.shopcard.catalog.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "import_jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportJobJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "shop_id", nullable = false)
    private UUID shopId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(nullable = false, length = 50)
    private String status; // 'PENDING', 'PROCESSING', 'COMPLETED', 'FAILED'

    @Column(name = "total_records")
    @Builder.Default
    private Integer totalRecords = 0;

    @Column(name = "processed_records")
    @Builder.Default
    private Integer processedRecords = 0;

    @Column(name = "failed_records")
    @Builder.Default
    private Integer failedRecords = 0;

    @Column(name = "error_log", columnDefinition = "TEXT")
    private String errorLog;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;
}
