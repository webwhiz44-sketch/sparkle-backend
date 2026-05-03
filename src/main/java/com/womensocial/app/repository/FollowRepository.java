package com.womensocial.app.repository;

import com.womensocial.app.model.entity.Follow;
import com.womensocial.app.model.enums.FollowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    List<Follow> findByFollowingIdAndStatus(Long followingId, FollowStatus status);

    List<Follow> findByFollowerIdAndStatus(Long followerId, FollowStatus status);

    long countByFollowerIdAndStatus(Long followerId, FollowStatus status);

    long countByFollowingIdAndStatus(Long followingId, FollowStatus status);

    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);
}
