package com.womensocial.app.model.dto.response;

import com.womensocial.app.model.entity.Notification;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {

    private Long id;
    private String type;
    private String message;
    private Long actorId;
    private String actorName;
    private String actorImageUrl;
    private Long postId;
    private Long communityId;
    private boolean read;
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType().name())
                .message(n.getMessage())
                .actorId(n.getActor() != null ? n.getActor().getId() : null)
                .actorName(n.getActor() != null ? n.getActor().getDisplayName() : null)
                .actorImageUrl(n.getActor() != null ? n.getActor().getProfileImageUrl() : null)
                .postId(n.getPostId())
                .communityId(n.getCommunityId())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
