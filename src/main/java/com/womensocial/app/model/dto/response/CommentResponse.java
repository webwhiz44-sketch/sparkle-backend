package com.womensocial.app.model.dto.response;

import com.womensocial.app.model.entity.Comment;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentResponse {

    private Long id;
    private UserResponse author;
    private String content;
    private int likeCount;
    private boolean likedByMe;
    private Long parentCommentId;
    private LocalDateTime createdAt;

    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .author(UserResponse.from(comment.getUser()))
                .content(comment.getContent())
                .likeCount(comment.getLikeCount())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
