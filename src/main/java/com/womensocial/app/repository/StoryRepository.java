package com.womensocial.app.repository;

import com.womensocial.app.model.entity.Story;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {
    Page<Story> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Story> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
