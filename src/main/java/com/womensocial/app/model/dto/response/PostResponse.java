package com.womensocial.app.model.dto.response;

import com.womensocial.app.model.entity.Post;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PostResponse {

    private Long id;
    private UserResponse author;
    private boolean anonymous;
    private Long communityId;
    private String communityName;
    private String content;
    private String imageUrl;
    private List<String> topicTags;
    private int likeCount;
    private int commentCount;
    private boolean likedByMe;
    private boolean savedByMe;
    private PollResponse poll;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PostResponse from(Post post) {
        boolean anon = Boolean.TRUE.equals(post.getIsAnonymous());
        UserResponse authorResponse = anon
                ? UserResponse.builder().id(0L).displayName("Anonymous").build()
                : UserResponse.from(post.getUser());
        return PostResponse.builder()
                .id(post.getId())
                .author(authorResponse)
                .anonymous(anon)
                .communityId(post.getCommunity() != null ? post.getCommunity().getId() : null)
                .communityName(post.getCommunity() != null ? post.getCommunity().getName() : null)
                .content(post.getContent())
                .imageUrl(post.getImageUrl())
                .topicTags(post.getTopicTags())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
