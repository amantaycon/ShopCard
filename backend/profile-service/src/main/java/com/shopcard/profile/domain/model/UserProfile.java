package com.shopcard.profile.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_profiles", indexes = {
    @Index(name = "idx_username", columnList = "username", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "bio", length = 1000)
    private String bio;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "theme")
    @Builder.Default
    private String theme = "light";

    @Column(name = "email_notifications")
    @Builder.Default
    private boolean emailNotifications = true;

    @Column(name = "shop_settings_banner")
    private String shopSettingsBanner;

    @Column(name = "custom_settings_json", columnDefinition = "text")
    private String customSettingsJson;
}
