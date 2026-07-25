package com.shopcard.auth.controller;

import com.shopcard.auth.dto.*;
import com.shopcard.auth.domain.ports.in.*;
import com.shopcard.auth.domain.ports.out.TokenServicePort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;
    private final LoginGoogleUseCase loginGoogleUseCase;
    private final RefreshSessionUseCase refreshSessionUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final LogoutUseCase logoutUseCase;
    private final SendVerificationCodeUseCase sendVerificationCodeUseCase;
    private final VerifyCodeUseCase verifyCodeUseCase;
    private final InitiateRegistrationUseCase initiateRegistrationUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;
    private final UpdateRoleUseCase updateRoleUseCase;
    private final TokenServicePort tokenServicePort;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterCommand command = new RegisterCommand(
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                request.getPhoneNumber(),
                request.getRoles()
        );
        AuthResult result = registerUserUseCase.register(command);
        return ResponseEntity.ok(mapToResponse(result));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginCommand command = new LoginCommand(request.getEmail(), request.getPassword());
        AuthResult result = loginUseCase.login(command);
        return ResponseEntity.ok(mapToResponse(result));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@Valid @RequestBody GoogleOAuthRequest request) {
        GoogleLoginCommand command = new GoogleLoginCommand(request.getIdToken());
        AuthResult result = loginGoogleUseCase.loginGoogle(command);
        return ResponseEntity.ok(mapToResponse(result));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestParam("refreshToken") String refreshToken) {
        AuthResult result = refreshSessionUseCase.refresh(refreshToken);
        return ResponseEntity.ok(mapToResponse(result));
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        verifyEmailUseCase.verifyEmail(token);
        return ResponseEntity.ok("Email verified successfully! You can now log in.");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam("email") String email) {
        forgotPasswordUseCase.forgotPassword(email);
        return ResponseEntity.ok("If the email exists, a password reset link has been dispatched to your inbox.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        ResetPasswordCommand command = new ResetPasswordCommand(request.getToken(), request.getNewPassword());
        resetPasswordUseCase.resetPassword(command);
        return ResponseEntity.ok("Password has been reset successfully.");
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        logoutUseCase.logout(authHeader);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register/send-code")
    public ResponseEntity<Void> sendVerificationCode(@Valid @RequestBody EmailVerificationRequest request) {
        sendVerificationCodeUseCase.sendVerificationCode(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register/verify-code")
    public ResponseEntity<Void> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        verifyCodeUseCase.verifyCode(request.getEmail(), request.getCode());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register/initiate")
    public ResponseEntity<AuthResponse> initiateRegistration(@Valid @RequestBody InitiateRegistrationRequest request) {
        AuthResult result = initiateRegistrationUseCase.initiateRegistration(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(mapToResponse(result));
    }

    @PutMapping("/profile")
    public ResponseEntity<Void> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UpdateProfileRequest request) {
        String token = authHeader.substring(7);
        String userIdStr = tokenServicePort.extractUserId(token);
        updateProfileUseCase.updateProfile(
                UUID.fromString(userIdStr),
                request.getFirstName(),
                request.getLastName(),
                request.getDateOfBirth(),
                request.getUsername(),
                request.getAgreedTerms()
        );
        return ResponseEntity.ok().build();
    }

    @PutMapping("/role")
    public ResponseEntity<Void> updateRole(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UpdateRoleRequest request) {
        String token = authHeader.substring(7);
        String userIdStr = tokenServicePort.extractUserId(token);
        updateRoleUseCase.updateRole(UUID.fromString(userIdStr), request.getRole());
        return ResponseEntity.ok().build();
    }

    private AuthResponse mapToResponse(AuthResult result) {
        return AuthResponse.builder()
                .userId(result.userId())
                .email(result.email())
                .firstName(result.firstName())
                .lastName(result.lastName())
                .emailVerified(result.emailVerified())
                .roles(result.roles())
                .accessToken(result.accessToken())
                .refreshToken(result.refreshToken())
                .build();
    }
}
