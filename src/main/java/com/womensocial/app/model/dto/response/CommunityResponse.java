package com.womensocial.app.model.dto.response;

import com.womensocial.app.model.entity.Community;
import com.womensocial.app.model.enums.TopicCategory;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommunityResponse {

    private Long id;
    private String name;
    private String description;
    private TopicCategory category;
    private String coverImageUrl;
    private int memberCount;
    private int postCount;
    private boolean isMember;
    private LocalDateTime createdAt;

    public static CommunityResponse from(Community community) {
        return CommunityResponse.builder()
                .id(community.getId())
                .name(community.getName())
                .description(community.getDescription())
                .category(community.getCategory())
                .coverImageUrl(community.getCoverImageUrl())
                .memberCount(community.getMemberCount())
                .postCount(community.getPostCount())
                .createdAt(community.getCreatedAt())
                .build();
    }
}
