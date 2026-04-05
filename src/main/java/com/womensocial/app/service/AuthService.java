package com.womensocial.app.service;

import com.womensocial.app.exception.BadRequestException;
import com.womensocial.app.exception.ResourceNotFoundException;
import com.womensocial.app.model.dto.request.ChangePasswordRequest;
import com.womensocial.app.model.dto.request.LoginRequest;
import com.womensocial.app.model.dto.request.SignupRequest;
import com.womensocial.app.model.dto.response.AuthResponse;
import com.womensocial.app.model.dto.response.UserResponse;
import com.womensocial.app.model.entity.RefreshToken;
import com.womensocial.app.model.entity.User;
import com.womensocial.app.repository.RefreshTokenRepository;
import com.womensocial.app.repository.UserRepository;
import com.womensocial.app.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName())
                .interests(request.getInterests())
                .isActive(true)
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
