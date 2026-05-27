package com.womensocial.app.service;

import com.womensocial.app.exception.BadRequestException;
import com.womensocial.app.exception.ResourceNotFoundException;
import com.womensocial.app.model.dto.request.LoginRequest;
import com.womensocial.app.model.dto.request.SignupRequest;
import com.womensocial.app.model.dto.response.AuthResponse;
import com.womensocial.app.model.dto.response.UserResponse;
import com.womensocial.app.model.entity.OtpToken;
import com.womensocial.app.model.entity.RefreshToken;
import com.womensocial.app.model.entity.User;
import com.womensocial.app.repository.OtpTokenRepository;
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

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int OTP_MAX_ATTEMPTS = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 30;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final FaceVerificationService faceVerificationService;
    private final EmailService emailService;

    @Value("${jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public void requestOtp(String email) {
        String normalizedEmail = email.toLowerCase().trim();

        // Enforce resend cooldown — check if a valid OTP was issued within the last 30 seconds
        otpTokenRepository.findLatestValid(normalizedEmail, LocalDateTime.now()).ifPresent(existing -> {
            long secondsSinceCreation = java.time.Duration.between(existing.getCreatedAt(), LocalDateTime.now()).getSeconds();
            if (secondsSinceCreation < RESEND_COOLDOWN_SECONDS) {
                throw new BadRequestException("Please wait " + (RESEND_COOLDOWN_SECONDS - secondsSinceCreation) + " seconds before requesting a new code.");
            }
        });

        // Invalidate any previous OTPs for this email
        otpTokenRepository.invalidateAllForEmail(normalizedEmail);

        // Generate 6-digit code
        String rawCode = String.format("%06d", secureRandom.nextInt(1_000_000));
        String hashedCode = passwordEncoder.encode(rawCode);

        OtpToken otp = OtpToken.builder()
                .email(normalizedEmail)
                .code(hashedCode)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .build();
        otpTokenRepository.save(otp);

        emailService.sendOtpEmail(normalizedEmail, rawCode);
        log.info("[Auth] OTP issued for email={}", normalizedEmail);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (user.getPassword() == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!user.getIsActive()) {
            throw new BadRequestException("Account is deactivated");
        }

        log.info("[Auth] Login successful for userId={}", user.getId());
        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        log.info("[Auth] Signup attempt for email={}", email);

        faceVerificationService.consumeToken(request.getFaceVerificationToken());

        if (userRepository.existsByEmail(email)) {
            log.warn("[Auth] Signup rejected — email already registered: {}", email);
            throw new BadRequestException("Email is already registered. Please sign in instead.");
        }

        validateAndConsumeOtp(email, request.getOtpCode());

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName())
                .interests(request.getInterests())
                .isActive(true)
                .faceVerified(true)
                .build();

        user = userRepository.save(user);
        log.info("[Auth] Signup successful — userId={}, email={}", user.getId(), user.getEmail());
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

    private void validateAndConsumeOtp(String email, String rawCode) {
        OtpToken otp = otpTokenRepository.findLatestValid(email, LocalDateTime.now())
                .orElseThrow(() -> new BadRequestException("Code expired or not found. Please request a new one."));

        if (otp.getAttempts() >= OTP_MAX_ATTEMPTS) {
            throw new BadRequestException("Too many incorrect attempts. Please request a new code.");
        }

        if (!passwordEncoder.matches(rawCode, otp.getCode())) {
            otp.setAttempts(otp.getAttempts() + 1);
            otpTokenRepository.save(otp);
            int remaining = OTP_MAX_ATTEMPTS - otp.getAttempts();
            throw new BadCredentialsException(remaining > 0
                    ? "Incorrect code. " + remaining + " attempt" + (remaining == 1 ? "" : "s") + " remaining."
                    : "Too many incorrect attempts. Please request a new code.");
        }

        otp.setUsed(true);
        otpTokenRepository.save(otp);
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
