package com.shopcard.profile.controller;

import com.shopcard.profile.domain.model.UserProfile;
import com.shopcard.profile.dto.ProfileRequest;
import com.shopcard.profile.dto.ProfileResponse;
import com.shopcard.profile.dto.SettingsRequest;
import com.shopcard.profile.infrastructure.persistence.repository.UserProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/profiles")
public class ProfileController {

    private final UserProfileRepository userProfileRepository;

    public ProfileController(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @GetMapping("/my")
    public ResponseEntity<ProfileResponse> getMyProfile(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserProfile profile = userProfileRepository.findById(userId)
                .orElseGet(() -> {
                    UserProfile newProfile = UserProfile.builder()
                            .userId(userId)
                            .username("user_" + (userId.length() > 8 ? userId.substring(0, 8) : userId))
                            .theme("light")
                            .emailNotifications(true)
                            .build();
                    return userProfileRepository.save(newProfile);
                });

        return ResponseEntity.ok(mapToResponse(profile));
    }

    @PutMapping("/my")
    public ResponseEntity<?> updateMyProfile(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody ProfileRequest request) {

        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserProfile profile = userProfileRepository.findById(userId)
                .orElseGet(() -> UserProfile.builder().userId(userId).theme("light").build());

        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            Optional<UserProfile> existing = userProfileRepository.findByUsername(request.getUsername());
            if (existing.isPresent() && !existing.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Username is already taken by another user.");
            }
            profile.setUsername(request.getUsername());
        }

        if (request.getFirstName() != null) profile.setFirstName(request.getFirstName());
        if (request.getLastName() != null) profile.setLastName(request.getLastName());
        if (request.getBio() != null) profile.setBio(request.getBio());
        if (request.getAvatarUrl() != null) profile.setAvatarUrl(request.getAvatarUrl());

        UserProfile saved = userProfileRepository.save(profile);
        return ResponseEntity.ok(mapToResponse(saved));
    }

    @PutMapping("/my/settings")
    public ResponseEntity<ProfileResponse> updateMySettings(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody SettingsRequest request) {

        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserProfile profile = userProfileRepository.findById(userId)
                .orElseGet(() -> UserProfile.builder()
                        .userId(userId)
                        .username("user_" + (userId.length() > 8 ? userId.substring(0, 8) : userId))
                        .theme("light")
                        .build());

        if (request.getTheme() != null) {
            profile.setTheme(request.getTheme());
        }
        if (request.getEmailNotifications() != null) {
            profile.setEmailNotifications(request.getEmailNotifications());
        }
        if (request.getShopSettingsBanner() != null) {
            profile.setShopSettingsBanner(request.getShopSettingsBanner());
        }
        if (request.getCustomSettingsJson() != null) {
            profile.setCustomSettingsJson(request.getCustomSettingsJson());
        }

        UserProfile saved = userProfileRepository.save(profile);
        return ResponseEntity.ok(mapToResponse(saved));
    }

    @GetMapping("/u/{username}")
    public ResponseEntity<ProfileResponse> getProfileByUsername(@PathVariable("username") String username) {
        return userProfileRepository.findByUsername(username)
                .map(profile -> ResponseEntity.ok(mapToResponse(profile)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ProfileResponse> getProfileByUserId(@PathVariable("userId") String userId) {
        return userProfileRepository.findById(userId)
                .map(profile -> ResponseEntity.ok(mapToResponse(profile)))
                .orElse(ResponseEntity.notFound().build());
    }

    private ProfileResponse mapToResponse(UserProfile profile) {
        return ProfileResponse.builder()
                .userId(profile.getUserId())
                .username(profile.getUsername())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .bio(profile.getBio())
                .avatarUrl(profile.getAvatarUrl())
                .theme(profile.getTheme())
                .emailNotifications(profile.isEmailNotifications())
                .shopSettingsBanner(profile.getShopSettingsBanner())
                .customSettingsJson(profile.getCustomSettingsJson())
                .build();
    }
}
