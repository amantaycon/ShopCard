package com.shopcard.catalog.controller;

import com.shopcard.catalog.domain.model.ImportJob;
import com.shopcard.catalog.domain.model.Product;
import com.shopcard.catalog.domain.ports.in.*;
import com.shopcard.catalog.dto.ProductRequest;
import com.shopcard.catalog.dto.ProductResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class CatalogController {

    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final GetProductsByShopUseCase getProductsByShopUseCase;
    private final SearchProductsUseCase searchProductsUseCase;
    private final BulkImportUseCase bulkImportUseCase;
    private final GetImportJobStatusUseCase getImportJobStatusUseCase;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @RequestParam UUID shopId,
            @Valid @RequestBody ProductRequest request
    ) {
        ProductCommand command = toCommand(request);
        Product product = createProductUseCase.createProduct(shopId, command);
        return ResponseEntity.ok(mapToResponse(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @RequestParam UUID shopId,
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequest request
    ) {
        ProductCommand command = toCommand(request);
        Product product = updateProductUseCase.updateProduct(shopId, id, command);
        return ResponseEntity.ok(mapToResponse(product));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getProducts(@RequestParam UUID shopId) {
        List<Product> products = getProductsByShopUseCase.getProductsByShop(shopId);
        List<ProductResponse> responses = products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> search(@RequestParam String query) {
        List<Product> products = searchProductsUseCase.searchProducts(query);
        List<ProductResponse> responses = products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/import")
    public ResponseEntity<ImportJob> uploadFile(
            @RequestParam UUID shopId,
            @RequestParam("file") MultipartFile file
    ) throws Exception {
        ImportJob job = bulkImportUseCase.startImport(shopId, file.getOriginalFilename(), file.getInputStream());
        return ResponseEntity.ok(job);
    }

    @GetMapping("/import/jobs/{jobId}")
    public ResponseEntity<ImportJob> getImportJobStatus(@PathVariable UUID jobId) {
        ImportJob job = getImportJobStatusUseCase.getImportJobStatus(jobId);
        return ResponseEntity.ok(job);
    }

    private ProductCommand toCommand(ProductRequest request) {
        return new ProductCommand(
                request.getName(),
                request.getDescription(),
                request.getSku(),
                request.getPrice(),
                request.getImageUrl(),
                request.getCategoryName(),
                request.getIsAvailable()
        );
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .shopId(product.getShopId())
                .name(product.getName())
                .description(product.getDescription())
                .sku(product.getSku())
                .price(product.getPrice())
                .imageUrl(product.getImageUrl())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .isAvailable(product.getIsAvailable())
                .build();
    }
}
