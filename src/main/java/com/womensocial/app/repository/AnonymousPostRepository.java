package com.womensocial.app.repository;

import com.womensocial.app.model.entity.AnonymousPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AnonymousPostRepository extends JpaRepository<AnonymousPost, Long> {

    @Query("""
            SELECT ap FROM AnonymousPost ap
            WHERE ap.user.id NOT IN (
                SELECT b.blocked.id FROM Block b WHERE b.blocker.id = :currentUserId
            )
            ORDER BY ap.createdAt DESC
            """)
    Page<AnonymousPost> findAllExcludingBlocked(@Param("currentUserId") Long currentUserId, Pageable pageable);

    @Query(value = """
            SELECT ap.* FROM anonymous_posts ap
            WHERE ap.user_id NOT IN (
                SELECT b.blocked_id FROM blocks b WHERE b.blocker_id = :userId
            )
            AND (
                ap.topic_tags IS NULL
                OR ap.topic_tags = '{}'
                OR ap.topic_tags && (SELECT u.interests FROM users u WHERE u.id = :userId)
            )
            ORDER BY ap.created_at DESC
            """,
            countQuery = """
            SELECT COUNT(*) FROM anonymous_posts ap
            WHERE ap.user_id NOT IN (
                SELECT b.blocked_id FROM blocks b WHERE b.blocker_id = :userId
            )
            AND (
                ap.topic_tags IS NULL
                OR ap.topic_tags = '{}'
                OR ap.topic_tags && (SELECT u.interests FROM users u WHERE u.id = :userId)
            )
            """,
            nativeQuery = true)
    Page<AnonymousPost> findFeedForUser(@Param("userId") Long userId, Pageable pageable);

    @Query(value = "SELECT ap.* FROM anonymous_posts ap WHERE :tag = ANY(ap.topic_tags) ORDER BY ap.created_at DESC",
            countQuery = "SELECT COUNT(*) FROM anonymous_posts ap WHERE :tag = ANY(ap.topic_tags)",
            nativeQuery = true)
    Page<AnonymousPost> findByTag(@Param("tag") String tag, Pageable pageable);

    // Used internally by moderation only
    boolean existsByIdAndUserId(Long id, Long userId);
}
