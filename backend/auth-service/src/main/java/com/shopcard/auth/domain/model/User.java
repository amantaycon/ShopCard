package com.shopcard.auth.domain.model;

import lombok.*;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class User {
    private UUID id;
    private String email;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    @Builder.Default
    private String provider = "LOCAL";
    private String providerId;
    @Builder.Default
    private Boolean isActive = true;
    @Builder.Default
    private Boolean emailVerified = false;
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
    private String username;
    private java.time.LocalDate dateOfBirth;
    private Boolean agreedTerms;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
