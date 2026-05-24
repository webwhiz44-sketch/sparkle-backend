package com.womensocial.app.model.dto.response;

import com.womensocial.app.model.entity.AnonymousPost;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AnonymousPostResponse {

    private Long id;
    private String content;
    private String imageUrl;
    private List<String> topicTags;
    private int likeCount;
    private int commentCount;
    private boolean likedByMe;
    private boolean anonymous;
    private Long authorId;
    private String authorName;
    private String authorImageUrl;
    private PollResponse poll;
    private LocalDateTime createdAt;

    public static AnonymousPostResponse from(AnonymousPost post) {
        AnonymousPostResponseBuilder builder = AnonymousPostResponse.builder()
                .id(post.getId())
                .content(post.getContent())
                .imageUrl(post.getImageUrl())
                .topicTags(post.getTopicTags())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .anonymous(post.isAnonymous())
                .createdAt(post.getCreatedAt());

        if (post.getUser() != null) {
            // authorId always returned so the owner can delete their own post
            builder.authorId(post.getUser().getId());
            if (!post.isAnonymous()) {
                builder.authorName(post.getUser().getDisplayName())
                       .authorImageUrl(post.getUser().getProfileImageUrl());
            }
        }

        return builder.build();
    }
}
