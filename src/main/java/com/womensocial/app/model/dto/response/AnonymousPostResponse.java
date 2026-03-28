package com.womensocial.app.model.dto.response;

import com.womensocial.app.model.entity.AnonymousPost;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

// IMPORTANT: No user fields — anonymity must be preserved
@Data
@Builder
public class AnonymousPostResponse {

    private Long id;
    private String content;
    private List<String> topicTags;
    private int likeCount;
    private int commentCount;
    private boolean likedByMe;
    private PollResponse poll;
    private LocalDateTime createdAt;

    public static AnonymousPostResponse from(AnonymousPost post) {
        return AnonymousPostResponse.builder()
                .id(post.getId())
                .content(post.getContent())
                .topicTags(post.getTopicTags())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .createdAt(post.getCreatedAt())
                .build();
    }
}
