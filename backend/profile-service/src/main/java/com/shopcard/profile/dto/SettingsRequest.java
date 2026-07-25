package com.shopcard.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingsRequest {
    private String theme;
    private Boolean emailNotifications;
    private String shopSettingsBanner;
    private String customSettingsJson;
}
