package com.womensocial.app.model.dto.response;

import com.womensocial.app.model.entity.Story;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class StoryResponse {

    private Long id;
    private UserResponse author;
    private String title;
    private String body;
    private String coverImageUrl;
    private List<String> tags;
    private int likeCount;
    private int commentCount;
    private int readTimeMinutes;
    private boolean likedByMe;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static StoryResponse from(Story story) {
        int wordCount = story.getBody().split("\\s+").length;
        return StoryResponse.builder()
                .id(story.getId())
                .author(UserResponse.from(story.getUser()))
                .title(story.getTitle())
                .body(story.getBody())
                .coverImageUrl(story.getCoverImageUrl())
                .tags(story.getTags())
                .likeCount(story.getLikeCount())
                .commentCount(story.getCommentCount())
                .readTimeMinutes(Math.max(1, wordCount / 200))
                .createdAt(story.getCreatedAt())
                .updatedAt(story.getUpdatedAt())
                .build();
    }
}
