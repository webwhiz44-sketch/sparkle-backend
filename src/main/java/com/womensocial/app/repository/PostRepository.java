package com.womensocial.app.repository;

import com.womensocial.app.model.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByCommunityId(Long communityId, Pageable pageable);

    Page<Post> findByUserId(Long userId, Pageable pageable);

    @Query("""
            SELECT p FROM Post p
            WHERE p.user.id NOT IN (
                SELECT b.blocked.id FROM Block b WHERE b.blocker.id = :userId
            )
            ORDER BY p.createdAt DESC
            """)
    Page<Post> findFeedForUser(@Param("userId") Long userId, Pageable pageable);

    @Query(value = "SELECT p.* FROM posts p WHERE :tag = ANY(p.topic_tags) ORDER BY p.created_at DESC",
            countQuery = "SELECT COUNT(*) FROM posts p WHERE :tag = ANY(p.topic_tags)",
            nativeQuery = true)
    Page<Post> findByTag(@Param("tag") String tag, Pageable pageable);

    @Query(value = """
            SELECT tag FROM (
                SELECT unnest(topic_tags) AS tag FROM posts
                WHERE created_at > NOW() - INTERVAL '7 days'
                UNION ALL
                SELECT unnest(topic_tags) AS tag FROM anonymous_posts
                WHERE created_at > NOW() - INTERVAL '7 days'
            ) t
            GROUP BY tag
            ORDER BY COUNT(*) DESC
            LIMIT 10
            """, nativeQuery = true)
    List<String> findTrendingTags();
}
