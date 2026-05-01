package com.womensocial.app.service;

import com.womensocial.app.exception.ResourceNotFoundException;
import com.womensocial.app.model.dto.request.UpdateProfileRequest;
import com.womensocial.app.model.dto.response.UserResponse;
import com.womensocial.app.model.entity.User;
import com.womensocial.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        User user = findUserById(userId);
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = findUserById(userId);

        if (request.getDisplayName() != null) {
            user.setDisplayName(request.getDisplayName());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getInterests() != null) {
            user.setInterests(request.getInterests());
        }
        if (request.getProfileImageUrl() != null) {
            user.setProfileImageUrl(request.getProfileImageUrl());
        }

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateAvatar(Long userId, String imageUrl) {
        User user = findUserById(userId);
        user.setProfileImageUrl(imageUrl);
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getPublicProfile(Long userId) {
        User user = findUserById(userId);
        return UserResponse.from(user);
    }

    @Transactional
    public void deleteAccount(Long userId) {
        User user = findUserById(userId);
        userRepository.delete(user);
    }

    public User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
}
