package com.womensocial.app.model.dto.response;

import com.womensocial.app.model.entity.Follow;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FollowResponse {

    private Long id;
    private Long followerId;
    private String followerDisplayName;
    private String followerProfileImageUrl;
    private Long followingId;
    private String followingDisplayName;
    private String followingProfileImageUrl;
    private String status;
    private LocalDateTime createdAt;

    public static FollowResponse from(Follow follow) {
        return FollowResponse.builder()
                .id(follow.getId())
                .followerId(follow.getFollower().getId())
                .followerDisplayName(follow.getFollower().getDisplayName())
                .followerProfileImageUrl(follow.getFollower().getProfileImageUrl())
                .followingId(follow.getFollowing().getId())
                .followingDisplayName(follow.getFollowing().getDisplayName())
                .followingProfileImageUrl(follow.getFollowing().getProfileImageUrl())
                .status(follow.getStatus().name())
                .createdAt(follow.getCreatedAt())
                .build();
    }
}
