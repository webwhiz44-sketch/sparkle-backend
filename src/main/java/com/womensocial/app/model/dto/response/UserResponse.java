package com.womensocial.app.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.womensocial.app.model.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private Long id;
    private String email;
    private String displayName;
    private String bio;
    private String profileImageUrl;
    private List<String> interests;
    private LocalDateTime createdAt;
    private Long followerCount;
    private Long followingCount;
    private String followStatus; // "NONE", "PENDING", "ACCEPTED" — null for own profile view

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .bio(user.getBio())
                .profileImageUrl(user.getProfileImageUrl())
                .interests(user.getInterests())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
