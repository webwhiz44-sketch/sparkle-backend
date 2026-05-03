package com.womensocial.app.service;

import com.womensocial.app.exception.BadRequestException;
import com.womensocial.app.exception.ResourceNotFoundException;
import com.womensocial.app.exception.UnauthorizedException;
import com.womensocial.app.model.dto.request.FollowRequest;
import com.womensocial.app.model.dto.response.FollowResponse;
import com.womensocial.app.model.dto.response.UserResponse;
import com.womensocial.app.model.entity.Follow;
import com.womensocial.app.model.entity.User;
import com.womensocial.app.model.enums.FollowStatus;
import com.womensocial.app.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserService userService;

    @Transactional
    public FollowResponse sendFollowRequest(Long followerId, FollowRequest request) {
        if (followerId.equals(request.getTargetUserId())) {
            throw new BadRequestException("You cannot follow yourself");
        }
        if (followRepository.existsByFollowerIdAndFollowingId(followerId, request.getTargetUserId())) {
            throw new BadRequestException("Follow request already exists");
        }
        User follower = userService.findUserById(followerId);
        User following = userService.findUserById(request.getTargetUserId());

        Follow follow = Follow.builder()
                .follower(follower)
                .following(following)
                .status(FollowStatus.PENDING)
                .build();

        return FollowResponse.from(followRepository.save(follow));
    }

    @Transactional
    public void acceptFollowRequest(Long followId, Long currentUserId) {
        Follow follow = findFollowById(followId);
        if (!follow.getFollowing().getId().equals(currentUserId)) {
            throw new UnauthorizedException("Only the target user can accept this request");
        }
        if (follow.getStatus() != FollowStatus.PENDING) {
            throw new BadRequestException("Follow request is not pending");
        }
        follow.setStatus(FollowStatus.ACCEPTED);
        followRepository.save(follow);
    }

    @Transactional
    public void rejectFollowRequest(Long followId, Long currentUserId) {
        Follow follow = findFollowById(followId);
        if (!follow.getFollowing().getId().equals(currentUserId)) {
            throw new UnauthorizedException("Only the target user can reject this request");
        }
        follow.setStatus(FollowStatus.REJECTED);
        followRepository.save(follow);
    }

    @Transactional
    public void unfollow(Long followId, Long currentUserId) {
        Follow follow = findFollowById(followId);
        if (!follow.getFollower().getId().equals(currentUserId)) {
            throw new UnauthorizedException("Only the follower can unfollow");
        }
        followRepository.delete(follow);
    }

    @Transactional(readOnly = true)
    public List<FollowResponse> getPendingRequests(Long userId) {
        return followRepository.findByFollowingIdAndStatus(userId, FollowStatus.PENDING)
                .stream()
                .map(FollowResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getFollowers(Long userId) {
        return followRepository.findByFollowingIdAndStatus(userId, FollowStatus.ACCEPTED)
                .stream()
                .map(f -> UserResponse.from(f.getFollower()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getFollowing(Long userId) {
        return followRepository.findByFollowerIdAndStatus(userId, FollowStatus.ACCEPTED)
                .stream()
                .map(f -> UserResponse.from(f.getFollowing()))
                .toList();
    }

    @Transactional(readOnly = true)
    public String getFollowStatus(Long currentUserId, Long targetUserId) {
        return followRepository.findByFollowerIdAndFollowingId(currentUserId, targetUserId)
                .map(f -> f.getStatus().name())
                .orElse("NONE");
    }

    private Follow findFollowById(Long followId) {
        return followRepository.findById(followId)
                .orElseThrow(() -> new ResourceNotFoundException("Follow not found with id: " + followId));
    }
}
