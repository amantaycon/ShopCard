package com.shopcard.shop.controller;

import com.shopcard.shop.dto.ShopRequest;
import com.shopcard.shop.dto.ShopResponse;
import com.shopcard.shop.domain.ports.in.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/shops")
@RequiredArgsConstructor
public class ShopController {

    private final CreateOrUpdateShopUseCase createOrUpdateShopUseCase;
    private final GetShopUseCase getShopUseCase;
    private final GetNearbyShopsUseCase getNearbyShopsUseCase;
    private final FollowShopUseCase followShopUseCase;
    private final UnfollowShopUseCase unfollowShopUseCase;

    @PostMapping
    public ResponseEntity<ShopResponse> createOrUpdateShop(
            @RequestHeader("X-User-Id") String ownerId,
            @Valid @RequestBody ShopRequest request
    ) {
        ShopCommand command = new ShopCommand(
                request.getName(),
                request.getDescription(),
                request.getAddress(),
                request.getLatitude(),
                request.getLongitude(),
                request.getPhone(),
                request.getEmail(),
                request.getLogoUrl(),
                request.getBannerUrl(),
                request.getBusinessType(),
                request.getShopType()
        );
        ShopResult result = createOrUpdateShopUseCase.createOrUpdateShop(UUID.fromString(ownerId), command);
        return ResponseEntity.ok(mapToResponse(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShopResponse> getShop(@PathVariable UUID id) {
        ShopResult result = getShopUseCase.getShop(id);
        return ResponseEntity.ok(mapToResponse(result));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<ShopResponse>> getNearbyShops(
            @RequestParam double longitude,
            @RequestParam double latitude,
            @RequestParam(defaultValue = "5000") double radius // Default 5km
    ) {
        List<ShopResult> results = getNearbyShopsUseCase.getNearbyShops(longitude, latitude, radius);
        List<ShopResponse> responses = results.stream().map(this::mapToResponse).collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{id}/follow")
    public ResponseEntity<Void> followShop(
            @RequestHeader("X-User-Id") String customerId,
            @PathVariable UUID id
    ) {
        followShopUseCase.followShop(id, UUID.fromString(customerId));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/unfollow")
    public ResponseEntity<Void> unfollowShop(
            @RequestHeader("X-User-Id") String customerId,
            @PathVariable UUID id
    ) {
        unfollowShopUseCase.unfollowShop(id, UUID.fromString(customerId));
        return ResponseEntity.ok().build();
    }

    private ShopResponse mapToResponse(ShopResult result) {
        return ShopResponse.builder()
                .id(result.shop().getId())
                .ownerId(result.shop().getOwnerId())
                .name(result.shop().getName())
                .description(result.shop().getDescription())
                .address(result.shop().getAddress())
                .latitude(result.shop().getCoordinates().latitude())
                .longitude(result.shop().getCoordinates().longitude())
                .phone(result.shop().getPhone())
                .email(result.shop().getEmail())
                .logoUrl(result.shop().getLogoUrl())
                .bannerUrl(result.shop().getBannerUrl())
                .isVerified(result.shop().getIsVerified())
                .businessType(result.shop().getBusinessType())
                .shopType(result.shop().getShopType())
                .followerCount(result.followerCount())
                .build();
    }
}
