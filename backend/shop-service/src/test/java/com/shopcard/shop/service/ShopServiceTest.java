package com.shopcard.shop.service;

import com.shopcard.shop.domain.model.Coordinates;
import com.shopcard.shop.domain.model.Shop;
import com.shopcard.shop.domain.model.ShopFollower;
import com.shopcard.shop.domain.ports.in.ShopCommand;
import com.shopcard.shop.domain.ports.in.ShopResult;
import com.shopcard.shop.domain.ports.out.ShopFollowerRepositoryPort;
import com.shopcard.shop.domain.ports.out.ShopRepositoryPort;
import com.shopcard.shop.domain.service.ShopService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShopServiceTest {

    @Mock
    private ShopRepositoryPort shopRepositoryPort;

    @Mock
    private ShopFollowerRepositoryPort shopFollowerRepositoryPort;

    @InjectMocks
    private ShopService shopService;

    @Test
    void createOrUpdateShop_newShop_shouldSaveAndReturnShopResult() {
        UUID ownerId = UUID.randomUUID();
        ShopCommand command = new ShopCommand(
                "My Shop",
                "Description",
                "123 Address",
                45.0,
                90.0,
                "555-1234",
                "shop@example.com",
                "logo.png",
                "banner.png",
                "Shop",
                "General Store"
        );

        when(shopRepositoryPort.findByOwnerId(ownerId)).thenReturn(Optional.empty());
        when(shopRepositoryPort.save(any(Shop.class))).thenAnswer(invocation -> {
            Shop shop = invocation.getArgument(0);
            shop.setId(UUID.randomUUID());
            return shop;
        });
        when(shopFollowerRepositoryPort.countByShopId(any(UUID.class))).thenReturn(0L);

        ShopResult result = shopService.createOrUpdateShop(ownerId, command);

        assertThat(result.shop().getName()).isEqualTo("My Shop");
        assertThat(result.shop().getCoordinates().longitude()).isEqualTo(90.0);
        assertThat(result.shop().getCoordinates().latitude()).isEqualTo(45.0);
        assertThat(result.followerCount()).isZero();

        verify(shopRepositoryPort).save(any(Shop.class));
    }

    @Test
    void getShop_existingShop_shouldReturnShopResult() {
        UUID shopId = UUID.randomUUID();
        Shop shop = Shop.builder()
                .id(shopId)
                .name("Super Shop")
                .build();

        when(shopRepositoryPort.findById(shopId)).thenReturn(Optional.of(shop));
        when(shopFollowerRepositoryPort.countByShopId(shopId)).thenReturn(15L);

        ShopResult result = shopService.getShop(shopId);

        assertThat(result.shop().getName()).isEqualTo("Super Shop");
        assertThat(result.followerCount()).isEqualTo(15L);
    }

    @Test
    void getShop_missingShop_shouldThrowException() {
        UUID shopId = UUID.randomUUID();
        when(shopRepositoryPort.findById(shopId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shopService.getShop(shopId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void getNearbyShops_shouldReturnList() {
        Shop shop = Shop.builder()
                .id(UUID.randomUUID())
                .name("Nearby Shop")
                .build();

        when(shopRepositoryPort.findNearbyShops(90.0, 45.0, 5000.0)).thenReturn(List.of(shop));
        when(shopFollowerRepositoryPort.countByShopId(any(UUID.class))).thenReturn(2L);

        List<ShopResult> results = shopService.getNearbyShops(90.0, 45.0, 5000.0);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).shop().getName()).isEqualTo("Nearby Shop");
        assertThat(results.get(0).followerCount()).isEqualTo(2L);
    }

    @Test
    void followShop_newFollower_shouldSaveFollower() {
        UUID shopId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Shop shop = Shop.builder().id(shopId).build();

        when(shopRepositoryPort.findById(shopId)).thenReturn(Optional.of(shop));
        when(shopFollowerRepositoryPort.findByShopIdAndCustomerId(shopId, customerId)).thenReturn(Optional.empty());

        shopService.followShop(shopId, customerId);

        verify(shopFollowerRepositoryPort).save(any(ShopFollower.class));
    }

    @Test
    void followShop_alreadyFollowing_shouldDoNothing() {
        UUID shopId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Shop shop = Shop.builder().id(shopId).build();

        when(shopRepositoryPort.findById(shopId)).thenReturn(Optional.of(shop));
        when(shopFollowerRepositoryPort.findByShopIdAndCustomerId(shopId, customerId))
                .thenReturn(Optional.of(ShopFollower.builder().build()));

        shopService.followShop(shopId, customerId);

        verify(shopFollowerRepositoryPort, never()).save(any());
    }

    @Test
    void unfollowShop_existingFollower_shouldDeleteFollower() {
        UUID shopId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        ShopFollower follower = ShopFollower.builder().id(1L).build();

        when(shopFollowerRepositoryPort.findByShopIdAndCustomerId(shopId, customerId)).thenReturn(Optional.of(follower));

        shopService.unfollowShop(shopId, customerId);

        verify(shopFollowerRepositoryPort).delete(follower);
    }

    @Test
    void unfollowShop_notFollowing_shouldThrowException() {
        UUID shopId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        when(shopFollowerRepositoryPort.findByShopIdAndCustomerId(shopId, customerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shopService.unfollowShop(shopId, customerId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Not following");
    }
}
