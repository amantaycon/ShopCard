package com.shopcard.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileRequest {
    private String username;
    private String firstName;
    private String lastName;
    private String bio;
    private String avatarUrl;
}
