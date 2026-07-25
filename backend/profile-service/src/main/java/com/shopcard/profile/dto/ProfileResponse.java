package com.shopcard.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private String userId;
    private String username;
    private String firstName;
    private String lastName;
    private String bio;
    private String avatarUrl;
    private String theme;
    private boolean emailNotifications;
    private String shopSettingsBanner;
    private String customSettingsJson;
}
