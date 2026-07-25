package com.shopcard.inventory.controller;

import com.shopcard.inventory.domain.model.Inventory;
import com.shopcard.inventory.domain.ports.in.GetInventoryUseCase;
import com.shopcard.inventory.domain.ports.in.UpdateStockUseCase;
import com.shopcard.inventory.dto.InventoryResponse;
import com.shopcard.inventory.dto.UpdateStockRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final UpdateStockUseCase updateStockUseCase;
    private final GetInventoryUseCase getInventoryUseCase;

    @PostMapping("/stock-in")
    public ResponseEntity<InventoryResponse> stockIn(
            @RequestParam UUID shopId,
            @Valid @RequestBody UpdateStockRequest request
    ) {
        Inventory updated = updateStockUseCase.updateStock(
                shopId,
                request.getProductId(),
                request.getQuantity(),
                "STOCK_IN",
                "MANUAL_ENTRY"
        );
        return ResponseEntity.ok(mapToResponse(updated));
    }

    @GetMapping
    public ResponseEntity<List<InventoryResponse>> getInventory(@RequestParam UUID shopId) {
        List<Inventory> inventoryList = getInventoryUseCase.getInventoryByShop(shopId);
        List<InventoryResponse> responses = inventoryList.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    private InventoryResponse mapToResponse(Inventory inventory) {
        return InventoryResponse.builder()
                .productId(inventory.getProductId())
                .shopId(inventory.getShopId())
                .stockQty(inventory.getStockQty())
                .reservedQty(inventory.getReservedQty())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}
