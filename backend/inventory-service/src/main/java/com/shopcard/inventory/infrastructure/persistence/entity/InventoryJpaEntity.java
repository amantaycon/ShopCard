package com.shopcard.inventory.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "inventories",
        indexes = {
                @Index(name = "idx_inventory_shop_id", columnList = "shop_id")
        }
)
@Check(constraints = "stock_qty >= 0 and reserved_qty >= 0 and reserved_qty <= stock_qty")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryJpaEntity {

    @Id
    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "shop_id", nullable = false)
    private UUID shopId;

    @Column(name = "stock_qty", nullable = false)
    @Builder.Default
    private Integer stockQty = 0;

    @Column(name = "reserved_qty", nullable = false)
    @Builder.Default
    private Integer reservedQty = 0;

    @Version
    private Long version;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;
}
