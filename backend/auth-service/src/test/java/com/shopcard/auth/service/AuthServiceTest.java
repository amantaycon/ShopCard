package com.shopcard.auth.service;

import com.shopcard.auth.domain.model.PasswordResetToken;
import com.shopcard.auth.domain.model.Role;
import com.shopcard.auth.domain.model.User;
import com.shopcard.auth.domain.model.VerificationToken;
import com.shopcard.auth.domain.ports.in.*;
import com.shopcard.auth.domain.ports.out.*;
import com.shopcard.auth.domain.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private RoleRepositoryPort roleRepositoryPort;

    @Mock
    private VerificationTokenRepositoryPort verificationTokenRepositoryPort;

    @Mock
    private PasswordResetTokenRepositoryPort passwordResetTokenRepositoryPort;

    @Mock
    private LockoutCachePort lockoutCachePort;

    @Mock
    private BlocklistCachePort blocklistCachePort;

    @Mock
    private EventPublisherPort eventPublisherPort;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @Mock
    private TokenServicePort tokenServicePort;

    @Mock
    private EmailVerificationCachePort emailVerificationCachePort;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldCreateUnverifiedUserAndSaveTokenAndPublishEvent() {
        RegisterCommand cmd = new RegisterCommand(
                "test@shopcard.com",
                "password123",
                "John",
                "Doe",
                "1234567890",
                Set.of("ROLE_CUSTOMER")
        );

        when(userRepositoryPort.findByEmail(cmd.email())).thenReturn(Optional.empty());
        when(passwordEncoderPort.encode(cmd.password())).thenReturn("encoded_pass");
        when(roleRepositoryPort.findByName("ROLE_CUSTOMER")).thenReturn(Optional.of(Role.builder().name("ROLE_CUSTOMER").build()));
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        AuthResult resp = authService.register(cmd);

        assertThat(resp.email()).isEqualTo("test@shopcard.com");
        assertThat(resp.emailVerified()).isFalse();
        verify(verificationTokenRepositoryPort).save(any(VerificationToken.class));
        verify(eventPublisherPort).publishUserRegisteredEvent(any(User.class), anyString());
    }

    @Test
    void verifyEmail_validToken_shouldVerifyUser() {
        String tokenStr = "valid-token";
        User user = User.builder().email("test@shopcard.com").emailVerified(false).build();
        VerificationToken token = VerificationToken.builder()
                .token(tokenStr)
                .user(user)
                .expiryDate(ZonedDateTime.now().plusHours(1))
                .build();

        when(verificationTokenRepositoryPort.findByToken(tokenStr)).thenReturn(Optional.of(token));

        authService.verifyEmail(tokenStr);

        assertThat(user.getEmailVerified()).isTrue();
        verify(userRepositoryPort).save(user);
        verify(verificationTokenRepositoryPort).delete(token);
    }

    @Test
    void verifyEmail_expiredToken_shouldThrowException() {
        String tokenStr = "expired-token";
        User user = User.builder().email("test@shopcard.com").emailVerified(false).build();
        VerificationToken token = VerificationToken.builder()
                .token(tokenStr)
                .user(user)
                .expiryDate(ZonedDateTime.now().minusHours(1))
                .build();

        when(verificationTokenRepositoryPort.findByToken(tokenStr)).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.verifyEmail(tokenStr))
                .hasMessageContaining("expired");

        verify(verificationTokenRepositoryPort).delete(token);
        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    void login_unverifiedEmail_shouldThrowException() {
        LoginCommand cmd = new LoginCommand("unverified@shopcard.com", "pass123");

        User user = User.builder()
                .email(cmd.email())
                .passwordHash("encoded_pass")
                .emailVerified(false)
                .isActive(true)
                .build();

        when(lockoutCachePort.isLocked(cmd.email())).thenReturn(false);
        when(userRepositoryPort.findByEmail(cmd.email())).thenReturn(Optional.of(user));
        when(passwordEncoderPort.matches(cmd.password(), user.getPasswordHash())).thenReturn(true);

        assertThatThrownBy(() -> authService.login(cmd))
                .hasMessageContaining("not verified");
    }

    @Test
    void login_lockedAccount_shouldThrowException() {
        LoginCommand cmd = new LoginCommand("locked@shopcard.com", "pass123");

        when(lockoutCachePort.isLocked(cmd.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.login(cmd))
                .hasMessageContaining("locked");

        verify(userRepositoryPort, never()).findByEmail(any());
    }

    @Test
    void forgotPassword_validEmail_shouldPublishResetEvent() {
        String email = "reset@shopcard.com";
        User user = User.builder().email(email).firstName("John").build();

        when(userRepositoryPort.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordResetTokenRepositoryPort.findByUser(user)).thenReturn(Optional.empty());

        authService.forgotPassword(email);

        verify(passwordResetTokenRepositoryPort).save(any(PasswordResetToken.class));
        verify(eventPublisherPort).publishPasswordResetEvent(any(User.class), anyString());
    }

    @Test
    void resetPassword_validToken_shouldChangePassword() {
        String tokenStr = "reset-token";
        User user = User.builder().email("reset@shopcard.com").passwordHash("old_pass").build();
        PasswordResetToken token = PasswordResetToken.builder()
                .token(tokenStr)
                .user(user)
                .expiryDate(ZonedDateTime.now().plusMinutes(10))
                .build();

        when(passwordResetTokenRepositoryPort.findByToken(tokenStr)).thenReturn(Optional.of(token));
        when(passwordEncoderPort.encode("new_pass")).thenReturn("encoded_new_pass");

        authService.resetPassword(new ResetPasswordCommand(tokenStr, "new_pass"));

        assertThat(user.getPasswordHash()).isEqualTo("encoded_new_pass");
        verify(userRepositoryPort).save(user);
        verify(passwordResetTokenRepositoryPort).delete(token);
    }
}
