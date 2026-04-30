package com.womensocial.app.repository;

import com.womensocial.app.model.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByPostIdAndParentCommentIsNull(Long postId, Pageable pageable);
    Page<Comment> findByAnonymousPostIdAndParentCommentIsNull(Long anonymousPostId, Pageable pageable);
    Page<Comment> findByStoryIdAndParentCommentIsNull(Long storyId, Pageable pageable);
}
