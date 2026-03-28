package com.womensocial.app.repository;

import com.womensocial.app.model.entity.Community;
import com.womensocial.app.model.enums.TopicCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Long> {
    boolean existsByName(String name);
    Page<Community> findByCategory(TopicCategory category, Pageable pageable);
}
