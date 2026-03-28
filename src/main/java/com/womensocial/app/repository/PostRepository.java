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
                SELECT b.blocked.id FROM Block b WHERE b.blocker.id = :currentUserId
            )
            AND (
                p.community.id IN (
                    SELECT cm.community.id FROM CommunityMember cm WHERE cm.user.id = :currentUserId
                )
                OR EXISTS (
                    SELECT 1 FROM Post p2 WHERE p2.id = p.id
                    AND p2.topicTags IS NOT NULL
                )
            )
            ORDER BY p.createdAt DESC
            """)
    Page<Post> findFeedForUser(@Param("currentUserId") Long currentUserId, Pageable pageable);
}
