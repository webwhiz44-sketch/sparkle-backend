package com.womensocial.app.repository;

import com.womensocial.app.model.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUserIdAndPostId(Long userId, Long postId);
    Optional<Like> findByUserIdAndAnonymousPostId(Long userId, Long anonymousPostId);
    Optional<Like> findByUserIdAndCommentId(Long userId, Long commentId);
    boolean existsByUserIdAndPostId(Long userId, Long postId);
    boolean existsByUserIdAndAnonymousPostId(Long userId, Long anonymousPostId);
    boolean existsByUserIdAndCommentId(Long userId, Long commentId);
    boolean existsByUserIdAndStoryId(Long userId, Long storyId);
    Optional<Like> findByUserIdAndStoryId(Long userId, Long storyId);
}
