package com.womensocial.app.service;

import com.womensocial.app.exception.ResourceNotFoundException;
import com.womensocial.app.model.dto.request.UpdateProfileRequest;
import com.womensocial.app.model.dto.response.UserResponse;
import com.womensocial.app.model.entity.User;
import com.womensocial.app.model.enums.FollowStatus;
import com.womensocial.app.repository.FollowRepository;
import com.womensocial.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        User user = findUserById(userId);
        UserResponse response = UserResponse.from(user);
        response.setFollowerCount(followRepository.countByFollowingIdAndStatus(userId, FollowStatus.ACCEPTED));
        response.setFollowingCount(followRepository.countByFollowerIdAndStatus(userId, FollowStatus.ACCEPTED));
        return response;
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
    public UserResponse getPublicProfile(Long targetUserId, Long viewerUserId) {
        User user = findUserById(targetUserId);
        UserResponse response = UserResponse.from(user);
        response.setFollowerCount(followRepository.countByFollowingIdAndStatus(targetUserId, FollowStatus.ACCEPTED));
        response.setFollowingCount(followRepository.countByFollowerIdAndStatus(targetUserId, FollowStatus.ACCEPTED));
        if (viewerUserId != null && !viewerUserId.equals(targetUserId)) {
            String status = followRepository.findByFollowerIdAndFollowingId(viewerUserId, targetUserId)
                    .map(f -> f.getStatus().name())
                    .orElse("NONE");
            response.setFollowStatus(status);
        }
        return response;
    }

    @Transactional
    public void deleteAccount(Long userId) {
        User user = findUserById(userId);
        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public java.util.List<UserResponse> searchUsers(String query, Long currentUserId) {
        if (query == null || query.isBlank() || query.length() < 2) return java.util.List.of();
        var users = userRepository.searchByDisplayName(query.trim(),
                org.springframework.data.domain.PageRequest.of(0, 20));
        return users.stream()
                .filter(u -> !u.getId().equals(currentUserId))
                .map(u -> {
                    UserResponse r = UserResponse.from(u);
                    String status = followRepository.findByFollowerIdAndFollowingId(currentUserId, u.getId())
                            .map(f -> f.getStatus().name())
                            .orElse("NONE");
                    r.setFollowStatus(status);
                    return r;
                })
                .toList();
    }

    public User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
}
