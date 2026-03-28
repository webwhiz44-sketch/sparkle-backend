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

    // Used internally by moderation only
    boolean existsByIdAndUserId(Long id, Long userId);
}
