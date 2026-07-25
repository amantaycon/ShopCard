package com.shopcard.inventory.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "stock_transactions",
        indexes = {
                @Index(name = "idx_stock_tx_product_id", columnList = "product_id"),
                @Index(name = "idx_stock_tx_reference_type", columnList = "reference_id, transaction_type")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_stock_tx_idempotency_key", columnNames = "idempotency_key")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTransactionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "shop_id", nullable = false)
    private UUID shopId;

    @Column(name = "transaction_type", nullable = false, length = 50)
    private String transactionType; // 'STOCK_IN', 'ORDER_RESERVED', 'ORDER_RELEASED', 'ORDER_DEDUCTED'

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "reference_id")
    private String referenceId; // E.g. Order ID

    @Column(name = "stock_before", nullable = false)
    private Integer stockBefore;

    @Column(name = "stock_after", nullable = false)
    private Integer stockAfter;

    @Column(name = "reserved_before", nullable = false)
    private Integer reservedBefore;

    @Column(name = "reserved_after", nullable = false)
    private Integer reservedAfter;

    @Column(name = "idempotency_key", nullable = false, length = 160)
    private String idempotencyKey;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;
}
