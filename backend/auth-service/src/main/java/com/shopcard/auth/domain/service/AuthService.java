package com.shopcard.auth.domain.service;

import com.shopcard.auth.domain.model.PasswordResetToken;
import com.shopcard.auth.domain.model.Role;
import com.shopcard.auth.domain.model.User;
import com.shopcard.auth.domain.model.VerificationToken;
import com.shopcard.auth.domain.ports.in.*;
import com.shopcard.auth.domain.ports.out.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService implements
        RegisterUserUseCase,
        VerifyEmailUseCase,
        LoginUseCase,
        ForgotPasswordUseCase,
        ResetPasswordUseCase,
        LogoutUseCase,
        RefreshSessionUseCase,
        LoginGoogleUseCase,
        SendVerificationCodeUseCase,
        VerifyCodeUseCase,
        InitiateRegistrationUseCase,
        UpdateProfileUseCase,
        UpdateRoleUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final RoleRepositoryPort roleRepositoryPort;
    private final VerificationTokenRepositoryPort verificationTokenRepositoryPort;
    private final PasswordResetTokenRepositoryPort passwordResetTokenRepositoryPort;
    private final LockoutCachePort lockoutCachePort;
    private final BlocklistCachePort blocklistCachePort;
    private final EventPublisherPort eventPublisherPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final TokenServicePort tokenServicePort;
    private final EmailVerificationCachePort emailVerificationCachePort;

    @Override
    public AuthResult register(RegisterCommand command) {
        if (userRepositoryPort.findByEmail(command.email()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        Set<Role> roles = new HashSet<>();
        if (command.roles() != null && !command.roles().isEmpty()) {
            for (String roleName : command.roles()) {
                Role role = roleRepositoryPort.findByName(roleName)
                        .orElseGet(() -> roleRepositoryPort.save(Role.builder().name(roleName).build()));
                roles.add(role);
            }
        } else {
            Role customerRole = roleRepositoryPort.findByName("ROLE_CUSTOMER")
                    .orElseGet(() -> roleRepositoryPort.save(Role.builder().name("ROLE_CUSTOMER").build()));
            roles.add(customerRole);
        }

        User user = User.builder()
                .email(command.email())
                .passwordHash(passwordEncoderPort.encode(command.password()))
                .firstName(command.firstName())
                .lastName(command.lastName())
                .phoneNumber(command.phoneNumber())
                .roles(roles)
                .provider("LOCAL")
                .isActive(true)
                .emailVerified(false)
                .build();

        User savedUser = userRepositoryPort.save(user);

        String tokenStr = UUID.randomUUID().toString();
        VerificationToken token = VerificationToken.builder()
                .token(tokenStr)
                .user(savedUser)
                .expiryDate(ZonedDateTime.now().plusDays(1))
                .build();
        verificationTokenRepositoryPort.save(token);

        eventPublisherPort.publishUserRegisteredEvent(savedUser, tokenStr);

        return new AuthResult(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                false,
                savedUser.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
                null,
                null
        );
    }

    @Override
    public void verifyEmail(String tokenStr) {
        VerificationToken token = verificationTokenRepositoryPort.findByToken(tokenStr)
                .orElseThrow(() -> new RuntimeException("Invalid or missing verification token"));

        if (token.getExpiryDate().isBefore(ZonedDateTime.now())) {
            verificationTokenRepositoryPort.delete(token);
            throw new RuntimeException("Verification token has expired. Please register again.");
        }

        User user = token.getUser();
        user.setEmailVerified(true);
        userRepositoryPort.save(user);

        verificationTokenRepositoryPort.delete(token);
    }

    @Override
    public AuthResult login(LoginCommand command) {
        String email = command.email();

        if (lockoutCachePort.isLocked(email)) {
            throw new RuntimeException("Account is temporarily locked. Try again in 15 minutes.");
        }

        User user = userRepositoryPort.findByEmail(email)
                .orElseThrow(() -> {
                    lockoutCachePort.incrementFailedAttempts(email);
                    return new RuntimeException("Invalid email or password");
                });

        if (!passwordEncoderPort.matches(command.password(), user.getPasswordHash())) {
            lockoutCachePort.incrementFailedAttempts(email);
            throw new RuntimeException("Invalid email or password");
        }

        lockoutCachePort.clearFailedAttempts(email);

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new RuntimeException("User account is inactive");
        }

        if (Boolean.FALSE.equals(user.getEmailVerified())) {
            throw new RuntimeException("Email address is not verified. Please check your inbox.");
        }

        return buildAuthResult(user);
    }

    @Override
    public AuthResult loginGoogle(GoogleLoginCommand command) {
        String email = command.idToken();
        if (!email.contains("@")) {
            email = email + "@gmail.com";
        }

        String finalEmail = email;
        User user = userRepositoryPort.findByEmail(finalEmail)
                .orElseGet(() -> {
                    Role customerRole = roleRepositoryPort.findByName("ROLE_CUSTOMER")
                            .orElseGet(() -> roleRepositoryPort.save(Role.builder().name("ROLE_CUSTOMER").build()));

                    User newUser = User.builder()
                            .email(finalEmail)
                            .firstName("Google")
                            .lastName("User")
                            .provider("GOOGLE")
                            .providerId(UUID.randomUUID().toString())
                            .roles(Set.of(customerRole))
                            .isActive(true)
                            .emailVerified(true)
                            .build();
                    return userRepositoryPort.save(newUser);
                });

        return buildAuthResult(user);
    }

    @Override
    public void forgotPassword(String email) {
        User user = userRepositoryPort.findByEmail(email).orElse(null);
        if (user == null) {
            return;
        }

        passwordResetTokenRepositoryPort.findByUser(user).ifPresent(passwordResetTokenRepositoryPort::delete);

        String tokenStr = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(tokenStr)
                .user(user)
                .expiryDate(ZonedDateTime.now().plusMinutes(15))
                .build();
        passwordResetTokenRepositoryPort.save(resetToken);

        eventPublisherPort.publishPasswordResetEvent(user, tokenStr);
    }

    @Override
    public void resetPassword(ResetPasswordCommand command) {
        PasswordResetToken token = passwordResetTokenRepositoryPort.findByToken(command.token())
                .orElseThrow(() -> new RuntimeException("Invalid or missing password reset token"));

        if (token.getExpiryDate().isBefore(ZonedDateTime.now())) {
            passwordResetTokenRepositoryPort.delete(token);
            throw new RuntimeException("Password reset token has expired. Request a new one.");
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoderPort.encode(command.newPassword()));
        userRepositoryPort.save(user);

        passwordResetTokenRepositoryPort.delete(token);
    }

    @Override
    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        String token = authHeader.substring(7);
        if (tokenServicePort.isTokenValid(token)) {
            try {
                Date expiration = tokenServicePort.extractExpiration(token);
                long remainingMillis = expiration.getTime() - System.currentTimeMillis();
                if (remainingMillis > 0) {
                    blocklistCachePort.blocklistToken(token, remainingMillis);
                }
            } catch (Exception e) {
                // Ignore token decode failures during logout
            }
        }
    }

    @Override
    public AuthResult refresh(String refreshToken) {
        if (!tokenServicePort.isTokenValid(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String userIdStr = tokenServicePort.extractUserId(refreshToken);
        User user = userRepositoryPort.findById(UUID.fromString(userIdStr))
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (Boolean.FALSE.equals(user.getEmailVerified())) {
            throw new RuntimeException("Email address is not verified.");
        }

        String newAccessToken = tokenServicePort.generateToken(user);

        return new AuthResult(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmailVerified(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
                newAccessToken,
                refreshToken
        );
    }

    @Override
    public void sendVerificationCode(String email) {
        if (userRepositoryPort.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }
        String code = String.format("%06d", new java.util.Random().nextInt(1000000));
        emailVerificationCachePort.storeCode(email, code);
        eventPublisherPort.publishEmailVerificationEvent(email, code);
    }

    @Override
    public void verifyCode(String email, String code) {
        String cachedCode = emailVerificationCachePort.getCode(email);
        if (cachedCode == null || !cachedCode.equals(code)) {
            throw new IllegalArgumentException("Invalid or expired verification code");
        }
        emailVerificationCachePort.clearCode(email);
        emailVerificationCachePort.markEmailAsVerified(email);
    }

    @Override
    public AuthResult initiateRegistration(String email, String password) {
        if (!emailVerificationCachePort.isEmailVerified(email)) {
            throw new IllegalArgumentException("Email is not verified");
        }
        if (userRepositoryPort.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepositoryPort.findByName("ROLE_USER")
                .orElseGet(() -> roleRepositoryPort.save(Role.builder().name("ROLE_USER").build()));
        roles.add(userRole);

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoderPort.encode(password))
                .roles(roles)
                .provider("LOCAL")
                .isActive(true)
                .emailVerified(true)
                .build();

        User savedUser = userRepositoryPort.save(user);
        emailVerificationCachePort.clearEmailVerificationStatus(email);

        return buildAuthResult(savedUser);
    }

    @Override
    public void updateProfile(UUID userId, String firstName, String lastName, LocalDate dateOfBirth, String username, Boolean agreedTerms) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Optional<User> existingUser = userRepositoryPort.findByUsername(username);
        if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
            throw new IllegalArgumentException("Username is already taken");
        }

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setDateOfBirth(dateOfBirth);
        user.setUsername(username);
        user.setAgreedTerms(agreedTerms);

        userRepositoryPort.save(user);
    }

    @Override
    public void updateRole(UUID userId, String role) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String roleName = role.toUpperCase();
        if (!roleName.startsWith("ROLE_")) {
            roleName = "ROLE_" + roleName;
        }

        if (!roleName.equals("ROLE_CUSTOMER") && !roleName.equals("ROLE_SHOP_OWNER") && !roleName.equals("ROLE_DELIVERY_PARTNER")) {
            throw new IllegalArgumentException("Invalid role selected");
        }

        String finalRoleName = roleName;
        Role targetRole = roleRepositoryPort.findByName(finalRoleName)
                .orElseGet(() -> roleRepositoryPort.save(Role.builder().name(finalRoleName).build()));

        Set<Role> roles = new HashSet<>();
        roles.add(targetRole);
        user.setRoles(roles);

        userRepositoryPort.save(user);
    }

    private AuthResult buildAuthResult(User user) {
        String accessToken = tokenServicePort.generateToken(user);
        String refreshToken = tokenServicePort.generateRefreshToken(user);

        return new AuthResult(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmailVerified(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
                accessToken,
                refreshToken
        );
    }
}
