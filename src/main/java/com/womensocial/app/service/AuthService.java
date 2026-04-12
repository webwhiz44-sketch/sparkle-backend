package com.womensocial.app.service;

import com.womensocial.app.exception.BadRequestException;
import com.womensocial.app.exception.ResourceNotFoundException;
import com.womensocial.app.model.dto.request.ChangePasswordRequest;
import com.womensocial.app.model.dto.request.ForgotPasswordRequest;
import com.womensocial.app.model.dto.request.LoginRequest;
import com.womensocial.app.model.dto.request.ResetPasswordRequest;
import com.womensocial.app.model.dto.request.SignupRequest;
import com.womensocial.app.model.dto.response.AuthResponse;
import com.womensocial.app.model.dto.response.UserResponse;
import com.womensocial.app.model.entity.PasswordResetToken;
import com.womensocial.app.model.entity.RefreshToken;
import com.womensocial.app.model.entity.User;
import com.womensocial.app.repository.PasswordResetTokenRepository;
import com.womensocial.app.repository.RefreshTokenRepository;
import com.womensocial.app.repository.UserRepository;
import com.womensocial.app.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final FaceVerificationService faceVerificationService;
    private final EmailService emailService;

    @Value("${jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    @Value("${app.password-reset.expiry-minutes:30}")
    private int passwordResetExpiryMinutes;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        // Validate and consume the face verification token before creating the account
        faceVerificationService.consumeToken(request.getFaceVerificationToken());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName())
                .interests(request.getInterests())
                .isActive(true)
                .faceVerified(true)
                .build();

        user = userRepository.save(user);
        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!user.getIsActive()) {
            throw new BadRequestException("Account is deactivated");
        }

        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse refreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if (refreshToken.getRevoked()) {
            throw new BadRequestException("Refresh token has been revoked");
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Refresh token has expired");
        }

        // Rotate refresh token
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        return generateAuthResponse(refreshToken.getUser());
    }

    @Transactional
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        refreshTokenRepository.revokeAllByUser(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("New password must be different from the current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        refreshTokenRepository.revokeAllByUser(user);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // Always return success to prevent email enumeration
        userRepository.findByEmail(request.getEmail().toLowerCase().trim()).ifPresent(user -> {
            // Invalidate any existing unused tokens
            passwordResetTokenRepository.invalidateAllForUser(user);

            String rawToken = UUID.randomUUID().toString().replace("-", "");
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .token(rawToken)
                    .expiresAt(LocalDateTime.now().plusMinutes(passwordResetExpiryMinutes))
                    .build();
            passwordResetTokenRepository.save(resetToken);

            emailService.sendPasswordResetEmail(user.getEmail(), user.getDisplayName(), rawToken);
            log.info("Password reset token issued for user {}", user.getId());
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if (resetToken.getUsed()) {
            throw new BadRequestException("Reset token has already been used");
        }

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Revoke all refresh tokens so existing sessions are invalidated
        refreshTokenRepository.revokeAllByUser(user);
        log.info("Password reset completed for user {}", user.getId());
    }

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getEmail());
        RefreshToken refreshToken = createRefreshToken(user);
        return AuthResponse.of(accessToken, refreshToken.getToken(), UserResponse.from(user));
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiry / 1000))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(token);
    }
}
