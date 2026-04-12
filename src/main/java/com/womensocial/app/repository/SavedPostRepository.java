package com.womensocial.app.repository;

import com.womensocial.app.model.entity.SavedPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SavedPostRepository extends JpaRepository<SavedPost, Long> {

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    Optional<SavedPost> findByUserIdAndPostId(Long userId, Long postId);

    @Query("SELECT sp FROM SavedPost sp JOIN FETCH sp.post p JOIN FETCH p.user WHERE sp.user.id = :userId ORDER BY sp.savedAt DESC")
    Page<SavedPost> findByUserId(@Param("userId") Long userId, Pageable pageable);
}
