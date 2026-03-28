package com.womensocial.app.model.dto.response;

import com.womensocial.app.model.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String displayName;
    private String bio;
    private String profileImageUrl;
    private List<String> interests;
    private LocalDateTime createdAt;

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
